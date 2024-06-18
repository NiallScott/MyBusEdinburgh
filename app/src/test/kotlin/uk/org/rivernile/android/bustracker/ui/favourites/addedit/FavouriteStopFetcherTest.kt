/*
 * Copyright (C) 2021 - 2024 Niall 'Rivernile' Scott
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

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.FakeStopName
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [FavouriteStopFetcher].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class FavouriteStopFetcherTest {

    @Mock
    private lateinit var favouritesRepository: FavouritesRepository
    @Mock
    private lateinit var busStopsRepository: BusStopsRepository

    private lateinit var fetcher: FavouriteStopFetcher

    @BeforeTest
    fun setUp() {
        fetcher = FavouriteStopFetcher(favouritesRepository, busStopsRepository)
    }

    @Test
    fun loadFavouriteStopAndDetailsWithNullStopCodeEmitsInProgress() = runTest {
        fetcher.loadFavouriteStopAndDetails(null).test {
            assertEquals(UiState.InProgress, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun loadFavouriteStopAndDetailsWithEmptyStopCodeEmitsInProgress() = runTest {
        fetcher.loadFavouriteStopAndDetails("").test {
            assertEquals(UiState.InProgress, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun loadFavouriteStopAndDetailsWithNoFavouriteOrDetailsEmitsAddMode() = runTest {
        whenever(favouritesRepository.getFavouriteStopFlow("123456"))
            .thenReturn(flowOf(null))
        whenever(busStopsRepository.getNameForStopFlow("123456"))
            .thenReturn(intervalFlowOf(10L, 10L, null))

        fetcher.loadFavouriteStopAndDetails("123456").test {
            assertEquals(UiState.InProgress, awaitItem())
            assertEquals(UiState.Mode.Add("123456", null), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun loadFavouriteStopAndDetailsWithNoFavouriteButStopNameEmitsAddMode() = runTest {
        val stopName = FakeStopName("Name", "Locality")
        whenever(favouritesRepository.getFavouriteStopFlow("123456"))
            .thenReturn(flowOf(null))
        whenever(busStopsRepository.getNameForStopFlow("123456"))
            .thenReturn(intervalFlowOf(10L, 10L, stopName))

        fetcher.loadFavouriteStopAndDetails("123456").test {
            assertEquals(UiState.InProgress, awaitItem())
            assertEquals(UiState.Mode.Add("123456", stopName), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun loadFavouriteStopAndDetailsWithFavouriteButNoDetailsEmitsEditMode() = runTest {
        val favouriteStop = FavouriteStop("123456", "Stored name")
        whenever(favouritesRepository.getFavouriteStopFlow("123456"))
            .thenReturn(flowOf(favouriteStop))
        whenever(busStopsRepository.getNameForStopFlow("123456"))
            .thenReturn(intervalFlowOf(10L, 10L, null))

        fetcher.loadFavouriteStopAndDetails("123456").test {
            assertEquals(UiState.InProgress, awaitItem())
            assertEquals(UiState.Mode.Edit("123456", null, favouriteStop), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun loadFavouriteStopAndDetailsWithFavouriteAndStopNameEmitsEditMode() = runTest {
        val favouriteStop = FavouriteStop("123456", "Stored name")
        val stopName = FakeStopName("Name", "Locality")
        whenever(favouritesRepository.getFavouriteStopFlow("123456"))
            .thenReturn(flowOf(favouriteStop))
        whenever(busStopsRepository.getNameForStopFlow("123456"))
            .thenReturn(intervalFlowOf(10L, 10L, stopName))

        fetcher.loadFavouriteStopAndDetails("123456").test {
            assertEquals(UiState.InProgress, awaitItem())
            assertEquals(UiState.Mode.Edit("123456", stopName, favouriteStop), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun loadFavouriteStopAndDetailsPropagatesUpdatesToFavouriteStop() = runTest {
        val favouriteStop1 = FavouriteStop("123456", "Stored name 1")
        val favouriteStop2 = FavouriteStop("123456", "Stored name 2")
        val stopName = FakeStopName("Name", "Locality")
        whenever(favouritesRepository.getFavouriteStopFlow("123456"))
            .thenReturn(
                intervalFlowOf(
                    10L,
                    100L,
                    favouriteStop1,
                    favouriteStop2,
                    null
                )
            )
        whenever(busStopsRepository.getNameForStopFlow("123456"))
            .thenReturn(flowOf(stopName))

        fetcher.loadFavouriteStopAndDetails("123456").test {
            assertEquals(UiState.InProgress, awaitItem())
            assertEquals(UiState.Mode.Edit("123456", stopName, favouriteStop1), awaitItem())
            assertEquals(UiState.Mode.Edit("123456", stopName, favouriteStop2), awaitItem())
            assertEquals(UiState.Mode.Add("123456", stopName), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun loadFavouriteStopAndDetailsPropagatesUpdatesToStopName() = runTest {
        val favouriteStop = FavouriteStop("123456", "Stored name")
        val stopName1 = FakeStopName("Name 1", "Locality")
        val stopName2 = FakeStopName("Name 2", "Locality")
        val stopName3 = FakeStopName("Name 3", "Locality")
        whenever(favouritesRepository.getFavouriteStopFlow("123456"))
            .thenReturn(flowOf(favouriteStop))
        whenever(busStopsRepository.getNameForStopFlow("123456"))
            .thenReturn(
                intervalFlowOf(
                    10L,
                    100L,
                    stopName1,
                    stopName2,
                    stopName3
                )
            )

        fetcher.loadFavouriteStopAndDetails("123456").test {
            assertEquals(UiState.InProgress, awaitItem())
            assertEquals(UiState.Mode.Edit("123456", stopName1, favouriteStop), awaitItem())
            assertEquals(UiState.Mode.Edit("123456", stopName2, favouriteStop), awaitItem())
            assertEquals(UiState.Mode.Edit("123456", stopName3, favouriteStop), awaitItem())
            awaitComplete()
        }
    }
}