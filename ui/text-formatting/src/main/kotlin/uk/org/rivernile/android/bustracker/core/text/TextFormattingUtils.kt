/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.busstops.StopName
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.ui.textformatting.R
import javax.inject.Inject

/**
 * This class contains methods which provide common ways for formatting text in the app.
 *
 * @param context The application [Context].
 * @author Niall Scott
 */
class TextFormattingUtils @Inject internal constructor(
    private val context: Context) {

    /**
     * Format a bus stop name [String] without including the stop code.
     *
     * @param stopName The stop name data.
     * @return The formatted stop name, excluding the stop code.
     */
    fun formatBusStopName(stopName: StopName) =
        stopName
            .locality
            ?.ifEmpty { null }
            ?.let {
                context.getString(R.string.busstop_name_only_with_locality, stopName.name, it)
            } ?: stopName.name

    /**
     * Format a bus stop name [String] containing the stop code.
     *
     * @param stopIdentifier The stop identifier.
     * @param stopName The stop name data.
     * @return The formatted stop name, containing the stop code.
     */
    fun formatBusStopNameWithStopCode(stopIdentifier: StopIdentifier, stopName: StopName?) =
        stopName?.let {
            it.locality
                ?.ifEmpty { null }
                ?.let { locality ->
                    context.getString(
                        R.string.busstop_locality,
                        it.name,
                        locality,
                        stopIdentifier.toHumanReadableString()
                    )
                } ?: run {
                    context.getString(
                        R.string.busstop,
                        it.name,
                        stopIdentifier.toHumanReadableString()
                    )
                }
        } ?: stopIdentifier.toHumanReadableString()
}
