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

package uk.org.rivernile.android.bustracker.core.database.settings.daos

import android.content.ContentProvider
import android.content.ContentUris
import android.content.ContentValues
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
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.settings.AlertsContract
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert

/**
 * Tests for [AndroidAlertsDao].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AndroidAlertsDaoTest {

    companion object {

        private const val TEST_AUTHORITY = "test.authority"
    }

    @Mock
    internal lateinit var contract: AlertsContract

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: MockContentResolver
    private val contentUri = Uri.parse("content://$TEST_AUTHORITY/tableName")

    private lateinit var alertsDao: AndroidAlertsDao

    @Before
    fun setUp() {
        mockContentResolver = MockContentResolver()
        mockContext = object : MockContext() {
            override fun getContentResolver() = mockContentResolver
        }

        alertsDao = AndroidAlertsDao(mockContext, contract)

        whenever(contract.getContentUri())
                .thenReturn(contentUri)
    }

    @Test
    fun addArrivalAlertSendsThroughCorrectParametersForInsert() {
        val expected = ContentValues().apply {
            put(AlertsContract.TYPE, AlertsContract.ALERTS_TYPE_TIME)
            put(AlertsContract.TIME_ADDED, 123L)
            put(AlertsContract.STOP_CODE, "123456")
            put(AlertsContract.SERVICE_NAMES, "1,2,3")
            put(AlertsContract.TIME_TRIGGER, 5)
        }
        val alert = ArrivalAlert(0,
                123L,
                "123456",
                listOf("1", "2", "3"),
                5)
       object : MockContentProvider() {
            override fun insert(uri: Uri, values: ContentValues): Uri? {
                assertEquals(contentUri, uri)
                assertEquals(expected, values)

                return ContentUris.withAppendedId(uri, 1)
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.addArrivalAlert(alert)

        assertEquals(1, result)
    }

    @Test
    fun addArrivalAlertHandlesSingleServiceNameCorrectly() {
        val expected = ContentValues().apply {
            put(AlertsContract.TYPE, AlertsContract.ALERTS_TYPE_TIME)
            put(AlertsContract.TIME_ADDED, 123L)
            put(AlertsContract.STOP_CODE, "123456")
            put(AlertsContract.SERVICE_NAMES, "1")
            put(AlertsContract.TIME_TRIGGER, 5)
        }
        val alert = ArrivalAlert(0,
                123L,
                "123456",
                listOf("1"),
                5)
        object : MockContentProvider() {
            override fun insert(uri: Uri, values: ContentValues): Uri? {
                assertEquals(contentUri, uri)
                assertEquals(expected, values)

                return ContentUris.withAppendedId(uri, 1)
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.addArrivalAlert(alert)

        assertEquals(1, result)
    }

    @Test
    fun addProximityAlertSendsThroughCorrectParametersForInsert() {
        val expected = ContentValues().apply {
            put(AlertsContract.TYPE, AlertsContract.ALERTS_TYPE_PROXIMITY)
            put(AlertsContract.TIME_ADDED, 123L)
            put(AlertsContract.STOP_CODE, "123456")
            put(AlertsContract.DISTANCE_FROM, 10)
        }
        val alert = ProximityAlert(0,
                123L,
                "123456",
                10)
        object : MockContentProvider() {
            override fun insert(uri: Uri, values: ContentValues): Uri? {
                assertEquals(contentUri, uri)
                assertEquals(expected, values)

                return null
            }
        }.also { addMockProvider(it) }

        alertsDao.addProximityAlert(alert)
    }

    @Test
    fun removeArrivalAlertSendsThroughCorrectParametersForDelete() {
        val expectedSelectionArgs = arrayOf("1", AlertsContract.ALERTS_TYPE_TIME.toString())
        object : MockContentProvider() {
            override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
                assertEquals(contentUri, uri)
                assertEquals("${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)

                return 0
            }
        }.also { addMockProvider(it) }

        alertsDao.removeArrivalAlert(1)
    }

    @Test
    fun removeProximityAlertSendsThroughCorrectParametersForDelete() {
        val expectedSelectionArgs = arrayOf("5", AlertsContract.ALERTS_TYPE_PROXIMITY.toString())
        object : MockContentProvider() {
            override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
                assertEquals(contentUri, uri)
                assertEquals("${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)

                return 0
            }
        }.also { addMockProvider(it) }

        alertsDao.removeProximityAlert(5)
    }

    @Test
    fun getAllArrivalAlertsWithNullResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForArrivalAlert()
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.getAllArrivalAlerts()

        assertNull(result)
    }

    @Test
    fun getAllArrivalAlertsWithEmptyResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForArrivalAlert()
        val cursor = MatrixCursor(arrayOf(
                AlertsContract.ID,
                AlertsContract.TIME_ADDED,
                AlertsContract.STOP_CODE,
                AlertsContract.SERVICE_NAMES,
                AlertsContract.TIME_TRIGGER))
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.getAllArrivalAlerts()

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllArrivalAlertsWithSingleItemAndSingleServiceIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForArrivalAlert()
        val cursor = MatrixCursor(arrayOf(
                AlertsContract.ID,
                AlertsContract.TIME_ADDED,
                AlertsContract.STOP_CODE,
                AlertsContract.SERVICE_NAMES,
                AlertsContract.TIME_TRIGGER))
        cursor.addRow(arrayOf(1, 123L, "123456", "1", 5))
        val expectedItem = ArrivalAlert(1, 123L, "123456", listOf("1"), 5)
        val expected = listOf(expectedItem)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.getAllArrivalAlerts()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllArrivalAlertsWithSingleItemAndMultipleServicesIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForArrivalAlert()
        val cursor = MatrixCursor(arrayOf(
                AlertsContract.ID,
                AlertsContract.TIME_ADDED,
                AlertsContract.STOP_CODE,
                AlertsContract.SERVICE_NAMES,
                AlertsContract.TIME_TRIGGER))
        cursor.addRow(arrayOf(1, 123L, "123456", "1,2,3", 5))
        val expectedItem = ArrivalAlert(1, 123L, "123456", listOf("1", "2", "3"), 5)
        val expected = listOf(expectedItem)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.getAllArrivalAlerts()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllArrivalAlertsWithSingleItemAndMultipleServicesWithSpacesIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForArrivalAlert()
        val cursor = MatrixCursor(arrayOf(
                AlertsContract.ID,
                AlertsContract.TIME_ADDED,
                AlertsContract.STOP_CODE,
                AlertsContract.SERVICE_NAMES,
                AlertsContract.TIME_TRIGGER))
        cursor.addRow(arrayOf(1, 123L, "123456", "1 , 2 , 3", 5))
        val expectedItem = ArrivalAlert(1, 123L, "123456", listOf("1", "2", "3"), 5)
        val expected = listOf(expectedItem)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.getAllArrivalAlerts()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllArrivalAlertsWithMultipleItemsIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForArrivalAlert()
        val cursor = MatrixCursor(arrayOf(
                AlertsContract.ID,
                AlertsContract.TIME_ADDED,
                AlertsContract.STOP_CODE,
                AlertsContract.SERVICE_NAMES,
                AlertsContract.TIME_TRIGGER))
        cursor.addRow(arrayOf(1, 123L, "123456", "1,2,3", 5))
        cursor.addRow(arrayOf(2, 124L, "987654", "10", 15))
        cursor.addRow(arrayOf(3, 125L, "246802", "101,202,303", 3))
        val expectedItem1 = ArrivalAlert(1, 123L, "123456", listOf("1", "2", "3"), 5)
        val expectedItem2 = ArrivalAlert(2, 124L, "987654", listOf("10"), 15)
        val expectedItem3 = ArrivalAlert(3, 125L, "246802", listOf("101", "202", "303"), 3)
        val expected = listOf(expectedItem1, expectedItem2, expectedItem3)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.getAllArrivalAlerts()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllArrivalAlertStopCodesWithNullResultIsHandledCorrectly() {
        val expectedProjection = arrayOf(AlertsContract.STOP_CODE)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.getAllArrivalAlertStopCodes()

        assertNull(result)
    }

    @Test
    fun getAllArrivalAlertStopCodesWithEmptyResultIsHandledCorrectly() {
        val expectedProjection = arrayOf(AlertsContract.STOP_CODE)
        val cursor = MatrixCursor(arrayOf(AlertsContract.STOP_CODE))
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.getAllArrivalAlertStopCodes()

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllArrivalAlertStopCodesWithSingleItemIsHandledCorrectly() {
        val expectedProjection = arrayOf(AlertsContract.STOP_CODE)
        val cursor = MatrixCursor(arrayOf(AlertsContract.STOP_CODE))
        cursor.addRow(arrayOf("123456"))
        val expected = listOf("123456")
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.getAllArrivalAlertStopCodes()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllArrivalAlertStopCodesWithMultipleItemsIsHandledCorrectly() {
        val expectedProjection = arrayOf(AlertsContract.STOP_CODE)
        val cursor = MatrixCursor(arrayOf(AlertsContract.STOP_CODE))
        cursor.addRow(arrayOf("123456"))
        cursor.addRow(arrayOf("987654"))
        cursor.addRow(arrayOf("246802"))
        val expected = listOf("123456", "987654", "246802")
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.getAllArrivalAlertStopCodes()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getArrivalAlertCountReturnsZeroWhenCursorIsNull() {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.getArrivalAlertCount()

        assertEquals(0, result)
    }

    @Test
    fun getArrivalAlertCountReturnsZeroWhenCursorIsEmpty() {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
        val cursor = MatrixCursor(arrayOf(AlertsContract.COUNT))
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.getArrivalAlertCount()

        assertEquals(0, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getArrivalAlertCountReturnsValueWhenCursorIsPopulated() {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
        val cursor = MatrixCursor(arrayOf(AlertsContract.COUNT))
        cursor.addRow(arrayOf(5))
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also { addMockProvider(it) }

        val result = alertsDao.getArrivalAlertCount()

        assertEquals(5, result)
        assertTrue(cursor.isClosed)
    }

    private fun addMockProvider(provider: ContentProvider) {
        mockContentResolver.addProvider(TEST_AUTHORITY, provider)
    }

    private fun getExpectedProjectionForArrivalAlert() = arrayOf(
            AlertsContract.ID,
            AlertsContract.TIME_ADDED,
            AlertsContract.STOP_CODE,
            AlertsContract.SERVICE_NAMES,
            AlertsContract.TIME_TRIGGER)
}