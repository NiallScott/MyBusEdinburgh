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
import android.content.ContentValues
import android.content.Context
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
        val mockContentProvider = object : MockContentProvider() {
            override fun insert(uri: Uri, values: ContentValues): Uri? {
                assertEquals(contentUri, uri)
                assertEquals(expected, values)

                return null
            }
        }
        addMockProvider(mockContentProvider)

        alertsDao.addArrivalAlert(alert)
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
        val mockContentProvider = object : MockContentProvider() {
            override fun insert(uri: Uri, values: ContentValues): Uri? {
                assertEquals(contentUri, uri)
                assertEquals(expected, values)

                return null
            }
        }
        addMockProvider(mockContentProvider)

        alertsDao.addArrivalAlert(alert)
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
        val mockContentProvider = object : MockContentProvider() {
            override fun insert(uri: Uri, values: ContentValues): Uri? {
                assertEquals(contentUri, uri)
                assertEquals(expected, values)

                return null
            }
        }
        addMockProvider(mockContentProvider)

        alertsDao.addProximityAlert(alert)
    }

    @Test
    fun removeArrivalAlertSendsThroughCorrectParamtersForDelete() {
        val expectedSelectionArgs = arrayOf("1", AlertsContract.ALERTS_TYPE_TIME.toString())
        val mockContentProvider = object : MockContentProvider() {
            override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
                assertEquals(contentUri, uri)
                assertEquals("${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)

                return 0
            }
        }
        addMockProvider(mockContentProvider)

        alertsDao.removeArrivalAlert(1)
    }

    @Test
    fun removeProximityAlertSendsThroughCorrectParamtersForDelete() {
        val expectedSelectionArgs = arrayOf("5", AlertsContract.ALERTS_TYPE_PROXIMITY.toString())
        val mockContentProvider = object : MockContentProvider() {
            override fun delete(uri: Uri, selection: String?, selectionArgs: Array<String>?): Int {
                assertEquals(contentUri, uri)
                assertEquals("${AlertsContract.ID} = ? AND ${AlertsContract.TYPE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)

                return 0
            }
        }
        addMockProvider(mockContentProvider)

        alertsDao.removeProximityAlert(5)
    }

    private fun addMockProvider(provider: ContentProvider) {
        mockContentResolver.addProvider(TEST_AUTHORITY, provider)
    }
}