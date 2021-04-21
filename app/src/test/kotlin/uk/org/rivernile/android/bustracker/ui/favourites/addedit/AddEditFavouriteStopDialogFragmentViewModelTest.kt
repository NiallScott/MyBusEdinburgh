/*
 * Copyright (C) 2021 Niall 'Rivernile' Scott
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
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.database.settings.entities.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.LiveDataTestObserver
import uk.org.rivernile.android.bustracker.utils.Event

/**
 * Tests for [AddEditFavouriteStopDialogFragmentViewModel].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
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
    fun uiStateLiveDataEmitsStateFromUpstreamWithNullStopCode() {
        whenever(fetcher.loadFavouriteStopAndDetails(null))
                .thenReturn(flowOf(UiState.InProgress))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val viewModel = createViewModel()

        viewModel.stopCode = null
        viewModel.uiStateLiveData.observeForever(uiStateObserver)

        uiStateObserver.assertValues(UiState.InProgress)
    }

    @Test
    fun uiStateLiveDataEmitsStateFromUpstreamWithEmptyStopCode() {
        whenever(fetcher.loadFavouriteStopAndDetails(""))
                .thenReturn(flowOf(UiState.InProgress))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val viewModel = createViewModel()

        viewModel.stopCode = ""
        viewModel.uiStateLiveData.observeForever(uiStateObserver)

        uiStateObserver.assertValues(UiState.InProgress)
    }

    // FIXME: when coroutines fixes this bug
    @Ignore("Currently unable to test flows with delay during testing. Awaiting fix in coroutines")
    @Test
    fun uiStateLiveDataEmitsStateFromUpstreamWithPopulatedStopCode() = runBlockingTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flow {
                    emit(UiState.InProgress)
                    delay(100L)
                    emit(UiState.Mode.Add("123456", StopName("Name", "Locality")))
                })
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        advanceUntilIdle()

        uiStateObserver.assertValues(
                UiState.InProgress,
                UiState.Mode.Add("123456", StopName("Name", "Locality")))
    }

    @Test
    fun isPositiveButtonEnabledLiveDataIsFalseWhenStopNameIsNull() {
        val isPositiveButtonEnabledObserver = LiveDataTestObserver<Boolean>()
        val viewModel = createViewModel()

        viewModel.updateStopName(null)
        viewModel.isPositiveButtonEnabledLiveData.observeForever(isPositiveButtonEnabledObserver)

        isPositiveButtonEnabledObserver.assertValues(false)
    }

    @Test
    fun isPositiveButtonEnabledLiveDataIsFalseWhenStopNameIsEmpty() {
        val isPositiveButtonEnabledObserver = LiveDataTestObserver<Boolean>()
        val viewModel = createViewModel()

        viewModel.updateStopName("")
        viewModel.isPositiveButtonEnabledLiveData.observeForever(isPositiveButtonEnabledObserver)

        isPositiveButtonEnabledObserver.assertValues(false)
    }

    @Test
    fun isPositiveButtonEnabledLiveDataIsTrueWhenStopNameIsSingleCharacter() {
        val isPositiveButtonEnabledObserver = LiveDataTestObserver<Boolean>()
        val viewModel = createViewModel()

        viewModel.updateStopName("a")
        viewModel.isPositiveButtonEnabledLiveData.observeForever(isPositiveButtonEnabledObserver)

        isPositiveButtonEnabledObserver.assertValues(true)
    }

    @Test
    fun isPositiveButtonEnabledLiveDataIsTrueWhenStopNameIsMultipleCharacters() {
        val isPositiveButtonEnabledObserver = LiveDataTestObserver<Boolean>()
        val viewModel = createViewModel()

        viewModel.updateStopName("abc123")
        viewModel.isPositiveButtonEnabledLiveData.observeForever(isPositiveButtonEnabledObserver)

        isPositiveButtonEnabledObserver.assertValues(true)
    }

    @Test
    fun prePopulatedNameLiveDataDoesNotEmitWhenUiStateIsInProgress() {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(UiState.InProgress))
        val viewModel = createViewModel()
        val prePopulatedNameObserver = LiveDataTestObserver<Event<String>?>()

        viewModel.stopCode = "123456"
        viewModel.prePopulateNameLiveData.observeForever(prePopulatedNameObserver)

        prePopulatedNameObserver.assertEmpty()
    }

    @Test
    fun prePopulatedNameLiveDataDoesNotEmitWhenUiStateIsInProgressWithState() {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(UiState.InProgress))
        val savedState = SavedStateHandle(
                mapOf(STATE_PRE_POPULATED_STOP_CODE to "123456"))
        val viewModel = createViewModel(savedState)
        val prePopulatedNameObserver = LiveDataTestObserver<Event<String>?>()

        viewModel.stopCode = "123456"
        viewModel.prePopulateNameLiveData.observeForever(prePopulatedNameObserver)

        prePopulatedNameObserver.assertEmpty()
    }

    @Test
    fun prePopulatedNameLiveDataEmitsStopCodeOnFirstLoadForStopCodeInAddModeWithNullStopName() {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Add("123456", null)))
        val viewModel = createViewModel()
        val prePopulatedNameObserver = LiveDataTestObserver<Event<String>?>()

        viewModel.stopCode = "123456"
        viewModel.prePopulateNameLiveData.observeForever(prePopulatedNameObserver)

        assertEquals("123456", prePopulatedNameObserver.observedValues.first()?.peek())
    }

    @Test
    fun prePopulatedNameLiveDataEmitsStopCodeOnFirstLoadForStopCodeInAddModeWithNonNullStopName() {
        val stopName = StopName("Stop name", "Locality")
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Add("123456", stopName)))
        whenever(textFormattingUtils.formatBusStopName(stopName))
                .thenReturn("Formatted name")
        val viewModel = createViewModel()
        val prePopulatedNameObserver = LiveDataTestObserver<Event<String>?>()

        viewModel.stopCode = "123456"
        viewModel.prePopulateNameLiveData.observeForever(prePopulatedNameObserver)

        assertEquals("Formatted name", prePopulatedNameObserver.observedValues.first()?.peek())
    }

    @Test
    fun prePopulatedNameLiveDataDoesNotEmitFurtherUpdateFrStopCodeInAddMode() {
        val stopName = StopName("Stop name", "Locality")
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Add("123456", stopName)))
        val savedState = SavedStateHandle(
                mapOf(STATE_PRE_POPULATED_STOP_CODE to "123456"))
        val viewModel = createViewModel(savedState)
        val prePopulatedNameObserver = LiveDataTestObserver<Event<String>?>()

        viewModel.stopCode = "123456"
        viewModel.prePopulateNameLiveData.observeForever(prePopulatedNameObserver)

        prePopulatedNameObserver.assertEmpty()
    }

    @Test
    fun prePopulatedNameLiveDataEmitsCurrentFavouriteNameOnFirstLoadInEditMode() {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Edit(
                                "123456",
                                StopName("Stop name", "Locality"),
                                FavouriteStop(1, "123456", "Saved name"))))
        val viewModel = createViewModel()
        val prePopulatedNameObserver = LiveDataTestObserver<Event<String>?>()

        viewModel.stopCode = "123456"
        viewModel.prePopulateNameLiveData.observeForever(prePopulatedNameObserver)

        assertEquals("Saved name", prePopulatedNameObserver.observedValues.first()?.peek())
    }

    @Test
    fun prePopulatedNameLiveDataDoesNotEmitFurtherUpdateFrStopCodeInEditMode() {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Edit(
                                "123456",
                                StopName("Stop name", "Locality"),
                                FavouriteStop(1, "123456", "Saved name"))))
        val savedState = SavedStateHandle(
                mapOf(STATE_PRE_POPULATED_STOP_CODE to "123456"))
        val viewModel = createViewModel(savedState)
        val prePopulatedNameObserver = LiveDataTestObserver<Event<String>?>()

        viewModel.stopCode = "123456"
        viewModel.prePopulateNameLiveData.observeForever(prePopulatedNameObserver)

        prePopulatedNameObserver.assertEmpty()
    }

    @Test
    fun onSubmitClickedWhenInProgressWithNullNameDoesNothing() = runBlockingTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(UiState.InProgress))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.updateStopName(null)
        viewModel.onSubmitClicked()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInProgressWithEmptyNameDoesNothing() = runBlockingTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(UiState.InProgress))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.updateStopName("")
        viewModel.onSubmitClicked()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInProgressWithPopulatedNameDoesNothing() = runBlockingTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(UiState.InProgress))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.updateStopName("abc123")
        viewModel.onSubmitClicked()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInAddModeWithNullNameDoesNothing() = runBlockingTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Add("123456", StopName("Stop name", "Locality"))))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.updateStopName(null)
        viewModel.onSubmitClicked()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInAddModeWithEmptyNameDoesNothing() = runBlockingTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Add("123456", StopName("Stop name", "Locality"))))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.updateStopName("")
        viewModel.onSubmitClicked()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInAddModeWithPopulatedNameAddsFavouriteStop() = runBlockingTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Add("123456", StopName("Stop name", "Locality"))))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.updateStopName("abc123")
        viewModel.onSubmitClicked()

        verify(favouritesRepository)
                .addFavouriteStop(FavouriteStop(
                        stopCode = "123456",
                        stopName = "abc123"))
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInEditModeWithNullNameDoesNothing() = runBlockingTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Edit(
                                "123456",
                                StopName("Stop name", "Locality"),
                                FavouriteStop(1, "123456", "Saved name"))))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.updateStopName(null)
        viewModel.onSubmitClicked()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInEditModeWithEmptyNameDoesNothing() = runBlockingTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Edit(
                                "123456",
                                StopName("Stop name", "Locality"),
                                FavouriteStop(1, "123456", "Saved name"))))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.updateStopName("")
        viewModel.onSubmitClicked()

        verify(favouritesRepository, never())
                .addFavouriteStop(any())
        verify(favouritesRepository, never())
                .updateFavouriteStop(any())
    }

    @Test
    fun onSubmitClickedWhenInEditModeWithPopulatedNameEditsFavouriteStop() = runBlockingTest {
        whenever(fetcher.loadFavouriteStopAndDetails("123456"))
                .thenReturn(flowOf(
                        UiState.Mode.Edit(
                                "123456",
                                StopName("Stop name", "Locality"),
                                FavouriteStop(1, "123456", "Saved name"))))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val viewModel = createViewModel()

        viewModel.stopCode = "123456"
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.updateStopName("abc123")
        viewModel.onSubmitClicked()

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
                    coroutineRule)

    private val runBlockingTest = coroutineRule::runBlockingTest
}