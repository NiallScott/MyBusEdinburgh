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
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.services.FakeServicesRepository
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [RealUiFavouriteStopsRetriever].
 *
 * @author Niall Scott
 */
class RealUiFavouriteStopsRetrieverTest {

    @Test
    fun allFavouriteStopsFlowWithNullFavouriteStopsEmitsNull() = runTest {
        val retriever = createUiFavouriteStopsRetriever(
            favouriteStopsRetriever = FakeFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = { flowOf(null) }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { flowOf(null) }
            )
        )

        retriever.allFavouriteStopsFlow.test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithEmptyFavouriteStopsEmitsNull() = runTest {
        val retriever = createUiFavouriteStopsRetriever(
            favouriteStopsRetriever = FakeFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = { flowOf(emptyList()) }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { flowOf(null) }
            )
        )

        retriever.allFavouriteStopsFlow.test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithPopulatedStopsButNullColoursEmitsFavourites() = runTest {
        val retriever = createUiFavouriteStopsRetriever(
            favouriteStopsRetriever = FakeFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = {
                    flowOf(
                        listOf(
                            FavouriteStopWithServices(
                                stopIdentifier = "123456".toNaptanStopIdentifier(),
                                savedName = "Saved Name",
                                services = listOf(service(1), service(2))
                            )
                        )
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { flowOf(null) }
            ),
            dropdownMenuGenerator = FakeUiFavouriteDropdownMenuGenerator(
                onGetDropdownMenuItemsForStopsFlow = { flowOf(null) }
            )
        )

        retriever.allFavouriteStopsFlow.test {
            assertEquals(
                listOf(
                    UiFavouriteStop(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        savedName = "Saved Name",
                        services = persistentListOf(
                            UiServiceName(
                                serviceName = "1",
                                colours = null
                            ),
                            UiServiceName(
                                serviceName = "2",
                                colours = null
                            )
                        ),
                        dropdownMenu = null
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithPopulatedFavouritesButEmptyColoursEmitsFavourites() = runTest {
        val retriever = createUiFavouriteStopsRetriever(
            favouriteStopsRetriever = FakeFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = {
                    flowOf(
                        listOf(
                            FavouriteStopWithServices(
                                stopIdentifier = "123456".toNaptanStopIdentifier(),
                                savedName = "Saved Name",
                                services = listOf(service(1), service(2))
                            )
                        )
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { flowOf(emptyMap()) }
            ),
            dropdownMenuGenerator = FakeUiFavouriteDropdownMenuGenerator(
                onGetDropdownMenuItemsForStopsFlow = { flowOf(null) }
            )
        )

        retriever.allFavouriteStopsFlow.test {
            assertEquals(
                listOf(
                    UiFavouriteStop(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        savedName = "Saved Name",
                        services = persistentListOf(
                            UiServiceName(
                                serviceName = "1",
                                colours = null
                            ),
                            UiServiceName(
                                serviceName = "2",
                                colours = null
                            )
                        ),
                        dropdownMenu = null
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithPopulatedFavouritesAndColorsEmitsFavourites() = runTest {
        val retriever = createUiFavouriteStopsRetriever(
            favouriteStopsRetriever = FakeFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = {
                    flowOf(
                        listOf(
                            FavouriteStopWithServices(
                                stopIdentifier = "123456".toNaptanStopIdentifier(),
                                savedName = "Saved Name",
                                services = listOf(service(1), service(2))
                            )
                        )
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = {
                    flowOf(
                        mapOf(
                            service(1) to ServiceColours(
                                colourPrimary = 100,
                                colourOnPrimary = 101
                            )
                        )
                    )
                }
            ),
            dropdownMenuGenerator = FakeUiFavouriteDropdownMenuGenerator(
                onGetDropdownMenuItemsForStopsFlow = { flowOf(null) }
            )
        )

        retriever.allFavouriteStopsFlow.test {
            assertEquals(
                listOf(
                    UiFavouriteStop(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        savedName = "Saved Name",
                        services = persistentListOf(
                            UiServiceName(
                                serviceName = "1",
                                colours = UiServiceColours(
                                    backgroundColour = 100,
                                    textColour = 101
                                )
                            ),
                            UiServiceName(
                                serviceName = "2",
                                colours = null
                            )
                        ),
                        dropdownMenu = null
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithPopulatedFavouritesButEmptyDropdownEmitsFavourites() = runTest {
        val retriever = createUiFavouriteStopsRetriever(
            favouriteStopsRetriever = FakeFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = {
                    flowOf(
                        listOf(
                            FavouriteStopWithServices(
                                stopIdentifier = "123456".toNaptanStopIdentifier(),
                                savedName = "Saved Name",
                                services = null
                            )
                        )
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { flowOf(null) }
            ),
            dropdownMenuGenerator = FakeUiFavouriteDropdownMenuGenerator(
                onGetDropdownMenuItemsForStopsFlow = { flowOf(null) }
            )
        )

        retriever.allFavouriteStopsFlow.test {
            assertEquals(
                listOf(
                    UiFavouriteStop(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        savedName = "Saved Name",
                        services = null,
                        dropdownMenu = null
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithPopulatedFavouritesAndDropdownMenusEmitsFavourites() = runTest {
        val retriever = createUiFavouriteStopsRetriever(
            favouriteStopsRetriever = FakeFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = {
                    flowOf(
                        listOf(
                            FavouriteStopWithServices(
                                stopIdentifier = "123456".toNaptanStopIdentifier(),
                                savedName = "Saved Name",
                                services = null
                            )
                        )
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { flowOf(null) }
            ),
            dropdownMenuGenerator = FakeUiFavouriteDropdownMenuGenerator(
                onGetDropdownMenuItemsForStopsFlow = {
                    flowOf(
                        mapOf(
                            "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu()
                        )
                    )
                }
            )
        )

        retriever.allFavouriteStopsFlow.test {
            assertEquals(
                listOf(
                    UiFavouriteStop(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        savedName = "Saved Name",
                        services = null,
                        dropdownMenu = UiFavouriteDropdownMenu()
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createUiFavouriteStopsRetriever(
        favouriteStopsRetriever: FavouriteStopsRetriever = FakeFavouriteStopsRetriever(),
        servicesRepository: ServicesRepository = FakeServicesRepository(),
        dropdownMenuGenerator: UiFavouriteDropdownMenuGenerator =
            FakeUiFavouriteDropdownMenuGenerator()
    ): RealUiFavouriteStopsRetriever {
        return RealUiFavouriteStopsRetriever(
            favouriteStopsRetriever = favouriteStopsRetriever,
            servicesRepository = servicesRepository,
            dropdownMenuGenerator = dropdownMenuGenerator
        )
    }

    private fun service(id: Int): ServiceDescriptor {
        return FakeServiceDescriptor(
            serviceName = id.toString(),
            operatorCode = "TEST$id"
        )
    }
}
