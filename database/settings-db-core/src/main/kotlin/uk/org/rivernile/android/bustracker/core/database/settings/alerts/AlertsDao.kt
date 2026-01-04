/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.settings.alerts

import kotlinx.coroutines.flow.Flow

/**
 * This DAO is used to access alerts.
 *
 * @author Niall Scott
 */
interface AlertsDao {

    /**
     * Add a new arrival alert.
     *
     * @param arrivalAlert The alert to add.
     */
    suspend fun addArrivalAlert(arrivalAlert: ArrivalAlertEntity)

    /**
     * Add a new proximity alert.
     *
     * @param proximityAlert The alert to add.
     */
    suspend fun addProximityAlert(proximityAlert: ProximityAlertEntity)

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
     * Get a [Flow] which emits whether there is an arrival alert set for the given [stopCode].
     *
     * @param stopCode The stop code to check for active arrival alerts.
     * @return A [Flow] which emits whether there is an arrival alert set for the given [stopCode].
     */
    fun getHasArrivalAlertFlow(stopCode: String): Flow<Boolean>

    /**
     * Get a [Flow] which emits whether there is a proximity alert set for the given [stopCode].
     *
     * @param stopCode The stop code to check for active proximity alerts.
     * @return A [Flow] which emits whether there is a proximity alert set for the given [stopCode].
     */
    fun getHasProximityAlertFlow(stopCode: String): Flow<Boolean>

    /**
     * A [Flow] which emits all active alerts. This will be `null` when no alerts are set.
     */
    val allAlertsFlow: Flow<List<AlertEntity>?>

    /**
     * Get an active proximity alert.
     *
     * @param id The ID of the proximity alert.
     * @return The [ProximityAlertEntity], or `null` if it doesn't exist.
     */
    suspend fun getProximityAlert(id: Int): ProximityAlertEntity?

    /**
     * Get all the arrival alerts.
     *
     * @return All the arrival alerts.
     */
    suspend fun getAllArrivalAlerts(): List<ArrivalAlertEntity>?

    /**
     * Get all the stop codes that have arrival alerts against them.
     *
     * @return A [Set] of stop codes with arrival alerts.
     */
    suspend fun getAllArrivalAlertStopCodes(): List<String>?

    /**
     * Get the number of current arrival alerts.
     *
     * @return The number of current arrival alerts.
     */
    suspend fun getArrivalAlertCount(): Int

    /**
     * A [Flow] which emits all the stop codes which have arrival alerts set.
     */
    val arrivalAlertStopCodesFlow: Flow<List<String>?>

    /**
     * A [Flow] which emits the number of active arrival alerts.
     */
    val arrivalAlertCountFlow: Flow<Int>

    /**
     * A [Flow] which emits all the stop codes which have proximity alerts set.
     */
    val proximityAlertStopCodesFlow: Flow<List<String>?>

    /**
     * A [Flow] which emits all active proximity alerts.
     */
    val allProximityAlertsFlow: Flow<List<ProximityAlertEntity>?>

    /**
     * Get the number of current proximity alerts.
     *
     * @return The number of current proximity alerts.
     */
    suspend fun getProximityAlertCount(): Int
}
