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

package uk.org.rivernile.android.bustracker.core.database.settings

import uk.org.rivernile.android.bustracker.core.domain.toAtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * Tests for [RoomSettingsTypeConverters].
 *
 * @author Niall Scott
 */
class RoomSettingsTypeConvertersTest {

    @Test
    fun convertFromMillisToInstantConvertsMillisecondsValueToInstant() {
        val result = RoomSettingsTypeConverters().convertFromMillisToInstant(123456L)

        assertEquals(Instant.fromEpochMilliseconds(123456L), result)
    }

    @Test
    fun convertFromInstantToMillisConvertsInstantToMillisecondsValue() {
        val result = RoomSettingsTypeConverters()
            .convertFromInstantToMillis(Instant.fromEpochMilliseconds(123456L))

        assertEquals(123456L, result)
    }

    @Test
    fun convertFromStopCodeStringToStopIdentifierConvertsFromStringValueToNaptanStopIdentifier() {
        val result = RoomSettingsTypeConverters()
            .convertFromStopCodeStringToStopIdentifier("123456")

        assertEquals("123456".toNaptanStopIdentifier(), result)
    }

    @Test(expected = UnsupportedOperationException::class)
    fun convertFromStopIdentifierToStopCodeStringThrowsExceptionWhenIdentifierIsAtcoCode() {
        RoomSettingsTypeConverters()
            .convertFromStopIdentifierToStopCodeString("123456".toAtcoStopIdentifier())
    }

    @Test
    fun convertFromStopIdentifierToStopCodeStringConvertsToStringWhenIdentifierIsNaptanCode() {
        val result = RoomSettingsTypeConverters()
            .convertFromStopIdentifierToStopCodeString("123456".toNaptanStopIdentifier())

        assertEquals("123456", result)
    }
}
