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

package uk.org.rivernile.android.bustracker.ui.alerts;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * This class represents bus stop information displayed for an alert.
 *
 * @author Niall Scott
 */
class BusStop {

    private final String stopCode;
    private final String stopName;
    private final double latitude;
    private final double longitude;
    private final int orientation;
    private final String locality;

    /**
     * Create a new {@code BusStop}.
     *
     * @param stopCode The stop code.
     * @param stopName The name of the stop.
     * @param latitude The latitude of the stop.
     * @param longitude The longitude of the stop.
     * @param orientation The orientation of the stop.
     * @param locality The locality of the stop.
     */
    BusStop(@NonNull final String stopCode, @Nullable final String stopName, final double latitude,
            final double longitude, final int orientation, @Nullable final String locality) {
        if (TextUtils.isEmpty(stopCode)) {
            throw new IllegalArgumentException("stopCode must not be null or empty.");
        }

        this.stopCode = stopCode;
        this.stopName = stopName;
        this.latitude = latitude;
        this.longitude = longitude;
        this.orientation = orientation;
        this.locality = locality;
    }

    /**
     * Get the stop code.
     *
     * @return The stop code.
     */
    @NonNull
    String getStopCode() {
        return stopCode;
    }

    /**
     * Get the stop name.
     *
     * @return The stop name.
     */
    @Nullable
    String getStopName() {
        return stopName;
    }

    /**
     * Get the latitude.
     *
     * @return The latitude.
     */
    double getLatitude() {
        return latitude;
    }

    /**
     * Get the longitude.
     *
     * @return The longitude.
     */
    double getLongitude() {
        return longitude;
    }

    /**
     * Get the orientation.
     *
     * @return The orientation.
     */
    int getOrientation() {
        return orientation;
    }

    /**
     * Get the locality.
     *
     * @return The locality.
     */
    @Nullable
    public String getLocality() {
        return locality;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }


        final BusStop busStop = (BusStop) o;

        return stopCode.equals(busStop.stopCode);
    }

    @Override
    public int hashCode() {
        return stopCode.hashCode();
    }
}
