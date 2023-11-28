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

package uk.org.rivernile.android.bustracker.core.database.busstop.service

import androidx.room.Dao
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * This is the Room implementation of [ServiceDao].
 *
 * @author Niall Scott
 */
@Dao
internal abstract class RoomServiceDao {

    @get:Query("""
        SELECT name 
        FROM service 
        ORDER BY CASE WHEN name GLOB '[^0-9.]*' THEN name ELSE cast(name AS int) END
    """)
    abstract val allServiceNamesFlow: Flow<List<String>?>

    @get:Query("""
        SELECT COUNT(*) 
        FROM service
    """)
    abstract val serviceCountFlow: Flow<Int?>

    fun getColoursForServicesFlow(services: Set<String>?): Flow<Map<String, Int>?> {
        val flow = services
            ?.ifEmpty { null }
            ?.let {
                getColoursForServicesFlowInternal(it)
            }
            ?: coloursForAllServicesFlow

        return flow.map(this::mapToServiceColourMap)
    }

    @get:Query("""
        SELECT name, hexColour 
        FROM service 
        WHERE hexColour NOT NULL
    """)
    abstract val coloursForAllServicesFlow: Flow<List<RoomServiceColour>?>

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
    abstract fun getServiceDetailsFlow(stopCode: String): Flow<List<RoomServiceDetails>?>

    @Query("""
        SELECT name, hexColour 
        FROM service 
        WHERE name IN (:services) 
        AND hexColour NOT NULL
    """)
    abstract fun getColoursForServicesFlowInternal(
        services: Set<String>): Flow<List<RoomServiceColour>?>

    private fun mapToServiceColourMap(serviceColours: List<RoomServiceColour>?): Map<String, Int>? {
        return serviceColours
            ?.ifEmpty { null }
            ?.let { scs ->
                val result = HashMap<String, Int>(scs.size)

                scs.forEach { serviceColour ->
                    serviceColour
                        .colour
                        ?.let { colour ->
                            result[serviceColour.name] = colour
                        }
                }

                result.ifEmpty { null }
                    ?.toMap()
            }
    }
}