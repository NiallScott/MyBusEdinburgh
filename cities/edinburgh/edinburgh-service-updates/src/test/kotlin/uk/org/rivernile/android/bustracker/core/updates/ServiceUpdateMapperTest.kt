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

package uk.org.rivernile.android.bustracker.core.updates

import kotlinx.datetime.Instant
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdateType
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdate as EndpointServiceUpdate

/**
 * Tests for [ServiceUpdateMapper].
 *
 * @author Niall Scott
 */
class ServiceUpdateMapperTest {

    private lateinit var mapper: ServiceUpdateMapper

    @BeforeTest
    fun setUp() {
        mapper = ServiceUpdateMapper()
    }

    @Test
    fun mapToServiceUpdatesReturnsNullWhenEndpointServiceUpdatesIsNull() {
        val result = mapper.mapToServiceUpdates(null)

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesReturnsNullWhenEndpointServiceUpdatesIsEmpty() {
        val result = mapper.mapToServiceUpdates(emptyList())

        assertNull(result)
    }

    @Test
    fun mapToServiceUpdatesReturnsMappedIncidentServiceUpdate() {
        val time = Instant.fromEpochMilliseconds(123L)
        val expected = listOf(createIncidentServiceUpdate(time))

        val result = mapper.mapToServiceUpdates(
            listOf(
                createIncidentEndpointServiceUpdate(time)
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceUpdatesReturnsMappedPlannedServiceUpdate() {
        val time = Instant.fromEpochMilliseconds(123L)
        val expected = listOf(createPlannedServiceUpdate(time))

        val result = mapper.mapToServiceUpdates(
            listOf(
                createPlannedEndpointServiceUpdate(time)
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun mapToServiceUpdatesReturnsMappedServiceUpdates() {
        val incidentTime = Instant.fromEpochMilliseconds(123L)
        val plannedTime = Instant.fromEpochMilliseconds(456L)
        val expected = listOf(
            createIncidentServiceUpdate(incidentTime),
            createPlannedServiceUpdate(plannedTime)
        )

        val result = mapper.mapToServiceUpdates(
            listOf(
                createIncidentEndpointServiceUpdate(incidentTime),
                createPlannedEndpointServiceUpdate(plannedTime)
            )
        )

        assertEquals(expected, result)
    }

    private fun createIncidentEndpointServiceUpdate(lastUpdated: Instant): EndpointServiceUpdate {
        return EndpointServiceUpdate(
            id = "incidentId",
            lastUpdated = lastUpdated,
            serviceUpdateType = ServiceUpdateType.INCIDENT,
            summary = "incident summary",
            affectedServices = setOf("1", "2"),
            url = "http://one"
        )
    }

    private fun createPlannedEndpointServiceUpdate(lastUpdated: Instant): EndpointServiceUpdate {
        return EndpointServiceUpdate(
            id = "plannedId",
            lastUpdated = lastUpdated,
            serviceUpdateType = ServiceUpdateType.PLANNED,
            summary = "planned summary",
            affectedServices = setOf("3", "4"),
            url = "http://two"
        )
    }

    private fun createIncidentServiceUpdate(lastUpdated: Instant): IncidentServiceUpdate {
        return IncidentServiceUpdate(
            id = "incidentId",
            lastUpdated = lastUpdated,
            summary = "incident summary",
            affectedServices = setOf("1", "2"),
            url = "http://one"
        )
    }

    private fun createPlannedServiceUpdate(lastUpdated: Instant): PlannedServiceUpdate {
        return PlannedServiceUpdate(
            id = "plannedId",
            lastUpdated = lastUpdated,
            summary = "planned summary",
            affectedServices = setOf("3", "4"),
            url = "http://two"
        )
    }
}