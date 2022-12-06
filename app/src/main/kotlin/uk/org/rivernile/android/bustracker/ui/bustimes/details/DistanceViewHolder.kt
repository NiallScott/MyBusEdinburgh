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
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemStopDetailsLocationBinding

/**
 * This [RecyclerView.ViewHolder] shows the distance between a stop and the device. If the distance
 * is unknown (either stop location is unknown or device location is unknown) then generic error
 * text will be displayed. If the location is not available due to permissions, the text label will
 * communicate this too.
 *
 * @param viewBinding An object holding the UI elements.
 * @author Niall Scott
 */
class DistanceViewHolder(
        private val viewBinding: ListItemStopDetailsLocationBinding)
    : RecyclerView.ViewHolder(viewBinding.root) {

    /**
     * Populate this [RecyclerView.ViewHolder] with the data in the item.
     *
     * @param item The data to use to populate this [RecyclerView.ViewHolder].
     */
    fun populate(item: UiItem.Distance?) {
        viewBinding.txtDistance.apply {
            when (item) {
                is UiItem.Distance.Known ->
                    text = viewBinding.root.context.getString(R.string.stopdetails_stop_distance,
                            item.distanceKilometers)
                is UiItem.Distance.PermissionDenied ->
                    setText(R.string.stopdetails_stop_distance_permission_required)
                is UiItem.Distance.Unknown ->
                    setText(R.string.stopdetails_stop_distance_unknown)
                is UiItem.Distance.LocationOff ->
                    setText(R.string.stopdetails_stop_distance_location_off)
                else -> text = null
            }
        }
    }
}