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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toAtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [RealUiContentRetriever].
 *
 * @author Niall Scott
 */
class RealUiContentRetrieverTest {

    @Test
    fun uiContentFlowEmitsNoStopDetailsErrorWhenStopCodeIsNull() = runTest {
        val retriever = createRetriever(
            arguments = FakeArguments(
                onStopIdentifierFlow = { flowOf(null) }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.NoStopDetailsError, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsNoStopDetailsWhenStopDetailsAreNull() = runTest {
        val retriever = createRetriever(
            arguments = FakeArguments(
                onStopIdentifierFlow = { flowOf("123456".toNaptanStopIdentifier()) }
            ),
            stopDetailsRetriever = FakeUiStopDetailsRetriever(
                onGetUiStopDetailsFlow = {
                    assertEquals("123456".toNaptanStopIdentifier(), it)
                    flowOf(null)
                }
            ),
            servicesItemsRetriever = FakeUiServicesItemsRetriever(
                onGetUiServicesItemsFlow = {
                    assertEquals("123456".toNaptanStopIdentifier(), it)
                    flowOf(
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
                            )
                        )
                    )
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(UiContent.NoStopDetailsError, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiContentFlowEmitsContentWhenStopDetailsIsNotNull() = runTest {
        val retriever = createRetriever(
            arguments = FakeArguments(
                onStopIdentifierFlow = { flowOf("123456".toNaptanStopIdentifier()) }
            ),
            stopDetailsRetriever = FakeUiStopDetailsRetriever(
                onGetUiStopDetailsFlow = {
                    assertEquals("123456".toNaptanStopIdentifier(), it)
                    flowOf(
                        UiStopDetails(
                            naptanCode = "123456".toNaptanStopIdentifier(),
                            atcoCode = "atco123456".toAtcoStopIdentifier(),
                            latLon = UiLatLon(
                                latitude = 1.1,
                                longitude = 2.2
                            ),
                            orientation = StopOrientation.NORTH_EAST,
                            stopDistance = UiStopDistance.Distance.Meters(
                                distance = 123
                            ),
                            isMapShown = true
                        )
                    )
                }
            ),
            servicesItemsRetriever = FakeUiServicesItemsRetriever(
                onGetUiServicesItemsFlow = {
                    assertEquals("123456".toNaptanStopIdentifier(), it)
                    flowOf(
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
                            )
                        )
                    )
                }
            )
        )

        retriever.uiContentFlow.test {
            assertEquals(
                UiContent.Content(
                    stopDetails = UiStopDetails(
                        naptanCode = "123456".toNaptanStopIdentifier(),
                        atcoCode = "atco123456".toAtcoStopIdentifier(),
                        latLon = UiLatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        orientation = StopOrientation.NORTH_EAST,
                        stopDistance = UiStopDistance.Distance.Meters(
                            distance = 123
                        ),
                        isMapShown = true
                    ),
                    servicesItems = persistentListOf(
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
                        )
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createRetriever(
        arguments: Arguments = FakeArguments(),
        stopDetailsRetriever: UiStopDetailsRetriever = FakeUiStopDetailsRetriever(),
        servicesItemsRetriever: UiServicesItemsRetriever = FakeUiServicesItemsRetriever()
    ): RealUiContentRetriever {
        return RealUiContentRetriever(
            arguments = arguments,
            stopDetailsRetriever = stopDetailsRetriever,
            servicesItemsRetriever = servicesItemsRetriever
        )
    }
}
