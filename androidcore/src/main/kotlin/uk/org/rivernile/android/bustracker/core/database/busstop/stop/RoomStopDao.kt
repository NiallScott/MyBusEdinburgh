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

package uk.org.rivernile.android.bustracker.core.database.busstop.stop

import androidx.room.Dao
import androidx.room.MapInfo
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * This is the Room implementation of [StopDao].
 *
 * @author Niall Scott
 */
@Dao
internal abstract class RoomStopDao : StopDao {

    override fun getNameForStopFlow(stopCode: String): Flow<StopName?> =
        getNameForStopFlowInternal(stopCode)

    override fun getLocationForStopFlow(stopCode: String): Flow<StopLocation?> =
        getLocationForStopFlowInternal(stopCode)

    override fun getStopDetailsFlow(stopCode: String): Flow<StopDetails?> =
        getStopDetailsFlowInternal(stopCode)

    override fun getStopDetailsFlow(stopCodes: Set<String>): Flow<Map<String, StopDetails>?> =
        getStopDetailsFlowInternal(stopCodes)

    override fun getStopDetailsWithServiceFilterFlow(
        serviceFilter: Set<String>?): Flow<List<StopDetails>?> {
        return serviceFilter
            ?.ifEmpty { null }
            ?.let {
                getStopDetailsWithServiceFilterFlowInternal(it)
            }
            ?: allStopDetailsFlow
    }

    override fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double): Flow<List<StopDetailsWithServices>?> =
        getStopDetailsWithinSpanFlowInternal(minLatitude, minLongitude, maxLatitude, maxLongitude)

    override fun getStopDetailsWithinSpanFlow(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double,
        serviceFilter: Set<String>): Flow<List<StopDetailsWithServices>?> =
        getStopDetailsWithinSpanFlowInternal(
            minLatitude,
            minLongitude,
            maxLatitude,
            maxLongitude,
            serviceFilter)

    override fun getStopSearchResultsFlow(searchTerm: String): Flow<List<StopSearchResult>?> =
        getStopSearchResultsFlowInternal(searchTerm)

    @Query("""
        SELECT stopName AS name, locality 
        FROM bus_stop 
        WHERE stopCode = :stopCode 
        LIMIT 1
    """)
    abstract fun getNameForStopFlowInternal(stopCode: String): Flow<RoomStopName?>

    @Query("""
        SELECT latitude, longitude 
        FROM bus_stop 
        WHERE stopCode = :stopCode 
        AND latitude NOT NULL 
        AND longitude NOT NULL 
        LIMIT 1
    """)
    abstract fun getLocationForStopFlowInternal(stopCode: String): Flow<RoomStopLocation?>

    @Query("""
        SELECT stopCode, stopName AS name, locality, latitude, longitude, orientation 
        FROM bus_stop 
        WHERE stopCode = :stopCode 
        AND latitude NOT NULL 
        AND longitude NOT NULL 
        LIMIT 1
    """)
    abstract fun getStopDetailsFlowInternal(stopCode: String): Flow<RoomStopDetails?>

    @Query("""
        SELECT stopCode, stopName AS name, locality, latitude, longitude, orientation 
        FROM bus_stop 
        WHERE stopCode IN (:stopsCodes) 
        AND latitude NOT NULL 
        AND longitude NOT NULL 
    """)
    @MapInfo(keyColumn = "stopCode")
    abstract fun getStopDetailsFlowInternal(
        stopsCodes: Set<String>): Flow<Map<String, RoomStopDetails>?>

    @get:Query("""
        SELECT stopCode, stopName AS name, locality, latitude, longitude, orientation 
        FROM bus_stop 
        WHERE latitude NOT NULL 
        AND longitude NOT NULL
    """)
    abstract val allStopDetailsFlow: Flow<List<RoomStopDetails>?>

    @Query("""
        SELECT stopCode, stopName AS name, locality, latitude, longitude, orientation 
        FROM bus_stop 
        WHERE stopCode IN (
            SELECT stopCode 
            FROM service_stop 
            WHERE serviceName IN (:serviceFilter)
        )
        AND latitude NOT NULL 
        AND longitude NOT NULL
    """)
    abstract fun getStopDetailsWithServiceFilterFlowInternal(
        serviceFilter: Set<String>): Flow<List<RoomStopDetails>?>

    @Query("""
        SELECT stopCode, stopName AS name, locality, latitude, longitude, orientation, (
            SELECT group_concat(serviceName) 
            FROM (
                SELECT stopCode, serviceName 
                FROM service_stop 
                WHERE bus_stop.stopCode = service_stop.stopCode 
                ORDER BY CASE WHEN serviceName GLOB '[^0-9.]*' THEN 
                    serviceName ELSE cast(serviceName AS int) END
            )
        ) AS serviceListing
        FROM bus_stop 
        WHERE (latitude BETWEEN :minLatitude AND :maxLatitude) 
        AND (longitude BETWEEN :minLongitude AND :maxLongitude)
    """)
    abstract fun getStopDetailsWithinSpanFlowInternal(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double): Flow<List<RoomStopDetailsWithServices>?>

    @Query("""
        SELECT stopCode, stopName AS name, locality, latitude, longitude, orientation, (
            SELECT group_concat(serviceName) 
            FROM (
                SELECT stopCode, serviceName 
                FROM service_stop 
                WHERE bus_stop.stopCode = service_stop.stopCode 
                ORDER BY CASE WHEN serviceName GLOB '[^0-9.]*' THEN 
                    serviceName ELSE cast(serviceName AS int) END
            )
        ) AS serviceListing
        FROM bus_stop 
        WHERE (latitude BETWEEN :minLatitude AND :maxLatitude) 
        AND (longitude BETWEEN :minLongitude AND :maxLongitude)
        AND stopCode IN (
            SELECT DISTINCT stopCode 
            FROM service_stop 
            WHERE serviceName IN (:serviceFilter)
        )
    """)
    abstract fun getStopDetailsWithinSpanFlowInternal(
        minLatitude: Double,
        minLongitude: Double,
        maxLatitude: Double,
        maxLongitude: Double,
        serviceFilter: Set<String>): Flow<List<RoomStopDetailsWithServices>?>

    @Query("""
        SELECT stopCode, stopName AS name, locality, orientation, (
            SELECT group_concat(serviceName) 
            FROM (
                SELECT stopCode, serviceName 
                FROM service_stop 
                WHERE bus_stop.stopCode = service_stop.stopCode 
                ORDER BY CASE WHEN serviceName GLOB '[^0-9.]*' THEN 
                    serviceName ELSE cast(serviceName AS int) END
            )
        ) AS serviceListing
        FROM bus_stop 
        WHERE stopCode LIKE '%' || :searchTerm || '%' 
        OR stopName LIKE '%' || :searchTerm || '%' 
        OR locality LIKE '%' || :searchTerm || '%'
    """)
    abstract fun getStopSearchResultsFlowInternal(
        searchTerm: String): Flow<List<RoomStopSearchResult>?>
}