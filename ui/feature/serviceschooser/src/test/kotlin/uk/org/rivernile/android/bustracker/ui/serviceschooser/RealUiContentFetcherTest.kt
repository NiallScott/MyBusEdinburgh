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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toParcelableNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.services.ServiceWithColour
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [RealUiContentFetcher].
 *
 * @author Niall Scott
 */
class RealUiContentFetcherTest {

    @Test
    fun uiContentFlowWithNullParamsEmitsInProgress() = runTest {
        val fetcher = createFetcher(
            arguments = FakeArguments(
                onParamsFlow = { flowOf(null) }
            ),
            state = FakeState(
                onSelectedServicesFlow = { flowOf(emptySet()) }
            ),
            operatorAndServicesFetcher = FakeOperatorAndServicesFetcher(
                onOperatorAndServicesFlow = { flowOf(null) }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowInAllServicesModeWhenNullServicesEmitsNoGlobalServices() = runTest {
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
            state = FakeState(
                onSelectedServicesFlow = { flowOf(emptySet()) }
            ),
            operatorAndServicesFetcher = FakeOperatorAndServicesFetcher(
                onOperatorAndServicesFlow = { flowOf(null) }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(UiContent.Error.NoGlobalServices, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowInAllServicesModeWhenEmptyServicesEmitsNoGlobalServices() = runTest {
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
            state = FakeState(
                onSelectedServicesFlow = { flowOf(emptySet()) }
            ),
            operatorAndServicesFetcher = FakeOperatorAndServicesFetcher(
                onOperatorAndServicesFlow = { flowOf(emptyMap()) }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(UiContent.Error.NoGlobalServices, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowInStopModeWhenNullServicesEmitsNoGlobalServices() = runTest {
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
            state = FakeState(
                onSelectedServicesFlow = { flowOf(emptySet()) }
            ),
            operatorAndServicesFetcher = FakeOperatorAndServicesFetcher(
                onOperatorAndServicesFlow = { flowOf(null) }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(UiContent.Error.NoServicesForStop, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowInStopModeWhenEmptyServicesEmitsNoGlobalServices() = runTest {
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
            state = FakeState(
                onSelectedServicesFlow = { flowOf(emptySet()) }
            ),
            operatorAndServicesFetcher = FakeOperatorAndServicesFetcher(
                onOperatorAndServicesFlow = { flowOf(emptyMap()) }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(UiContent.Error.NoServicesForStop, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsContentWithItemsWhenHasServicesAndNoServicesSelected() = runTest {
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
            state = FakeState(
                onSelectedServicesFlow = { flowOf(emptySet()) }
            ),
            operatorAndServicesFetcher = FakeOperatorAndServicesFetcher(
                onOperatorAndServicesFlow = { flowOf(operatorsAndServices) }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Content(
                    persistentListOf(
                        UiServiceChooserItem.Operator.Named(
                            operatorId = "TEST1",
                            operatorName = "Test 1"
                        ),
                        UiServiceChooserItem.Service(
                            serviceDescriptor = ServiceDescriptor(
                                serviceName = "1",
                                operatorCode = "TEST1"
                            ),
                            serviceName = UiServiceName(
                                serviceName = "1",
                                colours = null
                            ),
                            isSelected = false
                        ),
                        UiServiceChooserItem.Service(
                            serviceDescriptor = ServiceDescriptor(
                                serviceName = "2",
                                operatorCode = "TEST1"
                            ),
                            serviceName = UiServiceName(
                                serviceName = "2",
                                colours = null
                            ),
                            isSelected = false
                        ),
                        UiServiceChooserItem.Operator.Named(
                            operatorId = "TEST2",
                            operatorName = "Test 2"
                        ),
                        UiServiceChooserItem.Service(
                            serviceDescriptor = ServiceDescriptor(
                                serviceName = "3",
                                operatorCode = "TEST2"
                            ),
                            serviceName = UiServiceName(
                                serviceName = "3",
                                colours = null
                            ),
                            isSelected = false
                        )
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsContentWithItemsWhenHasServicesAndHasSelectedServices() = runTest {
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
            state = FakeState(
                onSelectedServicesFlow = {
                    flowOf(
                        setOf(
                            ServiceDescriptor(
                                serviceName = "2",
                                operatorCode = "TEST1"
                            ),
                            ServiceDescriptor(
                                serviceName = "4",
                                operatorCode = "TEST2"
                            )
                        )
                    )
                }
            ),
            operatorAndServicesFetcher = FakeOperatorAndServicesFetcher(
                onOperatorAndServicesFlow = { flowOf(operatorsAndServices) }
            )
        )

        fetcher.uiContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                UiContent.Content(
                    persistentListOf(
                        UiServiceChooserItem.Operator.Named(
                            operatorId = "TEST1",
                            operatorName = "Test 1"
                        ),
                        UiServiceChooserItem.Service(
                            serviceDescriptor = ServiceDescriptor(
                                serviceName = "1",
                                operatorCode = "TEST1"
                            ),
                            serviceName = UiServiceName(
                                serviceName = "1",
                                colours = null
                            ),
                            isSelected = false
                        ),
                        UiServiceChooserItem.Service(
                            serviceDescriptor = ServiceDescriptor(
                                serviceName = "2",
                                operatorCode = "TEST1"
                            ),
                            serviceName = UiServiceName(
                                serviceName = "2",
                                colours = null
                            ),
                            isSelected = true
                        ),
                        UiServiceChooserItem.Operator.Named(
                            operatorId = "TEST2",
                            operatorName = "Test 2"
                        ),
                        UiServiceChooserItem.Service(
                            serviceDescriptor = ServiceDescriptor(
                                serviceName = "3",
                                operatorCode = "TEST2"
                            ),
                            serviceName = UiServiceName(
                                serviceName = "3",
                                colours = null
                            ),
                            isSelected = false
                        )
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createFetcher(
        arguments: Arguments = FakeArguments(),
        state: State = FakeState(),
        operatorAndServicesFetcher: OperatorAndServicesFetcher = FakeOperatorAndServicesFetcher(),
        comparator: Comparator<String> = naturalOrder()
    ): RealUiContentFetcher {
        return RealUiContentFetcher(
            arguments = arguments,
            state = state,
            operatorAndServicesFetcher = operatorAndServicesFetcher,
            comparator = comparator
        )
    }

    private val service1 get() = ServiceWithColour(
        serviceDescriptor = ServiceDescriptor(
            serviceName = "1",
            operatorCode = "TEST1"
        ),
        colours = null
    )

    private val service2 get() = ServiceWithColour(
        serviceDescriptor = ServiceDescriptor(
            serviceName = "2",
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

    private val operatorsAndServices get() =
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
