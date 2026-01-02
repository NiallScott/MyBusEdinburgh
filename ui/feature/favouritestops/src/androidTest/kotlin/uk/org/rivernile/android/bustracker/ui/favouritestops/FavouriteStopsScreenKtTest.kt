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

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

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

    @Test
    fun showStopDataActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowStopData(stopCode = "123456")
                    ),
                    onShowStopData = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showStopDataActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<String>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowStopData(stopCode = "123456")
                    ),
                    onShowStopData = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456"),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showEditFavouriteStopActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowEditFavouriteStop(stopCode = "123456")
                    ),
                    onShowEditFavouriteStop = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showEditFavouriteStopActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<String>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowEditFavouriteStop(stopCode = "123456")
                    ),
                    onShowEditFavouriteStop = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456"),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showConfirmRemoveFavouriteActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowConfirmRemoveFavourite(stopCode = "123456")
                    ),
                    onShowConfirmRemoveFavourite = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showConfirmRemoveFavouriteActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<String>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowConfirmRemoveFavourite(stopCode = "123456")
                    ),
                    onShowConfirmRemoveFavourite = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456"),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showOnMapActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowOnMap(stopCode = "123456")
                    ),
                    onShowOnMap = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showOnMapActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<String>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowOnMap(stopCode = "123456")
                    ),
                    onShowOnMap = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456"),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showAddArrivalAlertActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowAddArrivalAlert(stopCode = "123456")
                    ),
                    onShowAddArrivalAlert = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showAddArrivalAlertActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<String>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowAddArrivalAlert(stopCode = "123456")
                    ),
                    onShowAddArrivalAlert = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456"),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showConfirmRemoveArrivalAlertActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowConfirmRemoveArrivalAlert(stopCode = "123456")
                    ),
                    onShowConfirmRemoveArrivalAlert = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showConfirmRemoveArrivalAlertActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<String>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowConfirmRemoveArrivalAlert(stopCode = "123456")
                    ),
                    onShowConfirmRemoveArrivalAlert = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456"),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showAddProximityAlertActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowAddProximityAlert(stopCode = "123456")
                    ),
                    onShowAddProximityAlert = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showAddProximityAlertAlertActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<String>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowAddProximityAlert(stopCode = "123456")
                    ),
                    onShowAddProximityAlert = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456"),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showConfirmRemoveProximityAlertActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowConfirmRemoveProximityAlert(stopCode = "123456")
                    ),
                    onShowConfirmRemoveProximityAlert = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showConfirmRemoveProximityAlertAlertActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<String>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowConfirmRemoveProximityAlert(stopCode = "123456")
                    ),
                    onShowConfirmRemoveProximityAlert = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456"),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun addShortcutActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.AddShortcut(
                            stopCode = "123456",
                            savedName = "Saved Name"
                        )
                    ),
                    onAddShortcut = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun addShortcutActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<UiFavouriteShortcut>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.AddShortcut(
                            stopCode = "123456",
                            savedName = "Saved Name"
                        )
                    ),
                    onAddShortcut = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf(
                UiFavouriteShortcut(
                    stopCode = "123456",
                    name = "Saved Name"
                )
            ),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Composable
    private fun FavouriteStopsScreenWithStateWithDefaults(
        state: UiState,
        onItemClicked: (String, String) -> Unit = { _, _ -> throw NotImplementedError() },
        onOpenDropdownClicked: (String) -> Unit = { throw NotImplementedError() },
        onDropdownMenuDismissed: () -> Unit = { throw NotImplementedError() },
        onEditFavouriteNameClick: (String) -> Unit = { throw NotImplementedError() },
        onRemoveFavouriteClick: (String) -> Unit = { throw NotImplementedError() },
        onAddArrivalAlertClick: (String) -> Unit = { throw NotImplementedError() },
        onRemoveArrivalAlertClick: (String) -> Unit = { throw NotImplementedError() },
        onAddProximityAlertClick: (String) -> Unit = { throw NotImplementedError() },
        onRemoveProximityAlertClick: (String) -> Unit = { throw NotImplementedError() },
        onShowOnMapClick: (String) -> Unit = { throw NotImplementedError() },
        onActionLaunched: () -> Unit = { throw NotImplementedError() },
        onShowStopData: ((String) -> Unit)? = { throw NotImplementedError() },
        onShowEditFavouriteStop: ((String) -> Unit)? = { throw NotImplementedError() },
        onShowConfirmRemoveFavourite: ((String) -> Unit)? = { throw NotImplementedError() },
        onShowOnMap: ((String) -> Unit)? = { throw NotImplementedError() },
        onShowAddArrivalAlert: ((String) -> Unit)? = { throw NotImplementedError() },
        onShowConfirmRemoveArrivalAlert: ((String) -> Unit)? = { throw NotImplementedError() },
        onShowAddProximityAlert: ((String) -> Unit)? = { throw NotImplementedError() },
        onShowConfirmRemoveProximityAlert: ((String) -> Unit)? = { throw NotImplementedError() },
        onAddShortcut: ((UiFavouriteShortcut) -> Unit)? = { throw NotImplementedError() }
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
            onActionLaunched = onActionLaunched,
            onShowStopData = onShowStopData,
            onShowEditFavouriteStop = onShowEditFavouriteStop,
            onShowConfirmRemoveFavourite = onShowConfirmRemoveFavourite,
            onShowOnMap = onShowOnMap,
            onShowAddArrivalAlert = onShowAddArrivalAlert,
            onShowConfirmRemoveArrivalAlert = onShowConfirmRemoveArrivalAlert,
            onShowAddProximityAlert = onShowAddProximityAlert,
            onShowConfirmRemoveProximityAlert = onShowConfirmRemoveProximityAlert,
            onAddShortcut = onAddShortcut
        )
    }
}

private class Tracker<T> : (T) -> Unit {

    val observedValues get() = _observedValues.toList()
    private val _observedValues = mutableListOf<T>()

    override fun invoke(p1: T) {
        _observedValues += p1
    }
}

private class InvocationCounter : () -> Unit {

    var count = 0
        private set

    override fun invoke() {
        count++
    }
}
