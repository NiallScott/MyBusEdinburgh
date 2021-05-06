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

import androidx.recyclerview.widget.RecyclerView
import uk.org.rivernile.edinburghbustracker.android.databinding.SimpleListItem2Binding

/**
 * This class represents a single row, which is a single favourite stop item.
 *
 * @param viewBinding The view binding for this row.
 * @param clickListener Where click events should be sent.
 * @param isCreateShortcutMode Are we in the create shortcut mode? Long clicking is not enabled when
 * in the shortcut mode, so this value is used to enable/disable long clicking.
 * @author Niall Scott
 */
class FavouriteStopViewHolder(
        private val viewBinding: SimpleListItem2Binding,
        private val clickListener: OnFavouriteItemClickListener,
        isCreateShortcutMode: Boolean)
    : RecyclerView.ViewHolder(viewBinding.root) {

    private var favouriteStop: UiFavouriteStop? = null

    init {
        viewBinding.root.apply {
            setOnClickListener {
                handleClick()
            }

            if (!isCreateShortcutMode) {
                isLongClickable = true

                setOnLongClickListener {
                    handleLongClick()
                }
            }
        }
    }

    /**
     * Populate the favourite stop in this [RecyclerView.ViewHolder]. If the favourite stop is
     * `null`, values will be blanked out (this shouldn't happen in practice).
     *
     * @param favouriteStop The favourite stop to populate.
     */
    fun populate(favouriteStop: UiFavouriteStop?) {
        this.favouriteStop = favouriteStop

        viewBinding.apply {
            favouriteStop?.let {
                text1.text = it.favouriteStop.stopName
                text2.text = it.services?.joinToString(", ")
            } ?: run {
                text1.text = null
                text2.text = null
            }
        }
    }

    /**
     * Handle the favourite stop being clicked.
     */
    private fun handleClick() {
        favouriteStop?.let {
            clickListener.onFavouriteClicked(it.favouriteStop)
        }
    }

    /**
     * Handle a long click on a favourite item. As Android's long click listener wishes to know if
     * the long click is consumed, this method propagates the return result from calling the
     * listener back to Android's listener.
     *
     * @return The result of calling [OnFavouriteItemClickListener.onFavouriteLongClicked], or
     * `false` if the favourite stop is `null`.
     */
    private fun handleLongClick() = favouriteStop?.let {
        clickListener.onFavouriteLongClicked(it.favouriteStop.stopCode)
    } ?: false
}