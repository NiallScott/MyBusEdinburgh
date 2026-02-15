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
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ProvidableCompositionLocal
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.compositionLocalWithComputedDefaultOf
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import dagger.hilt.android.qualifiers.ApplicationContext
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.ui.textformatting.R
import javax.inject.Inject

/**
 * This class contains methods which provide common ways for formatting stop names in the app.
 *
 * @author Niall Scott
 */
public interface StopNameFormatter {

    /**
     * Format to a bus stop name [String] without including the stop code.
     *
     * @param stopName The stop name data.
     * @return The formatted stop name, excluding the stop code.
     */
    public fun formatBusStopName(stopName: UiStopName): String

    /**
     * Format to a bus stop name [String] containing the stop code.
     *
     * @param stopIdentifier The stop identifier.
     * @param stopName The stop name data.
     * @return The formatted stop name, containing the stop code.
     */
    public fun formatBusStopNameWithStopIdentifier(
        stopIdentifier: StopIdentifier,
        stopName: UiStopName?
    ): String
}

/**
 * A [ProvidableCompositionLocal] which provides the [StopNameFormatter] to use for formatting stop
 * names. By default, this will provide the real run-time instance. This can be replaced at testing
 * time by providing a new [ProvidableCompositionLocal].
 */
public val LocalStopNameFormatter: ProvidableCompositionLocal<StopNameFormatter> =
    compositionLocalWithComputedDefaultOf {
        RealStopNameFormatter(
            context = LocalContext.currentValue.applicationContext
        )
    }

/**
 * A convenience [Composable] which formats the supplied [stopName] using
 * [StopNameFormatter.formatBusStopName].
 *
 * @param stopName See [StopNameFormatter.formatBusStopName].
 * @return See [StopNameFormatter.formatBusStopName].
 * @see StopNameFormatter.formatBusStopName
 */
@Composable
@ReadOnlyComposable
public fun formatBusStopName(stopName: UiStopName): String {
    // Read LocalConfiguration so that this Composable is recomposed when it changes.
    LocalConfiguration.current
    return LocalStopNameFormatter.current.formatBusStopName(stopName = stopName)
}

/**
 * A convenience [Composable] which formats the supplied [stopIdentifier] and [stopName] using
 * [StopNameFormatter.formatBusStopNameWithStopIdentifier].
 *
 * @param stopIdentifier See [StopNameFormatter.formatBusStopNameWithStopIdentifier].
 * @param stopName See [StopNameFormatter.formatBusStopNameWithStopIdentifier].
 * @return See [StopNameFormatter.formatBusStopNameWithStopIdentifier].
 * @see StopNameFormatter.formatBusStopNameWithStopIdentifier
 */
@Composable
@ReadOnlyComposable
public fun formatBusStopNameWithStopIdentifier(
    stopIdentifier: StopIdentifier,
    stopName: UiStopName?
): String {
    // Read LocalConfiguration so that this Composable is recomposed when it changes.
    LocalConfiguration.current
    return LocalStopNameFormatter.current.formatBusStopNameWithStopIdentifier(
        stopIdentifier = stopIdentifier,
        stopName = stopName
    )
}

internal class RealStopNameFormatter @Inject constructor(
    @param:ApplicationContext private val context: Context
) : StopNameFormatter {

    override fun formatBusStopName(stopName: UiStopName): String {
        return stopName
            .locality
            ?.ifEmpty { null }
            ?.let {
                context.getString(R.string.busstop_name_only_with_locality, stopName.name, it)
            }
            ?: stopName.name
    }

    override fun formatBusStopNameWithStopIdentifier(
        stopIdentifier: StopIdentifier,
        stopName: UiStopName?
    ): String {
        return stopName
            ?.let {
                it.locality
                    ?.ifEmpty { null }
                    ?.let { locality ->
                        context.getString(
                            R.string.busstop_locality,
                            it.name,
                            locality,
                            stopIdentifier.toHumanReadableString()
                        )
                    }
                    ?: context.getString(
                        R.string.busstop,
                        it.name,
                        stopIdentifier.toHumanReadableString()
                    )
            }
            ?: stopIdentifier.toHumanReadableString()
    }
}
