/*
 * Copyright (C) 2022 - 2024 Niall 'Rivernile' Scott
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
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [ServicePointsRepository].
 *
 * @author Niall Scott
 */
class ServicePointsRepositoryTest {

    @Test
    fun getServicePointsFlowReturnsFlowInstanceFromDao() = runTest {
        val first = FakeServicePoint(
            serviceName = "1",
            chainage = 1,
            latitude = 1.1,
            longitude = 1.2
        )
        val second = FakeServicePoint(
            serviceName = "2",
            chainage = 2,
            latitude = 2.1,
            longitude = 2.2
        )
        val third = FakeServicePoint(
            serviceName = "3",
            chainage = 3,
            latitude = 3.1,
            longitude = 3.2
        )
        val repository = createServicePointsRepository(
            servicePointDao = FakeServicePointDao(
                onGetServicePointsFlow = {
                    assertEquals(setOf("1", "2", "3"), it)
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

        repository.getServicePointsFlow(setOf("1", "2", "3")).test {
            assertNull(awaitItem())
            assertEquals(listOf(first), awaitItem())
            assertEquals(listOf(first, second, third), awaitItem())
            awaitComplete()
        }
    }

    private fun createServicePointsRepository(
        servicePointDao: ServicePointDao = FakeServicePointDao()
    ): ServicePointsRepository {
        return ServicePointsRepository(servicePointDao)
    }
}