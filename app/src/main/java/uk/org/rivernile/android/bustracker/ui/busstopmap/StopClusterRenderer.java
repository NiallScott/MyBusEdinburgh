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

import android.content.Context;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import uk.org.rivernile.android.utils.MapsUtils;

/**
 * This is a subclassed {@link DefaultClusterRenderer} to allow us to customise the presentation of
 * the {@link Marker} before it is displayed on the map.
 *
 * @author Niall Scott
 * @see DefaultClusterRenderer
 */
class StopClusterRenderer extends DefaultClusterRenderer<Stop> {

    /**
     * See {@link DefaultClusterRenderer}.
     *
     * @param context See {@link DefaultClusterRenderer}.
     * @param map See {@link DefaultClusterRenderer}.
     * @param clusterManager See {@link DefaultClusterRenderer}.
     * @see DefaultClusterRenderer
     */
    StopClusterRenderer(final Context context, final GoogleMap map,
            final ClusterManager<Stop> clusterManager) {
        super(context, map, clusterManager);
    }

    @Override
    protected void onBeforeClusterItemRendered(final Stop item,
            final MarkerOptions markerOptions) {
        MapsUtils.applyStopDirectionToMarker(markerOptions, item.getOrientation());
        markerOptions.anchor(0.5f, 1.f)
                .draggable(false);
    }

    @Override
    protected void onClusterItemRendered(final Stop clusterItem, final Marker marker) {
        marker.setTag(clusterItem.getStopCode());
    }
}
