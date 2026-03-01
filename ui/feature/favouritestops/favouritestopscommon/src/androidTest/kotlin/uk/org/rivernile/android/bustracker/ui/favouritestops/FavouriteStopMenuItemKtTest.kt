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

package uk.org.rivernile.android.bustracker.ui.favouritestops

import androidx.activity.ComponentActivity
import androidx.compose.material3.DropdownMenu
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.favourites.R as Rfavourites
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Tests for `FavouriteStopMenuItem.kt`.
 *
 * @author Niall Scott
 */
class FavouriteStopMenuItemKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun addFavouriteStopMenuItemShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { }
                ) {
                    FavouriteStopMenuItem(
                        menuItem = UiFavouriteStopDropdownMenuItem(isFavouriteStop = false),
                        onAddFavouriteStopClick = itemClickedCounter,
                        onRemoveFavouriteStopClick = { fail() }
                    )
                }
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(Rfavourites.string.favourite_stop_add)

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_FAVOURITE_STOP)
            .apply {
                assertExists()
                assertTextEquals(expectedText)
                assertIsEnabled()
                performClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_FAVOURITE_STOP)
            .assertDoesNotExist()
        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun removeFavouriteStopMenuItemShowsCorrectTextAndFiresClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                DropdownMenu(
                    expanded = true,
                    onDismissRequest = { }
                ) {
                    FavouriteStopMenuItem(
                        menuItem = UiFavouriteStopDropdownMenuItem(isFavouriteStop = true),
                        onAddFavouriteStopClick = { fail() },
                        onRemoveFavouriteStopClick = itemClickedCounter
                    )
                }
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(Rfavourites.string.favourite_stop_remove)

        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_REMOVE_FAVOURITE_STOP)
            .apply {
                assertExists()
                assertTextEquals(expectedText)
                assertIsEnabled()
                performClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_MENU_ITEM_ADD_FAVOURITE_STOP)
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
