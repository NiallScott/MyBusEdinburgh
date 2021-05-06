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

package uk.org.rivernile.android.bustracker.ui.favourites

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
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
@ExperimentalCoroutinesApi
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
    fun favouritesLiveDataEmitsNullFromUpstream() {
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(null))
        val viewModel = createViewModel()

        val observer = viewModel.favouritesLiveData.test()

        observer.assertValues(null)
    }

    @Test
    fun favouritesLiveDataEmitsEmptyListFromUpstream() {
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(emptyList()))
        val viewModel = createViewModel()

        val observer = viewModel.favouritesLiveData.test()

        observer.assertValues(emptyList())
    }

    @Test
    fun favouritesLiveDataEmitsPopulatedListFromUpstream() {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()

        val observer = viewModel.favouritesLiveData.test()

        observer.assertValues(favourites)
    }

    @Test
    fun uiStateLiveDataEmitsInProgressWhenFavouritesIsNull() {
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(null))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()

        observer.assertValues(UiState.PROGRESS)
    }

    @Test
    fun uiStateLiveDataEmitsErrorWhenFavouritesIsEmpty() {
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(emptyList()))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()

        observer.assertValues(UiState.ERROR)
    }

    @Test
    fun uiStateLiveDataEmitsContentWhenFavouritesIsPopulated() {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()

        val observer = viewModel.uiStateLiveData.test()

        observer.assertValues(UiState.CONTENT)
    }

    @Test
    fun showContextMenuLiveDataIsFalseByDefault() {
        val viewModel = createViewModel()

        val observer = viewModel.showContextMenuLiveData.test()

        observer.assertValues(false)
    }

    @Test
    fun showContextMenuLiveDataIsFalseWhenPreviousStateIsNull() {
        val viewModel = createViewModel(SavedStateHandle(
                mapOf(STATE_SELECTED_STOP_CODE to null)))

        val observer = viewModel.showContextMenuLiveData.test()

        observer.assertValues(false)
    }

    @Test
    fun showContextMenuLiveDataIsFalseWhenPreviousStateIsEmpty() {
        val viewModel = createViewModel(SavedStateHandle(
                mapOf(STATE_SELECTED_STOP_CODE to "")))

        val observer = viewModel.showContextMenuLiveData.test()

        observer.assertValues(false)
    }

    @Test
    fun showContextMenuLiveDataIsTrueWhenPreviousStateIsPopulated() {
        val viewModel = createViewModel(SavedStateHandle(
                mapOf(STATE_SELECTED_STOP_CODE to "123456")))

        val observer = viewModel.showContextMenuLiveData.test()

        observer.assertValues(true)
    }

    @Test
    fun showContextMenuLiveDataEmitsFalseWhenPreviousStateIsPopulatedThenStopIsUnselected() {
        val viewModel = createViewModel(SavedStateHandle(
                mapOf(STATE_SELECTED_STOP_CODE to "123456")))

        val observer = viewModel.showContextMenuLiveData.test()
        viewModel.onFavouriteStopUnselected()

        observer.assertValues(true, false)
    }

    @Test
    fun showContextMenuLiveDataIsFalseWhenStopIsLongClickedInShortcutMode() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = true

        val observer = viewModel.showContextMenuLiveData.test()
        val result = viewModel.onFavouriteStopLongClicked("123456")

        observer.assertValues(false)
        assertFalse(result)
    }

    @Test
    fun showContextMenuLiveDataIsFalseWhenStopIsLongClickedInNormalModeWithEmptyStopCode() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.showContextMenuLiveData.test()
        val result = viewModel.onFavouriteStopLongClicked("")

        observer.assertValues(false)
        assertFalse(result)
    }

    @Test
    fun showContextMenuLiveDataIsTrueWhenStopIsLongClickedInNormalModeWithPopulatedStopCode() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.showContextMenuLiveData.test()
        val result = viewModel.onFavouriteStopLongClicked("123456")

        observer.assertValues(false, true)
        assertTrue(result)
    }

    @Test
    fun showContextMenuLiveDataSelectThenUnselectCycle() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.showContextMenuLiveData.test()
        val result = viewModel.onFavouriteStopLongClicked("123456")
        viewModel.onFavouriteStopUnselected()

        observer.assertValues(false, true, false)
        assertTrue(result)
    }

    @Test
    fun selectedStopNameLiveDataIsNullByDefault() {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.selectedStopNameLiveData.test()

        observer.assertValues(null)
    }

    // FIXME: when coroutines fixes this bug
    @Ignore("Currently unable to test flows with delay during testing. Awaiting fix in coroutines")
    @Test
    fun selectedStopNameLiveDataUsesNameOfStopFromPreviousState() = coroutineRule.runBlockingTest {
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
        viewModel.onFavouriteStopUnselected()
        advanceUntilIdle()

        observer.assertValues(UiFavouriteName("222222", StopName("Name 2", null)), null)
    }

    @Test
    fun selectedStopNameLiveDataEmitsNullWhenStopCodeIsEmpty() {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.selectedStopNameLiveData.test()
        viewModel.onFavouriteStopLongClicked("")

        observer.assertValues(null)
    }

    @Test
    fun selectedStopNameLiveDataEmitsNullWhenStopIsNotFound() {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.selectedStopNameLiveData.test()
        viewModel.onFavouriteStopLongClicked("444444")

        observer.assertValues(null)
    }

    // FIXME: when coroutines fixes this bug
    @Ignore("Currently unable to test flows with delay during testing. Awaiting fix in coroutines")
    @Test
    fun selectedStopNameLiveDataEmitsNameWhenStopIsFound() {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.selectedStopNameLiveData.test()
        viewModel.onFavouriteStopLongClicked("222222")

        observer.assertValues(null, UiFavouriteName("222222", StopName("Name 2", null)))
    }

    // FIXME: when coroutines fixes this bug
    @Ignore("Currently unable to test flows with delay during testing. Awaiting fix in coroutines")
    @Test
    fun selectedStopNameLiveDataEmitNullWhenStopIsUnselected() {
        val favourites = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Name 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Name 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Name 3"), listOf("5", "6")))
        whenever(favouriteStopsRetriever.favouriteStopsFlow)
                .thenReturn(flowOf(favourites))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val observer = viewModel.selectedStopNameLiveData.test()
        viewModel.onFavouriteStopLongClicked("222222")
        viewModel.onFavouriteStopUnselected()

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
    fun isArrivalAlertEnabledLiveDataEmitsFalseByDefault() {
        val viewModel = createViewModel()

        val observer = viewModel.isArrivalAlertEnabledLiveData.test()

        observer.assertValues(false)
    }

    @Test
    fun isArrivalAlertEnabledLiveDataEmitsFalseWhenSelectedStopCodeIsEmpty() {
        val viewModel = createViewModel()

        val observer = viewModel.isArrivalAlertEnabledLiveData.test()
        viewModel.onFavouriteStopLongClicked("")

        observer.assertValues(false)
    }

    @Test
    fun isArrivalAlertEnabledLiveDataEmitsTrueWhenDoesNotHaveArrivalAlert() {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.isArrivalAlertEnabledLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")

        observer.assertValues(false, true)
    }

    @Test
    fun isArrivalAlertEnabledLiveDataEmitsTrueWhenDoesHaveArrivalAlert() {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val observer = viewModel.isArrivalAlertEnabledLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")

        observer.assertValues(false, true)
    }

    @Test
    fun isProximityAlertEnabledLiveDataEmitsFalseByDefault() {
        val viewModel = createViewModel()

        val observer = viewModel.isProximityAlertEnabledLiveData.test()

        observer.assertValues(false)
    }

    @Test
    fun isProximityAlertEnabledLiveDataEmitsFalseWhenSelectedStopCodeIsEmpty() {
        val viewModel = createViewModel()

        val observer = viewModel.isProximityAlertEnabledLiveData.test()
        viewModel.onFavouriteStopLongClicked("")

        observer.assertValues(false)
    }

    @Test
    fun isProximityAlertEnabledLiveDataEmitsTrueWhenDoesNotHaveProximityAlert() {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.isProximityAlertEnabledLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")

        observer.assertValues(false, true)
    }

    @Test
    fun isProximityAlertEnabledLiveDataEmitsTrueWhenDoesHaveProximityAlert() {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val observer = viewModel.isProximityAlertEnabledLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")

        observer.assertValues(false, true)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsFalseByDefault() {
        val viewModel = createViewModel()

        val observer = viewModel.hasArrivalAlertLiveData.test()

        observer.assertValues(false)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsFalseWhenSelectedStopCodeIsEmpty() {
        val viewModel = createViewModel()

        val observer = viewModel.hasArrivalAlertLiveData.test()
        viewModel.onFavouriteStopLongClicked("")

        observer.assertValues(false)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsFalseWhenDoesNotHaveArrivalAlert() {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.hasArrivalAlertLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")

        observer.assertValues(false)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsTrueWhenDoesHaveArrivalAlert() {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val observer = viewModel.hasArrivalAlertLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")

        observer.assertValues(false, true)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsFalseByDefault() {
        val viewModel = createViewModel()

        val observer = viewModel.hasProximityAlertLiveData.test()

        observer.assertValues(false)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsFalseWhenSelectedStopCodeIsEmpty() {
        val viewModel = createViewModel()

        val observer = viewModel.hasProximityAlertLiveData.test()
        viewModel.onFavouriteStopLongClicked("")

        observer.assertValues(false)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsFalseWhenDoesNotHaveProximityAlert() {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()

        val observer = viewModel.hasProximityAlertLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")

        observer.assertValues(false)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsTrueWhenDoesHaveProximityAlert() {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()

        val observer = viewModel.hasProximityAlertLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")

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
    fun onEditFavouriteClickedShowsEditFavouriteStopWhenFavouriteSelected() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showEditFavouriteStopObserver = viewModel.showEditFavouriteStopLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")
        val result = viewModel.onEditFavouriteClicked()

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
    fun onDeleteFavouriteClickedShowsDeleteFavouriteStopWhenFavouriteSelected() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showDeleteFavouriteStopObserver = viewModel.showConfirmDeleteFavouriteLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")
        val result = viewModel.onDeleteFavouriteClicked()

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
    fun onShowOnMapClickedShowsMapWhenFavouriteSelected() {
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showOnMapObserver = viewModel.showOnMapLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")
        val result = viewModel.onShowOnMapClicked()

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
    fun onProximityAlertClickedShowsAddProximityAlertWhenFavouriteSelectedAndNoProxAlert() {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showConfirmDeleteProximityAlertObserver =
                viewModel.showConfirmDeleteProximityAlertLiveData.test()
        val showAddProximityAlertObserver = viewModel.showAddProximityAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        val hasProximityAlertObserver = viewModel.hasProximityAlertLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")
        val result = viewModel.onProximityAlertClicked()

        showConfirmDeleteProximityAlertObserver.assertEmpty()
        showAddProximityAlertObserver.assertValues("123456")
        showContextMenuObserver.assertValues(false, true, false)
        hasProximityAlertObserver.assertValues(false)
        assertTrue(result)
    }

    @Test
    fun onProximityAlertClickedShowsRemoveProximityAlertWhenFavouriteSelectedAndHasProxAlert() {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showConfirmDeleteProximityAlertObserver =
                viewModel.showConfirmDeleteProximityAlertLiveData.test()
        val showAddProximityAlertObserver = viewModel.showAddProximityAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        val hasProximityAlertObserver = viewModel.hasProximityAlertLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")
        val result = viewModel.onProximityAlertClicked()

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
    fun onArrivalAlertClickedShowsAddArrivalAlertWhenFavouriteSelectedAndNoArrivalAlert() {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(false))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showConfirmDeleteArrivalAlertObserver =
                viewModel.showConfirmDeleteArrivalAlertLiveData.test()
        val showAddArrivalAlertObserver = viewModel.showAddArrivalAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        val hasArrivalAlertObserver = viewModel.hasArrivalAlertLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")
        val result = viewModel.onArrivalAlertClicked()

        showConfirmDeleteArrivalAlertObserver.assertEmpty()
        showAddArrivalAlertObserver.assertValues("123456")
        showContextMenuObserver.assertValues(false, true, false)
        hasArrivalAlertObserver.assertValues(false)
        assertTrue(result)
    }

    @Test
    fun onArrivalAlertClickedShowsRemoveArrivalAlertWhenFavouriteSelectedAndHasArrivalAlert() {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val viewModel = createViewModel()
        viewModel.isCreateShortcutMode = false

        val showConfirmDeleteArrivalAlertObserver =
                viewModel.showConfirmDeleteArrivalAlertLiveData.test()
        val showAddArrivalAlertObserver = viewModel.showAddArrivalAlertLiveData.test()
        val showContextMenuObserver = viewModel.showContextMenuLiveData.test()
        val hasArrivalAlertObserver = viewModel.hasArrivalAlertLiveData.test()
        viewModel.onFavouriteStopLongClicked("123456")
        val result = viewModel.onArrivalAlertClicked()

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