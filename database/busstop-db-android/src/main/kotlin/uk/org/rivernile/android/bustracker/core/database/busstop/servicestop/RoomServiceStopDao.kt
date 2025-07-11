/*
 * Copyright (C) 2023 - 2025 Niall 'Rivernile' Scott
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

/**
 * This is the Room implementation of [ServiceStopDao].
 *
 * @author Niall Scott
 */
@Dao
internal interface RoomServiceStopDao : ServiceStopDao {

    @Query("""
        SELECT serviceName 
        FROM service_stop 
        WHERE stopCode = :stopCode 
        ORDER BY CASE WHEN serviceName GLOB '[^0-9.]*' THEN 
            serviceName ELSE cast(serviceName AS int) END
    """)
    override fun getServicesForStopFlow(stopCode: String): Flow<List<String>?>

    @Query("""
        SELECT stopCode, serviceName
        FROM service_stop
        WHERE stopCode IN (:stopCodes)
        ORDER BY stopCode ASC, 
            CASE WHEN serviceName GLOB '[^0-9.]*' THEN serviceName ELSE cast(serviceName AS int) END
    """)
    override fun getServicesForStopsFlow(
        stopCodes: Set<String>
    ): Flow<Map<
            @MapColumn(columnName = "stopCode") String,
            List<@MapColumn(columnName = "serviceName") String>>?>
}