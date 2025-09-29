/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.FakeAlertsRepository
import uk.org.rivernile.android.bustracker.core.features.FakeFeatureRepository
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [RealUiFavouriteDropdownMenuGenerator].
 *
 * @author Niall Scott
 */
class RealUiFavouriteDropdownMenuGeneratorTest {

    @Test
    fun uiFavouriteDropdownItemsForStopFlowEmitsNullWhenSelectedStopIsNull() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            state = FakeState(
                onSelectedStopCodeFlow = { flowOf(null) }
            )
        )

        generator.uiFavouriteDropdownItemsForStopFlow.test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiFavouriteDropdownItemsForStopFlowEmitsNullWhenSelectedStopIsEmpty() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            state = FakeState(
                onSelectedStopCodeFlow = { flowOf("") }
            )
        )

        generator.uiFavouriteDropdownItemsForStopFlow.test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun uiFavouriteDropdownItemsForStopFlowWithArrivalAlertDisabled() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            state = FakeState(
                onSelectedStopCodeFlow = { flowOf("123456") }
            ),
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { false },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onHasProximityAlertFlow = {
                    assertEquals("123456", it)
                    flowOf(true)
                }
            )
        )

        generator.uiFavouriteDropdownItemsForStopFlow.test {
            assertEquals(
                "123456" to UiFavouriteDropdownMenu(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.EditFavouriteName,
                        UiFavouriteDropdownItem.RemoveFavourite,
                        UiFavouriteDropdownItem.AddProximityAlert(isEnabled = false),
                        UiFavouriteDropdownItem.ShowOnMap
                    )
                ),
                awaitItem()
            )
            assertEquals(
                "123456" to UiFavouriteDropdownMenu(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.EditFavouriteName,
                        UiFavouriteDropdownItem.RemoveFavourite,
                        UiFavouriteDropdownItem.RemoveProximityAlert,
                        UiFavouriteDropdownItem.ShowOnMap
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiFavouriteDropdownItemsForStopFlowWithProximityAlertDisabled() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            state = FakeState(
                onSelectedStopCodeFlow = { flowOf("123456") }
            ),
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { false },
                onHasStopMapUiFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onHasArrivalAlertFlow = {
                    assertEquals("123456", it)
                    flowOf(true)
                }
            )
        )

        generator.uiFavouriteDropdownItemsForStopFlow.test {
            assertEquals(
                "123456" to UiFavouriteDropdownMenu(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.EditFavouriteName,
                        UiFavouriteDropdownItem.RemoveFavourite,
                        UiFavouriteDropdownItem.AddArrivalAlert(isEnabled = false),
                        UiFavouriteDropdownItem.ShowOnMap
                    )
                ),
                awaitItem()
            )
            assertEquals(
                "123456" to UiFavouriteDropdownMenu(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.EditFavouriteName,
                        UiFavouriteDropdownItem.RemoveFavourite,
                        UiFavouriteDropdownItem.RemoveArrivalAlert,
                        UiFavouriteDropdownItem.ShowOnMap
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiFavouriteDropdownItemsForStopFlowWithStopMapDisabled() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            state = FakeState(
                onSelectedStopCodeFlow = { flowOf("123456") }
            ),
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { false }
            ),
            alertsRepository = FakeAlertsRepository(
                onHasArrivalAlertFlow = {
                    assertEquals("123456", it)
                    flowOf(true)
                },
                onHasProximityAlertFlow = {
                    assertEquals("123456", it)
                    flowOf(true)
                }
            )
        )

        generator.uiFavouriteDropdownItemsForStopFlow.test {
            assertEquals(
                "123456" to UiFavouriteDropdownMenu(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.EditFavouriteName,
                        UiFavouriteDropdownItem.RemoveFavourite,
                        UiFavouriteDropdownItem.AddArrivalAlert(isEnabled = false),
                        UiFavouriteDropdownItem.AddProximityAlert(isEnabled = false),
                    )
                ),
                awaitItem()
            )
            assertEquals(
                "123456" to UiFavouriteDropdownMenu(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.EditFavouriteName,
                        UiFavouriteDropdownItem.RemoveFavourite,
                        UiFavouriteDropdownItem.RemoveArrivalAlert,
                        UiFavouriteDropdownItem.RemoveProximityAlert,
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiFavouriteDropdownItemsForStopFlowWithAllFeaturesEnabled() = runTest {
        val generator = createUiFavouriteDropdownItemsGenerator(
            state = FakeState(
                onSelectedStopCodeFlow = { flowOf("123456") }
            ),
            featureRepository = FakeFeatureRepository(
                onHasArrivalAlertFeature = { true },
                onHasProximityAlertFeature = { true },
                onHasStopMapUiFeature = { true }
            ),
            alertsRepository = FakeAlertsRepository(
                onHasArrivalAlertFlow = {
                    assertEquals("123456", it)
                    flowOf(true)
                },
                onHasProximityAlertFlow = {
                    assertEquals("123456", it)
                    flowOf(true)
                }
            )
        )

        generator.uiFavouriteDropdownItemsForStopFlow.test {
            assertEquals(
                "123456" to UiFavouriteDropdownMenu(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.EditFavouriteName,
                        UiFavouriteDropdownItem.RemoveFavourite,
                        UiFavouriteDropdownItem.AddArrivalAlert(isEnabled = false),
                        UiFavouriteDropdownItem.AddProximityAlert(isEnabled = false),
                        UiFavouriteDropdownItem.ShowOnMap
                    )
                ),
                awaitItem()
            )
            assertEquals(
                "123456" to UiFavouriteDropdownMenu(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.EditFavouriteName,
                        UiFavouriteDropdownItem.RemoveFavourite,
                        UiFavouriteDropdownItem.RemoveArrivalAlert,
                        UiFavouriteDropdownItem.RemoveProximityAlert,
                        UiFavouriteDropdownItem.ShowOnMap
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createUiFavouriteDropdownItemsGenerator(
        state: State = FakeState(),
        featureRepository: FeatureRepository = FakeFeatureRepository(),
        alertsRepository: AlertsRepository = FakeAlertsRepository()
    ): RealUiFavouriteDropdownMenuGenerator {
        return RealUiFavouriteDropdownMenuGenerator(
            state = state,
            featureRepository = featureRepository,
            alertsRepository = alertsRepository
        )
    }
}
