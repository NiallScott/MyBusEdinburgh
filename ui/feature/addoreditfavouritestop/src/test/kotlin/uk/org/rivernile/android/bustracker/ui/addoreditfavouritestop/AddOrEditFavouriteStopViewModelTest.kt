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

package uk.org.rivernile.android.bustracker.ui.addoreditfavouritestop

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.favourites.FakeFavouritesRepository
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.core.shortcuts.FakeShortcutsRepository
import uk.org.rivernile.android.bustracker.core.shortcuts.FavouriteStopShortcut
import uk.org.rivernile.android.bustracker.core.shortcuts.ShortcutsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail

/**
 * Tests for [AddOrEditFavouriteStopViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AddOrEditFavouriteStopViewModelTest {

    @Test
    fun uiStateFlowEmitsDefaultUiStateByDefault() = runTest {
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = ::emptyFlow
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsExpectedValues() = runTest {
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = {
                    flow {
                        emit(null)
                        delay(3L)
                        emit(UiAction.DismissDialog)
                    }
                }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = {
                    flow {
                        emit(UiContent.InProgress)
                        delay(1L)
                        emit(
                            UiContent.Mode.Add(
                                stopIdentifier = "123456".toNaptanStopIdentifier(),
                                stopName = null,
                                isPositiveButtonEnabled = false
                            )
                        )
                        delay(1L)
                        emit(
                            UiContent.Mode.Edit(
                                stopIdentifier = "123456".toNaptanStopIdentifier(),
                                stopName = null,
                                isPositiveButtonEnabled = true,
                                savedName = "Saved Name"
                            )
                        )
                    }
                }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    content = UiContent.InProgress,
                    action = null
                ),
                awaitItem()
            )
            assertEquals(
                UiState(
                    content = UiContent.Mode.Add(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        stopName = null,
                        isPositiveButtonEnabled = false
                    ),
                    action = null
                ),
                awaitItem()
            )
            assertEquals(
                UiState(
                    content = UiContent.Mode.Edit(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        stopName = null,
                        isPositiveButtonEnabled = true,
                        savedName = "Saved Name"
                    ),
                    action = null
                ),
                awaitItem()
            )
            assertEquals(
                UiState(
                    content = UiContent.Mode.Edit(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        stopName = null,
                        isPositiveButtonEnabled = true,
                        savedName = "Saved Name"
                    ),
                    action = UiAction.DismissDialog
                ),
                awaitItem()
            )
        }
    }

    @Test
    fun setStopNameTextSetsValueOnState() = runTest {
        val stopNameTexts = mutableListOf<String?>()
        val viewModel = createViewModel(
            state = FakeState(
                onSetStopNameText = {
                    stopNameTexts += it
                },
                onActionFlow = ::emptyFlow
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = ::emptyFlow
            )
        )

        viewModel.stopNameText = "Some Stop"

        assertEquals(listOf<String?>("Some Stop"), stopNameTexts)
    }

    @Test
    fun onGetStopNameTextGetsValueFromState() = runTest {
        val viewModel = createViewModel(
            state = FakeState(
                onGetStopNameText = {
                    "Some Stop"
                },
                onActionFlow = ::emptyFlow
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = ::emptyFlow
            )
        )

        val result = viewModel.stopNameText

        assertEquals("Some Stop", result)
    }

    @Test
    fun onAddButtonClickedShouldNotAddOrUpdateFavouriteStopWhenNameIsNull() = runTest {
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onGetStopIdentifier = { "123456".toNaptanStopIdentifier() }
            ),
            state = FakeState(
                onGetStopNameText = { null },
                onActionFlow = { emptyFlow() }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = { emptyFlow() }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onAddOrUpdateFavouriteStop = {
                    fail("Not expecting addOrUpdateFavouriteStop() to be called.")
                }
            )
        )

        viewModel.onAddButtonClicked()
    }

    @Test
    fun onAddButtonClickedShouldNotAddOrUpdateFavouriteStopWhenNameIsEmpty() = runTest {
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onGetStopIdentifier = { "123456".toNaptanStopIdentifier() }
            ),
            state = FakeState(
                onGetStopNameText = { "" },
                onActionFlow = { emptyFlow() }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = { emptyFlow() }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onAddOrUpdateFavouriteStop = {
                    fail("Not expecting addOrUpdateFavouriteStop() to be called.")
                }
            )
        )

        viewModel.onAddButtonClicked()
    }

    @Test
    fun onAddButtonClickedShouldNotAddOrUpdateFavouriteStopWhenStopIdentifierIsNull() = runTest {
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onGetStopIdentifier = { null }
            ),
            state = FakeState(
                onGetStopNameText = { "Stop Name" },
                onActionFlow = { emptyFlow() }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = { emptyFlow() }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onAddOrUpdateFavouriteStop = {
                    fail("Not expecting addOrUpdateFavouriteStop() to be called.")
                }
            )
        )

        viewModel.onAddButtonClicked()
    }

    @Test
    fun onAddButtonClickedAddsOrUpdatesFavouriteStop() = runTest {
        val addedOrUpdatedFavouriteStops = mutableListOf<FavouriteStop>()
        val updatedShortcuts = mutableListOf<FavouriteStopShortcut>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onGetStopIdentifier = { "123456".toNaptanStopIdentifier() }
            ),
            state = FakeState(
                onGetStopNameText = { "Stop Name" },
                onActionFlow = { emptyFlow() }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = { emptyFlow() }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onAddOrUpdateFavouriteStop = {
                    addedOrUpdatedFavouriteStops += it
                }
            ),
            shortcutsRepository = FakeShortcutsRepository(
                onUpdateFavouriteShortcut = {
                    updatedShortcuts += it
                }
            )
        )

        viewModel.onAddButtonClicked()

        assertEquals(
            listOf(
                FavouriteStop(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = "Stop Name"
                )
            ),
            addedOrUpdatedFavouriteStops
        )
        assertEquals(
            listOf(
                FavouriteStopShortcut(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    displayName = "Stop Name"
                )
            ),
            updatedShortcuts
        )
    }

    @Test
    fun onKeyboardActionButtonPressedShouldNotAddOrUpdateFavouriteStopWhenNameIsNull() = runTest {
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onGetStopIdentifier = { "123456".toNaptanStopIdentifier() }
            ),
            state = FakeState(
                onGetStopNameText = { null },
                onSetAction = {
                    fail("Not expecting any actions to be set.")
                },
                onActionFlow = { emptyFlow() }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = { emptyFlow() }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onAddOrUpdateFavouriteStop = {
                    fail("Not expecting addOrUpdateFavouriteStop() to be called.")
                }
            )
        )

        viewModel.onKeyboardActionButtonPressed()
    }

    @Test
    fun onKeyboardActionButtonPressedShouldNotAddOrUpdateFavouriteStopWhenNameIsEmpty() = runTest {
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onGetStopIdentifier = { "123456".toNaptanStopIdentifier() }
            ),
            state = FakeState(
                onGetStopNameText = { "" },
                onSetAction = {
                    fail("Not expecting any actions to be set.")
                },
                onActionFlow = { emptyFlow() }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = { emptyFlow() }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onAddOrUpdateFavouriteStop = {
                    fail("Not expecting addOrUpdateFavouriteStop() to be called.")
                }
            )
        )

        viewModel.onKeyboardActionButtonPressed()
    }

    @Test
    fun onKeyboardActionButtonPressedShouldNotAddOrUpdateFavouriteStopWhenStopIdentifierIsNull() =
        runTest {
            val actions = mutableListOf<UiAction?>()
            val viewModel = createViewModel(
                arguments = FakeArguments(
                    onGetStopIdentifier = { null }
                ),
                state = FakeState(
                    onGetStopNameText = { "Stop Name" },
                    onSetAction = {
                        actions += it
                    },
                    onActionFlow = { emptyFlow() }
                ),
                uiContentFetcher = FakeUiContentFetcher(
                    onUiContentFlow = { emptyFlow() }
                ),
                favouritesRepository = FakeFavouritesRepository(
                    onAddOrUpdateFavouriteStop = {
                        fail("Not expecting addOrUpdateFavouriteStop() to be called.")
                    }
                )
            )

            viewModel.onKeyboardActionButtonPressed()

            assertEquals(
                listOf<UiAction?>(
                    UiAction.DismissDialog
                ),
                actions
            )
        }

    @Test
    fun onKeyboardActionButtonPressedAddsOrUpdatesFavouriteStop() = runTest {
        val addedOrUpdatedFavouriteStops = mutableListOf<FavouriteStop>()
        val updatedShortcuts = mutableListOf<FavouriteStopShortcut>()
        val actions = mutableListOf<UiAction?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onGetStopIdentifier = { "123456".toNaptanStopIdentifier() }
            ),
            state = FakeState(
                onGetStopNameText = { "Stop Name" },
                onSetAction = {
                    actions += it
                },
                onActionFlow = { emptyFlow() }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = { emptyFlow() }
            ),
            favouritesRepository = FakeFavouritesRepository(
                onAddOrUpdateFavouriteStop = {
                    addedOrUpdatedFavouriteStops += it
                }
            ),
            shortcutsRepository = FakeShortcutsRepository(
                onUpdateFavouriteShortcut = {
                    updatedShortcuts += it
                }
            )
        )

        viewModel.onKeyboardActionButtonPressed()

        assertEquals(
            listOf(
                FavouriteStop(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = "Stop Name"
                )
            ),
            addedOrUpdatedFavouriteStops
        )
        assertEquals(
            listOf(
                FavouriteStopShortcut(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    displayName = "Stop Name"
                )
            ),
            updatedShortcuts
        )
        assertEquals(
            listOf<UiAction?>(
                UiAction.DismissDialog
            ),
            actions
        )
    }

    @Test
    fun onActionLaunchedSetsActionInStateToNull() = runTest {
        val actions = mutableListOf<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onSetAction = {
                    actions += it
                },
                onActionFlow = { emptyFlow() }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = { emptyFlow() }
            )
        )

        viewModel.onActionLaunched()

        assertEquals(
            listOf<UiAction?>(null),
            actions
        )
    }

    private fun TestScope.createViewModel(
        arguments: Arguments = FakeArguments(),
        state: State = FakeState(),
        uiContentFetcher: UiContentFetcher = FakeUiContentFetcher(),
        favouritesRepository: FavouritesRepository = FakeFavouritesRepository(),
        shortcutsRepository: ShortcutsRepository = FakeShortcutsRepository()
    ): AddOrEditFavouriteStopViewModel {
        return AddOrEditFavouriteStopViewModel(
            arguments = arguments,
            state = state,
            uiContentFetcher = uiContentFetcher,
            favouritesRepository = favouritesRepository,
            shortcutsRepository = shortcutsRepository,
            defaultCoroutineDispatcher = UnconfinedTestDispatcher(scheduler = testScheduler),
            applicationCoroutineScope = this,
            viewModelCoroutineScope = backgroundScope
        )
    }
}
