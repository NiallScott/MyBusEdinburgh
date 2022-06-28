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

package uk.org.rivernile.android.bustracker.ui.bustimes.details

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.mock
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [UiItemRetriever].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class UiItemRetrieverTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var distanceRetriever: DistanceRetriever
    @Mock
    private lateinit var servicesRetriever: ServicesRetriever
    @Mock
    private lateinit var featureRepository: FeatureRepository

    private lateinit var retriever: UiItemRetriever

    @Before
    fun setUp() {
        retriever = UiItemRetriever(
                busStopsRepository,
                distanceRetriever,
                servicesRetriever,
                featureRepository)
    }

    @Test
    fun createUiItemFlowEmitsUnknownDistanceAndNoServicesWhenStopCodeIsNull() = runTest {
        val sharedFlowCoroutineScope = createSharedFlowCoroutineScope()
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
                .thenReturn(flowOf(UiItem.Distance.Unknown))

        val observer = retriever.createUiItemFlow(
                MutableStateFlow<String?>(null),
                flowOf(PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED)),
                sharedFlowCoroutineScope)
                .test(this)
        advanceUntilIdle()
        observer.finish()
        sharedFlowCoroutineScope.cancel()

        observer.assertValues(
                listOf(
                        UiItem.Distance.Unknown,
                        UiItem.NoServices))
    }

    @Test
    fun createUiItemFlowEmitsUnknownDistanceAndNoServicesWhenStopServicesNull() = runTest {
        val sharedFlowCoroutineScope = createSharedFlowCoroutineScope()
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
                .thenReturn(flowOf(UiItem.Distance.Unknown))
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flowOf(null))
        whenever(servicesRetriever.getServicesFlow("123456"))
                .thenReturn(flowOf(null))

        val observer = retriever.createUiItemFlow(
                MutableStateFlow<String?>("123456"),
                flowOf(PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED)),
                sharedFlowCoroutineScope)
                .test(this)
        advanceUntilIdle()
        observer.finish()
        sharedFlowCoroutineScope.cancel()

        observer.assertValues(
                listOf(
                        UiItem.Distance.Unknown,
                        UiItem.NoServices))
    }

    @Test
    fun createUiItemFlowEmitsNoServicesWhenStopDetailsIsNullAndServicesEmpty() = runTest {
        val sharedFlowCoroutineScope = createSharedFlowCoroutineScope()
        val services = emptyList<UiItem.Service>()
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
                .thenReturn(flowOf(UiItem.Distance.Unknown))
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flowOf(null))
        whenever(servicesRetriever.getServicesFlow("123456"))
                .thenReturn(flowOf(services))

        val observer = retriever.createUiItemFlow(
                MutableStateFlow<String?>("123456"),
                flowOf(PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED)),
                sharedFlowCoroutineScope)
                .test(this)
        advanceUntilIdle()
        observer.finish()
        sharedFlowCoroutineScope.cancel()

        observer.assertValues(
                listOf(
                        UiItem.Distance.Unknown,
                        UiItem.NoServices))
    }

    @Test
    fun createUiItemFlowEmitsServicesWhenStopDetailsIsNullAndServicesPopulated() = runTest {
        val sharedFlowCoroutineScope = createSharedFlowCoroutineScope()
        val service1 = mock<UiItem.Service>()
        val service2 = mock<UiItem.Service>()
        val service3 = mock<UiItem.Service>()
        val services = listOf(service1, service2, service3)
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
                .thenReturn(flowOf(UiItem.Distance.Unknown))
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flowOf(null))
        whenever(servicesRetriever.getServicesFlow("123456"))
                .thenReturn(flowOf(services))

        val observer = retriever.createUiItemFlow(
                MutableStateFlow<String?>("123456"),
                flowOf(PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED)),
                sharedFlowCoroutineScope)
                .test(this)
        advanceUntilIdle()
        observer.finish()
        sharedFlowCoroutineScope.cancel()

        observer.assertValues(
                listOf(
                        UiItem.Distance.Unknown,
                        service1,
                        service2,
                        service3))
    }

    @Test
    fun createUiItemFlowDoesNotEmitMapItemWhenDoesNotHaveMapFeature() = runTest {
        val sharedFlowCoroutineScope = createSharedFlowCoroutineScope()
        val stopDetails = createStopDetails()
        givenStopMapFeatureAvailability(false)
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
                .thenReturn(flowOf(UiItem.Distance.Known(1.2f)))
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flowOf(stopDetails))
        whenever(servicesRetriever.getServicesFlow("123456"))
                .thenReturn(flowOf(null))

        val observer = retriever.createUiItemFlow(
                MutableStateFlow<String?>("123456"),
                flowOf(PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED)),
                sharedFlowCoroutineScope)
                .test(this)
        advanceUntilIdle()
        observer.finish()
        sharedFlowCoroutineScope.cancel()

        observer.assertValues(
                listOf(
                        UiItem.Distance.Known(1.2f),
                        UiItem.NoServices))
    }

    @Test
    fun createUiItemFlowEmitsMapItemWhenHasMapFeature() = runTest {
        val sharedFlowCoroutineScope = createSharedFlowCoroutineScope()
        val stopDetails = createStopDetails()
        givenStopMapFeatureAvailability(true)
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
                .thenReturn(flowOf(UiItem.Distance.Known(1.2f)))
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flowOf(stopDetails))
        whenever(servicesRetriever.getServicesFlow("123456"))
                .thenReturn(flowOf(null))

        val observer = retriever.createUiItemFlow(
                MutableStateFlow<String?>("123456"),
                flowOf(PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED)),
                sharedFlowCoroutineScope)
                .test(this)
        advanceUntilIdle()
        observer.finish()
        sharedFlowCoroutineScope.cancel()

        observer.assertValues(
                listOf(
                        UiItem.Map(
                                1.1,
                                2.2,
                                3),
                        UiItem.Distance.Known(1.2f),
                        UiItem.NoServices))
    }

    @Test
    fun createUiItemFlowWithRepresentativeExample() = runTest {
        val sharedFlowCoroutineScope = createSharedFlowCoroutineScope()
        val stopDetails = createStopDetails()
        val service1 = mock<UiItem.Service>()
        val service2 = mock<UiItem.Service>()
        val service3 = mock<UiItem.Service>()
        val services = listOf(service1, service2, service3)
        givenStopMapFeatureAvailability(true)
        whenever(distanceRetriever.createDistanceFlow(any(), any()))
                .thenReturn(flowOf(UiItem.Distance.Known(1.2f)))
        whenever(busStopsRepository.getBusStopDetailsFlow("123456"))
                .thenReturn(flowOf(stopDetails))
        whenever(servicesRetriever.getServicesFlow("123456"))
                .thenReturn(flowOf(services))

        val observer = retriever.createUiItemFlow(
                MutableStateFlow<String?>("123456"),
                flowOf(PermissionsState(
                        PermissionState.GRANTED,
                        PermissionState.GRANTED)),
                sharedFlowCoroutineScope)
                .test(this)
        advanceUntilIdle()
        observer.finish()
        sharedFlowCoroutineScope.cancel()

        observer.assertValues(
                listOf(
                        UiItem.Map(
                                1.1,
                                2.2,
                                3),
                        UiItem.Distance.Known(1.2f),
                        service1,
                        service2,
                        service3))
    }

    private fun createSharedFlowCoroutineScope() = CoroutineScope(coroutineRule.testDispatcher)

    private fun givenStopMapFeatureAvailability(isAvailable: Boolean) {
        whenever(featureRepository.hasStopMapUiFeature)
                .thenReturn(isAvailable)
    }

    private fun createStopDetails() = StopDetails(
            "123456",
            StopName(
                    "Stop name",
                    "Locality"),
            1.1,
            2.2,
            3)
}