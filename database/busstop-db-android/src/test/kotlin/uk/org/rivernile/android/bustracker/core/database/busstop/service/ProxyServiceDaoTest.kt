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

package uk.org.rivernile.android.bustracker.core.database.busstop.service

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopDatabase
import uk.org.rivernile.android.bustracker.core.database.busstop.FakeBusStopDatabase
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [ProxyServiceDao].
 *
 * @author Niall Scott
 */
class ProxyServiceDaoTest {

    @Test
    fun allServiceNamesWithColourFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = listOf(
            FakeServiceWithColour(
                descriptor = FakeServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST"
                ),
                colours = FakeServiceColours(
                    colourPrimary = 100,
                    colourOnPrimary = 101
                )
            )
        )
        val second = listOf(
            FakeServiceWithColour(
                descriptor = FakeServiceDescriptor(
                    serviceName = "2",
                    operatorCode = "TEST"
                ),
                colours = FakeServiceColours(
                    colourPrimary = 200,
                    colourOnPrimary = 201
                )
            )
        )
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyServiceDao(
            database = FakeBusStopDatabase(
                onServiceDao = {
                    FakeServiceDao(
                        onAllServiceNamesWithColourFlow = { flows.removeFirst() }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.allServiceNamesWithColourFlow.test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    @Test
    fun getServiceNamesWithColourFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = listOf(
            FakeServiceWithColour(
                descriptor = FakeServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST"
                ),
                colours = FakeServiceColours(
                    colourPrimary = 100,
                    colourOnPrimary = 101
                )
            )
        )
        val second = listOf(
            FakeServiceWithColour(
                descriptor = FakeServiceDescriptor(
                    serviceName = "2",
                    operatorCode = "TEST"
                ),
                colours = FakeServiceColours(
                    colourPrimary = 200,
                    colourOnPrimary = 201
                )
            )
        )
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyServiceDao(
            database = FakeBusStopDatabase(
                onServiceDao = {
                    FakeServiceDao(
                        onGetServiceNamesWithColourFlow = {
                            assertEquals("123456", it)
                            flows.removeFirst()
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.getServiceNamesWithColourFlow("123456").test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    @Test
    fun serviceCountFlowRespondsToDatabaseOpenStatus() = runTest {
        val flows = ArrayDeque(listOf(flowOf(0), flowOf(10)))
        val dao = createProxyServiceDao(
            database = FakeBusStopDatabase(
                onServiceDao = {
                    FakeServiceDao(
                        onServiceCountFlow = { flows.removeFirst() }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.serviceCountFlow.test {
            assertEquals(0, awaitItem())
            assertEquals(10, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    @Test
    fun getColoursForServicesFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = mapOf<ServiceDescriptor, ServiceColours>(
            FakeServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST"
            ) to FakeServiceColours(
                colourPrimary = 100,
                colourOnPrimary = 101
            )
        )
        val second = mapOf<ServiceDescriptor, ServiceColours>(
            FakeServiceDescriptor(
                serviceName = "2",
                operatorCode = "TEST"
            ) to FakeServiceColours(
                colourPrimary = 200,
                colourOnPrimary = 201
            )
        )
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyServiceDao(
            database = FakeBusStopDatabase(
                onServiceDao = {
                    FakeServiceDao(
                        onGetColoursForServicesFlow = {
                            assertNull(it)
                            flows.removeFirst()
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.getColoursForServicesFlow(null).test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    @Test
    fun getServiceDetailsFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = listOf(
            FakeServiceDetails(
                descriptor = FakeServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST"
                ),
                description = "Description 1",
                colours = FakeServiceColours(
                    colourPrimary = 100,
                    colourOnPrimary = 101
                )
            )
        )
        val second = listOf(
            FakeServiceDetails(
                descriptor = FakeServiceDescriptor(
                    serviceName = "2",
                    operatorCode = "TEST"
                ),
                description = "Description 2",
                colours = FakeServiceColours(
                    colourPrimary = 200,
                    colourOnPrimary = 201
                )
            )
        )
        val flows = ArrayDeque(
            listOf(
                flowOf(first),
                flowOf(second)
            )
        )
        val dao = createProxyServiceDao(
            database = FakeBusStopDatabase(
                onServiceDao = {
                    FakeServiceDao(
                        onGetServiceDetailsFlow = {
                            assertEquals("123456", it)
                            flows.removeFirst()
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.getServiceDetailsFlow("123456").test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(flows.isEmpty())
    }

    private fun createProxyServiceDao(
        database: BusStopDatabase = FakeBusStopDatabase()
    ): ProxyServiceDao {
        return ProxyServiceDao(database = database)
    }
}
