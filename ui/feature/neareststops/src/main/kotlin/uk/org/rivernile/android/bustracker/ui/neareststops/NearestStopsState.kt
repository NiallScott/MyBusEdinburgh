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

package uk.org.rivernile.android.bustracker.ui.neareststops

/**
 * This encapsulates the possible states as a result of attempting to load nearest stops.
 *
 * @author Niall Scott
 */
internal sealed interface NearestStopsState {

    /**
     * These are the stops available for the current location of the device.
     *
     * @property stops The stops available for the current location of the device.
     */
    data class Stops(
        val stops: List<NearestStop>?
    ) : NearestStopsState

    /**
     * There was an error.
     */
    sealed interface Error : NearestStopsState {

        /**
         * No location feature is available on the device.
         */
        data object NoLocationFeature : Error

        /**
         * The app has insufficient location permissions to obtain the device location.
         */
        data object InsufficientLocationPermissions : Error

        /**
         * Location services have been disabled on the device.
         */
        data object LocationOff : Error

        /**
         * It is not currently possible to obtain a location for this device.
         */
        data object LocationUnknown : Error
    }
}
