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

package uk.org.rivernile.android.bustracker.ui.bustimes.details

import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator
import uk.org.rivernile.edinburghbustracker.android.databinding.StopdetailsMapItemBinding

/**
 * This [RecyclerView.ViewHolder] shows a [GoogleMap] for a stop, with the map camera centered on
 * the stop location and a marker placed at this location.
 *
 * @param viewBinding An object holding the view objects.
 * @param stopMapMarkerDecorator Used to correctly decorate the stop marker.
 * @param clickListener Where click events should be sent.
 * @author Niall Scott
 */
class MapViewHolder(
        viewBinding: StopdetailsMapItemBinding,
        private val stopMapMarkerDecorator: StopMapMarkerDecorator,
        private val clickListener: OnDetailItemClickListener)
    : RecyclerView.ViewHolder(viewBinding.root) {

    private var item: UiItem.Map? = null
    private var map: GoogleMap? = null
    private var marker: Marker? = null

    init {
        viewBinding.mapView.apply {
            onCreate(null)
            getMapAsync { map ->
                this@MapViewHolder.map = map
                map.setOnMapClickListener {
                    handleMapClicked()
                }

                populateMap(map, item)
            }
        }
    }

    /**
     * Populate this [RecyclerView.ViewHolder] with the given item data.
     *
     * @param item The item to populate this [RecyclerView.ViewHolder] with.
     */
    fun populate(item: UiItem.Map?) {
        this.item = item

        map?.let {
            populateMap(it, item)
        }
    }

    /**
     * Populate the map by moving the camera to the stop location and placing a marker there.
     *
     * @param map The [GoogleMap] to populate.
     * @param item The object containing the coordinate data.
     */
    private fun populateMap(
            map: GoogleMap,
            item: UiItem.Map?) {
        marker?.remove()
        marker = null

        item?.let {
            val latLon = LatLng(it.latitude, it.longitude)
            map.moveCamera(CameraUpdateFactory.newLatLng(latLon))

            marker = MarkerOptions()
                    .position(latLon)
                    .apply {
                        stopMapMarkerDecorator.applyStopDirectionToMarker(this, it.orientation)
                    }
                    .let(map::addMarker)
        }
    }

    /**
     * Handles the map being clicked by forwarding the event to the [OnDetailItemClickListener].
     */
    private fun handleMapClicked() {
        item?.let(clickListener::onMapClicked)
    }
}