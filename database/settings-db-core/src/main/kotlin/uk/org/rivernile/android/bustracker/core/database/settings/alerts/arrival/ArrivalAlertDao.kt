/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival

import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier

/**
 * This DAO is used to access arrival alerts.
 *
 * @author Niall Scott
 */
public interface ArrivalAlertDao {

    /**
     * Add a new arrival alert.
     *
     * @param arrivalAlert The alert to add.
     */
    public suspend fun addArrivalAlert(arrivalAlert: ArrivalAlert)

    /**
     * Remove an arrival alert.
     *
     * @param id The ID of the arrival alert to remove.
     */
    public suspend fun removeArrivalAlert(id: Int)

    /**
     * Remove an arrival alert by stop code.
     *
     * @param stopIdentifier The stop to remove the arrival alert for.
     */
    public suspend fun removeArrivalAlert(stopIdentifier: StopIdentifier)

    /**
     * Remove all arrival alerts.
     */
    public suspend fun removeAllArrivalAlerts()

    /**
     * Get a [Flow] which emits whether there is an arrival alert set for the given
     * [stopIdentifier].
     *
     * @param stopIdentifier The stop to check for active arrival alerts.
     * @return A [Flow] which emits whether there is an arrival alert set for the given
     * [stopIdentifier].
     */
    public fun getHasArrivalAlertFlow(stopIdentifier: StopIdentifier): Flow<Boolean>

    /**
     * Get all the arrival alerts.
     *
     * @return All the arrival alerts.
     */
    public suspend fun getAllArrivalAlerts(): List<ArrivalAlert>?

    /**
     * A [Flow] which emits all know [ArrivalAlert]s.
     */
    public val allArrivalAlertsFlow: Flow<List<ArrivalAlert>?>

    /**
     * Get all the stops that have arrival alerts against them.
     *
     * @return A [Set] of [StopIdentifier]s with arrival alerts.
     */
    public suspend fun getAllArrivalAlertStops(): Set<StopIdentifier>?

    /**
     * A [Flow] which emits all the stop identifiers which have arrival alerts set.
     */
    public val allArrivalAlertStopsFlow: Flow<List<StopIdentifier>?>

    /**
     * Get the number of current arrival alerts.
     *
     * @return The number of current arrival alerts.
     */
    public suspend fun getArrivalAlertCount(): Int

    /**
     * A [Flow] which emits the number of active arrival alerts.
     */
    public val arrivalAlertCountFlow: Flow<Int>
}
