/*
 * Copyright (C) 2021 - 2026 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
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
        retriever = FavouriteStopsRetriever(
            favouritesRepository,
            serviceStopsRepository,
            naturalOrder()
        )
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
            FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
            FavouriteStop("222222".toNaptanStopIdentifier(), "Favourite 2"),
            FavouriteStop("333333".toNaptanStopIdentifier(), "Favourite 3")
        )
        val expected = listOf(
            UiFavouriteStop(
                FavouriteStop(
                    "111111".toNaptanStopIdentifier(),
                    "Favourite 1"
                ),
                null,
                false
            ),
            UiFavouriteStop(
                FavouriteStop(
                    "222222".toNaptanStopIdentifier(),
                    "Favourite 2"
                ),
                null,
                false
            ),
            UiFavouriteStop(
                FavouriteStop(
                    "333333".toNaptanStopIdentifier(),
                    "Favourite 3"
                ),
                null,
                false
            )
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
            FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
            FavouriteStop("222222".toNaptanStopIdentifier(), "Favourite 2"),
            FavouriteStop("333333".toNaptanStopIdentifier(), "Favourite 3")
        )
        val expected = listOf(
            UiFavouriteStop(
                FavouriteStop(
                    "111111".toNaptanStopIdentifier(),
                    "Favourite 1"
                ),
                null,
                false
            ),
            UiFavouriteStop(
                FavouriteStop(
                    "222222".toNaptanStopIdentifier(),
                    "Favourite 2"
                ),
                null,
                false
            ),
            UiFavouriteStop(
                FavouriteStop(
                    "333333".toNaptanStopIdentifier(),
                    "Favourite 3"
                ),
                null,
                false
            )
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
            FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
            FavouriteStop("222222".toNaptanStopIdentifier(), "Favourite 2"),
            FavouriteStop("333333".toNaptanStopIdentifier(), "Favourite 3")
        )
        val expected = listOf(
            UiFavouriteStop(
                FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
                listOf(service(1), service(2), service(3)),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("222222".toNaptanStopIdentifier(), "Favourite 2"),
                null,
                false
            ),
            UiFavouriteStop(
                FavouriteStop("333333".toNaptanStopIdentifier(), "Favourite 3"),
                listOf(service(1)),
                false
            )
        )
        whenever(favouritesRepository.allFavouriteStopsFlow)
            .thenReturn(flowOf(favouriteStops))
        val stopIdentifiers = setOf(
            "111111".toNaptanStopIdentifier(),
            "222222".toNaptanStopIdentifier(),
            "333333".toNaptanStopIdentifier()
        )
        whenever(serviceStopsRepository.getServicesForStopsFlow(stopIdentifiers))
            .thenReturn(
                flowOf(
                    mapOf(
                        "111111".toNaptanStopIdentifier() to
                            listOf(service(2), service(1), service(3)),
                        "333333".toNaptanStopIdentifier() to listOf(service(1))
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
            FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
            FavouriteStop("222222".toNaptanStopIdentifier(), "Favourite 2"),
            FavouriteStop("333333".toNaptanStopIdentifier(), "Favourite 3")
        )
        val favouriteStops2 = listOf(
            FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
            FavouriteStop("333333".toNaptanStopIdentifier(), "Favourite 3")
        )
        val favouriteStops3 = listOf(
            FavouriteStop("444444".toNaptanStopIdentifier(), "Favourite 4"),
            FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
            FavouriteStop("222222".toNaptanStopIdentifier(), "Favourite 2")
        )
        val expected1 = listOf(
            UiFavouriteStop(
                FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
                listOf(service(1), service(2)),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("222222".toNaptanStopIdentifier(), "Favourite 2"),
                listOf(service(3), service(4)),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("333333".toNaptanStopIdentifier(), "Favourite 3"),
                listOf(service(5), service(6)),
                false
            )
        )
        val expected2 = listOf(
            UiFavouriteStop(
                FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
                listOf(service(1), service(2)),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("333333".toNaptanStopIdentifier(), "Favourite 3"),
                listOf(service(5), service(6)),
                false
            )
        )
        val expected3 = listOf(
            UiFavouriteStop(
                FavouriteStop("444444".toNaptanStopIdentifier(), "Favourite 4"),
                listOf(service(7), service(8)),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
                listOf(service(1), service(2)),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("222222".toNaptanStopIdentifier(), "Favourite 2"),
                listOf(service(3), service(4)),
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
                        "111111".toNaptanStopIdentifier() to listOf(service(2), service(1)),
                        "222222".toNaptanStopIdentifier() to listOf(service(3), service(4)),
                        "333333".toNaptanStopIdentifier() to listOf(service(6), service(5)),
                        "444444".toNaptanStopIdentifier() to listOf(service(7), service(8))
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
            FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
            FavouriteStop("222222".toNaptanStopIdentifier(), "Favourite 2"),
            FavouriteStop("333333".toNaptanStopIdentifier(), "Favourite 3")
        )
        val expected1 = listOf(
            UiFavouriteStop(
                FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
                listOf(service(1), service(2)),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("222222".toNaptanStopIdentifier(), "Favourite 2"),
                listOf(service(3), service(4)),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("333333".toNaptanStopIdentifier(), "Favourite 3"),
                listOf(service(5), service(6)),
                false
            )
        )
        val expected2 = listOf(
            UiFavouriteStop(
                FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
                listOf(service(1), service(2)),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("222222".toNaptanStopIdentifier(), "Favourite 2"),
                listOf(service(7), service(8)),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("333333".toNaptanStopIdentifier(), "Favourite 3"),
                listOf(service(5), service(6)),
                false
            )
        )
        val expected3 = listOf(
            UiFavouriteStop(
                FavouriteStop("111111".toNaptanStopIdentifier(), "Favourite 1"),
                // ...seemingly in wrong order, because of natural sort
                listOf(service(10), service(9)),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("222222".toNaptanStopIdentifier(), "Favourite 2"),
                listOf(service(7), service(8)),
                false
            ),
            UiFavouriteStop(
                FavouriteStop("333333".toNaptanStopIdentifier(), "Favourite 3"),
                listOf(service(5), service(6)),
                false
            )
        )
        whenever(favouritesRepository.allFavouriteStopsFlow)
            .thenReturn(flowOf(favouriteStops))
        val stopIdentifiers = setOf(
            "111111".toNaptanStopIdentifier(),
            "222222".toNaptanStopIdentifier(),
            "333333".toNaptanStopIdentifier()
        )
        whenever(serviceStopsRepository.getServicesForStopsFlow(stopIdentifiers))
            .thenReturn(
                intervalFlowOf(
                    0L,
                    100L,
                    mapOf(
                        "111111".toNaptanStopIdentifier() to listOf(service(1), service(2)),
                        "222222".toNaptanStopIdentifier() to listOf(service(4), service(3)),
                        "333333".toNaptanStopIdentifier() to listOf(service(5), service(6))
                    ),
                    mapOf(
                        "111111".toNaptanStopIdentifier() to listOf(service(1), service(2)),
                        "222222".toNaptanStopIdentifier() to listOf(service(8), service(7)),
                        "333333".toNaptanStopIdentifier() to listOf(service(5), service(6))
                    ),
                    mapOf(
                        "111111".toNaptanStopIdentifier() to listOf(service(10), service(9)),
                        "222222".toNaptanStopIdentifier() to listOf(service(7), service(8)),
                        "333333".toNaptanStopIdentifier() to listOf(service(6), service(5))
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

    private fun service(id: Int): ServiceDescriptor {
        return FakeServiceDescriptor(
            serviceName = id.toString(),
            operatorCode = "TEST$id"
        )
    }
}
