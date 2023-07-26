/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

    @Mock
    private lateinit var serviceColourProvider: ServiceColourProvider

    private lateinit var colourOverride: EdinburghServiceColourOverride

    @Before
    fun setUp() {
        colourOverride = EdinburghServiceColourOverride(serviceColourProvider)
    }

    @Test
    fun overrideServiceColourReturnsCurrentColourWhenServiceIsNumeric() {
        val result = colourOverride.overrideServiceColour("1", 1)

        assertEquals(1, result)
    }

    @Test
    fun overrideServiceColourReturnsCurrentColourWhenServiceIsTextButNotNightService() {
        val result = colourOverride.overrideServiceColour("abc123", 1)

        assertEquals(1, result)
    }

    @Test
    fun overrideServiceColourReturnsNightServiceColourWhenServiceIsNightServiceLowercase() {
        whenever(serviceColourProvider.nightServiceColour)
            .thenReturn(2)

        val result = colourOverride.overrideServiceColour("n25", 1)

        assertEquals(2, result)
    }

    @Test
    fun overrideServiceColourReturnsNightServiceColourWhenServiceIsNightServiceUppercase() {
        whenever(serviceColourProvider.nightServiceColour)
            .thenReturn(2)

        val result = colourOverride.overrideServiceColour("N25", 1)

        assertEquals(2, result)
    }
}