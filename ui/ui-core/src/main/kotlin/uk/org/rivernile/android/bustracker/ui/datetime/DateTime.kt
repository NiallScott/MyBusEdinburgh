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

package uk.org.rivernile.android.bustracker.ui.datetime

import androidx.compose.runtime.Composable
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.text.DateFormat
import java.text.SimpleDateFormat

/**
 * A [androidx.compose.runtime.CompositionLocal] which provides a [DateFormat] for formatting
 * timestamps in to date and time format, suitable for displaying on user interfaces.
 *
 * This is not provided by default in the theme. Many pieces of UI will not require this, so the
 * allocation of a [DateFormat] object will be wasteful. Therefore, any pieces of UI which want to
 * use this must use a [androidx.compose.runtime.CompositionLocalProvider] and supply an appropriate
 * instance of [DateFormat].
 *
 * For example, to have a [DateFormat] which responds to device configuration changes, use
 * [rememberDateTimeFormatter].
 *
 * If this [androidx.compose.runtime.CompositionLocal] is used without a value being provided, an
 * [IllegalStateException] will be thrown at the usage site.
 *
 * @see [rememberDateTimeFormatter]
 * @author Niall Scott
 */
val LocalDateTimeFormatter = compositionLocalOf<DateFormat> {
    error("LocalDateTimeFormatter has not been set with a value.")
}

/**
 * This [remember]s a [DateFormat] instance which is reinitialised every time there is a change
 * in [LocalConfiguration]. This is because the [LocalConfiguration] may have a locale change which
 * may mean the date and time format has changed.
 *
 * @return A [DateFormat] instance suitable for formatting date/time strings.
 * @author Niall Scott
 */
@Composable
fun rememberDateTimeFormatter(): DateFormat = remember(LocalConfiguration.current) {
    SimpleDateFormat.getDateTimeInstance()
}