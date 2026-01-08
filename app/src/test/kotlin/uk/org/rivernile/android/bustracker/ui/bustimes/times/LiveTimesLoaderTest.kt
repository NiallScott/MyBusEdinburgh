/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [LiveTimesLoader].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class LiveTimesLoaderTest {

    @Mock
    private lateinit var arguments: Arguments
    @Mock
    private lateinit var refreshController: RefreshController
    @Mock
    private lateinit var liveTimesRetriever: LiveTimesRetriever
    @Mock
    private lateinit var liveTimesTransform: LiveTimesTransform
    @Mock
    private lateinit var preferenceRepository: PreferenceRepository

    private lateinit var loader: LiveTimesLoader

    @BeforeTest
    fun setUp() {
        loader = LiveTimesLoader(
            arguments,
            refreshController,
            liveTimesRetriever,
            liveTimesTransform,
            preferenceRepository
        )
    }

    @Test
    fun loadLiveTimesFlowWithNullStopCodeYieldsNoStopCodeError() = runTest {
        whenever(arguments.stopCodeFlow)
            .thenReturn(flowOf(null))
        whenever(refreshController.refreshTriggerFlow)
            .thenReturn(flowOf(Unit))
        whenever(preferenceRepository.liveTimesNumberOfDeparturesFlow)
            .thenReturn(flowOf(4))
        val expected = mock<UiTransformedResult.Error>()
        whenever(liveTimesTransform
            .getLiveTimesTransformFlow(UiResult.Error(Long.MAX_VALUE, ErrorType.NO_STOP_CODE))
        ).thenReturn(flowOf(expected))

        loader.liveTimesFlow.test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
        verify(liveTimesRetriever, never())
                .getLiveTimesFlow(any(), any())
    }

    @Test
    fun loadLiveTimesFlowWithPopulatedStopCodeLoadsLiveTimes() = runTest {
        val stop = UiStop("123456", emptyList())
        whenever(arguments.stopCodeFlow)
            .thenReturn(flowOf("123456"))
        whenever(refreshController.refreshTriggerFlow)
            .thenReturn(flowOf(Unit))
        whenever(preferenceRepository.liveTimesNumberOfDeparturesFlow)
            .thenReturn(flowOf(4))
        whenever(liveTimesRetriever.getLiveTimesFlow("123456", 4))
            .thenReturn(flowOf(UiResult.Success(123L, stop)))
        val expected = mock<UiTransformedResult.Success>()
        whenever(liveTimesTransform
            .getLiveTimesTransformFlow(UiResult.Success(123L, stop))
        ).thenReturn(flowOf(expected))

        loader.liveTimesFlow.test {
            assertEquals(expected, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun loadLiveTimesFlowWithStopCodeChangeCausesReload() = runTest {
        val stop1 = UiStop("123456", emptyList())
        val stop2 = UiStop("246813", emptyList())
        whenever(arguments.stopCodeFlow)
            .thenReturn(intervalFlowOf(0L, 10L, "123456", "246813"))
        whenever(refreshController.refreshTriggerFlow)
            .thenReturn(flowOf(Unit))
        whenever(preferenceRepository.liveTimesNumberOfDeparturesFlow)
            .thenReturn(flowOf(4))
        whenever(liveTimesRetriever.getLiveTimesFlow("123456", 4))
            .thenReturn(flowOf(UiResult.Success(123L, stop1)))
        whenever(liveTimesRetriever.getLiveTimesFlow("246813", 4))
            .thenReturn(flowOf(UiResult.Success(123L, stop2)))
        val expected1 = mock<UiTransformedResult.Success>()
        val expected2 = mock<UiTransformedResult.Success>()
        whenever(liveTimesTransform
            .getLiveTimesTransformFlow(UiResult.Success(123L, stop1))
        ).thenReturn(flowOf(expected1))
        whenever(liveTimesTransform
            .getLiveTimesTransformFlow(UiResult.Success(123L, stop2))
        ).thenReturn(flowOf(expected2))

        loader.liveTimesFlow.test {
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun loadLiveTimesFlowWithNumberOfDepartureChangeCausesReload() = runTest {
        val stop = UiStop("123456", emptyList())
        whenever(arguments.stopCodeFlow)
            .thenReturn(flowOf("123456"))
        whenever(refreshController.refreshTriggerFlow)
            .thenReturn(flowOf(Unit))
        whenever(preferenceRepository.liveTimesNumberOfDeparturesFlow)
            .thenReturn(intervalFlowOf(0L, 10L, 4, 2))
        whenever(liveTimesRetriever.getLiveTimesFlow("123456", 4))
            .thenReturn(flowOf(UiResult.Error(123L, ErrorType.NO_CONNECTIVITY)))
        whenever(liveTimesRetriever.getLiveTimesFlow("123456", 2))
            .thenReturn(flowOf(UiResult.Success(123L, stop)))
        val expected1 = mock<UiTransformedResult.Error>()
        val expected2 = mock<UiTransformedResult.Success>()
        whenever(liveTimesTransform
            .getLiveTimesTransformFlow(UiResult.Error(123L, ErrorType.NO_CONNECTIVITY))
        ).thenReturn(flowOf(expected1))
        whenever(liveTimesTransform
            .getLiveTimesTransformFlow(UiResult.Success(123L, stop))
        ).thenReturn(flowOf(expected2))

        loader.liveTimesFlow.test {
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun loadLiveTimesFlowWitRefreshCausesReload() = runTest {
        val stop = UiStop("123456", emptyList())
        val loadFlow1 = flowOf(UiResult.Error(123L, ErrorType.NO_CONNECTIVITY))
        val loadFlow2 = flowOf(UiResult.Success(123L, stop))
        whenever(arguments.stopCodeFlow)
            .thenReturn(flowOf("123456"))
        whenever(refreshController.refreshTriggerFlow)
            .thenReturn(intervalFlowOf(0L, 10L, Unit, Unit))
        whenever(preferenceRepository.liveTimesNumberOfDeparturesFlow)
            .thenReturn(flowOf(4))
        whenever(liveTimesRetriever.getLiveTimesFlow("123456", 4))
            .thenReturn(loadFlow1, loadFlow2)
        val expected1 = mock<UiTransformedResult.Error>()
        val expected2 = mock<UiTransformedResult.Success>()
        whenever(liveTimesTransform
            .getLiveTimesTransformFlow(UiResult.Error(123L, ErrorType.NO_CONNECTIVITY))
        ).thenReturn(flowOf(expected1))
        whenever(liveTimesTransform
            .getLiveTimesTransformFlow(UiResult.Success(123L, stop))
        ).thenReturn(flowOf(expected2))

        loader.liveTimesFlow.test {
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            awaitComplete()
        }
    }
}
