/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [DisplayStopDataActivityViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class DisplayStopDataActivityViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var favouritesRepository: FavouritesRepository
    @Mock
    private lateinit var alertsRepository: AlertsRepository

    private lateinit var viewModel: DisplayStopDataActivityViewModel

    @Before
    fun setUp() {
        viewModel = DisplayStopDataActivityViewModel(
            busStopsRepository,
            favouritesRepository,
            alertsRepository,
            coroutineRule.testDispatcher)
    }

    @Test
    fun stopCodeLiveDataIsNullByDefault() {
        val observer = viewModel.stopCodeLiveData.test()

        observer.assertValues(null)
    }

    @Test
    fun stopCodeLiveDataOnlyEmitsDistinctCodeOnce() = runTest {
        val observer = viewModel.stopCodeLiveData.test()

        viewModel.stopCode = "123"
        advanceUntilIdle()
        viewModel.stopCode = "123"
        advanceUntilIdle()
        viewModel.stopCode = "123"
        advanceUntilIdle()

        observer.assertValues(null, "123")
    }

    @Test
    fun stopCodeLiveDataEmitsGivenSequenceCorrectly() = runTest {
        val observer = viewModel.stopCodeLiveData.test()

        viewModel.stopCode = "1"
        advanceUntilIdle()
        viewModel.stopCode = "2"
        advanceUntilIdle()
        viewModel.stopCode = "2"
        advanceUntilIdle()
        viewModel.stopCode = "3"
        advanceUntilIdle()

        observer.assertValues(null, "1", "2", "3")
    }

    @Test
    fun busStopDetailsWithNullStopCodeEmitsNullDetails() {
        val observer = viewModel.stopDetailsLiveData.test()

        viewModel.stopCode = null

        observer.assertValues(null)
        assertNull(viewModel.stopDetailsLiveData.value)
    }

    @Test
    fun busStopDetailsWithEmptyStopCodeEmitsNullDetails() {
        val observer = viewModel.stopDetailsLiveData.test()

        viewModel.stopCode = ""

        observer.assertValues(null)
        assertNull(viewModel.stopDetailsLiveData.value)
    }

    @Test
    fun busStopDetailsWithStopCodeWhichIsNotFoundReturnsNullDetails() = runTest {
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flowOf(null))
        val observer = viewModel.stopDetailsLiveData.test()

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun busStopDetailsWithStopCodeWhichIsFoundReturnsDetails() = runTest {
        val details = StopDetails(
                "123456",
                StopName(
                        "Name",
                        "Locality"),
                1.2,
                3.4,
                5)
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flowOf(details))
        val observer = viewModel.stopDetailsLiveData.test()

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        observer.assertValues(null, details)
    }

    @Test
    fun busStopDetailsWithStopCodesCanUpdateData() = runTest {
        val details = StopDetails(
                "123456",
                StopName(
                        "Name",
                        "Locality"),
                1.2,
                3.4,
                5)
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(intervalFlowOf(0L, 10L, null, details, null))
        val observer = viewModel.stopDetailsLiveData.test()

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        observer.assertValues(null, details, null)
    }

    @Test
    fun isFavouriteLiveDataEmitsNullWhenStopCodeIsNull() {
        val observer = viewModel.isFavouriteLiveData.test()

        viewModel.stopCode = null

        observer.assertValues(null)
    }

    @Test
    fun isFavouriteLiveDataEmitsNullWhenStopCodeIsEmpty() {
        val observer = viewModel.isFavouriteLiveData.test()

        viewModel.stopCode = ""

        observer.assertValues(null)
    }

    @Test
    fun isFavouriteLiveDataEmitsNullFollowedByRepositoryValue() = runTest {
        whenever(favouritesRepository.isStopAddedAsFavouriteFlow("123456"))
                .thenReturn(flowOf(true))
        val observer = viewModel.isFavouriteLiveData.test()

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        observer.assertValues(null, true)
    }

    @Test
    fun isFavouriteLiveDataCopesWithStopChange() = runTest {
        whenever(favouritesRepository.isStopAddedAsFavouriteFlow("123"))
                .thenReturn(intervalFlowOf(10L, 0L, false))
        whenever(favouritesRepository.isStopAddedAsFavouriteFlow("456"))
                .thenReturn(intervalFlowOf(10L, 0L, true))
        val observer = viewModel.isFavouriteLiveData.test()

        viewModel.stopCode = "123"
        advanceUntilIdle()
        viewModel.stopCode = "456"
        advanceUntilIdle()

        observer.assertValues(null, false, null, true)
    }

    @Test
    fun isFavouriteLiveDataOnlyEmitsDistinctValues() = runTest {
        whenever(favouritesRepository.isStopAddedAsFavouriteFlow("123456"))
                .thenReturn(intervalFlowOf(0L, 10L, true, false, false, false, true))
        val observer = viewModel.isFavouriteLiveData.test()

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        observer.assertValues(null, true, false, true)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsNullWhenStopCodeIsNull() {
        val observer = viewModel.hasArrivalAlertLiveData.test()

        viewModel.stopCode = null

        observer.assertValues(null)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsNullWhenStopCodeIsEmpty() {
        val observer = viewModel.hasArrivalAlertLiveData.test()

        viewModel.stopCode = ""

        observer.assertValues(null)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsNullFollowedByRepositoryValue() = runTest {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val observer = viewModel.hasArrivalAlertLiveData.test()

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        observer.assertValues(null, true)
    }

    @Test
    fun hasArrivalAlertLiveDataCopesWithStopChange() = runTest {
        whenever(alertsRepository.hasArrivalAlertFlow("123"))
                .thenReturn(intervalFlowOf(10L, 0L, false))
        whenever(alertsRepository.hasArrivalAlertFlow("456"))
                .thenReturn(intervalFlowOf(10L, 0L, true))
        val observer = viewModel.hasArrivalAlertLiveData.test()

        viewModel.stopCode = "123"
        advanceUntilIdle()
        viewModel.stopCode = "456"
        advanceUntilIdle()

        observer.assertValues(null, false, null, true)
    }

    @Test
    fun hasArrivalAlertLiveDataOnlyEmitsDistinctValues() = runTest {
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(intervalFlowOf(0L, 10L, true, false, false, false, true))
        val observer = viewModel.hasArrivalAlertLiveData.test()

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        observer.assertValues(null, true, false, true)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsNullWhenStopCodeIsNull() {
        val observer = viewModel.hasProximityAlertLiveData.test()

        viewModel.stopCode = null

        observer.assertValues(null)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsNullWhenStopCodeIsEmpty() {
        val observer = viewModel.hasProximityAlertLiveData.test()

        viewModel.stopCode = ""

        observer.assertValues(null)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsNullFollowedByRepositoryValue() = runTest {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flowOf(true))
        val observer = viewModel.hasProximityAlertLiveData.test()

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        observer.assertValues(null, true)
    }

    @Test
    fun hasProximityAlertLiveDataCopesWithStopChange() = runTest {
        whenever(alertsRepository.hasProximityAlertFlow("123"))
                .thenReturn(intervalFlowOf(10L, 0L, false))
        whenever(alertsRepository.hasProximityAlertFlow("456"))
                .thenReturn(intervalFlowOf(10L, 0L, true))
        val observer = viewModel.hasProximityAlertLiveData.test()

        viewModel.stopCode = "123"
        advanceUntilIdle()
        viewModel.stopCode = "456"
        advanceUntilIdle()

        observer.assertValues(null, false, null, true)
    }

    @Test
    fun hasProximityAlertLiveDataOnlyEmitsDistinctValues() = runTest {
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(intervalFlowOf(0L, 10L, true, false, false, false, true))
        val observer = viewModel.hasProximityAlertLiveData.test()

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        observer.assertValues(null, true, false, true)
    }

    @Test
    fun onFavouriteMenuItemClickedWithNoStopCodePerformsNoAction() = runTest {
        val showAddObserver = viewModel.showAddFavouriteLiveData.test()
        val showRemoveObserver = viewModel.showRemoveFavouriteLiveData.test()

        viewModel.onFavouriteMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onFavouriteMenuItemClickedWithNullStopCodePerformsNoAction() = runTest {
        val showAddObserver = viewModel.showAddFavouriteLiveData.test()
        val showRemoveObserver = viewModel.showRemoveFavouriteLiveData.test()

        viewModel.stopCode = null
        viewModel.onFavouriteMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onFavouriteMenuItemClickedWithEmptyStopCodePerformsNoAction() = runTest {
        val showAddObserver = viewModel.showAddFavouriteLiveData.test()
        val showRemoveObserver = viewModel.showRemoveFavouriteLiveData.test()

        viewModel.stopCode = ""
        viewModel.onFavouriteMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onFavouriteMenuItemClickedWhenAddedAsFavouriteShowsRemoveFavourite() = runTest {
        val showAddObserver = viewModel.showAddFavouriteLiveData.test()
        val showRemoveObserver = viewModel.showRemoveFavouriteLiveData.test()
        viewModel.isFavouriteLiveData.test()
        whenever(favouritesRepository.isStopAddedAsFavouriteFlow("123456"))
                .thenReturn(flowOf(true))

        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onFavouriteMenuItemClicked()
        advanceUntilIdle()

        showRemoveObserver.assertValues("123456")
        showAddObserver.assertEmpty()
    }

    @Test
    fun onFavouriteMenuItemClickedWhenAddedNotAsFavouriteShowsAddFavourite() = runTest {
        val showAddObserver = viewModel.showAddFavouriteLiveData.test()
        val showRemoveObserver = viewModel.showRemoveFavouriteLiveData.test()
        viewModel.isFavouriteLiveData.test()
        whenever(favouritesRepository.isStopAddedAsFavouriteFlow("123456"))
                .thenReturn(flowOf(true))

        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onFavouriteMenuItemClicked()
        advanceUntilIdle()

        showRemoveObserver.assertValues("123456")
        showAddObserver.assertEmpty()
    }

    @Test
    fun onArrivalAlertMenuItemClickedWithNoStopCodePerformsNoAction() = runTest {
        val showAddObserver = viewModel.showAddArrivalAlertLiveData.test()
        val showRemoveObserver = viewModel.showRemoveArrivalAlertLiveData.test()

        viewModel.onArrivalAlertMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onArrivalAlertMenuItemClickedWithNullStopCodePerformsNoAction() = runTest {
        val showAddObserver = viewModel.showAddArrivalAlertLiveData.test()
        val showRemoveObserver = viewModel.showRemoveArrivalAlertLiveData.test()

        viewModel.stopCode = null
        viewModel.onArrivalAlertMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onArrivalAlertMenuItemClickedWithEmptyStopCodePerformsNoAction() = runTest {
        val showAddObserver = viewModel.showAddArrivalAlertLiveData.test()
        val showRemoveObserver = viewModel.showRemoveArrivalAlertLiveData.test()

        viewModel.stopCode = ""
        viewModel.onArrivalAlertMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onArrivalAlertMenuItemClickedWhenAddedAsArrivalAlertShowsRemoveArrivalAlert() = runTest {
        val showAddObserver = viewModel.showAddArrivalAlertLiveData.test()
        val showRemoveObserver = viewModel.showRemoveArrivalAlertLiveData.test()
        viewModel.hasArrivalAlertLiveData.test()
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(true))

        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onArrivalAlertMenuItemClicked()
        advanceUntilIdle()

        showRemoveObserver.assertValues("123456")
        showAddObserver.assertEmpty()
    }

    @Test
    fun onArrivalAlertMenuItemClickedWhenAddedNotAsArrivalAlertShowsAddArrivalAlert() = runTest {
        val showAddObserver = viewModel.showAddArrivalAlertLiveData.test()
        val showRemoveObserver = viewModel.showRemoveArrivalAlertLiveData.test()
        viewModel.hasArrivalAlertLiveData.test()
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flowOf(true))

        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onArrivalAlertMenuItemClicked()
        advanceUntilIdle()

        showRemoveObserver.assertValues("123456")
        showAddObserver.assertEmpty()
    }

    @Test
    fun onProximityAlertMenuItemClickedWithNoStopCodePerformsNoAction() = runTest {
        val showAddObserver = viewModel.showAddProximityAlertLiveData.test()
        val showRemoveObserver = viewModel.showRemoveProximityAlertLiveData.test()

        viewModel.onProximityAlertMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onProximityAlertMenuItemClickedWithNullStopCodePerformsNoAction() = runTest {
        val showAddObserver = viewModel.showAddProximityAlertLiveData.test()
        val showRemoveObserver = viewModel.showRemoveProximityAlertLiveData.test()

        viewModel.stopCode = null
        viewModel.onProximityAlertMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onProximityAlertMenuItemClickedWithEmptyStopCodePerformsNoAction() = runTest {
        val showAddObserver = viewModel.showAddProximityAlertLiveData.test()
        val showRemoveObserver = viewModel.showRemoveProximityAlertLiveData.test()

        viewModel.stopCode = ""
        viewModel.onProximityAlertMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onProximityAlertMenuItemClickedWhenAddedAsProximityAlertShowsRemoveProximityAlert() =
            runTest {
                val showAddObserver = viewModel.showAddProximityAlertLiveData.test()
                val showRemoveObserver = viewModel.showRemoveProximityAlertLiveData.test()
                viewModel.hasProximityAlertLiveData.test()
                whenever(alertsRepository.hasProximityAlertFlow("123456"))
                        .thenReturn(flowOf(true))

                viewModel.stopCode = "123456"
                advanceUntilIdle()
                viewModel.onProximityAlertMenuItemClicked()
                advanceUntilIdle()

                showRemoveObserver.assertValues("123456")
                showAddObserver.assertEmpty()
            }

    @Test
    fun onProximityAlertMenuItemClickedWhenAddedNotAsProximityAlertShowsAddProximityAlert() =
            runTest {
                val showAddObserver = viewModel.showAddProximityAlertLiveData.test()
                val showRemoveObserver = viewModel.showRemoveProximityAlertLiveData.test()
                viewModel.hasProximityAlertLiveData.test()
                whenever(alertsRepository.hasProximityAlertFlow("123456"))
                        .thenReturn(flowOf(true))

                viewModel.stopCode = "123456"
                advanceUntilIdle()
                viewModel.onProximityAlertMenuItemClicked()
                advanceUntilIdle()

                showRemoveObserver.assertValues("123456")
                showAddObserver.assertEmpty()
            }

    @Test
    fun onStreetViewMenuItemClickedPerformsNoActionWhenStopDetailsIsNull() = runTest {
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flowOf(null))
        val showStreetView = viewModel.showStreetViewLiveData.test()
        viewModel.stopDetailsLiveData.test()

        viewModel.stopCode = "123456"
        viewModel.onStreetViewMenuItemClicked()

        showStreetView.assertEmpty()
    }

    @Test
    fun onStreetViewMenuItemClickedShowsStreetViewWhenStopDetailsIsNotNull() = runTest {
        val details = StopDetails(
                "123456",
                StopName(
                        "Name",
                        "Locality"),
                1.2,
                3.4,
                5)
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flowOf(details))
        val showStreetView = viewModel.showStreetViewLiveData.test()
        viewModel.stopDetailsLiveData.test()

        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onStreetViewMenuItemClicked()
        advanceUntilIdle()

        showStreetView.assertValues(details)
    }
}