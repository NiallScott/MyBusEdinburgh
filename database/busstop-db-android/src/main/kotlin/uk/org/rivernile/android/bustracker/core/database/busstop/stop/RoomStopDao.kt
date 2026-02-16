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

package uk.org.rivernile.android.bustracker.core.database.busstop.stop

import androidx.room.Dao
import androidx.room.MapColumn
import androidx.room.Query
import androidx.room.RawQuery
import androidx.room.RoomRawQuery
import androidx.room.Transaction
import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.database.busstop.operator.RoomOperatorEntity
import uk.org.rivernile.android.bustracker.core.database.busstop.service.RoomServiceEntity
import uk.org.rivernile.android.bustracker.core.database.busstop.service.bindStatement
import uk.org.rivernile.android.bustracker.core.database.busstop.service.createPlaceholders
import uk.org.rivernile.android.bustracker.core.database.busstop.servicestop.RoomServiceStopEntity
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor

/**
 * This is the Room implementation of [StopDao].
 *
 * @author Niall Scott
 */
@Dao
internal abstract class RoomStopDao : StopDao {

    @Query("""
        SELECT name, locality
        FROM stop
        WHERE naptan_code = :naptanStopCode
        LIMIT 1
    """)
    abstract override fun getNameForStopFlow(naptanStopCode: String): Flow<RoomStopName?>

    @Query("""
        SELECT latitude, longitude
        FROM stop
        WHERE naptan_code = :naptanStopCode
        LIMIT 1
    """)
    abstract override fun getLocationForStopFlow(naptanStopCode: String): Flow<RoomStopLocation?>

    @Query("""
        SELECT naptan_code, name, locality, latitude, longitude, bearing
        FROM stop
        WHERE naptan_code = :naptanStopCode
        LIMIT 1
    """)
    abstract override fun getStopDetailsFlow(naptanStopCode: String): Flow<RoomStopDetails?>

    @Query("""
        SELECT naptan_code, name, locality, latitude, longitude, bearing
        FROM stop
        WHERE naptan_code IN (:naptanStopCodes)
    """)
    abstract override fun getStopDetailsFlow(
        naptanStopCodes: Set<String>
    ): Flow<Map<@MapColumn(columnName = "naptan_code") String, RoomStopDetails>>

    override fun getStopDetailsWithServiceFilterFlow(
        serviceFilter: Set<ServiceDescriptor>?
    ): Flow<List<StopDetails>> {
        return serviceFilter
            ?.ifEmpty { null }
            ?.let {
                getStopDetailsWithServiceFilterFlowInternal(
                    createGetStopDetailsWithServiceFilterRawQuery(it)
                )
            }
            ?: allStopDetailsFlow
    }

    @Transaction
    @Query("""
        SELECT id, naptan_code, name, locality, latitude, longitude, bearing
        FROM stop
        WHERE (latitude BETWEEN :minLatitude AND :maxLatitude)
        AND (longitude BETWEEN :minLongitude AND :maxLongitude)
    """)
    abstract override fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double
    ): Flow<List<RoomStopDetailsWithServices>>

    override fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double,
        serviceFilter: Set<ServiceDescriptor>
    ): Flow<List<RoomStopDetailsWithServices>> {
        return getStopDetailsWithinSpanFlowInternal(
            createGetStopDetailsWithinSpanRawQuery(
                minLatitude = minLatitude,
                minLongitude = minLongitude,
                maxLatitude = maxLatitude,
                maxLongitude = maxLongitude,
                serviceFilter = serviceFilter
            )
        )
    }

    @Transaction
    @Query("""
        SELECT id, naptan_code, name, locality, bearing
        FROM stop
        WHERE naptan_code LIKE '%' || :searchTerm || '%'
        OR atco_code LIKE '%' || :searchTerm || '%'
        OR name LIKE '%' || :searchTerm || '%'
        OR locality LIKE '%' || :searchTerm || '%'
    """)
    abstract override fun getStopSearchResultsFlow(
        searchTerm: String
    ): Flow<List<RoomStopSearchResult>>

    @get:Query("""
        SELECT naptan_code, name, locality, latitude, longitude, bearing
        FROM stop
        WHERE latitude NOT NULL
        AND longitude NOT NULL
    """)
    abstract val allStopDetailsFlow: Flow<List<RoomStopDetails>>

    @RawQuery(
        observedEntities = [
            RoomOperatorEntity::class,
            RoomServiceEntity::class,
            RoomServiceStopEntity::class,
            RoomStopEntity::class
        ]
    )
    abstract fun getStopDetailsWithServiceFilterFlowInternal(
        query: RoomRawQuery
    ): Flow<List<RoomStopDetails>>

    @Transaction
    @RawQuery(
        observedEntities = [
            RoomOperatorEntity::class,
            RoomServiceEntity::class,
            RoomServiceStopEntity::class,
            RoomStopEntity::class
        ]
    )
    abstract fun getStopDetailsWithinSpanFlowInternal(
        rawQuery: RoomRawQuery
    ): Flow<List<RoomStopDetailsWithServices>>

    private fun createGetStopDetailsWithServiceFilterRawQuery(
        serviceFilter: Set<ServiceDescriptor>
    ): RoomRawQuery {
        val sqlQuery = buildString {
            append("""
                SELECT naptan_code, name, locality, latitude, longitude, bearing
                FROM stop
                WHERE id IN (
                    SELECT stop_id
                    FROM service_stop
                    WHERE service_id IN (
                        SELECT id
                        FROM service_view
                        WHERE (name, operator_code) IN (VALUES
            """.trimIndent())

            append(serviceFilter.createPlaceholders())

            append("""
                       )
                    )
                )
            """.trimIndent())
        }

        return RoomRawQuery(
            sql = sqlQuery,
            onBindStatement = {
                serviceFilter.forEachIndexed { index, serviceDescriptor ->
                    val baseIndex = (index * 2) + 1
                    it.bindText(baseIndex, serviceDescriptor.serviceName)
                    it.bindText(baseIndex + 1, serviceDescriptor.operatorCode)
                }
            }
        )
    }

    private fun createGetStopDetailsWithinSpanRawQuery(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double,
        serviceFilter: Set<ServiceDescriptor>
    ): RoomRawQuery {
        val sqlQuery = buildString {
            append("""
                SELECT id, naptan_code, name, locality, latitude, longitude, bearing
                FROM stop
                WHERE (latitude BETWEEN ? AND ?)
                AND (longitude BETWEEN ? AND ?)
                AND id IN (
                    SELECT stop_id
                    FROM service_stop
                    WHERE service_id IN (
                        SELECT id
                        FROM service_view
                        WHERE (name, operator_code) IN (VALUES
            """.trimIndent())

            append(serviceFilter.createPlaceholders())

            append("""
                       )
                    )
                )
            """.trimIndent())
        }

        return RoomRawQuery(
            sql = sqlQuery,
            onBindStatement = {
                it.bindDouble(1, minLatitude)
                it.bindDouble(2, maxLatitude)
                it.bindDouble(3, minLongitude)
                it.bindDouble(4, maxLongitude)

                serviceFilter.bindStatement(statement = it, offset = 4)
            }
        )
    }
}
