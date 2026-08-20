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

package uk.org.rivernile.android.bustracker.ui.search

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `SearchScreen.kt`.
 *
 * @author Niall Scott
 */
class SearchScreenKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsEmptySearchTermErrorWhenContentIsEmptySearchTerm() {
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.EmptySearchTerm
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR)
            .apply {
                assertExists()
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_ERROR_TEXT))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.search_error_empty)
                    )
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertDoesNotExist()
    }

    @Test
    fun showsIndeterminateProgressWhenContentIsInProgress() {
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertExists()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertDoesNotExist()
    }

    @Test
    fun showsNoResultsErrorWhenContentIsNoResults() {
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.NoResults
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR)
            .apply {
                assertExists()
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_ERROR_TEXT))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.search_error_no_results)
                    )
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertDoesNotExist()
    }

    @Test
    fun showsContentWhenContentIsContent() {
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Content(
                            results = persistentListOf(
                                UiStopSearchResult(
                                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                                    stopName = UiStopName(
                                        name = "Stop name",
                                        locality = "Locality"
                                    ),
                                    services = null,
                                    orientation = StopOrientation.NORTH_EAST,
                                    dropdownMenu = UiStopSearchResultDropdownMenu()
                                )
                            )
                        )
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertExists()
    }

    @Test
    fun showStopDataActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
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
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
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
    fun showAddFavouriteStopActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowAddFavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowAddFavouriteStop = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showAddFavouriteStopActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowAddFavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowAddFavouriteStop = actionTracker,
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
    fun showRemoveFavouriteStopActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowRemoveFavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowRemoveFavouriteStop = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showRemoveFavouriteStopActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowRemoveFavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowRemoveFavouriteStop = actionTracker,
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
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
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
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
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
    fun showRemoveArrivalAlertActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowRemoveArrivalAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowRemoveArrivalAlert = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showRemoveArrivalAlertActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowRemoveArrivalAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowRemoveArrivalAlert = actionTracker,
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
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
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
    fun showAddProximityAlertActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
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
    fun showRemoveProximityAlertActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowRemoveProximityAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowRemoveProximityAlert = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showRemoveProximityAlertActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowRemoveProximityAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowRemoveProximityAlert = actionTracker,
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
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
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
                SearchScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
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

    @Composable
    private fun SearchScreenWithStateWithDefaults(
        state: UiState,
        modifier: Modifier = Modifier,
        onItemClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onAddFavouriteStopClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRemoveFavouriteStopClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onAddArrivalAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRemoveArrivalAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onAddProximityAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRemoveProximityAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onShowOnMapClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onActionLaunched: () -> Unit = { throw NotImplementedError() },
        onShowStopData: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowAddFavouriteStop: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowRemoveFavouriteStop: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowAddArrivalAlert: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowRemoveArrivalAlert: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowAddProximityAlert: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowRemoveProximityAlert: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowOnMap: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
    ) {
        SearchScreenWithState(
            state = state,
            onItemClick = onItemClick,
            onAddFavouriteStopClick = onAddFavouriteStopClick,
            onRemoveFavouriteStopClick = onRemoveFavouriteStopClick,
            onAddArrivalAlertClick = onAddArrivalAlertClick,
            onRemoveArrivalAlertClick = onRemoveArrivalAlertClick,
            onAddProximityAlertClick = onAddProximityAlertClick,
            onRemoveProximityAlertClick = onRemoveProximityAlertClick,
            onShowOnMapClick = onShowOnMapClick,
            onActionLaunched = onActionLaunched,
            modifier = modifier,
            onShowStopData = onShowStopData,
            onShowAddFavouriteStop = onShowAddFavouriteStop,
            onShowRemoveFavouriteStop = onShowRemoveFavouriteStop,
            onShowAddArrivalAlert = onShowAddArrivalAlert,
            onShowRemoveArrivalAlert = onShowRemoveArrivalAlert,
            onShowAddProximityAlert = onShowAddProximityAlert,
            onShowRemoveProximityAlert = onShowRemoveProximityAlert,
            onShowOnMap = onShowOnMap
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
