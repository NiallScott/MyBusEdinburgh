/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.servicestop

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopDatabase
import uk.org.rivernile.android.bustracker.core.database.busstop.FakeBusStopDatabase
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [ProxyServiceStopDao].
 *
 * @author Niall Scott
 */
class ProxyServiceStopDaoTest {

    @Test
    fun getServicesForStopFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = listOf(
            FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST"
            )
        )
        val second = listOf(
            FakeServiceDescriptor(
                serviceName = "2",
                operatorCode = "TEST"
            )
        )
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyServiceStopDao(
            database = FakeBusStopDatabase(
                onServiceStopDao = {
                    FakeServiceStopDao(
                        onGetServicesForStopFlow = {
                            assertEquals("123456", it)
                            flows.removeFirst()
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.getServicesForStopFlow("123456").test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    @Test
    fun getServicesForStopsFlowRespondsToDatbaseOpenStatus() = runTest {
        val first = mapOf(
            "1" to listOf(
                FakeServiceDescriptor(
                    serviceName = "100",
                    operatorCode = "TEST"
                )
            )
        )
        val second = mapOf(
            "2" to listOf(
                FakeServiceDescriptor(
                    serviceName = "200",
                    operatorCode = "TEST"
                )
            )
        )
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyServiceStopDao(
            database = FakeBusStopDatabase(
                onServiceStopDao = {
                    FakeServiceStopDao(
                        onGetServicesForStopsFlow = {
                            assertEquals(setOf("123456"), it)
                            flows.removeFirst()
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.getServicesForStopsFlow(setOf("123456")).test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    private fun createProxyServiceStopDao(
        database: BusStopDatabase = FakeBusStopDatabase()
    ): ProxyServiceStopDao {
        return ProxyServiceStopDao(database = database)
    }
}
