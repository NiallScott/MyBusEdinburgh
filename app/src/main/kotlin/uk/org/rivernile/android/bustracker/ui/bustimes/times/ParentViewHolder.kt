/*
 * Copyright (C) 2020 - 2024 Niall 'Rivernile' Scott
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemBusTimesParentBinding

/**
 * This [RecyclerView.ViewHolder] shows a parent live times item. A parent item is one which has all
 * the contents of the child one, plus shows the service name and an indicator to show whether the
 * service is expanded or not.
 *
 * Also, the row is clickable to allow for expansion and collapsing.
 *
 * @param viewBinding The root [View] of this [RecyclerView.ViewHolder].
 * @param populator An implementation used to populate the fields.
 * @param clickListener Where click events should be sent to.
 * @author Niall Scott
 */
class ParentViewHolder(
    private val viewBinding: ListItemBusTimesParentBinding,
    private val populator: ViewHolderFieldPopulator,
    private val clickListener: OnParentClickedListener
) : RecyclerView.ViewHolder(viewBinding.root) {

    private val defaultBackground = MaterialColors.getColor(
        itemView,
        com.google.android.material.R.attr.colorTertiary
    )
    private val defaultTextColour = MaterialColors.getColor(
        itemView,
        com.google.android.material.R.attr.colorOnTertiary
    )

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

        viewBinding.apply {
            populateServiceName(newItem)
            populateServiceColours(oldItem, newItem)
            populator.populateDestination(txtDestination, newItem)
            populator.populateTime(txtTime, newItem)
            populateExpandCollapseIndicator(oldItem, newItem)
        }
    }

    /**
     * Populate the service name.
     *
     * @param item The name is obtained from here. If [item] is `null`, then the text will be set
     * to `null`.
     */
    private fun populateServiceName(item: UiLiveTimesItem?) {
        viewBinding.txtServiceName.text = item?.serviceName
    }

    /**
     * Populate the service text background and text colour with the service colours.
     *
     * @param oldItem The old item, used to decide if the item should be updated.
     * @param newItem The new item containing the service colour to populate.
     */
    private fun populateServiceColours(oldItem: UiLiveTimesItem?, newItem: UiLiveTimesItem?) {
        viewBinding.txtServiceName.apply {
            newItem?.also {
                if (oldItem == null || oldItem.serviceColours != it.serviceColours) {
                    val backgroundColour = it.serviceColours?.primaryColour ?: defaultBackground
                    val textColour = it.serviceColours?.colourOnPrimary ?: defaultTextColour
                    backgroundTintList = ColorStateList.valueOf(backgroundColour)
                    setTextColor(textColour)
                }
            } ?: run {
                backgroundTintList = ColorStateList.valueOf(defaultBackground)
                setTextColor(defaultTextColour)
            }
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
        newItem: UiLiveTimesItem?
    ) {
        viewBinding.apply {
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