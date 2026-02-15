/*
 * Copyright (C) 2022 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops

import androidx.recyclerview.widget.RecyclerView
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopOrientation
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemNearestStopBinding

/**
 * A [RecyclerView.ViewHolder] which shows nearest stop items.
 *
 * @param viewBinding The view binding.
 * @param clickListener This listener is called when there are click events on this view holder.
 * @param stopMapMarkerDecorator Used to populate the stop marker based on the orientation.
 * @param textFormattingUtils Used to format stop name details.
 * @param directionStrings Direction content description [String]s. This is passed in here as a
 * caching mechanism.
 * @author Niall Scott
 */
class NearestStopViewHolder(
    private val viewBinding: ListItemNearestStopBinding,
    private val clickListener: OnNearStopItemClickListener,
    private val stopMapMarkerDecorator: StopMapMarkerDecorator,
    private val textFormattingUtils: TextFormattingUtils,
    private val directionStrings: Array<String>
) : RecyclerView.ViewHolder(viewBinding.root) {

    private var item: UiNearestStop? = null

    init {
        viewBinding.root.apply {
            setOnClickListener {
                handleClick()
            }

            setOnLongClickListener {
                handleLongClick()
            }
        }
    }

    /**
     * Populate this item with a [UiNearestStop].
     *
     * @param item The item to populate this [RecyclerView.ViewHolder] with.
     */
    fun populate(item: UiNearestStop?) {
        this.item = item

        viewBinding.apply {
            item?.let {
                root.isChecked = it.isSelected
                val orientation = it.orientation

                imgDirection.setImageResource(
                        stopMapMarkerDecorator.getStopDirectionDrawableResourceId(orientation))
                imgDirection.contentDescription = getDirectionString(orientation)

                text1.text = textFormattingUtils.formatBusStopNameWithStopCode(
                    it.stopIdentifier,
                    it.stopName
                )

                text2.text = it
                    .services
                    ?.ifEmpty { null }
                    ?.joinToString { service -> service.serviceName }
                    ?: viewBinding.root.context.getString(R.string.neareststops_no_services)

                txtDistance.text = viewBinding.root.context.getString(
                        R.string.neareststops_distance_format,
                        it.distance)
            } ?: run {
                root.isChecked = false
                imgDirection.setImageResource(0)
                imgDirection.contentDescription = null
                text1.text = null
                text2.text = null
                txtDistance.text = null
            }
        }
    }

    /**
     * Handle this item being clicked.
     */
    private fun handleClick() {
        item?.let(clickListener::onNearestStopClicked)
    }

    /**
     * Handle this item being long clicked.
     *
     * @return `true` if the long click was handled, otherwise `false`.
     */
    private fun handleLongClick() = item?.let {
        clickListener.onNearestStopLongClicked(it.stopIdentifier)
    } ?: false

    /**
     * Get the direction [String] for the given [orientation].
     *
     * @param orientation The orientation to get the string for.
     * @return The string for the orientation.
     */
    private fun getDirectionString(orientation: StopOrientation) = when (orientation) {
        StopOrientation.NORTH -> directionStrings[0]
        StopOrientation.NORTH_EAST -> directionStrings[1]
        StopOrientation.EAST -> directionStrings[2]
        StopOrientation.SOUTH_EAST -> directionStrings[3]
        StopOrientation.SOUTH -> directionStrings[4]
        StopOrientation.SOUTH_WEST -> directionStrings[5]
        StopOrientation.WEST -> directionStrings[6]
        StopOrientation.NORTH_WEST -> directionStrings[7]
        else -> viewBinding.root.context.getString(R.string.orientation_unknown)
    }
}
