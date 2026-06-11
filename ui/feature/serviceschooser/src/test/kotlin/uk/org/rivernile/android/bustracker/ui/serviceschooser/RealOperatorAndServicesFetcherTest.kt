/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
 *
 * This software is provided 'as-is', without any express or implied
 * warranty.  In no event will the authors or contributors be held liable for
 * any damages arising from the use of this software.
 *
 * The aforementioned copyright holder(s) hereby grant you a
 * non-transferrable right to use this software for any purpose (including
 * commercial applications), and to modify it and redistribute it, subject to
 * the following conditions:
 *
 *  1. This notice may not be removed or altered from any file it appears in.
 *
 *  2. Any modifications made to this software, except those defined in
 *     clause 3 of this agreement, must be released under this license, and
 *     the source code of any modifications must be made available on a
 *     publically accessible (and locateable) website, or sent to the
 *     original author of this software.
 *
 *  3. Software modifications that do not alter the functionality of the
 *     software but are simply adaptations to a specific environment are
 *     exempt from clause 2.
 *
 */

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toParcelableNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.operators.FakeOperatorsRepository
import uk.org.rivernile.android.bustracker.core.operators.OperatorName
import uk.org.rivernile.android.bustracker.core.operators.OperatorsRepository
import uk.org.rivernile.android.bustracker.core.services.FakeServicesRepository
import uk.org.rivernile.android.bustracker.core.services.ServiceWithColour
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [RealOperatorAndServicesFetcher].
 *
 * @author Niall Scott
 */
class RealOperatorAndServicesFetcherTest {

    @Test
    fun operatorAndServicesFlowEmitsNullWhenParamsIsNull() = runTest {
        val fetcher = createFetcher(
            arguments = FakeArguments(
                onParamsFlow = { flowOf(null) }
            )
        )

        fetcher.operatorAndServicesFlow.test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun operatorAndServicesFlowEmitsMapWhenParamsIsAllServices() = runTest {
        val fetcher = createFetcher(
            arguments = FakeArguments(
                onParamsFlow = {
                    flowOf(
                        ServicesChooserParams.AllServices(
                            titleResId = 0,
                            selectedServices = null
                        )
                    )
                }
            ),
            operatorsRepository = FakeOperatorsRepository(
                onAllOperatorNamesFlow = { flowOf(operatorsNames) }
            ),
            servicesRepository = FakeServicesRepository(
                onAllServiceNamesWithColourFlow = { flowOf(services) }
            )
        )

        fetcher.operatorAndServicesFlow.test {
            assertEquals(expectedOperatorAndServices, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun operatorAndServicesFlowEmitsMapWhenParamsIsStop() = runTest {
        val fetcher = createFetcher(
            arguments = FakeArguments(
                onParamsFlow = {
                    flowOf(
                        ServicesChooserParams.Stop(
                            titleResId = 0,
                            selectedServices = null,
                            stopIdentifier = "123456".toParcelableNaptanStopIdentifier()
                        )
                    )
                }
            ),
            operatorsRepository = FakeOperatorsRepository(
                onAllOperatorNamesFlow = { flowOf(operatorsNames) }
            ),
            servicesRepository = FakeServicesRepository(
                onGetServiceNamesWithColourFlow = { flowOf(services) }
            )
        )

        fetcher.operatorAndServicesFlow.test {
            assertEquals(expectedOperatorAndServices, awaitItem())
            awaitComplete()
        }
    }

    private fun createFetcher(
        arguments: Arguments = FakeArguments(),
        operatorsRepository: OperatorsRepository = FakeOperatorsRepository(),
        servicesRepository: ServicesRepository = FakeServicesRepository()
    ): RealOperatorAndServicesFetcher {
        return RealOperatorAndServicesFetcher(
            arguments = arguments,
            operatorsRepository = operatorsRepository,
            servicesRepository = servicesRepository
        )
    }

    private val operatorsNames get() = mapOf(
        "TEST1" to OperatorName("Test 1"),
        "TEST2" to OperatorName("Test 2")
    )

    private val service1 get() = ServiceWithColour(
        serviceDescriptor = ServiceDescriptor(
            serviceName = "1",
            operatorCode = "TEST1"
        ),
        colours = null
    )

    private val service2 get() = ServiceWithColour(
        serviceDescriptor = ServiceDescriptor(
            serviceName = "12",
            operatorCode = "TEST1"
        ),
        colours = null
    )

    private val service3 get() = ServiceWithColour(
        serviceDescriptor = ServiceDescriptor(
            serviceName = "3",
            operatorCode = "TEST2"
        ),
        colours = null
    )

    private val services = listOf(service1, service2, service3)

    private val expectedOperatorAndServices get() =
        mapOf<UiServiceChooserItem.Operator, List<ServiceWithColour>>(
            UiServiceChooserItem.Operator.Named(
                operatorId = "TEST1",
                operatorName = "Test 1"
            ) to listOf(service1, service2),
            UiServiceChooserItem.Operator.Named(
                operatorId = "TEST2",
                operatorName = "Test 2"
            ) to listOf(service3)
        )
}
