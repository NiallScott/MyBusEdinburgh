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

package uk.org.rivernile.android.bustracker.ui.search

import androidx.recyclerview.widget.RecyclerView
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.SearchListItemBinding

/**
 * A [RecyclerView.ViewHolder] which shows search result items.
 *
 * @param viewBinding The view binding.
 * @param clickListener This listener is called when there are click events on this view holder.
 * @param stopMapMarkerDecorator Used to populate the stop marker based on the orientation.
 * @param textFormattingUtils Used to format stop name details.
 * @param directionStrings Direction content description [String]s. This is passed in here as a
 * caching mechanism.
 * @author Niall Scott
 */
class SearchResultViewHolder(
        private val viewBinding: SearchListItemBinding,
        private val clickListener: OnItemClickedListener,
        private val stopMapMarkerDecorator: StopMapMarkerDecorator,
        private val textFormattingUtils: TextFormattingUtils,
        private val directionStrings: Array<String>)
    : RecyclerView.ViewHolder(viewBinding.root) {

    private var item: UiSearchResult? = null

    init {
        viewBinding.root.setOnClickListener {
            handleClick()
        }
    }

    /**
     * Populate this item with a [UiSearchResult].
     *
     * @param item The item to populate this [RecyclerView.ViewHolder] with.
     */
    fun populate(item: UiSearchResult?) {
        this.item = item

        viewBinding.apply {
            item?.let {
                val orientation = it.orientation

                imgDirection.setImageResource(
                        stopMapMarkerDecorator.getStopDirectionDrawableResourceId(orientation))

                text1.text = textFormattingUtils.formatBusStopNameWithStopCode(
                        it.stopCode,
                        it.stopName)

                text2.text = it.services?.ifBlank { null }
                        ?: viewBinding.root.context.getString(R.string.search_item_no_services)

                if (orientation >= 0 && orientation < directionStrings.size) {
                    directionStrings[orientation]
                } else {
                    viewBinding.root.context.getString(R.string.orientation_unknown)
                }.let(imgDirection::setContentDescription)
            } ?: run {
                imgDirection.setImageResource(0)
                imgDirection.contentDescription = null
                text1.text = null
                text2.text = null
            }
        }
    }

    /**
     * Handle this item being clicked.
     */
    private fun handleClick() {
        item?.let(clickListener::onItemClicked)
    }
}