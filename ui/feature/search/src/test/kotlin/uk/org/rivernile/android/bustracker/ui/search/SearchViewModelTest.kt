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

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests for [SearchViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class SearchViewModelTest {

    @Test
    fun uiStateFlowInitiallyEmitsDefaultFlow() = runTest {
        val viewModel = createViewModel()

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsState() = runTest {
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = {
                    flowOf(
                        UiAction.ShowStopData(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    )
                }
            ),
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = {
                    flow {
                        delay(1.milliseconds)
                        emit(UiContent.NoResults)
                    }
                }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            assertEquals(
                UiState(
                    content = UiContent.NoResults,
                    action = UiAction.ShowStopData(
                        stopIdentifier = "123456".toNaptanStopIdentifier()
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsWhenActionChanges() = runTest {
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = {
                    flow {
                        emit(null)
                        delay(2.milliseconds)
                        emit(
                            UiAction.ShowStopData(
                                stopIdentifier = "123456".toNaptanStopIdentifier()
                            )
                        )
                    }
                }
            ),
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = {
                    flow {
                        delay(1.milliseconds)
                        emit(UiContent.NoResults)
                    }
                }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            assertEquals(
                UiState(
                    content = UiContent.NoResults,
                    action = null
                ),
                awaitItem()
            )
            assertEquals(
                UiState(
                    content = UiContent.NoResults,
                    action = UiAction.ShowStopData(
                        stopIdentifier = "123456".toNaptanStopIdentifier()
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun setSearchTermSetsSearchTermOnState() = runTest {
        val searchTerms = mutableListOf<String?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetSearchTerm = { searchTerms += it }
            )
        )

        viewModel.searchTerm = "search term 1"
        viewModel.searchTerm = "search term 2"

        assertEquals(
            listOf<String?>("search term 1", "search term 2"),
            searchTerms
        )
    }

    @Test
    fun onItemClickedSetsShowsStopDataAction() = runTest {
        val itemTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = itemTracker
            )
        )

        viewModel.onItemClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowStopData(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            itemTracker.items
        )
    }

    @Test
    fun onAddFavouriteStopClickedSetsShowAddFavouriteStopAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onAddFavouriteStopClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowAddFavouriteStop(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
    }

    @Test
    fun onRemoveFavouriteClickedSetsShowRemoveFavouriteStopAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onRemoveFavouriteStopClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowRemoveFavouriteStop(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
    }

    @Test
    fun onAddArrivalAlertClickedSetsShowAddArrivalAlertAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onAddArrivalAlertClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowAddArrivalAlert(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
    }

    @Test
    fun onRemoveArrivalAlertClickedSetsShowRemoveArrivalAlertAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onRemoveArrivalAlertClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowRemoveArrivalAlert(
                    stopIdentifier = "123456".toNaptanStopIdentifier()
                )
            ),
            actionTracker.items
        )
    }

    @Test
    fun onAddProxAlertClickedSetsShowAddProxAlertAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onAddProximityAlertClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowAddProximityAlert(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
    }

    @Test
    fun onRemoveProxAlertClickedSetsShowRemoveProxAlertAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onRemoveProximityAlertClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowRemoveProximityAlert(
                    stopIdentifier = "123456".toNaptanStopIdentifier()
                )
            ),
            actionTracker.items
        )
    }

    @Test
    fun onShowOnMapClickedSetsShowOnMapAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onShowOnMapClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowOnMap(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
    }

    @Test
    fun onActionLaunchedSetsNullAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onActionLaunched()

        assertEquals(listOf(null), actionTracker.items)
    }

    private fun TestScope.createViewModel(
        state: State = FakeState(
            onActionFlow = ::emptyFlow
        ),
        uiContentRetriever: UiContentRetriever = FakeUiContentRetriever(
            onUiContentFlow = ::emptyFlow
        )
    ): SearchViewModel {
        return SearchViewModel(
            state = state,
            uiContentRetriever = uiContentRetriever,
            defaultCoroutineDispatcher = UnconfinedTestDispatcher(scheduler = testScheduler),
            viewModelCoroutineScope = backgroundScope
        )
    }

    private class ItemTracker<T> : (T) -> Unit {

        val items get() = _items.toList()
        private val _items = mutableListOf<T>()

        override fun invoke(p1: T) {
            _items += p1
        }
    }
}
