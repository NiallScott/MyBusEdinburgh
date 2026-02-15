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

package uk.org.rivernile.android.bustracker.core.database.settings.alerts.proximity

import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier

/**
 * This DAO is used to access proximity alerts.
 *
 * @author Niall Scott
 */
public interface ProximityAlertDao {

    /**
     * Add a new proximity alert.
     *
     * @param proximityAlert The alert to add.
     */
    public suspend fun addProximityAlert(proximityAlert: ProximityAlert)

    /**
     * Remove a proximity alert.
     *
     * @param id The ID of the proximity alert to remove.
     */
    public suspend fun removeProximityAlert(id: Int)

    /**
     * Remove a proximity alert by stop identifier.
     *
     * @param stopIdentifier The stop to remove the proximity alert for.
     */
    public suspend fun removeProximityAlert(stopIdentifier: StopIdentifier)

    /**
     * Remove all proximity alerts.
     */
    public suspend fun removeAllProximityAlerts()

    /**
     * Get a [Flow] which emits whether there is a proximity alert set for the given
     * [stopIdentifier].
     *
     * @param stopIdentifier The stop to check for active proximity alerts.
     * @return A [Flow] which emits whether there is a proximity alert set for the given
     * [stopIdentifier].
     */
    public fun getHasProximityAlertFlow(stopIdentifier: StopIdentifier): Flow<Boolean>

    /**
     * Get an active proximity alert.
     *
     * @param id The ID of the proximity alert.
     * @return The [ProximityAlert], or `null` if it doesn't exist.
     */
    public suspend fun getProximityAlert(id: Int): ProximityAlert?

    /**
     * A [Flow] which emits all active proximity alerts.
     */
    public val allProximityAlertsFlow: Flow<List<ProximityAlert>?>

    /**
     * A [Flow] which emits all the stop identifiers which have proximity alerts set.
     */
    public val allProximityAlertStopsFlow: Flow<List<StopIdentifier>?>

    /**
     * Get the number of current proximity alerts.
     *
     * @return The number of current proximity alerts.
     */
    public suspend fun getProximityAlertCount(): Int
}
