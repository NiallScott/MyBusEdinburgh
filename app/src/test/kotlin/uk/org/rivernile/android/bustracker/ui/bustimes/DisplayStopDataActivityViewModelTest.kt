/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.LiveDataTestObserver

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

    private val stringObserver = LiveDataTestObserver<String?>()
    private val stopDetailsObserver = LiveDataTestObserver<StopDetails?>()
    private val booleanObserver = LiveDataTestObserver<Boolean?>()

    private lateinit var viewModel: DisplayStopDataActivityViewModel

    @Before
    fun setUp() {
        viewModel = DisplayStopDataActivityViewModel(busStopsRepository, favouritesRepository,
                alertsRepository)
    }

    @Test
    fun distinctStopCodeLiveDataIsNullByDefault() {
        viewModel.distinctStopCodeLiveData.observeForever(stringObserver)

        stringObserver.assertValues()
        assertNull(viewModel.distinctStopCodeLiveData.value)
    }

    @Test
    fun distinctStopCodeLiveDataOnlyEmitsDistinctCodeOnce() {
        viewModel.distinctStopCodeLiveData.observeForever(stringObserver)

        viewModel.stopCode = "123"
        viewModel.stopCode = "123"
        viewModel.stopCode = "123"

        stringObserver.assertValues("123")
    }

    @Test
    fun distinctStopCodeLiveDataEmitsGivenSequenceCorrectly() {
        viewModel.distinctStopCodeLiveData.observeForever(stringObserver)

        viewModel.stopCode = "1"
        viewModel.stopCode = "2"
        viewModel.stopCode = "2"
        viewModel.stopCode = "3"

        stringObserver.assertValues("1", "2", "3")
    }

    @Test
    fun busStopDetailsWithNullStopCodeEmitsNullDetails() {
        viewModel.busStopDetails.observeForever(stopDetailsObserver)

        viewModel.stopCode = null

        stopDetailsObserver.assertValues(null)
        assertNull(viewModel.busStopDetails.value)
    }

    @Test
    fun busStopDetailsWithEmptyStopCodeEmitsNullDetails() {
        viewModel.busStopDetails.observeForever(stopDetailsObserver)

        viewModel.stopCode = ""

        stopDetailsObserver.assertValues(null)
        assertNull(viewModel.busStopDetails.value)
    }

    @Test
    fun busStopDetailsWithStopCodeWhichIsNotFoundReturnsNullDetails() = runTest {
        val flow = flow<StopDetails?> { emit(null) }
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flow)
        viewModel.busStopDetails.observeForever(stopDetailsObserver)

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        stopDetailsObserver.assertValues(null)
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
        val flow = flow<StopDetails?> { emit(details) }
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flow)
        viewModel.busStopDetails.observeForever(stopDetailsObserver)

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        stopDetailsObserver.assertValues(details)
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
        val flow = flow {
            emit(null)
            emit(details)
            emit(null)
        }
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flow)
        viewModel.busStopDetails.observeForever(stopDetailsObserver)

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        stopDetailsObserver.assertValues(null, details, null)
    }

    @Test
    fun isFavouriteLiveDataEmitsNullWhenStopCodeIsNull() {
        viewModel.isFavouriteLiveData.observeForever(booleanObserver)

        viewModel.stopCode = null

        booleanObserver.assertValues(null)
    }

    @Test
    fun isFavouriteLiveDataEmitsNullWhenStopCodeIsEmpty() {
        viewModel.isFavouriteLiveData.observeForever(booleanObserver)

        viewModel.stopCode = ""

        booleanObserver.assertValues(null)
    }

    @Test
    fun isFavouriteLiveDataEmitsNullFollowedByRepositoryValue() = runTest {
        val flow = flow { emit(true) }
        whenever(favouritesRepository.isStopAddedAsFavouriteFlow("123456"))
                .thenReturn(flow)
        viewModel.isFavouriteLiveData.observeForever(booleanObserver)

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        booleanObserver.assertValues(null, true)
    }

    @Test
    fun isFavouriteLiveDataCopesWithStopChange() = runTest {
        val flow1 = flow { emit(false) }
        val flow2 = flow { emit(true) }
        whenever(favouritesRepository.isStopAddedAsFavouriteFlow("123"))
                .thenReturn(flow1)
        whenever(favouritesRepository.isStopAddedAsFavouriteFlow("456"))
                .thenReturn(flow2)
        viewModel.isFavouriteLiveData.observeForever(booleanObserver)

        viewModel.stopCode = "123"
        advanceUntilIdle()
        viewModel.stopCode = "456"
        advanceUntilIdle()

        booleanObserver.assertValues(null, false, null, true)
    }

    @Test
    fun isFavouriteLiveDataOnlyEmitsDistinctValues() = runTest {
        val flow = flow {
            emit(true)
            emit(false)
            emit(false)
            emit(false)
            emit(true)
        }
        whenever(favouritesRepository.isStopAddedAsFavouriteFlow("123456"))
                .thenReturn(flow)
        viewModel.isFavouriteLiveData.observeForever(booleanObserver)

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        booleanObserver.assertValues(null, true, false, true)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsNullWhenStopCodeIsNull() {
        viewModel.hasArrivalAlertLiveData.observeForever(booleanObserver)

        viewModel.stopCode = null

        booleanObserver.assertValues(null)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsNullWhenStopCodeIsEmpty() {
        viewModel.hasArrivalAlertLiveData.observeForever(booleanObserver)

        viewModel.stopCode = ""

        booleanObserver.assertValues(null)
    }

    @Test
    fun hasArrivalAlertLiveDataEmitsNullFollowedByRepositoryValue() = runTest {
        val flow = flow { emit(true) }
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flow)
        viewModel.hasArrivalAlertLiveData.observeForever(booleanObserver)

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        booleanObserver.assertValues(null, true)
    }

    @Test
    fun hasArrivalAlertLiveDataCopesWithStopChange() = runTest {
        val flow1 = flow { emit(false) }
        val flow2 = flow { emit(true) }
        whenever(alertsRepository.hasArrivalAlertFlow("123"))
                .thenReturn(flow1)
        whenever(alertsRepository.hasArrivalAlertFlow("456"))
                .thenReturn(flow2)
        viewModel.hasArrivalAlertLiveData.observeForever(booleanObserver)

        viewModel.stopCode = "123"
        advanceUntilIdle()
        viewModel.stopCode = "456"
        advanceUntilIdle()

        booleanObserver.assertValues(null, false, null, true)
    }

    @Test
    fun hasArrivalAlertLiveDataOnlyEmitsDistinctValues() = runTest {
        val flow = flow {
            emit(true)
            emit(false)
            emit(false)
            emit(false)
            emit(true)
        }
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flow)
        viewModel.hasArrivalAlertLiveData.observeForever(booleanObserver)

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        booleanObserver.assertValues(null, true, false, true)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsNullWhenStopCodeIsNull() {
        viewModel.hasProximityAlertLiveData.observeForever(booleanObserver)

        viewModel.stopCode = null

        booleanObserver.assertValues(null)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsNullWhenStopCodeIsEmpty() {
        viewModel.hasProximityAlertLiveData.observeForever(booleanObserver)

        viewModel.stopCode = ""

        booleanObserver.assertValues(null)
    }

    @Test
    fun hasProximityAlertLiveDataEmitsNullFollowedByRepositoryValue() = runTest {
        val flow = flow { emit(true) }
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flow)
        viewModel.hasProximityAlertLiveData.observeForever(booleanObserver)

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        booleanObserver.assertValues(null, true)
    }

    @Test
    fun hasProximityAlertLiveDataCopesWithStopChange() = runTest {
        val flow1 = flow { emit(false) }
        val flow2 = flow { emit(true) }
        whenever(alertsRepository.hasProximityAlertFlow("123"))
                .thenReturn(flow1)
        whenever(alertsRepository.hasProximityAlertFlow("456"))
                .thenReturn(flow2)
        viewModel.hasProximityAlertLiveData.observeForever(booleanObserver)

        viewModel.stopCode = "123"
        advanceUntilIdle()
        viewModel.stopCode = "456"
        advanceUntilIdle()

        booleanObserver.assertValues(null, false, null, true)
    }

    @Test
    fun hasProximityAlertLiveDataOnlyEmitsDistinctValues() = runTest {
        val flow = flow {
            emit(true)
            emit(false)
            emit(false)
            emit(false)
            emit(true)
        }
        whenever(alertsRepository.hasProximityAlertFlow("123456"))
                .thenReturn(flow)
        viewModel.hasProximityAlertLiveData.observeForever(booleanObserver)

        viewModel.stopCode = "123456"
        advanceUntilIdle()

        booleanObserver.assertValues(null, true, false, true)
    }

    @Test
    fun onFavouriteMenuItemClickedWithNoStopCodePerformsNoAction() = runTest {
        val showAddObserver = LiveDataTestObserver<String>()
        val showRemoveObserver = LiveDataTestObserver<String>()
        viewModel.showAddFavouriteLiveData.observeForever(showAddObserver)
        viewModel.showRemoveFavouriteLiveData.observeForever(showRemoveObserver)

        viewModel.onFavouriteMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onFavouriteMenuItemClickedWithNullStopCodePerformsNoAction() = runTest {
        val showAddObserver = LiveDataTestObserver<String>()
        val showRemoveObserver = LiveDataTestObserver<String>()
        viewModel.showAddFavouriteLiveData.observeForever(showAddObserver)
        viewModel.showRemoveFavouriteLiveData.observeForever(showRemoveObserver)

        viewModel.stopCode = null
        viewModel.onFavouriteMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onFavouriteMenuItemClickedWithEmptyStopCodePerformsNoAction() = runTest {
        val showAddObserver = LiveDataTestObserver<String>()
        val showRemoveObserver = LiveDataTestObserver<String>()
        viewModel.showAddFavouriteLiveData.observeForever(showAddObserver)
        viewModel.showRemoveFavouriteLiveData.observeForever(showRemoveObserver)

        viewModel.stopCode = ""
        viewModel.onFavouriteMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onFavouriteMenuItemClickedWhenAddedAsFavouriteShowsRemoveFavourite() = runTest {
        val showAddObserver = LiveDataTestObserver<String>()
        val showRemoveObserver = LiveDataTestObserver<String>()
        viewModel.showAddFavouriteLiveData.observeForever(showAddObserver)
        viewModel.showRemoveFavouriteLiveData.observeForever(showRemoveObserver)
        viewModel.isFavouriteLiveData.observeForever(booleanObserver)
        val flow = flow { emit(true) }
        whenever(favouritesRepository.isStopAddedAsFavouriteFlow("123456"))
                .thenReturn(flow)

        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onFavouriteMenuItemClicked()
        advanceUntilIdle()

        showRemoveObserver.assertValues("123456")
        showAddObserver.assertEmpty()
    }

    @Test
    fun onFavouriteMenuItemClickedWhenAddedNotAsFavouriteShowsAddFavourite() = runTest {
        val showAddObserver = LiveDataTestObserver<String>()
        val showRemoveObserver = LiveDataTestObserver<String>()
        viewModel.showAddFavouriteLiveData.observeForever(showAddObserver)
        viewModel.showRemoveFavouriteLiveData.observeForever(showRemoveObserver)
        viewModel.isFavouriteLiveData.observeForever(booleanObserver)
        val flow = flow { emit(true) }
        whenever(favouritesRepository.isStopAddedAsFavouriteFlow("123456"))
                .thenReturn(flow)

        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onFavouriteMenuItemClicked()
        advanceUntilIdle()

        showRemoveObserver.assertValues("123456")
        showAddObserver.assertEmpty()
    }

    @Test
    fun onArrivalAlertMenuItemClickedWithNoStopCodePerformsNoAction() = runTest {
        val showAddObserver = LiveDataTestObserver<String>()
        val showRemoveObserver = LiveDataTestObserver<String>()
        viewModel.showAddArrivalAlertLiveData.observeForever(showAddObserver)
        viewModel.showRemoveArrivalAlertLiveData.observeForever(showRemoveObserver)

        viewModel.onArrivalAlertMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onArrivalAlertMenuItemClickedWithNullStopCodePerformsNoAction() = runTest {
        val showAddObserver = LiveDataTestObserver<String>()
        val showRemoveObserver = LiveDataTestObserver<String>()
        viewModel.showAddArrivalAlertLiveData.observeForever(showAddObserver)
        viewModel.showRemoveArrivalAlertLiveData.observeForever(showRemoveObserver)

        viewModel.stopCode = null
        viewModel.onArrivalAlertMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onArrivalAlertMenuItemClickedWithEmptyStopCodePerformsNoAction() = runTest {
        val showAddObserver = LiveDataTestObserver<String>()
        val showRemoveObserver = LiveDataTestObserver<String>()
        viewModel.showAddArrivalAlertLiveData.observeForever(showAddObserver)
        viewModel.showRemoveArrivalAlertLiveData.observeForever(showRemoveObserver)

        viewModel.stopCode = ""
        viewModel.onArrivalAlertMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onArrivalAlertMenuItemClickedWhenAddedAsArrivalAlertShowsRemoveArrivalAlert() = runTest {
        val showAddObserver = LiveDataTestObserver<String>()
        val showRemoveObserver = LiveDataTestObserver<String>()
        viewModel.showAddArrivalAlertLiveData.observeForever(showAddObserver)
        viewModel.showRemoveArrivalAlertLiveData.observeForever(showRemoveObserver)
        viewModel.hasArrivalAlertLiveData.observeForever(booleanObserver)
        val flow = flow { emit(true) }
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flow)

        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onArrivalAlertMenuItemClicked()
        advanceUntilIdle()

        showRemoveObserver.assertValues("123456")
        showAddObserver.assertEmpty()
    }

    @Test
    fun onArrivalAlertMenuItemClickedWhenAddedNotAsArrivalAlertShowsAddArrivalAlert() = runTest {
        val showAddObserver = LiveDataTestObserver<String>()
        val showRemoveObserver = LiveDataTestObserver<String>()
        viewModel.showAddArrivalAlertLiveData.observeForever(showAddObserver)
        viewModel.showRemoveArrivalAlertLiveData.observeForever(showRemoveObserver)
        viewModel.hasArrivalAlertLiveData.observeForever(booleanObserver)
        val flow = flow { emit(true) }
        whenever(alertsRepository.hasArrivalAlertFlow("123456"))
                .thenReturn(flow)

        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onArrivalAlertMenuItemClicked()
        advanceUntilIdle()

        showRemoveObserver.assertValues("123456")
        showAddObserver.assertEmpty()
    }

    @Test
    fun onProximityAlertMenuItemClickedWithNoStopCodePerformsNoAction() = runTest {
        val showAddObserver = LiveDataTestObserver<String>()
        val showRemoveObserver = LiveDataTestObserver<String>()
        viewModel.showAddProximityAlertLiveData.observeForever(showAddObserver)
        viewModel.showRemoveProximityAlertLiveData.observeForever(showRemoveObserver)

        viewModel.onProximityAlertMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onProximityAlertMenuItemClickedWithNullStopCodePerformsNoAction() = runTest {
        val showAddObserver = LiveDataTestObserver<String>()
        val showRemoveObserver = LiveDataTestObserver<String>()
        viewModel.showAddProximityAlertLiveData.observeForever(showAddObserver)
        viewModel.showRemoveProximityAlertLiveData.observeForever(showRemoveObserver)

        viewModel.stopCode = null
        viewModel.onProximityAlertMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onProximityAlertMenuItemClickedWithEmptyStopCodePerformsNoAction() = runTest {
        val showAddObserver = LiveDataTestObserver<String>()
        val showRemoveObserver = LiveDataTestObserver<String>()
        viewModel.showAddProximityAlertLiveData.observeForever(showAddObserver)
        viewModel.showRemoveProximityAlertLiveData.observeForever(showRemoveObserver)

        viewModel.stopCode = ""
        viewModel.onProximityAlertMenuItemClicked()

        showAddObserver.assertEmpty()
        showRemoveObserver.assertEmpty()
    }

    @Test
    fun onProximityAlertMenuItemClickedWhenAddedAsProximityAlertShowsRemoveProximityAlert() =
            runTest {
                val showAddObserver = LiveDataTestObserver<String>()
                val showRemoveObserver = LiveDataTestObserver<String>()
                viewModel.showAddProximityAlertLiveData.observeForever(showAddObserver)
                viewModel.showRemoveProximityAlertLiveData.observeForever(showRemoveObserver)
                viewModel.hasProximityAlertLiveData.observeForever(booleanObserver)
                val flow = flow { emit(true) }
                whenever(alertsRepository.hasProximityAlertFlow("123456"))
                        .thenReturn(flow)

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
                val showAddObserver = LiveDataTestObserver<String>()
                val showRemoveObserver = LiveDataTestObserver<String>()
                viewModel.showAddProximityAlertLiveData.observeForever(showAddObserver)
                viewModel.showRemoveProximityAlertLiveData.observeForever(showRemoveObserver)
                viewModel.hasProximityAlertLiveData.observeForever(booleanObserver)
                val flow = flow { emit(true) }
                whenever(alertsRepository.hasProximityAlertFlow("123456"))
                        .thenReturn(flow)

                viewModel.stopCode = "123456"
                advanceUntilIdle()
                viewModel.onProximityAlertMenuItemClicked()
                advanceUntilIdle()

                showRemoveObserver.assertValues("123456")
                showAddObserver.assertEmpty()
            }

    @Test
    fun onStreetViewMenuItemClickedPerformsNoActionWhenStopDetailsIsNull() = runTest {
        val flow = flow<StopDetails?> { emit(null) }
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flow)
        val showStreetView = LiveDataTestObserver<StopDetails>()
        viewModel.showStreetViewLiveData.observeForever(showStreetView)
        viewModel.busStopDetails.observeForever(stopDetailsObserver)

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
        val flow = flow { emit(details) }
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flow)
        val showStreetView = LiveDataTestObserver<StopDetails>()
        viewModel.showStreetViewLiveData.observeForever(showStreetView)
        viewModel.busStopDetails.observeForever(stopDetailsObserver)

        viewModel.stopCode = "123456"
        advanceUntilIdle()
        viewModel.onStreetViewMenuItemClicked()
        advanceUntilIdle()

        showStreetView.assertValues(details)
    }
}