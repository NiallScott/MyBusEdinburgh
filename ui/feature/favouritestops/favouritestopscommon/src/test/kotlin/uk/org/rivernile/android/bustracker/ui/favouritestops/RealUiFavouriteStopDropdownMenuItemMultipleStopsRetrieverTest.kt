/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favouritestops

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.favourites.FakeFavouritesRepository
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [RealUiFavouriteStopDropdownMenuItemMultipleStopsRetriever].
 *
 * @author Niall Scott
 */
class RealUiFavouriteStopDropdownMenuItemMultipleStopsRetrieverTest {

    @Test
    fun getUiFavouriteStopDropdownMenuItemsFlowEmitsNullWhenStopIdentifiersIsEmpty() = runTest {
        createRetriever().getUiFavouriteStopDropdownMenuItemsFlow(emptySet()).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiFavouriteStopDropdownMenuItemsFlowWhenFavouriteStopsIsNull() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            favouritesRepository = FakeFavouritesRepository(
                onAllFavouriteStopsStopIdentifiersFlow = { flowOf(null) }
            )
        )

        retriever.getUiFavouriteStopDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteStopDropdownMenuItem>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteStopDropdownMenuItem(
                        isFavouriteStop = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiFavouriteStopDropdownMenuItemsFlowWhenFavouriteStopsIsEmpty() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            favouritesRepository = FakeFavouritesRepository(
                onAllFavouriteStopsStopIdentifiersFlow = { flowOf(emptySet()) }
            )
        )

        retriever.getUiFavouriteStopDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteStopDropdownMenuItem>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteStopDropdownMenuItem(
                        isFavouriteStop = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiFavouriteStopDropdownMenuItemsFlowWhenFavouriteStopsHasNoMatchingItems() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            favouritesRepository = FakeFavouritesRepository(
                onAllFavouriteStopsStopIdentifiersFlow = {
                    flowOf(setOf("987654".toNaptanStopIdentifier()))
                }
            )
        )

        retriever.getUiFavouriteStopDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteStopDropdownMenuItem>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteStopDropdownMenuItem(
                        isFavouriteStop = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiFavouriteStopDropdownMenuItemsFlowWhenFavouriteStopsHasMatchingItem() = runTest {
        val requestedStops = setOf("123456".toNaptanStopIdentifier())
        val retriever = createRetriever(
            favouritesRepository = FakeFavouritesRepository(
                onAllFavouriteStopsStopIdentifiersFlow = {
                    flowOf(setOf("123456".toNaptanStopIdentifier()))
                }
            )
        )

        retriever.getUiFavouriteStopDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteStopDropdownMenuItem>(
                    "123456".toNaptanStopIdentifier() to UiFavouriteStopDropdownMenuItem(
                        isFavouriteStop = true
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun getUiFavouriteStopDropdownMenuItemsFlowWithMultipleStops() = runTest {
        val requestedStops = setOf(
            "1".toNaptanStopIdentifier(),
            "2".toNaptanStopIdentifier(),
            "3".toNaptanStopIdentifier(),
            "4".toNaptanStopIdentifier(),
            "5".toNaptanStopIdentifier()
        )
        val retriever = createRetriever(
            favouritesRepository = FakeFavouritesRepository(
                onAllFavouriteStopsStopIdentifiersFlow = {
                    flowOf(
                        setOf(
                            "2".toNaptanStopIdentifier(),
                            "4".toNaptanStopIdentifier()
                        )
                    )
                }
            )
        )

        retriever.getUiFavouriteStopDropdownMenuItemsFlow(requestedStops).test {
            assertEquals(
                mapOf<StopIdentifier, UiFavouriteStopDropdownMenuItem>(
                    "1".toNaptanStopIdentifier() to UiFavouriteStopDropdownMenuItem(
                        isFavouriteStop = false
                    ),
                    "2".toNaptanStopIdentifier() to UiFavouriteStopDropdownMenuItem(
                        isFavouriteStop = true
                    ),
                    "3".toNaptanStopIdentifier() to UiFavouriteStopDropdownMenuItem(
                        isFavouriteStop = false
                    ),
                    "4".toNaptanStopIdentifier() to UiFavouriteStopDropdownMenuItem(
                        isFavouriteStop = true
                    ),
                    "5".toNaptanStopIdentifier() to UiFavouriteStopDropdownMenuItem(
                        isFavouriteStop = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createRetriever(
        favouritesRepository: FavouritesRepository = FakeFavouritesRepository()
    ): UiFavouriteStopDropdownMenuItemMultipleStopsRetriever {
        return RealUiFavouriteStopDropdownMenuItemMultipleStopsRetriever(
            favouritesRepository = favouritesRepository
        )
    }
}
