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

package uk.org.rivernile.android.bustracker.core.text

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import uk.org.rivernile.android.bustracker.ui.textformatting.R
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [RealStopNameFormatter].
 *
 * @author Niall Scott
 */
class RealStopNameFormatterTest {

    @Test
    fun formatBusStopNameOnlyUsesStopNameWhenLocalityIsNull() {
        val formatter = createStopNameFormatter()
        val stopName = UiStopName(
            name = "Test1",
            locality = null
        )

        val result = formatter.formatBusStopName(stopName)

        assertEquals("Test1", result)
    }

    @Test
    fun formatBusStopNameOnlyUsesStopNameWhenLocalityIsEmpty() {
        val formatter = createStopNameFormatter()
        val stopName = UiStopName(
            name = "Test1",
            locality = ""
        )

        val result = formatter.formatBusStopName(stopName)

        assertEquals("Test1", result)
    }

    @Test
    fun formatBusStopNameUsesStopNameAndLocalityWhenLocalityIsAvailable() {
        val formatter = createStopNameFormatter()
        val stopName = UiStopName(
            name = "Test1",
            locality = "Some Place"
        )

        val result = formatter.formatBusStopName(stopName)

        assertEquals(
            applicationContext.getString(
                R.string.busstop_name_only_with_locality,
                "Test1",
                "Some Place"
            ),
            result
        )
    }

    @Test
    fun formatBusStopNameWithStopCodeOnlyUsesStopCodeWhenNameIsNull() {
        val formatter = createStopNameFormatter()

        val result = formatter.formatBusStopNameWithStopCode(
            stopCode = "123456",
            stopName = null
        )

        assertEquals("123456", result)
    }

    @Test
    fun formatBusStopNameWithStopCodeUsesStopNameAndCodeWhenLocalityIsNull() {
        val formatter = createStopNameFormatter()
        val stopName = UiStopName(
            name = "Test1",
            locality = null
        )

        val result = formatter.formatBusStopNameWithStopCode("123456", stopName)

        assertEquals(
            applicationContext.getString(
                R.string.busstop,
                "Test1",
                "123456"
            ),
            result
        )
    }

    @Test
    fun formatBusStopNameWithStopCodeUsesStopNameAndCodeWhenLocalityIsEmpty() {
        val formatter = createStopNameFormatter()
        val stopName = UiStopName(
            name = "Test1",
            locality = ""
        )

        val result = formatter.formatBusStopNameWithStopCode("123456", stopName)

        assertEquals(
            applicationContext.getString(
                R.string.busstop,
                "Test1",
                "123456"
            ),
            result
        )
    }

    @Test
    fun formatBusStopNameWithStopCodeUsesStopNameAndLocalityAndCodeWhenLocalityIsAvailable() {
        val formatter = createStopNameFormatter()
        val stopName = UiStopName(
            name = "Test1",
            locality = "Some Place"
        )

        val result = formatter.formatBusStopNameWithStopCode("123456", stopName)

        assertEquals(
            applicationContext.getString(
                R.string.busstop_locality,
                "Test1",
                "Some Place",
                "123456"
            ),
            result
        )
    }

    private fun createStopNameFormatter(): RealStopNameFormatter {
        return RealStopNameFormatter(
            context = applicationContext
        )
    }

    private val applicationContext get() = ApplicationProvider.getApplicationContext<Context>()
}
