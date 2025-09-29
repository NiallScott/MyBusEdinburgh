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

import androidx.activity.ComponentActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `FavouriteStopItemDropdownMenu.kt`.
 *
 * @author Niall Scott
 */
class FavouriteStopItemDropdownMenuKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun editFavouriteNameMenuItemShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopItemDropdownMenuWithDefaults(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.EditFavouriteName
                    ),
                    onEditFavouriteNameClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.favouritestops_menu_edit))
            .apply {
                assertIsEnabled()
                performClick()
            }
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun removeFavouriteMenuItemShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopItemDropdownMenuWithDefaults(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.RemoveFavourite
                    ),
                    onRemoveFavouriteClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.favouritestops_menu_delete))
            .apply {
                assertIsEnabled()
                performClick()
            }
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun addArrivalAlertMenuItemDisabledShowsCorrectTextAndDoesNotFireClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopItemDropdownMenuWithDefaults(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.AddArrivalAlert(isEnabled = false)
                    ),
                    onAddArrivalAlertClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.favouritestops_menu_time_add)
            )
            .apply {
                assertIsNotEnabled()
                performClick()
            }
        assertEquals(0, itemClickedCounter.count)
    }

    @Test
    fun addArrivalAlertMenuItemEnabledShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopItemDropdownMenuWithDefaults(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.AddArrivalAlert(isEnabled = true)
                    ),
                    onAddArrivalAlertClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.favouritestops_menu_time_add)
            )
            .apply {
                assertIsEnabled()
                performClick()
            }
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun removeArrivalAlertMenuItemShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopItemDropdownMenuWithDefaults(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.RemoveArrivalAlert
                    ),
                    onRemoveArrivalAlertClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.favouritestops_menu_time_rem)
            )
            .apply {
                assertIsEnabled()
                performClick()
            }
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun addProximityAlertMenuItemDisabledShowsCorrectTextAndDoesNotFireClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopItemDropdownMenuWithDefaults(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.AddProximityAlert(isEnabled = false)
                    ),
                    onAddProximityAlertClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.favouritestops_menu_prox_add)
            )
            .apply {
                assertIsNotEnabled()
                performClick()
            }
        assertEquals(0, itemClickedCounter.count)
    }

    @Test
    fun addProximityAlertMenuItemEnabledShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopItemDropdownMenuWithDefaults(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.AddProximityAlert(isEnabled = true)
                    ),
                    onAddProximityAlertClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.favouritestops_menu_prox_add)
            )
            .apply {
                assertIsEnabled()
                performClick()
            }
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun removeProximityAlertMenuItemShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopItemDropdownMenuWithDefaults(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.RemoveProximityAlert
                    ),
                    onRemoveProximityAlertClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.favouritestops_menu_prox_rem)
            )
            .apply {
                assertIsEnabled()
                performClick()
            }
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun showOnMapMenuItemShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopItemDropdownMenuWithDefaults(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.ShowOnMap
                    ),
                    onShowOnMapClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithText(
                composeTestRule.activity.getString(R.string.favouritestops_menu_showonmap)
            )
            .apply {
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
                FavouriteStopItemDropdownMenuWithDefaults(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.EditFavouriteName,
                        UiFavouriteDropdownItem.RemoveFavourite,
                        UiFavouriteDropdownItem.AddArrivalAlert(isEnabled = true),
                        UiFavouriteDropdownItem.AddProximityAlert(isEnabled = true),
                        UiFavouriteDropdownItem.ShowOnMap
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
    private fun FavouriteStopItemDropdownMenuWithDefaults(
        items: ImmutableList<UiFavouriteDropdownItem>,
        onDropdownMenuDismissed: () -> Unit = { throw NotImplementedError() },
        onEditFavouriteNameClick: () -> Unit = { throw NotImplementedError() },
        onRemoveFavouriteClick: () -> Unit = { throw NotImplementedError() },
        onAddArrivalAlertClick: () -> Unit = { throw NotImplementedError() },
        onRemoveArrivalAlertClick: () -> Unit = { throw NotImplementedError() },
        onAddProximityAlertClick: () -> Unit = { throw NotImplementedError() },
        onRemoveProximityAlertClick: () -> Unit = { throw NotImplementedError() },
        onShowOnMapClick: () -> Unit = { throw NotImplementedError() }
    ) {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            FavouriteStopItemDropdownMenu(
                items = items,
                onDropdownMenuDismissed = onDropdownMenuDismissed,
                onEditFavouriteNameClick = onEditFavouriteNameClick,
                onRemoveFavouriteClick = onRemoveFavouriteClick,
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
