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

package uk.org.rivernile.android.bustracker.core.location

/**
 * This represents a state which can emitted while requesting location updates.
 *
 * @author Niall Scott
 */
public sealed interface LocationUpdate {

    /**
     * No location is yet known so we are now awaiting on location updates.
     */
    public data object AwaitingLocation : LocationUpdate

    /**
     * A location update is available.
     *
     * @property location The location that this update represents.
     */
    @JvmInline
    public value class Update(
        public val location: Location
    ) : LocationUpdate

    /**
     * There was an error while trying to receive location updates. See the subtypes for more
     * information.
     */
    public sealed interface Error : LocationUpdate {

        /**
         * The device does not have location features.
         */
        public data object NoLocationFeature : Error

        /**
         * The permissions granted to this application are not sufficient to receive location
         * updates.
         */
        public data object InsufficientLocationPermissions : Error

        /**
         * Location services are currently turned off on this device.
         */
        public data object LocationOff : Error
    }
}
