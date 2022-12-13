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

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uk.org.rivernile.android.bustracker.map.MapStyleApplicator
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemStopDetailsLocationBinding
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemStopDetailsMapBinding
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemStopDetailsNoServicesBinding
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemStopDetailsServiceBinding

/**
 * This [ListAdapter] displays application information for a stop, which may include a map, the
 * distance between the device and the stop, and a listing of services for the stop.
 *
 * @param context The [android.app.Activity] [Context].
 * @param stopMapMarkerDecorator An implementation used to decorate map marker items.
 * @param mapStyleApplicator Used to apply the correct style to the map.
 * @param clickListener Where item click events should be sent to.
 * @author Niall Scott
 */
class StopDetailsAdapter(
        context: Context,
        private val stopMapMarkerDecorator: StopMapMarkerDecorator,
        private val mapStyleApplicator: MapStyleApplicator,
        private val clickListener: OnDetailItemClickListener)
    : ListAdapter<UiItem, RecyclerView.ViewHolder>(ItemEquator()) {

    companion object {

        private const val ITEM_TYPE_MAP = 1
        private const val ITEM_TYPE_DISTANCE = 2
        private const val ITEM_TYPE_SERVICE = 3
        private const val ITEM_TYPE_NO_SERVICES = 4
    }

    private val inflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        ITEM_TYPE_MAP ->
            MapViewHolder(
                    ListItemStopDetailsMapBinding.inflate(inflater, parent, false),
                    stopMapMarkerDecorator,
                    mapStyleApplicator,
                    clickListener)
        ITEM_TYPE_DISTANCE ->
            DistanceViewHolder(ListItemStopDetailsLocationBinding.inflate(inflater, parent, false))
        ITEM_TYPE_SERVICE ->
            ServiceViewHolder(ListItemStopDetailsServiceBinding.inflate(inflater, parent, false))
        ITEM_TYPE_NO_SERVICES ->
            NoServicesViewHolder(
                    ListItemStopDetailsNoServicesBinding.inflate(inflater, parent, false))
        else -> throw IllegalStateException("Unknown viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, emptyList())
    }

    override fun onBindViewHolder(
            holder: RecyclerView.ViewHolder,
            position: Int,
            payloads: List<Any>) {
        when (holder) {
            is MapViewHolder -> holder.populate(getItem(position) as? UiItem.Map)
            is DistanceViewHolder -> holder.populate(getItem(position) as? UiItem.Distance)
            is ServiceViewHolder -> {
                val oldItem = payloads.firstOrNull() as? UiItem.Service
                holder.populate(oldItem, getItem(position) as? UiItem.Service)
            }
        }
    }

    override fun getItemViewType(position: Int) = getItem(position)?.let {
        when (it) {
            is UiItem.Map -> ITEM_TYPE_MAP
            is UiItem.Distance -> ITEM_TYPE_DISTANCE
            is UiItem.Service -> ITEM_TYPE_SERVICE
            is UiItem.NoServices -> ITEM_TYPE_NO_SERVICES
        }
    } ?: throw IllegalStateException("Item at position $position is null")

    override fun getItemId(position: Int) = when (val item = getItem(position)) {
        is UiItem.Service -> item.id
        is UiItem.Map -> -1L
        is UiItem.Distance -> -2L
        is UiItem.NoServices -> -3L
    }

    /**
     * This is used to compare [UiItem]s to determine recycler changes.
     */
    private class ItemEquator : DiffUtil.ItemCallback<UiItem>() {

        override fun areItemsTheSame(oldItem: UiItem, newItem: UiItem) = when {
            oldItem is UiItem.Service && newItem is UiItem.Service -> oldItem.id == newItem.id
            oldItem::class == newItem::class -> true
            else -> false
        }

        override fun areContentsTheSame(oldItem: UiItem, newItem: UiItem) = oldItem == newItem

        override fun getChangePayload(oldItem: UiItem, newItem: UiItem) = oldItem
    }
}