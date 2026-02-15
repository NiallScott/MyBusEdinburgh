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
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.shortcuts.FavouriteStopShortcut
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
                                    stopIdentifier = "123456".toNaptanStopIdentifier(),
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
                        action = UiAction.ShowStopData(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
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
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowStopData(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowStopData = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
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
                        action = UiAction.ShowEditFavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
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
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowEditFavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowEditFavouriteStop = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
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
                        action = UiAction.ShowConfirmRemoveFavourite(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
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
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowConfirmRemoveFavourite(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowConfirmRemoveFavourite = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
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
                        action = UiAction.ShowOnMap(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
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
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowOnMap(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowOnMap = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
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
                        action = UiAction.ShowAddArrivalAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
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
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowAddArrivalAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowAddArrivalAlert = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
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
                        action = UiAction.ShowConfirmRemoveArrivalAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
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
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowConfirmRemoveArrivalAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowConfirmRemoveArrivalAlert = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
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
                        action = UiAction.ShowAddProximityAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
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
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowAddProximityAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowAddProximityAlert = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
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
                        action = UiAction.ShowConfirmRemoveProximityAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
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
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.ShowConfirmRemoveProximityAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowConfirmRemoveProximityAlert = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
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
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
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
        val actionTracker = Tracker<FavouriteStopShortcut>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                FavouriteStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Empty,
                        action = UiAction.AddShortcut(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
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
                FavouriteStopShortcut(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    displayName = "Saved Name"
                )
            ),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Composable
    private fun FavouriteStopsScreenWithStateWithDefaults(
        state: UiState,
        onItemClicked: (StopIdentifier, String) -> Unit = { _, _ -> throw NotImplementedError() },
        onOpenDropdownClicked: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onDropdownMenuDismissed: () -> Unit = { throw NotImplementedError() },
        onEditFavouriteNameClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRemoveFavouriteClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onAddShortcutClick: (StopIdentifier, String) -> Unit =
            { _, _ -> throw NotImplementedError() },
        onAddArrivalAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRemoveArrivalAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onAddProximityAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRemoveProximityAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onShowOnMapClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onActionLaunched: () -> Unit = { throw NotImplementedError() },
        onShowStopData: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowEditFavouriteStop: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowConfirmRemoveFavourite: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowOnMap: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowAddArrivalAlert: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowConfirmRemoveArrivalAlert: ((StopIdentifier) -> Unit)? =
            { throw NotImplementedError() },
        onShowAddProximityAlert: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowConfirmRemoveProximityAlert: ((StopIdentifier) -> Unit)? =
            { throw NotImplementedError() },
        onAddShortcut: ((FavouriteStopShortcut) -> Unit)? = { throw NotImplementedError() }
    ) {
        FavouriteStopsScreenWithState(
            state = state,
            onItemClicked = onItemClicked,
            onOpenDropdownClicked = onOpenDropdownClicked,
            onDropdownMenuDismissed = onDropdownMenuDismissed,
            onEditFavouriteNameClick = onEditFavouriteNameClick,
            onRemoveFavouriteClick = onRemoveFavouriteClick,
            onAddShortcutClick = onAddShortcutClick,
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
