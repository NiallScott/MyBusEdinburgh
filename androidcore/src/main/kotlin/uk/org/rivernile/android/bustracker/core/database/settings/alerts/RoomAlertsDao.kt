/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * This is the Room-specific implementation of [AlertsDao].
 *
 * @author Niall Scott
 */
@Dao
internal abstract class RoomAlertsDao : AlertsDao {

    companion object {

        private const val ALERT_TYPE_PROXIMITY = 1
        private const val ALERT_TYPE_ARRIVAL = 2
    }

    override suspend fun addArrivalAlert(arrivalAlert: ArrivalAlertEntity) {
        addAlertInternal(mapToRoomAlertEntity(arrivalAlert))
    }

    override suspend fun addProximityAlert(proximityAlert: ProximityAlertEntity) {
        addAlertInternal(mapToRoomAlertEntity(proximityAlert))
    }

    @Query("""
        DELETE FROM active_alerts 
        WHERE id = :id 
        AND type = $ALERT_TYPE_ARRIVAL
    """)
    abstract override suspend fun removeArrivalAlert(id: Int)

    @Query("""
        DELETE FROM active_alerts 
        WHERE stopCode = :stopCode 
        AND type = $ALERT_TYPE_ARRIVAL
    """)
    abstract override suspend fun removeArrivalAlert(stopCode: String)

    @Query("""
        DELETE FROM active_alerts 
        WHERE type = $ALERT_TYPE_ARRIVAL
    """)
    abstract override suspend fun removeAllArrivalAlerts()

    @Query("""
        DELETE FROM active_alerts 
        WHERE id = :id 
        AND type = $ALERT_TYPE_PROXIMITY
    """)
    abstract override suspend fun removeProximityAlert(id: Int)

    @Query("""
        DELETE FROM active_alerts 
        WHERE stopCode = :stopCode 
        AND type = $ALERT_TYPE_PROXIMITY
    """)
    abstract override suspend fun removeProximityAlert(stopCode: String)

    @Query("""
        DELETE FROM active_alerts 
        WHERE type = $ALERT_TYPE_PROXIMITY
    """)
    abstract override suspend fun removeAllProximityAlerts()

    override fun getHasArrivalAlertFlow(stopCode: String) =
        getHasArrivalAlertFlowInternal(stopCode)
            .map { it > 0 }

    override fun getHasProximityAlertFlow(stopCode: String) =
        getHasProximityAlertFlowInternal(stopCode)
            .map { it > 0 }

    override val allAlertsFlow get() =
        allAlertsFlowInternal
            .map(this::mapToAlertEntities)

    override suspend fun getProximityAlert(id: Int) =
        mapToProximityAlertEntity(getProximityAlertInternal(id))

    override suspend fun getAllArrivalAlerts() =
        getAllArrivalAlertsInternal()
            ?.mapNotNull(this::mapToArrivalAlertEntity)
            ?.ifEmpty { null }

    @Query("""
        SELECT DISTINCT stopCode 
        FROM active_alerts 
        WHERE type = $ALERT_TYPE_ARRIVAL
    """)
    abstract override suspend fun getAllArrivalAlertStopCodes(): List<String>?

    @Query("""
        SELECT COUNT(*) 
        FROM active_alerts 
        WHERE type = $ALERT_TYPE_ARRIVAL
    """)
    abstract override suspend fun getArrivalAlertCount(): Int

    @get:Query("""
        SELECT COUNT(*) 
        FROM active_alerts 
        WHERE type = $ALERT_TYPE_ARRIVAL
    """)
    abstract override val arrivalAlertCountFlow: Flow<Int>

    override val allProximityAlertsFlow get() =
        allProximityAlertsFlowInternal
            .map { proximityAlerts ->
                proximityAlerts
                    ?.mapNotNull(this::mapToProximityAlertEntity)
                    ?.ifEmpty { null }
            }

    @Query("""
        SELECT COUNT(*) 
        FROM active_alerts 
        WHERE type = $ALERT_TYPE_PROXIMITY
    """)
    abstract override suspend fun getProximityAlertCount(): Int

    @Insert
    abstract suspend fun addAlertInternal(alert: RoomAlertEntity)

    @Query("""
        SELECT COUNT(*) 
        FROM active_alerts 
        WHERE stopCode = :stopCode 
        AND type = $ALERT_TYPE_ARRIVAL
    """)
    abstract fun getHasArrivalAlertFlowInternal(stopCode: String): Flow<Int>

    @Query("""
        SELECT COUNT(*) 
        FROM active_alerts 
        WHERE stopCode = :stopCode 
        AND type = $ALERT_TYPE_PROXIMITY
    """)
    abstract fun getHasProximityAlertFlowInternal(stopCode: String): Flow<Int>

