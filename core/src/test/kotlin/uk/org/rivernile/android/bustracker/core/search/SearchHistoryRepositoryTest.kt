/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.search

import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.verify
import uk.org.rivernile.android.bustracker.core.database.search.daos.SearchHistoryDao
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [SearchHistoryRepository].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class SearchHistoryRepositoryTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var searchHistoryDao: SearchHistoryDao

    private lateinit var searchHistoryRepository: SearchHistoryRepository

    @Before
    fun setUp() {
        searchHistoryRepository = SearchHistoryRepository(searchHistoryDao)
    }

    @Test
    fun addSearchTermAddsSearchTermToDao() = runTest {
        searchHistoryRepository.addSearchTerm("test")

        verify(searchHistoryDao)
                .addSearchTerm("test")
    }

    @Test
    fun clearSearchHistoryCallsDao() = runTest {
        searchHistoryRepository.clearSearchHistory()

        verify(searchHistoryDao)
                .clearSearchHistory()
    }
}