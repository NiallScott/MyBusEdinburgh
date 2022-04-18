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
import org.junit.Assert.assertFalse
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.settings.FavouritesContract
import uk.org.rivernile.android.bustracker.core.database.settings.entities.FavouriteStop
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import kotlin.test.assertNull
import kotlin.test.assertTrue

/**
 * Tests for [AndroidFavouritesDao].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AndroidFavouritesDaoTest {

    companion object {

        private const val TEST_AUTHORITY = "test.authority"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var contract: FavouritesContract

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: MockContentResolver
    private val contentUri = Uri.parse("content://$TEST_AUTHORITY/tableName")

    private lateinit var favouritesDao: AndroidFavouritesDao

    @Before
    fun setUp() {
        mockContentResolver = MockContentResolver()
        mockContext = object : MockContext() {
            override fun getContentResolver() = mockContentResolver
        }

        favouritesDao = AndroidFavouritesDao(mockContext, contract, coroutineRule.testDispatcher)

        whenever(contract.getContentUri())
                .thenReturn(contentUri)
    }

    @Test
    fun isStopAddedAsFavouriteReturnsFalseWhenCursorIsNull() = runTest {
        val expectedProjection = arrayOf(FavouritesContract.COUNT)
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${FavouritesContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this@AndroidFavouritesDaoTest::addMockProvider)

        val result = favouritesDao.isStopAddedAsFavourite("123456")

        assertFalse(result)
    }

    @Test
    fun isStopAddedAsFavouriteReturnsFalseWhenCursorIsEmpty() = runTest {
        val expectedProjection = arrayOf(FavouritesContract.COUNT)
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
                assertEquals("${FavouritesContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidFavouritesDaoTest::addMockProvider)

        val result = favouritesDao.isStopAddedAsFavourite("123456")

        assertFalse(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun isStopAddedAsFavouriteReturnsFalseWhenCursorReturnsCountOfZeroForStopCode() = runTest {
        val expectedProjection = arrayOf(FavouritesContract.COUNT)
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
                assertEquals("${FavouritesContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidFavouritesDaoTest::addMockProvider)

        val result = favouritesDao.isStopAddedAsFavourite("123456")

        assertFalse(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun isStopAddedAsFavouriteReturnsTrueWhenCursorReturnsCountOfGreaterThanZeroForStopCode() =
            runTest {
        val expectedProjection = arrayOf(FavouritesContract.COUNT)
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
                assertEquals("${FavouritesContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidFavouritesDaoTest::addMockProvider)

        val result = favouritesDao.isStopAddedAsFavourite("123456")

        assertTrue(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun addFavouriteStopsWithSingleItemAddsFavouriteStop() {
        val favouriteStop = FavouriteStop(0L, "100001", "Stop 1")
        val favouriteStops = listOf(favouriteStop)
        val expectedContentValues = ContentValues().apply {
            put(FavouritesContract.STOP_CODE, "100001")
            put(FavouritesContract.STOP_NAME, "Stop 1")
        }
        val expectedContentValuesArray = arrayOf(expectedContentValues)
        object : MockContentProvider() {
            override fun bulkInsert(uri: Uri, values: Array<out ContentValues>): Int {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedContentValuesArray, values)

                return 1
            }
        }.also(this::addMockProvider)

        val result = favouritesDao.addFavouriteStops(favouriteStops)

        assertEquals(1, result)
    }

    @Test
    fun addFavouriteStopsWithMultipleItemsAddsFavouriteStops() {
        val favouriteStop1 = FavouriteStop(0L, "100001", "Stop 1")
        val favouriteStop2 = FavouriteStop(0L, "100002", "Stop 2")
        val favouriteStop3 = FavouriteStop(0L, "100003", "Stop 3")
        val favouriteStops = listOf(favouriteStop1, favouriteStop2, favouriteStop3)
        val expectedContentValues1 = ContentValues().apply {
            put(FavouritesContract.STOP_CODE, "100001")
            put(FavouritesContract.STOP_NAME, "Stop 1")
        }
        val expectedContentValues2 = ContentValues().apply {
            put(FavouritesContract.STOP_CODE, "100002")
            put(FavouritesContract.STOP_NAME, "Stop 2")
        }
        val expectedContentValues3 = ContentValues().apply {
            put(FavouritesContract.STOP_CODE, "100003")
            put(FavouritesContract.STOP_NAME, "Stop 3")
        }
        val expectedContentValuesArray = arrayOf(expectedContentValues1, expectedContentValues2,
                expectedContentValues3)
        object : MockContentProvider() {
            override fun bulkInsert(uri: Uri, values: Array<out ContentValues>): Int {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedContentValuesArray, values)

                return 3
            }
        }.also(this::addMockProvider)

        val result = favouritesDao.addFavouriteStops(favouriteStops)

        assertEquals(3, result)
    }

    @Test
    fun addFavouriteStopAddsFavouriteStop() = runTest {
        val favouriteStop = FavouriteStop(0L, "123456", "Stop name")
        val expectedContentValues = ContentValues().apply {
            put(FavouritesContract.STOP_CODE, "123456")
            put(FavouritesContract.STOP_NAME, "Stop name")
        }
        val newItemUri = ContentUris.withAppendedId(contentUri, 1L)
        object : MockContentProvider() {
            override fun insert(uri: Uri, values: ContentValues?): Uri {
                assertEquals(contentUri, uri)
                assertEquals(expectedContentValues, values)

                return newItemUri
            }
        }.also(this@AndroidFavouritesDaoTest::addMockProvider)

        favouritesDao.addFavouriteStop(favouriteStop)
    }

    @Test
    fun updateFavouriteStopUpdatesFavouriteStop() = runTest {
        val favouriteStop = FavouriteStop(1L, "123456", "New name")
        val expectedContentValues = ContentValues().apply {
            put(FavouritesContract.STOP_NAME, "New name")
        }
        val expectedUri = ContentUris.withAppendedId(contentUri, 1L)
        object : MockContentProvider() {
            override fun update(
                    uri: Uri,
                    values: ContentValues?,
                    selection: String?,
                    selectionArgs: Array<out String>?): Int {
                assertEquals(expectedUri, uri)
                assertEquals(expectedContentValues, values)
                assertNull(selection)
                assertNull(selectionArgs)

                return 1
            }
        }.also(this@AndroidFavouritesDaoTest::addMockProvider)

        favouritesDao.updateFavouriteStop(favouriteStop)
    }

    @Test
    fun removeFavouriteStopRemovesFavouriteStop() = runTest {
        object : MockContentProvider() {
            override fun delete(
                    uri: Uri,
                    selection: String?,
                    selectionArgs: Array<out String>?): Int {
                assertEquals(contentUri, uri)
                assertEquals("${FavouritesContract.STOP_CODE} = ?", selection)
                assertArrayEquals(arrayOf("123456"), selectionArgs)

                return 1
            }
        }.also(this@AndroidFavouritesDaoTest::addMockProvider)

        favouritesDao.removeFavouriteStop("123456")
    }

    @Test
    fun removeAllFavouriteStopsUsesCorrectParameters() {
        object : MockContentProvider() {
            override fun delete(
                    uri: Uri,
                    selection: String?,
                    selectionArgs: Array<out String>?): Int {
                assertEquals(contentUri, uri)
                assertNull(selection)
                assertNull(selectionArgs)

                return 1
            }
        }.also(this::addMockProvider)

        val result = favouritesDao.removeAllFavouriteStops()

        assertEquals(1, result)
    }

    @Test
    fun getAllFavouriteStopsWithNullResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForFavouriteStop()
        object : MockContentProvider() {
            override fun query(uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this::addMockProvider)

        val result = favouritesDao.getAllFavouriteStops()

        assertNull(result)
    }

    @Test
    fun getAllFavouriteStopsWithEmptyResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForFavouriteStop()
        val cursor = MatrixCursor(expectedProjection)
        object : MockContentProvider() {
            override fun query(uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = favouritesDao.getAllFavouriteStops()

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getAllFavouriteStopsWithNonEmptyResultIsHandledCorrectly() {
        val expectedProjection = getExpectedProjectionForFavouriteStop()
        val cursor = MatrixCursor(expectedProjection)
        cursor.apply {
            addRow(arrayOf(1, "100001", "Stop 1"))
            addRow(arrayOf(2, "100002", "Stop 2"))
            addRow(arrayOf(3, "100003", "Stop 3"))
        }
        val expected = listOf(
                FavouriteStop(1L, "100001", "Stop 1"),
                FavouriteStop(2L, "100002", "Stop 2"),
                FavouriteStop(3L, "100003", "Stop 3"))
        object : MockContentProvider() {
            override fun query(uri: Uri,
                    projection: Array<String>?,
                    selection: String?,
                    selectionArgs: Array<String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this::addMockProvider)

        val result = favouritesDao.getAllFavouriteStops()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getFavouriteStopWithNullCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForFavouriteStopSingle()
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
                assertEquals("${FavouritesContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return null
            }
        }.also(this@AndroidFavouritesDaoTest::addMockProvider)

        val result = favouritesDao.getFavouriteStop("123456")

        assertNull(result)
    }

    @Test
    fun getFavouriteStopWithEmptyCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForFavouriteStopSingle()
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
                assertEquals("${FavouritesContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidFavouritesDaoTest::addMockProvider)

        val result = favouritesDao.getFavouriteStop("123456")

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getFavouriteStopWithPopulatedCursorReturnsFavouriteStop() = runTest {
        val expectedProjection = getExpectedProjectionForFavouriteStopSingle()
        val expectedSelectionArgs = arrayOf("123456")
        val cursor = MatrixCursor(expectedProjection)
        val expected = FavouriteStop(1L, "123456", "Stop name")
        cursor.addRow(arrayOf(1, "Stop name"))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals("${FavouritesContract.STOP_CODE} = ?", selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertNull(sortOrder)

                return cursor
            }
        }.also(this@AndroidFavouritesDaoTest::addMockProvider)

        val result = favouritesDao.getFavouriteStop("123456")

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getFavouriteStopsWithNullCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForFavouriteStop()
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertEquals("${FavouritesContract.STOP_NAME} ASC", sortOrder)

                return null
            }
        }.also(this@AndroidFavouritesDaoTest::addMockProvider)

        val result = favouritesDao.getFavouriteStops()

        assertNull(result)
    }

    @Test
    fun getFavouriteStopsWithEmptyCursorReturnsNull() = runTest {
        val expectedProjection = getExpectedProjectionForFavouriteStop()
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
                assertNull(selection)
                assertNull(selectionArgs)
                assertEquals("${FavouritesContract.STOP_NAME} ASC", sortOrder)

                return cursor
            }
        }.also(this@AndroidFavouritesDaoTest::addMockProvider)

        val result = favouritesDao.getFavouriteStops()

        assertNull(result)
        assertTrue(cursor.isClosed)
    }

    @Test
    fun getFavouriteStopsWithNonEmptyResultIsHandledCorrectly() = runTest {
        val expectedProjection = getExpectedProjectionForFavouriteStop()
        val cursor = MatrixCursor(expectedProjection).apply {
            addRow(arrayOf(1, "100001", "Stop 1"))
            addRow(arrayOf(2, "100002", "Stop 2"))
            addRow(arrayOf(3, "100003", "Stop 3"))
        }
        val expected = listOf(
                FavouriteStop(1L, "100001", "Stop 1"),
                FavouriteStop(2L, "100002", "Stop 2"),
                FavouriteStop(3L, "100003", "Stop 3"))
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertNull(selection)
                assertNull(selectionArgs)
                assertEquals("${FavouritesContract.STOP_NAME} ASC", sortOrder)

                return cursor
            }
        }.also(this@AndroidFavouritesDaoTest::addMockProvider)

        val result = favouritesDao.getFavouriteStops()

        assertEquals(expected, result)
        assertTrue(cursor.isClosed)
    }

    private fun addMockProvider(provider: ContentProvider) {
        mockContentResolver.addProvider(TEST_AUTHORITY, provider)
    }

    private fun getExpectedProjectionForFavouriteStop() = arrayOf(
            FavouritesContract.ID,
            FavouritesContract.STOP_CODE,
            FavouritesContract.STOP_NAME)

    private fun getExpectedProjectionForFavouriteStopSingle() = arrayOf(
            FavouritesContract.ID,
            FavouritesContract.STOP_NAME)
}