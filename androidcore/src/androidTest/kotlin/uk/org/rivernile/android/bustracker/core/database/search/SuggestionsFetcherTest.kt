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

package uk.org.rivernile.android.bustracker.core.database.search

import android.app.SearchManager
import android.content.ContentProvider
import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.net.Uri
import android.provider.BaseColumns
import android.test.mock.MockContentProvider
import android.test.mock.MockContentResolver
import android.test.mock.MockContext
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopsContract
import kotlin.test.assertNotNull
import kotlin.test.assertTrue

/**
 * Tests for [SuggestionsFetcher].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class SuggestionsFetcherTest {

    companion object {

        private const val TEST_AUTHORITY = "test.authority"

        private const val EXPECTED_SELECTION = "${BusStopsContract.LOCALITY} LIKE ?"
        private const val EXPECTED_SORT_ORDER = "${BusStopsContract.LOCALITY} ASC"

        private const val MOCK_PACKAGE_NAME = "com.test.application"

        private val COLUMNS = arrayOf(
                SearchManager.SUGGEST_COLUMN_FORMAT,
                SearchManager.SUGGEST_COLUMN_ICON_1,
                SearchManager.SUGGEST_COLUMN_TEXT_1,
                SearchManager.SUGGEST_COLUMN_TEXT_2,
                SearchManager.SUGGEST_COLUMN_QUERY,
                BaseColumns._ID)
    }

    @Mock
    private lateinit var contract: BusStopsContract

    private lateinit var mockContext: Context
    private lateinit var mockContentResolver: MockContentResolver
    private val contentUri = Uri.parse("content://$TEST_AUTHORITY/tableName")

    private lateinit var fetcher: SuggestionsFetcher

    @Before
    fun setUp() {
        mockContentResolver = MockContentResolver()
        mockContext = object : MockContext() {
            override fun getContentResolver() = mockContentResolver

            override fun getPackageName() = MOCK_PACKAGE_NAME
        }

        fetcher = SuggestionsFetcher(mockContext, contract)

        whenever(contract.getContentUri())
                .thenReturn(contentUri)
    }

    @Test
    fun returnsNullCursorWhenNullRecentItemsAndNullSuggestions() {
        val expectedProjection = getExpectedProjectionsForLocality()
        val expectedSelectionArgs = arrayOf("%Test%")
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals(EXPECTED_SELECTION, selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertEquals(EXPECTED_SORT_ORDER, sortOrder)

                return null
            }
        }.also(this::addMockProvider)

        val result = fetcher.fetchAndMergeSuggestions(null, "Test")

        assertNull(result)
    }

    @Test
    fun returnsEmptyCursorWhenEmptyRecentItemsAndNullSuggestions() {
        val expectedProjection = getExpectedProjectionsForLocality()
        val expectedSelectionArgs = arrayOf("%Test%")
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals(EXPECTED_SELECTION, selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertEquals(EXPECTED_SORT_ORDER, sortOrder)

                return null
            }
        }.also(this::addMockProvider)
        val recentCursor = createEmptyCursor()

        val result = fetcher.fetchAndMergeSuggestions(recentCursor, "Test")

        assertNotNull(result)
        assertEquals(0, result.count)
    }

    @Test
    fun returnsCursorWithOnlyRecentSearchesWhenRecentHasItemAndNullSuggestions() {
        val expectedProjection = getExpectedProjectionsForLocality()
        val expectedSelectionArgs = arrayOf("%Test%")
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals(EXPECTED_SELECTION, selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertEquals(EXPECTED_SORT_ORDER, sortOrder)

                return null
            }
        }.also(this::addMockProvider)
        val recentRow = arrayOf(
                null,
                "android.resource://$MOCK_PACKAGE_NAME/icon",
                "Row 1",
                "Row 2",
                "Query",
                1)
        val recentCursor = createEmptyCursor().also {
            it.addRow(recentRow)
        }

        val result = fetcher.fetchAndMergeSuggestions(recentCursor, "Test")

        assertNotNull(result)
        assertEquals(1, result.count)
        assertTrue(result.moveToFirst())
        assertNull(result.getString(0))
        assertEquals(recentRow[1], result.getString(1))
        assertEquals(recentRow[2], result.getString(2))
        assertEquals(recentRow[3], result.getString(3))
        assertEquals(recentRow[4], result.getString(4))
        assertEquals(1, result.getInt(5))
    }

    @Test
    fun returnsEmptyCursorWhenNullRecentItemsAndEmptyRecentSuggestions() {
        val expectedProjection = getExpectedProjectionsForLocality()
        val expectedSelectionArgs = arrayOf("%Test%")
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals(EXPECTED_SELECTION, selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertEquals(EXPECTED_SORT_ORDER, sortOrder)

                return MatrixCursor(expectedProjection)
            }
        }.also(this::addMockProvider)

        val result = fetcher.fetchAndMergeSuggestions(null, "Test")

        assertNotNull(result)
        assertEquals(0, result.count)
    }

    @Test
    fun returnsCursorWithOnlySuggestionsWhenRecentIsNullAndHasSingleSuggestion() {
        val expectedProjection = getExpectedProjectionsForLocality()
        val expectedSelectionArgs = arrayOf("%Test%")
        val localityCursor = MatrixCursor(arrayOf(BusStopsContract.LOCALITY)).also {
            it.addRow(arrayOf("Test Locality"))
        }
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals(EXPECTED_SELECTION, selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertEquals(EXPECTED_SORT_ORDER, sortOrder)

                return localityCursor
            }
        }.also(this::addMockProvider)
        val suggestionRow = arrayOf(
                null,
                "android.resource://$MOCK_PACKAGE_NAME/${android.R.color.transparent}",
                "Test Locality",
                null,
                "Test Locality",
                1)

        val result = fetcher.fetchAndMergeSuggestions(null, "Test")

        assertNotNull(result)
        assertEquals(1, result.count)
        assertTrue(result.moveToFirst())
        assertNull(result.getString(0))
        assertEquals(suggestionRow[1], result.getString(1))
        assertEquals(suggestionRow[2], result.getString(2))
        assertNull(suggestionRow[3])
        assertEquals(suggestionRow[4], result.getString(4))
        assertEquals(suggestionRow[5], result.getInt(5))
    }

    @Test
    fun returnsCursorWithRecentAndSuggestionsWhenRecentIsPopulatedAndSuggestionsIsPopulated() {
        val expectedProjection = getExpectedProjectionsForLocality()
        val expectedSelectionArgs = arrayOf("%Test%")
        val localityCursor = MatrixCursor(arrayOf(BusStopsContract.LOCALITY)).also {
            it.addRow(arrayOf("Test Locality"))
        }
        object : MockContentProvider() {
            override fun query(
                    uri: Uri,
                    projection: Array<out String>?,
                    selection: String?,
                    selectionArgs: Array<out String>?,
                    sortOrder: String?): Cursor? {
                assertEquals(contentUri, uri)
                assertArrayEquals(expectedProjection, projection)
                assertEquals(EXPECTED_SELECTION, selection)
                assertArrayEquals(expectedSelectionArgs, selectionArgs)
                assertEquals(EXPECTED_SORT_ORDER, sortOrder)

                return localityCursor
            }
        }.also(this::addMockProvider)
        val recentRow = arrayOf(
                null,
                "android.resource://$MOCK_PACKAGE_NAME/icon",
                "Row 1",
                "Row 2",
                "Query",
                1)
        val recentCursor = createEmptyCursor().also {
            it.addRow(recentRow)
        }
        val suggestionRow = arrayOf(
                null,
                "android.resource://$MOCK_PACKAGE_NAME/${android.R.color.transparent}",
                "Test Locality",
                null,
                "Test Locality",
                2)

        val result = fetcher.fetchAndMergeSuggestions(recentCursor, "Test")

        assertNotNull(result)
        assertEquals(2, result.count)
        // Recent Cursor
        assertTrue(result.moveToFirst())
        assertNull(result.getString(0))
        assertEquals(recentRow[1], result.getString(1))
        assertEquals(recentRow[2], result.getString(2))
        assertEquals(recentRow[3], result.getString(3))
        assertEquals(recentRow[4], result.getString(4))
        assertEquals(recentRow[5], result.getInt(5))
        // Suggestion Cursor
        assertTrue(result.moveToNext())
        assertNull(result.getString(0))
        assertEquals(suggestionRow[1], result.getString(1))
        assertEquals(suggestionRow[2], result.getString(2))
        assertNull(suggestionRow[3])
        assertEquals(suggestionRow[4], result.getString(4))
        assertEquals(suggestionRow[5], result.getInt(5))
    }

    private fun addMockProvider(provider: ContentProvider) {
        mockContentResolver.addProvider(TEST_AUTHORITY, provider)
    }

    private fun getExpectedProjectionsForLocality() = arrayOf(
            "DISTINCT " + BusStopsContract.LOCALITY)

    private fun createEmptyCursor() = MatrixCursor(COLUMNS)
}