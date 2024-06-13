/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.AndroidBusStopDatabase
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [ProxyServicePointDao].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class ProxyServicePointDaoTest {

    @Mock
    private lateinit var database: AndroidBusStopDatabase

    @Mock
    private lateinit var roomServicePointDao: RoomServicePointDao

    private lateinit var dao: ProxyServicePointDao

    @BeforeTest
    fun setUp() {
        dao = ProxyServicePointDao(database)

        whenever(database.roomServicePointDao)
            .thenReturn(roomServicePointDao)
    }

    @Test
    fun getServicePointsFlow() = runTest {
        val first = mock<List<ServicePoint>>()
        val second = mock<List<ServicePoint>>()
        whenever(database.isDatabaseOpenFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        whenever(roomServicePointDao.getServicePointsFlow(anyOrNull()))
            .thenReturn(
                flowOf(first),
                flowOf(second)
            )

        dao.getServicePointsFlow(null).test {
            assertEquals(first, awaitItem())
            assertEquals(second, awaitItem())
            awaitComplete()
        }
    }
}