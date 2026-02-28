/*
 * Copyright (C) 2025 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favouritestops

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FakeFeatureRepository
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.ui.alerts.FakeUiAlertDropdownMenuItemMultipleStopsRetriever
import uk.org.rivernile.android.bustracker.ui.alerts.UiAlertDropdownMenuItemMultipleStopsRetriever
import uk.org.rivernile.android.bustracker.ui.alerts.UiArrivalAlertDropdownMenuItem
import uk.org.rivernile.android.bustracker.ui.alerts.UiProximityAlertDropdownMenuItem
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [RealUiFavouriteDropdownMenuGenerator].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class RealUiFavouriteDropdownMenuGeneratorTest {

    @Test
    fun getDropdownMenuItemsForStopsFlowWhenStopsIsEmptyEmitsNull() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            state = FakeState(
                onSelectedStopIdentifierFlow = { flowOf(null) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertMenuItemsRetriever = FakeUiAlertDropdownMenuItemMultipleStopsRetriever(
                onGetUiArrivalAlertDropdownMenuItemsFlow = { flowOf(null) },
                onGetUiProximityAlertDropdownMenuItemsFlow = { flowOf(null) }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(emptySet()).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getDropdownMenuItemsForStopsFlowWhenInShortcutModeEmitsNull() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(true) }
            ),
            state = FakeState(
                onSelectedStopIdentifierFlow = { flowOf(null) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertMenuItemsRetriever = FakeUiAlertDropdownMenuItemMultipleStopsRetriever(
                onGetUiArrivalAlertDropdownMenuItemsFlow = { flowOf(null) },
                onGetUiProximityAlertDropdownMenuItemsFlow = { flowOf(null) }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getDropdownMenuItemsForStopsFlowWhenHasPinShortcutFeatureDisabled() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            state = FakeState(
                onSelectedStopIdentifierFlow = { flowOf(null) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { false }
            ),
            alertMenuItemsRetriever = FakeUiAlertDropdownMenuItemMultipleStopsRetriever(
                onGetUiArrivalAlertDropdownMenuItemsFlow = { flowOf(null) },
                onGetUiProximityAlertDropdownMenuItemsFlow = { flowOf(null) }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = false,
                        arrivalAlertDropdownItem = null,
                        proximityAlertDropdownItem = null,
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getDropdownMenuItemsForStopsFlowWhenStopMapFeatureDisabled() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            state = FakeState(
                onSelectedStopIdentifierFlow = { flowOf(null) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { false },
                onHasPinShortcutFeature = { true }
            ),
            alertMenuItemsRetriever = FakeUiAlertDropdownMenuItemMultipleStopsRetriever(
                onGetUiArrivalAlertDropdownMenuItemsFlow = { flowOf(null) },
                onGetUiProximityAlertDropdownMenuItemsFlow = { flowOf(null) }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = null,
                        proximityAlertDropdownItem = null,
                        isStopMapItemShown = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getDropdownMenuItemsForStopsFlowWhenAlertsNull() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            state = FakeState(
                onSelectedStopIdentifierFlow = { flowOf(null) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertMenuItemsRetriever = FakeUiAlertDropdownMenuItemMultipleStopsRetriever(
                onGetUiArrivalAlertDropdownMenuItemsFlow = { flowOf(null) },
                onGetUiProximityAlertDropdownMenuItemsFlow = { flowOf(null) }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = null,
                        proximityAlertDropdownItem = null,
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getDropdownMenuItemsForStopsFlowWhenAlertsEmpty() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            state = FakeState(
                onSelectedStopIdentifierFlow = { flowOf(null) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertMenuItemsRetriever = FakeUiAlertDropdownMenuItemMultipleStopsRetriever(
                onGetUiArrivalAlertDropdownMenuItemsFlow = { flowOf(emptyMap()) },
                onGetUiProximityAlertDropdownMenuItemsFlow = { flowOf(emptyMap()) }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = null,
                        proximityAlertDropdownItem = null,
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getDropdownMenuItemsForStopsFlowWhenAlertsDoNotContainStop() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            state = FakeState(
                onSelectedStopIdentifierFlow = { flowOf(null) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertMenuItemsRetriever = FakeUiAlertDropdownMenuItemMultipleStopsRetriever(
                onGetUiArrivalAlertDropdownMenuItemsFlow = {
                    flowOf(
                        mapOf(
                            "987654".toNaptanStopIdentifier() to UiArrivalAlertDropdownMenuItem(
                                hasArrivalAlert = false
                            )
                        )
                    )
               },
                onGetUiProximityAlertDropdownMenuItemsFlow = {
                    flowOf(
                        mapOf(
                            "987654".toNaptanStopIdentifier() to UiProximityAlertDropdownMenuItem(
                                hasProximityAlert = false
                            )
                        )
                    )
                }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = null,
                        proximityAlertDropdownItem = null,
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getDropdownMenuItemsForStopsFlowWhenAlertsContainStop() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            state = FakeState(
                onSelectedStopIdentifierFlow = { flowOf(null) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertMenuItemsRetriever = FakeUiAlertDropdownMenuItemMultipleStopsRetriever(
                onGetUiArrivalAlertDropdownMenuItemsFlow = {
                    flowOf(
                        mapOf(
                            "123456".toNaptanStopIdentifier() to UiArrivalAlertDropdownMenuItem(
                                hasArrivalAlert = true
                            )
                        )
                    )
                },
                onGetUiProximityAlertDropdownMenuItemsFlow = {
                    flowOf(
                        mapOf(
                            "123456".toNaptanStopIdentifier() to UiProximityAlertDropdownMenuItem(
                                hasProximityAlert = true
                            )
                        )
                    )
                }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = UiArrivalAlertDropdownMenuItem(
                            hasArrivalAlert = true
                        ),
                        proximityAlertDropdownItem = UiProximityAlertDropdownMenuItem(
                            hasProximityAlert = true
                        ),
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getDropdownMenuItemsForStopsFlowWhenSelectedStopDoesNotMatch() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            state = FakeState(
                onSelectedStopIdentifierFlow = { flowOf("987654".toNaptanStopIdentifier()) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertMenuItemsRetriever = FakeUiAlertDropdownMenuItemMultipleStopsRetriever(
                onGetUiArrivalAlertDropdownMenuItemsFlow = { flowOf(null) },
                onGetUiProximityAlertDropdownMenuItemsFlow = { flowOf(null) }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = null,
                        proximityAlertDropdownItem = null,
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getDropdownMenuItemsForStopsFlowWhenSelectedStopMatches() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            state = FakeState(
                onSelectedStopIdentifierFlow = { flowOf("123456".toNaptanStopIdentifier()) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertMenuItemsRetriever = FakeUiAlertDropdownMenuItemMultipleStopsRetriever(
                onGetUiArrivalAlertDropdownMenuItemsFlow = { flowOf(null) },
                onGetUiProximityAlertDropdownMenuItemsFlow = { flowOf(null) }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = true,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = null,
                        proximityAlertDropdownItem = null,
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun TestScope.createUiFavouriteDropdownItemsGenerator(
        arguments: Arguments = FakeArguments(),
        state: State = FakeState(),
        featureRepository: FeatureRepository = FakeFeatureRepository(),
        alertMenuItemsRetriever: UiAlertDropdownMenuItemMultipleStopsRetriever =
            FakeUiAlertDropdownMenuItemMultipleStopsRetriever()
    ): RealUiFavouriteDropdownMenuGenerator {
        return RealUiFavouriteDropdownMenuGenerator(
            arguments = arguments,
            state = state,
            featureRepository = featureRepository,
            alertMenuItemsRetriever = alertMenuItemsRetriever,
            defaultCoroutineDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelCoroutineScope = backgroundScope
        )
    }
}
