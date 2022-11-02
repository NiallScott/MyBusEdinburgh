/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.services

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever

/**
 * Tests for [EdinburghServiceColourOverride].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class EdinburghServiceColourOverrideTest {

    companion object {

        private const val COLOUR_NIGHT_SERVICE = 0x000000
    }

    @Mock
    private lateinit var serviceColourProvider: ServiceColourProvider

    private lateinit var colourOverride: EdinburghServiceColourOverride

    @Before
    fun setUp() {
        colourOverride = EdinburghServiceColourOverride(serviceColourProvider)
    }

    @Test
    fun overrideServiceColoursReturnsNullWhenServicesIsNullAndServicesColoursIsNull() {
        val result = colourOverride.overrideServiceColours(null, null)

        assertNull(result)
    }

    @Test
    fun overrideServiceColoursReturnsNullWhenServicesIsEmptyAndServiceColoursIsNull() {
        val result = colourOverride.overrideServiceColours(emptySet(), null)

        assertNull(result)
    }

    @Test
    fun overrideServiceColoursReturnsServiceColoursWhenServicesIsNullAndServiceColoursIsNotNull() {
        val serviceColours = mapOf(
                "1" to 0x000000,
                "2" to 0xFFFFFF)

        val result = colourOverride.overrideServiceColours(null, serviceColours)

        assertEquals(serviceColours, result)
    }

    @Test
    fun overrideServiceColoursReturnsServiceColoursWhenServicesIsEmptyAndServiceColoursIsNotNull() {
        val serviceColours = mapOf(
                "1" to 0x000000,
                "2" to 0xFFFFFF)

        val result = colourOverride.overrideServiceColours(emptySet(), serviceColours)

        assertEquals(serviceColours, result)
    }

    @Test
    fun overrideServiceColoursReturnsNullWhenServicesIsPopulatedAndServiceColoursIsEmpty() {
        val result = colourOverride.overrideServiceColours(setOf("1", "2"), emptyMap())

        assertNull(result)
    }

    @Test
    fun overrideServiceColoursReturnsInputMapWhenNoNightServices() {
        val serviceColours = mapOf(
                "1" to 0x000000,
                "2" to 0xFFFFFF,
                "3" to 0x0000FF)

        val result = colourOverride.overrideServiceColours(setOf("1", "2", "3", "4"),
                serviceColours)

        assertEquals(serviceColours, result)
    }

    @Test
    fun overrideServiceColoursDoesNotOverwriteNightServiceColourFromInput() {
        val serviceColours = mapOf(
                "1" to 0x0000FF,
                "2" to 0xFFFFFF,
                "N3" to 0x00FF00)

        val result = colourOverride.overrideServiceColours(setOf("1", "2", "N3"),
                serviceColours)

        assertEquals(serviceColours, result)
    }

    @Test
    fun overrideServiceColoursInsertsMissingNightServicesWhenInputIsNull() {
        givenServiceColourProviderReturnsColourForNightService()
        val serviceColours = mapOf(
                "N1" to COLOUR_NIGHT_SERVICE,
                "N2" to COLOUR_NIGHT_SERVICE,
                "N3" to COLOUR_NIGHT_SERVICE)

        val result = colourOverride.overrideServiceColours(setOf("N1", "N2", "N3"), null)

        assertEquals(serviceColours, result)
    }

    @Test
    fun overrideServiceColoursInsertsMissingNightServicesWhenInputIsEmpty() {
        givenServiceColourProviderReturnsColourForNightService()
        val serviceColours = mapOf(
                "N1" to COLOUR_NIGHT_SERVICE,
                "N2" to COLOUR_NIGHT_SERVICE,
                "N3" to COLOUR_NIGHT_SERVICE)

        val result = colourOverride.overrideServiceColours(setOf("N1", "N2", "N3"), emptyMap())

        assertEquals(serviceColours, result)
    }

    @Test
    fun overrideServiceColoursInsertsMissingNightServiceCaseInsensitive() {
        givenServiceColourProviderReturnsColourForNightService()
        val serviceColours = mapOf(
                "n1" to COLOUR_NIGHT_SERVICE)

        val result = colourOverride.overrideServiceColours(setOf("n1"), null)

        assertEquals(serviceColours, result)
    }

    @Test
    fun overrideServiceColoursInsertsMissingNightServiceWithTrailingCharacters() {
        givenServiceColourProviderReturnsColourForNightService()
        val serviceColours = mapOf(
                "n1abc" to COLOUR_NIGHT_SERVICE)

        val result = colourOverride.overrideServiceColours(setOf("n1abc"), null)

        assertEquals(serviceColours, result)
    }

    @Test
    fun overrideServiceColoursDoesNotAddNonNightService() {
        val result = colourOverride.overrideServiceColours(setOf("1"), null)

        assertNull(result)
    }

    @Test
    fun overrideServiceColoursBehavesCorrectlyWithRepresentativeExample() {
        givenServiceColourProviderReturnsColourForNightService()
        val serviceColours = mapOf(
                "1" to 0x0000FF,
                "2" to 0xFF0000,
                "N3" to 0x00FF00)
        val expected = mapOf(
                "1" to 0x0000FF,
                "2" to 0xFF0000,
                "N3" to 0x00FF00,
                "N4" to COLOUR_NIGHT_SERVICE)

        val result = colourOverride.overrideServiceColours(setOf("1", "2", "N3", "N4"),
                serviceColours)

        assertEquals(expected, result)
    }

    private fun givenServiceColourProviderReturnsColourForNightService() {
        whenever(serviceColourProvider.getNightServiceColour())
                .thenReturn(COLOUR_NIGHT_SERVICE)
    }
}