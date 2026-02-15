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

package uk.org.rivernile.android.bustracker.core.database.busstop.servicestop

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.database.busstop.service.RoomServiceDescriptor

/**
 * This is the Room implementation of [ServiceStopDao].
 *
 * @author Niall Scott
 */
@Dao
internal interface RoomServiceStopDao : ServiceStopDao {

    @Query("""
        SELECT service.name AS name, service.operator_code AS operator_code
        FROM service_stop
        LEFT JOIN service_view AS service ON service_id = service.id
        WHERE stop_id = (
            SELECT id
            FROM stop
            WHERE naptan_code = :naptanStopCode
        )
        ORDER BY CASE WHEN service.name GLOB '[^0-9.]*' THEN
            service.name ELSE cast(service.name AS int) END
    """)
    abstract override fun getServicesForStopFlow(
        naptanStopCode: String
    ): Flow<List<RoomServiceDescriptor>>

    @Query("""
        SELECT stop.naptan_code AS naptan_code, service.name AS name,
            service.operator_code AS operator_code
        FROM service_stop
        LEFT JOIN stop ON stop.id = stop_id
        LEFT JOIN service_view AS service ON service.id = service_id
        WHERE stop.naptan_code IN (:naptanStopCodes)
        ORDER BY stop.naptan_code ASC,
            CASE WHEN service.name GLOB '[^0-9.]*' THEN service.name
            ELSE cast(service.name AS int) END
    """)
    override fun getServicesForStopsFlow(
        naptanStopCodes: Set<String>
    ): Flow<Map<@MapColumn(columnName = "naptan_code") String, List<RoomServiceDescriptor>>>
}
