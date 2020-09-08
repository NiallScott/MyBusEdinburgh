/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

import android.content.res.ColorStateList
import android.view.View
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import uk.org.rivernile.android.bustracker.widget.ExpandCollapseIndicator
import uk.org.rivernile.edinburghbustracker.android.R

/**
 * This [RecyclerView.ViewHolder] shows a parent live times item. A parent item is one which has all
 * the contents of the child one, plus shows the service name and an indicator to show whether the
 * service is expanded or not.
 *
 * Also, the row is clickable to allow for expansion and collapsing.
 *
 * @param itemView The root [View] of this [RecyclerView.ViewHolder].
 * @param populator An implementation used to populate the fields.
 * @param clickListener Where click events should be sent to.
 * @author Niall Scott
 */
class ParentViewHolder(
        itemView: View,
        private val populator: ViewHolderFieldPopulator,
        private val clickListener: OnParentClickedListener)
    : RecyclerView.ViewHolder(itemView) {

    private val txtServiceName: TextView = itemView.findViewById(R.id.txtServiceName)
    private val txtDestination: TextView = itemView.findViewById(R.id.txtDestination)
    private val txtTime: TextView = itemView.findViewById(R.id.txtTime)
    private val imgArrow: ExpandCollapseIndicator = itemView.findViewById(R.id.imgArrow)

    private val defaultBackground = ContextCompat.getColor(itemView.context, R.color.colorAccent)

    private var item: UiLiveTimesItem? = null

    init {
        itemView.setOnClickListener {
            handleItemClicked()
        }
    }

    /**
     * Populate this [RecyclerView.ViewHolder] with the given [UiLiveTimesItem] data.
     *
     * @param oldItem The old item, used for comparison.
     * @param newItem The data to use to populate this item.
     */
    fun populate(oldItem: UiLiveTimesItem?, newItem: UiLiveTimesItem?) {
        this.item = newItem
        populateServiceName(newItem)
        populateServiceBackground(oldItem, newItem)
        populator.populateDestination(txtDestination, newItem)
        populator.populateTime(txtTime, newItem)
        populateExpandCollapseIndicator(oldItem, newItem)
    }

    /**
     * Populate the service name.
     *
     * @param item The name is obtained from here. If [item] is `null`, then the text will be set
     * to `null`.
     */
    private fun populateServiceName(item: UiLiveTimesItem?) {
        txtServiceName.text = item?.serviceName
    }

    /**
     * Populate the service text background with the service colour.
     *
     * @param oldItem The old item, used to decide if the item should be updated.
     * @param newItem The new item containing the service colour to populate.
     */
    private fun populateServiceBackground(oldItem: UiLiveTimesItem?, newItem: UiLiveTimesItem?) {
        newItem?.also {
            if (oldItem == null || oldItem.serviceColour != it.serviceColour) {
                val backgroundColour = it.serviceColour ?: defaultBackground
                txtServiceName.backgroundTintList = ColorStateList.valueOf(backgroundColour)
            }
        } ?: run {
            txtServiceName.backgroundTintList = ColorStateList.valueOf(defaultBackground)
        }
    }

    /**
     * Populate the expand/collapse indicator with the correct status.
     *
     * @param oldItem Used to set the indicator to this state prior to running the animation.
     * @param newItem Contains the new expand state.
     */
    private fun populateExpandCollapseIndicator(
            oldItem: UiLiveTimesItem?,
            newItem: UiLiveTimesItem?) {
        newItem?.also {
            val oldState = oldItem?.expanded ?: it.expanded
            val newState = it.expanded

            if (oldState) {
                imgArrow.expand(false)
            } else {
                imgArrow.collapse(false)
            }

            if (newState) {
                imgArrow.expand(true)
            } else {
                imgArrow.collapse(true)
            }
        } ?: imgArrow.collapse(false)
    }

    /**
     * Handle the row being clicked.
     */
    private fun handleItemClicked() {
        item?.let {
            clickListener.invoke(it.serviceName)
        }
    }
}