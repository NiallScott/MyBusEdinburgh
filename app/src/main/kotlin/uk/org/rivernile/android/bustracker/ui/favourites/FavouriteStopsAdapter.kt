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

package uk.org.rivernile.android.bustracker.ui.favourites

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import uk.org.rivernile.edinburghbustracker.android.databinding.SimpleListItem2Binding

/**
 * This [ListAdapter] populates each row for the user's favourite stops.
 *
 * @param context The [android.app.Activity] [Context].
 * @param clickListener Where favourite item click events should be sent.
 * @param isCreateShortcutMode Are we in the create shortcut mode?
 * @author Niall Scott
 */
class FavouriteStopsAdapter(
        context: Context,
        private val clickListener: OnFavouriteItemClickListener,
        private val isCreateShortcutMode: Boolean)
    : ListAdapter<UiFavouriteStop, FavouriteStopViewHolder>(ItemEquator()) {

    private val inflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            FavouriteStopViewHolder(
                    SimpleListItem2Binding.inflate(inflater, parent, false),
                    clickListener,
                    isCreateShortcutMode)

    override fun onBindViewHolder(holder: FavouriteStopViewHolder, position: Int) {
        holder.populate(getItem(position))
    }

    override fun getItemId(position: Int) =
            getItem(position)?.favouriteStop?.stopCode?.hashCode()?.toLong() ?: 0L

    /**
     * This is used to compare [UiFavouriteStop] items to determine position updates.
     */
    private class ItemEquator : DiffUtil.ItemCallback<UiFavouriteStop>() {

        override fun areItemsTheSame(oldItem: UiFavouriteStop, newItem: UiFavouriteStop) =
                oldItem.favouriteStop.stopCode == newItem.favouriteStop.stopCode

        override fun areContentsTheSame(oldItem: UiFavouriteStop, newItem: UiFavouriteStop) =
                oldItem == newItem
    }
}