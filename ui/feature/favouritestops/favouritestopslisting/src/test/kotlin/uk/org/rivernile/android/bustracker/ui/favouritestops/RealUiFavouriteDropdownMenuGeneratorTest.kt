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
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.FakeAlertsRepository
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FakeFeatureRepository
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.fail

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
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = { flowOf(null) },
                onProximityAlertStopIdentifiersFlow = { flowOf(null) }
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
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = { flowOf(null) },
                onProximityAlertStopIdentifiersFlow = { flowOf(null) }
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
                onHasArrivalAlertFeature = { false },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { false }
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = {
                    fail("Not expecting to get the arrival alerts stop identifiers Flow.")
                },
                onProximityAlertStopIdentifiersFlow = { flowOf(null) }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = false,
                        arrivalAlertDropdownItem = null,
                        proximityAlertDropdownItem = UiProximityAlertDropdownItem(
                            hasProximityAlert = false
                        ),
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun getDropdownMenuItemsForStopsFlowWhenArrivalAlertsDisabled() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            state = FakeState(
                onSelectedStopIdentifierFlow = { flowOf(null) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { false },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = {
                    fail("Not expecting to get the arrival alerts stop identifiers Flow.")
                },
                onProximityAlertStopIdentifiersFlow = { flowOf(null) }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = null,
                        proximityAlertDropdownItem = UiProximityAlertDropdownItem(
                            hasProximityAlert = false
                        ),
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun getDropdownMenuItemsForStopsFlowWhenProximityAlertsDisabled() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            state = FakeState(
                onSelectedStopIdentifierFlow = { flowOf(null) }
            ),
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { false },
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = { flowOf(null) },
                onProximityAlertStopIdentifiersFlow = {
                    fail("Not expecting to get the arrival alerts stop identifiers Flow.")
                }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = UiArrivalAlertDropdownItem(
                            hasArrivalAlert = false
                        ),
                        proximityAlertDropdownItem = null,
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
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
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { false },
                onHasPinShortcutFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = { flowOf(null) },
                onProximityAlertStopIdentifiersFlow = { flowOf(null) }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = UiArrivalAlertDropdownItem(
                            hasArrivalAlert = false
                        ),
                        proximityAlertDropdownItem = UiProximityAlertDropdownItem(
                            hasProximityAlert = false
                        ),
                        isStopMapItemShown = false
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
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
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = { flowOf(null) },
                onProximityAlertStopIdentifiersFlow = { flowOf(null) }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = UiArrivalAlertDropdownItem(
                            hasArrivalAlert = false
                        ),
                        proximityAlertDropdownItem = UiProximityAlertDropdownItem(
                            hasProximityAlert = false
                        ),
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
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
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = { flowOf(emptySet()) },
                onProximityAlertStopIdentifiersFlow = { flowOf(emptySet()) }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = UiArrivalAlertDropdownItem(
                            hasArrivalAlert = false
                        ),
                        proximityAlertDropdownItem = UiProximityAlertDropdownItem(
                            hasProximityAlert = false
                        ),
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
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
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = {
                    flowOf(setOf("987654".toNaptanStopIdentifier()))
                },
                onProximityAlertStopIdentifiersFlow = {
                    flowOf(setOf("987654".toNaptanStopIdentifier()))
                }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = UiArrivalAlertDropdownItem(
                            hasArrivalAlert = false
                        ),
                        proximityAlertDropdownItem = UiProximityAlertDropdownItem(
                            hasProximityAlert = false
                        ),
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
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
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = {
                    flowOf(setOf("123456".toNaptanStopIdentifier()))
                },
                onProximityAlertStopIdentifiersFlow = {
                    flowOf(setOf("123456".toNaptanStopIdentifier()))
                }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = UiArrivalAlertDropdownItem(
                            hasArrivalAlert = true
                        ),
                        proximityAlertDropdownItem = UiProximityAlertDropdownItem(
                            hasProximityAlert = true
                        ),
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
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
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = {
                    flowOf(setOf("123456".toNaptanStopIdentifier()))
                },
                onProximityAlertStopIdentifiersFlow = {
                    flowOf(setOf("123456".toNaptanStopIdentifier()))
                }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = false,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = UiArrivalAlertDropdownItem(
                            hasArrivalAlert = true
                        ),
                        proximityAlertDropdownItem = UiProximityAlertDropdownItem(
                            hasProximityAlert = true
                        ),
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
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
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { true },
                onHasPinShortcutFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onArrivalAlertStopIdentifiersFlow = {
                    flowOf(setOf("123456".toNaptanStopIdentifier()))
                },
                onProximityAlertStopIdentifiersFlow = {
                    flowOf(setOf("123456".toNaptanStopIdentifier()))
                }
            )
        )

        generator.getDropdownMenuItemsForStopsFlow(setOf("123456".toNaptanStopIdentifier())).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteDropdownMenu>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu(
                        isShown = true,
                        isShortcutItemShown = true,
                        arrivalAlertDropdownItem = UiArrivalAlertDropdownItem(
                            hasArrivalAlert = true
                        ),
                        proximityAlertDropdownItem = UiProximityAlertDropdownItem(
                            hasProximityAlert = true
                        ),
                        isStopMapItemShown = true
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    private fun TestScope.createUiFavouriteDropdownItemsGenerator(
        arguments: Arguments = FakeArguments(),
        state: State = FakeState(),
        featureRepository: FeatureRepository = FakeFeatureRepository(),
        alertsRepository: AlertsRepository = FakeAlertsRepository()
    ): RealUiFavouriteDropdownMenuGenerator {
        return RealUiFavouriteDropdownMenuGenerator(
            arguments = arguments,
            state = state,
            featureRepository = featureRepository,
            alertsRepository = alertsRepository,
            defaultCoroutineDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelCoroutineScope = backgroundScope
        )
    }
}
