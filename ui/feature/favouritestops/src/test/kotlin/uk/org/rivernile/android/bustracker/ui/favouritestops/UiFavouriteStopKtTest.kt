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

import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for `UiFavouriteStop.kt`.
 *
 * @author Niall Scott
 */
class UiFavouriteStopKtTest {

    @Test
    fun toUiFavouriteStopsWithEmptyListMapsToEmptyList() {
        val result = emptyList<FavouriteStopWithServices>().toUiFavouriteStops(
            serviceColours = null,
            dropdownMenus = null
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun toUiFavouriteStopsMapsStopIdentifierAndSavedName() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = null
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = null,
                dropdownMenu = null
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            serviceColours = null,
            dropdownMenus = null
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsMapsStopIdentifierAndSavedNameWithEmptyServices() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = emptyList()
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = null,
                dropdownMenu = null
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            serviceColours = null,
            dropdownMenus = null
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsMapsColoursWithNullColoursMap() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = listOf(service(1), service(2), service(3))
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = persistentListOf(
                    UiServiceName(
                        serviceName = "1",
                        colours = null
                    ),
                    UiServiceName(
                        serviceName = "2",
                        colours = null
                    ),
                    UiServiceName(
                        serviceName = "3",
                        colours = null
                    )
                ),
                dropdownMenu = null
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            serviceColours = null,
            dropdownMenus = null
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsMapsColoursWithEmptyColoursMap() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = listOf(service(1), service(2), service(3))
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = persistentListOf(
                    UiServiceName(
                        serviceName = "1",
                        colours = null
                    ),
                    UiServiceName(
                        serviceName = "2",
                        colours = null
                    ),
                    UiServiceName(
                        serviceName = "3",
                        colours = null
                    )
                ),
                dropdownMenu = null
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            serviceColours = emptyMap(),
            dropdownMenus = null
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsMapsColoursWithPopulatedColoursMap() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = listOf(service(1), service(2), service(3))
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = persistentListOf(
                    UiServiceName(
                        serviceName = "1",
                        colours = UiServiceColours(
                            backgroundColour = 100,
                            textColour = 200
                        )
                    ),
                    UiServiceName(
                        serviceName = "2",
                        colours = null
                    ),
                    UiServiceName(
                        serviceName = "3",
                        colours = UiServiceColours(
                            backgroundColour = 300,
                            textColour = 400
                        )
                    )
                ),
                dropdownMenu = null
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            serviceColours = mapOf(
                service(1) to ServiceColours(
                    colourPrimary = 100,
                    colourOnPrimary = 200
                ),
                service(3) to ServiceColours(
                    colourPrimary = 300,
                    colourOnPrimary = 400
                )
            ),
            dropdownMenus = null
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsDoesNotIncludeDropdownMenuWhenMapIsEmpty() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = null
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = null,
                dropdownMenu = null
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            serviceColours = null,
            dropdownMenus = emptyMap()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsDoesNotIncludeDropdownMenuWhenMapContainsUnrecognisedItem() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = null
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = null,
                dropdownMenu = null
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            serviceColours = null,
            dropdownMenus = mapOf(
                "987654".toNaptanStopIdentifier() to UiFavouriteDropdownMenu()
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsIncludesDropdownMenuWhenMapContainsExpectedStop() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = null
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                savedName = "Saved name",
                services = null,
                dropdownMenu = UiFavouriteDropdownMenu()
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            serviceColours = null,
            dropdownMenus = mapOf(
                "123456".toNaptanStopIdentifier() to UiFavouriteDropdownMenu()
            )
        )

        assertEquals(expected, result)
    }

    private fun service(id: Int): ServiceDescriptor {
        return FakeServiceDescriptor(
            serviceName = id.toString(),
            operatorCode = "TEST$id"
        )
    }
}
