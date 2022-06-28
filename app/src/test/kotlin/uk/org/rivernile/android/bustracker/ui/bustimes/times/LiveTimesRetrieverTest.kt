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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
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
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.NoConnectivityException
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Service
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Stop
import uk.org.rivernile.android.bustracker.core.livetimes.LiveTimesRepository
import uk.org.rivernile.android.bustracker.core.livetimes.Result
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [LiveTimesRetriever].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LiveTimesRetrieverTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var liveTimesRepository: LiveTimesRepository
    @Mock
    private lateinit var servicesRepository: ServicesRepository
    @Mock
    private lateinit var liveTimesMapper: LiveTimesMapper

    private lateinit var retriever: LiveTimesRetriever

    @Before
    fun setUp() {
        retriever = LiveTimesRetriever(liveTimesRepository, servicesRepository, liveTimesMapper)
    }

    @Test
    fun getLiveTimesFlowWithInProgressResultDoesNotGetColours() = runTest {
        val liveTimesFlow = flowOf(Result.InProgress)
        whenever(liveTimesRepository.getLiveTimesFlow("123456", 4))
                .thenReturn(liveTimesFlow)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", Result.InProgress,
                null))
                .thenReturn(UiResult.InProgress)

        val observer = retriever.getLiveTimesFlow("123456", 4).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiResult.InProgress)
        verify(servicesRepository, never())
                .getColoursForServicesFlow(anyOrNull())
    }

    @Test
    fun getLiveTimesFlowWithErrorsResultDoesNotGetColours() = runTest {
        val errorResult = Result.Error(123L, NoConnectivityException())
        val errorUiResult = UiResult.Error(123L, ErrorType.NO_CONNECTIVITY)
        val liveTimesFlow = flowOf(Result.InProgress, errorResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456", 4))
                .thenReturn(liveTimesFlow)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", Result.InProgress,
                null))
                .thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", errorResult,
                null))
                .thenReturn(errorUiResult)

        val observer = retriever.getLiveTimesFlow("123456", 4).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiResult.InProgress, errorUiResult)
        verify(servicesRepository, never())
                .getColoursForServicesFlow(anyOrNull())
    }

    @Test
    fun getLiveTimesFlowWithSuccessButNoStopDoesNotGetColours() = runTest {
        val successResult = Result.Success(
                LiveTimes(
                        emptyMap(),
                        123L,
                        false))
        val errorUiResult = UiResult.Error(123L, ErrorType.NO_DATA)
        val liveTimesFlow = flowOf(Result.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456", 4))
                .thenReturn(liveTimesFlow)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", Result.InProgress,
                null))
                .thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", successResult,
                null))
                .thenReturn(errorUiResult)

        val observer = retriever.getLiveTimesFlow("123456", 4).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiResult.InProgress, errorUiResult)
        verify(servicesRepository, never())
                .getColoursForServicesFlow(anyOrNull())
    }

    @Test
    fun getLiveTimesFlowWithSuccessButNoServicesDoesNotGetColours() = runTest {
        val successResult = Result.Success(
                LiveTimes(
                        mapOf(
                                "123456" to Stop(
                                        "123456",
                                        "Stop name",
                                        emptyList(),
                                        false)
                        ),
                        123L,
                        false))
        val errorUiResult = UiResult.Error(123L, ErrorType.NO_DATA)
        val liveTimesFlow = flowOf(Result.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456", 4))
                .thenReturn(liveTimesFlow)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", Result.InProgress,
                null))
                .thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", successResult,
                null))
                .thenReturn(errorUiResult)

        val observer = retriever.getLiveTimesFlow("123456", 4).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiResult.InProgress, errorUiResult)
        verify(servicesRepository, never())
                .getColoursForServicesFlow(anyOrNull())
    }

    @Test
    fun getLiveTimesFlowWithSuccessSingleServiceWithNullColours() = runTest {
        val successResult = Result.Success(
                LiveTimes(
                        mapOf(
                                "123456" to Stop(
                                        "123456",
                                        "Stop name",
                                        listOf(
                                                Service(
                                                        "1",
                                                        emptyList(),
                                                        null,
                                                        null,
                                                        isDisrupted = false,
                                                        isDiverted = false)
                                        ),
                                        false)
                        ),
                        123L,
                        false))
        val serviceColoursFlow = flowOf<Map<String, Int>?>(null)
        val successUiResult = UiResult.Success(
                123L,
                UiStop(
                        "123456",
                        "Stop name",
                        listOf(
                                UiService(
                                        "1",
                                        null,
                                        emptyList()))))
        val liveTimesFlow = flowOf(Result.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456", 4))
                .thenReturn(liveTimesFlow)
        whenever(servicesRepository.getColoursForServicesFlow(arrayOf("1")))
                .thenReturn(serviceColoursFlow)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", Result.InProgress,
                null))
                .thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", successResult,
                null))
                .thenReturn(successUiResult)

        val observer = retriever.getLiveTimesFlow("123456", 4).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiResult.InProgress, successUiResult)
    }

    @Test
    fun getLiveTimesFlowWithSuccessSingleServiceWithEmptyColours() = runTest {
        val successResult = Result.Success(
                LiveTimes(
                        mapOf(
                                "123456" to Stop(
                                        "123456",
                                        "Stop name",
                                        listOf(
                                                Service(
                                                        "1",
                                                        emptyList(),
                                                        null,
                                                        null,
                                                        isDisrupted = false,
                                                        isDiverted = false)
                                        ),
                                        false)
                        ),
                        123L,
                        false))
        val serviceColoursFlow = flowOf<Map<String, Int>?>(emptyMap())
        val successUiResult = UiResult.Success(
                123L,
                UiStop(
                        "123456",
                        "Stop name",
                        listOf(
                                UiService(
                                        "1",
                                        null,
                                        emptyList()))))
        val liveTimesFlow = flowOf(Result.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456", 4))
                .thenReturn(liveTimesFlow)
        whenever(servicesRepository.getColoursForServicesFlow(arrayOf("1")))
                .thenReturn(serviceColoursFlow)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", Result.InProgress,
                null))
                .thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", successResult,
                emptyMap()))
                .thenReturn(successUiResult)

        val observer = retriever.getLiveTimesFlow("123456", 4).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiResult.InProgress, successUiResult)
    }

    @Test
    fun getLiveTimesFlowWithSuccessSingleServiceGetsColours() = runTest {
        val successResult = Result.Success(
                LiveTimes(
                        mapOf(
                                "123456" to Stop(
                                        "123456",
                                        "Stop name",
                                        listOf(
                                                Service(
                                                        "1",
                                                        emptyList(),
                                                        null,
                                                        null,
                                                        isDisrupted = false,
                                                        isDiverted = false)
                                        ),
                                        false)
                        ),
                        123L,
                        false))
        val serviceColours = mapOf(
                "1" to 0xFFFFFF)
        val serviceColoursFlow = flowOf(serviceColours)
        val successUiResult = UiResult.Success(
                123L,
                UiStop(
                        "123456",
                        "Stop name",
                        listOf(
                                UiService(
                                        "1",
                                        0xFFFFFF,
                                        emptyList()))))
        val liveTimesFlow = flowOf(Result.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456", 4))
                .thenReturn(liveTimesFlow)
        whenever(servicesRepository.getColoursForServicesFlow(arrayOf("1")))
                .thenReturn(serviceColoursFlow)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", Result.InProgress,
                null))
                .thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", successResult,
                serviceColours))
                .thenReturn(successUiResult)

        val observer = retriever.getLiveTimesFlow("123456", 4).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiResult.InProgress, successUiResult)
    }

    @Test
    fun getLiveTimesFlowWithSuccessMultipleServicesGetsColours() = runTest {
        val successResult = Result.Success(
                LiveTimes(
                        mapOf(
                                "123456" to Stop(
                                        "123456",
                                        "Stop name",
                                        listOf(
                                                Service(
                                                        "1",
                                                        emptyList(),
                                                        null,
                                                        null,
                                                        isDisrupted = false,
                                                        isDiverted = false),
                                                Service(
                                                        "2",
                                                        emptyList(),
                                                        null,
                                                        null,
                                                        isDisrupted = false,
                                                        isDiverted = false),
                                                Service(
                                                        "3",
                                                        emptyList(),
                                                        null,
                                                        null,
                                                        isDisrupted = false,
                                                        isDiverted = false)
                                        ),
                                        false)
                        ),
                        123L,
                        false))
        val serviceColours = mapOf(
                "1" to 0xFFFFFF,
                "2" to 0xFF0000,
                "3" to 0x00FF00)
        val serviceColoursFlow = flowOf(serviceColours)
        val successUiResult = UiResult.Success(
                123L,
                UiStop(
                        "123456",
                        "Stop name",
                        listOf(
                                UiService(
                                        "1",
                                        0xFFFFFF,
                                        emptyList()),
                                UiService(
                                        "2",
                                        0xFF0000,
                                        emptyList()),
                                UiService(
                                        "3",
                                        0x00FF00,
                                        emptyList())
                        )))
        val liveTimesFlow = flowOf(Result.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456", 4))
                .thenReturn(liveTimesFlow)
        whenever(servicesRepository.getColoursForServicesFlow(arrayOf("1", "2", "3")))
                .thenReturn(serviceColoursFlow)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", Result.InProgress,
                null))
                .thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", successResult,
                serviceColours))
                .thenReturn(successUiResult)

        val observer = retriever.getLiveTimesFlow("123456", 4).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiResult.InProgress, successUiResult)
    }

    @Test
    fun getLiveTimesFlowWithSuccessSingleServiceUpdatesColours() = runTest {
        val successResult = Result.Success(
                LiveTimes(
                        mapOf(
                                "123456" to Stop(
                                        "123456",
                                        "Stop name",
                                        listOf(
                                                Service(
                                                        "1",
                                                        emptyList(),
                                                        null,
                                                        null,
                                                        isDisrupted = false,
                                                        isDiverted = false)
                                        ),
                                        false)
                        ),
                        123L,
                        false))
        val serviceColours1 = mapOf(
                "1" to 0xFFFFFF)
        val serviceColours2 = mapOf(
                "1" to 0xFF0000)
        val serviceColoursFlow = flowOf(serviceColours1, serviceColours2)
        val successUiResult1 = UiResult.Success(
                123L,
                UiStop(
                        "123456",
                        "Stop name",
                        listOf(
                                UiService(
                                        "1",
                                        0xFFFFFF,
                                        emptyList()))))
        val successUiResult2 = UiResult.Success(
                123L,
                UiStop(
                        "123456",
                        "Stop name",
                        listOf(
                                UiService(
                                        "1",
                                        0xFF0000,
                                        emptyList()))))
        val liveTimesFlow = flowOf(Result.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456", 4))
                .thenReturn(liveTimesFlow)
        whenever(servicesRepository.getColoursForServicesFlow(arrayOf("1")))
                .thenReturn(serviceColoursFlow)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", Result.InProgress,
                null))
                .thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", successResult,
                serviceColours1))
                .thenReturn(successUiResult1)
        whenever(liveTimesMapper.mapLiveTimesAndColoursToUiResult("123456", successResult,
                serviceColours2))
                .thenReturn(successUiResult2)

        val observer = retriever.getLiveTimesFlow("123456", 4).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiResult.InProgress, successUiResult1, successUiResult2)
    }
}