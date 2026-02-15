/*
 * Copyright (C) 2025 - 2026 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.favourites.FakeFavouritesRepository
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.core.servicestops.FakeServiceStopsRepository
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [RealFavouriteStopsRetriever].
 *
 * @author Niall Scott
 */
class RealFavouriteStopsRetrieverTest {

    @Test
    fun allFavouriteStopsFlowWithNullFavouriteStopsEmitsNull() = runTest {
        val retriever = createFavouriteStopsRetriever(
            favouritesRepository = FakeFavouritesRepository(
                onAllFavouriteStopsFlow = { flowOf(null) }
            )
        )

        retriever.allFavouriteStopsFlow.test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithEmptyFavouriteStopsEmitsNull() = runTest {
        val retriever = createFavouriteStopsRetriever(
            favouritesRepository = FakeFavouritesRepository(
                onAllFavouriteStopsFlow = { flowOf(emptyList()) }
            )
        )

        retriever.allFavouriteStopsFlow.test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithPopulatedFavouritesEmitsCorrectItems() = runTest {
        val retriever = createFavouriteStopsRetriever(
            favouritesRepository = FakeFavouritesRepository(
                onAllFavouriteStopsFlow = {
                    flowOf(
                        listOf(
                            FavouriteStop(
                                stopIdentifier = "1".toNaptanStopIdentifier(),
                                stopName = "One"
                            ),
                            FavouriteStop(
                                stopIdentifier = "2".toNaptanStopIdentifier(),
                                stopName = "Two"
                            ),
                            FavouriteStop(
                                stopIdentifier = "3".toNaptanStopIdentifier(),
                                stopName = "Three"
                            )
                        )
                    )
                }
            ),
            serviceStopsRepository = FakeServiceStopsRepository(
                onGetServicesForStopsFlow = { stopIdentifiers ->
                    assertEquals(
                        setOf(
                            "1".toNaptanStopIdentifier(),
                            "2".toNaptanStopIdentifier(),
                            "3".toNaptanStopIdentifier()
                        ),
                        stopIdentifiers
                    )
                    flowOf(
                        mapOf(
                            "1".toNaptanStopIdentifier() to listOf(service(100), service(200)),
                            "3".toNaptanStopIdentifier() to listOf(service(300), service(400))
                        )
                    )
                }
            )
        )

        retriever.allFavouriteStopsFlow.test {
            assertEquals(
                listOf(
                    FavouriteStopWithServices(
                        stopIdentifier = "1".toNaptanStopIdentifier(),
                        savedName = "One",
                        services = listOf(service(100), service(200))
                    ),
                    FavouriteStopWithServices(
                        stopIdentifier = "2".toNaptanStopIdentifier(),
                        savedName = "Two",
                        services = null
                    ),
                    FavouriteStopWithServices(
                        stopIdentifier = "3".toNaptanStopIdentifier(),
                        savedName = "Three",
                        services = listOf(service(300), service(400))
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createFavouriteStopsRetriever(
        favouritesRepository: FavouritesRepository = FakeFavouritesRepository(),
        serviceStopsRepository: ServiceStopsRepository = FakeServiceStopsRepository()
    ): RealFavouriteStopsRetriever {
        return RealFavouriteStopsRetriever(
            favouritesRepository = favouritesRepository,
            serviceStopsRepository = serviceStopsRepository
        )
    }

    private fun service(id: Int): ServiceDescriptor {
        return FakeServiceDescriptor(
            serviceName = id.toString(),
            operatorCode = "TEST$id"
        )
    }
}
