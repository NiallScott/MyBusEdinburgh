/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for `ServiceColoursMapper.kt`.
 *
 * @author Niall Scott
 */
class ServiceColoursMapperKtTest {

    @Test
    fun toUiServiceNamesOrNullReturnsNullWhenServiceNamesIsNull() {
        val result = toUiServiceNamesOrNull(
            serviceNames = null,
            serviceColours = null,
            serviceNamesComparator = naturalOrder()
        )

        assertNull(result)
    }

    @Test
    fun toUiServiceNamesOrNullReturnsNullWhenServicesCollectionIsEmpty() {
        val result = toUiServiceNamesOrNull(
            serviceNames = emptySet(),
            serviceColours = null,
            serviceNamesComparator = naturalOrder()
        )

        assertNull(result)
    }

    @Test
    fun toUiServiceNamesOrNullReturnsServiceWithoutColourWhenServiceColoursIsNull() {
        val expected = listOf(
            UiServiceName(
                serviceName = "1",
                colours = null
            )
        )

        val result = toUiServiceNamesOrNull(
            serviceNames = setOf("1"),
            serviceColours = null,
            serviceNamesComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiServiceNamesOrNullReturnsServiceWithoutColourWhenServiceColoursIsEmpty() {
        val expected = listOf(
            UiServiceName(
                serviceName = "1",
                colours = null
            )
        )

        val result = toUiServiceNamesOrNull(
            serviceNames = setOf("1"),
            serviceColours = emptyMap(),
            serviceNamesComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiServiceNamesOrNullReturnsServiceWithoutColourWhenServiceColourNotKnown() {
        val expected = listOf(
            UiServiceName(
                serviceName = "1",
                colours = null
            )
        )

        val result = toUiServiceNamesOrNull(
            serviceNames = setOf("1"),
            serviceColours = mapOf(
                "2" to ServiceColours(
                    primaryColour = 0x00111111,
                    colourOnPrimary = 0x00222222
                )
            ),
            serviceNamesComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiServiceNamesOrNullReturnsServiceWithColourWhenServiceColourKnown() {
        val expected = listOf(
            UiServiceName(
                serviceName = "1",
                colours = UiServiceColours(
                    backgroundColour = 0x00111111,
                    textColour = 0x00222222
                )
            )
        )

        val result = toUiServiceNamesOrNull(
            serviceNames = setOf("1"),
            serviceColours = mapOf(
                "1" to ServiceColours(
                    primaryColour = 0x00111111,
                    colourOnPrimary = 0x00222222
                )
            ),
            serviceNamesComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiServiceNamesOrNullReturnsExpectedValuesWithRepresentativeExample() {
        val expected = listOf(
            UiServiceName(
                serviceName = "1",
                colours = UiServiceColours(
                    backgroundColour = 0x00111111,
                    textColour = 0x00222222
                )
            ),
            UiServiceName(
                serviceName = "2",
                colours = null
            ),
            UiServiceName(
                serviceName = "3",
                colours = UiServiceColours(
                    backgroundColour = 0x00333333,
                    textColour = 0x00444444
                )
            )
        )

        val result = toUiServiceNamesOrNull(
            serviceNames = setOf("2", "1", "3"),
            serviceColours = mapOf(
                "1" to ServiceColours(
                    primaryColour = 0x00111111,
                    colourOnPrimary = 0x00222222
                ),
                "3" to ServiceColours(
                    primaryColour = 0x00333333,
                    colourOnPrimary = 0x00444444
                )
            ),
            serviceNamesComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }
}