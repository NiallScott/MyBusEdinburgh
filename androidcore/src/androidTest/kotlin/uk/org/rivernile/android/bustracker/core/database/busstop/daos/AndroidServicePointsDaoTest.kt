/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.os.CancellationSignal
import android.test.mock.MockContentProvider
import android.test.mock.MockContentResolver
import android.test.mock.MockContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.ServicePointsContract
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.ServicePoint
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [AndroidServicePointsDao].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AndroidServicePointsDaoTest {

    companion object {

        private const val TEST_AUTHORITY = "test.authority"

        private const val SERVICE_POINTS_SORT_CLAUSE =
                "${ServicePointsContract.SERVICE_NAME} ASC, " +
                        "${ServicePointsContract.CHAINAGE} ASC, " +
                        "${ServicePointsContract.ORDER_VALUE} ASC"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var contract: ServicePointsContract

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: MockContentResolver
    private val contentUri = Uri.parse("content://$TEST_AUTHORITY/tableName")

    private lateinit var servicePointsDao: AndroidServicePointsDao

    @Before
    fun setUp() {
        mockContentResolver = MockContentResolver()
        mockContext = object : MockContext() {
            override fun getContentResolver() = mockContentResolver
        }

        servicePointsDao = AndroidServicePointsDao(
                mockContext,
                contract,
                coroutineRule.testDispatcher)

        whenever(contract.getContentUri())
                .thenReturn(contentUri)
    }

    @Test
    fun getServicePointsWithNullServiceNamesWithNullCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForServicePoints()
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?,
                    cancellationSignal: CancellationSignal?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertEquals(SERVICE_POINTS_SORT_CLAUSE, sortOrder)

                return null
            }
        }.also(this@AndroidServicePointsDaoTest::addMockProvider)

        val result = servicePointsDao.getServicePoints(null)

        assertNull(result)
    }

    @Test
    fun getServicePointsWithNullServiceNamesWithEmptyCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForServicePoints()
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?,
                    cancellationSignal: CancellationSignal?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertEquals(SERVICE_POINTS_SORT_CLAUSE, sortOrder)

                return cursor
            }
        }.also(this@AndroidServicePointsDaoTest::addMockProvider)

        val result = servicePointsDao.getServicePoints(null)

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getServicePointsWithNullServiceNamesWithPopulatedCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForServicePoints()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("123456", 1000, 1.1, 1.2))
        cursor.addRow(arrayOf("987654", 2000, 2.1, 2.2))
        cursor.addRow(arrayOf("123456", 3000, 3.1, 3.2))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?,
                    cancellationSignal: CancellationSignal?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertEquals(SERVICE_POINTS_SORT_CLAUSE, sortOrder)

                return cursor
            }
        }.also(this@AndroidServicePointsDaoTest::addMockProvider)
        val expected = listOf(
                ServicePoint("123456", 1000, 1.1, 1.2),
                ServicePoint("987654", 2000, 2.1, 2.2),
                ServicePoint("123456", 3000, 3.1, 3.2))

        val result = servicePointsDao.getServicePoints(null)

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getServicePointsWithEmptyServiceNamesWithNullCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForServicePoints()
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?,
                    cancellationSignal: CancellationSignal?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertEquals(SERVICE_POINTS_SORT_CLAUSE, sortOrder)

                return null
            }
        }.also(this@AndroidServicePointsDaoTest::addMockProvider)

        val result = servicePointsDao.getServicePoints(emptySet())

        assertNull(result)
    }

    @Test
    fun getServicePointsWithEmptyServiceNamesWithEmptyCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForServicePoints()
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?,
                    cancellationSignal: CancellationSignal?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertEquals(SERVICE_POINTS_SORT_CLAUSE, sortOrder)

                return cursor
            }
        }.also(this@AndroidServicePointsDaoTest::addMockProvider)

        val result = servicePointsDao.getServicePoints(emptySet())

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getServicePointsPopulatedEmptyServiceNamesWithPopulatedCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForServicePoints()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("123456", 1000, 1.1, 1.2))
        cursor.addRow(arrayOf("987654", 2000, 2.1, 2.2))
        cursor.addRow(arrayOf("123456", 3000, 3.1, 3.2))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?,
                    cancellationSignal: CancellationSignal?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertEquals(SERVICE_POINTS_SORT_CLAUSE, sortOrder)

                return cursor
            }
        }.also(this@AndroidServicePointsDaoTest::addMockProvider)
        val expected = listOf(
                ServicePoint("123456", 1000, 1.1, 1.2),
                ServicePoint("987654", 2000, 2.1, 2.2),
                ServicePoint("123456", 3000, 3.1, 3.2))

        val result = servicePointsDao.getServicePoints(emptySet())

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getServicePointsWithPopulatedServiceNamesWithNullCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForServicePoints()
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?,
                    cancellationSignal: CancellationSignal?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${ServicePointsContract.SERVICE_NAME} IN (?,?,?)", selection)
                assertArrayEquals(arrayOf("1", "2", "3"), selectionArgs)
                assertEquals(SERVICE_POINTS_SORT_CLAUSE, sortOrder)

                return null
            }
        }.also(this@AndroidServicePointsDaoTest::addMockProvider)

        val result = servicePointsDao.getServicePoints(setOf("1", "2", "3"))

        assertNull(result)
    }

    @Test
    fun getServicePointsWithPopulatedServiceNamesWithEmptyCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForServicePoints()
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?,
                    cancellationSignal: CancellationSignal?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${ServicePointsContract.SERVICE_NAME} IN (?,?,?)", selection)
                assertArrayEquals(arrayOf("1", "2", "3"), selectionArgs)
                assertEquals(SERVICE_POINTS_SORT_CLAUSE, sortOrder)

                return cursor
            }
        }.also(this@AndroidServicePointsDaoTest::addMockProvider)

        val result = servicePointsDao.getServicePoints(setOf("1", "2", "3"))

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getServicePointsWithEmptyServiceNamesWithPopulatedCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForServicePoints()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("123456", 1000, 1.1, 1.2))
        cursor.addRow(arrayOf("987654", 2000, 2.1, 2.2))
        cursor.addRow(arrayOf("123456", 3000, 3.1, 3.2))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?,
                    cancellationSignal: CancellationSignal?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${ServicePointsContract.SERVICE_NAME} IN (?,?,?)", selection)
                assertArrayEquals(arrayOf("1", "2", "3"), selectionArgs)
                assertEquals(SERVICE_POINTS_SORT_CLAUSE, sortOrder)

                return cursor
            }
        }.also(this@AndroidServicePointsDaoTest::addMockProvider)
        val expected = listOf(
                ServicePoint("123456", 1000, 1.1, 1.2),
                ServicePoint("987654", 2000, 2.1, 2.2),
                ServicePoint("123456", 3000, 3.1, 3.2))

        val result = servicePointsDao.getServicePoints(setOf("1", "2", "3"))

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    private fun addMockProvider(provider: ContentProvider) {
        mockContentResolver.addProvider(TEST_AUTHORITY, provider)
    }

    private fun getExpectedProjectionForServicePoints() = arrayOf(
            ServicePointsContract.SERVICE_NAME,
            ServicePointsContract.CHAINAGE,
            ServicePointsContract.LATITUDE,
            ServicePointsContract.LONGITUDE)
}