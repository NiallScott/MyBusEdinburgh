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

package uk.org.rivernile.android.bustracker.ui.alerts

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.FakeAlertsRepository
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FakeFeatureRepository
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [RealUiAlertDropdownMenuItemMultipleStopsRetriever].
 *
 * @author Niall Scott
 */
class RealUiAlertDropdownMenuItemMultipleStopsRetrieverTest {

    @Test
    fun getUiArrivalAlertDropdownMenuItemsFlowWhenNoArrivalAlertFeatureEmitsNull() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { false }
            )
        )

        retriever.getUiArrivalAlertDropdownMenuItemsFlow(requestedStops).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiArrivalAlertDropdownMenuItemsFlowWhenRequestedStopsIsEmpty() = runTest {
        val retriever = createRetriever(
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { true }
            )
        )

        retriever.getUiArrivalAlertDropdownMenuItemsFlow(emptySet()).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiArrivalAlertDropdownMenuItemsFlowWhenArrivalAlertsIsNull() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = {
                    flowOf(null)
                }
            ),
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { true }
            )
        )

        retriever.getUiArrivalAlertDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiArrivalAlertDropdownMenuItem>(
                    "123456".toNaptanStopIdentifier() to UiArrivalAlertDropdownMenuItem(
                        hasArrivalAlert = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiArrivalAlertDropdownMenuItemsFlowWhenArrivalAlertsIsEmpty() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = { flowOf(emptySet()) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { true }
            )
        )

        retriever.getUiArrivalAlertDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiArrivalAlertDropdownMenuItem>(
                    "123456".toNaptanStopIdentifier() to UiArrivalAlertDropdownMenuItem(
                        hasArrivalAlert = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiArrivalAlertDropdownMenuItemsFlowWhenArrivalAlertsHasNoMatchingItems() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = {
                    flowOf(setOf("987654".toNaptanStopIdentifier()))
                }
            ),
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { true }
            )
        )

        retriever.getUiArrivalAlertDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiArrivalAlertDropdownMenuItem>(
                    "123456".toNaptanStopIdentifier() to UiArrivalAlertDropdownMenuItem(
                        hasArrivalAlert = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiArrivalAlertDropdownMenuItemsFlowWhenArrivalAlertsHasMatchingItem() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = {
                    flowOf(setOf("123456".toNaptanStopIdentifier()))
                }
            ),
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { true }
            )
        )

        retriever.getUiArrivalAlertDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiArrivalAlertDropdownMenuItem>(
                    "123456".toNaptanStopIdentifier() to UiArrivalAlertDropdownMenuItem(
                        hasArrivalAlert = true
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiArrivalAlertDropdownMenuItemsFlowWithMultipleStops() = runTest {
        val requestedStops = setOf(
            "1".toNaptanStopIdentifier(),
            "2".toNaptanStopIdentifier(),
            "3".toNaptanStopIdentifier(),
            "4".toNaptanStopIdentifier(),
            "5".toNaptanStopIdentifier()
        )
        val retriever = createRetriever(
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = {
                    flowOf(
                        setOf(
                            "2".toNaptanStopIdentifier(),
                            "4".toNaptanStopIdentifier()
                        )
                    )
                }
            ),
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { true }
            )
        )

        retriever.getUiArrivalAlertDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiArrivalAlertDropdownMenuItem>(
                    "1".toNaptanStopIdentifier() to UiArrivalAlertDropdownMenuItem(
                        hasArrivalAlert = false
                    ),
                    "2".toNaptanStopIdentifier() to UiArrivalAlertDropdownMenuItem(
                        hasArrivalAlert = true
                    ),
                    "3".toNaptanStopIdentifier() to UiArrivalAlertDropdownMenuItem(
                        hasArrivalAlert = false
                    ),
                    "4".toNaptanStopIdentifier() to UiArrivalAlertDropdownMenuItem(
                        hasArrivalAlert = true
                    ),
                    "5".toNaptanStopIdentifier() to UiArrivalAlertDropdownMenuItem(
                        hasArrivalAlert = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiProximityAlertDropdownMenuItemsFlowWhenNoProximityAlertFeatureEmitsNull() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            featureRepository = FakeFeatureRepository(
                onHasProximityAlertFeature = { false }
            )
        )

        retriever.getUiProximityAlertDropdownMenuItemsFlow(requestedStops).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiProximityAlertDropdownMenuItemsFlowWhenRequestedStopsIsEmpty() = runTest {
        val retriever = createRetriever(
            featureRepository = FakeFeatureRepository(
                onHasProximityAlertFeature = { true }
            )
        )

        retriever.getUiProximityAlertDropdownMenuItemsFlow(emptySet()).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiProximityAlertDropdownMenuItemsFlowWhenProximityAlertsIsNull() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            alertsRepository = FakeAlertsRepository(
                onProximityAlertStopIdentifiersFlow = {
                    flowOf(null)
                }
            ),
            featureRepository = FakeFeatureRepository(
                onHasProximityAlertFeature = { true }
            )
        )

        retriever.getUiProximityAlertDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiProximityAlertDropdownMenuItem>(
                    "123456".toNaptanStopIdentifier() to UiProximityAlertDropdownMenuItem(
                        hasProximityAlert = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiProximityAlertDropdownMenuItemsFlowWhenProximityAlertsIsEmpty() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            alertsRepository = FakeAlertsRepository(
                onProximityAlertStopIdentifiersFlow = { flowOf(emptySet()) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasProximityAlertFeature = { true }
            )
        )

        retriever.getUiProximityAlertDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiProximityAlertDropdownMenuItem>(
                    "123456".toNaptanStopIdentifier() to UiProximityAlertDropdownMenuItem(
                        hasProximityAlert = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiProximityAlertDropdownMenuItemsFlowWhenProximityAlertsHasNoMatchingItems() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            alertsRepository = FakeAlertsRepository(
                onProximityAlertStopIdentifiersFlow = {
                    flowOf(setOf("987654".toNaptanStopIdentifier()))
                }
            ),
            featureRepository = FakeFeatureRepository(
                onHasProximityAlertFeature = { true }
            )
        )

        retriever.getUiProximityAlertDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiProximityAlertDropdownMenuItem>(
                    "123456".toNaptanStopIdentifier() to UiProximityAlertDropdownMenuItem(
                        hasProximityAlert = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiProximityAlertDropdownMenuItemsFlowWhenProximityAlertsHasMatchingItem() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            alertsRepository = FakeAlertsRepository(
                onProximityAlertStopIdentifiersFlow = {
                    flowOf(setOf("123456".toNaptanStopIdentifier()))
                }
            ),
            featureRepository = FakeFeatureRepository(
                onHasProximityAlertFeature = { true }
            )
        )

        retriever.getUiProximityAlertDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiProximityAlertDropdownMenuItem>(
                    "123456".toNaptanStopIdentifier() to UiProximityAlertDropdownMenuItem(
                        hasProximityAlert = true
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiProximityAlertDropdownMenuItemsFlowWithMultipleStops() = runTest {
        val requestedStops = setOf(
            "1".toNaptanStopIdentifier(),
            "2".toNaptanStopIdentifier(),
            "3".toNaptanStopIdentifier(),
            "4".toNaptanStopIdentifier(),
            "5".toNaptanStopIdentifier()
        )
        val retriever = createRetriever(
            alertsRepository = FakeAlertsRepository(
                onProximityAlertStopIdentifiersFlow = {
                    flowOf(
                        setOf(
                            "2".toNaptanStopIdentifier(),
                            "4".toNaptanStopIdentifier()
                        )
                    )
                }
            ),
            featureRepository = FakeFeatureRepository(
                onHasProximityAlertFeature = { true }
            )
        )

        retriever.getUiProximityAlertDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiProximityAlertDropdownMenuItem>(
                    "1".toNaptanStopIdentifier() to UiProximityAlertDropdownMenuItem(
                        hasProximityAlert = false
                    ),
                    "2".toNaptanStopIdentifier() to UiProximityAlertDropdownMenuItem(
                        hasProximityAlert = true
                    ),
                    "3".toNaptanStopIdentifier() to UiProximityAlertDropdownMenuItem(
                        hasProximityAlert = false
                    ),
                    "4".toNaptanStopIdentifier() to UiProximityAlertDropdownMenuItem(
                        hasProximityAlert = true
                    ),
                    "5".toNaptanStopIdentifier() to UiProximityAlertDropdownMenuItem(
                        hasProximityAlert = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createRetriever(
        alertsRepository: AlertsRepository = FakeAlertsRepository(),
        featureRepository: FeatureRepository = FakeFeatureRepository()
    ): RealUiAlertDropdownMenuItemMultipleStopsRetriever {
        return RealUiAlertDropdownMenuItemMultipleStopsRetriever(
            alertsRepository = alertsRepository,
            featureRepository = featureRepository
        )
    }
}
