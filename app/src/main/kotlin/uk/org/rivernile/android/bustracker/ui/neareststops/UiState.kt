/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops

/**
 * This sealed interface encapsulates the possible UI states.
 *
 * @author Niall Scott
 */
sealed interface UiState {

    /**
     * The success item. The [nearestStops] item will be non-empty.
     *
     * @property nearestStops The nearest stop items.
     */
    data class Success(
            val nearestStops: List<UiNearestStop>) : UiState

    /**
     * The progress item.
     */
    object InProgress : UiState

    /**
     * This sealed interface encapsulates the possible error types.
     */
    sealed interface Error : UiState {

        /**
         * Should the error show a resolve button?
         */
        val showResolveButton: Boolean

        /**
         * This state is emitted when the device is not capable of obtaining the location. This is
         * an unrecoverable error.
         */
        object NoLocationFeature : Error {

            override val showResolveButton get() = false
        }

        /**
         * This state is emitted when we have insufficient permission from the user to obtain
         * locations.
         */
        object InsufficientLocationPermissions : Error {

            override val showResolveButton get() = true
        }

        /**
         * This state is emitted when the system location provider is unavailable.
         */
        object LocationOff : Error {

            override val showResolveButton get() = true
        }

        /**
         * This state is emitted when the location is unknown.
         */
        object LocationUnknown : Error {

            override val showResolveButton get() = false
        }

        /**
         * This state is emitted when there are no known nearest stops.
         */
        object NoNearestStops : Error {

            override val showResolveButton get() = false
        }
    }
}