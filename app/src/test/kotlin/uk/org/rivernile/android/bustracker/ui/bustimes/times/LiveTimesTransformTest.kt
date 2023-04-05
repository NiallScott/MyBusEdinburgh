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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.eq
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [LiveTimesTransform].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class LiveTimesTransformTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var preferenceRepository: PreferenceRepository
    @Mock
    private lateinit var transformations: LiveTimesTransformations
    @Mock
    private lateinit var expandedServicesTracker: ExpandedServicesTracker

    @Mock
    private lateinit var uiStop: UiStop
    @Mock
    private lateinit var uiLiveTimesItem1: UiLiveTimesItem
    @Mock
    private lateinit var uiLiveTimesItem2: UiLiveTimesItem
    private val uiServices1 = listOf<UiService>(mock())
    private val uiServices2 = listOf<UiService>(mock())

    private lateinit var transform: LiveTimesTransform

    @Before
    fun setUp() {
        transform = LiveTimesTransform(
            preferenceRepository,
            transformations,
            expandedServicesTracker)

        whenever(uiStop.services)
                .thenReturn(uiServices1)
    }

    @Test
    fun getLiveTimesTransformFlowWithInProgressYieldsInProgress() = runTest {
        givenPreferencesReturnsFlowWithNominalValues()
        whenever(expandedServicesTracker.expandedServicesFlow)
            .thenReturn(flowOf(emptySet()))
        val liveTimesFlow = flowOf(UiResult.InProgress)

        val observer = transform.getLiveTimesTransformFlow(liveTimesFlow).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(UiTransformedResult.InProgress)
    }

    @Test
    fun getLiveTimesTransformFlowWithErrorYieldsError() = runTest {
        givenPreferencesReturnsFlowWithNominalValues()
        val liveTimesFlow = intervalFlowOf(
            100L,
            10L,
            UiResult.InProgress,
            UiResult.Error(123L, ErrorType.SERVER_ERROR))
        whenever(expandedServicesTracker.expandedServicesFlow)
            .thenReturn(flowOf(emptySet()))

        val observer = transform.getLiveTimesTransformFlow(liveTimesFlow).test(this)
        advanceTimeBy(1000L)
        observer.finish()

        observer.assertValues(
            UiTransformedResult.InProgress,
            UiTransformedResult.Error(123L, ErrorType.SERVER_ERROR))
    }

    @Test
    fun getLiveTimesTransformFlowWithSuccessResultingInEmptyResultYieldsNoDataError() = runTest {
        givenPreferencesReturnsFlowWithNominalValues()
        val liveTimesFlow = intervalFlowOf(
            100L,
            10L,
            UiResult.InProgress,
            UiResult.Success(123L, uiStop))
        whenever(expandedServicesTracker.expandedServicesFlow)
            .thenReturn(flowOf(emptySet()))
        whenever(transformations.filterNightServices(uiServices1, false))
            .thenReturn(uiServices1)
        whenever(transformations.sortServices(uiServices1, false))
            .thenReturn(uiServices1)
        whenever(transformations.applyExpansions(uiServices1, emptySet()))
            .thenReturn(emptyList())

        val observer = transform.getLiveTimesTransformFlow(liveTimesFlow).test(this)
        advanceTimeBy(1000L)
        observer.finish()

        observer.assertValues(
            UiTransformedResult.InProgress,
            UiTransformedResult.Error(123L, ErrorType.NO_DATA))
    }

    @Test
    fun getLiveTimesTransformFlowWithSuccessAndResultingInNonEmptyListYieldsSuccess() = runTest {
        whenever(preferenceRepository.isLiveTimesSortByTimeFlow())
            .thenReturn(flowOf(true))
        whenever(preferenceRepository.isLiveTimesShowNightServicesEnabledFlow())
            .thenReturn(flowOf(true))
        val liveTimesFlow = intervalFlowOf(
            100L,
            10L,
            UiResult.InProgress,
            UiResult.Success(123L, uiStop))
        whenever(expandedServicesTracker.expandedServicesFlow)
            .thenReturn(flowOf(setOf("1")))
        whenever(transformations.filterNightServices(uiServices1, true))
            .thenReturn(uiServices1)
        whenever(transformations.sortServices(uiServices1, true))
            .thenReturn(uiServices1)
        whenever(transformations.applyExpansions(uiServices1, setOf("1")))
            .thenReturn(listOf(uiLiveTimesItem1))

        val observer = transform.getLiveTimesTransformFlow(liveTimesFlow).test(this)
        advanceTimeBy(1000L)
        observer.finish()

        observer.assertValues(
            UiTransformedResult.InProgress,
            UiTransformedResult.Success(123L, listOf(uiLiveTimesItem1)))
    }

    @Test
    fun getLiveTimesTransformFlowWithSuccessCopesWithUpstreamRefresh() = runTest {
        whenever(preferenceRepository.isLiveTimesSortByTimeFlow())
            .thenReturn(flowOf(true))
        whenever(preferenceRepository.isLiveTimesShowNightServicesEnabledFlow())
            .thenReturn(flowOf(true))
        val liveTimesFlow = intervalFlowOf(
            100L,
            10L,
            UiResult.InProgress,
            UiResult.Success(123L, uiStop),
            UiResult.InProgress,
            UiResult.Success(123L, mock()))
        whenever(expandedServicesTracker.expandedServicesFlow)
            .thenReturn(flowOf(setOf("1")))
        whenever(transformations.filterNightServices(any(), eq(true)))
            .thenReturn(uiServices1, uiServices2)
        whenever(transformations.sortServices(any(), eq(true)))
            .thenReturn(uiServices1, uiServices2)
        whenever(transformations.applyExpansions(any(), eq(setOf("1"))))
            .thenReturn(listOf(uiLiveTimesItem1), listOf(uiLiveTimesItem2))

        val observer = transform.getLiveTimesTransformFlow(liveTimesFlow).test(this)
        advanceTimeBy(1000L)
        observer.finish()

        observer.assertValues(
            UiTransformedResult.InProgress,
            UiTransformedResult.Success(123L, listOf(uiLiveTimesItem1)),
            UiTransformedResult.InProgress,
            UiTransformedResult.Success(123L, listOf(uiLiveTimesItem2)))
    }

    @Test
    fun getLiveTimesTransformFlowWithSuccessCopesWithNightServicePreferenceChange() = runTest {
        whenever(preferenceRepository.isLiveTimesShowNightServicesEnabledFlow())
            .thenReturn(flow {
                emit(false)
                delay(200L)
                emit(true)
            })
        whenever(preferenceRepository.isLiveTimesSortByTimeFlow())
                .thenReturn(flowOf(false))
        val liveTimesFlow = intervalFlowOf(
            100L,
            10L,
            UiResult.InProgress,
            UiResult.Success(123L, uiStop))
        whenever(expandedServicesTracker.expandedServicesFlow)
            .thenReturn(flowOf(setOf("1")))
        whenever(transformations.filterNightServices(uiServices1, false))
            .thenReturn(uiServices1)
        whenever(transformations.filterNightServices(uiServices1, true))
            .thenReturn(uiServices2)
        whenever(transformations.sortServices(uiServices1, false))
            .thenReturn(uiServices1)
        whenever(transformations.sortServices(uiServices2, false))
            .thenReturn(uiServices2)
        whenever(transformations.applyExpansions(uiServices1, setOf("1")))
            .thenReturn(listOf(uiLiveTimesItem1))
        whenever(transformations.applyExpansions(uiServices2, setOf("1")))
            .thenReturn(listOf(uiLiveTimesItem2))

        val observer = transform.getLiveTimesTransformFlow(liveTimesFlow).test(this)
        advanceTimeBy(1000L)
        observer.finish()

        observer.assertValues(
            UiTransformedResult.InProgress,
            UiTransformedResult.Success(123L, listOf(uiLiveTimesItem1)),
            UiTransformedResult.Success(123L, listOf(uiLiveTimesItem2)))
    }

    @Test
    fun getLiveTimesTransformFlowWithSuccessCopesWithSortingPreferenceChange() = runTest {
        whenever(preferenceRepository.isLiveTimesShowNightServicesEnabledFlow())
            .thenReturn(flowOf(false))
        whenever(preferenceRepository.isLiveTimesSortByTimeFlow())
            .thenReturn(flow {
                emit(false)
                delay(200L)
                emit(true)
            })
        val liveTimesFlow = intervalFlowOf(
            100L,
            10L,
            UiResult.InProgress,
            UiResult.Success(123L, uiStop))
        whenever(expandedServicesTracker.expandedServicesFlow)
            .thenReturn(flowOf(setOf("1")))
        whenever(transformations.filterNightServices(uiServices1, false))
            .thenReturn(uiServices1, uiServices2)
        whenever(transformations.sortServices(uiServices1, false))
            .thenReturn(uiServices1)
        whenever(transformations.sortServices(uiServices2, true))
            .thenReturn(uiServices2)
        whenever(transformations.applyExpansions(uiServices1, setOf("1")))
            .thenReturn(listOf(uiLiveTimesItem1))
        whenever(transformations.applyExpansions(uiServices2, setOf("1")))
            .thenReturn(listOf(uiLiveTimesItem2))

        val observer = transform.getLiveTimesTransformFlow(liveTimesFlow).test(this)
        advanceTimeBy(1000L)
        observer.finish()

        observer.assertValues(
            UiTransformedResult.InProgress,
            UiTransformedResult.Success(123L, listOf(uiLiveTimesItem1)),
            UiTransformedResult.Success(123L, listOf(uiLiveTimesItem2)))
    }

    @Test
    fun getLiveTimesTransformFlowWithSuccessCopesWithExpandedServicesChange() = runTest {
        whenever(preferenceRepository.isLiveTimesShowNightServicesEnabledFlow())
            .thenReturn(flowOf(false))
        whenever(preferenceRepository.isLiveTimesSortByTimeFlow())
            .thenReturn(flowOf(false))
        val liveTimesFlow = intervalFlowOf(
            100L,
            10L,
            UiResult.InProgress,
            UiResult.Success(123L, uiStop))
        whenever(expandedServicesTracker.expandedServicesFlow)
            .thenReturn(intervalFlowOf(0L, 200L, setOf("1"), setOf("1", "2")))
        whenever(transformations.filterNightServices(uiServices1, false))
            .thenReturn(uiServices1)
        whenever(transformations.sortServices(uiServices1, false))
            .thenReturn(uiServices1)
        whenever(transformations.applyExpansions(uiServices1, setOf("1")))
            .thenReturn(listOf(uiLiveTimesItem1))
        whenever(transformations.applyExpansions(uiServices1, setOf("1", "2")))
            .thenReturn(listOf(uiLiveTimesItem2))

        val observer = transform.getLiveTimesTransformFlow(liveTimesFlow).test(this)
        advanceTimeBy(1000L)
        observer.finish()

        observer.assertValues(
            UiTransformedResult.InProgress,
            UiTransformedResult.Success(123L, listOf(uiLiveTimesItem1)),
            UiTransformedResult.Success(123L, listOf(uiLiveTimesItem2)))
    }

    private fun givenPreferencesReturnsFlowWithNominalValues() {
        whenever(preferenceRepository.isLiveTimesSortByTimeFlow())
            .thenReturn(flowOf(false))
        whenever(preferenceRepository.isLiveTimesShowNightServicesEnabledFlow())
            .thenReturn(flowOf(false))
    }
}