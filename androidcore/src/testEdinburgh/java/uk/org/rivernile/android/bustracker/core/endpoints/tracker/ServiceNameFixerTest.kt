/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.junit.MockitoJUnitRunner

/**
 * Tests for [ServiceNameFixer].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class ServiceNameFixerTest {

    companion object {

        private const val SERVICE_TRAM = "TRAM"
    }

    private lateinit var serviceNameFixer: ServiceNameFixer

    @Before
    fun setUp() {
        serviceNameFixer = ServiceNameFixer()
    }

    @Test
    fun correctServiceNameWithNullServiceNameReturnsNull() {
        val result = serviceNameFixer.correctServiceName(null)

        assertNull(result)
    }

    @Test
    fun correctServiceNameWithRandomServiceNameReturnsServiceName() {
        val result = serviceNameFixer.correctServiceName("Random")

        assertEquals("Random", result)
    }

    @Test
    fun correctServiceNameWithService50ReturnsTram() {
        val result = serviceNameFixer.correctServiceName("50")

        assertEquals(SERVICE_TRAM, result)
    }

    @Test
    fun correctServiceNameWithServiceT50ReturnsTram() {
        val result = serviceNameFixer.correctServiceName("T50")

        assertEquals(SERVICE_TRAM, result)
    }
}