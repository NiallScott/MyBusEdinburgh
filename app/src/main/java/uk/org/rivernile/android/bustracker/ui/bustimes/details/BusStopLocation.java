/*
 * Copyright (C) 2016 Niall 'Rivernile' Scott
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
 */

package uk.org.rivernile.android.bustracker.ui.bustimes.details;

/**
 * This class encapsulates a bus stop location for {@link StopDetailsFragment}.
 *
 * @author Niall Scott
 */
class BusStopLocation {

    private final double latitude;
    private final double longitude;
    private final int orientation;

    /**
     * Create a new {@code BusStopLocation}.
     *
     * @param latitude The bus stop latitude.
     * @param longitude The bus stop longitude.
     * @param orientation The bus stop orientation.
     */
    BusStopLocation(final double latitude, final double longitude, final int orientation) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.orientation = orientation;
    }

    /**
     * Get the bus stop latitude.
     *
     * @return The bus stop latitude.
     */
    double getLatitude() {
        return latitude;
    }

    /**
     * Get the bus stop longitude.
     *
     * @return The bus stop longitude.
     */
    double getLongitude() {
        return longitude;
    }

    /**
     * Get the bus stop orientation.
     *
     * @return The bus stop orientation.
     */
    int getOrientation() {
        return orientation;
    }
}
