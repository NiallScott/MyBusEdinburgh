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

package uk.org.rivernile.android.bustracker.ui.about

import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.SimpleListItem1Binding

/**
 * This [RecyclerView.ViewHolder] shows a single line 'about' item.
 *
 * @param viewBinding The view binding.
 * @param itemClickedListener The click listener which is called when the item is clicked.
 * @author Niall Scott
 */
class OneLineItemViewHolder(
        private val viewBinding: SimpleListItem1Binding,
        private val itemClickedListener: OnItemClickedListener)
    : RecyclerView.ViewHolder(viewBinding.root) {

    private var item: UiAboutItem.OneLineItem? = null

    init {
        viewBinding.root.setOnClickListener {
            handleItemClicked()
        }
    }

    /**
     * Populate the item.
     *
     * @param item The item to populate with.
     */
    fun populate(item: UiAboutItem.OneLineItem?) {
        this.item = item

        viewBinding.apply {
            item?.let {
                text1.setText(getItemText(it))
                root.isClickable = it.isClickable
                root.isFocusable = it.isClickable
            } ?: run {
                text1.text = null
                root.isClickable = false
                root.isFocusable = false
            }
        }
    }

    /**
     * Get the text String resource for the item.
     *
     * @param item The item.
     * @return The String resource ID for the item.
     */
    @StringRes
    private fun getItemText(item: UiAboutItem.OneLineItem) = when (item) {
        is UiAboutItem.OneLineItem.Credits -> R.string.about_credits
        is UiAboutItem.OneLineItem.OpenSourceLicences -> R.string.about_open_source
    }

    /**
     * Handle the item being clicked.
     */
    private fun handleItemClicked() {
        item?.let {
            if (it.isClickable) {
                itemClickedListener.onItemClicked(it)
            }
        }
    }
}