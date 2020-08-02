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

package uk.org.rivernile.android.bustracker.core.favourites

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.settings.daos.FavouritesDao
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [FavouritesRepository].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class FavouritesRepositoryTest {

    @Rule
    @JvmField
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var favouritesDao: FavouritesDao

    private lateinit var repository: FavouritesRepository

    @Before
    fun setUp() {
        repository = FavouritesRepository(favouritesDao, coroutineRule.testDispatcher)
    }

    @Test
    fun isStopAddedAsFavouriteFlowGetsInitialValue() = coroutineRule.runBlockingTest {
        whenever(favouritesDao.isStopAddedAsFavourite("123456"))
                .thenReturn(false)

        val observer = repository.isStopAddedAsFavouriteFlow("123456").test(this)
        observer.finish()

        observer.assertValues(false)
        verify(favouritesDao)
                .removeOnFavouritesChangedListener(any())
    }

    @Test
    fun isStopAddedAsFavouriteFlowRespondsToFavouritesChanged() = coroutineRule.runBlockingTest {
        doAnswer {
            val listener = it.getArgument<FavouritesDao.OnFavouritesChangedListener>(0)
            listener.onFavouritesChanged()
            listener.onFavouritesChanged()
        }.whenever(favouritesDao).addOnFavouritesChangedListener(any())
        whenever(favouritesDao.isStopAddedAsFavourite("123456"))
                .thenReturn(false, true, false)

        val observer = repository.isStopAddedAsFavouriteFlow("123456").test(this)
        observer.finish()

        observer.assertValues(false, true, false)
        verify(favouritesDao)
                .removeOnFavouritesChangedListener(any())
    }
}