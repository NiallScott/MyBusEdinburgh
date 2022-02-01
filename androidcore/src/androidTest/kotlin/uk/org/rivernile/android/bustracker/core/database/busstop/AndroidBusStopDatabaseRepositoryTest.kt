/*
 * Copyright (C) 2019 - 2022 Niall 'Rivernile' Scott
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

import android.content.ContentResolver
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.test.mock.MockContentProvider
import android.test.mock.MockContentResolver
import android.test.mock.MockContext
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.busstop.daos.DatabaseInformationDao
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.DatabaseMetadata
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test
import java.io.File
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [AndroidBusStopDatabaseRepository].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AndroidBusStopDatabaseRepositoryTest {

    companion object {

        private const val TEST_AUTHORITY = "test.authority"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var contract: BusStopDatabaseContract
    @Mock
    private lateinit var databaseInformationDao: DatabaseInformationDao

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: MockContentResolver
    private val contentUri = Uri.parse("content://$TEST_AUTHORITY/tableName")

    private lateinit var database: AndroidBusStopDatabaseRepository

    @Before
    fun setUp() {
        mockContentResolver = MockContentResolver()
        mockContext = object : MockContext() {
            override fun getContentResolver(): ContentResolver = mockContentResolver
        }

        database = AndroidBusStopDatabaseRepository(
                mockContext,
                contract,
                databaseInformationDao)

        whenever(contract.getContentUri())
                .thenReturn(contentUri)
    }

    @Test
    fun replaceDatabaseCallsCorrectParametersOnContentResolver() {
        val fakeFile = File("/fake/file/path.db")
        val mockContentProvider = object : MockContentProvider() {
            override fun call(method: String,
                              arg: String?,
                              extras: Bundle?): Bundle? {
                assertEquals(BusStopDatabaseContract.METHOD_REPLACE_DATABASE, method)
                assertEquals(fakeFile.absolutePath, arg)
                assertNull(extras)

                return null
            }
        }

        mockContentResolver.addProvider(TEST_AUTHORITY, mockContentProvider)

        database.replaceDatabase(fakeFile)
    }

    @Test
    fun databaseMetadataFlowReturnsFlowFromDatabaseInformationDao() = runBlockingTest {
        val databaseMetadata = DatabaseMetadata(123L, "1.2.3")
        whenever(databaseInformationDao.databaseMetadataFlow)
                .thenReturn(flowOf(databaseMetadata))

        val observer = database.databaseMetadataFlow.test(this)
        advanceUntilIdle()

        observer.assertValues(databaseMetadata)
    }

    private val runBlockingTest = coroutineRule::runBlockingTest
}