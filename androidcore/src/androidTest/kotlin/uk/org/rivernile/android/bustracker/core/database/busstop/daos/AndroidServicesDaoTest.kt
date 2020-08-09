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
import android.graphics.Color
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
import uk.org.rivernile.android.bustracker.core.database.busstop.ServicesContract
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [AndroidServicesDao].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AndroidServicesDaoTest {

    companion object {

        private const val TEST_AUTHORITY = "test.authority"
    }

    @Mock
    private lateinit var contract: ServicesContract

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: MockContentResolver
    private val contentUri = Uri.parse("content://$TEST_AUTHORITY/tableName")

    private lateinit var servicesDao: ServicesDao

    @Before
    fun setUp() {
        mockContentResolver = MockContentResolver()
        mockContext = object : MockContext() {
            override fun getContentResolver() = mockContentResolver
        }

        servicesDao = AndroidServicesDao(mockContext, contract)

        whenever(contract.getContentUri())
                .thenReturn(contentUri)
    }

    @Test
    fun getColoursForServicesSendsCorrectSelectionArgsWhenServicesIsNull() {
        val expectedProjection = getExpectedProjectionForColours()
        val expectedSelection = "${ServicesContract.COLOUR} IS NOT NULL"
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals(expectedSelection, selection)
                assertNull(selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this::addMockProvider)

        servicesDao.getColoursForServices(null)
    }

    @Test
    fun getColoursForServicesSendsCorrectSelectionArgsWhenServicesIsEmpty() {
        val expectedProjection = getExpectedProjectionForColours()
        val expectedSelection = "${ServicesContract.COLOUR} IS NOT NULL"
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals(expectedSelection, selection)
                assertNull(selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this::addMockProvider)

        servicesDao.getColoursForServices(arrayOf())
    }

    @Test
    fun getColoursForServicesSendsCorrectSelectionArgsWhenHasSingleService() {
        val expectedProjection = getExpectedProjectionForColours()
        val expectedSelection = "${ServicesContract.COLOUR} IS NOT NULL " +
                "AND ${ServicesContract.NAME} IN (?)"
        val expectedSelectionArgs = arrayOf("1")
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals(expectedSelection, selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this::addMockProvider)

        servicesDao.getColoursForServices(arrayOf("1"))
    }

    @Test
    fun getColoursForServicesSendsCorrectSelectionArgsWhenHasMultipleServices() {
        val expectedProjection = getExpectedProjectionForColours()
        val expectedSelection = "${ServicesContract.COLOUR} IS NOT NULL " +
                "AND ${ServicesContract.NAME} IN (?,?,?)"
        val expectedSelectionArgs = arrayOf("1", "2", "3")
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals(expectedSelection, selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this::addMockProvider)

        servicesDao.getColoursForServices(arrayOf("1", "2", "3"))
    }

    @Test
    fun getColoursForServicesReturnsNullWhenCursorIsNull() {
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? = null
        }.also(this::addMockProvider)

        val result = servicesDao.getColoursForServices(null)

        assertNull(result)
    }

    @Test
    fun getColoursForServicesReturnsNullWhenCursorIsEmpty() {
        val cursor = MatrixCursor(getExpectedProjectionForColours())
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? = cursor
        }.also(this::addMockProvider)

        val result = servicesDao.getColoursForServices(null)

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getColoursForServicesExcludesItemsWithNullColour() {
        val cursor = MatrixCursor(getExpectedProjectionForColours())
        cursor.addRow(arrayOf("1", null))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? = cursor
        }.also(this::addMockProvider)

        val result = servicesDao.getColoursForServices(null)

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getColoursForServicesExcludesItemsWithColourWhichFailsParsing() {
        val cursor = MatrixCursor(getExpectedProjectionForColours())
        cursor.addRow(arrayOf("1", "foobar"))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? = cursor
        }.also(this::addMockProvider)

        val result = servicesDao.getColoursForServices(null)

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getColoursForServicesReturnsSingleValidItem() {
        val cursor = MatrixCursor(getExpectedProjectionForColours())
        cursor.addRow(arrayOf("1", "#000000"))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? = cursor
        }.also(this::addMockProvider)
        val expected = mapOf("1" to Color.BLACK)

        val result = servicesDao.getColoursForServices(null)

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getColoursForServicesReturnsMultipleValidItems() {
        val cursor = MatrixCursor(getExpectedProjectionForColours())
        cursor.addRow(arrayOf("1", "#000000"))
        cursor.addRow(arrayOf("2", "#FFFFFF"))
        cursor.addRow(arrayOf("3", "#FF0000"))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? = cursor
        }.also(this::addMockProvider)
        val expected = mapOf(
                "1" to Color.BLACK,
                "2" to Color.WHITE,
                "3" to Color.RED)

        val result = servicesDao.getColoursForServices(null)

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    private fun addMockProvider(provider: ContentProvider) {
        mockContentResolver.addProvider(TEST_AUTHORITY, provider)
    }

    private fun getExpectedProjectionForColours() = arrayOf(
            ServicesContract.NAME,
            ServicesContract.COLOUR)
}