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
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdate
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for `JsonServiceUpdateEvents.kt`.
 *
 * @author Niall Scott
 */
class JsonServiceUpdateEventsKtTest {

    @Test
    fun toServiceUpdatesOrNullReturnsNullWhenEventsIsNull() {
        val result = JsonServiceUpdateEvents(
            events = null
        ).toServiceUpdatesOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdatesOrNullReturnsNullWhenEventsIsEmpty() {
        val result = JsonServiceUpdateEvents(
            events = emptyList()
        ).toServiceUpdatesOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdatesOrNullReturnsNullWhenSingleItemIsNotValid() {
        val result = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(id = null)
            )
        ).toServiceUpdatesOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsListWhenSingleItemIsValid() {
        val expected = listOf(createServiceUpdate())

        val result = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent()
            )
        ).toServiceUpdatesOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toServiceUpdateOrNullReturnsListWhenSomeItemsAreInvalid() {
        val expected = listOf(createServiceUpdate())

        val result = JsonServiceUpdateEvents(
            events = listOf(
                createJsonEvent(id = null),
                createJsonEvent()
            )
        ).toServiceUpdatesOrNull()

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