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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier

/**
 * This is the Room-specific implementation of [ProximityAlertDao].
 *
 * @author Niall Scott
 */
@Dao
internal abstract class RoomProximityAlertDao : ProximityAlertDao {

    @Transaction
    override suspend fun addProximityAlert(proximityAlert: ProximityAlert) {
        addProximityAlertInternal(proximityAlert.toProximityAlertEntity())
    }

    @Query("""
        DELETE FROM proximity_alert
        WHERE id = :id
    """)
    abstract override suspend fun removeProximityAlert(id: Int)

    @Query("""
        DELETE FROM proximity_alert
        WHERE stop_code = :stopIdentifier
    """)
    abstract override suspend fun removeProximityAlert(stopIdentifier: StopIdentifier)

    @Query("""
        DELETE FROM proximity_alert
    """)
    abstract override suspend fun removeAllProximityAlerts()

    override fun getHasProximityAlertFlow(stopIdentifier: StopIdentifier): Flow<Boolean> {
        return getHasProximityAlertFlowInternal(stopIdentifier)
            .map { it > 0 }
            .onStart { deleteOldAlerts() }
    }

    @Transaction
    override suspend fun getProximityAlert(id: Int): RoomProximityAlert? {
        deleteOldAlerts()
        return getProximityAlertInternal(id)
    }

    override val allProximityAlertsFlow: Flow<List<ProximityAlert>> get() =
        allProximityAlertsFlowInternal
            .onStart { deleteOldAlerts() }

    override val allProximityAlertStopsFlow: Flow<List<StopIdentifier>?> get() =
        allProximityAlertStopsFlowInternal
            .onStart { deleteOldAlerts() }

    @Transaction
    override suspend fun getProximityAlertCount(): Int {
        deleteOldAlerts()
        return getProximityAlertCountInternal()
    }

    @Insert
    abstract suspend fun addProximityAlertInternal(proximityAlert: RoomProximityAlertEntity)

    @Query("""
        SELECT COUNT(*)
        FROM proximity_alert
        WHERE stop_code = :stopIdentifier
    """)
    abstract fun getHasProximityAlertFlowInternal(stopIdentifier: StopIdentifier): Flow<Int>

    @Query("""
        SELECT id, time_added_millis, stop_code, radius_trigger_meters
        FROM proximity_alert
        WHERE id = :id
    """)
    abstract suspend fun getProximityAlertInternal(id: Int): RoomProximityAlert?

    @get:Query("""
        SELECT id, time_added_millis, stop_code, radius_trigger_meters
        FROM proximity_alert
    """)
    abstract val allProximityAlertsFlowInternal: Flow<List<RoomProximityAlert>>

    @get:Query("""
        SELECT DISTINCT stop_code
        FROM proximity_alert
    """)
    abstract val allProximityAlertStopsFlowInternal: Flow<List<StopIdentifier>?>

    @Query("""
        SELECT COUNT(*)
        FROM proximity_alert
    """)
    abstract suspend fun getProximityAlertCountInternal(): Int

    @Query("""
        DELETE FROM proximity_alert
        WHERE time_added_millis < ((SELECT strftime('%s','now') * 1000) - 3600000)
    """)
    abstract suspend fun deleteOldAlerts()
}
