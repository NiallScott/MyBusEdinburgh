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

import androidx.activity.ComponentActivity
import androidx.compose.material3.DropdownMenu
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import uk.org.rivernile.android.bustracker.core.alerts.R as Ralert
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Tests for `AlertsDropdownMenuItems.kt`.
 *
 * @author Niall Scott
 */
class AlertsDropdownMenuItemsKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun addArrivalAlertMenuItemShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { }
                ) {
                    ArrivalAlertMenuItem(
                        menuItem = UiArrivalAlertDropdownMenuItem(hasArrivalAlert = false),
                        onAddArrivalAlertClick = itemClickedCounter,
                        onRemoveArrivalAlertClick = { fail() }
                    )
                }
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(Ralert.string.time_alert_add)

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_ARRIVAL_ALERT)
            .apply {
                assertExists()
                assertTextEquals(expectedText)
                assertIsEnabled()
                performClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_ARRIVAL_ALERT)
            .assertDoesNotExist()
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun removeArrivalAlertMenuItemShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { }
                ) {
                    ArrivalAlertMenuItem(
                        menuItem = UiArrivalAlertDropdownMenuItem(hasArrivalAlert = true),
                        onAddArrivalAlertClick = { fail() },
                        onRemoveArrivalAlertClick = itemClickedCounter
                    )
                }
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(Ralert.string.time_alert_rem)

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_ARRIVAL_ALERT)
            .apply {
                assertExists()
                assertTextEquals(expectedText)
                assertIsEnabled()
                performClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_ARRIVAL_ALERT)
            .assertDoesNotExist()
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun addProximityAlertMenuItemShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { }
                ) {
                    ProximityAlertMenuItem(
                        menuItem = UiProximityAlertDropdownMenuItem(hasProximityAlert = false),
                        onAddProximityAlertClick = itemClickedCounter,
                        onRemoveProximityAlertClick = { fail() }
                    )
                }
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(Ralert.string.prox_alert_add)

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_PROXIMITY_ALERT)
            .apply {
                assertExists()
                assertTextEquals(expectedText)
                assertIsEnabled()
                performClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_PROXIMITY_ALERT)
            .assertDoesNotExist()
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun removeProximityAlertMenuItemShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { }
                ) {
                    ProximityAlertMenuItem(
                        menuItem = UiProximityAlertDropdownMenuItem(hasProximityAlert = true),
                        onAddProximityAlertClick = { fail() },
                        onRemoveProximityAlertClick = itemClickedCounter
                    )
                }
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(Ralert.string.prox_alert_rem)

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_PROXIMITY_ALERT)
            .apply {
                assertExists()
                assertTextEquals(expectedText)
                assertIsEnabled()
                performClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_PROXIMITY_ALERT)
            .assertDoesNotExist()
        assertEquals(1, itemClickedCounter.count)
    }

    private class ItemClickedCounter : () -> Unit {

        var count = 0
            private set

        override fun invoke() {
            count++
        }
    }
}
