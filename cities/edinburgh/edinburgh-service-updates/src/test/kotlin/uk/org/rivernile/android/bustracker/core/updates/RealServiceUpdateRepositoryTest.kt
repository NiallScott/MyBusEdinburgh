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

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.FakeServiceUpdatesEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdatesEndpoint
import uk.org.rivernile.android.bustracker.core.endpoints.updates.service.ServiceUpdatesResponse
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [ServiceUpdateRepository].
 *
 * @author Niall Scott
 */
class RealServiceUpdateRepositoryTest {

    @Test
    fun serviceUpdatesFlowEmitsServerErrorWhenEndpointsReturnsServerError() = runTest {
        val repository = createServiceUpdateRepository(
            serviceUpdatesEndpoint = FakeServiceUpdatesEndpoint(
                onGetServiceUpdates = {
                    ServiceUpdatesResponse.Error.ServerError()
                }
            )
        )

        repository.serviceUpdatesFlow.test {
            assertEquals(ServiceUpdatesResult.InProgress, awaitItem())
            assertEquals(ServiceUpdatesResult.Error.Server, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun serviceUpdatesFlowEmitsServerErrorWhenEndpointsReturnsIoError() = runTest {
        val exception = RuntimeException()
        val repository = createServiceUpdateRepository(
            serviceUpdatesEndpoint = FakeServiceUpdatesEndpoint(
                onGetServiceUpdates = {
                    ServiceUpdatesResponse.Error.Io(exception)
                }
            )
        )

        repository.serviceUpdatesFlow.test {
            assertEquals(ServiceUpdatesResult.InProgress, awaitItem())
            assertEquals(ServiceUpdatesResult.Error.Io(exception), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun serviceUpdatesFlowEmitsServerErrorWhenEndpointsReturnsNoConnectivity() = runTest {
        val repository = createServiceUpdateRepository(
            serviceUpdatesEndpoint = FakeServiceUpdatesEndpoint(
                onGetServiceUpdates = {
                    ServiceUpdatesResponse.Error.NoConnectivity
                }
            )
        )

        repository.serviceUpdatesFlow.test {
            assertEquals(ServiceUpdatesResult.InProgress, awaitItem())
            assertEquals(ServiceUpdatesResult.Error.NoConnectivity, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun serviceUpdatesFlowEmitsSuccessWhenEndpointReturnsSuccess() = runTest {
        val repository = createServiceUpdateRepository(
            serviceUpdatesEndpoint = FakeServiceUpdatesEndpoint(
                onGetServiceUpdates = {
                    ServiceUpdatesResponse.Success(null)
                }
            )
        )

        repository.serviceUpdatesFlow.test {
            assertEquals(ServiceUpdatesResult.InProgress, awaitItem())
            assertEquals(ServiceUpdatesResult.Success(null), awaitItem())
            awaitComplete()
        }
    }

    private fun createServiceUpdateRepository(
        serviceUpdatesEndpoint: ServiceUpdatesEndpoint = FakeServiceUpdatesEndpoint()
    ): ServiceUpdateRepository {
        return RealServiceUpdateRepository(serviceUpdatesEndpoint)
    }
}