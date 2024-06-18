/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.FakeFavouriteStopEntity
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.FakeFavouriteStopEntityFactory
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.FakeFavouriteStopsDao
import uk.org.rivernile.android.bustracker.core.database.settings.favouritestops.FavouriteStopsDao
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [FavouritesRepository].
 *
 * @author Niall Scott
 */
class FavouritesRepositoryTest {

    @Test
    fun addOrUpdateFavouriteStopCallsDao() = runTest {
        val favouriteStopsDao = FakeFavouriteStopsDao()
        val repository = createFavouritesRepository(favouriteStopsDao)

        repository.addOrUpdateFavouriteStop(
            FavouriteStop(stopCode = "123456", stopName = "Stop name")
        )

        assertEquals(
            FakeFavouriteStopEntity(stopCode = "123456", stopName = "Stop name"),
            favouriteStopsDao.addedOrUpdatedFavouriteStops.last()
        )
    }

    @Test
    fun removeFavouriteStopCallsDao() = runTest {
        val favouriteStopsDao = FakeFavouriteStopsDao()
        val repository = createFavouritesRepository(favouriteStopsDao)

        repository.removeFavouriteStop("123456")

        assertEquals("123456", favouriteStopsDao.removedFavouriteStops.last())
    }

    @Test
    fun isStopAddedAsFavouriteEmitsDistinctValues() = runTest {
        val repository = createFavouritesRepository(
            favouriteStopsDao = FakeFavouriteStopsDao(
                onIsStopAddedAsFavouriteFlow = {
                    assertEquals("123456", it)
                    intervalFlowOf(0L, 10L, false, false, true, true, false)
                }
            )
        )

        repository.isStopAddedAsFavouriteFlow("123456").test {
            assertFalse(awaitItem())
            assertTrue(awaitItem())
            assertFalse(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getFavouriteStopsFlowEmitsDistinctValues() = runTest {
        val stop1 = FakeFavouriteStopEntity(stopCode = "123456", stopName = "Name 1")
        val stop2 = FakeFavouriteStopEntity(stopCode = "123456", stopName = "Name 2")
        val repository = createFavouritesRepository(
            favouriteStopsDao = FakeFavouriteStopsDao(
                onGetFavouriteStopFlow = {
                    assertEquals("123456", it)
                    intervalFlowOf(0L, 10L, null, null, stop1, stop1, stop2, stop2, stop1)
                }
            )
        )
        val expected1 = FavouriteStop(stopCode = "123456", stopName = "Name 1")
        val expected2 = FavouriteStop(stopCode = "123456", stopName = "Name 2")

        repository.getFavouriteStopFlow("123456").test {
            assertNull(awaitItem())
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            assertEquals(expected1, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowEmitsDistinctValues() = runTest {
        val stop1 = FakeFavouriteStopEntity(stopCode = "1", stopName = "Name 1")
        val stop2 = FakeFavouriteStopEntity(stopCode = "2", stopName = "Name 2")
        val stop3 = FakeFavouriteStopEntity(stopCode = "3", stopName = "Name 3")
        val repository = createFavouritesRepository(
            favouriteStopsDao = FakeFavouriteStopsDao(
                onAllFavouriteStopsFlow = {
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        null,
                        listOf(stop1),
                        listOf(stop1),
                        listOf(stop1, stop2, stop3)
                    )
                }
            )
        )
        val expected1 = FavouriteStop(stopCode = "1", stopName = "Name 1")
        val expected2 = FavouriteStop(stopCode = "2", stopName = "Name 2")
        val expected3 = FavouriteStop(stopCode = "3", stopName = "Name 3")

        repository.allFavouriteStopsFlow.test {
            assertNull(awaitItem())
            assertEquals(listOf(expected1), awaitItem())
            assertEquals(listOf(expected1, expected2, expected3), awaitItem())
            awaitComplete()
        }
    }

    private fun createFavouritesRepository(
        favouriteStopsDao: FavouriteStopsDao
    ): FavouritesRepository {
        return FavouritesRepository(
            favouriteStopsDao = favouriteStopsDao,
            entityFactory = FakeFavouriteStopEntityFactory()
        )
    }
}