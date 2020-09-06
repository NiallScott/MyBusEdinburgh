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
 * This class encapsulates the result of transforming a loading or loaded [UiResult]. This is
 * required because the data goes through transformation for display purposes before being exposed
 * to the UI.
 *
 * @author Niall Scott
 */
sealed class UiTransformedResult {

    /**
     * This represents a request currently in progress.
     */
    object InProgress : UiTransformedResult()

    /**
     * This represents a successfully completed request.
     *
     * @param receiveTime The time the data was received on the device at, for the purposes of
     * working out the age of the data.
     * @param items The items (live departures) to display.
     */
    data class Success(
            val receiveTime: Long,
            val items: List<UiLiveTimesItem>) : UiTransformedResult()

    /**
     * This represents a request which completed with an error.
     *
     * @param receiveTime The time the data was received on the device at, for the purposes of
     * working out the age of the data.
     * @param error The error to display to the user.
     */
    data class Error(
            val receiveTime: Long,
            val error: ErrorType) : UiTransformedResult()
}