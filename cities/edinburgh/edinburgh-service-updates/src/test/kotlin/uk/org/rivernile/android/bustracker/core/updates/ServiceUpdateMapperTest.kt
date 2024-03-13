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