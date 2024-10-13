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
    fun toServiceUpdatesOrNullReturnsNullWhenServiceUpdatesIsEmpty() {
        val result = emptyList<ServiceUpdate>().toServiceUpdatesOrNull()

        assertNull(result)
    }

    @Test
    fun toServiceUpdatesOrNullReturnsSingleIncidentServiceUpdate() {
        val time = Instant.fromEpochMilliseconds(123L)
        val expected = listOf(createIncidentServiceUpdate(time))

        val result = listOf(
            createIncidentEndpointServiceUpdate(time)
        ).toServiceUpdatesOrNull()

        assertEquals(expected, result)
    }

    @Test
    fun toServiceUpdatesOrNullReturnsSinglePlannedServiceUpdate() {
        val time = Instant.fromEpochMilliseconds(123L)
        val expected = listOf(createPlannedServiceUpdate(time))

        val resul = listOf(
            createPlannedEndpointServiceUpdate(time)
        ).toServiceUpdatesOrNull()

        assertEquals(expected, resul)
    }

    @Test
    fun toServiceUpdatesOrNullReturnsMappedServiceUpdates() {
        val incidentTime = Instant.fromEpochMilliseconds(123L)
        val plannedTime = Instant.fromEpochMilliseconds(456L)
        val expected = listOf(
            createIncidentServiceUpdate(incidentTime),
            createPlannedServiceUpdate(plannedTime)
        )

        val result = listOf(
            createIncidentEndpointServiceUpdate(incidentTime),
            createPlannedEndpointServiceUpdate(plannedTime)
        ).toServiceUpdatesOrNull()

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