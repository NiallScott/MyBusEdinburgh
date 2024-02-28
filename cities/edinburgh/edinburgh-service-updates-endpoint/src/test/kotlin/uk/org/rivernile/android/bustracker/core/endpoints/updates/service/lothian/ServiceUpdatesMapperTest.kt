/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.updates.service.lothian

import kotlinx.datetime.Instant
import org.junit.Before
import org.junit.Test
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdate
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdateType
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [ServiceUpdatesMapper].
 *
 * @author Niall Scott
 */
class ServiceUpdatesMapperTest {

    private lateinit var mapper: ServiceUpdatesMapper

    private val createdTime = Instant.parse("2024-02-13T09:08:25+00:00")
    private val modifiedTime = Instant.parse("2024-02-14T15:24:26+00:00")

    @Before
    fun setUp() {
        mapper = ServiceUpdatesMapper()
    }

    @Test
    fun mapToServiceUpdatesReturnsNullWhenEventsIsNull() {
        val result = mapper.mapToServiceUpdates(null)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesReturnsNullWhenEventsListIsNull() {
        val events = JsonServiceUpdateEvents(null)

        val result = mapper.mapToServiceUpdates(events)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesReturnsNullWhenEventsListIsEmpty() {
        val events = JsonServiceUpdateEvents(emptyList())

        val result = mapper.mapToServiceUpdates(events)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesDoesNotMapItemWhenIdIsNull() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(id = null)
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesDoesNotMapItemWhenIdIsEmpty() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(id = "")
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesDoesNotMapItemWhenThereAreNoTimestamps() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(
                    createdTime = null,
                    lastUpdatedTime = null
                )
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesDoesNotMapItemWhenSeverityIsNull() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(severity = null)
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesDoesNotMapItemWhenSeverityIsEmpty() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(severity = "")
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesDoesNotMapItemWhenSeverityIsUnknownItem() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(severity = "unknown")
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesDoesNotMapItemWhenDescriptionsIsNull() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(descriptions = null)
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesDoesNotMapItemWhenDescriptionsIsEmpty() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(descriptions = emptyMap())
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesDoesNotMapItemWhenDescriptionsDoesNotContainEnglishItem() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(descriptions = mapOf("fr" to "Non"))
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesDoesNotMapItemWhenDescriptionsEnglishItemIsEmpty() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(descriptions = mapOf("en" to ""))
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesMapsItemWhenValid() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent()
            )
        )
        val expected = listOf(
            createServiceUpdate()
        )

        val result = mapper.mapToServiceUpdates(events)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceUpdatesUsesCreatedTimeWhenLastUpdatedTimeIsMissing() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(
                    lastUpdatedTime = null
                )
            )
        )
        val expected = listOf(
            createServiceUpdate(
                lastUpdated = this.createdTime
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceUpdatesHandlesSeverityIncidentItemUppercase() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(
                    severity = "INCIDENT"
                )
            )
        )
        val expected = listOf(
            createServiceUpdate(
                serviceUpdateType = ServiceUpdateType.INCIDENT
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceUpdatesHandlesSeverityIncidentItemLowercase() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(
                    severity = "incident"
                )
            )
        )
        val expected = listOf(
            createServiceUpdate(
                serviceUpdateType = ServiceUpdateType.INCIDENT
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceUpdatesHandlesSeverityPlannedItemUppercase() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(
                    severity = "PLANNED"
                )
            )
        )
        val expected = listOf(
            createServiceUpdate(
                serviceUpdateType = ServiceUpdateType.PLANNED
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceUpdatesHandlesSeverityPlannedItemLowercase() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(
                    severity = "planned"
                )
            )
        )
        val expected = listOf(
            createServiceUpdate(
                serviceUpdateType = ServiceUpdateType.PLANNED
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceUpdatesHandlesNullRoutesAffected() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(
                    routesAffected = null
                )
            )
        )
        val expected = listOf(
            createServiceUpdate(
                affectedServices = null
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceUpdatesHandlesEmptyRoutesAffected() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(
                    routesAffected = emptyList()
                )
            )
        )
        val expected = listOf(
            createServiceUpdate(
                affectedServices = null
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceUpdatesHandlesRoutesAffectedWithItemsContainingNullServiceNames() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(
                    routesAffected = listOf(
                        JsonRouteAffected(name = null)
                    )
                )
            )
        )
        val expected = listOf(
            createServiceUpdate(
                affectedServices = null
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceUpdatesHandlesRoutesAffectedWithItemsContainingEmptyServiceNames() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(
                    routesAffected = listOf(
                        JsonRouteAffected(name = "")
                    )
                )
            )
        )
        val expected = listOf(
            createServiceUpdate(
                affectedServices = null
            )
        )

        val result = mapper.mapToServiceUpdates(events)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceUpdatesHandlesNullUrl() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(url = null)
            )
        )
        val expected = listOf(
            createServiceUpdate(url = null)
        )

        val result = mapper.mapToServiceUpdates(events)

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceUpdatesHandlesEmptyUrl() {
        val events = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(url = "")
            )
        )
        val expected = listOf(
            createServiceUpdate(url = null)
        )

        val result = mapper.mapToServiceUpdates(events)

        assertEquals(expected, result)
    }

    private fun createJsonEvent(
        id: String? = "id",
        createdTime: Instant? = this.createdTime,
        lastUpdatedTime: Instant? = this.modifiedTime,
        severity: String? = "incident",
        descriptions: Map<String, String>? = mapOf("en" to "Description"),
        routesAffected: List<JsonRouteAffected>? = listOf(
            JsonRouteAffected(name = "1"),
            JsonRouteAffected(name = "2"),
            JsonRouteAffected(name = "3")
        ),
        url: String? = "https://some/url"
    ): JsonEvent {
        return JsonEvent(
            id,
            createdTime,
            lastUpdatedTime,
            severity,
            descriptions,
            routesAffected,
            url
        )
    }

    private fun createServiceUpdate(
        id: String = "id",
        lastUpdated: Instant = this.modifiedTime,
        serviceUpdateType: ServiceUpdateType = ServiceUpdateType.INCIDENT,
        summary: String = "Description",
        affectedServices: Set<String>? = setOf("1", "2", "3"),
        url: String? = "https://some/url"
    ): ServiceUpdate {
        return ServiceUpdate(
            id,
            lastUpdated,
            serviceUpdateType,
            summary,
            affectedServices,
            url
        )
    }
}