/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.updates

/**
 * This sealed interface encapsulates the result types when obtaining service updates.
 *
 * @author Niall Scott
 */
public sealed interface ServiceUpdatesResult {

    /**
     * The request is in progress.
     */
    public data object InProgress : ServiceUpdatesResult

    /**
     * The result is successful.
     *
     * @property serviceUpdates The service update data.
     * @property loadTimeMillis The time this data was loaded at, in milliseconds since the UNIX
     * epoch.
     */
    public data class Success(
        val serviceUpdates: List<ServiceUpdate>?,
        val loadTimeMillis: Long
    ) : ServiceUpdatesResult

    /**
     * This interface describes errors which can arise from getting service updates.
     */
    public sealed interface Error : ServiceUpdatesResult {

        /**
         * The time this data was loaded at, in milliseconds since the UNIX epoch.
         */
        public val loadTimeMillis: Long

        /**
         * The result is not successful due to no connectivity.
         *
         * @property loadTimeMillis The time this data was loaded at, in milliseconds since the
         * UNIX epoch.
         */
        public data class NoConnectivity(
            override val loadTimeMillis: Long
        ) : Error

        /**
         * The result is not successful due to an IO error.
         *
         * @property loadTimeMillis The time this data was loaded at, in milliseconds since the UNIX
         * epoch.
         * @property throwable The [Throwable] which caused this error.
         */
        public data class Io(
            override val loadTimeMillis: Long,
            val throwable: Throwable
        ) : Error

        /**
         * The result is not successful because of a server error.
         *
         * @property loadTimeMillis The time this data was loaded at, in milliseconds since the UNIX
         * epoch.
         */
        public data class Server(
            override val loadTimeMillis: Long
        ) : Error
    }
}