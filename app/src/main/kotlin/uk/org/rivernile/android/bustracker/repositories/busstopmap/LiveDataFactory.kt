/*
 * Copyright (C) 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.repositories.busstopmap

import android.content.Context
import uk.org.rivernile.android.bustracker.utils.ClearableLiveData
import uk.org.rivernile.android.bustracker.utils.Strings
import javax.inject.Inject

/**
 * This class is used to create instances of [android.arch.lifecycle.LiveData] objects required by
 * the stop map.
 *
 * @author Niall Scott
 * @param context A [Context] instance.
 * @param strings A [Strings] instance.
 */
class LiveDataFactory @Inject constructor(private val context: Context,
                                          private val strings: Strings) {

    /**
     * Obtain a new instance of a [ClearableLiveData] object which is able to get all service names.
     *
     * @return A new [ClearableLiveData] for getting all service names.
     */
    internal fun createServiceNamesLiveData(): ClearableLiveData<Array<String>> =
            ServiceNamesLiveData(context)

    /**
     * Obtain a new instance of a [ClearableLiveData] object which is able to get stops based on an
     * optional service filter.
     *
     * @param filteredServices An optional array of [String] services to filter stops on.
     * @return A new [ClearableLiveData] for getting stops.
     */
    internal fun createBusStopsLiveData(filteredServices: Array<String>?)
            : ClearableLiveData<Map<String, Stop>> =
            BusStopsLiveData(context, strings, filteredServices)

    /**
     * Obtain a new instance of a [ClearableLiveData] object which is able to get a stop based on
     * its `stopCode`.
     *
     * @param stopCode The stop code of the stop.
     * @return A new [ClearableLiveData] for getting the given stop.
     */
    internal fun createBusStopLiveData(stopCode: String): ClearableLiveData<SelectedStop> =
            BusStopLiveData(context, stopCode)
}