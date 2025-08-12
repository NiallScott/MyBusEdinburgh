/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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

@file:OptIn(ExperimentalTime::class)

package uk.org.rivernile.android.bustracker.core.endpoints.updates.service.lothian

import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdate
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Tests for `JsonEvent.kt`.
 *
 * @author Niall Scott
 */
class JsonEventKtTest {

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenIdIsNull() {
        val result = createJsonEvent(id = null)
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenIdIsEmpty() {
        val result = createJsonEvent(id = "")
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenTimestampsAreNull() {
        val result = createJsonEvent(
            createdTime = null,
            lastUpdatedTime = null
        ).toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenSeverityIsNull() {
        val result = createJsonEvent(severity = null)
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenSeverityIsEmpty() {
        val result = createJsonEvent(severity = "")
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenSeverityIsUnknown() {
        val result = createJsonEvent(severity = "unknown")
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenTitlesIsNull() {
        val result = createJsonEvent(titles = null)
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenTitlesIsEmpty() {
        val result = createJsonEvent(titles = emptyMap())
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenTitlesDoesNotContainEnglishItem() {
        val result = createJsonEvent(titles = mapOf("fr" to "Non"))
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenTitlesEnglishItemIsNull() {
        val result = createJsonEvent(titles = mapOf("en" to null))
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenTitlesEnglishItemIsEmpty() {
        val result = createJsonEvent(titles = mapOf("en" to ""))
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenDescriptionsIsNull() {
        val result = createJsonEvent(descriptions = null)
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenDescriptionsIsEmpty() {
        val result = createJsonEvent(descriptions = emptyMap())
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenDescriptionsDoesNotContainEnglishItem() {
        val result = createJsonEvent(descriptions = mapOf("fr" to "Non"))
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenDescriptionsEnglishItemIsNull() {
        val result = createJsonEvent(descriptions = mapOf("en" to null))
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNullWhenDescriptionsEnglishItemIsEmpty() {
        val result = createJsonEvent(descriptions = mapOf("en" to ""))
            .toServiceUpdateOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsNonNullWhenJsonEventIsValid() {
        val expected = createServiceUpdate()

        val result = createJsonEvent().toServiceUpdateOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toServiceUpdateOrNullUsesCreatedTimeWhenLastUpdatedTimeIsMissing() {
        val expected = createServiceUpdate(lastUpdated = this.createdTime)

        val result = createJsonEvent(lastUpdatedTime = null)
            .toServiceUpdateOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toServiceUpdateOrNullHandleSeverityIncidentItemUppercase() {
        val expected = createServiceUpdate(
            serviceUpdateType = ServiceUpdateType.INCIDENT
        )

        val result = createJsonEvent(
            severity = "INCIDENT"
        ).toServiceUpdateOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toServiceUpdateOrNullHandleSeverityIncidentItemLowercase() {
        val expected = createServiceUpdate(
            serviceUpdateType = ServiceUpdateType.INCIDENT
        )

        val result = createJsonEvent(
            severity = "incident"
        ).toServiceUpdateOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toServiceUpdateOrNullHandleSeverityPlannedItemUppercase() {
        val expected = createServiceUpdate(
            serviceUpdateType = ServiceUpdateType.PLANNED
        )

        val result = createJsonEvent(
            severity = "PLANNED"
        ).toServiceUpdateOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toServiceUpdateOrNullHandleSeverityPlannedItemLowercase() {
        val expected = createServiceUpdate(
            serviceUpdateType = ServiceUpdateType.PLANNED
        )

        val result = createJsonEvent(
            severity = "planned"
        ).toServiceUpdateOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toServiceUpdateOrNullHandlesNullRoutesAffected() {
        val expected = createServiceUpdate(affectedServices = null)

        val result = createJsonEvent(routesAffected = null)
            .toServiceUpdateOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toServiceUpdateOrNullHandlesEmptyRoutesAffected() {
        val expected = createServiceUpdate(affectedServices = null)

        val result = createJsonEvent(routesAffected = emptyList())
            .toServiceUpdateOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toServiceUpdateOrNullHandlesNullUrl() {
        val expected = createServiceUpdate(url = null)

        val result = createJsonEvent(url = null)
            .toServiceUpdateOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toServiceUpdateOrNullHandlesEmptyUrl() {
        val expected = createServiceUpdate(url = null)

        val result = createJsonEvent(url = "")
            .toServiceUpdateOrNull()

        assertEquals(expected, result)
    }

    private val createdTime get() = Instant.parse("2024-02-13T09:08:25+00:00")
    private val modifiedTime get() = Instant.parse("2024-02-14T15:24:26+00:00")

    private fun createJsonEvent(
        id: String? = "id",
        createdTime: Instant? = this.createdTime,
        lastUpdatedTime: Instant? = this.modifiedTime,
        severity: String? = "incident",
        titles: Map<String, String?>? = mapOf("en" to "Title"),
        descriptions: Map<String, String?>? = mapOf("en" to "Description"),
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
            titles,
            descriptions,
            routesAffected,
            url
        )
    }

    private fun createServiceUpdate(
        id: String = "id",
        lastUpdated: Instant = this.modifiedTime,
        serviceUpdateType: ServiceUpdateType = ServiceUpdateType.INCIDENT,
        title: String = "Title",
        summary: String = "Description",
        affectedServices: Set<String>? = setOf("1", "2", "3"),
        url: String? = "https://some/url"
    ): ServiceUpdate {
        return ServiceUpdate(
            id,
            lastUpdated,
            serviceUpdateType,
            title,
            summary,
            affectedServices,
            url
        )
    }
}