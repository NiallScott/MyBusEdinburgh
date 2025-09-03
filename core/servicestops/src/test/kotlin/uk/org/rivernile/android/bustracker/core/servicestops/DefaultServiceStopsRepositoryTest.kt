/*
 * Copyright (C) 2021 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.servicestops

import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.busstop.servicestop.FakeServiceStopDao
import uk.org.rivernile.android.bustracker.core.database.busstop.servicestop.ServiceStopDao
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [ServiceStopsRepository].
 *
 * @author Niall Scott
 */
class DefaultServiceStopsRepositoryTest {

    @Test
    fun getServicesForStopFlowReturnsFlowFromDao() = runTest {
        val repository = createServiceStopsRepository(
            serviceStopDao = FakeServiceStopDao(
                onGetServicesForStopFlow = {
                    assertEquals("123456", it)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        listOf("1"),
                        listOf("1", "2")
                    )
                }
            )
        )

        repository.getServicesForStopFlow("123456").test {
            assertNull(awaitItem())
            assertEquals(listOf("1"), awaitItem())
            assertEquals(listOf("1", "2"), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getServicesForStopsFlowReturnsFlowFromDao() = runTest {
        val repository = createServiceStopsRepository(
            serviceStopDao = FakeServiceStopDao(
                onGetServicesForStopsFlow = {
                    assertEquals(setOf("123456", "987654"), it)
                    intervalFlowOf(
                        0L,
                        10L,
                        null,
                        mapOf("123456" to listOf("1")),
                        mapOf(
                            "123456" to listOf("1"),
                            "987654" to listOf("1", "2")
                        )
                    )
                }
            )
        )

        repository.getServicesForStopsFlow(setOf("123456", "987654")).test {
            assertNull(awaitItem())
            assertEquals(mapOf("123456" to listOf("1")), awaitItem())
            assertEquals(
                mapOf(
                    "123456" to listOf("1"),
                    "987654" to listOf("1", "2")
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createServiceStopsRepository(
        serviceStopDao: ServiceStopDao = FakeServiceStopDao()
    ): DefaultServiceStopsRepository {
        return DefaultServiceStopsRepository(serviceStopDao)
    }
}
