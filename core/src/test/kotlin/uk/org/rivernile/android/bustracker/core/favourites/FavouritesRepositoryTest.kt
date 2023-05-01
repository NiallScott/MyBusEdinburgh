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

package uk.org.rivernile.android.bustracker.core.favourites

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.FavouriteStopEntity
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.FavouriteStopEntityFactory
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.FavouriteStopsDao
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [FavouritesRepository].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class FavouritesRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var favouritesStopsDao: FavouriteStopsDao
    @Mock
    private lateinit var entityFactory: FavouriteStopEntityFactory

    private lateinit var repository: FavouritesRepository

    @Before
    fun setUp() {
        repository = FavouritesRepository(
            favouritesStopsDao,
            entityFactory)
    }

    @Test
    fun addOrUpdateFavouriteStopCallsDao() = runTest {
        val favouriteStop = mock<FavouriteStopEntity>()
        whenever(entityFactory.createFavouriteStopEntity("123456", "Stop name"))
            .thenReturn(favouriteStop)

        repository.addOrUpdateFavouriteStop(FavouriteStop("123456", "Stop name"))

        verify(favouritesStopsDao)
            .addOrUpdateFavouriteStop(favouriteStop)
    }

    @Test
    fun removeFavouriteStopCallsDao() = runTest {
        repository.removeFavouriteStop("123456")

        verify(favouritesStopsDao)
            .removeFavouriteStop("123456")
    }

    @Test
    fun isStopAddedAsFavouriteEmitsDistinctValues() = runTest {
        whenever(favouritesStopsDao.isStopAddedAsFavouriteFlow("123456"))
            .thenReturn(intervalFlowOf(0L, 10L, false, false, true, true, false))

        val observer = repository.isStopAddedAsFavouriteFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(false, true, false)
    }

    @Test
    fun getFavouriteStopsFlowEmitsDistinctValues() = runTest {
        val stop1 = MockFavouriteStopEntity("123456", "Name 1")
        val stop2 = MockFavouriteStopEntity("123456", "Name 2")
        val expected1 = FavouriteStop("123456", "Name 1")
        val expected2 = FavouriteStop("123456", "Name 2")
        whenever(favouritesStopsDao.getFavouriteStopFlow("123456"))
            .thenReturn(intervalFlowOf(0L, 10L, null, null, stop1, stop1, stop2, stop2, stop1))

        val observer = repository.getFavouriteStopFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null, expected1, expected2, expected1)
    }

    @Test
    fun allFavouriteStopsFlowEmitsDistinctValues() = runTest {
        val stop1 = MockFavouriteStopEntity("1", "Name 1")
        val stop2 = MockFavouriteStopEntity("2", "Name 2")
        val stop3 = MockFavouriteStopEntity("3", "Name 3")
        val expected1 = FavouriteStop("1", "Name 1")
        val expected2 = FavouriteStop("2", "Name 2")
        val expected3 = FavouriteStop("3", "Name 3")
        val flow = intervalFlowOf(
            0L,
            10L,
            null,
            null,
            listOf(stop1),
            listOf(stop1),
            listOf(stop1, stop2, stop3))
        whenever(favouritesStopsDao.allFavouriteStopsFlow)
            .thenReturn(flow)

        val observer = repository.allFavouriteStopsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
            null,
            listOf(expected1),
            listOf(expected1, expected2, expected3))
    }

    private data class MockFavouriteStopEntity(
        override val stopCode: String,
        override val stopName: String) : FavouriteStopEntity
}