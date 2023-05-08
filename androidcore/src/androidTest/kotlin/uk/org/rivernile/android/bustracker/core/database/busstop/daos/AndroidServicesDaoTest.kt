/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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
import android.os.CancellationSignal
import android.test.mock.MockContentProvider
import android.test.mock.MockContentResolver
import android.test.mock.MockContext
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.ServicesContract
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.ServiceDetails
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
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

        private const val SERVICE_SORT_CLAUSE =
                "CASE WHEN ${ServicesContract.NAME} GLOB '[^0-9.]*' THEN " +
                        "${ServicesContract.NAME} ELSE " +
                        "cast(${ServicesContract.NAME} AS int) END"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var contract: ServicesContract
    @Mock
    private lateinit var exceptionLogger: ExceptionLogger

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: MockContentResolver
    private val contentUri = Uri.parse("content://$TEST_AUTHORITY/tableName")

    private lateinit var servicesDao: AndroidServicesDao

    @Before
    fun setUp() {
        mockContentResolver = MockContentResolver()
        mockContext = object : MockContext() {
            override fun getContentResolver() = mockContentResolver
        }

        servicesDao = AndroidServicesDao(
                mockContext,
                contract,
                exceptionLogger,
                coroutineRule.testDispatcher)

        whenever(contract.getContentUri())
                .thenReturn(contentUri)
    }

    @Test
    fun getColoursForServicesSendsCorrectSelectionArgsWhenServicesIsNull() = runTest {
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
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        servicesDao.getColoursForServices(null)
    }

    @Test
    fun getColoursForServicesSendsCorrectSelectionArgsWhenServicesIsEmpty() = runTest {
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
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        servicesDao.getColoursForServices(emptySet())
    }

    @Test
    fun getColoursForServicesSendsCorrectSelectionArgsWhenHasSingleService() = runTest {
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
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        servicesDao.getColoursForServices(setOf("1"))
    }

    @Test
    fun getColoursForServicesSendsCorrectSelectionArgsWhenHasMultipleServices() = runTest {
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
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        servicesDao.getColoursForServices(setOf("1", "2", "3"))
    }

    @Test
    fun getColoursForServicesReturnsNullWhenCursorIsNull() = runTest {
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? = null
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        val result = servicesDao.getColoursForServices(null)

        assertNull(result)
    }

    @Test
    fun getColoursForServicesReturnsNullWhenCursorIsEmpty() = runTest {
        val cursor = MatrixCursor(getExpectedProjectionForColours())
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor = cursor
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        val result = servicesDao.getColoursForServices(null)

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getColoursForServicesExcludesItemsWithNullColour() = runTest {
        val cursor = MatrixCursor(getExpectedProjectionForColours())
        cursor.addRow(arrayOf("1", null))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor = cursor
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        val result = servicesDao.getColoursForServices(null)

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getColoursForServicesExcludesItemsWithColourWhichFailsParsing() = runTest {
        val cursor = MatrixCursor(getExpectedProjectionForColours())
        cursor.addRow(arrayOf("1", "foobar"))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor = cursor
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        val result = servicesDao.getColoursForServices(null)

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getColoursForServicesReturnsSingleValidItem() = runTest {
        val cursor = MatrixCursor(getExpectedProjectionForColours())
        cursor.addRow(arrayOf("1", "#000000"))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor = cursor
        }.also(this@AndroidServicesDaoTest::addMockProvider)
        val expected = mapOf("1" to Color.BLACK)

        val result = servicesDao.getColoursForServices(null)

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getColoursForServicesReturnsMultipleValidItems() = runTest {
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
                    sortOrder: String?): Cursor = cursor
        }.also(this@AndroidServicesDaoTest::addMockProvider)
        val expected = mapOf(
                "1" to Color.BLACK,
                "2" to Color.WHITE,
                "3" to Color.RED)

        val result = servicesDao.getColoursForServices(null)

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getServiceDetailsReturnsNullWhenServicesIsEmpty() = runTest {
        val result = servicesDao.getServiceDetails(emptySet())

        assertNull(result)
    }

    @Test
    fun getServiceDetailsProducesCorrectSelectionArgsForSingleService() = runTest {
        val expectedProjection = getExpectedProjectionForServiceDetails()
        val expectedSelection = "${ServicesContract.NAME} IN (?)"
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
                assertEquals(expectedSelection, selection)
                assertArrayEquals(selectionArgs, arrayOf("1"))
                assertNull(sortOrder)

                return null
            }
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        servicesDao.getServiceDetails(setOf("1"))
    }

    @Test
    fun getServiceDetailsWithNullCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForServiceDetails()
        val expectedSelection = "${ServicesContract.NAME} IN (?,?,?)"
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
                assertEquals(expectedSelection, selection)
                assertArrayEquals(selectionArgs, arrayOf("1", "2", "3"))
                assertNull(sortOrder)

                return null
            }
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        val result = servicesDao.getServiceDetails(setOf("1", "2", "3"))

        assertNull(result)
    }

    @Test
    fun getServiceDetailsWithEmptyCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForServiceDetails()
        val expectedSelection = "${ServicesContract.NAME} IN (?,?,?)"
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
                assertEquals(expectedSelection, selection)
                assertArrayEquals(selectionArgs, arrayOf("1", "2", "3"))
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        val result = servicesDao.getServiceDetails(setOf("1", "2", "3"))

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getServiceDetailsWithSingleItemReturnsSingleItem() = runTest {
        val expectedProjection = getExpectedProjectionForServiceDetails()
        val expectedSelection = "${ServicesContract.NAME} IN (?,?,?)"
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("2", "Route 2", "#000000"))
        val expected = mapOf("2" to ServiceDetails("2", "Route 2", Color.BLACK))
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
                assertEquals(expectedSelection, selection)
                assertArrayEquals(selectionArgs, arrayOf("1", "2", "3"))
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        val result = servicesDao.getServiceDetails(setOf("1", "2", "3"))

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getServiceDetailsWithMultipleItemReturnsMultipleItems() = runTest {
        val expectedProjection = getExpectedProjectionForServiceDetails()
        val expectedSelection = "${ServicesContract.NAME} IN (?,?,?)"
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("1", "Route 1", "#FFFFFF"))
        cursor.addRow(arrayOf("2", "Route 2", "#000000"))
        cursor.addRow(arrayOf("3", "Route 3", "#FF0000"))
        val expected = mapOf(
                "1" to ServiceDetails("1", "Route 1", Color.WHITE),
                "2" to ServiceDetails("2", "Route 2", Color.BLACK),
                "3" to ServiceDetails("3", "Route 3", Color.RED))
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
                assertEquals(expectedSelection, selection)
                assertArrayEquals(selectionArgs, arrayOf("1", "2", "3"))
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        val result = servicesDao.getServiceDetails(setOf("1", "2", "3"))

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getServiceDetailsWithNullDescriptionAndInvalidColourHandledCorrectly() = runTest {
        val expectedProjection = getExpectedProjectionForServiceDetails()
        val expectedSelection = "${ServicesContract.NAME} IN (?,?,?)"
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("1", "Route 1", "#FFFFFF"))
        cursor.addRow(arrayOf("2", null, "#000000"))
        cursor.addRow(arrayOf("3", "Route 3", "foobar"))
        val expected = mapOf(
                "1" to ServiceDetails("1", "Route 1", Color.WHITE),
                "2" to ServiceDetails("2", null, Color.BLACK),
                "3" to ServiceDetails("3", "Route 3", null))
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
                assertEquals(expectedSelection, selection)
                assertArrayEquals(selectionArgs, arrayOf("1", "2", "3"))
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        val result = servicesDao.getServiceDetails(setOf("1", "2", "3"))

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllServiceNamesWithNullCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForAllServiceNames()
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
                assertEquals(SERVICE_SORT_CLAUSE, sortOrder)

                return null
            }
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        val result = servicesDao.getAllServiceNames()

        assertNull(result)
    }

    @Test
    fun getAllServiceNamesWithNullCursorReturnsEmpty() = runTest {
        val expectedProjection = getExpectedProjectionForAllServiceNames()
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
                assertEquals(SERVICE_SORT_CLAUSE, sortOrder)

                return cursor
            }
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        val result = servicesDao.getAllServiceNames()

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllServiceNamesWithSingleItemReturnsSingleItem() = runTest {
        val expectedProjection = getExpectedProjectionForAllServiceNames()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("1"))
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
                assertEquals(SERVICE_SORT_CLAUSE, sortOrder)

                return cursor
            }
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        val result = servicesDao.getAllServiceNames()

        assertEquals(listOf("1"), result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllServiceNamesWithMultipleItemsReturnsMultipleItems() = runTest {
        val expectedProjection = getExpectedProjectionForAllServiceNames()
        val cursor = MatrixCursor(expectedProjection)
        cursor.addRow(arrayOf("1"))
        cursor.addRow(arrayOf("2"))
        cursor.addRow(arrayOf("3"))
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
                assertEquals(SERVICE_SORT_CLAUSE, sortOrder)

                return cursor
            }
        }.also(this@AndroidServicesDaoTest::addMockProvider)

        val result = servicesDao.getAllServiceNames()

        assertEquals(listOf("1", "2", "3"), result)
        assertTrue(cursor.isClosed)
    }

    private fun addMockProvider(provider: ContentProvider) {
        mockContentResolver.addProvider(TEST_AUTHORITY, provider)
    }

    private fun getExpectedProjectionForColours() = arrayOf(
            ServicesContract.NAME,
            ServicesContract.COLOUR)

    private fun getExpectedProjectionForServiceDetails() = arrayOf(
            ServicesContract.NAME,
            ServicesContract.DESCRIPTION,
            ServicesContract.COLOUR)

    private fun getExpectedProjectionForAllServiceNames() =
            arrayOf(ServicesContract.NAME)
}