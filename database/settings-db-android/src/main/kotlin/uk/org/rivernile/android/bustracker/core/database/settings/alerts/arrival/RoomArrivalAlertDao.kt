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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier

/**
 * This is the Room-specific implementation of [ArrivalAlertDao].
 *
 * @author Niall Scott
 */
@Dao
internal abstract class RoomArrivalAlertDao : ArrivalAlertDao {

    @Transaction
    override suspend fun addArrivalAlert(arrivalAlert: ArrivalAlert) {
        val id = addArrivalAlert(arrivalAlert.toArrivalAlertEntity())
        addArrivalAlertServices(arrivalAlert.services.toArrivalAlertServiceEntityList(id))
    }

    @Query("""
        DELETE FROM arrival_alert
        WHERE id = :id
    """)
    abstract override suspend fun removeArrivalAlert(id: Int)

    @Query("""
        DELETE FROM arrival_alert
        WHERE stop_code = :stopIdentifier
    """)
    abstract override suspend fun removeArrivalAlert(stopIdentifier: StopIdentifier)

    @Query("""
        DELETE FROM arrival_alert
    """)
    abstract override suspend fun removeAllArrivalAlerts()

    override fun getHasArrivalAlertFlow(stopIdentifier: StopIdentifier): Flow<Boolean> {
        return getHasArrivalAlertFlowInternal(stopIdentifier)
            .map { it > 0 }
            .onStart { deleteOldAlerts() }
    }

    @Transaction
    @Query("""
        SELECT id, time_added_millis, stop_code, time_trigger_minutes
        FROM arrival_alert
    """)
    abstract override suspend fun getAllArrivalAlerts(): List<RoomArrivalAlert>

    override val allArrivalAlertsFlow get() = getAllArrivalAlertsFlowInternal()
        .onStart { deleteOldAlerts() }

    @Transaction
    override suspend fun getAllArrivalAlertStops(): Set<StopIdentifier> {
        deleteOldAlerts()
        return getAllArrivalAlertStopsInternal().toSet()
    }

    override val allArrivalAlertStopsFlow: Flow<List<StopIdentifier>?> get() =
        allArrivalAlertStopsFlowInternal
            .onStart { deleteOldAlerts() }

    @Transaction
    override suspend fun getArrivalAlertCount(): Int {
        deleteOldAlerts()
        return getArrivalAlertCountInternal()
    }

    override val arrivalAlertCountFlow: Flow<Int> get() =
        arrivalAlertCountFlowInternal
            .onStart { deleteOldAlerts() }

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun addArrivalAlert(arrivalAlert: RoomArrivalAlertEntity): Long

    @Insert
    abstract fun addArrivalAlertServices(services: List<RoomArrivalAlertServiceEntity>)

    @Query("""
        SELECT COUNT(*)
        FROM arrival_alert
        WHERE stop_code = :stopIdentifier
    """)
    abstract fun getHasArrivalAlertFlowInternal(stopIdentifier: StopIdentifier): Flow<Int>

    @Transaction
    @Query("""
        SELECT id, time_added_millis, stop_code, time_trigger_minutes
        FROM arrival_alert
    """)
    abstract fun getAllArrivalAlertsFlowInternal(): Flow<List<RoomArrivalAlert>>

    @Query("""
        SELECT DISTINCT stop_code
        FROM arrival_alert
    """)
    abstract suspend fun getAllArrivalAlertStopsInternal(): List<StopIdentifier>

    @get:Query("""
        SELECT DISTINCT stop_code
        FROM arrival_alert
    """)
    abstract val allArrivalAlertStopsFlowInternal: Flow<List<StopIdentifier>?>

    @Query("""
        SELECT COUNT(*)
        FROM arrival_alert
    """)
    abstract suspend fun getArrivalAlertCountInternal(): Int

    @get:Query("""
        SELECT COUNT(*)
        FROM arrival_alert
    """)
    abstract val arrivalAlertCountFlowInternal: Flow<Int>

    @Query("""
        DELETE FROM arrival_alert
        WHERE time_added_millis < ((SELECT strftime('%s','now') * 1000) - 3600000)
    """)
    abstract suspend fun deleteOldAlerts()
}
