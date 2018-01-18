/*
 * Copyright (C) 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap;

import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.ClusterItem;

/**
 * This model class represents a stop on the map. It implements the {@link ClusterItem} interface
 * to allow it to be clustered.
 *
 * @author Niall Scott
 */
class Stop implements ClusterItem {

    private final LatLng position;
    private final String title;
    private final String stopCode;
    private final int orientation;

    /**
     * Create a new {@code Stop}.
     *
     * @param position The position of the stop, as represented by a Google Map {@link LatLng}
     * instance.
     * @param title The title of the stop.
     * @param stopCode The stop code.
     * @param orientation The orientation of the stop.
     */
    Stop(final LatLng position, final String title, final String stopCode, final int orientation) {
        this.position = position;
        this.title = title;
        this.stopCode = stopCode;
        this.orientation = orientation;
    }

    @Override
    public LatLng getPosition() {
        return position;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getSnippet() {
        return null;
    }

    /**
     * Get the stop code.
     *
     * @return The stop code.
     */
    String getStopCode() {
        return stopCode;
    }

    /**
     * Get the orientation.
     *
     * @return The orientation.
     */
    int getOrientation() {
        return orientation;
    }
}
