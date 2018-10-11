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

import android.annotation.SuppressLint
import android.content.Context
import android.database.Cursor
import android.graphics.Color
import android.support.annotation.ColorInt
import android.text.TextUtils
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions
import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract
import uk.org.rivernile.android.bustracker.database.busstop.BusStopDatabase
import uk.org.rivernile.android.bustracker.utils.CursorLiveData
import java.lang.IllegalArgumentException
import java.util.Collections

/**
 * This [CursorLiveData] loads route lines for the supplied services from the database.
 *
 * @author Niall Scott
 * @param context A [Context] instance.
 * @param services An optional array of service names to load route lines for. If this is null, no
 * route lines will be loaded and `null` will be returned.
 */
internal class RouteLineLiveData(private val context: Context,
                                 private val services: Array<String>?)
    : CursorLiveData<Map<String, List<PolylineOptions>>>() {

    override fun onBeginObservingCursor() {
        context.contentResolver.registerContentObserver(BusStopContract.ServicePoints.CONTENT_URI,
                false, contentObserver)
    }

    @SuppressLint("Recycle")
    override fun loadCursor() = services?.let {
        context.contentResolver.query(
                BusStopContract.ServicePoints.CONTENT_URI,
                arrayOf(BusStopContract.ServicePoints.SERVICE_NAME,
                        BusStopContract.ServicePoints.CHAINAGE,
                        BusStopContract.ServicePoints.LATITUDE,
                        BusStopContract.ServicePoints.LONGITUDE),
                BusStopContract.ServicePoints.SERVICE_NAME + " IN (" +
                        BusStopDatabase.generateInPlaceholders(it.size) + ')',
                it,
                BusStopContract.ServicePoints.SERVICE_NAME + " ASC, " +
                        BusStopContract.ServicePoints.CHAINAGE + " ASC, " +
                        BusStopContract.ServicePoints.ORDER_VALUE + " ASC",
                cancellationSignal)
    }

    override fun processCursor(cursor: Cursor?): Map<String, List<PolylineOptions>>? {
        if (cursor == null || cursor.count < 1) {
            return null
        }

        val result = HashMap<String, List<PolylineOptions>>()
        val columnServiceName = cursor.getColumnIndex(BusStopContract.ServicePoints.SERVICE_NAME)
        val columnChainage = cursor.getColumnIndex(BusStopContract.ServicePoints.CHAINAGE)
        val columnLatitude = cursor.getColumnIndex(BusStopContract.ServicePoints.LATITUDE)
        val columnLongitude = cursor.getColumnIndex(BusStopContract.ServicePoints.LONGITUDE)
        cursor.moveToPosition(-1)
        val serviceColours = BusStopDatabase.getServiceColours(context, services)

        var currentService = ""
        var currentPolylineList: ArrayList<PolylineOptions>? = null
        var polylineOptions: PolylineOptions? = null
        var currentColour = Color.BLACK
        var currentChainage = -1

        while (cursor.moveToNext()) {
            val service = cursor.getString(columnServiceName)

            if (currentService != service) {
                currentService = service
                currentPolylineList = ArrayList()
                result[currentService] = Collections.unmodifiableList(currentPolylineList)
                currentColour = getColourForService(serviceColours, service)
                currentChainage = -1
            }

            val chainage = cursor.getInt(columnChainage)

            if (chainage != currentChainage) {
                polylineOptions = PolylineOptions().color(currentColour)
                currentChainage = chainage
                currentPolylineList?.add(polylineOptions)
            }

            polylineOptions?.add(LatLng(
                    cursor.getDouble(columnLatitude),
                    cursor.getDouble(columnLongitude)))
        }

        return Collections.unmodifiableMap(result)
    }

    override fun onStopObservingCursor() {
        context.contentResolver.unregisterContentObserver(contentObserver)
    }

    /**
     * Get the colour the line should be for a specific service.
     *
     * @param colours The pre-loaded mapping of service name to colour hex.
     * @param serviceName The service name to get the colour for.
     * @return The colour for the service, represented as as an integer. If the colour could not be
     * found for a service, a default colour will be returned.
     */
    @ColorInt
    private fun getColourForService(colours: Map<String, String>?, serviceName: String?): Int {
        if (colours != null && !colours.isEmpty() && !TextUtils.isEmpty(serviceName)) {
            val hex = colours[serviceName]

            if (!TextUtils.isEmpty(hex)) {
                try {
                    return Color.parseColor(hex)
                } catch (ignored: IllegalArgumentException) {
                    // Fall through to return statement.
                }
            }
        }

        return Color.BLACK
    }
}