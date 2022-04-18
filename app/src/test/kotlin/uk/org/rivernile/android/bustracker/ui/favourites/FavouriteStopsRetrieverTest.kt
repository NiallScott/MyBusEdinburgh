/*
 * Copyright (C) 2021 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.settings.entities.FavouriteStop
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [FavouriteStopsRetriever].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class FavouriteStopsRetrieverTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var favouritesRepository: FavouritesRepository
    @Mock
    private lateinit var serviceStopsRepository: ServiceStopsRepository

    private lateinit var retriever: FavouriteStopsRetriever

    @Before
    fun setUp() {
        retriever = FavouriteStopsRetriever(favouritesRepository, serviceStopsRepository)
    }

    @Test
    fun favouriteStopsFlowWithNullFavouriteStopsEmitsEmptyList() = runTest {
        whenever(favouritesRepository.favouriteStopsFlow)
                .thenReturn(flowOf(null))

        val observer = retriever.favouriteStopsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null, emptyList())
        verify(serviceStopsRepository, never())
                .getServicesForStopsFlow(any())
    }

    @Test
    fun favouriteStopsFlowWithEmptyFavouriteStopsEmitsEmptyList() = runTest {
        whenever(favouritesRepository.favouriteStopsFlow)
                .thenReturn(flowOf(emptyList()))

        val observer = retriever.favouriteStopsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null, emptyList())
        verify(serviceStopsRepository, never())
                .getServicesForStopsFlow(any())
    }

    @Test
    fun favouriteStopsFlowWithFavouriteStopsAndNullServicesEmitsExpectedList() = runTest {
        val favouriteStops = listOf(
                FavouriteStop(1L, "111111", "Favourite 1"),
                FavouriteStop(2L, "222222", "Favourite 2"),
                FavouriteStop(3L, "333333", "Favourite 3"))
        val expected = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Favourite 1"), null),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Favourite 2"), null),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Favourite 3"), null))
        whenever(favouritesRepository.favouriteStopsFlow)
                .thenReturn(flowOf(favouriteStops))
        whenever(serviceStopsRepository.getServicesForStopsFlow(any()))
                .thenReturn(flowOf(null))

        val observer = retriever.favouriteStopsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null, expected)
    }

    @Test
    fun favouriteStopsFlowWithFavouriteStopsAndEmptyServicesEmitsExpectedList() = runTest {
        val favouriteStops = listOf(
                FavouriteStop(1L, "111111", "Favourite 1"),
                FavouriteStop(2L, "222222", "Favourite 2"),
                FavouriteStop(3L, "333333", "Favourite 3"))
        val expected = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Favourite 1"), null),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Favourite 2"), null),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Favourite 3"), null))
        whenever(favouritesRepository.favouriteStopsFlow)
                .thenReturn(flowOf(favouriteStops))
        whenever(serviceStopsRepository.getServicesForStopsFlow(any()))
                .thenReturn(flowOf(emptyMap()))

        val observer = retriever.favouriteStopsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null, expected)
    }

    @Test
    fun favouriteStopsFlowWithFavouritesAndPopulatedServicesEmitsExpectedList() = runTest {
        val favouriteStops = listOf(
                FavouriteStop(1L, "111111", "Favourite 1"),
                FavouriteStop(2L, "222222", "Favourite 2"),
                FavouriteStop(3L, "333333", "Favourite 3"))
        val expected = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Favourite 1"), listOf("1", "2", "3")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Favourite 2"), null),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Favourite 3"), listOf("1")))
        whenever(favouritesRepository.favouriteStopsFlow)
                .thenReturn(flowOf(favouriteStops))
        val stopCodes = setOf("111111", "222222", "333333")
        whenever(serviceStopsRepository.getServicesForStopsFlow(stopCodes))
                .thenReturn(flowOf(mapOf(
                        "111111" to listOf("1", "2", "3"),
                        "333333" to listOf("1"))))

        val observer = retriever.favouriteStopsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null, expected)
    }

    @Test
    fun favouriteStopsFlowWithChangingFavouritesEmitsExpectedLists() = runTest {
        val favouriteStops1 = listOf(
                FavouriteStop(1L, "111111", "Favourite 1"),
                FavouriteStop(2L, "222222", "Favourite 2"),
                FavouriteStop(3L, "333333", "Favourite 3"))
        val favouriteStops2 = listOf(
                FavouriteStop(1L, "111111", "Favourite 1"),
                FavouriteStop(3L, "333333", "Favourite 3"))
        val favouriteStops3 = listOf(
                FavouriteStop(4L, "444444", "Favourite 4"),
                FavouriteStop(1L, "111111", "Favourite 1"),
                FavouriteStop(2L, "222222", "Favourite 2"))
        val expected1 = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Favourite 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Favourite 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Favourite 3"), listOf("5", "6")))
        val expected2 = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Favourite 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Favourite 3"), listOf("5", "6")))
        val expected3 = listOf(
                UiFavouriteStop(FavouriteStop(4L, "444444", "Favourite 4"), listOf("7", "8")),
                UiFavouriteStop(FavouriteStop(1L, "111111", "Favourite 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Favourite 2"), listOf("3", "4")))
        whenever(favouritesRepository.favouriteStopsFlow)
                .thenReturn(flow {
                    emit(favouriteStops1)
                    delay(100L)
                    emit(favouriteStops2)
                    delay(100L)
                    emit(favouriteStops3)
                })
        whenever(serviceStopsRepository.getServicesForStopsFlow(any()))
                .thenReturn(flowOf(mapOf(
                        "111111" to listOf("1", "2"),
                        "222222" to listOf("3", "4"),
                        "333333" to listOf("5", "6"),
                        "444444" to listOf("7", "8"))))

        val observer = retriever.favouriteStopsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null, expected1, expected2, expected3)
    }

    @Test
    fun favouriteStopsFlowWithChangingServicesEmitsExpectedList() = runTest {
        val favouriteStops = listOf(
                FavouriteStop(1L, "111111", "Favourite 1"),
                FavouriteStop(2L, "222222", "Favourite 2"),
                FavouriteStop(3L, "333333", "Favourite 3"))
        val expected1 = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Favourite 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Favourite 2"), listOf("3", "4")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Favourite 3"), listOf("5", "6")))
        val expected2 = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Favourite 1"), listOf("1", "2")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Favourite 2"), listOf("7", "8")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Favourite 3"), listOf("5", "6")))
        val expected3 = listOf(
                UiFavouriteStop(FavouriteStop(1L, "111111", "Favourite 1"), listOf("9", "10")),
                UiFavouriteStop(FavouriteStop(2L, "222222", "Favourite 2"), listOf("7", "8")),
                UiFavouriteStop(FavouriteStop(3L, "333333", "Favourite 3"), listOf("5", "6")))
        whenever(favouritesRepository.favouriteStopsFlow)
                .thenReturn(flowOf(favouriteStops))
        val stopCodes = setOf("111111", "222222", "333333")
        whenever(serviceStopsRepository.getServicesForStopsFlow(stopCodes))
                .thenReturn(flow {
                    emit(mapOf(
                            "111111" to listOf("1", "2"),
                            "222222" to listOf("3", "4"),
                            "333333" to listOf("5", "6")))
                    delay(100L)
                    emit(mapOf(
                            "111111" to listOf("1", "2"),
                            "222222" to listOf("7", "8"),
                            "333333" to listOf("5", "6")))
                    delay(100L)
                    emit(mapOf(
                            "111111" to listOf("9", "10"),
                            "222222" to listOf("7", "8"),
                            "333333" to listOf("5", "6")))
                })

        val observer = retriever.favouriteStopsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(null, expected1, expected2, expected3)
    }
}