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

import app.cash.turbine.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [FavouriteStopsViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class FavouriteStopsViewModelTest {

    @Test
    fun uiStateFlowInitiallyEmitsDefaultFlow() = runTest {
        val viewModel = createViewModel()

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsEmptyWhenFavouriteStopsIsNull() = runTest {
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) }
            ),
            uiFavouriteStopsRetriever = FakeUiFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = {
                    flow {
                        delay(1L)
                        emit(null)
                    }
                }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            assertEquals(
                UiState(
                    content = UiContent.Empty
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsEmptyWhenFavouriteStopsIsEmpty() = runTest {
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) }
            ),
            uiFavouriteStopsRetriever = FakeUiFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = {
                    flow {
                        delay(1L)
                        emit(emptyList())
                    }
                }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            assertEquals(
                UiState(
                    content = UiContent.Empty
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsContentWhenFavouriteStopsIsPopulated() = runTest {
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) }
            ),
            uiFavouriteStopsRetriever = FakeUiFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = {
                    flow {
                        delay(1L)
                        emit(
                            listOf(
                                UiFavouriteStop(
                                    stopCode = "123456",
                                    savedName = "Saved Name",
                                    services = null,
                                    dropdownMenu = null
                                )
                            )
                        )
                    }
                }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            assertEquals(
                UiState(
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
                        delay(2L)
                        emit(UiAction.ShowStopData(stopCode = "123456"))
                    }
                }
            ),
            uiFavouriteStopsRetriever = FakeUiFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = {
                    flow {
                        delay(1L)
                        emit(
                            listOf(
                                UiFavouriteStop(
                                    stopCode = "123456",
                                    savedName = "Saved Name",
                                    services = null,
                                    dropdownMenu = null
                                )
                            )
                        )
                    }
                }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            assertEquals(
                UiState(
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
                ),
                awaitItem()
            )
            assertEquals(
                UiState(
                    content = UiContent.Content(
                        favouriteStops = persistentListOf(
                            UiFavouriteStop(
                                stopCode = "123456",
                                savedName = "Saved Name",
                                services = null,
                                dropdownMenu = null
                            )
                        )
                    ),
                    action = UiAction.ShowStopData(stopCode = "123456")
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onItemClickedSetsShowStopDataActionWhenNotInShortcutMode() = runTest {
        val itemTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = itemTracker
            )
        )

        viewModel.onItemClicked("123456", "Saved Name")

        assertEquals(
            listOf(
                UiAction.ShowStopData(stopCode = "123456")
            ),
            itemTracker.items
        )
    }

    @Test
    fun onItemClickedSetsAddShortcutActionWhenInShortcutMode() = runTest {
        val itemTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { true }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = itemTracker
            )
        )

        viewModel.onItemClicked("123456", "Saved Name")

        assertEquals(
            listOf(
                UiAction.AddShortcut(
                    stopCode = "123456",
                    savedName = "Saved Name"
                )
            ),
            itemTracker.items
        )
    }

    @Test
    fun onItemOpenDropdownClickedSetsSelectedStopCodeWhenNotInShortcutMode() = runTest {
        val itemTracker = ItemTracker<String?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetSelectedStopCode = itemTracker
            )
        )

        viewModel.onItemOpenDropdownClicked("123456")

        assertEquals(listOf("123456"), itemTracker.items)
    }

    @Test
    fun onItemOpenDropdownClickedDoesNothingWhenInShortcutMode() = runTest {
        val itemTracker = ItemTracker<String?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { true }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetSelectedStopCode = itemTracker
            )
        )

        viewModel.onItemOpenDropdownClicked("123456")

        assertTrue(itemTracker.items.isEmpty())
    }

    @Test
    fun onDropdownMenuDismissedSetsSelectedStopCodeToNullWhenNotInShortcutMode() = runTest {
        val itemTracker = ItemTracker<String?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetSelectedStopCode = itemTracker
            )
        )

        viewModel.onDropdownMenuDismissed()

        assertEquals(listOf(null), itemTracker.items)
    }

    @Test
    fun onDropdownMenuDismissedDoesNothingWhenInShortcutMode() = runTest {
        val itemTracker = ItemTracker<String?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { true }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetSelectedStopCode = itemTracker
            )
        )

        viewModel.onDropdownMenuDismissed()

        assertTrue(itemTracker.items.isEmpty())
    }

    @Test
    fun onEditFavouriteNameClickedSetsShowEditActionAndDismissesDropdownMenu() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopCodeTracker = ItemTracker<String?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker,
                onSetSelectedStopCode = selectedStopCodeTracker
            )
        )

        viewModel.onEditFavouriteNameClicked("123456")

        assertEquals(
            listOf(
                UiAction.ShowEditFavouriteStop(stopCode = "123456")
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopCodeTracker.items)
    }

    @Test
    fun onEditFavouriteNameClickedDoesNotingWhenInShortcutMode() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { true }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker
            )
        )

        viewModel.onEditFavouriteNameClicked("123456")

        assertTrue(actionTracker.items.isEmpty())
    }

    @Test
    fun onRemoveFavouriteClickedSetsShowConfirmRemoveActionAndDismissesDropdownMenu() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopCodeTracker = ItemTracker<String?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker,
                onSetSelectedStopCode = selectedStopCodeTracker
            )
        )

        viewModel.onRemoveFavouriteClicked("123456")

        assertEquals(
            listOf(
                UiAction.ShowConfirmRemoveFavourite(stopCode = "123456")
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopCodeTracker.items)
    }

    @Test
    fun onRemoveFavouriteClickedDoesNothingWhenInShortcutMode() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { true }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker
            )
        )

        viewModel.onRemoveFavouriteClicked("123456")

        assertTrue(actionTracker.items.isEmpty())
    }

    @Test
    fun onAddArrivalAlertClickedSetsShowAddArrivalAlertActionAndDismissesDropdownMenu() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopCodeTracker = ItemTracker<String?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker,
                onSetSelectedStopCode = selectedStopCodeTracker
            )
        )

        viewModel.onAddArrivalAlertClicked("123456")

        assertEquals(
            listOf(
                UiAction.ShowAddArrivalAlert(stopCode = "123456")
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopCodeTracker.items)
    }

    @Test
    fun onAddArrivalAlertClickedDoesNothingWhenInShortcutMode() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { true }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker
            )
        )

        viewModel.onAddArrivalAlertClicked("123456")

        assertTrue(actionTracker.items.isEmpty())
    }

    @Test
    fun onRemoveArrivalAlertClickedSetsShowConfirmRemoveAlertActionAndDismissesDropdownMenu() =
        runTest {
            val actionTracker = ItemTracker<UiAction?>()
            val selectedStopCodeTracker = ItemTracker<String?>()
            val viewModel = createViewModel(
                arguments = FakeArguments(
                    onIsShortcutMode = { false }
                ),
                state = FakeState(
                    onActionFlow = { emptyFlow() },
                    onSetAction = actionTracker,
                    onSetSelectedStopCode = selectedStopCodeTracker
                )
            )

            viewModel.onRemoveArrivalAlertClicked("123456")

            assertEquals(
                listOf(
                    UiAction.ShowConfirmRemoveArrivalAlert(stopCode = "123456")
                ),
                actionTracker.items
            )
            assertEquals(listOf(null), selectedStopCodeTracker.items)
        }

    @Test
    fun onRemoveArrivalAlertClickedDoesNothingWhenInShortcutMode() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { true }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker
            )
        )

        viewModel.onRemoveArrivalAlertClicked("123456")

        assertTrue(actionTracker.items.isEmpty())
    }

    @Test
    fun onAddProxAlertClickedSetsShowAddProxAlertActionAndDismissesDropdownMenu() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopCodeTracker = ItemTracker<String?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker,
                onSetSelectedStopCode = selectedStopCodeTracker
            )
        )

        viewModel.onAddProximityAlertClicked("123456")

        assertEquals(
            listOf(
                UiAction.ShowAddProximityAlert(stopCode = "123456")
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopCodeTracker.items)
    }

    @Test
    fun onAddProxAlertClickedDoesNothingWhenInShortcutMode() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { true }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker
            )
        )

        viewModel.onAddProximityAlertClicked("123456")

        assertTrue(actionTracker.items.isEmpty())
    }

    @Test
    fun onRemoveProxAlertClickedSetsShowConfirmRemoveAlertActionAndDismissesDropdownMenu() =
        runTest {
            val actionTracker = ItemTracker<UiAction?>()
            val selectedStopCodeTracker = ItemTracker<String?>()
            val viewModel = createViewModel(
                arguments = FakeArguments(
                    onIsShortcutMode = { false }
                ),
                state = FakeState(
                    onActionFlow = { emptyFlow() },
                    onSetAction = actionTracker,
                    onSetSelectedStopCode = selectedStopCodeTracker
                )
            )

            viewModel.onRemoveProximityAlertClicked("123456")

            assertEquals(
                listOf(
                    UiAction.ShowConfirmRemoveProximityAlert(stopCode = "123456")
                ),
                actionTracker.items
            )
            assertEquals(listOf(null), selectedStopCodeTracker.items)
        }

    @Test
    fun onRemoveProxAlertClickedDoesNothingWhenInShortcutMode() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { true }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker
            )
        )

        viewModel.onRemoveProximityAlertClicked("123456")

        assertTrue(actionTracker.items.isEmpty())
    }

    @Test
    fun onShowOnMapClickedSetsShowOnMapActionAndDismissesDropdownMenu() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopCodeTracker = ItemTracker<String?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker,
                onSetSelectedStopCode = selectedStopCodeTracker
            )
        )

        viewModel.onShowOnMapClicked("123456")

        assertEquals(
            listOf(
                UiAction.ShowOnMap(stopCode = "123456")
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopCodeTracker.items)
    }

    @Test
    fun onShowOnMapClickedDoesNothingWhenInShortcutMode() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { true }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker
            )
        )

        viewModel.onShowOnMapClicked("123456")

        assertTrue(actionTracker.items.isEmpty())
    }

    @Test
    fun onActionLaunchedResetsActionToNull() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker
            )
        )

        viewModel.onActionLaunched()

        assertEquals(listOf(null), actionTracker.items)
    }

    private fun TestScope.createViewModel(
        arguments: Arguments = FakeArguments(),
        state: State = FakeState(
            onActionFlow = { emptyFlow() }
        ),
        uiFavouriteStopsRetriever: UiFavouriteStopsRetriever = FakeUiFavouriteStopsRetriever(
            onAllFavouriteStopsFlow = { emptyFlow() }
        )
    ): FavouriteStopsViewModel {
        return FavouriteStopsViewModel(
            arguments = arguments,
            state = state,
            uiFavouriteStopsRetriever = uiFavouriteStopsRetriever,
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
