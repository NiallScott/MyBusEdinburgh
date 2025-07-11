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

package uk.org.rivernile.android.bustracker.core.database.busstop.service

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * This is the Room implementation of [ServiceDao].
 *
 * @author Niall Scott
 */
@Dao
internal abstract class RoomServiceDao : ServiceDao {

    @get:Query("""
        SELECT name, hexColour 
        FROM service 
        ORDER BY CASE WHEN name GLOB '[^0-9.]*' THEN name ELSE cast(name AS int) END
    """)
    abstract override val allServiceNamesWithColourFlow: Flow<List<RoomServiceWithColour>?>

    @Query("""
        SELECT name, hexColour 
        FROM service 
        WHERE name IN (
            SELECT serviceName  
            FROM service_stop 
            WHERE stopCode = :stopCode
        )
        ORDER BY CASE WHEN name GLOB '[^0-9.]*' THEN name ELSE cast(name AS int) END
    """)
    abstract override fun getServiceNamesWithColourFlow(
        stopCode: String
    ): Flow<List<RoomServiceWithColour>?>

    @get:Query("""
        SELECT COUNT(*) 
        FROM service
    """)
    abstract override val serviceCountFlow: Flow<Int?>

    override fun getColoursForServicesFlow(services: Set<String>?): Flow<Map<String, Int?>?> {
        val flow = services
            ?.ifEmpty { null }
            ?.let {
                getColoursForServicesFlowInternal(it)
            }
            ?: coloursForAllServicesFlow

        return flow.map { serviceColours ->
            serviceColours?.mapValues {
                it.value?.colour
            }
        }
    }

    @get:Query("""
        SELECT name, hexColour 
        FROM service
    """)
    abstract val coloursForAllServicesFlow: Flow<Map<
            @MapColumn(columnName = "name") String,
            RoomServiceColour?>?>

    @Query("""
        SELECT name, description, hexColour 
        FROM service 
        WHERE name IN (
            SELECT serviceName 
            FROM service_stop 
            WHERE stopCode = :stopCode
        )
        ORDER BY CASE WHEN name GLOB '[^0-9.]*' THEN name ELSE cast(name AS int) END
    """)
    abstract override fun getServiceDetailsFlow(stopCode: String): Flow<List<RoomServiceDetails>?>

    @Query("""
        SELECT name, hexColour 
        FROM service 
        WHERE name IN (:services) 
        AND hexColour NOT NULL
    """)
    abstract fun getColoursForServicesFlowInternal(
        services: Set<String>
    ): Flow<Map<@MapColumn(columnName = "name") String, RoomServiceColour?>?>
}