    @get:Query("""
        SELECT id, timeAdded, stopCode, type, distanceFrom, serviceNames, timeTrigger 
        FROM active_alerts 
        ORDER BY timeAdded ASC
    """)
    abstract val allAlertsFlowInternal: Flow<List<RoomAlertEntity>?>

    @Query("""
        SELECT id, timeAdded, stopCode, type, distanceFrom, serviceNames, timeTrigger 
        FROM active_alerts 
        WHERE id = :id 
        AND type = $ALERT_TYPE_PROXIMITY 
        LIMIT 1
    """)
    abstract suspend fun getProximityAlertInternal(id: Int): RoomAlertEntity?

    @Query("""
        SELECT id, timeAdded, stopCode, type, distanceFrom, serviceNames, timeTrigger 
        FROM active_alerts 
        WHERE type = $ALERT_TYPE_ARRIVAL
    """)
    abstract suspend fun getAllArrivalAlertsInternal(): List<RoomAlertEntity>?

    @get:Query("""
        SELECT id, timeAdded, stopCode, type, distanceFrom, serviceNames, timeTrigger 
        FROM active_alerts 
        WHERE type = $ALERT_TYPE_PROXIMITY
    """)
    abstract val allProximityAlertsFlowInternal: Flow<List<RoomAlertEntity>?>

    /**
     * Map the given [ArrivalAlertEntity] to a [RoomAlertEntity].
     *
     * @param arrivalAlert The [ArrivalAlertEntity] to map.
     * @return The [ArrivalAlertEntity] mapped to a [RoomAlertEntity].
     */
    private fun mapToRoomAlertEntity(arrivalAlert: ArrivalAlertEntity) =
        RoomAlertEntity(
            0,
            ALERT_TYPE_ARRIVAL,
            arrivalAlert.timeAdded,
            arrivalAlert.stopCode,
            null,
            arrivalAlert.serviceNames.joinToString(","),
            arrivalAlert.timeTrigger)

    /**
     * Map the given [ProximityAlertEntity] to a [RoomAlertEntity].
     *
     * @param proximityAlert The [ProximityAlertEntity] to map.
     * @return The [ProximityAlertEntity] mapped to a [RoomAlertEntity].
     */
    private fun mapToRoomAlertEntity(proximityAlert: ProximityAlertEntity) =
        RoomAlertEntity(
            0,
            ALERT_TYPE_PROXIMITY,
            proximityAlert.timeAdded,
            proximityAlert.stopCode,
            proximityAlert.distanceFrom,
            null,
            null)

    /**
     * Map the given [List] of [RoomAlertEntity]s to a [List] of [AlertEntity]s.
     *
     * @param alerts The [RoomAlertEntity]s to map.
     * @return The [List] of [RoomAlertEntity]s mapped to a [List] of [AlertEntity]s.
     */
    private fun mapToAlertEntities(alerts: List<RoomAlertEntity>?): List<AlertEntity>? =
        alerts
            ?.mapNotNull(this::mapToAlertEntity)
            ?.ifEmpty { null }

    /**
     * Map the given [RoomAlertEntity] to an [AlertEntity].
     *
     * @param alert The [RoomAlertEntity] to map.
     * @return The [RoomAlertEntity] mapped to an [AlertEntity].
     */
    private fun mapToAlertEntity(alert: RoomAlertEntity): AlertEntity? {
        return when (alert.type) {
            ALERT_TYPE_PROXIMITY -> mapToProximityAlertEntity(alert)
            ALERT_TYPE_ARRIVAL -> mapToArrivalAlertEntity(alert)
            else -> null
        }
    }

    /**
     * Map the given [RoomAlertEntity] to a [ArrivalAlertEntity].
     *
     * @param alert The [RoomAlertEntity] to map.
     * @return The [RoomAlertEntity] mapped to a [ArrivalAlertEntity].
     */
    private fun mapToArrivalAlertEntity(alert: RoomAlertEntity?): ArrivalAlertEntity? {
        return alert?.let {
            val serviceNames = it
                .serviceNames
                ?.split(',')
                ?.map(String::trim)
                ?: return null
            val timeTrigger = it.timeTrigger ?: return null

            return ArrivalAlertEntity(
                it.id,
                it.timeAdded,
                it.stopCode,
                serviceNames,
                timeTrigger)
        }
    }

    /**
     * Map the given [RoomAlertEntity] to a [ProximityAlertEntity].
     *
     * @param alert The [RoomAlertEntity] to map.
     * @return The [RoomAlertEntity] mapped to a [ProximityAlertEntity].
     */
    private fun mapToProximityAlertEntity(alert: RoomAlertEntity?): ProximityAlertEntity? {
        return alert?.let {
            val distanceFrom = it.distanceFrom ?: return null

            return ProximityAlertEntity(
                it.id,
                it.timeAdded,
                it.stopCode,
                distanceFrom)
        }
    }
}