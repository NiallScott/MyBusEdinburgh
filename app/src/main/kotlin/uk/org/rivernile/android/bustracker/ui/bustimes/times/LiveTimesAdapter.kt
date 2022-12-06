/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemBusTimesChildBinding
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemBusTimesParentBinding

typealias OnParentClickedListener = (serviceName: String) -> Unit

/**
 * This [ListAdapter] shows a list of live times. This is arranged in a parent-child way, whereby
 * the parent contains the service (and the first vehicle) and the following rows are vehicles 2 to
 * n.
 *
 * The arrangement in to parent and children is done elsewhere and is supplied to this adapter
 * already in the correct way.
 *
 * @param context The [android.app.Activity] [Context].
 * @param populator An implementation used to populate the row fields.
 * @param parentClickedListener This listener is called when the parent item row has been clicked.
 * @author Niall Scott
 */
class LiveTimesAdapter(
        context: Context,
        private val populator: ViewHolderFieldPopulator,
        private val parentClickedListener: OnParentClickedListener)
    : ListAdapter<UiLiveTimesItem, RecyclerView.ViewHolder>(ItemEquator()) {

    companion object {

        private const val ITEM_TYPE_PARENT = 1
        private const val ITEM_TYPE_CHILD = 2
    }

    private val inflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return if (viewType == ITEM_TYPE_PARENT) {
            ParentViewHolder(
                    ListItemBusTimesParentBinding.inflate(inflater, parent, false),
                    populator,
                    parentClickedListener)
        } else {
            ChildViewHolder(
                    ListItemBusTimesChildBinding.inflate(inflater, parent, false),
                    populator)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        onBindViewHolder(holder, position, emptyList())
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int,
            payloads: List<Any>) {
        val oldItem = payloads.firstOrNull() as UiLiveTimesItem?
        val newItem = getItem(position)

        when (holder) {
            is ParentViewHolder -> holder.populate(oldItem, newItem)
            is ChildViewHolder -> holder.populate(newItem)
        }
    }

    override fun getItemViewType(position: Int): Int {
        val item = getItem(position)

        return if (item?.isParent == true) {
            ITEM_TYPE_PARENT
        } else {
            ITEM_TYPE_CHILD
        }
    }

    override fun getItemId(position: Int) = getItem(position)?.let {
        ((it.serviceName.hashCode() * 31) + it.position).toLong()
    } ?: 0L

    /**
     * This is used to compare [UiLiveTimesItem]s to determine recycler changes.
     */
    private class ItemEquator : DiffUtil.ItemCallback<UiLiveTimesItem>() {

        override fun areItemsTheSame(oldItem: UiLiveTimesItem, newItem: UiLiveTimesItem): Boolean {
            return oldItem.serviceName == newItem.serviceName &&
                    oldItem.position == newItem.position
        }

        override fun areContentsTheSame(oldItem: UiLiveTimesItem, newItem: UiLiveTimesItem) =
                oldItem == newItem

        override fun getChangePayload(oldItem: UiLiveTimesItem, newItem: UiLiveTimesItem) = oldItem
    }
}