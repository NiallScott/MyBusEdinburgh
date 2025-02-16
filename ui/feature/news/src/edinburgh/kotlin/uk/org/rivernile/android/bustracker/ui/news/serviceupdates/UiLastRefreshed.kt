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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import androidx.compose.runtime.Immutable
import uk.org.rivernile.android.bustracker.core.time.ElapsedTimeMinutes

/**
 * This class enumerates the different displays for the last refresh time.
 *
 * @author Niall Scott
 */
internal sealed interface UiLastRefreshed {

    /**
     * There is no last refresh time. This is an unusual state, designed to encapsulate the case
     * that the current time is prior to the supplied event time.
     */
    @Immutable
    data object Never : UiLastRefreshed

    /**
     * The last refresh time is less than 1 minute ago.
     */
    @Immutable
    data object Now : UiLastRefreshed

    /**
     * The last refresh time is these number of minutes.
     *
     * @property minutes The number of minutes since the last refresh.
     */
    @Immutable
    data class Minutes(
        val minutes: Int
    ) : UiLastRefreshed

    /**
     * The last refresh time is more than one hour ago.
     */
    @Immutable
    data object MoreThanOneHour : UiLastRefreshed
}

/**
 * Map an [ElapsedTimeMinutes] to an [UiLastRefreshed].
 *
 * @return This [ElapsedTimeMinutes] as an [UiLastRefreshed].
 */
internal fun ElapsedTimeMinutes.toUiLastRefreshed(): UiLastRefreshed {
    return when (this) {
        is ElapsedTimeMinutes.None -> UiLastRefreshed.Never
        is ElapsedTimeMinutes.Now -> UiLastRefreshed.Now
        is ElapsedTimeMinutes.Minutes -> UiLastRefreshed.Minutes(minutes = minutes)
        is ElapsedTimeMinutes.MoreThanOneHour ->  UiLastRefreshed.MoreThanOneHour
    }
}