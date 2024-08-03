/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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

import android.content.res.ColorStateList
import androidx.annotation.ColorInt
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.color.MaterialColors
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemServiceChooserServiceBinding

/**
 * This [RecyclerView.ViewHolder] shows the service item.
 *
 * @param viewBinding The layout for this view holder.
 * @param clickListener Where click events should be sent to.
 * @author Niall Scott
 */
class ServiceViewHolder(
    private val viewBinding: ListItemServiceChooserServiceBinding,
    private val clickListener: OnServiceClickedListener
) : RecyclerView.ViewHolder(viewBinding.root) {

    private var item: UiService? = null
    private val defaultBackground = MaterialColors.getColor(
        viewBinding.root,
        com.google.android.material.R.attr.colorTertiary
    )
    private val defaultTextColour = MaterialColors.getColor(
        viewBinding.root,
        com.google.android.material.R.attr.colorOnTertiary
    )
    private val colourOnSurface = MaterialColors.getColor(
        viewBinding.root,
        com.google.android.material.R.attr.colorOnSurface
    )

    init {
        viewBinding.txtServiceName.setOnClickListener {
            handleServiceClicked()
        }
    }

    /**
     * Populate this view holder.
     *
     * @param oldItem The previous item in this view holder.
     * @param newItem The new item in this view holder.
     */
    fun populate(oldItem: UiService?, newItem: UiService?) {
        item = newItem

        viewBinding.txtServiceName.apply {
            newItem?.let {
                text = it.serviceName
                isChecked = it.isSelected

                if (oldItem == null || oldItem.serviceColours != it.serviceColours) {
                    val backgroundColour = it.serviceColours?.primaryColour ?: defaultBackground
                    val textColour = it.serviceColours?.colourOnPrimary ?: defaultTextColour
                    backgroundTintList = ColorStateList.valueOf(backgroundColour)
                    setTextColor(
                        createColorStateListForServiceName(
                            colourOnSurface,
                            textColour
                        )
                    )
                }
            } ?: run {
                text = null
                isChecked = false
                backgroundTintList = ColorStateList.valueOf(defaultBackground)
                setTextColor(
                    createColorStateListForServiceName(
                        colourOnSurface,
                        defaultTextColour
                    )
                )
            }
        }
    }

    /**
     * Handle the item being clicked.
     */
    private fun handleServiceClicked() {
        item?.let {
            clickListener.onServiceClicked(it.serviceName)
        }
    }

    /**
     * For the combination of selected and unselected service text colours, create the
     * [ColorStateList] to be applied to the text view to control the text colour.
     *
     * @param unselectedColour The unselected colour.
     * @param selectedColor The selected colour.
     * @return The resulting [ColorStateList].
     */
    private fun createColorStateListForServiceName(
        @ColorInt unselectedColour: Int,
        @ColorInt selectedColor: Int
    ): ColorStateList {
        return ColorStateList(
            arrayOf(
                intArrayOf(android.R.attr.state_checked),
                intArrayOf()
            ),
            intArrayOf(
                selectedColor,
                unselectedColour
            )
        )
    }
}