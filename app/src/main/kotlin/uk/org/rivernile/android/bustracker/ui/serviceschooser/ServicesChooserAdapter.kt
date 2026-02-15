/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemServiceChooserServiceBinding

/**
 * This is the [ListAdapter] for the services chooser.
 *
 * @param context The [android.app.Activity] [Context].
 * @param clickListener Where click events should be sent to.
 * @author Niall Scott
 */
class ServicesChooserAdapter(
    context: Context,
    private val clickListener: OnServiceClickedListener
) : ListAdapter<UiService, ServiceViewHolder>(ItemEquator()) {

    private val inflater = LayoutInflater.from(context)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        ServiceViewHolder(
            ListItemServiceChooserServiceBinding.inflate(inflater, parent, false),
            clickListener
        )

    override fun onBindViewHolder(holder: ServiceViewHolder, position: Int) {
        onBindViewHolder(holder, position, emptyList())
    }

    override fun onBindViewHolder(
        holder: ServiceViewHolder,
        position: Int,
        payloads: List<Any>
    ) {
        val oldItem = payloads.firstOrNull() as? UiService
        holder.populate(oldItem, getItem(position))
    }

    override fun getItemId(position: Int) =
        getItem(position)?.serviceDescriptor?.hashCode()?.toLong() ?: -1L

    /**
     * This is used to compare [UiService]s to determine recycler changes.
     */
    private class ItemEquator : DiffUtil.ItemCallback<UiService>() {

        override fun areItemsTheSame(oldItem: UiService, newItem: UiService) =
            oldItem.serviceDescriptor == newItem.serviceDescriptor

        override fun areContentsTheSame(oldItem: UiService, newItem: UiService) =
            oldItem == newItem

        override fun getChangePayload(oldItem: UiService, newItem: UiService) = oldItem
    }
}
