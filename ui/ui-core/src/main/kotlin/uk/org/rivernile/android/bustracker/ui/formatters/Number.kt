/*
 * Copyright (C) 2025 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.formatters

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import java.text.NumberFormat

/**
 * A [androidx.compose.runtime.CompositionLocal] which provides a [NumberFormat] for formatting
 * numbers, suitable for displaying on user interfaces. This value will be updated when
 * [LocalConfiguration] changes.
 *
 * For better performance, you may wish to provide this [ProvidableCompositionLocal] with
 * [rememberNumberFormatter] so that the [NumberFormat] instance is not created on every usage.
 *
 * @see rememberNumberFormatter
 * @author Niall Scott
 */
public val LocalNumberFormatter: ProvidableCompositionLocal<NumberFormat> =
    compositionLocalWithComputedDefaultOf {
        LocalConfiguration.currentValue
        NumberFormat.getInstance()
    }

/**
 * This [remember]s a [NumberFormat] instance which is reinitialised every time there is a change
 * in [LocalConfiguration]. This is because the [LocalConfiguration] may have a locale change which
 * may mean the number format has changed.
 *
 * @return A [NumberFormat] instance suitable for formatting numbers.
 * @author Niall Scott
 */
@Composable
public fun rememberNumberFormatter(): NumberFormat = remember(LocalConfiguration.current) {
    NumberFormat.getInstance()
}
