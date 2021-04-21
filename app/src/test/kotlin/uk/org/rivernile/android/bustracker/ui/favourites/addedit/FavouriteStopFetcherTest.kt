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

import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.database.settings.entities.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [FavouriteStopFetcher].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class FavouriteStopFetcherTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var favouritesRepository: FavouritesRepository
    @Mock
    private lateinit var busStopsRepository: BusStopsRepository

    private lateinit var fetcher: FavouriteStopFetcher

    @Before
    fun setUp() {
        fetcher = FavouriteStopFetcher(favouritesRepository, busStopsRepository)
    }

    @Test
    fun loadFavouriteStopAndDetailsWithNullStopCodeEmitsInProgress() = runBlockingTest {
        val observer = fetcher.loadFavouriteStopAndDetails(null).test(this)
        observer.finish()

        observer.assertValues(UiState.InProgress)
    }

    @Test
    fun loadFavouriteStopAndDetailsWithEmptyStopCodeEmitsInProgress() = runBlockingTest {
        val observer = fetcher.loadFavouriteStopAndDetails("").test(this)
        observer.finish()

        observer.assertValues(UiState.InProgress)
    }

    @Test
    fun loadFavouriteStopAndDetailsWithNoFavouriteOrDetailsEmitsAddMode() = runBlockingTest {
        whenever(favouritesRepository.getFavouriteStopFlow("123456"))
                .thenReturn(flowOf(null))
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(null))

        val observer = fetcher.loadFavouriteStopAndDetails("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiState.InProgress, UiState.Mode.Add("123456", null))
    }

    @Test
    fun loadFavouriteStopAndDetailsWithNoFavouriteButStopNameEmitsAddMode() = runBlockingTest {
        val stopName = StopName("Name", "Locality")
        whenever(favouritesRepository.getFavouriteStopFlow("123456"))
                .thenReturn(flowOf(null))
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(stopName))

        val observer = fetcher.loadFavouriteStopAndDetails("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiState.InProgress,
                UiState.Mode.Add("123456", stopName))
    }

    @Test
    fun loadFavouriteStopAndDetailsWithFavouriteButNoDetailsEmitsEditMode() = runBlockingTest {
        val favouriteStop = FavouriteStop(1, "123456", "Stored name")
        whenever(favouritesRepository.getFavouriteStopFlow("123456"))
                .thenReturn(flowOf(favouriteStop))
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(null))

        val observer = fetcher.loadFavouriteStopAndDetails("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiState.InProgress,
                UiState.Mode.Edit("123456", null, favouriteStop))
    }

    @Test
    fun loadFavouriteStopAndDetailsWithFavouriteAndStopNameEmitsEditMode() = runBlockingTest {
        val favouriteStop = FavouriteStop(1, "123456", "Stored name")
        val stopName = StopName("Name", "Locality")
        whenever(favouritesRepository.getFavouriteStopFlow("123456"))
                .thenReturn(flowOf(favouriteStop))
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(stopName))

        val observer = fetcher.loadFavouriteStopAndDetails("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiState.InProgress,
                UiState.Mode.Edit("123456", stopName, favouriteStop))
    }

    @Test
    fun loadFavouriteStopAndDetailsPropagatesUpdatesToFavouriteStop() = runBlockingTest {
        val favouriteStop1 = FavouriteStop(1, "123456", "Stored name 1")
        val favouriteStop2 = FavouriteStop(1, "123456", "Stored name 2")
        val stopName = StopName("Name", "Locality")
        whenever(favouritesRepository.getFavouriteStopFlow("123456"))
                .thenReturn(flow {
                    emit(favouriteStop1)
                    delay(100L)
                    emit(favouriteStop2)
                    delay(100L)
                    emit(null)
                })
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(stopName))

        val observer = fetcher.loadFavouriteStopAndDetails("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiState.InProgress,
                UiState.Mode.Edit("123456", stopName, favouriteStop1),
                UiState.Mode.Edit("123456", stopName, favouriteStop2),
                UiState.Mode.Add("123456", stopName))
    }

    @Test
    fun loadFavouriteStopAndDetailsPropagatesUpdatesToStopName() = runBlockingTest {
        val favouriteStop = FavouriteStop(1, "123456", "Stored name")
        val stopName1 = StopName("Name 1", "Locality")
        val stopName2 = StopName("Name 2", "Locality")
        val stopName3 = StopName("Name 3", "Locality")
        whenever(favouritesRepository.getFavouriteStopFlow("123456"))
                .thenReturn(flowOf(favouriteStop))
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flow {
                    emit(stopName1)
                    delay(100L)
                    emit(stopName2)
                    delay(100L)
                    emit(stopName3)
                })

        val observer = fetcher.loadFavouriteStopAndDetails("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiState.InProgress,
                UiState.Mode.Edit("123456", stopName1, favouriteStop),
                UiState.Mode.Edit("123456", stopName2, favouriteStop),
                UiState.Mode.Edit("123456", stopName3, favouriteStop))
    }

    private val runBlockingTest = coroutineRule::runBlockingTest
}