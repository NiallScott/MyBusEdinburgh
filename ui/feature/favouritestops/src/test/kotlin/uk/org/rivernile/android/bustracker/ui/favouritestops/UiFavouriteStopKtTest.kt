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

import kotlinx.collections.immutable.persistentListOf
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
            isShortcutMode = false,
            serviceColours = null,
            dropdownMenu = null
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun toUiFavouriteStopsMapsStopCodeAndSavedName() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopCode = "123456",
                savedName = "Saved name",
                services = null
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopCode = "123456",
                savedName = "Saved name",
                services = null,
                dropdownMenu = UiFavouriteDropdownMenu()
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            isShortcutMode = false,
            serviceColours = null,
            dropdownMenu = null
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsMapsStopCodeAndSavedNameWithEmptyServices() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopCode = "123456",
                savedName = "Saved name",
                services = emptyList()
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopCode = "123456",
                savedName = "Saved name",
                services = null,
                dropdownMenu = UiFavouriteDropdownMenu()
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            isShortcutMode = false,
            serviceColours = null,
            dropdownMenu = null
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsMapsColoursWithNullColoursMap() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopCode = "123456",
                savedName = "Saved name",
                services = listOf("1", "2", "3")
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopCode = "123456",
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
                dropdownMenu = UiFavouriteDropdownMenu()
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            isShortcutMode = false,
            serviceColours = null,
            dropdownMenu = null
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsMapsColoursWithEmptyColoursMap() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopCode = "123456",
                savedName = "Saved name",
                services = listOf("1", "2", "3")
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopCode = "123456",
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
                dropdownMenu = UiFavouriteDropdownMenu()
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            isShortcutMode = false,
            serviceColours = emptyMap(),
            dropdownMenu = null
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsMapsColoursWithPopulatedColoursMap() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopCode = "123456",
                savedName = "Saved name",
                services = listOf("1", "2", "3")
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopCode = "123456",
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
                dropdownMenu = UiFavouriteDropdownMenu()
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            isShortcutMode = false,
            serviceColours = mapOf(
                "1" to ServiceColours(
                    primaryColour = 100,
                    colourOnPrimary = 200
                ),
                "3" to ServiceColours(
                    primaryColour = 300,
                    colourOnPrimary = 400
                )
            ),
            dropdownMenu = null
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsMapsWithNotShortcutModeAndDropdownForUnrecognisedStop() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopCode = "123456",
                savedName = "Saved name",
                services = null
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopCode = "123456",
                savedName = "Saved name",
                services = null,
                dropdownMenu = UiFavouriteDropdownMenu()
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            isShortcutMode = false,
            serviceColours = null,
            dropdownMenu = "987654" to UiFavouriteDropdownMenu(
                items = persistentListOf(
                    UiFavouriteDropdownItem.EditFavouriteName,
                    UiFavouriteDropdownItem.RemoveFavourite
                )
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsMapsWithNotShortcutModeAndDropdownMenuForRecognisedStop() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopCode = "123456",
                savedName = "Saved name",
                services = null
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopCode = "123456",
                savedName = "Saved name",
                services = null,
                dropdownMenu = UiFavouriteDropdownMenu(
                    items = persistentListOf(
                        UiFavouriteDropdownItem.EditFavouriteName,
                        UiFavouriteDropdownItem.RemoveFavourite
                    )
                )
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            isShortcutMode = false,
            serviceColours = null,
            dropdownMenu = "123456" to UiFavouriteDropdownMenu(
                items = persistentListOf(
                    UiFavouriteDropdownItem.EditFavouriteName,
                    UiFavouriteDropdownItem.RemoveFavourite
                )
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsMapsWithShortcutModeAndNullDropdownItem() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopCode = "123456",
                savedName = "Saved name",
                services = null
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopCode = "123456",
                savedName = "Saved name",
                services = null,
                dropdownMenu = null
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            isShortcutMode = true,
            serviceColours = null,
            dropdownMenu = null
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiFavouriteStopsMapsWithShortcutModeAndNonNullDropdownItem() {
        val favouriteStops = listOf(
            FavouriteStopWithServices(
                stopCode = "123456",
                savedName = "Saved name",
                services = null
            )
        )
        val expected = listOf(
            UiFavouriteStop(
                stopCode = "123456",
                savedName = "Saved name",
                services = null,
                dropdownMenu = null
            )
        )

        val result = favouriteStops.toUiFavouriteStops(
            isShortcutMode = true,
            serviceColours = null,
            dropdownMenu = "123456" to UiFavouriteDropdownMenu(
                items = persistentListOf(
                    UiFavouriteDropdownItem.EditFavouriteName,
                    UiFavouriteDropdownItem.RemoveFavourite
                )
            )
        )

        assertEquals(expected, result)
    }
}
