/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.domain

import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `ParcelableStopIdentifier.kt`.
 *
 * @author Niall Scott
 */
class ParcelableStopIdentifierKtTest {

    @Test
    fun toStopIdentifierMapsToAtcoStopIdentifierWhenParcelableAtcoStopIdentifier() {
        val result = (ParcelableAtcoStopIdentifier("123456") as ParcelableStopIdentifier)
            .toStopIdentifier()

        assertEquals("123456".toAtcoStopIdentifier(), result)
    }

    @Test
    fun toStopIdentifierMapsToNaptanStopIdentifierWhenParcelableNaptanStopIdentifier() {
        val result = (ParcelableNaptanStopIdentifier("123456") as ParcelableStopIdentifier)
            .toStopIdentifier()

        assertEquals("123456".toNaptanStopIdentifier(), result)
    }

    @Test
    fun toAtcoStopIdentifierMapsToAtcoStopIdentifier() {
        val result = ParcelableAtcoStopIdentifier("123456").toAtcoStopIdentifier()

        assertEquals("123456".toAtcoStopIdentifier(), result)
    }

    @Test
    fun toNaptanStopIdentifierMapsToNaptanStopIdentifier() {
        val result = ParcelableNaptanStopIdentifier("123456").toNaptanStopIdentifier()

        assertEquals("123456".toNaptanStopIdentifier(), result)
    }

    @Test
    fun toParcelableAtcoStopIdentifierMapsToParcelableAtcoStopIdentifier() {
        val result = "123456".toParcelableAtcoStopIdentifier()

        assertEquals(
            ParcelableAtcoStopIdentifier("123456"),
            result
        )
    }

    @Test
    fun toParcelableNaptanStopIdentifierMapsToParcelableNaptanStopIdentifier() {
        val result = "123456".toParcelableNaptanStopIdentifier()

        assertEquals(
            ParcelableNaptanStopIdentifier("123456"),
            result
        )
    }

    @Test
    fun toParcelableStopIdentifierMapsToParcelableAtcoStopIdentifier() {
        val result = (AtcoStopIdentifier("123456") as StopIdentifier)
            .toParcelableStopIdentifier()

        assertEquals("123456".toParcelableAtcoStopIdentifier(), result)
    }

    @Test
    fun toParcelableStopIdentifierMapsToParcelableNaptanStopIdentifier() {
        val result = (NaptanStopIdentifier("123456") as StopIdentifier)
            .toParcelableStopIdentifier()

        assertEquals("123456".toParcelableNaptanStopIdentifier(), result)
    }

    @Test
    fun toParcelableAtcoStopIdentifierAsIdentifierMapsToParcelableAtcoStopIdentifier() {
        val result = AtcoStopIdentifier("123456").toParcelableAtcoStopIdentifier()

        assertEquals("123456".toParcelableAtcoStopIdentifier(), result)
    }

    @Test
    fun toParcelableNaptanStopIdentifierAsIdentifierMapsToParcelableNaptanStopIdentifier() {
        val result = NaptanStopIdentifier("123456").toParcelableNaptanStopIdentifier()

        assertEquals("123456".toParcelableNaptanStopIdentifier(), result)
    }
}
