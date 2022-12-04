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

import android.content.res.ColorStateList
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.StopdetailsServiceItemBinding

/**
 * This [RecyclerView.ViewHolder] shows a row for a service and its route description.
 *
 * @param viewBinding An object holding the view.
 * @author Niall Scott
 */
class ServiceViewHolder(
        private val viewBinding: StopdetailsServiceItemBinding)
    : RecyclerView.ViewHolder(viewBinding.root) {

    private val defaultBackground = MaterialColors.getColor(viewBinding.root, R.attr.colorTertiary)

    /**
     * Populate this [RecyclerView.ViewHolder] with the contents of [newItem].
     *
     * @param oldItem The old item, used to diff contents.
     * @param newItem The new item to populate with.
     */
    fun populate(oldItem: UiItem.Service?, newItem: UiItem.Service?) {
        viewBinding.apply {
            newItem?.let {
                txtServiceName.text = it.name
                txtDescription.text = it.description

                if (oldItem == null || oldItem.colour != it.colour) {
                    val backgroundColour = it.colour ?: defaultBackground
                    txtServiceName.backgroundTintList = ColorStateList.valueOf(backgroundColour)
                }
            } ?: run {
                txtServiceName.text = null
                txtDescription.text = null
                txtServiceName.backgroundTintList = ColorStateList.valueOf(defaultBackground)
            }
        }
    }
}