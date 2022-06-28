/*
 * Copyright (C) 2021 - 2022 Niall 'Rivernile' Scott
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
import android.test.mock.MockContentProvider
import android.test.mock.MockContentResolver
import android.test.mock.MockContext
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.ServiceStopsContract
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import kotlin.test.assertNull

/**
 * Tests for [AndroidServiceStopsDao].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class AndroidServiceStopsDaoTest {

    companion object {

        private const val TEST_AUTHORITY = "test.authority"

        private const val SERVICE_SORT_CLAUSE =
                "CASE WHEN ${ServiceStopsContract.SERVICE_NAME} GLOB '[^0-9.]*' THEN " +
                        "${ServiceStopsContract.SERVICE_NAME} ELSE " +
                        "cast(${ServiceStopsContract.SERVICE_NAME} AS int) END"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var contract: ServiceStopsContract

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: MockContentResolver
    private val contentUri = Uri.parse("content://$TEST_AUTHORITY/tableName")

    private lateinit var serviceStopsDao: ServiceStopsDao

    @Before
    fun setUp() {
        mockContentResolver = MockContentResolver()
        mockContext = object : MockContext() {
            override fun getContentResolver() = mockContentResolver
        }

        serviceStopsDao = AndroidServiceStopsDao(
                mockContext,
                contract,
                coroutineRule.testDispatcher)

        whenever(contract.getContentUri())
                .thenReturn(contentUri)
    }

    @Test
    fun getServicesForStopWithNullCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForServices()
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${ServiceStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertEquals(SERVICE_SORT_CLAUSE, sortOrder)

                return null
            }
        }.also(this@AndroidServiceStopsDaoTest::addMockProvider)

        val result = serviceStopsDao.getServicesForStop("123456")

        assertNull(result)
    }

    @Test
    fun getServicesForStopWithEmptyCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForServices()
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(arrayOf(ServiceStopsContract.SERVICE_NAME), projection)
                assertEquals("${ServiceStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertEquals(SERVICE_SORT_CLAUSE, sortOrder)

                return cursor
            }
        }.also(this@AndroidServiceStopsDaoTest::addMockProvider)

        val result = serviceStopsDao.getServicesForStop("123456")

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getServicesForStopWithSingleItemCursorReturnsExpectedList() = runTest {
        val expectedProjection = getExpectedProjectionForServices()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("1"))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(arrayOf(ServiceStopsContract.SERVICE_NAME), projection)
                assertEquals("${ServiceStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertEquals(SERVICE_SORT_CLAUSE, sortOrder)

                return cursor
            }
        }.also(this@AndroidServiceStopsDaoTest::addMockProvider)

        val result = serviceStopsDao.getServicesForStop("123456")

        assertEquals(listOf("1"), result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getServicesForStopWithMultipleItemsCursorReturnsExpectedList() = runTest {
        val expectedProjection = getExpectedProjectionForServices()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("1"))
        cursor.addRow(arrayOf("3"))
        cursor.addRow(arrayOf("5"))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(arrayOf(ServiceStopsContract.SERVICE_NAME), projection)
                assertEquals("${ServiceStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertEquals(SERVICE_SORT_CLAUSE, sortOrder)

                return cursor
            }
        }.also(this@AndroidServiceStopsDaoTest::addMockProvider)

        val result = serviceStopsDao.getServicesForStop("123456")

        assertEquals(listOf("1", "3", "5"), result)
        assertTrue(cursor.isClosed)
    }

    private fun addMockProvider(provider: ContentProvider) {
        mockContentResolver.addProvider(TEST_AUTHORITY, provider)
    }

    private fun getExpectedProjectionForServices() = arrayOf(ServiceStopsContract.SERVICE_NAME)
}