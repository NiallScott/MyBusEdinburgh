/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops

import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [FavouritesStateRetriever].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class FavouritesStateRetrieverTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var favouritesRepository: FavouritesRepository

    private lateinit var retriever: FavouritesStateRetriever

    @Before
    fun setUp() {
        retriever = FavouritesStateRetriever(favouritesRepository)
    }

    @Test
    fun getIsAddedAsFavouriteStopEmitsNullWhenStopCodeIsNull() = runTest {
        val stopCodeFlow = flowOf(null)

        val observer = retriever.getIsAddedAsFavouriteStopFlow(stopCodeFlow).test(this)
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun getIsAddedAsFavouriteStopEmitsNullWhenStopCodeIsEmpty() = runTest {
        val stopCodeFlow = flowOf("")

        val observer = retriever.getIsAddedAsFavouriteStopFlow(stopCodeFlow).test(this)
        advanceUntilIdle()

        observer.assertValues(null)
    }

    @Test
    fun getIsAddedAsFavouriteStopEmitsValuesFromFavouritesRepository() = runTest {
        val stopCodeFlow = flowOf("123456")
        whenever(favouritesRepository.isStopAddedAsFavouriteFlow("123456"))
                .thenReturn(flowOf(false, true, false))

        val observer = retriever.getIsAddedAsFavouriteStopFlow(stopCodeFlow).test(this)
        advanceUntilIdle()

        observer.assertValues(null, false, true, false)
    }
}