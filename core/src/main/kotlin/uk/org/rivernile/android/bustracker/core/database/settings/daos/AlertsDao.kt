/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.settings.daos

import uk.org.rivernile.android.bustracker.core.database.settings.entities.Alert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert

/**
 * This DAO is used to access alerts created in the app.
 *
 * @author Niall Scott
 */
interface AlertsDao {

    /**
     * Add a new [OnAlertsChangedListener] to be informed when the alerts data has been changed.
     *
     * @param listener The listener to add.
     */
    fun addOnAlertsChangedListener(listener: OnAlertsChangedListener)

    /**
     * Remove a [OnAlertsChangedListener] so it is no longer informed that alerts have been changed.
     *
     * @param listener The listener to remove.
     */
    fun removeOnAlertsChangedListener(listener: OnAlertsChangedListener)

    /**
     * Add a new arrival alert to the database.
     *
     * @param arrivalAlert The alert to add.
     * @return The ID of the new added arrival alert.
     */
    suspend fun addArrivalAlert(arrivalAlert: ArrivalAlert): Long

    /**
     * Add a new proximity alert to the database.
     *
     * @param proximityAlert The alert to add.
     */
    suspend fun addProximityAlert(proximityAlert: ProximityAlert)

    /**
     * Remove an arrival alert.
     *
     * @param id The ID of the arrival alert to remove.
     */
    suspend fun removeArrivalAlert(id: Int)

    /**
     * Remove an arrival alert by stop code.
     *
     * @param stopCode The stop code to remove the arrival alert for.
     */
    suspend fun removeArrivalAlert(stopCode: String)

    /**
     * Remove all arrival alerts.
     */
    suspend fun removeAllArrivalAlerts()

    /**
     * Remove a proximity alert.
     *
     * @param id The ID of the proximity alert to remove.
     */
    suspend fun removeProximityAlert(id: Int)

    /**
     * Remove a proximity alert by stop code.
     *
     * @param stopCode The stop code to remove the proximity alert for.
     */
    suspend fun removeProximityAlert(stopCode: String)

    /**
     * Remove all proximity alerts.
     */
    suspend fun removeAllProximityAlerts()

    /**
     * Get all user-set active alerts.
     *
     * @return All user set active alerts.
     */
    suspend fun getAllAlerts(): List<Alert>?

    /**
     * Get an active proximity alert.
     *
     * @param id The ID of the proximity alert.
     * @return The [ProximityAlert], or `null` if it doesn't exist.
     */
    suspend fun getProximityAlert(id: Int): ProximityAlert?

    /**
     * Get all the arrival alerts.
     *
     * @return All the arrival alerts.
     */
    suspend fun getAllArrivalAlerts(): List<ArrivalAlert>?

    /**
     * Get all the stop codes that have arrival alerts against them.
     *
     * @return A [Set] of stop codes with arrival alerts.
     */
    suspend fun getAllArrivalAlertStopCodes(): Set<String>?

    /**
     * Get the number of current arrival alerts.
     *
     * @return The number of current arrival alerts.
     */
    suspend fun getArrivalAlertCount(): Int

    /**
     * Get all the proximity alerts.
     *
     * @return All the proximity alerts.
     */
    fun getAllProximityAlerts(): List<ProximityAlert>?

    /**
     * Get the number of current proximity alerts.
     *
     * @return The number of current proximity alerts.
     */
    suspend fun getProximityAlertCount(): Int

    /**
     * Does the given `stopCode` have an arrival alert set?
     *
     * @param stopCode The stop code to check.
     * @return `true` if the given `stopCode` has an arrival alert set, otherwise `false`.
     */
    suspend fun hasArrivalAlert(stopCode: String): Boolean

    /**
     * Does the given `stopCode` have a proximity alert set?
     *
     * @param stopCode The stop code to check.
     * @return `true` if the given `stopCode` has a proximity alert set, otherwise `false`.
     */
    suspend fun hasProximityAlert(stopCode: String): Boolean

    /**
     * This interface should be implemented to listen for changes to alerts. Call
     * [addOnAlertsChangedListener] to register the listener.
     */
    interface OnAlertsChangedListener {

        /**
         * This is called when the alerts have changed.
         */
        fun onAlertsChanged()
    }
}