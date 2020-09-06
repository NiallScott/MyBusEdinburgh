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
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
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

    companion object {

        private const val COLLAPSED_DEGREES = 0f
        private const val EXPANDED_DEGREES = 180f
    }

    private val txtServiceName: TextView = itemView.findViewById(R.id.txtServiceName)
    private val txtDestination: TextView = itemView.findViewById(R.id.txtDestination)
    private val txtTime: TextView = itemView.findViewById(R.id.txtTime)
    private val imgArrow: ImageView = itemView.findViewById(R.id.imgArrow)

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
     * @param item The data to use to populate this item.
     */
    fun populate(item: UiLiveTimesItem?) {
        this.item = item
        populator.populateDestination(txtDestination, item)
        populator.populateTime(txtTime, item)

        item?.let {
            txtServiceName.text = it.serviceName
            val backgroundColour = it.serviceColour ?:defaultBackground
            txtServiceName.backgroundTintList = ColorStateList.valueOf(backgroundColour)

            imgArrow.rotation = if (it.expanded) EXPANDED_DEGREES else COLLAPSED_DEGREES
        } ?: run {
            txtServiceName.text = null
            txtServiceName.backgroundTintList = ColorStateList.valueOf(defaultBackground)
            imgArrow.rotation = COLLAPSED_DEGREES
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