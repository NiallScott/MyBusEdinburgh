/*
 * Copyright (C) 2021 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites.addedit

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toParcelableNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toParcelableStopIdentifier
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [AddEditFavouriteStopDialogFragmentViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AddEditFavouriteStopDialogFragmentViewModelTest {

    companion object {

        private const val STATE_STOP_IDENTIFIER = "stopIdentifier"
        private const val STATE_PRE_POPULATED_STOP_IDENTIFIER = "prePopulatedStopIdentifier"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var favouritesRepository: FavouritesRepository
    @Mock
    private lateinit var fetcher: FavouriteStopFetcher
    @Mock
    private lateinit var textFormattingUtils: TextFormattingUtils

    @Test
    fun uiStateLiveDataEmitsStateFromUpstreamWithNullStopIdentifier() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails(null))
            .thenReturn(flowOf(UiState.InProgress))
        val viewModel = createViewModel(stopIdentifier = null)

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.InProgress)
    }

    @Test
    fun uiStateLiveDataEmitsStateFromUpstreamWithEmptyStopIdentifier() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("".toNaptanStopIdentifier()))
            .thenReturn(flowOf(UiState.InProgress))
        val viewModel = createViewModel(stopIdentifier = "".toNaptanStopIdentifier())

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.InProgress)
    }

    @Test
    fun uiStateLiveDataEmitsStateFromUpstreamWithPopulatedStopIdentifier() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(flow {
                emit(UiState.InProgress)
                delay(100L)
                emit(
                    UiState.Mode.Add(
                        "123456".toNaptanStopIdentifier(),
                        FakeStopName("Name", "Locality")
                    )
                )
            })
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
            UiState.InProgress,
            UiState.Mode.Add("123456".toNaptanStopIdentifier(), FakeStopName("Name", "Locality")))
    }

    @Test
    fun isPositiveButtonEnabledLiveDataIsFalseWhenStopNameIsNull() = runTest {
        val viewModel = createViewModel()

        viewModel.stopName = null
        val observer = viewModel.isPositiveButtonEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isPositiveButtonEnabledLiveDataIsFalseWhenStopNameIsEmpty() = runTest {
        val viewModel = createViewModel()

        viewModel.stopName = ""
        val observer = viewModel.isPositiveButtonEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isPositiveButtonEnabledLiveDataIsTrueWhenStopNameIsSingleCharacter() = runTest {
        val viewModel = createViewModel()

        viewModel.stopName = "a"
        val observer = viewModel.isPositiveButtonEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun isPositiveButtonEnabledLiveDataIsTrueWhenStopNameIsMultipleCharacters() = runTest {
        val viewModel = createViewModel()

        viewModel.stopName = "abc123"
        val observer = viewModel.isPositiveButtonEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun prePopulatedNameLiveDataDoesNotEmitWhenUiStateIsInProgress() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
                .thenReturn(flowOf(UiState.InProgress))
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        advanceUntilIdle()
        val observer = viewModel.prePopulateNameLiveData.test()

        observer.assertEmpty()
    }

    @Test
    fun prePopulatedNameLiveDataDoesNotEmitWhenUiStateIsInProgressWithState() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
                .thenReturn(flowOf(UiState.InProgress))
        val savedState = SavedStateHandle(
            mapOf(
                STATE_PRE_POPULATED_STOP_IDENTIFIER to "123456".toParcelableNaptanStopIdentifier()
            )
        )
        val viewModel = createViewModel(savedState, "123456".toNaptanStopIdentifier())

        advanceUntilIdle()
        val observer = viewModel.prePopulateNameLiveData.test()

        observer.assertEmpty()
    }

    @Test
    fun prePopulatedNameLiveDataEmitsStopIdOnFirstLoadForStopIdInAddModeWithNullStopName() =
            runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(flowOf(
                UiState.Mode.Add("123456".toNaptanStopIdentifier(), null))
            )
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        val observer = viewModel.prePopulateNameLiveData.test()
        advanceUntilIdle()

        assertEquals("123456", observer.observedValues.first().peek())
    }

    @Test
    fun prePopulatedNameLiveDataEmitsStopIdOnFirstLoadForStopIdInAddModeWithNonNullStopName() =
            runTest {
        val stopName = FakeStopName("Stop name", "Locality")
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(flowOf(
                UiState.Mode.Add("123456".toNaptanStopIdentifier(), stopName))
            )
        whenever(textFormattingUtils.formatBusStopName(stopName))
            .thenReturn("Formatted name")
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        val observer = viewModel.prePopulateNameLiveData.test()
        advanceUntilIdle()

        assertEquals("Formatted name", observer.observedValues.first().peek())
    }

    @Test
    fun prePopulatedNameLiveDataDoesNotEmitFurtherUpdateFrStopIdentifierInAddMode() = runTest {
        val stopName = FakeStopName("Stop name", "Locality")
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(flowOf(
                UiState.Mode.Add("123456".toNaptanStopIdentifier(), stopName))
            )
        val savedState = SavedStateHandle(
            mapOf(
                STATE_PRE_POPULATED_STOP_IDENTIFIER to "123456".toParcelableNaptanStopIdentifier()
            )
        )
        val viewModel = createViewModel(savedState, "123456".toNaptanStopIdentifier())

        val observer = viewModel.prePopulateNameLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun prePopulatedNameLiveDataEmitsCurrentFavouriteNameOnFirstLoadInEditMode() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(
                flowOf(
                    UiState.Mode.Edit(
                        "123456".toNaptanStopIdentifier(),
                        FakeStopName("Stop name", "Locality"),
                        FavouriteStop("123456".toNaptanStopIdentifier(), "Saved name")
                    )
                )
            )
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        val observer = viewModel.prePopulateNameLiveData.test()
        advanceUntilIdle()

        assertEquals("Saved name", observer.observedValues.first().peek())
    }

    @Test
    fun prePopulatedNameLiveDataDoesNotEmitFurtherUpdateFrStopIdentifierInEditMode() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(
                flowOf(
                    UiState.Mode.Edit(
                        "123456".toNaptanStopIdentifier(),
                        FakeStopName("Stop name", "Locality"),
                        FavouriteStop("123456".toNaptanStopIdentifier(), "Saved name")
                    )
                )
            )
        val savedState = SavedStateHandle(
            mapOf(
                STATE_PRE_POPULATED_STOP_IDENTIFIER to "123456".toParcelableNaptanStopIdentifier()
            )
        )
        val viewModel = createViewModel(savedState, "123456".toNaptanStopIdentifier())

        val observer = viewModel.prePopulateNameLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun onSubmitClickedWhenInProgressWithNullNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(flowOf(UiState.InProgress))
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        viewModel.uiStateLiveData.test()
        viewModel.stopName = null
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
                .addOrUpdateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInProgressWithEmptyNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(flowOf(UiState.InProgress))
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        viewModel.uiStateLiveData.test()
        viewModel.stopName = ""
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
            .addOrUpdateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInProgressWithPopulatedNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(flowOf(UiState.InProgress))
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        viewModel.uiStateLiveData.test()
        viewModel.stopName = "abc123"
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
            .addOrUpdateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInAddModeWithNullNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(
                flowOf(
                    UiState.Mode.Add(
                        "123456".toNaptanStopIdentifier(),
                        FakeStopName("Stop name", "Locality")
                    )
                )
            )
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        viewModel.uiStateLiveData.test()
        viewModel.stopName = null
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
            .addOrUpdateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInAddModeWithEmptyNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(
                flowOf(
                    UiState.Mode.Add(
                        "123456".toNaptanStopIdentifier(),
                        FakeStopName("Stop name", "Locality")
                    )
                )
            )
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        viewModel.uiStateLiveData.test()
        viewModel.stopName = ""
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
                .addOrUpdateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInAddModeWithPopulatedNameAddsFavouriteStop() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(
                flowOf(
                    UiState.Mode.Add(
                        "123456".toNaptanStopIdentifier(),
                        FakeStopName("Stop name", "Locality")
                    )
                )
            )
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.stopName = "abc123"
        advanceUntilIdle()
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository)
            .addOrUpdateFavouriteStop(
                FavouriteStop(
                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                    stopName = "abc123"
                )
            )
    }

    @Test
    fun onSubmitClickedWhenInEditModeWithNullNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(
                flowOf(
                    UiState.Mode.Edit(
                        "123456".toNaptanStopIdentifier(),
                        FakeStopName("Stop name", "Locality"),
                        FavouriteStop("123456".toNaptanStopIdentifier(), "Saved name")
                    )
                )
            )
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.stopName = null
        advanceUntilIdle()
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
                .addOrUpdateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInEditModeWithEmptyNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(
                flowOf(
                    UiState.Mode.Edit(
                        "123456".toNaptanStopIdentifier(),
                        FakeStopName("Stop name", "Locality"),
                        FavouriteStop("123456".toNaptanStopIdentifier(), "Saved name")
                    )
                )
            )
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.stopName = ""
        advanceUntilIdle()
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
                .addOrUpdateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInEditModeWithPopulatedNameEditsFavouriteStop() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456".toNaptanStopIdentifier()))
            .thenReturn(
                flowOf(
                    UiState.Mode.Edit(
                        "123456".toNaptanStopIdentifier(),
                        FakeStopName("Stop name", "Locality"),
                        FavouriteStop("123456".toNaptanStopIdentifier(), "Saved name")
                    )
                )
            )
        val viewModel = createViewModel(stopIdentifier = "123456".toNaptanStopIdentifier())

        viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.stopName = "abc123"
        advanceUntilIdle()
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository)
            .addOrUpdateFavouriteStop(FavouriteStop("123456".toNaptanStopIdentifier(), "abc123"))
    }

    private fun createViewModel(
        savedState: SavedStateHandle = SavedStateHandle(),
        stopIdentifier: StopIdentifier? = null
    ): AddEditFavouriteStopDialogFragmentViewModel {
        savedState[STATE_STOP_IDENTIFIER] = stopIdentifier?.toParcelableStopIdentifier()

        return AddEditFavouriteStopDialogFragmentViewModel(
            savedState,
            favouritesRepository,
            fetcher,
            textFormattingUtils,
            coroutineRule.testDispatcher,
            coroutineRule.scope
        )
    }
}
