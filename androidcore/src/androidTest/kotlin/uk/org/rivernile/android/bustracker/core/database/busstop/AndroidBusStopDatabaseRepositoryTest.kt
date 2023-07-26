/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Ignore
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.database.DatabaseDao
import uk.org.rivernile.android.bustracker.core.database.busstop.database.DatabaseMetadata
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test
import java.io.File
import kotlin.test.assertFalse
import kotlin.test.assertTrue

/**
 * Tests for [AndroidBusStopDatabaseRepository].
 *
 * @author Niall Scott
 */
@Ignore("Cannot mock/spy class uk.org.rivernile.android.bustracker.core.database.busstop" +
        ".AndroidBusStopDatabase")
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AndroidBusStopDatabaseRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var database: AndroidBusStopDatabase
    @Mock
    private lateinit var databaseDao: DatabaseDao

    private lateinit var repository: AndroidBusStopDatabaseRepository

    @Before
    fun setUp() {
        repository = AndroidBusStopDatabaseRepository(
                database,
                databaseDao)
    }

    @Test
    fun replaceDatabaseReturnsFalseWhenReplaceDatabaseReturnsFalse() = runTest {
        val fakeFile = File("/fake/file/path.db")
        whenever(repository.replaceDatabase(fakeFile))
            .thenReturn(false)

        val result = repository.replaceDatabase(fakeFile)

        assertFalse(result)
    }

    @Test
    fun replaceDatabaseReturnsTrueWhenReplaceDatabaseReturnsTrue() = runTest {
        val fakeFile = File("/fake/file/path.db")
        whenever(repository.replaceDatabase(fakeFile))
            .thenReturn(true)

        val result = repository.replaceDatabase(fakeFile)

        assertTrue(result)
    }

    @Test
    fun databaseMetadataFlowReturnsFlowFromDatabaseInformationDao() = runTest {
        val databaseMetadata = mock<DatabaseMetadata>()
        whenever(databaseDao.databaseMetadataFlow)
            .thenReturn(flowOf(databaseMetadata))

        val observer = repository.databaseMetadataFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(databaseMetadata)
    }

    @Test
    fun getTopologyVersionIdReturnsTopologyIdFromDao() = runTest {
        whenever(databaseDao.topologyIdFlow)
            .thenReturn(flowOf("topoId"))

        val result = repository.getTopologyVersionId()

        assertEquals("topoId", result)
    }
}