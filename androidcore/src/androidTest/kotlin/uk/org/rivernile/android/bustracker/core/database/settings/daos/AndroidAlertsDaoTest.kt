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
import uk.org.rivernile.android.bustracker.core.database.settings.AlertsContract
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import kotlin.test.assertFalse

/**
 * Tests for [AndroidAlertsDao].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AndroidAlertsDaoTest {

    companion object {

        private const val TEST_AUTHORITY = "test.authority"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

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

        alertsDao = AndroidAlertsDao(
                mockContext,
                contract,
                coroutineRule.testDispatcher)

        whenever(contract.getContentUri())
                .thenReturn(contentUri)
    }

    @Test
    fun addArrivalAlertSendsThroughCorrectParametersForInsert() = runTest {
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
            override fun insert(uri: Uri, values: ContentValues?): Uri {
                assertEquals(contentUri, uri)
                assertEquals(expected, values)

                return ContentUris.withAppendedId(uri, 1)
            }
       }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.addArrivalAlert(alert)

        assertEquals(1, result)
    }

    @Test
    fun addArrivalAlertHandlesSingleServiceNameCorrectly() = runTest {
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
            override fun insert(uri: Uri, values: ContentValues?): Uri {
                assertEquals(contentUri, uri)
                assertEquals(expected, values)

                return ContentUris.withAppendedId(uri, 1)
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.addArrivalAlert(alert)

        assertEquals(1, result)
    }

    @Test
    fun addProximityAlertSendsThroughCorrectParametersForInsert() = runTest {
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
            override fun insert(uri: Uri, values: ContentValues?): Uri? {
                assertEquals(contentUri, uri)
                assertEquals(expected, values)

                return null
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        alertsDao.addProximityAlert(alert)
    }

    @Test
    fun removeArrivalAlertByIdSendsThroughCorrectParametersForDelete() = runTest {
        val expectedSelectionArgs = arrayOf("1", AlertsContract.ALERTS_TYPE_TIME.toString())
        object : MockContentProvider() {
            override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
                assertEquals(contentUri, uri)
                assertEquals("${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)

                return 0
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        alertsDao.removeArrivalAlert(1)
    }

    @Test
    fun removeArrivalAlertByStopCodeSendsThroughCorrectParametersForDelete() = runTest {
        val expectedSelectionArgs = arrayOf("123456", AlertsContract.ALERTS_TYPE_TIME.toString())
        object : MockContentProvider() {
            override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
                assertEquals(contentUri, uri)
                assertEquals("${AlertsContract.STOP_CODE} = ? AND ${AlertsContract.TYPE} = ?",
                        selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)

                return 0
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        alertsDao.removeArrivalAlert("123456")
    }

    @Test
    fun removeAllArrivalAlertsSendThroughCorrectParametersForDelete() = runTest {
        val expectedSelectionArgs = arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString())
        object : MockContentProvider() {
            override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
                assertEquals(contentUri, uri)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)

                return 0
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        alertsDao.removeAllArrivalAlerts()
    }

    @Test
    fun removeProximityAlertByIdSendsThroughCorrectParametersForDelete() = runTest {
        val expectedSelectionArgs = arrayOf("5", AlertsContract.ALERTS_TYPE_PROXIMITY.toString())
        object : MockContentProvider() {
            override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
                assertEquals(contentUri, uri)
                assertEquals("${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)

                return 0
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        alertsDao.removeProximityAlert(5)
    }

    @Test
    fun removeProximityAlertByStopCodeSendsThroughCorrectParametersForDelete() = runTest {
        val expectedSelectionArgs = arrayOf(
                "123456",
                AlertsContract.ALERTS_TYPE_PROXIMITY.toString())
        object : MockContentProvider() {
            override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
                assertEquals(contentUri, uri)
                assertEquals("${AlertsContract.STOP_CODE} = ? AND ${AlertsContract.TYPE} = ?",
                        selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)

                return 0
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        alertsDao.removeProximityAlert("123456")
    }

    @Test
    fun removeAllProximityAlertsSendsThroughCorrectParametersForDelete() = runTest {
        val expectedSelectionArgs = arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString())
        object : MockContentProvider() {
            override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
                assertEquals(contentUri, uri)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)

                return 0
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        alertsDao.removeAllProximityAlerts()
    }

    @Test
    fun getAllAlertsWithNullResultIsHandledCorrectly() = runTest {
        val expectedProjection = getExpectedProjectionForAllAlerts()
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertEquals("${AlertsContract.TIME_ADDED} ASC", sortOrder)

                return null
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.getAllAlerts()

        assertNull(result)
    }

    @Test
    fun getAllAlertsWithEmptyResultIsHandledCorrectly() = runTest {
        val expectedProjection = getExpectedProjectionForAllAlerts()
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertEquals("${AlertsContract.TIME_ADDED} ASC", sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.getAllAlerts()

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllAlertsWithPopulatedResultIsHandledCorrectly() = runTest {
        val expectedProjection = getExpectedProjectionForAllAlerts()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(1, 123L, "123456", AlertsContract.ALERTS_TYPE_TIME, null, "1", 5))
        cursor.addRow(arrayOf(2, 124L, "123457", AlertsContract.ALERTS_TYPE_PROXIMITY, 250, null,
                null))
        cursor.addRow(arrayOf(3, 125L, "123458", AlertsContract.ALERTS_TYPE_TIME, null, "2,3,4",
                10))
        cursor.addRow(arrayOf(4, 126L, "123459", AlertsContract.ALERTS_TYPE_PROXIMITY, 500, null,
                null))
        val expected = listOf(
                ArrivalAlert(1, 123L, "123456", listOf("1"), 5),
                ProximityAlert(2, 124L, "123457", 250),
                ArrivalAlert(3, 125L, "123458", listOf("2", "3", "4"), 10),
                ProximityAlert(4, 126L, "123459", 500))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertEquals("${AlertsContract.TIME_ADDED} ASC", sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.getAllAlerts()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getProximityAlertWithNullResultIsHandledCorrectly() = runTest {
        val expectedProjection = getExpectedProjectionForProximityAlert()
        val expectedSelectionArgs = arrayOf(
                10.toString(),
                AlertsContract.ALERTS_TYPE_PROXIMITY.toString())
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.getProximityAlert(10)

        assertNull(result)
    }

    @Test
    fun getProximityAlertWithEmptyResultIsHandledCorrectly() = runTest {
        val expectedProjection = getExpectedProjectionForProximityAlert()
        val expectedSelectionArgs = arrayOf(
                10.toString(),
                AlertsContract.ALERTS_TYPE_PROXIMITY.toString())
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.getProximityAlert(10)

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getProximityAlertWithNonEmptyResultIsHandledCorrectly() = runTest {
        val expectedProjection = getExpectedProjectionForProximityAlert()
        val expectedSelectionArgs = arrayOf(
                10.toString(),
                AlertsContract.ALERTS_TYPE_PROXIMITY.toString())
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(1, 123L, "123456", 50))
        val expectedItem = ProximityAlert(1, 123L, "123456", 50)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.getProximityAlert(10)

        assertEquals(expectedItem, result)
        assertTrue(cursor.isClosed)
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
        }.also(this::addMockProvider)

        val result = alertsDao.getAllArrivalAlerts()

        assertNull(result)
    }

    @Test
    fun getAllArrivalAlertsWithEmptyResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForArrivalAlert()
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = alertsDao.getAllArrivalAlerts()

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllArrivalAlertsWithSingleItemAndSingleServiceIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForArrivalAlert()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(1, 123L, "123456", "1", 5))
        val expectedItem = ArrivalAlert(1, 123L, "123456", listOf("1"), 5)
        val expected = listOf(expectedItem)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = alertsDao.getAllArrivalAlerts()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllArrivalAlertsWithSingleItemAndMultipleServicesIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForArrivalAlert()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(1, 123L, "123456", "1,2,3", 5))
        val expectedItem = ArrivalAlert(1, 123L, "123456", listOf("1", "2", "3"), 5)
        val expected = listOf(expectedItem)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = alertsDao.getAllArrivalAlerts()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllArrivalAlertsWithSingleItemAndMultipleServicesWithSpacesIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForArrivalAlert()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(1, 123L, "123456", "1 , 2 , 3", 5))
        val expectedItem = ArrivalAlert(1, 123L, "123456", listOf("1", "2", "3"), 5)
        val expected = listOf(expectedItem)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = alertsDao.getAllArrivalAlerts()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllArrivalAlertsWithMultipleItemsIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForArrivalAlert()
        val cursor = MatrixCursor(expectedProjection)
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
                               sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

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
        }.also(this::addMockProvider)

        val result = alertsDao.getAllArrivalAlertStopCodes()

        assertNull(result)
    }

    @Test
    fun getAllArrivalAlertStopCodesWithEmptyResultIsHandledCorrectly() {
        val expectedProjection = arrayOf(AlertsContract.STOP_CODE)
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = alertsDao.getAllArrivalAlertStopCodes()

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllArrivalAlertStopCodesWithSingleItemIsHandledCorrectly() {
        val expectedProjection = arrayOf(AlertsContract.STOP_CODE)
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("123456"))
        val expected = listOf("123456")
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = alertsDao.getAllArrivalAlertStopCodes()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllArrivalAlertStopCodesWithMultipleItemsIsHandledCorrectly() {
        val expectedProjection = arrayOf(AlertsContract.STOP_CODE)
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("123456"))
        cursor.addRow(arrayOf("987654"))
        cursor.addRow(arrayOf("246802"))
        val expected = listOf("123456", "987654", "246802")
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = alertsDao.getAllArrivalAlertStopCodes()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getArrivalAlertCountReturnsZeroWhenCursorIsNull() = runTest {
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
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.getArrivalAlertCount()

        assertEquals(0, result)
    }

    @Test
    fun getArrivalAlertCountReturnsZeroWhenCursorIsEmpty() = runTest {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.getArrivalAlertCount()

        assertEquals(0, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getArrivalAlertCountReturnsValueWhenCursorIsPopulated() = runTest {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(5))
        object : MockContentProvider() {
            override fun query(uri: Uri,
                               projection: Array<String>?,
                               selection: String?,
                               selectionArgs: Array<String>?,
                               sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.getArrivalAlertCount()

        assertEquals(5, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllProximityAlertsWithNullResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForProximityAlert()
        object : MockContentProvider() {
            override fun query(uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this::addMockProvider)

        val result = alertsDao.getAllProximityAlerts()

        assertNull(result)
    }

    @Test
    fun getAllProximityAlertsWithEmptyResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForProximityAlert()
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = alertsDao.getAllProximityAlerts()

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllProximityAlertsWithSingleItemIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForProximityAlert()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(1, 123L, "123456", 250))
        val expectedItem = ProximityAlert(1, 123L, "123456", 250)
        val expected = listOf(expectedItem)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = alertsDao.getAllProximityAlerts()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllProximityAlertsWithMultipleItemsIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForProximityAlert()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(1, 123L, "123456", 10))
        cursor.addRow(arrayOf(2, 124L, "987654", 20))
        cursor.addRow(arrayOf(3, 125L, "246802", 30))
        val expectedItem1 = ProximityAlert(1, 123L, "123456", 10)
        val expectedItem2 = ProximityAlert(2, 124L, "987654", 20)
        val expectedItem3 = ProximityAlert(3, 125L, "246802", 30)
        val expected = listOf(expectedItem1, expectedItem2, expectedItem3)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = alertsDao.getAllProximityAlerts()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getProximityAlertCountReturnsZeroWhenCursorIsNull() = runTest {
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
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.getProximityAlertCount()

        assertEquals(0, result)
    }

    @Test
    fun getProximityAlertCountReturnsZeroWhenCursorIsEmpty() = runTest {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.getProximityAlertCount()

        assertEquals(0, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getProximityAlertCountReturnsValueWhenCursorIsPopulated() = runTest {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(5))
        object : MockContentProvider() {
            override fun query(uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString()),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.getProximityAlertCount()

        assertEquals(5, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun hasArrivalAlertReturnsFalseWhenCursorIsNull() = runTest {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ? AND ${AlertsContract.STOP_CODE} = ?",
                        selection)
                assertArrayEquals(
                        arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString(), "123456"),
                        selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.hasArrivalAlert("123456")

        assertFalse(result)
    }

    @Test
    fun hasArrivalAlertReturnsFalseWhenCursorIsEmpty() = runTest {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
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
                assertEquals("${AlertsContract.TYPE} = ? AND ${AlertsContract.STOP_CODE} = ?",
                        selection)
                assertArrayEquals(
                        arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString(), "123456"),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.hasArrivalAlert("123456")

        assertFalse(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun hasArrivalAlertReturnsFalseWhenCursorReturnsCountOfZero() = runTest {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(0))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ? AND ${AlertsContract.STOP_CODE} = ?",
                        selection)
                assertArrayEquals(
                        arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString(), "123456"),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.hasArrivalAlert("123456")

        assertFalse(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun hasArrivalAlertReturnsFalseWhenCursorReturnsCountOfGreaterThanZero() = runTest {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(1))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ? AND ${AlertsContract.STOP_CODE} = ?",
                        selection)
                assertArrayEquals(
                        arrayOf(AlertsContract.ALERTS_TYPE_TIME.toString(), "123456"),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.hasArrivalAlert("123456")

        assertTrue(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun hasProximityAlertReturnsFalseWhenCursorIsNull() = runTest {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ? AND ${AlertsContract.STOP_CODE} = ?",
                        selection)
                assertArrayEquals(
                        arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString(), "123456"),
                        selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.hasProximityAlert("123456")

        assertFalse(result)
    }

    @Test
    fun hasProximityAlertReturnsFalseWhenCursorIsEmpty() = runTest {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
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
                assertEquals("${AlertsContract.TYPE} = ? AND ${AlertsContract.STOP_CODE} = ?",
                        selection)
                assertArrayEquals(
                        arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString(), "123456"),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.hasProximityAlert("123456")

        assertFalse(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun hasProximityAlertReturnsFalseWhenCursorReturnsCountOfZero() = runTest {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(0))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ? AND ${AlertsContract.STOP_CODE} = ?",
                        selection)
                assertArrayEquals(
                        arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString(), "123456"),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.hasProximityAlert("123456")

        assertFalse(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun hasProximityAlertReturnsFalseWhenCursorReturnsCountOfGreaterThanZero() = runTest {
        val expectedProjection = arrayOf(AlertsContract.COUNT)
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf(1))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${AlertsContract.TYPE} = ? AND ${AlertsContract.STOP_CODE} = ?",
                        selection)
                assertArrayEquals(
                        arrayOf(AlertsContract.ALERTS_TYPE_PROXIMITY.toString(), "123456"),
                        selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidAlertsDaoTest::addMockProvider)

        val result = alertsDao.hasProximityAlert("123456")

        assertTrue(result)
        assertTrue(cursor.isClosed)
    }

    private fun addMockProvider(provider: ContentProvider) {
        mockContentResolver.addProvider(TEST_AUTHORITY, provider)
    }

    private fun getExpectedProjectionForAllAlerts() = arrayOf(
            AlertsContract.ID,
            AlertsContract.TIME_ADDED,
            AlertsContract.STOP_CODE,
            AlertsContract.TYPE,
            AlertsContract.DISTANCE_FROM,
            AlertsContract.SERVICE_NAMES,
            AlertsContract.TIME_TRIGGER)

    private fun getExpectedProjectionForArrivalAlert() = arrayOf(
            AlertsContract.ID,
            AlertsContract.TIME_ADDED,
            AlertsContract.STOP_CODE,
            AlertsContract.SERVICE_NAMES,
            AlertsContract.TIME_TRIGGER)

    private fun getExpectedProjectionForProximityAlert() = arrayOf(
            AlertsContract.ID,
            AlertsContract.TIME_ADDED,
            AlertsContract.STOP_CODE,
            AlertsContract.DISTANCE_FROM)
}