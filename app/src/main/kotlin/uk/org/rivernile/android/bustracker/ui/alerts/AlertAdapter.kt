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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator
import uk.org.rivernile.edinburghbustracker.android.databinding.AlertmanagerProximityItemBinding
import uk.org.rivernile.edinburghbustracker.android.databinding.AlertmanagerTimeItemBinding

/**
 * This [ListAdapter] shows the user's currently set alerts.
 *
 * @param context The [android.app.Activity] [Context].
 * @param textFormattingUtils Used within the [RecyclerView.ViewHolder]s to populate the stop name.
 * @param stopMapMarkerDecorator Used within the [RecyclerView.ViewHolder]s to properly create the
 * stop markers on the map.
 * @param clickListener This listener is called when various click items have been clicked by the
 * user.
 * @author Niall Scott
 */
class AlertAdapter(
        context: Context,
        private val textFormattingUtils: TextFormattingUtils,
        private val stopMapMarkerDecorator: StopMapMarkerDecorator,
        private val clickListener: OnAlertItemClickListener)
    : ListAdapter<UiAlert, RecyclerView.ViewHolder>(ItemEquator()) {

    companion object {

        private const val VIEW_TYPE_ARRIVAL_ALERT = 1
        private const val VIEW_TYPE_PROXIMITY_ALERT = 2
    }

    private val inflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_ARRIVAL_ALERT ->
            ArrivalAlertViewHolder(
                    AlertmanagerTimeItemBinding.inflate(inflater, parent, false),
                    textFormattingUtils,
                    stopMapMarkerDecorator,
                    clickListener)
        VIEW_TYPE_PROXIMITY_ALERT ->
            ProximityAlertViewHolder(
                    AlertmanagerProximityItemBinding.inflate(inflater, parent, false),
                    textFormattingUtils,
                    stopMapMarkerDecorator,
                    clickListener)
        else -> throw IllegalStateException("Unrecognised viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is ArrivalAlertViewHolder -> holder.populate(getItem(position) as? UiAlert.ArrivalAlert)
            is ProximityAlertViewHolder ->
                holder.populate(getItem(position) as? UiAlert.ProximityAlert)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is UiAlert.ArrivalAlert -> VIEW_TYPE_ARRIVAL_ALERT
        is UiAlert.ProximityAlert -> VIEW_TYPE_PROXIMITY_ALERT
        else -> throw IllegalStateException("Unrecognised item: ${getItem(position)}")
    }

    override fun getItemId(position: Int) = getItem(position)?.id?.toLong() ?: -1L

    /**
     * This is used to compare [UiAlert]s to determine [RecyclerView] changes.
     */
    private class ItemEquator : DiffUtil.ItemCallback<UiAlert>() {

        override fun areItemsTheSame(oldItem: UiAlert, newItem: UiAlert) = oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: UiAlert, newItem: UiAlert) = oldItem == newItem
    }
}