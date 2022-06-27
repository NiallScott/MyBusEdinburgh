/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopsContract
import uk.org.rivernile.android.bustracker.core.database.busstop.ServiceStopsContract
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetailsWithServices
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopLocation
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [AndroidBusStopsDao].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AndroidBusStopsDaoTest {

    companion object {

        private const val TEST_AUTHORITY = "test.authority"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var contract: BusStopsContract

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: MockContentResolver
    private val contentUri = Uri.parse("content://$TEST_AUTHORITY/tableName")

    private lateinit var busStopsDao: AndroidBusStopsDao

    @Before
    fun setUp() {
        mockContentResolver = MockContentResolver()
        mockContext = object : MockContext() {
            override fun getContentResolver() = mockContentResolver
        }

        busStopsDao = AndroidBusStopsDao(mockContext, contract, coroutineRule.testDispatcher)

        whenever(contract.getContentUri())
                .thenReturn(contentUri)
    }

    @Test
    fun getNameForStopWithNullResultIsHandledCorrectly() = runTest {
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
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getNameForStop("123456")

        assertNull(result)
    }

    @Test
    fun getNameForStopWithEmptyResultIsHandledCorrectly() = runTest {
        val expectedProjection = getExpectedProjectionForStopName()
        val expectedSelectionArgs = arrayOf("123456")
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getNameForStop("123456")

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getNameForStopWithNonEmptyResultButNullNameIsHandledCorrectly() = runTest {
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
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getNameForStop("123456")

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getNameForStopWithNonEmptyResultButNullLocalityIsHandledCorrectly() = runTest {
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
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)
        val expected = StopName("Stop name", null)

        val result = busStopsDao.getNameForStop("123456")

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getNameForStopWithNonEmptyResultIsHandledCorrectly() = runTest {
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
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)
        val expected = StopName("Stop name", "Locality")

        val result = busStopsDao.getNameForStop("123456")

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getLocationForStopWithNullResultIsHandledCorrectly() = runTest {
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
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getLocationForStop("123456")

        assertNull(result)
    }

    @Test
    fun getLocationForStopWithEmptyResultIsHandledCorrectly() = runTest {
        val expectedProjection = getExpectedProjectionForStopLocation()
        val expectedSelectionArgs = arrayOf("123456")
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getLocationForStop("123456")

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getLocationForStopWithNonEmptyResultIsHandledCorrectly() = runTest {
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
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)
        val expected = StopLocation("123456", 1.0, 2.0)

        val result = busStopsDao.getLocationForStop("123456")

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getStopDetailsWithNullCursorReturnsNull() = runTest {
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
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getStopDetails("123456")

        assertNull(result)
    }

    @Test
    fun getStopDetailsWithEmptyCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForStopDetails()
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getStopDetails("123456")

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getStopDetailsWithNonEmptyCursorReturnsStopDetails() = runTest {
        val expectedProjection = getExpectedProjectionForStopDetails()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("Stop name", "Locality", 1.2, 3.4, 4))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)
        val expected = StopDetails(
                "123456",
                StopName(
                        "Stop name",
                        "Locality"),
                1.2,
                3.4,
                4)

        val result = busStopsDao.getStopDetails("123456")

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getStopDetailsMultiWithEmptyStopCodesReturnsNull() = runTest {
        val result = busStopsDao.getStopDetails(emptySet())

        assertNull(result)
    }

    @Test
    fun getStopDetailsMultiWithNullCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForStopDetailsMulti()
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} IN (?,?,?)", selection)
                assertArrayEquals(arrayOf("123456", "123457", "123458"), selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getStopDetails(setOf("123456", "123457", "123458"))

        assertNull(result)
    }

    @Test
    fun getStopDetailsMultiWithEmptyCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForStopDetailsMulti()
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} IN (?,?,?)", selection)
                assertArrayEquals(arrayOf("123456", "123457", "123458"), selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getStopDetails(setOf("123456", "123457", "123458"))

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getStopDetailsMultiWithNonEmptyCursorReturnsStopDetails() = runTest {
        val expectedProjection = getExpectedProjectionForStopDetailsMulti()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("123456", "Stop name 1", "Locality 1", 1.2, 3.4, 4))
        cursor.addRow(arrayOf("123457", "Stop name 2", "Locality 2", 1.3, 3.5, 5))
        cursor.addRow(arrayOf("123458", "Stop name 3", "Locality 3", 1.4, 3.6, 6))
        val expected = mapOf(
                "123456" to StopDetails(
                        "123456",
                        StopName(
                                "Stop name 1",
                                "Locality 1"),
                        1.2,
                        3.4,
                        4),
                "123457" to StopDetails(
                        "123457",
                        StopName(
                                "Stop name 2",
                                "Locality 2"),
                        1.3,
                        3.5,
                        5),
                "123458" to StopDetails(
                        "123458",
                        StopName(
                                "Stop name 3",
                                "Locality 3"),
                        1.4,
                        3.6,
                        6))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} IN (?,?,?)", selection)
                assertArrayEquals(arrayOf("123456", "123457", "123458"), selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getStopDetails(setOf("123456", "123457", "123458"))

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getServicesForStopsWithEmptyStopCodesReturnsNull() = runTest {
        val result = busStopsDao.getServicesForStops(emptySet())

        assertNull(result)
    }

    @Test
    fun getServicesForStopsWithNullCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForStopsServices()
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} IN (?)", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getServicesForStops(setOf("123456"))

        assertNull(result)
    }

    @Test
    fun getServicesForStopsWithEmptyCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForStopsServices()
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} IN (?,?,?)", selection)
                assertArrayEquals(arrayOf("111111", "222222", "333333"), selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getServicesForStops(setOf("111111", "222222", "333333"))

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getServicesForStopsWithNonEmptyCursorReturnsResult() = runTest {
        val expectedProjection = getExpectedProjectionForStopsServices()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("111111", "1, 2, 3"))
        cursor.addRow(arrayOf("222222", " 3 , 4 , 5 "))
        cursor.addRow(arrayOf("333333", "6,7, ,,,,"))
        cursor.addRow(arrayOf("444444", null))
        val expected = mapOf(
                "111111" to listOf("1", "2", "3"),
                "222222" to listOf("3", "4", "5"),
                "333333" to listOf("6", "7"))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${BusStopsContract.STOP_CODE} IN (?,?,?)", selection)
                assertArrayEquals(arrayOf("111111", "222222", "333333"), selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getServicesForStops(setOf("111111", "222222", "333333"))

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getStopDetailsWithSpanReturnsNullWhenCursorIsNull() = runTest {
        val expectedProjection = getExpectedProjectionForStopDetailsWithServices()
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
                assertEquals("(${BusStopsContract.LATITUDE} BETWEEN ? AND ?) AND " +
                        "(${BusStopsContract.LONGITUDE} BETWEEN ? AND ?)", selection)
                assertArrayEquals(
                        arrayOf(
                                1.0.toString(),
                                3.0.toString(),
                                2.0.toString(),
                                4.0.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getStopDetailsWithinSpan(1.0, 2.0, 3.0, 4.0)

        assertNull(result)
    }

    @Test
    fun getStopDetailsWithSpanReturnsNullWhenCursorIsEmpty() = runTest {
        val expectedProjection = getExpectedProjectionForStopDetailsWithServices()
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
                assertEquals("(${BusStopsContract.LATITUDE} BETWEEN ? AND ?) AND " +
                        "(${BusStopsContract.LONGITUDE} BETWEEN ? AND ?)", selection)
                assertArrayEquals(
                        arrayOf(
                                1.0.toString(),
                                3.0.toString(),
                                2.0.toString(),
                                4.0.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getStopDetailsWithinSpan(1.0, 2.0, 3.0, 4.0)

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getStopDetailsWithSpanReturnsItemWhenCursorHasSingleItem() = runTest {
        val expectedProjection = getExpectedProjectionForStopDetailsWithServices()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("123456", "Stop name", "Locality", 1.0, 2.0, 1, "1, 2, 3"))
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
                assertEquals("(${BusStopsContract.LATITUDE} BETWEEN ? AND ?) AND " +
                        "(${BusStopsContract.LONGITUDE} BETWEEN ? AND ?)", selection)
                assertArrayEquals(
                        arrayOf(
                                1.0.toString(),
                                3.0.toString(),
                                2.0.toString(),
                                4.0.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getStopDetailsWithinSpan(1.0, 2.0, 3.0, 4.0)

        assertEquals(
                listOf(
                        StopDetailsWithServices(
                                "123456",
                                StopName(
                                        "Stop name",
                                        "Locality"),
                                1.0,
                                2.0,
                                1,
                                "1, 2, 3")),
                result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getStopDetailsWithSpanReturnsItemsWhenCursorHasMultipleItems() = runTest {
        val expectedProjection = getExpectedProjectionForStopDetailsWithServices()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("123456", "Stop name 1", "Locality 1", 1.0, 2.0, 1, "1, 2, 3"))
        cursor.addRow(arrayOf("123457", "Stop name 2", "Locality 2", 3.0, 4.0, 2, "4, 5, 6"))
        cursor.addRow(arrayOf("123458", "Stop name 3", "Locality 3", 5.0, 6.0, 3, "7, 8, 9"))
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
                assertEquals("(${BusStopsContract.LATITUDE} BETWEEN ? AND ?) AND " +
                        "(${BusStopsContract.LONGITUDE} BETWEEN ? AND ?)", selection)
                assertArrayEquals(
                        arrayOf(
                                1.0.toString(),
                                3.0.toString(),
                                2.0.toString(),
                                4.0.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getStopDetailsWithinSpan(1.0, 2.0, 3.0, 4.0)

        assertEquals(
                listOf(
                        StopDetailsWithServices(
                                "123456",
                                StopName(
                                        "Stop name 1",
                                        "Locality 1"),
                                1.0,
                                2.0,
                                1,
                                "1, 2, 3"),
                        StopDetailsWithServices(
                                "123457",
                                StopName(
                                        "Stop name 2",
                                        "Locality 2"),
                                3.0,
                                4.0,
                                2,
                                "4, 5, 6"),
                        StopDetailsWithServices(
                                "123458",
                                StopName(
                                        "Stop name 3",
                                        "Locality 3"),
                                5.0,
                                6.0,
                                3,
                                "7, 8, 9")),
                result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getStopDetailsWithSpanFilteredReturnsNullWhenCursorIsNull() = runTest {
        val expectedProjection = getExpectedProjectionForStopDetailsWithServices()
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
                assertEquals("(${BusStopsContract.LATITUDE} BETWEEN ? AND ?) AND " +
                        "(${BusStopsContract.LONGITUDE} BETWEEN ? AND ?) " +
                        "AND ${BusStopsContract.STOP_CODE} IN (" +
                        "SELECT ${ServiceStopsContract.STOP_CODE} " +
                        "FROM ${ServiceStopsContract.TABLE_NAME} " +
                        "WHERE ${ServiceStopsContract.SERVICE_NAME} IN (?))",
                        selection)
                assertArrayEquals(
                        arrayOf(
                                1.0.toString(),
                                3.0.toString(),
                                2.0.toString(),
                                4.0.toString(),
                                "1"),
                        selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getStopDetailsWithinSpan(1.0, 2.0, 3.0, 4.0, listOf("1"))

        assertNull(result)
    }

    @Test
    fun getStopDetailsWithSpanFilteredReturnsNullWhenCursorIsEmpty() = runTest {
        val expectedProjection = getExpectedProjectionForStopDetailsWithServices()
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
                assertEquals("(${BusStopsContract.LATITUDE} BETWEEN ? AND ?) AND " +
                        "(${BusStopsContract.LONGITUDE} BETWEEN ? AND ?) " +
                        "AND ${BusStopsContract.STOP_CODE} IN (" +
                        "SELECT ${ServiceStopsContract.STOP_CODE} " +
                        "FROM ${ServiceStopsContract.TABLE_NAME} " +
                        "WHERE ${ServiceStopsContract.SERVICE_NAME} IN (?,?,?))",
                        selection)
                assertArrayEquals(
                        arrayOf(
                                1.0.toString(),
                                3.0.toString(),
                                2.0.toString(),
                                4.0.toString(),
                                "1",
                                "2",
                                "3"),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getStopDetailsWithinSpan(1.0, 2.0, 3.0, 4.0, listOf("1", "2", "3"))

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getStopDetailsWithSpanFilteredReturnsItemWhenCursorHasSingleItem() = runTest {
        val expectedProjection = getExpectedProjectionForStopDetailsWithServices()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("123456", "Stop name", "Locality", 1.0, 2.0, 1, "1, 2, 3"))
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
                assertEquals("(${BusStopsContract.LATITUDE} BETWEEN ? AND ?) AND " +
                        "(${BusStopsContract.LONGITUDE} BETWEEN ? AND ?) " +
                        "AND ${BusStopsContract.STOP_CODE} IN (" +
                        "SELECT ${ServiceStopsContract.STOP_CODE} " +
                        "FROM ${ServiceStopsContract.TABLE_NAME} " +
                        "WHERE ${ServiceStopsContract.SERVICE_NAME} IN (?,?,?))",
                        selection)
                assertArrayEquals(
                        arrayOf(
                                1.0.toString(),
                                3.0.toString(),
                                2.0.toString(),
                                4.0.toString(),
                                "1",
                                "2",
                                "3"),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getStopDetailsWithinSpan(1.0, 2.0, 3.0, 4.0, listOf("1", "2", "3"))

        assertEquals(
                listOf(
                        StopDetailsWithServices(
                                "123456",
                                StopName(
                                        "Stop name",
                                        "Locality"),
                                1.0,
                                2.0,
                                1,
                                "1, 2, 3")),
                result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getStopDetailsWithSpanFilteredReturnsItemsWhenCursorHasMultipleItems() = runTest {
        val expectedProjection = getExpectedProjectionForStopDetailsWithServices()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("123456", "Stop name 1", "Locality 1", 1.0, 2.0, 1, "1, 2, 3"))
        cursor.addRow(arrayOf("123457", "Stop name 2", "Locality 2", 3.0, 4.0, 2, "4, 5, 6"))
        cursor.addRow(arrayOf("123458", "Stop name 3", "Locality 3", 5.0, 6.0, 3, "7, 8, 9"))
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
                assertEquals("(${BusStopsContract.LATITUDE} BETWEEN ? AND ?) AND " +
                        "(${BusStopsContract.LONGITUDE} BETWEEN ? AND ?) " +
                        "AND ${BusStopsContract.STOP_CODE} IN (" +
                        "SELECT ${ServiceStopsContract.STOP_CODE} " +
                        "FROM ${ServiceStopsContract.TABLE_NAME} " +
                        "WHERE ${ServiceStopsContract.SERVICE_NAME} IN (?,?,?))",
                        selection)
                assertArrayEquals(
                        arrayOf(
                                1.0.toString(),
                                3.0.toString(),
                                2.0.toString(),
                                4.0.toString(),
                                "1",
                                "2",
                                "3"),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidBusStopsDaoTest::addMockProvider)

        val result = busStopsDao.getStopDetailsWithinSpan(1.0, 2.0, 3.0, 4.0, listOf("1", "2", "3"))

        assertEquals(
                listOf(
                        StopDetailsWithServices(
                                "123456",
                                StopName(
                                        "Stop name 1",
                                        "Locality 1"),
                                1.0,
                                2.0,
                                1,
                                "1, 2, 3"),
                        StopDetailsWithServices(
                                "123457",
                                StopName(
                                        "Stop name 2",
                                        "Locality 2"),
                                3.0,
                                4.0,
                                2,
                                "4, 5, 6"),
                        StopDetailsWithServices(
                                "123458",
                                StopName(
                                        "Stop name 3",
                                        "Locality 3"),
                                5.0,
                                6.0,
                                3,
                                "7, 8, 9")),
                result)
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
            BusStopsContract.LONGITUDE,
            BusStopsContract.ORIENTATION)

    private fun getExpectedProjectionForStopDetailsMulti() = arrayOf(
            BusStopsContract.STOP_CODE,
            BusStopsContract.STOP_NAME,
            BusStopsContract.LOCALITY,
            BusStopsContract.LATITUDE,
            BusStopsContract.LONGITUDE,
            BusStopsContract.ORIENTATION)

    private fun getExpectedProjectionForStopsServices() = arrayOf(
            BusStopsContract.STOP_CODE,
            BusStopsContract.SERVICE_LISTING)

    private fun getExpectedProjectionForStopDetailsWithServices() = arrayOf(
            BusStopsContract.STOP_CODE,
            BusStopsContract.STOP_NAME,
            BusStopsContract.LOCALITY,
            BusStopsContract.LATITUDE,
            BusStopsContract.LONGITUDE,
            BusStopsContract.ORIENTATION,
            BusStopsContract.SERVICE_LISTING)
}