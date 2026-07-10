/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.operator

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopDatabase
import uk.org.rivernile.android.bustracker.core.database.busstop.FakeBusStopDatabase
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [ProxyOperatorDao].
 *
 * @author Niall Scott
 */
class ProxyOperatorDaoTest {

    @Test
    fun allOperatorNamesFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = mapOf(
            "TEST1" to FakeOperatorName(displayName = "Name 1")
        )
        val second = mapOf(
            "TEST2" to FakeOperatorName(displayName = "Name 2")
        )
        val operatorNameMappingsQueue = ArrayDeque(listOf(first, second))
        val dao = createProxyOperatorDao(
            database = FakeBusStopDatabase(
                onOperatorDao = {
                    FakeOperatorDao(
                        onAllOperatorNamesFlow = {
                            flowOf(operatorNameMappingsQueue.removeFirst())
                        }
                    )
                },
                onIsDatabaseOpenFlow = { intervalFlowOf(0L, 10L, true, false, true) }
            )
        )

        dao.allOperatorNamesFlow.test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
        assertTrue(operatorNameMappingsQueue.isEmpty())
    }

    private fun createProxyOperatorDao(
        database: BusStopDatabase = FakeBusStopDatabase()
    ): ProxyOperatorDao {
        return ProxyOperatorDao(database = database)
    }
}
