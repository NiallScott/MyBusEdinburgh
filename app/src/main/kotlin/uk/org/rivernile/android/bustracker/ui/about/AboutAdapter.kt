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
 */

package uk.org.rivernile.android.bustracker.ui.about

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemAbout1LineBinding
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemAbout2LineBinding

/**
 * This adapter populates a list of 'about' items in a [RecyclerView].
 *
 * @param context The [android.app.Activity] [Context].
 * @param clickListener The listener which is called when an item has been clicked.
 * @author Niall Scott
 */
internal class AboutAdapter(
        context: Context,
        private val clickListener: OnItemClickedListener)
    : ListAdapter<UiAboutItem, RecyclerView.ViewHolder>(ItemEquator()) {

    companion object {

        private const val VIEW_TYPE_SINGLE = 0
        private const val VIEW_TYPE_DOUBLE = 1
    }

    private val inflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = when (viewType) {
        VIEW_TYPE_SINGLE ->
            OneLineItemViewHolder(
                    ListItemAbout1LineBinding.inflate(inflater, parent, false),
                    clickListener)
        VIEW_TYPE_DOUBLE ->
            TwoLinesItemViewHolder(
                    ListItemAbout2LineBinding.inflate(inflater, parent, false),
                    clickListener)
        else -> throw IllegalStateException("Unrecognised viewType: $viewType")
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is OneLineItemViewHolder ->
                holder.populate(getItem(position) as? UiAboutItem.OneLineItem)
            is TwoLinesItemViewHolder ->
                holder.populate(getItem(position) as? UiAboutItem.TwoLinesItem)
        }
    }

    override fun getItemViewType(position: Int) = when (getItem(position)) {
        is UiAboutItem.OneLineItem -> VIEW_TYPE_SINGLE
        is UiAboutItem.TwoLinesItem -> VIEW_TYPE_DOUBLE
        else -> throw IllegalStateException("Unrecognised item: ${getItem(position)}")
    }

    override fun getItemId(position: Int) =
            getItem(position)?.id?.toLong() ?: throw IllegalStateException()

    /**
     * Used to compare [UiAboutItem]s to determine [RecyclerView] changes.
     */
    private class ItemEquator : DiffUtil.ItemCallback<UiAboutItem>() {

        override fun areItemsTheSame(oldItem: UiAboutItem, newItem: UiAboutItem) =
                oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: UiAboutItem, newItem: UiAboutItem) =
                oldItem == newItem
    }
}