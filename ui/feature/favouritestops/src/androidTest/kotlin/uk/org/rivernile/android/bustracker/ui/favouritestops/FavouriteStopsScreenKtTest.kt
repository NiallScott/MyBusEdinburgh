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
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test

/**
 * Tests for `FavouriteStopsScreen.kt`.
 *
 * @author Niall Scott
 */
class FavouriteStopsScreenKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsIndeterminateProgressWhenContentIsProgress() {
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertExists()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_EMPTY_ERROR)
            .assertDoesNotExist()
    }

    @Test
    fun showsPopulatedContentWhenContentIsContent() {
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Content(
                            favouriteStops = persistentListOf(
                                UiFavouriteStop(
                                    stopCode = "123456",
                                    savedName = "Saved Name",
                                    services = null,
                                    dropdownMenu = null
                                )
                            )
                        )
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertExists()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_EMPTY_ERROR)
            .assertDoesNotExist()
    }

    @Test
    fun showsEmptyErrorWhenContentIsEmpty() {
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_EMPTY_ERROR)
            .assertExists()
    }

    @Test
    fun emptyErrorShowsCorrectText() {
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty
                    )
                )
            }
        }
        val expectedTitleText = composeTestRule
            .activity
            .getString(R.string.favouritestops_nosavedstops_title)
        val expectedBodyText = composeTestRule
            .activity
            .getString(R.string.favouritestops_nosavedstops_summary)

        composeTestRule
            .onNodeWithTag(TEST_TAG_EMPTY_ERROR_TITLE_TEXT)
            .assertTextEquals(expectedTitleText)
        composeTestRule
            .onNodeWithTag(TEST_TAG_EMPTY_ERROR_BODY_TEXT)
            .assertTextEquals(expectedBodyText)
    }

    @Composable
    private fun FavouriteStopsScreenWithStateWithDefaults(
        state: UiState,
        onItemClicked: (String) -> Unit = { throw NotImplementedError() },
        onOpenDropdownClicked: (String) -> Unit = { throw NotImplementedError() },
        onDropdownMenuDismissed: () -> Unit = { throw NotImplementedError() },
        onEditFavouriteNameClick: (String) -> Unit = { throw NotImplementedError() },
        onRemoveFavouriteClick: (String) -> Unit = { throw NotImplementedError() },
        onAddArrivalAlertClick: (String) -> Unit = { throw NotImplementedError() },
        onRemoveArrivalAlertClick: (String) -> Unit = { throw NotImplementedError() },
        onAddProximityAlertClick: (String) -> Unit = { throw NotImplementedError() },
        onRemoveProximityAlertClick: (String) -> Unit = { throw NotImplementedError() },
        onShowOnMapClick: (String) -> Unit = { throw NotImplementedError() },
        onActionLaunched: () -> Unit = { throw NotImplementedError() }
    ) {
        FavouriteStopsScreenWithState(
            state = state,
            onItemClicked = onItemClicked,
            onOpenDropdownClicked = onOpenDropdownClicked,
            onDropdownMenuDismissed = onDropdownMenuDismissed,
            onEditFavouriteNameClick = onEditFavouriteNameClick,
            onRemoveFavouriteClick = onRemoveFavouriteClick,
            onAddArrivalAlertClick = onAddArrivalAlertClick,
            onRemoveArrivalAlertClick = onRemoveArrivalAlertClick,
            onAddProximityAlertClick = onAddProximityAlertClick,
            onRemoveProximityAlertClick = onRemoveProximityAlertClick,
            onShowOnMapClick = onShowOnMapClick,
            onActionLaunched = onActionLaunched
        )
    }
}
