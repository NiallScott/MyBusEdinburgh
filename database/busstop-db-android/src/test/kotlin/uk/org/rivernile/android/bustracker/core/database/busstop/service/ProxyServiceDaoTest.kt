/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.AndroidBusStopDatabase
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [ProxyServiceDao].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class ProxyServiceDaoTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var database: AndroidBusStopDatabase

    @Mock
    private lateinit var roomServiceDao: RoomServiceDao

    private lateinit var dao: ProxyServiceDao

    @Before
    fun setUp() {
        dao = ProxyServiceDao(database)

        whenever(database.roomServiceDao)
            .thenReturn(roomServiceDao)
    }

    @Test
    fun allServiceNamesFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = mock<List<String>>()
        val second = mock<List<String>>()
        whenever(database.isDatabaseOpenFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        whenever(roomServiceDao.allServiceNamesFlow)
            .thenReturn(
                flowOf(first),
                flowOf(second))

        val observer = dao.allServiceNamesFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(first, second)
    }

    @Test
    fun serviceCountFlowRespondsToDatabaseOpenStatus() = runTest {
        whenever(database.isDatabaseOpenFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        whenever(roomServiceDao.serviceCountFlow)
            .thenReturn(
                flowOf(0),
                flowOf(10))

        val observer = dao.serviceCountFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(0, 10)
    }

    @Test
    fun getColoursForServicesFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = mock<Map<String, Int>>()
        val second = mock<Map<String, Int>>()
        whenever(database.isDatabaseOpenFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        whenever(roomServiceDao.getColoursForServicesFlow(anyOrNull()))
            .thenReturn(
                flowOf(first),
                flowOf(second))

        val observer = dao.getColoursForServicesFlow(null).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(first, second)
    }

    @Test
    fun getServiceDetailsFlowRespondsToDatabaseOpenStatus() = runTest {
        val first = mock<List<RoomServiceDetails>>()
        val second = mock<List<RoomServiceDetails>>()
        whenever(database.isDatabaseOpenFlow)
            .thenReturn(intervalFlowOf(0L, 10L, true, false, true))
        whenever(roomServiceDao.getServiceDetailsFlow("123456"))
            .thenReturn(
                flowOf(first),
                flowOf(second))

        val observer = dao.getServiceDetailsFlow("123456").test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(first, second)
    }
}