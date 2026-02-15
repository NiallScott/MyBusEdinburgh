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

package uk.org.rivernile.android.bustracker.core.database.busstop.service

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.database.busstop.operator.RoomOperatorEntity
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor

/**
 * This is the Room implementation of [ServiceDao].
 *
 * @author Niall Scott
 */
@Dao
internal abstract class RoomServiceDao : ServiceDao {

    @get:Query("""
        SELECT name, operator_code, colour_primary, colour_on_primary
        FROM service_view
        ORDER BY CASE WHEN name GLOB '[^0-9.]*' THEN name ELSE cast(name AS int) END
    """)
    abstract override val allServiceNamesWithColourFlow: Flow<List<RoomServiceWithColour>>

    @Query(
        """
        SELECT name, operator_code, colour_primary, colour_on_primary
        FROM service_view
        WHERE id IN (
            SELECT service_id
            FROM service_stop
            WHERE stop_id = (
                SELECT stop.id
                FROM stop
                WHERE naptan_code = :stopNaptanCode
                LIMIT 1
            )
        )
        ORDER BY CASE WHEN name GLOB '[^0-9.]*' THEN name ELSE cast(name AS int) END
    """
    )
    abstract override fun getServiceNamesWithColourFlow(
        stopNaptanCode: String
    ): Flow<List<RoomServiceWithColour>>

    @get:Query("""
        SELECT COUNT(*)
        FROM service
    """)
    abstract override val serviceCountFlow: Flow<Int?>

    override fun getColoursForServicesFlow(
        services: Set<ServiceDescriptor>?
    ): Flow<Map<ServiceDescriptor, ServiceColours>> {
        val flow = services
            ?.ifEmpty { null }
            ?.let {
                getColoursForServicesFlowInternal(createGetColoursForServicesRawQuery(services))
            }
            ?: allServiceNamesWithColourFlow

        return flow
            .map { serviceColours ->
                serviceColours.associate {
                    it.descriptor to it.colours
                }
            }
    }

    @Query("""
        SELECT name, operator_code, description, colour_primary, colour_on_primary
        FROM service_view
        WHERE id IN (
            SELECT service_id
            FROM service_stop
            WHERE stop_id = (
                SELECT stop.id
                FROM stop
                WHERE naptan_code = :naptanCode
                LIMIT 1
            )
        )
        ORDER BY CASE WHEN name GLOB '[^0-9.]*' THEN name ELSE cast(name AS int) END
    """)
    abstract override fun getServiceDetailsFlow(naptanCode: String): Flow<List<RoomServiceDetails>>

    @RawQuery(
        observedEntities = [
            RoomOperatorEntity::class,
            RoomServiceEntity::class
        ]
    )
    abstract fun getColoursForServicesFlowInternal(
        query: RoomRawQuery
    ): Flow<List<RoomServiceWithColour>>

    private fun createGetColoursForServicesRawQuery(
        services: Set<ServiceDescriptor>
    ): RoomRawQuery {
        val sqlQuery = buildString {
            append("""
                SELECT name, operator_code, colour_primary, colour_on_primary
                FROM service_view
                WHERE (name, operator_code) IN (VALUES
            """.trimIndent())

            append(services.createPlaceholders())

            append("""
                )
                ORDER BY CASE WHEN name GLOB '[^0-9.]*' THEN name ELSE cast(name AS int) END
            """.trimIndent())
        }

        return RoomRawQuery(
            sql = sqlQuery,
            onBindStatement = {
                services.bindStatement(it)
            }
        )
    }
}
