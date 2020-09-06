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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [LiveTimesLoader].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class LiveTimesLoaderTest {

    @Rule
    @JvmField
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var liveTimesRetriever: LiveTimesRetriever
    @Mock
    private lateinit var preferenceRepository: PreferenceRepository

    private lateinit var loader: LiveTimesLoader

    @Before
    fun setUp() {
        loader = LiveTimesLoader(liveTimesRetriever, preferenceRepository)
    }

    @Test
    fun loadLiveTimesFlowWithNullStopCodeYieldsNoStopCodeError() = coroutineRule.runBlockingTest {
        val stopCodes = flowOf<String?>(null)
        val numberOfDeparturesFlow = flowOf(4)
        val refreshFlow = flowOf(Unit)
        whenever(preferenceRepository.getLiveTimesNumberOfDeparturesFlow())
                .thenReturn(numberOfDeparturesFlow)

        val observer = loader.loadLiveTimesFlow(stopCodes, refreshFlow).test(this)
        observer.finish()

        observer.assertValues(UiResult.Error(Long.MAX_VALUE, ErrorType.NO_STOP_CODE))
        verify(liveTimesRetriever, never())
                .getLiveTimesFlow(any(), any())
    }

    @Test
    fun loadLiveTimesFlowWithPopulatedStopCodeLoadsLiveTimes() = coroutineRule.runBlockingTest {
        val stopCodes = flowOf("123456")
        val numberOfDeparturesFlow = flowOf(4)
        val refreshFlow = flowOf(Unit)
        val stop = UiStop("123456", null, emptyList())
        val loadFlow = flowOf(
                UiResult.InProgress,
                UiResult.Success(123L, stop))
        whenever(preferenceRepository.getLiveTimesNumberOfDeparturesFlow())
                .thenReturn(numberOfDeparturesFlow)
        whenever(liveTimesRetriever.getLiveTimesFlow("123456", 4))
                .thenReturn(loadFlow)

        val observer = loader.loadLiveTimesFlow(stopCodes, refreshFlow).test(this)
        observer.finish()

        observer.assertValues(
                UiResult.InProgress,
                UiResult.Success(123L, stop))
    }

    @Test
    fun loadLiveTimesFlowWithStopCodeChangeCausesReload() = coroutineRule.runBlockingTest {
        val stopCodes = flow {
            emit("123456")
            delay(100)
            emit("246813")
        }
        val numberOfDeparturesFlow = flowOf(4)
        val refreshFlow = flowOf(Unit)
        val stop1 = UiStop("123456", null, emptyList())
        val stop2 = UiStop("246813", null, emptyList())
        val loadFlow1 = flowOf(
                UiResult.InProgress,
                UiResult.Success(123L, stop1))
        val loadFlow2 = flowOf(
                UiResult.InProgress,
                UiResult.Success(123L, stop2))
        whenever(preferenceRepository.getLiveTimesNumberOfDeparturesFlow())
                .thenReturn(numberOfDeparturesFlow)
        whenever(liveTimesRetriever.getLiveTimesFlow("123456", 4))
                .thenReturn(loadFlow1)
        whenever(liveTimesRetriever.getLiveTimesFlow("246813", 4))
                .thenReturn(loadFlow2)

        val observer = loader.loadLiveTimesFlow(stopCodes, refreshFlow).test(this)
        advanceTimeBy(1000)
        observer.finish()

        observer.assertValues(
                UiResult.InProgress,
                UiResult.Success(123L, stop1),
                UiResult.InProgress,
                UiResult.Success(123L, stop2))
    }

    @Test
    fun loadLiveTimesFlowWithNumberOfDepartureChangeCausesReload() = coroutineRule.runBlockingTest {
        val stopCodes = flowOf("123456")
        val numberOfDeparturesFlow = flow {
            emit(4)
            delay(100)
            emit(2)
        }
        val refreshFlow = flowOf(Unit)
        val stop = UiStop("123456", null, emptyList())
        val loadFlow1 = flowOf(
                UiResult.InProgress,
                UiResult.Error(123L, ErrorType.NO_CONNECTIVITY))
        val loadFlow2 = flowOf(
                UiResult.InProgress,
                UiResult.Success(123L, stop))
        whenever(preferenceRepository.getLiveTimesNumberOfDeparturesFlow())
                .thenReturn(numberOfDeparturesFlow)
        whenever(liveTimesRetriever.getLiveTimesFlow("123456", 4))
                .thenReturn(loadFlow1)
        whenever(liveTimesRetriever.getLiveTimesFlow("123456", 2))
                .thenReturn(loadFlow2)

        val observer = loader.loadLiveTimesFlow(stopCodes, refreshFlow).test(this)
        advanceTimeBy(1000)
        observer.finish()

        observer.assertValues(
                UiResult.InProgress,
                UiResult.Error(123L, ErrorType.NO_CONNECTIVITY),
                UiResult.InProgress,
                UiResult.Success(123L, stop))
    }

    @Test
    fun loadLiveTimesFlowWitRefreshCausesReload() = coroutineRule.runBlockingTest {
        val stopCodes = flowOf("123456")
        val numberOfDeparturesFlow = flowOf(4)
        val refreshFlow = flow {
            emit(Unit)
            delay(100)
            emit(Unit)
        }
        val stop = UiStop("123456", null, emptyList())
        val loadFlow1 = flowOf(
                UiResult.InProgress,
                UiResult.Error(123L, ErrorType.NO_CONNECTIVITY))
        val loadFlow2 = flowOf(
                UiResult.InProgress,
                UiResult.Success(123L, stop))
        whenever(preferenceRepository.getLiveTimesNumberOfDeparturesFlow())
                .thenReturn(numberOfDeparturesFlow)
        whenever(liveTimesRetriever.getLiveTimesFlow("123456", 4))
                .thenReturn(loadFlow1, loadFlow2)

        val observer = loader.loadLiveTimesFlow(stopCodes, refreshFlow).test(this)
        advanceTimeBy(1000)
        observer.finish()

        observer.assertValues(
                UiResult.InProgress,
                UiResult.Error(123L, ErrorType.NO_CONNECTIVITY),
                UiResult.InProgress,
                UiResult.Success(123L, stop))
    }
}