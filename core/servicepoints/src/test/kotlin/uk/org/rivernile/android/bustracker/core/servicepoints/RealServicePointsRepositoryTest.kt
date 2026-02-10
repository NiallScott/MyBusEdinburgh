/*
 * Copyright (C) 2022 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.servicepoints

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint.FakeServicePoint
import uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint.FakeServicePointDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint.ServicePointDao
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [ServicePointsRepository].
 *
 * @author Niall Scott
 */
class RealServicePointsRepositoryTest {

    @Test
    fun getServicePointsFlowReturnsFlowInstanceFromDao() = runTest {
        val service1 = FakeServiceDescriptor(serviceName = "1", operatorCode = "TEST1")
        val service2 = FakeServiceDescriptor(serviceName = "2", operatorCode = "TEST2")
        val service3 = FakeServiceDescriptor(serviceName = "3", operatorCode = "TEST3")
        val first = FakeServicePoint(
            serviceDescriptor = service1,
            routeSection = 1,
            latitude = 1.1,
            longitude = 1.2
        )
        val second = FakeServicePoint(
            serviceDescriptor = service2,
            routeSection = 2,
            latitude = 2.1,
            longitude = 2.2
        )
        val third = FakeServicePoint(
            serviceDescriptor = service3,
            routeSection = 3,
            latitude = 3.1,
            longitude = 3.2
        )
        val repository = createServicePointsRepository(
            servicePointDao = FakeServicePointDao(
                onGetServicePointsFlow = {
                    assertEquals(setOf(service1, service2, service3), it)
                    flowOf(
                        null,
                        listOf(first),
                        listOf(
                            first,
                            second,
                            third
                        )
                    )
                }
            )
        )

        repository.getServicePointsFlow(setOf(service1, service2, service3)).test {
            assertNull(awaitItem())
            assertEquals(listOf(first).toServicePoints(), awaitItem())
            assertEquals(listOf(first, second, third).toServicePoints(), awaitItem())
            awaitComplete()
        }
    }

    private fun createServicePointsRepository(
        servicePointDao: ServicePointDao = FakeServicePointDao()
    ): RealServicePointsRepository {
        return RealServicePointsRepository(servicePointDao)
    }
}
