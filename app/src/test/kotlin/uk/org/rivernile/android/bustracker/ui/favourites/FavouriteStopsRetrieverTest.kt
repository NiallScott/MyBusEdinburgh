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

package uk.org.rivernile.android.bustracker.ui.favourites

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [FavouriteStopsRetriever].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class FavouriteStopsRetrieverTest {

    @Mock
    private lateinit var favouritesRepository: FavouritesRepository
    @Mock
    private lateinit var serviceStopsRepository: ServiceStopsRepository

    private lateinit var retriever: FavouriteStopsRetriever

    @BeforeTest
    fun setUp() {
        retriever = FavouriteStopsRetriever(favouritesRepository, serviceStopsRepository)
    }

    @Test
    fun allFavouriteStopsFlowWithNullFavouriteStopsEmitsEmptyList() = runTest {
        whenever(favouritesRepository.allFavouriteStopsFlow)
            .thenReturn(flowOf(null))

        retriever.allFavouriteStopsFlow.test {
            assertNull(awaitItem())
            assertEquals(emptyList(), awaitItem())
            awaitComplete()
        }
        verify(serviceStopsRepository, never())
                .getServicesForStopsFlow(any())
    }

    @Test
    fun allFavouriteStopsFlowWithEmptyFavouriteStopsEmitsEmptyList() = runTest {
        whenever(favouritesRepository.allFavouriteStopsFlow)
            .thenReturn(flowOf(emptyList()))

        retriever.allFavouriteStopsFlow.test {
            assertNull(awaitItem())
            assertEquals(emptyList(), awaitItem())
            awaitComplete()
        }
        verify(serviceStopsRepository, never())
            .getServicesForStopsFlow(any())
    }

    @Test
    fun allFavouriteStopsFlowWithFavouriteStopsAndNullServicesEmitsExpectedList() = runTest {
        val favouriteStops = listOf(
            FavouriteStop("111111", "Favourite 1"),
            FavouriteStop("222222", "Favourite 2"),
            FavouriteStop("333333", "Favourite 3")
        )
        val expected = listOf(
            UiFavouriteStop(FavouriteStop("111111", "Favourite 1"), null, false),
            UiFavouriteStop(FavouriteStop("222222", "Favourite 2"), null, false),
            UiFavouriteStop(FavouriteStop("333333", "Favourite 3"), null, false)
        )
        whenever(favouritesRepository.allFavouriteStopsFlow)
            .thenReturn(flowOf(favouriteStops))
        whenever(serviceStopsRepository.getServicesForStopsFlow(any()))
            .thenReturn(flowOf(null))

        retriever.allFavouriteStopsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithFavouriteStopsAndEmptyServicesEmitsExpectedList() = runTest {
        val favouriteStops = listOf(
            FavouriteStop("111111", "Favourite 1"),
            FavouriteStop("222222", "Favourite 2"),
            FavouriteStop("333333", "Favourite 3")
        )
        val expected = listOf(
            UiFavouriteStop(FavouriteStop("111111", "Favourite 1"), null, false),
            UiFavouriteStop(FavouriteStop("222222", "Favourite 2"), null, false),
            UiFavouriteStop(FavouriteStop("333333", "Favourite 3"), null, false)
        )
        whenever(favouritesRepository.allFavouriteStopsFlow)
            .thenReturn(flowOf(favouriteStops))
        whenever(serviceStopsRepository.getServicesForStopsFlow(any()))
            .thenReturn(flowOf(emptyMap()))

        retriever.allFavouriteStopsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithFavouritesAndPopulatedServicesEmitsExpectedList() = runTest {
        val favouriteStops = listOf(
            FavouriteStop("111111", "Favourite 1"),
            FavouriteStop("222222", "Favourite 2"),
            FavouriteStop("333333", "Favourite 3")
        )
        val expected = listOf(
            UiFavouriteStop(
                FavouriteStop("111111", "Favourite 1"),
                listOf("1", "2", "3"),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("222222", "Favourite 2"),
                null,
                false
            ),
            UiFavouriteStop(
                FavouriteStop("333333", "Favourite 3"),
                listOf("1"),
                false
            )
        )
        whenever(favouritesRepository.allFavouriteStopsFlow)
            .thenReturn(flowOf(favouriteStops))
        val stopCodes = setOf("111111", "222222", "333333")
        whenever(serviceStopsRepository.getServicesForStopsFlow(stopCodes))
            .thenReturn(
                flowOf(
                    mapOf(
                        "111111" to listOf("1", "2", "3"),
                        "333333" to listOf("1")
                    )
                )
            )

        retriever.allFavouriteStopsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithChangingFavouritesEmitsExpectedLists() = runTest {
        val favouriteStops1 = listOf(
            FavouriteStop("111111", "Favourite 1"),
            FavouriteStop("222222", "Favourite 2"),
            FavouriteStop("333333", "Favourite 3")
        )
        val favouriteStops2 = listOf(
            FavouriteStop("111111", "Favourite 1"),
            FavouriteStop("333333", "Favourite 3")
        )
        val favouriteStops3 = listOf(
            FavouriteStop("444444", "Favourite 4"),
            FavouriteStop("111111", "Favourite 1"),
            FavouriteStop("222222", "Favourite 2")
        )
        val expected1 = listOf(
            UiFavouriteStop(
                FavouriteStop("111111", "Favourite 1"),
                listOf("1", "2"),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("222222", "Favourite 2"),
                listOf("3", "4"),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("333333", "Favourite 3"),
                listOf("5", "6"),
                false
            )
        )
        val expected2 = listOf(
            UiFavouriteStop(
                FavouriteStop("111111", "Favourite 1"),
                listOf("1", "2"),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("333333", "Favourite 3"),
                listOf("5", "6"),
                false
            )
        )
        val expected3 = listOf(
            UiFavouriteStop(
                FavouriteStop("444444", "Favourite 4"),
                listOf("7", "8"),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("111111", "Favourite 1"),
                listOf("1", "2"),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("222222", "Favourite 2"),
                listOf("3", "4"),
                false
            )
        )
        whenever(favouritesRepository.allFavouriteStopsFlow)
            .thenReturn(
                intervalFlowOf(
                    0L,
                    100L,
                    favouriteStops1,
                    favouriteStops2,
                    favouriteStops3
                )
            )
        whenever(serviceStopsRepository.getServicesForStopsFlow(any()))
            .thenReturn(
                flowOf(
                    mapOf(
                        "111111" to listOf("1", "2"),
                        "222222" to listOf("3", "4"),
                        "333333" to listOf("5", "6"),
                        "444444" to listOf("7", "8")
                    )
                )
            )

        retriever.allFavouriteStopsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            assertEquals(expected3, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithChangingServicesEmitsExpectedList() = runTest {
        val favouriteStops = listOf(
            FavouriteStop("111111", "Favourite 1"),
            FavouriteStop("222222", "Favourite 2"),
            FavouriteStop("333333", "Favourite 3")
        )
        val expected1 = listOf(
            UiFavouriteStop(
                FavouriteStop("111111", "Favourite 1"),
                listOf("1", "2"),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("222222", "Favourite 2"),
                listOf("3", "4"),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("333333", "Favourite 3"),
                listOf("5", "6"),
                false
            )
        )
        val expected2 = listOf(
            UiFavouriteStop(
                FavouriteStop("111111", "Favourite 1"),
                listOf("1", "2"),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("222222", "Favourite 2"),
                listOf("7", "8"),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("333333", "Favourite 3"),
                listOf("5", "6"),
                false
            )
        )
        val expected3 = listOf(
            UiFavouriteStop(
                FavouriteStop("111111", "Favourite 1"),
                listOf("9", "10"),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("222222", "Favourite 2"),
                listOf("7", "8"),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("333333", "Favourite 3"),
                listOf("5", "6"),
                false
            )
        )
        whenever(favouritesRepository.allFavouriteStopsFlow)
            .thenReturn(flowOf(favouriteStops))
        val stopCodes = setOf("111111", "222222", "333333")
        whenever(serviceStopsRepository.getServicesForStopsFlow(stopCodes))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    100L,
                    mapOf(
                        "111111" to listOf("1", "2"),
                        "222222" to listOf("3", "4"),
                        "333333" to listOf("5", "6")
                    ),
                    mapOf(
                        "111111" to listOf("1", "2"),
                        "222222" to listOf("7", "8"),
                        "333333" to listOf("5", "6")
                    ),
                    mapOf(
                        "111111" to listOf("9", "10"),
                        "222222" to listOf("7", "8"),
                        "333333" to listOf("5", "6")
                    )
                )
            )

        retriever.allFavouriteStopsFlow.test {
            assertNull(awaitItem())
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            assertEquals(expected3, awaitItem())
        }
    }
}