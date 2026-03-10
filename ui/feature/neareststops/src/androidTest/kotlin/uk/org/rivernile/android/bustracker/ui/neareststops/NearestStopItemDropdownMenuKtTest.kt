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

package uk.org.rivernile.android.bustracker.ui.neareststops

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.alerts.TEST_TAG_MENU_ITEM_ADD_ARRIVAL_ALERT
import uk.org.rivernile.android.bustracker.ui.alerts.TEST_TAG_MENU_ITEM_ADD_PROXIMITY_ALERT
import uk.org.rivernile.android.bustracker.ui.alerts.TEST_TAG_MENU_ITEM_REMOVE_ARRIVAL_ALERT
import uk.org.rivernile.android.bustracker.ui.alerts.TEST_TAG_MENU_ITEM_REMOVE_PROXIMITY_ALERT
import uk.org.rivernile.android.bustracker.ui.alerts.UiArrivalAlertDropdownMenuItem
import uk.org.rivernile.android.bustracker.ui.alerts.UiProximityAlertDropdownMenuItem
import uk.org.rivernile.android.bustracker.ui.favouritestops.TEST_TAG_MENU_ITEM_ADD_FAVOURITE_STOP
import uk.org.rivernile.android.bustracker.ui.favouritestops.TEST_TAG_MENU_ITEM_REMOVE_FAVOURITE_STOP
import uk.org.rivernile.android.bustracker.ui.favouritestops.UiFavouriteStopDropdownMenuItem
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `NearestStopItemDropdownMenu.kt`.
 *
 * @author Niall Scott
 */
class NearestStopItemDropdownMenuKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun addFavouriteStopMenuItemDoesNotExistWhenMenuItemIsNull() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        favouriteStopDropdownItem = null
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_FAVOURITE_STOP)
            .assertDoesNotExist()
    }

    @Test
    fun addFavouriteStopMenuItemFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        favouriteStopDropdownItem = UiFavouriteStopDropdownMenuItem(
                            isFavouriteStop = false
                        )
                    ),
                    onAddFavouriteStopClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_FAVOURITE_STOP)
            .apply {
                assertExists()
                performClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_FAVOURITE_STOP)
            .assertDoesNotExist()
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun removeFavouriteStopMenuItemDoesNotExistWhenMenuItemIsNull() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        favouriteStopDropdownItem = null
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_FAVOURITE_STOP)
            .assertDoesNotExist()
    }

    @Test
    fun removeFavouriteStopMenuItemFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        favouriteStopDropdownItem = UiFavouriteStopDropdownMenuItem(
                            isFavouriteStop = true
                        )
                    ),
                    onRemoveFavouriteStopClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_FAVOURITE_STOP)
            .apply {
                assertExists()
                performClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_FAVOURITE_STOP)
            .assertDoesNotExist()
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun addArrivalAlertMenuItemDoesNotExistWhenMenuItemIsNull() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        arrivalAlertDropdownItem = null
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_ARRIVAL_ALERT)
            .assertDoesNotExist()
    }

    @Test
    fun addArrivalAlertMenuItemFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        arrivalAlertDropdownItem = UiArrivalAlertDropdownMenuItem(
                            hasArrivalAlert = false
                        )
                    ),
                    onAddArrivalAlertClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_ARRIVAL_ALERT)
            .apply {
                assertExists()
                performClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_ARRIVAL_ALERT)
            .assertDoesNotExist()
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun removeArrivalAlertMenuItemDoesNotExistWhenMenuItemIsNull() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        arrivalAlertDropdownItem = null
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_ARRIVAL_ALERT)
            .assertDoesNotExist()
    }

    @Test
    fun removeArrivalAlertMenuItemFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        arrivalAlertDropdownItem = UiArrivalAlertDropdownMenuItem(
                            hasArrivalAlert = true
                        )
                    ),
                    onRemoveArrivalAlertClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_ARRIVAL_ALERT)
            .apply {
                assertExists()
                performClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_ARRIVAL_ALERT)
            .assertDoesNotExist()
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun addProximityAlertMenuItemDoesNotExistWhenMenuItemIsNull() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        proximityAlertDropdownItem = null
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_PROXIMITY_ALERT)
            .assertDoesNotExist()
    }

    @Test
    fun addProximityAlertMenuItemFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        proximityAlertDropdownItem = UiProximityAlertDropdownMenuItem(
                            hasProximityAlert = false
                        )
                    ),
                    onAddProximityAlertClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_PROXIMITY_ALERT)
            .apply {
                assertExists()
                performClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_PROXIMITY_ALERT)
            .assertDoesNotExist()
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun removeProximityAlertMenuItemDoesNotExistWhenMenuItemIsNull() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        proximityAlertDropdownItem = null
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_PROXIMITY_ALERT)
            .assertDoesNotExist()
    }

    @Test
    fun removeProximityAlertMenuItemFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        proximityAlertDropdownItem = UiProximityAlertDropdownMenuItem(
                            hasProximityAlert = true
                        )
                    ),
                    onRemoveProximityAlertClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_PROXIMITY_ALERT)
            .apply {
                assertExists()
                performClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_PROXIMITY_ALERT)
            .assertDoesNotExist()
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun showOnMapMenuItemDoesNotExistWhenIsStopMapItemShownIsFalse() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        isStopMapItemShown = false
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_SHOW_ON_MAP)
            .assertDoesNotExist()
    }

    @Test
    fun showOnMapMenuItemShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true,
                        isStopMapItemShown = true
                    ),
                    onShowOnMapClick = itemClickedCounter
                )
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(R.string.neareststops_menu_showonmap)

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_SHOW_ON_MAP)
            .apply {
                assertExists()
                assertTextEquals(expectedText)
                assertIsEnabled()
                performClick()
            }
        assertEquals(1, itemClickedCounter.count)
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun dismissingMenuCausesDismissedHandlerToBeFired() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemDropdownMenuWithDefaults(
                    menu = UiNearestStopDropdownMenu(
                        isShown = true
                    ),
                    onDropdownMenuDismissed = itemClickedCounter
                )
            }
        }

        // We need to use UI Automator so that it can inject the back event properly. Popups are
        // shown in a different window so the back event needs to get to the other window.
        UiDevice.getInstance(InstrumentationRegistry.getInstrumentation()).pressBack()

        composeTestRule.runOnIdle {
            assertEquals(1, itemClickedCounter.count)
        }
    }

    @Composable
    private fun NearestStopItemDropdownMenuWithDefaults(
        menu: UiNearestStopDropdownMenu,
        onDropdownMenuDismissed: () -> Unit = { throw NotImplementedError() },
        onAddFavouriteStopClick: () -> Unit = { throw NotImplementedError() },
        onRemoveFavouriteStopClick: () -> Unit = { throw NotImplementedError() },
        onAddArrivalAlertClick: () -> Unit = { throw NotImplementedError() },
        onRemoveArrivalAlertClick: () -> Unit = { throw NotImplementedError() },
        onAddProximityAlertClick: () -> Unit = { throw NotImplementedError() },
        onRemoveProximityAlertClick: () -> Unit = { throw NotImplementedError() },
        onShowOnMapClick: () -> Unit = { throw NotImplementedError() },
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            NearestStopItemDropdownMenu(
                menu = menu,
                onDropdownMenuDismissed = onDropdownMenuDismissed,
                onAddFavouriteStopClick = onAddFavouriteStopClick,
                onRemoveFavouriteStopClick = onRemoveFavouriteStopClick,
                onAddArrivalAlertClick = onAddArrivalAlertClick,
                onRemoveArrivalAlertClick = onRemoveArrivalAlertClick,
                onAddProximityAlertClick = onAddProximityAlertClick,
                onRemoveProximityAlertClick = onRemoveProximityAlertClick,
                onShowOnMapClick = onShowOnMapClick
            )
        }
    }

    private class ItemClickedCounter : () -> Unit {

        var count = 0
            private set

        override fun invoke() {
            count++
        }
    }
}
