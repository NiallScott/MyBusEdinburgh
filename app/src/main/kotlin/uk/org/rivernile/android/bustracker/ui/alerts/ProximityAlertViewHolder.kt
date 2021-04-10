/*
 * Copyright (C) 2021 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts

import android.view.View
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.Circle
import com.google.android.gms.maps.model.CircleOptions
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.AlertmanagerProximityItemBinding

/**
 * This is a [RecyclerView.ViewHolder] to show a [UiAlert.ProximityAlert].
 *
 * @param viewBinding The view binding for this [RecyclerView.ViewHolder].
 * @param textFormattingUtils Used to format the stop name.
 * @param stopMapMarkerDecorator Used to populate the stop icon on the map.
 * @param clickListener Where click events should be sent to.
 * @author Niall Scott
 */
class ProximityAlertViewHolder(
        private val viewBinding: AlertmanagerProximityItemBinding,
        private val textFormattingUtils: TextFormattingUtils,
        private val stopMapMarkerDecorator: StopMapMarkerDecorator,
        private val clickListener: OnAlertItemClickListener)
    : RecyclerView.ViewHolder(viewBinding.root) {

    private val rangeRingStrokeColour = ContextCompat.getColor(itemView.context,
            R.color.map_range_ring_stroke)
    private val rangeRingFillColour = ContextCompat.getColor(itemView.context,
            R.color.map_range_ring_fill)
    private val rangeRingStrokeWidth = itemView.resources.getDimension(
            R.dimen.map_range_ring_stroke)

    private var alert: UiAlert.ProximityAlert? = null
    private var map: GoogleMap? = null
    private var marker: Marker? = null
    private var circle: Circle? = null

    init {
        viewBinding.apply {
            btnLocationSettings.setOnClickListener {
                handleLocationSettingsClicked()
            }

            btnRemove.setOnClickListener {
                handleRemoveClicked()
            }

            mapView.isClickable = false
            mapView.onCreate(null)
            mapView.getMapAsync { map ->
                this@ProximityAlertViewHolder.map = map
                populateMap(map, alert)
            }
        }
    }

    /**
     * Populate this [RecyclerView.ViewHolder] with an [UiAlert.ProximityAlert].
     *
     * @param alert The alert to populate the ViewHolder with.
     */
    fun populate(alert: UiAlert.ProximityAlert?) {
        this.alert = alert

        viewBinding.txtDescription.text = alert?.let {
            val stopName = textFormattingUtils.formatBusStopNameWithStopCode(it.stopCode,
                    it.stopDetails?.stopName)
            viewBinding.root.context.getString(R.string.alertmanager_prox_subtitle,
                    it.distanceFrom, stopName)
        }

        map?.let {
            populateMap(it, alert)
        }
    }

    /**
     * Populate the [GoogleMap] to move the camera center point to that of the stop, and add a
     * marker in this location to denote the stop. A circle is added around this point to denote
     * the set proximity distance.
     *
     * @param map The [GoogleMap] to populate.
     * @param alert The alert to populate the map with. If this is `null`, the map will be hidden.
     */
    private fun populateMap(
            map: GoogleMap,
            alert: UiAlert.ProximityAlert?) {
        marker?.remove()
        circle?.remove()

        alert?.let { a ->
            a.stopDetails?.let { stopDetails ->
                val latLon = LatLng(stopDetails.latitude, stopDetails.longitude)
                map.moveCamera(CameraUpdateFactory.newLatLng(latLon))

                circle = CircleOptions().center(latLon)
                        .radius(a.distanceFrom.toDouble())
                        .strokeColor(rangeRingStrokeColour)
                        .strokeWidth(rangeRingStrokeWidth)
                        .fillColor(rangeRingFillColour)
                        .let(map::addCircle)

                marker = MarkerOptions().position(latLon)
                        .apply {
                            stopMapMarkerDecorator.applyStopDirectionToMarker(this,
                                    stopDetails.orientation)
                        }
                        .let(map::addMarker)

                viewBinding.mapView.visibility = View.VISIBLE
            }
        } ?: run {
            marker = null
            circle = null
            viewBinding.mapView.visibility = View.GONE
        }
    }

    /**
     * Handle the user clicking on the button to show the system location settings.
     */
    private fun handleLocationSettingsClicked() {
        clickListener.onLocationSettingsClicked()
    }

    /**
     * Handle the user clicking on the button to remove the proximity alert.
     */
    private fun handleRemoveClicked() {
        alert?.let {
            clickListener.onRemoveProximityAlertClicked(it.stopCode)
        }
    }
}