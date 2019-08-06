/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.busstop.daos

import android.content.ContentProvider
import android.content.ContentResolver
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.test.mock.MockContentProvider
import android.test.mock.MockContentResolver
import android.test.mock.MockContext
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.busstop.DatabaseInformationContract

/**
 * Tests for [AndroidDatabaseInformationDao].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AndroidDatabaseInformationDaoTest {

    companion object {

        private const val TEST_AUTHORITY = "test.authority"
    }

    @Mock
    internal lateinit var contract: DatabaseInformationContract

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: MockContentResolver
    private val contentUri = Uri.parse("content://$TEST_AUTHORITY/tableName")

    private lateinit var databaseInformationDao: AndroidDatabaseInformationDao

    @Before
    fun setUp() {
        mockContentResolver = MockContentResolver()
        mockContext = object : MockContext() {
            override fun getContentResolver(): ContentResolver = mockContentResolver
        }

        databaseInformationDao = AndroidDatabaseInformationDao(mockContext, contract)

        whenever(contract.getContentUri())
                .thenReturn(contentUri)
    }

    @Test
    fun checkGetTopologyIdSendsThroughCorrectParametersForQuery() {
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertNotNull(projection)
                assertEquals(1, projection?.size)
                assertEquals(DatabaseInformationContract.CURRENT_TOPOLOGY_ID, projection?.get(0))
                assertNull(selection)
                assertNull(selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also { addMockProvider(it) }

        databaseInformationDao.getTopologyId()
    }

    @Test
    fun getTopologyIdReturnsNullCursorWhenContentProviderQueryReturnsNull() {
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                return null
            }
        }.also { addMockProvider(it) }

        val result = databaseInformationDao.getTopologyId()

        assertNull(result)
    }

    @Test
    fun getTopologyIdReturnsNullWhenAnEmptyCursorIsReturned() {
        val columns = arrayOf(DatabaseInformationContract.CURRENT_TOPOLOGY_ID)
        val cursor = MatrixCursor(columns)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                return cursor
            }
        }.also { addMockProvider(it) }

        val result = databaseInformationDao.getTopologyId()

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getTopologyIdReturnsTopologyIdWhenPopulatedCursorIsReturned() {
        val columns = arrayOf(DatabaseInformationContract.CURRENT_TOPOLOGY_ID)
        val cursor = MatrixCursor(columns)
        cursor.addRow(arrayOf("testTopoId"))
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                return cursor
            }
        }.also { addMockProvider(it) }

        val result = databaseInformationDao.getTopologyId()

        assertEquals("testTopoId", result)
        assertTrue(cursor.isClosed)
    }

    private fun addMockProvider(provider: ContentProvider) {
        mockContentResolver.addProvider(TEST_AUTHORITY, provider)
    }
}