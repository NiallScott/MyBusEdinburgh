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
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdate
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdateType
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for `ServiceUpdateMapper.kt`.
 *
 * @author Niall Scott
 */
class ServiceUpdateKtTest {

    @Test
    fun toIncidentsServiceUpdatesOrNullReturnsNullWhenServicesUpdatesIsEmpty() {
        val result = emptyList<ServiceUpdate>().toIncidentsServiceUpdatesOrNull()

        assertNull(result)
    }

    @Test
    fun toIncidentsServiceUpdatesOrNullReturnsNullWithSinglePlannedServiceUpdate() {
        val result = listOf(
            createPlannedEndpointServiceUpdate(Instant.fromEpochMilliseconds(123L))
        ).toIncidentsServiceUpdatesOrNull()

        assertNull(result)
    }

    @Test
    fun toIncidentsServiceUpdatesOrNullReturnsSingleItemWithSingleIncidentServiceUpdate() {
        val time = Instant.fromEpochMilliseconds(123L)
        val expected = listOf(createIncidentServiceUpdate(time))

        val result = listOf(
            createIncidentEndpointServiceUpdate(time)
        ).toIncidentsServiceUpdatesOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toIncidentsServiceUpdatesOrNullReturnsMappedServiceUpdates() {
        val time1 = Instant.fromEpochMilliseconds(123L)
        val time2 = Instant.fromEpochMilliseconds(456L)
        val expected = listOf(
            createIncidentServiceUpdate(time1),
            createIncidentServiceUpdate(time2)
        )

        val result = listOf(
            createIncidentEndpointServiceUpdate(time1),
            createPlannedEndpointServiceUpdate(Instant.fromEpochMilliseconds(789L)),
            createIncidentEndpointServiceUpdate(time2)
        ).toIncidentsServiceUpdatesOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toPlannedServiceUpdatesOrNullReturnsNullWhenServiceUpdatesIsEmpty() {
        val result = emptyList<ServiceUpdate>().toPlannedServiceUpdatesOrNull()

        assertNull(result)
    }

    @Test
    fun toPlannedServiceUpdatesOrNullReturnsNullWithSingleIncidentServiceUpdate() {
        val result = listOf(
            createIncidentEndpointServiceUpdate(Instant.fromEpochMilliseconds(123L))
        ).toPlannedServiceUpdatesOrNull()

        assertNull(result)
    }

    @Test
    fun toPlannedServiceUpdatesOrNullReturnsSingleItemWithSinglePlannedServiceUpdate() {
        val time = Instant.fromEpochMilliseconds(123L)
        val expected = listOf(createPlannedServiceUpdate(time))

        val result = listOf(
            createPlannedEndpointServiceUpdate(time)
        ).toPlannedServiceUpdatesOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toPlannedServiceUpdatesOrNullReturnsMappedServiceUpdates() {
        val time1 = Instant.fromEpochMilliseconds(123L)
        val time2 = Instant.fromEpochMilliseconds(456L)
        val expected = listOf(
            createPlannedServiceUpdate(time1),
            createPlannedServiceUpdate(time2)
        )

        val result = listOf(
            createPlannedEndpointServiceUpdate(time1),
            createIncidentEndpointServiceUpdate(Instant.fromEpochMilliseconds(789L)),
            createPlannedEndpointServiceUpdate(time2)
        ).toPlannedServiceUpdatesOrNull()

        assertEquals(expected, result)
    }

    private fun createIncidentEndpointServiceUpdate(lastUpdated: Instant): ServiceUpdate {
        return ServiceUpdate(
            id = "incidentId",
            lastUpdated = lastUpdated,
            serviceUpdateType = ServiceUpdateType.INCIDENT,
            title = "incident title",
            summary = "incident summary",
            affectedServices = setOf("1", "2"),
            url = "http://one"
        )
    }

    private fun createPlannedEndpointServiceUpdate(lastUpdated: Instant): ServiceUpdate {
        return ServiceUpdate(
            id = "plannedId",
            lastUpdated = lastUpdated,
            serviceUpdateType = ServiceUpdateType.PLANNED,
            title = "planned title",
            summary = "planned summary",
            affectedServices = setOf("3", "4"),
            url = "http://two"
        )
    }

    private fun createIncidentServiceUpdate(lastUpdated: Instant): IncidentServiceUpdate {
        return IncidentServiceUpdate(
            id = "incidentId",
            lastUpdated = lastUpdated,
            title = "incident title",
            summary = "incident summary",
            affectedServices = setOf("1", "2"),
            url = "http://one"
        )
    }

    private fun createPlannedServiceUpdate(lastUpdated: Instant): PlannedServiceUpdate {
        return PlannedServiceUpdate(
            id = "plannedId",
            lastUpdated = lastUpdated,
            title = "planned title",
            summary = "planned summary",
            affectedServices = setOf("3", "4"),
            url = "http://two"
        )
    }
}