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

package uk.org.rivernile.android.bustracker.ui.neareststops

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemNearestStopBinding

/**
 * This [ListAdapter] populates each row of the nearest stops.
 *
 * @param context The [android.app.Activity] [Context].
 * @param clickListener Where click events should be sent.
 * @param stopMapMarkerDecorator Used to populate the stop marker based on the orientation.
 * @param textFormattingUtils Used to format stop name details.
 * @author Niall Scott
 */
class NearestStopsAdapter(
        context: Context,
        private val clickListener: OnNearStopItemClickListener,
        private val stopMapMarkerDecorator: StopMapMarkerDecorator,
        private val textFormattingUtils: TextFormattingUtils)
    : ListAdapter<UiNearestStop, NearestStopViewHolder>(ItemEquator()) {

    private val inflater = LayoutInflater.from(context)
    private val directionStrings = context.resources.getStringArray(R.array.orientations)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            NearestStopViewHolder(
                    ListItemNearestStopBinding.inflate(inflater, parent, false),
                    clickListener,
                    stopMapMarkerDecorator,
                    textFormattingUtils,
                    directionStrings)

    override fun onBindViewHolder(holder: NearestStopViewHolder, position: Int) {
        holder.populate(getItem(position))
    }

    override fun getItemId(position: Int) =
            getItem(position)?.stopCode?.hashCode()?.toLong() ?: 0L

    /**
     * This class is used to compare [UiNearestStop] items to determine position updates.
     */
    private class ItemEquator : DiffUtil.ItemCallback<UiNearestStop>() {

        override fun areItemsTheSame(oldItem: UiNearestStop, newItem: UiNearestStop) =
                oldItem.stopCode == newItem.stopCode

        override fun areContentsTheSame(oldItem: UiNearestStop, newItem: UiNearestStop) =
                oldItem == newItem
    }
}