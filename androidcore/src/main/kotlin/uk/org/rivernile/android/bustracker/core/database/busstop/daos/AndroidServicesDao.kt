/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.daos

import android.content.Context
import android.database.ContentObserver
import android.graphics.Color
import android.os.Handler
import android.os.Looper
import uk.org.rivernile.android.bustracker.core.database.busstop.ServicesContract
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This is an Android concrete implementation of the [ServicesDao].
 *
 * @param context The application [Context].
 * @param contract The database contract, so we know how to talk to it.
 * @author Niall Scott
 */
@Singleton
internal class AndroidServicesDao @Inject constructor(
        private val context: Context,
        private val contract: ServicesContract): ServicesDao {

    private val listeners = mutableListOf<ServicesDao.OnServicesChangedListener>()
    private val observer = Observer()

    override fun addOnServicesChangedListener(listener: ServicesDao.OnServicesChangedListener) {
        synchronized(listeners) {
            listeners.apply {
                add(listener)

                if (size == 1) {
                    context.contentResolver.registerContentObserver(contract.getContentUri(), true,
                            observer)
                }
            }
        }
    }

    override fun removeOnServicesChangedListener(listener: ServicesDao.OnServicesChangedListener) {
        synchronized(listeners) {
            listeners.apply {
                remove(listener)

                if (isEmpty()) {
                    context.contentResolver.unregisterContentObserver(observer)
                }
            }
        }
    }

    override fun getColoursForServices(services: Array<String>?): Map<String, Int>? {
        var selection = "${ServicesContract.COLOUR} IS NOT NULL"

        val selectionArgs = services?.ifEmpty { null }?.let {
            selection += " AND ${ServicesContract.NAME} IN " +
                    "(${generateInClausePlaceholders(it.size)})"
            services
        }

        return context.contentResolver.query(
                contract.getContentUri(),
                arrayOf(
                        ServicesContract.NAME,
                        ServicesContract.COLOUR),
                selection,
                selectionArgs,
                null)?.use {
            // Fill the Cursor window.
            val count = it.count
            val nameColumn = it.getColumnIndex(ServicesContract.NAME)
            val colourColumn = it.getColumnIndex(ServicesContract.COLOUR)
            val result = HashMap<String, Int>(count)

            while (it.moveToNext()) {
                val name = it.getString(nameColumn)
                it.getString(colourColumn)?.let { c ->
                    try {
                        Color.parseColor(c)
                    } catch (ignored: IllegalArgumentException) {
                        null
                    }
                }?.let { colourInt ->
                    result[name] = colourInt
                }
            }

            result.ifEmpty { null }
        }
    }

    /**
     * Generate placeholders for a `WHERE` clause that contains an `IN` element.
     *
     * @param length The number of placeholders required.
     * @return A [String] containing the required number of placeholders for an `IN` element.
     */
    private fun generateInClausePlaceholders(length: Int) =
            Array(length) { "?" }
                    .joinToString(",")

    /**
     * For all of the currently registers listeners, dispatch an alert change to them.
     */
    private fun dispatchOnBusStopsChangedListeners() {
        synchronized(listeners) {
            listeners.forEach { listener ->
                listener.onServicesChanged()
            }
        }
    }

    /**
     * This inner class is used as the [ContentObserver] for observing changes to alerts.
     */
    private inner class Observer : ContentObserver(Handler(Looper.getMainLooper())) {

        override fun deliverSelfNotifications() = true

        override fun onChange(selfChange: Boolean) {
            dispatchOnBusStopsChangedListeners()
        }
    }
}