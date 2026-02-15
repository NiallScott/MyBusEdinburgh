/*
 * Copyright (C) 2021 - 2026 Niall 'Rivernile' Scott
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.map.MapStyleApplicator
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemAlertTimeBinding

/**
 * This is a [RecyclerView.ViewHolder] to show an [UiAlert.ArrivalAlert].
 *
 * @param viewBinding The view binding for this [RecyclerView.ViewHolder].
 * @param textFormattingUtils Used to format the stop name.
 * @param stopMapMarkerDecorator Used to populate the stop icon on the map.
 * @param mapStyleApplicator Used to style the map correctly.
 * @param clickListener Where click events should be sent to.
 * @author Niall Scott
 */
class ArrivalAlertViewHolder(
        private val viewBinding: ListItemAlertTimeBinding,
        private val textFormattingUtils: TextFormattingUtils,
        private val stopMapMarkerDecorator: StopMapMarkerDecorator,
        private val mapStyleApplicator: MapStyleApplicator,
        private val clickListener: OnAlertItemClickListener)
    : RecyclerView.ViewHolder(viewBinding.root) {

    private var alert: UiAlert.ArrivalAlert? = null
    private var map: GoogleMap? = null
    private var marker: Marker? = null

    init {
        viewBinding.apply {
            btnRemove.setOnClickListener {
                handleRemoveClicked()
            }

            mapView.isClickable = false
            mapView.onCreate(null)
            mapView.getMapAsync { map ->
                this@ArrivalAlertViewHolder.map = map
                mapStyleApplicator.applyMapStyle(mapView.context, map)
                populateMap(map, alert)
            }
        }
    }

    /**
     * Populate this [RecyclerView.ViewHolder] with an [UiAlert.ArrivalAlert].
     *
     * @param alert The alert to populate the ViewHolder with.
     */
    fun populate(alert: UiAlert.ArrivalAlert?) {
        this.alert = alert

        viewBinding.txtDescription.text = alert?.let {
            val stopName = textFormattingUtils.formatBusStopNameWithStopCode(it.stopIdentifier,
                    it.stopDetails?.stopName)
            val services = it.services
            val timeTrigger = it.timeTrigger
            val descriptionTextRes = if (services.size > 1) {
                R.plurals.alertmanager_time_subtitle_multiple_services
            } else {
                R.plurals.alertmanager_time_subtitle_single_service
            }

            viewBinding.root.resources.getQuantityString(
                descriptionTextRes,
                timeTrigger,
                services.joinToString(", ") { service -> service.serviceName },
                stopName,
                timeTrigger
            )
        }

        map?.let {
            populateMap(it, alert)
        }
    }

    /**
     * Populate the [GoogleMap] to move the camera center point to that of the stop, and add a
     * marker in this location to denote the stop.
     *
     * @param map The [GoogleMap] to populate.
     * @param alert The alert to populate the map with. If this is `null`, the map will be hidden.
     */
    private fun populateMap(
            map: GoogleMap,
            alert: UiAlert.ArrivalAlert?) {
        marker?.remove()

        alert?.stopDetails?.let {
            val latLon = LatLng(it.location.latitude, it.location.longitude)
            map.moveCamera(CameraUpdateFactory.newLatLng(latLon))

            marker = MarkerOptions()
                    .position(latLon)
                    .apply {
                        stopMapMarkerDecorator.applyStopDirectionToMarker(this,
                                it.orientation)
                    }
                    .let(map::addMarker)
            viewBinding.mapView.visibility = View.VISIBLE
        } ?: run {
            marker = null
            viewBinding.mapView.visibility = View.GONE
        }
    }

    /**
     * Handle the user clicking on the button to remove the arrival alert.
     */
    private fun handleRemoveClicked() {
        alert?.let {
            clickListener.onRemoveArrivalAlertClicked(it.stopIdentifier)
        }
    }
}
