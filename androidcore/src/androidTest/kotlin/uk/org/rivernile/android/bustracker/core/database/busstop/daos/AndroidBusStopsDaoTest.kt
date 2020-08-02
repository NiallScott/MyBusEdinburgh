/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopsContract
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [AndroidBusStopsDao].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AndroidBusStopsDaoTest {

    companion object {

        private const val TEST_AUTHORITY = "test.authority"
    }

    @Mock
    private lateinit var contract: BusStopsContract

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: MockContentResolver
    private val contentUri = Uri.parse("content://$TEST_AUTHORITY/tableName")

    private lateinit var busStopsDao: BusStopsDao

    @Before
    fun setUp() {
        mockContentResolver = MockContentResolver()
        mockContext = object : MockContext() {
            override fun getContentResolver() = mockContentResolver
        }

        busStopsDao = AndroidBusStopsDao(mockContext, contract)

        whenever(contract.getContentUri())
                .thenReturn(contentUri)
    }

    @Test
    fun getNameForStopWithNullResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForStopName()
        val expectedSelectionArgs = arrayOf("123456")
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this::addMockProvider)

        val result = busStopsDao.getNameForStop("123456")

        assertNull(result)
    }

    @Test
    fun getNameForStopWithEmptyResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForStopName()
        val expectedSelectionArgs = arrayOf("123456")
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = busStopsDao.getNameForStop("123456")

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getNameForStopWithNonEmptyResultButNullNameIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForStopName()
        val expectedSelectionArgs = arrayOf("123456")
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(null as String?, null as String?))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = busStopsDao.getNameForStop("123456")

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getNameForStopWithNonEmptyResultButNullLocalityIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForStopName()
        val expectedSelectionArgs = arrayOf("123456")
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("Stop name", null as String?))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)
        val expected = StopName("Stop name", null)

        val result = busStopsDao.getNameForStop("123456")

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getNameForStopWithNonEmptyResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForStopName()
        val expectedSelectionArgs = arrayOf("123456")
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("Stop name", "Locality"))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)
        val expected = StopName("Stop name", "Locality")

        val result = busStopsDao.getNameForStop("123456")

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getLocationForStopWithNullResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForStopLocation()
        val expectedSelectionArgs = arrayOf("123456")
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this::addMockProvider)

        val result = busStopsDao.getLocationForStop("123456")

        assertNull(result)
    }

    @Test
    fun getLocationForStopWithEmptyResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForStopLocation()
        val expectedSelectionArgs = arrayOf("123456")
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = busStopsDao.getLocationForStop("123456")

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getLocationForStopWithNonEmptyResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForStopLocation()
        val expectedSelectionArgs = arrayOf("123456")
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(1.0, 2.0))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)
        val expected = StopLocation("123456", 1.0, 2.0)

        val result = busStopsDao.getLocationForStop("123456")

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getStopDetailsWithNullCursorReturnsNull() {
        val expectedProjection = getExpectedProjectionForStopDetails()
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this::addMockProvider)

        val result = busStopsDao.getStopDetails("123456")

        assertNull(result)
    }

    @Test
    fun getStopDetailsWithEmptyCursorReturnsNull() {
        val expectedProjection = getExpectedProjectionForStopDetails()
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = busStopsDao.getStopDetails("123456")

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getStopDetailsWithNonEmptyCursorReturnsStopDetails() {
        val expectedProjection = getExpectedProjectionForStopDetails()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("Stop name", "Locality", 1.2, 3.4))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)
        val expected = StopDetails(
                "123456",
                StopName(
                        "Stop name",
                        "Locality"),
                1.2,
                3.4)

        val result = busStopsDao.getStopDetails("123456")

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    private fun addMockProvider(provider: ContentProvider) {
        mockContentResolver.addProvider(TEST_AUTHORITY, provider)
    }

    private fun getExpectedProjectionForStopName() = arrayOf(
            BusStopsContract.STOP_NAME,
            BusStopsContract.LOCALITY)

    private fun getExpectedProjectionForStopLocation() = arrayOf(
            BusStopsContract.LATITUDE,
            BusStopsContract.LONGITUDE)

    private fun getExpectedProjectionForStopDetails() = arrayOf(
            BusStopsContract.STOP_NAME,
            BusStopsContract.LOCALITY,
            BusStopsContract.LATITUDE,
            BusStopsContract.LONGITUDE)
}