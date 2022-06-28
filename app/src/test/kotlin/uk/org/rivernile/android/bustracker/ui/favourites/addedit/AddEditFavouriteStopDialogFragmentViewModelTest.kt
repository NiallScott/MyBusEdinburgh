/*
 * Copyright (C) 2021 - 2022 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.database.settings.entities.FavouriteStop
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

        private const val STATE_PRE_POPULATED_STOP_CODE = "prePopulatedStopCode"
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
    fun uiStateLiveDataEmitsStateFromUpstreamWithNullStopCode() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails(null))
                .thenReturn(flowOf(UiState.InProgress))
        val viewModel = createViewModel()

        viewModel.stopCode = null
        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.InProgress)
    }

    @Test
    fun uiStateLiveDataEmitsStateFromUpstreamWithEmptyStopCode() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails(""))
                .thenReturn(flowOf(UiState.InProgress))
        val viewModel = createViewModel()

        viewModel.stopCode = ""
        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.InProgress)
    }

    @Test
    fun uiStateLiveDataEmitsStateFromUpstreamWithPopulatedStopCode() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flow {
                    emit(UiState.InProgress)
                    delay(100L)
                    emit(UiState.Mode.Add("123456", StopName("Name", "Locality")))
                })
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(
                UiState.InProgress,
                UiState.Mode.Add("123456", StopName("Name", "Locality")))
    }

    @Test
    fun isPositiveButtonEnabledLiveDataIsFalseWhenStopNameIsNull() = runTest {
        val viewModel = createViewModel()

        viewModel.updateStopName(null)
        val observer = viewModel.isPositiveButtonEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isPositiveButtonEnabledLiveDataIsFalseWhenStopNameIsEmpty() = runTest {
        val viewModel = createViewModel()

        viewModel.updateStopName("")
        val observer = viewModel.isPositiveButtonEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isPositiveButtonEnabledLiveDataIsTrueWhenStopNameIsSingleCharacter() = runTest {
        val viewModel = createViewModel()

        viewModel.updateStopName("a")
        val observer = viewModel.isPositiveButtonEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun isPositiveButtonEnabledLiveDataIsTrueWhenStopNameIsMultipleCharacters() = runTest {
        val viewModel = createViewModel()

        viewModel.updateStopName("abc123")
        val observer = viewModel.isPositiveButtonEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun prePopulatedNameLiveDataDoesNotEmitWhenUiStateIsInProgress() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(UiState.InProgress))
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        advanceUntilIdle()
        val observer = viewModel.prePopulateNameLiveData.test()

        observer.assertEmpty()
    }

    @Test
    fun prePopulatedNameLiveDataDoesNotEmitWhenUiStateIsInProgressWithState() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(UiState.InProgress))
        val savedState = SavedStateHandle(
                mapOf(STATE_PRE_POPULATED_STOP_CODE to "123456"))
        val viewModel = createViewModel(savedState)

        viewModel.stopCode = "123456"
        advanceUntilIdle()
        val observer = viewModel.prePopulateNameLiveData.test()

        observer.assertEmpty()
    }

    @Test
    fun prePopulatedNameLiveDataEmitsStopCodeOnFirstLoadForStopCodeInAddModeWithNullStopName() =
            runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Add("123456", null)))
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        val observer = viewModel.prePopulateNameLiveData.test()
        advanceUntilIdle()

        assertEquals("123456", observer.observedValues.first().peek())
    }

    @Test
    fun prePopulatedNameLiveDataEmitsStopCodeOnFirstLoadForStopCodeInAddModeWithNonNullStopName() =
            runTest {
        val stopName = StopName("Stop name", "Locality")
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Add("123456", stopName)))
        whenever(textFormattingUtils.formatBusStopName(stopName))
                .thenReturn("Formatted name")
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        val observer = viewModel.prePopulateNameLiveData.test()
        advanceUntilIdle()

        assertEquals("Formatted name", observer.observedValues.first().peek())
    }

    @Test
    fun prePopulatedNameLiveDataDoesNotEmitFurtherUpdateFrStopCodeInAddMode() = runTest {
        val stopName = StopName("Stop name", "Locality")
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Add("123456", stopName)))
        val savedState = SavedStateHandle(
                mapOf(STATE_PRE_POPULATED_STOP_CODE to "123456"))
        val viewModel = createViewModel(savedState)

        viewModel.stopCode = "123456"
        val observer = viewModel.prePopulateNameLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun prePopulatedNameLiveDataEmitsCurrentFavouriteNameOnFirstLoadInEditMode() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Edit(
                                "123456",
                                StopName("Stop name", "Locality"),
                                FavouriteStop(1, "123456", "Saved name"))))
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        val observer = viewModel.prePopulateNameLiveData.test()
        advanceUntilIdle()

        assertEquals("Saved name", observer.observedValues.first().peek())
    }

    @Test
    fun prePopulatedNameLiveDataDoesNotEmitFurtherUpdateFrStopCodeInEditMode() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Edit(
                                "123456",
                                StopName("Stop name", "Locality"),
                                FavouriteStop(1, "123456", "Saved name"))))
        val savedState = SavedStateHandle(
                mapOf(STATE_PRE_POPULATED_STOP_CODE to "123456"))
        val viewModel = createViewModel(savedState)

        viewModel.stopCode = "123456"
        val observer = viewModel.prePopulateNameLiveData.test()
        advanceUntilIdle()

        observer.assertEmpty()
    }

    @Test
    fun onSubmitClickedWhenInProgressWithNullNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(UiState.InProgress))
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.test()
        viewModel.updateStopName(null)
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInProgressWithEmptyNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(UiState.InProgress))
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.test()
        viewModel.updateStopName("")
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInProgressWithPopulatedNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(UiState.InProgress))
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.test()
        viewModel.updateStopName("abc123")
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInAddModeWithNullNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Add("123456", StopName("Stop name", "Locality"))))
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.test()
        viewModel.updateStopName(null)
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInAddModeWithEmptyNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Add("123456", StopName("Stop name", "Locality"))))
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.test()
        viewModel.updateStopName("")
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInAddModeWithPopulatedNameAddsFavouriteStop() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Add("123456", StopName("Stop name", "Locality"))))
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.updateStopName("abc123")
        advanceUntilIdle()
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository)
                .addFavouriteStop(FavouriteStop(
                        stopCode = "123456",
                        stopName = "abc123"))
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInEditModeWithNullNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Edit(
                                "123456",
                                StopName("Stop name", "Locality"),
                                FavouriteStop(1, "123456", "Saved name"))))
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.updateStopName(null)
        advanceUntilIdle()
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInEditModeWithEmptyNameDoesNothing() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Edit(
                                "123456",
                                StopName("Stop name", "Locality"),
                                FavouriteStop(1, "123456", "Saved name"))))
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.updateStopName("")
        advanceUntilIdle()
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInEditModeWithPopulatedNameEditsFavouriteStop() = runTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Edit(
                                "123456",
                                StopName("Stop name", "Locality"),
                                FavouriteStop(1, "123456", "Saved name"))))
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.test()
        advanceUntilIdle()
        viewModel.updateStopName("abc123")
        advanceUntilIdle()
        viewModel.onSubmitClicked()
        advanceUntilIdle()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository)
                .updateFavouriteStop(FavouriteStop(1, "123456", "abc123"))
    }

    private fun createViewModel(savedState: SavedStateHandle = SavedStateHandle()) =
            AddEditFavouriteStopDialogFragmentViewModel(
                    savedState,
                    favouritesRepository,
                    fetcher,
                    textFormattingUtils,
                    coroutineRule.testDispatcher,
                    coroutineRule.scope)
}