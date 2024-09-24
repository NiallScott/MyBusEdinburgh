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

package uk.org.rivernile.android.bustracker.ui.news

import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for `UiAffectedService.kt`.
 *
 * @author Niall Scott
 */
class UiAffectedServiceKtTest {

    @Test
    fun toUiAffectedServicesOrNullReturnsNullWhenServiceNamesIsNull() {
        val result = toUiAffectedServicesOrNull(
            serviceNames = null,
            serviceColours = null,
            serviceNamesComparator = naturalOrder()
        )

        assertNull(result)
    }

    @Test
    fun toUiAffectedServicesOrNullReturnsNullWhenServicesCollectionIsEmpty() {
        val result = toUiAffectedServicesOrNull(
            serviceNames = emptySet(),
            serviceColours = null,
            serviceNamesComparator = naturalOrder()
        )

        assertNull(result)
    }

    @Test
    fun toUiAffectedServicesOrNullReturnsServiceWithoutColourWhenServiceColoursIsNull() {
        val expected = listOf(
            UiAffectedService(
                serviceName = "1",
                backgroundColour = null,
                textColour = null
            )
        )

        val result = toUiAffectedServicesOrNull(
            serviceNames = setOf("1"),
            serviceColours = null,
            serviceNamesComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiAffectedServicesOrNullReturnsServiceWithoutColourWhenServiceColoursIsEmpty() {
        val expected = listOf(
            UiAffectedService(
                serviceName = "1",
                backgroundColour = null,
                textColour = null
            )
        )

        val result = toUiAffectedServicesOrNull(
            serviceNames = setOf("1"),
            serviceColours = emptyMap(),
            serviceNamesComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiAffectedServicesOrNullReturnsServiceWithoutColourWhenServiceColourNotKnown() {
        val expected = listOf(
            UiAffectedService(
                serviceName = "1",
                backgroundColour = null,
                textColour = null
            )
        )

        val result = toUiAffectedServicesOrNull(
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
    fun toUiAffectedServicesOrNullReturnsServiceWithColourWhenServiceColourKnown() {
        val expected = listOf(
            UiAffectedService(
                serviceName = "1",
                backgroundColour = 0x00111111,
                textColour = 0x00222222
            )
        )

        val result = toUiAffectedServicesOrNull(
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
    fun toUiAffectedServicesOrNullReturnsExpectedValuesWithRepresentativeExample() {
        val expected = listOf(
            UiAffectedService(
                serviceName = "1",
                backgroundColour = 0x00111111,
                textColour = 0x00222222
            ),
            UiAffectedService(
                serviceName = "2",
                backgroundColour = null,
                textColour = null
            ),
            UiAffectedService(
                serviceName = "3",
                backgroundColour = 0x00333333,
                textColour = 0x00444444
            )
        )

        val result = toUiAffectedServicesOrNull(
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