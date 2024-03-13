package uk.org.rivernile.android.bustracker.core.updates

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdatesEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdatesResponse
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [ServiceUpdateRepository].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class ServiceUpdateRepositoryTest {

    @Mock
    private lateinit var serviceUpdatesEndpoint: ServiceUpdatesEndpoint
    @Mock
    private lateinit var serviceUpdateMapper: ServiceUpdateMapper

    private lateinit var repository: ServiceUpdateRepository

    @BeforeTest
    fun setUp() {
        repository = ServiceUpdateRepository(
            serviceUpdatesEndpoint,
            serviceUpdateMapper
        )
    }

    @Test
    fun serviceUpdatesFlowEmitsServerErrorWhenEndpointsReturnsServerError() = runTest {
        whenever(serviceUpdatesEndpoint.getServiceUpdates())
            .thenReturn(ServiceUpdatesResponse.Error.ServerError())

        repository.serviceUpdatesFlow.test {
            assertEquals(ServiceUpdatesResult.InProgress, awaitItem())
            assertEquals(ServiceUpdatesResult.Error.Server, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun serviceUpdatesFlowEmitsServerErrorWhenEndpointsReturnsIoError() = runTest {
        val exception = RuntimeException()
        whenever(serviceUpdatesEndpoint.getServiceUpdates())
            .thenReturn(ServiceUpdatesResponse.Error.Io(exception))

        repository.serviceUpdatesFlow.test {
            assertEquals(ServiceUpdatesResult.InProgress, awaitItem())
            assertEquals(ServiceUpdatesResult.Error.Io(exception), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun serviceUpdatesFlowEmitsServerErrorWhenEndpointsReturnsNoConnectivity() = runTest {
        whenever(serviceUpdatesEndpoint.getServiceUpdates())
            .thenReturn(ServiceUpdatesResponse.Error.NoConnectivity)

        repository.serviceUpdatesFlow.test {
            assertEquals(ServiceUpdatesResult.InProgress, awaitItem())
            assertEquals(ServiceUpdatesResult.Error.NoConnectivity, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun serviceUpdatesFlowEmitsSuccessWhenEndpointReturnsSuccess() = runTest {
        whenever(serviceUpdateMapper.mapToServiceUpdates(null))
            .thenReturn(null)
        whenever(serviceUpdatesEndpoint.getServiceUpdates())
            .thenReturn(ServiceUpdatesResponse.Success(null))

        repository.serviceUpdatesFlow.test {
            assertEquals(ServiceUpdatesResult.InProgress, awaitItem())
            assertEquals(ServiceUpdatesResult.Success(null), awaitItem())
            awaitComplete()
        }
    }
}