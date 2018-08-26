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
 *
 */

package uk.org.rivernile.android.bustracker.ui.busstopmap

import android.content.Context
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.clustering.ClusterManager
import com.google.maps.android.clustering.view.DefaultClusterRenderer
import uk.org.rivernile.android.bustracker.repositories.busstopmap.Stop
import uk.org.rivernile.android.utils.MapsUtils
import java.lang.ref.WeakReference

/**
 * This class overrides [DefaultClusterRenderer] so that the markers are customised and to also
 * provide a callback interface that's called when a marker is rendered.
 *
 * @author Niall Scott
 * @param context The [android.app.Activity] instance.
 * @param map The map object.
 * @param clusterManager The [ClusterManager] instance.
 * @param itemRenderedListener See [OnItemRenderedListener].
 * @param viewModel The [BusStopMapViewModel] being used to control the UI.
 */
internal class StopClusterRenderer(context: Context,
                                   map: GoogleMap,
                                   clusterManager: ClusterManager<Stop>,
                                   itemRenderedListener: OnItemRenderedListener,
                                   private val viewModel: BusStopMapViewModel)
    : DefaultClusterRenderer<Stop>(context, map, clusterManager) {

    private val itemRenderedListenerRef =
            WeakReference<OnItemRenderedListener>(itemRenderedListener)

    override fun onBeforeClusterItemRendered(item: Stop, markerOptions: MarkerOptions) {
        MapsUtils.applyStopDirectionToMarker(markerOptions, item.orientation)
        markerOptions.anchor(0.5f, 1f)
                .draggable(false)
    }

    override fun onClusterItemRendered(clusterItem: Stop, marker: Marker) {
        val stopCode = clusterItem.stopCode
        marker.tag = stopCode

        if (stopCode == viewModel.showMapMarkerBubble.value?.stopCode) {
            dispatchItemRenderedListener(marker)
        }
    }

    /**
     * Dispatch the listener for when the selected stop code is rendered.
     *
     * @param marker The rendered [Marker].
     */
    private fun dispatchItemRenderedListener(marker: Marker) {
        itemRenderedListenerRef.get()?.onItemRendered(marker)
    }

    /**
     * This interface should be implemented by the class interested in getting callbacks when a
     * [Marker] with that matches the selected stop is rendered to the map.
     */
    internal interface OnItemRenderedListener {

        /**
         * This is called when the [Marker] that matches the selected stop is rendered to the map.
         *
         * @param marker The [Marker] that has been rendered to the map.
         */
        fun onItemRendered(marker: Marker)
    }
}