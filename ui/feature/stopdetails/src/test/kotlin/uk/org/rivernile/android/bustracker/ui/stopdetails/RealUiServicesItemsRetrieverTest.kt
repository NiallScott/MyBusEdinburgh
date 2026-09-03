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

package uk.org.rivernile.android.bustracker.ui.stopdetails

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.operators.FakeOperatorsRepository
import uk.org.rivernile.android.bustracker.core.operators.OperatorName
import uk.org.rivernile.android.bustracker.core.operators.OperatorsRepository
import uk.org.rivernile.android.bustracker.core.services.FakeServicesRepository
import uk.org.rivernile.android.bustracker.core.services.ServiceDetails
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [RealUiServicesItemsRetriever].
 *
 * @author Niall Scott
 */
class RealUiServicesItemsRetrieverTest {

    @Test
    fun getUiServicesItemsFlowEmitsNoServicesWhenServicesIsNull() = runTest {
        val retriever = createRetriever(
            operatorsRepository = FakeOperatorsRepository(
                onAllOperatorNamesFlow = {
                    flowOf(
                        mapOf("TEST1" to OperatorName(displayName = "Test 1"))
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetServiceDetailsFlow = {
                    assertEquals("123456".toNaptanStopIdentifier(), it)
                    flowOf(null)
                }
            )
        )

        retriever
            .getUiServicesItemsFlow(stopIdentifier = "123456".toNaptanStopIdentifier())
            .test {
                assertEquals(listOf(UiServicesItem.NoServices), awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun getUiServicesItemsFlowEmitsNoServicesWhenServicesIsEmpty() = runTest {
        val retriever = createRetriever(
            operatorsRepository = FakeOperatorsRepository(
                onAllOperatorNamesFlow = {
                    flowOf(
                        mapOf("TEST1" to OperatorName(displayName = "Test 1"))
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetServiceDetailsFlow = {
                    assertEquals("123456".toNaptanStopIdentifier(), it)
                    flowOf(emptyList())
                }
            )
        )

        retriever
            .getUiServicesItemsFlow(stopIdentifier = "123456".toNaptanStopIdentifier())
            .test {
                assertEquals(listOf(UiServicesItem.NoServices), awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun getUiServicesItemsFlowEmitsServicesInCorrectOrder() = runTest {
        val retriever = createRetriever(
            operatorsRepository = FakeOperatorsRepository(
                onAllOperatorNamesFlow = {
                    flowOf(
                        mapOf(
                            "TEST1" to OperatorName("Test 1"),
                            "TEST2" to OperatorName("Test 2")
                        )
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetServiceDetailsFlow = {
                    assertEquals("123456".toNaptanStopIdentifier(), it)
                    flowOf(
                        listOf(
                            ServiceDetails(
                                serviceDescriptor = ServiceDescriptor(
                                    serviceName = "100",
                                    operatorCode = "UNKNOWN"
                                ),
                                description = "Description 100",
                                colours = null
                            ),
                            ServiceDetails(
                                serviceDescriptor = ServiceDescriptor(
                                    serviceName = "4",
                                    operatorCode = "TEST2"
                                ),
                                description = "Description 4",
                                colours = null
                            ),
                            ServiceDetails(
                                serviceDescriptor = ServiceDescriptor(
                                    serviceName = "6",
                                    operatorCode = "TEST2"
                                ),
                                description = null,
                                colours = null
                            ),
                            ServiceDetails(
                                serviceDescriptor = ServiceDescriptor(
                                    serviceName = "5",
                                    operatorCode = "TEST2"
                                ),
                                description = "Description 5",
                                colours = null
                            ),
                            ServiceDetails(
                                serviceDescriptor = ServiceDescriptor(
                                    serviceName = "3",
                                    operatorCode = "TEST1"
                                ),
                                description = "Description 3",
                                colours = null
                            ),
                            ServiceDetails(
                                serviceDescriptor = ServiceDescriptor(
                                    serviceName = "2",
                                    operatorCode = "TEST1"
                                ),
                                description = "Description 2",
                                colours = null
                            ),
                            ServiceDetails(
                                serviceDescriptor = ServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                ),
                                description = "Description 1",
                                colours = null
                            )
                        )
                    )
                }
            )
        )

        retriever
            .getUiServicesItemsFlow("123456".toNaptanStopIdentifier())
            .test {
                assertEquals(
                    listOf(
                        UiServicesItem.Operator.Named(
                            operatorId = "TEST1",
                            operatorName = "Test 1"
                        ),
                        UiServicesItem.Service(
                            serviceDescriptor = ServiceDescriptor(
                                serviceName = "1",
                                operatorCode = "TEST1"
                            ),
                            serviceName = UiServiceName(
                                serviceName = "1",
                                colours = null
                            ),
                            description = "Description 1"
                        ),
                        UiServicesItem.Service(
                            serviceDescriptor = ServiceDescriptor(
                                serviceName = "2",
                                operatorCode = "TEST1"
                            ),
                            serviceName = UiServiceName(
                                serviceName = "2",
                                colours = null
                            ),
                            description = "Description 2"
                        ),
                        UiServicesItem.Service(
                            serviceDescriptor = ServiceDescriptor(
                                serviceName = "3",
                                operatorCode = "TEST1"
                            ),
                            serviceName = UiServiceName(
                                serviceName = "3",
                                colours = null
                            ),
                            description = "Description 3"
                        ),
                        UiServicesItem.Operator.Named(
                            operatorId = "TEST2",
                            operatorName = "Test 2"
                        ),
                        UiServicesItem.Service(
                            serviceDescriptor = ServiceDescriptor(
                                serviceName = "4",
                                operatorCode = "TEST2"
                            ),
                            serviceName = UiServiceName(
                                serviceName = "4",
                                colours = null
                            ),
                            description = "Description 4"
                        ),
                        UiServicesItem.Service(
                            serviceDescriptor = ServiceDescriptor(
                                serviceName = "5",
                                operatorCode = "TEST2"
                            ),
                            serviceName = UiServiceName(
                                serviceName = "5",
                                colours = null
                            ),
                            description = "Description 5"
                        ),
                        UiServicesItem.Service(
                            serviceDescriptor = ServiceDescriptor(
                                serviceName = "6",
                                operatorCode = "TEST2"
                            ),
                            serviceName = UiServiceName(
                                serviceName = "6",
                                colours = null
                            ),
                            description = null
                        ),
                        UiServicesItem.Operator.Unknown,
                        UiServicesItem.Service(
                            serviceDescriptor = ServiceDescriptor(
                                serviceName = "100",
                                operatorCode = "UNKNOWN"
                            ),
                            serviceName = UiServiceName(
                                serviceName = "100",
                                colours = null
                            ),
                            description = "Description 100"
                        )
                    ),
                    awaitItem()
                )
                awaitComplete()
            }
    }

    private fun createRetriever(
        operatorsRepository: OperatorsRepository = FakeOperatorsRepository(),
        servicesRepository: ServicesRepository = FakeServicesRepository(),
        alphanumericComparator: Comparator<String> = naturalOrder()
    ): RealUiServicesItemsRetriever {
        return RealUiServicesItemsRetriever(
            operatorsRepository = operatorsRepository,
            servicesRepository = servicesRepository,
            alphanumericComparator = alphanumericComparator
        )
    }
}
