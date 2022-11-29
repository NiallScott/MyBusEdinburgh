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

package uk.org.rivernile.android.bustracker.ui.favourites

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.database.settings.entities.FavouriteStop
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [FavouriteStopsFragmentViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class FavouriteStopsFragmentViewModelTest {

    companion object {

        private const val STATE_SELECTED_STOP_CODE = "selectedStopCode"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var favouriteStopsRetriever: FavouriteStopsRetriever
    @Mock
    private lateinit var alertsRepository: AlertsRepository
    @Mock
    private lateinit var featureRepository: FeatureRepository

    @Test
    fun favouritesLiveDataEmitsNullFromUpstream() = runTest {
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(null))
        val viewModel = createViewModel()

        val observer = viewModel.favouritesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun favouritesLiveDataEmitsEmptyListFromUpstream() = runTest {
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(emptyList()))
        val viewModel = createViewModel()

        val observer = viewModel.favouritesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(emptyList())
    }

    @Test
    fun favouritesLiveDataEmitsPopulatedListFromUpstream() = runTest {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()

        val observer = viewModel.favouritesLiveData.test()
        advanceUntilIdle()

        observer.assertValues(favourites)
    }

    @Test
    fun uiStateLiveDataEmitsInProgressWhenFavouritesIsNull() = runTest {
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(null))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.PROGRESS)
    }

    @Test
    fun uiStateLiveDataEmitsErrorWhenFavouritesIsEmpty() = runTest {
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(emptyList()))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.ERROR)
    }

    @Test
    fun uiStateLiveDataEmitsContentWhenFavouritesIsPopulated() = runTest {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()
        advanceUntilIdle()

        observer.assertValues(UiState.CONTENT)
    }

    @Test
    fun showContextMenuLiveDataIsFalseByDefault() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun showContextMenuLiveDataIsFalseWhenPreviousStateIsNull() = runTest {
        val viewModel = createViewModel(SavedStateHandle(
                mapOf(STATE_SELECTED_STOP_CODE to null)))

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun showContextMenuLiveDataIsFalseWhenPreviousStateIsEmpty() = runTest {
        val viewModel = createViewModel(SavedStateHandle(
                mapOf(STATE_SELECTED_STOP_CODE to "")))

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun showContextMenuLiveDataIsTrueWhenPreviousStateIsPopulated() = runTest {
        val viewModel = createViewModel(SavedStateHandle(
                mapOf(STATE_SELECTED_STOP_CODE to "123456")))

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()

        observer.assertValues(true)
    }

    @Test
    fun showContextMenuLiveDataEmitsFalseWhenPreviousStateIsPopulatedThenStopIsUnselected() =
            runTest {
        val viewModel = createViewModel(SavedStateHandle(
                mapOf(STATE_SELECTED_STOP_CODE to "123456")))

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopUnselected()
        advanceUntilIdle()

        observer.assertValues(true, false)
    }

    @Test
    fun showContextMenuLiveDataIsFalseWhenStopIsLongClickedInShortcutMode() = runTest {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = true

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        val result = viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()

        observer.assertValues(false)
        assertFalse(result)
    }

    @Test
    fun showContextMenuLiveDataIsFalseWhenStopIsLongClickedInNormalModeWithEmptyStopCode() =
            runTest {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        val result = viewModel.onFavouriteStopLongClicked("")
        advanceUntilIdle()

        observer.assertValues(false)
        assertFalse(result)
    }

    @Test
    fun showContextMenuLiveDataIsTrueWhenStopIsLongClickedInNormalModeWithPopulatedStopCode() =
            runTest {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        val result = viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()

        observer.assertValues(false, true)
        assertTrue(result)
    }

    @Test
    fun showContextMenuLiveDataSelectThenUnselectCycle() = runTest {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        val result = viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()
        viewModel.onFavouriteStopUnselected()
        advanceUntilIdle()

        observer.assertValues(false, true, false)
        assertTrue(result)
    }

    @Test
    fun selectedStopNameLiveDataIsNullByDefault() = runTest {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.selectedStopNameLiveData.test()
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun selectedStopNameLiveDataUsesNameOfStopFromPreviousState() = runTest {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel(SavedStateHandle(
                mapOf(STATE_SELECTED_STOP_CODE to "222222")))
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.selectedStopNameLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopUnselected()
        advanceUntilIdle()

        observer.assertValues(UiFavouriteName("222222", StopName("Name 2", null)), null)
    }

    @Test
    fun selectedStopNameLiveDataEmitsNullWhenStopCodeIsEmpty() = runTest {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.selectedStopNameLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("")
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun selectedStopNameLiveDataEmitsNullWhenStopIsNotFound() = runTest {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.selectedStopNameLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("444444")
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun selectedStopNameLiveDataEmitsNameWhenStopIsFound() = runTest {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.selectedStopNameLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("222222")
        advanceUntilIdle()

        observer.assertValues(null, UiFavouriteName("222222", StopName("Name 2", null)))
    }

    @Test
    fun selectedStopNameLiveDataEmitNullWhenStopIsUnselected() = runTest {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.selectedStopNameLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("222222")
        advanceUntilIdle()
        viewModel.onFavouriteStopUnselected()
        advanceUntilIdle()

        observer.assertValues(null, UiFavouriteName("222222", StopName("Name 2", null)), null)
    }

    @Test
    fun isStopMapVisibleLiveDataEmitsFalseWhenDoesNotHaveStopMapFeature() {
        whenever(featureRepository.hasStopMapUiFeature)
                .thenReturn(false)
        val viewModel = createViewModel()

        val observer = viewModel.isStopMapVisibleLiveData.test()

        observer.assertValues(false)
    }

    @Test
    fun isStopMapVisibleLiveDataEmitsTrueWhenDoesHaveStopMapFeature() {
        whenever(featureRepository.hasStopMapUiFeature)
                .thenReturn(true)
        val viewModel = createViewModel()

        val observer = viewModel.isStopMapVisibleLiveData.test()

        observer.assertValues(true)
    }

    @Test
    fun isArrivalAlertVisibleLiveDataEmitsFalseWhenDoesNotHaveArrivalAlertFeature() {
        whenever(featureRepository.hasArrivalAlertFeature)
                .thenReturn(false)
        val viewModel = createViewModel()

        val observer = viewModel.isArrivalAlertVisibleLiveData.test()

        observer.assertValues(false)
    }

    @Test
    fun isArrivalAlertVisibleLiveDataEmitsTrueWhenDoesHaveArrivalAlertFeature() {
        whenever(featureRepository.hasArrivalAlertFeature)
                .thenReturn(true)
        val viewModel = createViewModel()

        val observer = viewModel.isArrivalAlertVisibleLiveData.test()

        observer.assertValues(true)
    }

    @Test
    fun isProximityAlertVisibleLiveDataEmitsFalseWhenDoesNotHaveProximityAlertFeature() {
        whenever(featureRepository.hasProximityAlertFeature)
                .thenReturn(false)
        val viewModel = createViewModel()

        val observer = viewModel.isProximityAlertVisibleLiveData.test()

        observer.assertValues(false)
    }

    @Test
    fun isProximityAlertVisibleLiveDataEmitsTrueWhenDoesHaveProximityAlertFeature() {
        whenever(featureRepository.hasProximityAlertFeature)
                .thenReturn(true)
        val viewModel = createViewModel()

        val observer = viewModel.isProximityAlertVisibleLiveData.test()

        observer.assertValues(true)
    }

    @Test
    fun isArrivalAlertEnabledLiveDataEmitsFalseByDefault() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.isArrivalAlertEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isArrivalAlertEnabledLiveDataEmitsFalseWhenSelectedStopCodeIsEmpty()  = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.isArrivalAlertEnabledLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("")
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isArrivalAlertEnabledLiveDataEmitsTrueWhenDoesNotHaveArrivalAlert() = runTest {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.isArrivalAlertEnabledLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()

        observer.assertValues(false, true)
    }

    @Test
    fun isArrivalAlertEnabledLiveDataEmitsTrueWhenDoesHaveArrivalAlert() = runTest {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val observer = viewModel.isArrivalAlertEnabledLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()

        observer.assertValues(false, true)
    }

    @Test
    fun isProximityAlertEnabledLiveDataEmitsFalseByDefault() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.isProximityAlertEnabledLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isProximityAlertEnabledLiveDataEmitsFalseWhenSelectedStopCodeIsEmpty() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.isProximityAlertEnabledLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("")
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun isProximityAlertEnabledLiveDataEmitsTrueWhenDoesNotHaveProximityAlert() = runTest {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.isProximityAlertEnabledLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()

        observer.assertValues(false, true)
    }

    @Test
    fun isProximityAlertEnabledLiveDataEmitsTrueWhenDoesHaveProximityAlert() = runTest {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val observer = viewModel.isProximityAlertEnabledLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()

        observer.assertValues(false, true)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsFalseByDefault() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.hasArrivalAlertLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsFalseWhenSelectedStopCodeIsEmpty() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.hasArrivalAlertLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("")
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsFalseWhenDoesNotHaveArrivalAlert() = runTest {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.hasArrivalAlertLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsTrueWhenDoesHaveArrivalAlert() = runTest {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val observer = viewModel.hasArrivalAlertLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()

        observer.assertValues(false, true)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsFalseByDefault() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.hasProximityAlertLiveData.test()
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsFalseWhenSelectedStopCodeIsEmpty() = runTest {
        val viewModel = createViewModel()

        val observer = viewModel.hasProximityAlertLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("")
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsFalseWhenDoesNotHaveProximityAlert() = runTest {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.hasProximityAlertLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()

        observer.assertValues(false)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsTrueWhenDoesHaveProximityAlert() = runTest {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val observer = viewModel.hasProximityAlertLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()

        observer.assertValues(false, true)
    }

    @Test
    fun onFavouriteStopClickedWhenInShortcutModeCallsCreateShortcut() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = true

        val createShortcutObserver = viewModel.createShortcutLiveData.test()
        val showStopDataObserver = viewModel.showStopDataLiveData.test()
        viewModel.onFavouriteStopClicked(FavouriteStop(1L, "123456", "Stop name"))

        createShortcutObserver.assertValues(FavouriteStop(1L, "123456", "Stop name"))
        showStopDataObserver.assertEmpty()
    }

    @Test
    fun onFavouriteStopClickedWhenInNormalModeAndStopSelectedDoesNotShowStopData() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false
        viewModel.onFavouriteStopLongClicked("123456")

        val createShortcutObserver = viewModel.createShortcutLiveData.test()
        val showStopDataObserver = viewModel.showStopDataLiveData.test()
        viewModel.onFavouriteStopClicked(FavouriteStop(1L, "123456", "Stop name"))

        createShortcutObserver.assertEmpty()
        showStopDataObserver.assertEmpty()
    }

    @Test
    fun onFavouriteStopClickedWhenInNormalModeCallsShowStopData() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val createShortcutObserver = viewModel.createShortcutLiveData.test()
        val showStopDataObserver = viewModel.showStopDataLiveData.test()
        viewModel.onFavouriteStopClicked(FavouriteStop(1L, "123456", "Stop name"))

        createShortcutObserver.assertEmpty()
        showStopDataObserver.assertValues("123456")
    }

    @Test
    fun onEditFavouriteClickedDoesNotShowEditFavouriteStopWhenNoFavouriteSelected() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showEditFavouriteStopObserver = viewModel.showEditFavouriteStopLiveData.test()
        val result = viewModel.onEditFavouriteClicked()

        showEditFavouriteStopObserver.assertEmpty()
        assertFalse(result)
    }

    @Test
    fun onEditFavouriteClickedShowsEditFavouriteStopWhenFavouriteSelected() = runTest {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showEditFavouriteStopObserver = viewModel.showEditFavouriteStopLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        val result = viewModel.onEditFavouriteClicked()
        advanceUntilIdle()

        showEditFavouriteStopObserver.assertValues("123456")
        showContextMenuObserver.assertValues(false, true, false)
        assertTrue(result)
    }

    @Test
    fun onDeleteFavouriteClickedDoesNotShowDeleteFavouriteStopWhenNoFavouriteSelected() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showConfirmDeleteFavouriteObserver = viewModel.showConfirmDeleteFavouriteLiveData.test()
        val result = viewModel.onDeleteFavouriteClicked()

        showConfirmDeleteFavouriteObserver.assertEmpty()
        assertFalse(result)
    }

    @Test
    fun onDeleteFavouriteClickedShowsDeleteFavouriteStopWhenFavouriteSelected() = runTest {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showDeleteFavouriteStopObserver = viewModel.showConfirmDeleteFavouriteLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        val result = viewModel.onDeleteFavouriteClicked()
        advanceUntilIdle()

        showDeleteFavouriteStopObserver.assertValues("123456")
        showContextMenuObserver.assertValues(false, true, false)
        assertTrue(result)
    }

    @Test
    fun onShowOnMapClickedDoesNotShowMapWhenNoFavouriteSelected() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showOnMapObserver = viewModel.showOnMapLiveData.test()
        val result = viewModel.onShowOnMapClicked()

        showOnMapObserver.assertEmpty()
        assertFalse(result)
    }

    @Test
    fun onShowOnMapClickedShowsMapWhenFavouriteSelected() = runTest {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showOnMapObserver = viewModel.showOnMapLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()

        val result = viewModel.onShowOnMapClicked()
        advanceUntilIdle()

        showOnMapObserver.assertValues("123456")
        showContextMenuObserver.assertValues(false, true, false)
        assertTrue(result)
    }

    @Test
    fun onProximityAlertClickedPerformsNoActionWhenNoFavouriteSelected() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showConfirmDeleteProximityAlertObserver =
                viewModel.showConfirmDeleteProximityAlertLiveData.test()
        val showAddProximityAlertObserver = viewModel.showAddProximityAlertLiveData.test()
        val result = viewModel.onProximityAlertClicked()

        showConfirmDeleteProximityAlertObserver.assertEmpty()
        showAddProximityAlertObserver.assertEmpty()
        assertFalse(result)
    }

    @Test
    fun onProximityAlertClickedShowsAddProximityAlertWhenFavouriteSelectedAndNoProxAlert() =
            runTest {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showConfirmDeleteProximityAlertObserver =
                viewModel.showConfirmDeleteProximityAlertLiveData.test()
        val showAddProximityAlertObserver = viewModel.showAddProximityAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        val hasProximityAlertObserver = viewModel.hasProximityAlertLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onProximityAlertClicked()
        advanceUntilIdle()

        showConfirmDeleteProximityAlertObserver.assertEmpty()
        showAddProximityAlertObserver.assertValues("123456")
        showContextMenuObserver.assertValues(false, true, false)
        hasProximityAlertObserver.assertValues(false)
        assertTrue(result)
    }

    @Test
    fun onProximityAlertClickedShowsRemoveProximityAlertWhenFavouriteSelectedAndHasProxAlert() =
            runTest {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showConfirmDeleteProximityAlertObserver =
                viewModel.showConfirmDeleteProximityAlertLiveData.test()
        val showAddProximityAlertObserver = viewModel.showAddProximityAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        val hasProximityAlertObserver = viewModel.hasProximityAlertLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onProximityAlertClicked()
        advanceUntilIdle()

        showConfirmDeleteProximityAlertObserver.assertValues("123456")
        showAddProximityAlertObserver.assertEmpty()
        showContextMenuObserver.assertValues(false, true, false)
        hasProximityAlertObserver.assertValues(false, true, false)
        assertTrue(result)
    }

    @Test
    fun onArrivalAlertClickedPerformsNoActionWhenNoFavouriteSelected() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showConfirmDeleteArrivalAlertObserver =
                viewModel.showConfirmDeleteArrivalAlertLiveData.test()
        val showAddArrivalAlertObserver = viewModel.showAddArrivalAlertLiveData.test()
        val result = viewModel.onArrivalAlertClicked()

        showConfirmDeleteArrivalAlertObserver.assertEmpty()
        showAddArrivalAlertObserver.assertEmpty()
        assertFalse(result)
    }

    @Test
    fun onArrivalAlertClickedShowsAddArrivalAlertWhenFavouriteSelectedAndNoArrivalAlert() =
            runTest {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showConfirmDeleteArrivalAlertObserver =
                viewModel.showConfirmDeleteArrivalAlertLiveData.test()
        val showAddArrivalAlertObserver = viewModel.showAddArrivalAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        val hasArrivalAlertObserver = viewModel.hasArrivalAlertLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onArrivalAlertClicked()
        advanceUntilIdle()

        showConfirmDeleteArrivalAlertObserver.assertEmpty()
        showAddArrivalAlertObserver.assertValues("123456")
        showContextMenuObserver.assertValues(false, true, false)
        hasArrivalAlertObserver.assertValues(false)
        assertTrue(result)
    }

    @Test
    fun onArrivalAlertClickedShowsRemoveArrivalAlertWhenFavouriteSelectedAndHasArrivalAlert() =
            runTest {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showConfirmDeleteArrivalAlertObserver =
                viewModel.showConfirmDeleteArrivalAlertLiveData.test()
        val showAddArrivalAlertObserver = viewModel.showAddArrivalAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        val hasArrivalAlertObserver = viewModel.hasArrivalAlertLiveData.test()
        advanceUntilIdle()
        viewModel.onFavouriteStopLongClicked("123456")
        advanceUntilIdle()
        val result = viewModel.onArrivalAlertClicked()
        advanceUntilIdle()

        showConfirmDeleteArrivalAlertObserver.assertValues("123456")
        showAddArrivalAlertObserver.assertEmpty()
        showContextMenuObserver.assertValues(false, true, false)
        hasArrivalAlertObserver.assertValues(false, true, false)
        assertTrue(result)
    }

    private fun createViewModel(savedState: SavedStateHandle = SavedStateHandle()) =
            FavouriteStopsFragmentViewModel(
                    savedState,
                    favouriteStopsRetriever,
                    alertsRepository,
                    featureRepository,
                    coroutineRule.testDispatcher)
}