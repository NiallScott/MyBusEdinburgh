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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.maps.android.clustering.ClusterManager;
import com.google.maps.android.clustering.view.DefaultClusterRenderer;

import java.lang.ref.WeakReference;

import uk.org.rivernile.android.utils.MapsUtils;

/**
 * This is a subclassed {@link DefaultClusterRenderer} to allow us to customise the presentation of
 * the {@link Marker} before it is displayed on the map.
 *
 * @author Niall Scott
 * @see DefaultClusterRenderer
 */
class StopClusterRenderer extends DefaultClusterRenderer<Stop> {

    private final WeakReference<OnItemRenderedListener> itemRenderedListenerRef;
    private String selectedStopCode;

    /**
     * See {@link DefaultClusterRenderer}.
     *
     * @param context See {@link DefaultClusterRenderer}.
     * @param map See {@link DefaultClusterRenderer}.
     * @param clusterManager See {@link DefaultClusterRenderer}.
     * @param itemRenderedListener The listener to call when {@link Marker}s have been rendered.
     * @see DefaultClusterRenderer
     */
    StopClusterRenderer(@NonNull final Context context, @NonNull final GoogleMap map,
            @NonNull final ClusterManager<Stop> clusterManager,
            @NonNull final OnItemRenderedListener itemRenderedListener) {
        super(context, map, clusterManager);

        itemRenderedListenerRef = new WeakReference<>(itemRenderedListener);
    }

    /**
     * Set the selected stop code. The {@link OnItemRenderedListener} will only be called when this
     * stop code is rendered.
     *
     * @param selectedStopCode The selected stop code.
     */
    void setSelectedStopCode(@Nullable final String selectedStopCode) {
        this.selectedStopCode = selectedStopCode;
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

        if (clusterItem.getStopCode().equals(selectedStopCode)) {
            dispatchItemRenderedListener(marker);
        }
    }

    /**
     * Dispatch the listener for when the selected stop code, as defined by
     * {@link #setSelectedStopCode(String)}, is rendered.
     *
     * @param marker The rendered {@link Marker}.
     */
    private void dispatchItemRenderedListener(@NonNull final Marker marker) {
        final OnItemRenderedListener listener = itemRenderedListenerRef.get();

        if (listener != null) {
            listener.onItemRendered(marker);
        }
    }

    /**
     * This interface should be implemented by the class interested in getting callbacks when a
     * {@link Marker} with a stop code defined by {@link #setSelectedStopCode(String)} is rendered
     * to the map.
     */
    interface OnItemRenderedListener {

        /**
         * This is called when the {@link Marker} with the stop code defined by
         * {@link #setSelectedStopCode(String)} is rendered to the map.
         *
         * @param marker The {@link Marker} that has been rendered to the map.
         */
        void onItemRendered(@NonNull Marker marker);
    }
}
