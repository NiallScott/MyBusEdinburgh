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

package uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint

import androidx.room.Dao
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.database.busstop.operator.RoomOperatorEntity
import uk.org.rivernile.android.bustracker.core.database.busstop.service.RoomServiceEntity
import uk.org.rivernile.android.bustracker.core.database.busstop.service.bindStatement
import uk.org.rivernile.android.bustracker.core.database.busstop.service.createPlaceholders
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor

/**
 * This is the Room implementation of [ServicePointDao].
 *
 * @author Niall Scott
 */
@Dao
internal abstract class RoomServicePointDao : ServicePointDao {

    override fun getServicePointsFlow(
        services: Set<ServiceDescriptor>?
    ): Flow<List<ServicePoint>> {
        return services
            ?.ifEmpty { null }
            ?.let {
                getServicePointsForServicesFlow(createGetServicePointsForServicesRawQuery(it))
            }
            ?: servicePointsForAllServicesFlow
    }

    @get:Query("""
        SELECT service.name AS name, service.operator_code AS operator_code, route_section,
            latitude, longitude
        FROM service_point
        LEFT JOIN service_view AS service ON service_point.service_id = service.id
        ORDER BY service.name ASC, service.operator_code ASC, route_section ASC, order_value ASC
    """)
    abstract val servicePointsForAllServicesFlow: Flow<List<RoomServicePoint>>

    @RawQuery(
        observedEntities = [
            RoomOperatorEntity::class,
            RoomServiceEntity::class,
            RoomServicePointEntity::class
        ]
    )
    abstract fun getServicePointsForServicesFlow(query: RoomRawQuery): Flow<List<RoomServicePoint>>

    private fun createGetServicePointsForServicesRawQuery(
        services: Set<ServiceDescriptor>
    ): RoomRawQuery {
        val sqlQuery = buildString {
            append("""
                SELECT service.name AS name, service.operator_code AS operator_code, route_section,
                    latitude, longitude
                FROM service_point
                LEFT JOIN service_view AS service ON service_point.service_id = service.id
                WHERE (service.name, service.operator_code) IN (VALUES
            """.trimIndent())

            append(services.createPlaceholders())

            append("""
                )
                ORDER BY service.name ASC, service.operator_code ASC, route_section ASC,
                    order_value ASC
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
