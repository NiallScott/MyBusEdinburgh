/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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

import android.view.View
import android.widget.TextView
import uk.org.rivernile.edinburghbustracker.android.R
import java.text.DateFormat
import java.util.Date
import javax.inject.Inject

/**
 * This class is used to populate the common [TextView]s in [LiveTimesAdapter]. This implementation
 * is shared between the view holders so that the same type of field is populated in the same way.
 *
 * @author Niall Scott
 */
class ViewHolderFieldPopulator @Inject constructor() {

    companion object {

        private const val DUE_CUTOFF = 2
        private const val MINUTES_CUTOFF = 59
    }

    private val busTimeFormat = DateFormat.getTimeInstance(DateFormat.SHORT)

    /**
     * Populate the destination [TextView]. This caters for cases such as diversions, and when the
     * item is `null`.
     *
     * @param textView The [TextView] to populate the data.
     * @param item The item to use as the data source.
     */
    fun populateDestination(textView: TextView, item: UiLiveTimesItem?) {
        item?.let {
            val vehicle = it.vehicle
            val destination = vehicle.destination

            if (vehicle.isDiverted) {
                if (destination?.isNotEmpty() == true) {
                    textView.text = textView.context.getString(
                            R.string.bustimes_diverted_with_destination, destination)
                } else {
                    textView.setText(R.string.bustimes_diverted)
                }
            } else {
                textView.text = destination
            }
        } ?: run {
            textView.text = null
        }
    }

    /**
     * Populate the time [TextView]. This copes with diverted vehicles, and correctly displays the
     * time depending on departure time of the vehicle.
     *
     * @param textView The [TextView] to populate the data.
     * @param item The item to use as the data source.
     */
    fun populateTime(textView: TextView, item: UiLiveTimesItem?) {
        item?.let {
            val vehicle = it.vehicle

            if (!vehicle.isDiverted) {
                val minutes = vehicle.departureMinutes
                val timeToDisplay = when {
                    minutes > MINUTES_CUTOFF -> busTimeFormat
                        .format(Date(vehicle.departureTime.toEpochMilliseconds()))
                    minutes < DUE_CUTOFF -> textView.context.getString(R.string.bustimes_due)
                    else -> minutes.toString()
                }

                textView.text = if (vehicle.isEstimatedTime) {
                    textView.context.getString(R.string.bustimes_estimated_time, timeToDisplay)
                } else {
                    timeToDisplay
                }

                textView.visibility = View.VISIBLE
            } else {
                textView.text = null
                textView.visibility = View.GONE
            }
        } ?: run {
            textView.text = null
            textView.visibility = View.GONE
        }
    }
}
