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
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.shortcuts.FakeShortcutsRepository
import uk.org.rivernile.android.bustracker.core.shortcuts.FavouriteStopShortcut
import uk.org.rivernile.android.bustracker.core.shortcuts.ShortcutsRepository
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
                                    stopIdentifier = "123456".toNaptanStopIdentifier(),
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
                                stopIdentifier = "123456".toNaptanStopIdentifier(),
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
                        emit(UiAction.ShowStopData(
                            stopIdentifier = "123456".toNaptanStopIdentifier())
                        )
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
                                    stopIdentifier = "123456".toNaptanStopIdentifier(),
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
                                stopIdentifier = "123456".toNaptanStopIdentifier(),
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
                                stopIdentifier = "123456".toNaptanStopIdentifier(),
                                savedName = "Saved Name",
                                services = null,
                                dropdownMenu = null
                            )
                        )
                    ),
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

        viewModel.onItemClicked("123456".toNaptanStopIdentifier(), "Saved Name")

        assertEquals(
            listOf(
                UiAction.ShowStopData(stopIdentifier = "123456".toNaptanStopIdentifier())
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

        viewModel.onItemClicked("123456".toNaptanStopIdentifier(), "Saved Name")

        assertEquals(
            listOf(
                UiAction.AddShortcut(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    savedName = "Saved Name"
                )
            ),
            itemTracker.items
        )
    }

    @Test
    fun onItemOpenDropdownClickedSetsSelectedStopIdentifierWhenNotInShortcutMode() = runTest {
        val itemTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetSelectedStopIdentifier = itemTracker
            )
        )

        viewModel.onItemOpenDropdownClicked("123456".toNaptanStopIdentifier())

        assertEquals(listOf("123456".toNaptanStopIdentifier()), itemTracker.items)
    }

    @Test
    fun onItemOpenDropdownClickedDoesNothingWhenInShortcutMode() = runTest {
        val itemTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { true }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetSelectedStopIdentifier = itemTracker
            )
        )

        viewModel.onItemOpenDropdownClicked("123456".toNaptanStopIdentifier())

        assertTrue(itemTracker.items.isEmpty())
    }

    @Test
    fun onDropdownMenuDismissedSetsSelectedStopIdentifierToNullWhenNotInShortcutMode() = runTest {
        val itemTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetSelectedStopIdentifier = itemTracker
            )
        )

        viewModel.onDropdownMenuDismissed()

        assertEquals(listOf(null), itemTracker.items)
    }

    @Test
    fun onDropdownMenuDismissedDoesNothingWhenInShortcutMode() = runTest {
        val itemTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { true }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetSelectedStopIdentifier = itemTracker
            )
        )

        viewModel.onDropdownMenuDismissed()

        assertTrue(itemTracker.items.isEmpty())
    }

    @Test
    fun onEditFavouriteNameClickedSetsShowEditActionAndDismissesDropdownMenu() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker,
                onSetSelectedStopIdentifier = selectedStopIdentifierTracker
            )
        )

        viewModel.onEditFavouriteNameClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowEditFavouriteStop(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopIdentifierTracker.items)
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

        viewModel.onEditFavouriteNameClicked("123456".toNaptanStopIdentifier())

        assertTrue(actionTracker.items.isEmpty())
    }

    @Test
    fun onRemoveFavouriteClickedSetsShowConfirmRemoveActionAndDismissesDropdownMenu() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker,
                onSetSelectedStopIdentifier = selectedStopIdentifierTracker
            )
        )

        viewModel.onRemoveFavouriteClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowConfirmRemoveFavourite(
                    stopIdentifier = "123456".toNaptanStopIdentifier()
                )
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopIdentifierTracker.items)
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

        viewModel.onRemoveFavouriteClicked("123456".toNaptanStopIdentifier())

        assertTrue(actionTracker.items.isEmpty())
    }

    @Test
    fun onAddShortcutClickedAddsShortcutAndDismissesDropdownMenu() = runTest {
        val addShortcutTracker = ItemTracker<FavouriteStopShortcut>()
        val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetSelectedStopIdentifier = selectedStopIdentifierTracker
            ),
            shortcutsRepository = FakeShortcutsRepository(
                onPinFavouriteShortcut = addShortcutTracker
            )
        )

        viewModel.onAddShortcutClicked("123456".toNaptanStopIdentifier(), "Saved Name")

        assertEquals(
            listOf(
                FavouriteStopShortcut(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    displayName = "Saved Name"
                )
            ),
            addShortcutTracker.items
        )
        assertEquals(listOf(null), selectedStopIdentifierTracker.items)
    }

    @Test
    fun onAddShortcutClickedDoesNothingWhenInShortcutMode() = runTest {
        val addShortcutTracker = ItemTracker<FavouriteStopShortcut>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { true }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() }
            ),
            shortcutsRepository = FakeShortcutsRepository(
                onPinFavouriteShortcut = addShortcutTracker
            )
        )

        viewModel.onAddShortcutClicked("123456".toNaptanStopIdentifier(), "Saved Name")

        assertTrue(addShortcutTracker.items.isEmpty())
    }

    @Test
    fun onAddArrivalAlertClickedSetsShowAddArrivalAlertActionAndDismissesDropdownMenu() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker,
                onSetSelectedStopIdentifier = selectedStopIdentifierTracker
            )
        )

        viewModel.onAddArrivalAlertClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowAddArrivalAlert(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopIdentifierTracker.items)
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

        viewModel.onAddArrivalAlertClicked("123456".toNaptanStopIdentifier())

        assertTrue(actionTracker.items.isEmpty())
    }

    @Test
    fun onRemoveArrivalAlertClickedSetsShowConfirmRemoveAlertActionAndDismissesDropdownMenu() =
        runTest {
            val actionTracker = ItemTracker<UiAction?>()
            val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
            val viewModel = createViewModel(
                arguments = FakeArguments(
                    onIsShortcutMode = { false }
                ),
                state = FakeState(
                    onActionFlow = { emptyFlow() },
                    onSetAction = actionTracker,
                    onSetSelectedStopIdentifier = selectedStopIdentifierTracker
                )
            )

            viewModel.onRemoveArrivalAlertClicked("123456".toNaptanStopIdentifier())

            assertEquals(
                listOf(
                    UiAction.ShowConfirmRemoveArrivalAlert(
                        stopIdentifier = "123456".toNaptanStopIdentifier()
                    )
                ),
                actionTracker.items
            )
            assertEquals(listOf(null), selectedStopIdentifierTracker.items)
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

        viewModel.onRemoveArrivalAlertClicked("123456".toNaptanStopIdentifier())

        assertTrue(actionTracker.items.isEmpty())
    }

    @Test
    fun onAddProxAlertClickedSetsShowAddProxAlertActionAndDismissesDropdownMenu() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker,
                onSetSelectedStopIdentifier = selectedStopIdentifierTracker
            )
        )

        viewModel.onAddProximityAlertClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowAddProximityAlert(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopIdentifierTracker.items)
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

        viewModel.onAddProximityAlertClicked("123456".toNaptanStopIdentifier())

        assertTrue(actionTracker.items.isEmpty())
    }

    @Test
    fun onRemoveProxAlertClickedSetsShowConfirmRemoveAlertActionAndDismissesDropdownMenu() =
        runTest {
            val actionTracker = ItemTracker<UiAction?>()
            val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
            val viewModel = createViewModel(
                arguments = FakeArguments(
                    onIsShortcutMode = { false }
                ),
                state = FakeState(
                    onActionFlow = { emptyFlow() },
                    onSetAction = actionTracker,
                    onSetSelectedStopIdentifier = selectedStopIdentifierTracker
                )
            )

            viewModel.onRemoveProximityAlertClicked("123456".toNaptanStopIdentifier())

            assertEquals(
                listOf(
                    UiAction.ShowConfirmRemoveProximityAlert(
                        stopIdentifier = "123456".toNaptanStopIdentifier()
                    )
                ),
                actionTracker.items
            )
            assertEquals(listOf(null), selectedStopIdentifierTracker.items)
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

        viewModel.onRemoveProximityAlertClicked("123456".toNaptanStopIdentifier())

        assertTrue(actionTracker.items.isEmpty())
    }

    @Test
    fun onShowOnMapClickedSetsShowOnMapActionAndDismissesDropdownMenu() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onIsShortcutMode = { false }
            ),
            state = FakeState(
                onActionFlow = { emptyFlow() },
                onSetAction = actionTracker,
                onSetSelectedStopIdentifier = selectedStopIdentifierTracker
            )
        )

        viewModel.onShowOnMapClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowOnMap(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopIdentifierTracker.items)
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

        viewModel.onShowOnMapClicked("123456".toNaptanStopIdentifier())

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
        ),
        shortcutsRepository: ShortcutsRepository = FakeShortcutsRepository()
    ): FavouriteStopsViewModel {
        return FavouriteStopsViewModel(
            arguments = arguments,
            state = state,
            uiFavouriteStopsRetriever = uiFavouriteStopsRetriever,
            shortcutsRepository = shortcutsRepository,
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
