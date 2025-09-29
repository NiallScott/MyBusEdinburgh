/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            favouriteStopsRetriever = FakeFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = { flowOf(null) }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { flowOf(null) }
            ),
            dropdownMenuGenerator = FakeUiFavouriteDropdownMenuGenerator(
                onUiFavouriteDropdownItemsForStopFlow = { flowOf(null) }
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
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            favouriteStopsRetriever = FakeFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = { flowOf(emptyList()) }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = { flowOf(null) }
            ),
            dropdownMenuGenerator = FakeUiFavouriteDropdownMenuGenerator(
                onUiFavouriteDropdownItemsForStopFlow = { flowOf(null) }
            )
        )

        retriever.allFavouriteStopsFlow.test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithNoShortcutMode() = runTest {
        val retriever = createUiFavouriteStopsRetriever(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(false) }
            ),
            favouriteStopsRetriever = FakeFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = {
                    flowOf(
                        listOf(
                            FavouriteStopWithServices(
                                stopCode = "100",
                                savedName = "Saved Name 1",
                                services = listOf("1", "2", "3")
                            ),
                            FavouriteStopWithServices(
                                stopCode = "200",
                                savedName = "Saved Name 2",
                                services = null
                            ),
                            FavouriteStopWithServices(
                                stopCode = "300",
                                savedName = "Saved Name 3",
                                services = listOf("3", "4", "5")
                            )
                        )
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = {
                    flowOf(
                        mapOf(
                            "1" to ServiceColours(
                                primaryColour = 1000,
                                colourOnPrimary = 1001
                            ),
                            "3" to ServiceColours(
                                primaryColour = 2000,
                                colourOnPrimary = 2001
                            ),
                            "5" to ServiceColours(
                                primaryColour = 3000,
                                colourOnPrimary = 3001
                            )
                        )
                    )
                }
            ),
            dropdownMenuGenerator = FakeUiFavouriteDropdownMenuGenerator(
                onUiFavouriteDropdownItemsForStopFlow = {
                    flowOf(
                        "200" to UiFavouriteDropdownMenu(
                            items = persistentListOf(
                                UiFavouriteDropdownItem.EditFavouriteName,
                                UiFavouriteDropdownItem.RemoveFavourite
                            )
                        )
                    )
                }
            )
        )

        retriever.allFavouriteStopsFlow.test {
            assertEquals(
                listOf(
                    UiFavouriteStop(
                        stopCode = "100",
                        savedName = "Saved Name 1",
                        services = persistentListOf(
                            UiServiceName(
                                serviceName = "1",
                                colours = UiServiceColours(
                                    backgroundColour = 1000,
                                    textColour = 1001
                                )
                            ),
                            UiServiceName(
                                serviceName = "2",
                                colours = null
                            ),
                            UiServiceName(
                                serviceName = "3",
                                colours = UiServiceColours(
                                    backgroundColour = 2000,
                                    textColour = 2001
                                )
                            )
                        ),
                        dropdownMenu = UiFavouriteDropdownMenu()
                    ),
                    UiFavouriteStop(
                        stopCode = "200",
                        savedName = "Saved Name 2",
                        services = null,
                        dropdownMenu = UiFavouriteDropdownMenu(
                            items = persistentListOf(
                                UiFavouriteDropdownItem.EditFavouriteName,
                                UiFavouriteDropdownItem.RemoveFavourite
                            )
                        )
                    ),
                    UiFavouriteStop(
                        stopCode = "300",
                        savedName = "Saved Name 3",
                        services = persistentListOf(
                            UiServiceName(
                                serviceName = "3",
                                colours = UiServiceColours(
                                    backgroundColour = 2000,
                                    textColour = 2001
                                )
                            ),
                            UiServiceName(
                                serviceName = "4",
                                colours = null
                            ),
                            UiServiceName(
                                serviceName = "5",
                                colours = UiServiceColours(
                                    backgroundColour = 3000,
                                    textColour = 3001
                                )
                            )
                        ),
                        dropdownMenu = UiFavouriteDropdownMenu()
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun allFavouriteStopsFlowWithShortcutMode() = runTest {
        val retriever = createUiFavouriteStopsRetriever(
            arguments = FakeArguments(
                onIsShortcutModeFlow = { flowOf(true) }
            ),
            favouriteStopsRetriever = FakeFavouriteStopsRetriever(
                onAllFavouriteStopsFlow = {
                    flowOf(
                        listOf(
                            FavouriteStopWithServices(
                                stopCode = "100",
                                savedName = "Saved Name 1",
                                services = listOf("1", "2", "3")
                            ),
                            FavouriteStopWithServices(
                                stopCode = "200",
                                savedName = "Saved Name 2",
                                services = null
                            ),
                            FavouriteStopWithServices(
                                stopCode = "300",
                                savedName = "Saved Name 3",
                                services = listOf("3", "4", "5")
                            )
                        )
                    )
                }
            ),
            servicesRepository = FakeServicesRepository(
                onGetColoursForServicesFlow = {
                    flowOf(
                        mapOf(
                            "1" to ServiceColours(
                                primaryColour = 1000,
                                colourOnPrimary = 1001
                            ),
                            "3" to ServiceColours(
                                primaryColour = 2000,
                                colourOnPrimary = 2001
                            ),
                            "5" to ServiceColours(
                                primaryColour = 3000,
                                colourOnPrimary = 3001
                            )
                        )
                    )
                }
            ),
            dropdownMenuGenerator = FakeUiFavouriteDropdownMenuGenerator(
                onUiFavouriteDropdownItemsForStopFlow = { flowOf(null) }
            )
        )

        retriever.allFavouriteStopsFlow.test {
            assertEquals(
                listOf(
                    UiFavouriteStop(
                        stopCode = "100",
                        savedName = "Saved Name 1",
                        services = persistentListOf(
                            UiServiceName(
                                serviceName = "1",
                                colours = UiServiceColours(
                                    backgroundColour = 1000,
                                    textColour = 1001
                                )
                            ),
                            UiServiceName(
                                serviceName = "2",
                                colours = null
                            ),
                            UiServiceName(
                                serviceName = "3",
                                colours = UiServiceColours(
                                    backgroundColour = 2000,
                                    textColour = 2001
                                )
                            )
                        ),
                        dropdownMenu = null
                    ),
                    UiFavouriteStop(
                        stopCode = "200",
                        savedName = "Saved Name 2",
                        services = null,
                        dropdownMenu = null
                    ),
                    UiFavouriteStop(
                        stopCode = "300",
                        savedName = "Saved Name 3",
                        services = persistentListOf(
                            UiServiceName(
                                serviceName = "3",
                                colours = UiServiceColours(
                                    backgroundColour = 2000,
                                    textColour = 2001
                                )
                            ),
                            UiServiceName(
                                serviceName = "4",
                                colours = null
                            ),
                            UiServiceName(
                                serviceName = "5",
                                colours = UiServiceColours(
                                    backgroundColour = 3000,
                                    textColour = 3001
                                )
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

    private fun createUiFavouriteStopsRetriever(
        arguments: Arguments = FakeArguments(),
        favouriteStopsRetriever: FavouriteStopsRetriever = FakeFavouriteStopsRetriever(),
        servicesRepository: ServicesRepository = FakeServicesRepository(),
        dropdownMenuGenerator: UiFavouriteDropdownMenuGenerator =
            FakeUiFavouriteDropdownMenuGenerator()
    ): RealUiFavouriteStopsRetriever {
        return RealUiFavouriteStopsRetriever(
            arguments = arguments,
            favouriteStopsRetriever = favouriteStopsRetriever,
            servicesRepository = servicesRepository,
            dropdownMenuGenerator = dropdownMenuGenerator
        )
    }
}
