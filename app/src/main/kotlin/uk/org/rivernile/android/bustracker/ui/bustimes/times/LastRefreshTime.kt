/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

/**
 * This class enumerates the different types of display of last refresh time.
 *
 * @author Niall Scott
 */
sealed class LastRefreshTime {

    /**
     * There is not a last refresh time.
     */
    object Never : LastRefreshTime()

    /**
     * The last refresh occurred within the last minute.
     */
    object Now : LastRefreshTime()

    /**
     * The last refresh occurred within these number of minutes.
     *
     * @property minutes The number of minutes since the last refresh.
     */
    data class Minutes(val minutes: Int) : LastRefreshTime()

    /**
     * The last refresh occurred more than one hour ago.
     */
    object MoreThanOneHour : LastRefreshTime()
}