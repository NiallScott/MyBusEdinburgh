/*
 * Copyright (C) 2018 - 2026 Niall 'Rivernile' Scott
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
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator

/**
 * This class overrides [DefaultClusterRenderer] so that the markers are customised.
 *
 * @param context The [android.app.Activity] instance.
 * @param map The [GoogleMap] object which will be operated upon.
 * @param clusterManager The [ClusterManager] instance.
 * @param stopMapMarkerDecorator Used to decorate the marker icons correctly.
 * @param textFormattingUtils Used to format text on the marker.
 * @author Niall Scott
 */
class StopClusterRenderer(
    context: Context,
    map: GoogleMap,
    clusterManager: ClusterManager<UiStopMarker>,
    private val stopMapMarkerDecorator: StopMapMarkerDecorator,
    private val textFormattingUtils: TextFormattingUtils
) : DefaultClusterRenderer<UiStopMarker>(context, map, clusterManager) {

    override fun onBeforeClusterItemRendered(item: UiStopMarker, markerOptions: MarkerOptions) {
        with(markerOptions) {
            stopMapMarkerDecorator.applyStopDirectionToMarker(this, item.orientation)
            title(
                textFormattingUtils.formatBusStopNameWithStopCode(
                    item.stopIdentifier,
                    item.stopName
                )
            )
            anchor(0.5f, 1f)
            draggable(false)
        }
    }

    override fun onClusterItemRendered(clusterItem: UiStopMarker, marker: Marker) {
        marker.tag = clusterItem

        if (clusterItem.isInfoWindowShown) {
            marker.showInfoWindow()
        } else if (marker.isInfoWindowShown) {
            marker.hideInfoWindow()
        }
    }

    override fun onClusterItemUpdated(item: UiStopMarker, marker: Marker) {
        val currentItem = marker.tag as? UiStopMarker

        if (!item.deepEquals(currentItem)) {
            marker.apply {
                tag = item
                stopMapMarkerDecorator.applyStopDirectionToMarker(this, item.orientation)
                title = textFormattingUtils.formatBusStopNameWithStopCode(
                    item.stopIdentifier,
                    item.stopName
                )
                setAnchor(0.5f, 1f)
                isDraggable = false
                position = item.position

                if (item.isInfoWindowShown) {
                    marker.showInfoWindow()
                } else if (marker.isInfoWindowShown) {
                    marker.hideInfoWindow()
                }
            }
        }
    }
}
