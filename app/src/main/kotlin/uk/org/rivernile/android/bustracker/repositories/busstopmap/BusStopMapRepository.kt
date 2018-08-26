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

import uk.org.rivernile.android.bustracker.utils.ClearableLiveData

/**
 * A repository for providing data for the stop map.
 *
 * @author Niall Scott
 * @param liveDataFactory A factory for creating required [android.arch.lifecycle.LiveData]
 * instances.
 */
class BusStopMapRepository(private val liveDataFactory: LiveDataFactory) {

    /**
     * Get a [ClearableLiveData] instance for getting service names.
     *
     * @return A [ClearableLiveData] instance for getting service names.
     */
    fun getServiceNames() = liveDataFactory.createServiceNamesLiveData()

    /**
     * Get a [ClearableLiveData] instance for getting stops to display on the map.
     *
     * @param filteredServices An optional filter to supply. Only stops which are associated with
     * services in this array are returned.
     * @return A [ClearableLiveData] instance for getting stops to display on the map.
     */
    fun getBusStops(filteredServices: Array<String>?): ClearableLiveData<Map<String, Stop>> =
            liveDataFactory.createBusStopsLiveData(filteredServices)

    /**
     * Get a [ClearableLiveData] for getting data for a given stop.
     *
     * @param stopCode The code of the stop to return.
     * @return A [ClearableLiveData] for getting data for a given stop.
     */
    fun getBusStop(stopCode: String): ClearableLiveData<SelectedStop>? =
            liveDataFactory.createBusStopLiveData(stopCode)
}