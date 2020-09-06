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

package uk.org.rivernile.android.bustracker.core.livetimes

import uk.org.rivernile.android.bustracker.core.endpoints.tracker.TrackerException

/**
 * This class encapsulates a result from the [LiveTimesRepository].
 *
 * @param T The type of data returned in the success condition.
 * @author Niall Scott
 */
sealed class Result<out T> {

    /**
     * This represents a request currently in progress.
     */
    object InProgress : Result<Nothing>()

    /**
     * This represents a request which was successful.
     *
     * @param T The type of data returned.
     * @property result The success data.
     */
    data class Success<out T>(val result: T) : Result<T>()

    /**
     * This represents a request which failed.
     *
     * @property receiveTime The time, in milliseconds since UNIX epoch, the error was received at.
     * @property exception The [TrackerException] which caused the failure.
     */
    data class Error(
            val receiveTime: Long,
            val exception: TrackerException) : Result<Nothing>()
}