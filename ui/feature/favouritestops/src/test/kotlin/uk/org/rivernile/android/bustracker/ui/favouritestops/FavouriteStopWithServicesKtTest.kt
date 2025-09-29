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

import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `FavouriteStopsWithServices.kt`.
 *
 * @author Niall Scott
 */
class FavouriteStopWithServicesKtTest {

    @Test
    fun toFavouriteStopsWithServicesWithEmptyListMapsToEmptyList() {
        val result = emptyList<FavouriteStop>().toFavouriteStopsWithServices(null)

        assertEquals(emptyList(), result)
    }

    @Test
    fun toFavouriteStopsWithServicesWithNullServicesMapMapsCorrectly() {
        val expected = listOf(
            FavouriteStopWithServices(
                stopCode = "123456",
                savedName = "Saved name",
                services = null
            )
        )

        val result = listOf(
            FavouriteStop(
                stopCode = "123456",
                stopName = "Saved name"
            )
        ).toFavouriteStopsWithServices(null)

        assertEquals(expected, result)
    }

    @Test
    fun toFavouriteStopsWithServicesWithEmptyServicesMapMapsCorrectly() {
        val expected = listOf(
            FavouriteStopWithServices(
                stopCode = "123456",
                savedName = "Saved name",
                services = null
            )
        )

        val result = listOf(
            FavouriteStop(
                stopCode = "123456",
                stopName = "Saved name"
            )
        ).toFavouriteStopsWithServices(emptyMap())

        assertEquals(expected, result)
    }

    @Test
    fun toFavouriteStopsWithServiceWithEmptyServicesMapsCorrectly() {
        val expected = listOf(
            FavouriteStopWithServices(
                stopCode = "123456",
                savedName = "Saved name",
                services = null
            )
        )

        val result = listOf(
            FavouriteStop(
                stopCode = "123456",
                stopName = "Saved name"
            )
        ).toFavouriteStopsWithServices(
            stopServices = mapOf(
                "123456" to emptyList()
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun toFavouriteStopsWithServiceWithServicesMapMapsCorrectly() {
        val expected = listOf(
            FavouriteStopWithServices(
                stopCode = "123456",
                savedName = "Saved name",
                services = listOf("1", "2", "3")
            )
        )

        val result = listOf(
            FavouriteStop(
                stopCode = "123456",
                stopName = "Saved name"
            )
        ).toFavouriteStopsWithServices(
            stopServices = mapOf(
                "123456" to listOf("1", "2", "3")
            )
        )

        assertEquals(expected, result)
    }
}
