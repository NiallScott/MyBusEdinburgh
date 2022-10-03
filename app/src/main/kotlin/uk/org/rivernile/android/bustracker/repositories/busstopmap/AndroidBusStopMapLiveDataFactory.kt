/*
 * Copyright (C) 2018 - 2022 Niall 'Rivernile' Scott
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
import com.google.android.gms.maps.model.PolylineOptions
import uk.org.rivernile.android.bustracker.utils.ClearableLiveData
import uk.org.rivernile.android.bustracker.utils.Strings
import javax.inject.Inject

/**
 * An Android specific implementation of [BusStopMapLiveDataFactory].
 *
 * @author Niall Scott
 * @param context A [Context] instance.
 * @param strings A [Strings] instance.
 */
class AndroidBusStopMapLiveDataFactory @Inject constructor(
        private val context: Context,
        private val strings: Strings): BusStopMapLiveDataFactory {

    override fun createBusStopsLiveData(filteredServices: Array<String>?)
            : ClearableLiveData<Map<String, Stop>> =
            BusStopsLiveData(context, strings, filteredServices)

    override fun createBusStopLiveData(stopCode: String): ClearableLiveData<SelectedStop> =
            BusStopLiveData(context, stopCode)

    override fun createRouteLineLiveData(services: Array<String>?)
            : ClearableLiveData<Map<String, List<PolylineOptions>>> {
        return RouteLineLiveData(context, services)
    }
}