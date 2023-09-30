/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes

/**
 * This sealed interface and its descendants encapsulate the possible responses from requesting
 * live times.
 *
 * @author Niall Scott
 */
sealed interface LiveTimesResult {

    /**
     * The request is currently in progress.
     */
    object InProgress : LiveTimesResult

    /**
     * The response is successful.
     *
     * @property liveTimes The [LiveTimes].
     */
    data class Success(
        val liveTimes: LiveTimes) : LiveTimesResult

    /**
     * This sealed interface and its descendants encapsulate error responses from requesting live
     * times.
     */
    sealed interface Error : LiveTimesResult {

        /**
         * The time the error was received at.
         */
        val receiveTime: Long

        /**
         * This response was not successful due to no connectivity.
         *
         * @property receiveTime The time the error was received at.
         */
        data class NoConnectivity(
            override val receiveTime: Long) : Error

        /**
         * This response was not successful due to an Io error.
         *
         * @property receiveTime The time the error was received at.
         * @property throwable The [Throwable] which caused the error.
         */
        data class Io(
            override val receiveTime: Long,
            val throwable: Throwable) : Error

        /**
         * This sealed interface and its descendants encapsulate possible server errors from
         * requesting live times.
         */
        sealed interface ServerError : Error {

            /**
             * There was an authentication error against the server.
             *
             * @property receiveTime The time the error was received at.
             */
            data class Authentication(
                override val receiveTime: Long) : ServerError

            /**
             * The server is reporting it is down for maintenance.
             *
             * @property receiveTime The time the error was received at.
             */
            data class Maintenance(
                override val receiveTime: Long) : ServerError

            /**
             * The server is reporting it is currently overloaded.
             *
             * @property receiveTime The time the error was received at.
             */
            data class SystemOverloaded(
                override val receiveTime: Long) : ServerError

            /**
             * There was some other error which we don't handle.
             *
             * @property receiveTime The time the error was received at.
             */
            data class Other(
                override val receiveTime: Long) : ServerError
        }
    }
}