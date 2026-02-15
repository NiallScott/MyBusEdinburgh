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
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.LiveTimes
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Service
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes.Stop
import uk.org.rivernile.android.bustracker.core.livetimes.LiveTimesRepository
import uk.org.rivernile.android.bustracker.core.livetimes.LiveTimesResult
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Instant

/**
 * Tests for [LiveTimesRetriever].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class LiveTimesRetrieverTest {

    @Mock
    private lateinit var liveTimesRepository: LiveTimesRepository
    @Mock
    private lateinit var servicesRepository: ServicesRepository
    @Mock
    private lateinit var liveTimesMapper: LiveTimesMapper

    private lateinit var retriever: LiveTimesRetriever

    @BeforeTest
    fun setUp() {
        retriever = LiveTimesRetriever(liveTimesRepository, servicesRepository, liveTimesMapper)
    }

    @Test
    fun getLiveTimesFlowWithInProgressResultDoesNotGetColours() = runTest {
        val liveTimesFlow = flowOf(LiveTimesResult.InProgress)
        whenever(liveTimesRepository.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4))
            .thenReturn(liveTimesFlow)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                LiveTimesResult.InProgress,
                null
            )
        ).thenReturn(UiResult.InProgress)

        retriever.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4).test {
            assertEquals(UiResult.InProgress, awaitItem())
            awaitComplete()
        }

        verify(servicesRepository, never())
            .getColoursForServicesFlow(anyOrNull())
    }

    @Test
    fun getLiveTimesFlowWithErrorsResultDoesNotGetColours() = runTest {
        val errorResult = LiveTimesResult.Error.NoConnectivity(123L)
        val errorUiResult = UiResult.Error(123L, ErrorType.NO_CONNECTIVITY)
        val liveTimesFlow = flowOf(LiveTimesResult.InProgress, errorResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4))
            .thenReturn(liveTimesFlow)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                LiveTimesResult.InProgress,
                null
            )
        ).thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult("123456".toNaptanStopIdentifier(), errorResult, null)
        ).thenReturn(errorUiResult)

        retriever.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4).test {
            assertEquals(UiResult.InProgress, awaitItem())
            assertEquals(errorUiResult, awaitItem())
            awaitComplete()
        }

        verify(servicesRepository, never())
            .getColoursForServicesFlow(anyOrNull())
    }

    @Test
    fun getLiveTimesFlowWithSuccessButNoStopDoesNotGetColours() = runTest {
        val successResult = LiveTimesResult.Success(
            LiveTimes(
                emptyMap(),
                Instant.fromEpochMilliseconds(123L)
            )
        )
        val errorUiResult = UiResult.Error(123L, ErrorType.NO_DATA)
        val liveTimesFlow = flowOf(LiveTimesResult.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4))
            .thenReturn(liveTimesFlow)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                LiveTimesResult.InProgress,
                null
            )
        ).thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult("123456".toNaptanStopIdentifier(), successResult, null)
        ).thenReturn(errorUiResult)

        retriever.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4).test {
            assertEquals(UiResult.InProgress, awaitItem())
            assertEquals(errorUiResult, awaitItem())
            awaitComplete()
        }

        verify(servicesRepository, never())
            .getColoursForServicesFlow(anyOrNull())
    }

    @Test
    fun getLiveTimesFlowWithSuccessButNoServicesDoesNotGetColours() = runTest {
        val successResult = LiveTimesResult.Success(
            LiveTimes(
                mapOf(
                    "123456".toNaptanStopIdentifier() to Stop(
                        "123456".toNaptanStopIdentifier(),
                        emptyList(),
                    )
                ),
                Instant.fromEpochMilliseconds(123L)
            )
        )
        val errorUiResult = UiResult.Error(123L, ErrorType.NO_DATA)
        val liveTimesFlow = flowOf(LiveTimesResult.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4))
            .thenReturn(liveTimesFlow)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                LiveTimesResult.InProgress,
                null
            )
        ).thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult("123456".toNaptanStopIdentifier(), successResult, null)
        ).thenReturn(errorUiResult)

        retriever.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4).test {
            assertEquals(UiResult.InProgress, awaitItem())
            assertEquals(errorUiResult, awaitItem())
            awaitComplete()
        }

        verify(servicesRepository, never())
            .getColoursForServicesFlow(anyOrNull())
    }

    @Test
    fun getLiveTimesFlowWithSuccessSingleServiceWithNullColours() = runTest {
        val successResult = LiveTimesResult.Success(
            LiveTimes(
                mapOf(
                    "123456".toNaptanStopIdentifier() to Stop(
                        "123456".toNaptanStopIdentifier(),
                        listOf(
                            Service(
                                service1,
                                emptyList()
                            )
                        )
                    )
                ),
                Instant.fromEpochMilliseconds(123L)
            )
        )
        val serviceColoursFlow = flowOf<Map<ServiceDescriptor, ServiceColours>?>(null)
        val successUiResult = UiResult.Success(
            123L,
            UiStop(
                "123456".toNaptanStopIdentifier(),
                listOf(
                    UiService(
                        service1,
                        null,
                        emptyList()
                    )
                )
            )
        )
        val liveTimesFlow = flowOf(LiveTimesResult.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4))
            .thenReturn(liveTimesFlow)
        whenever(servicesRepository.getColoursForServicesFlow(setOf(service1)))
            .thenReturn(serviceColoursFlow)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                LiveTimesResult.InProgress,
                null
            )
        ).thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                successResult,
                null
            )
        ).thenReturn(successUiResult)

        retriever.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4).test {
            assertEquals(UiResult.InProgress, awaitItem())
            assertEquals(successUiResult, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getLiveTimesFlowWithSuccessSingleServiceWithEmptyColours() = runTest {
        val successResult = LiveTimesResult.Success(
            LiveTimes(
                mapOf(
                    "123456".toNaptanStopIdentifier() to Stop(
                        "123456".toNaptanStopIdentifier(),
                        listOf(
                            Service(
                                service1,
                                emptyList()
                            )
                        ),
                    )
                ),
                Instant.fromEpochMilliseconds(123L)
            )
        )
        val serviceColoursFlow = flowOf<Map<ServiceDescriptor, ServiceColours>?>(emptyMap())
        val successUiResult = UiResult.Success(
            123L,
            UiStop(
                "123456".toNaptanStopIdentifier(),
                listOf(
                    UiService(
                        service1,
                        null,
                        emptyList()
                    )
                )
            )
        )
        val liveTimesFlow = flowOf(LiveTimesResult.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4))
            .thenReturn(liveTimesFlow)
        whenever(servicesRepository.getColoursForServicesFlow(setOf(service1)))
            .thenReturn(serviceColoursFlow)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                LiveTimesResult.InProgress,
                null
            )
        ).thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                successResult,
                emptyMap()))
            .thenReturn(successUiResult)

        retriever.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4).test {
            assertEquals(UiResult.InProgress, awaitItem())
            assertEquals(successUiResult, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getLiveTimesFlowWithSuccessSingleServiceGetsColours() = runTest {
        val successResult = LiveTimesResult.Success(
            LiveTimes(
                mapOf(
                    "123456".toNaptanStopIdentifier() to Stop(
                        "123456".toNaptanStopIdentifier(),
                        listOf(
                            Service(
                                service1,
                                emptyList()
                            )
                        )
                    )
                ),
                Instant.fromEpochMilliseconds(123L)
            )
        )
        val serviceColours = mapOf<ServiceDescriptor, ServiceColours>(
            service1 to ServiceColours(1, 10)
        )
        val serviceColoursFlow = flowOf(serviceColours)
        val successUiResult = UiResult.Success(
            123L,
            UiStop(
                "123456".toNaptanStopIdentifier(),
                listOf(
                    UiService(
                        service1,
                        ServiceColours(1, 10),
                        emptyList()
                    )
                )
            )
        )
        val liveTimesFlow = flowOf(LiveTimesResult.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4))
            .thenReturn(liveTimesFlow)
        whenever(servicesRepository.getColoursForServicesFlow(setOf(service1)))
            .thenReturn(serviceColoursFlow)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                LiveTimesResult.InProgress,
                null
            )
        )
            .thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                successResult,
                serviceColours
            )
        ).thenReturn(successUiResult)

        retriever.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4).test {
            assertEquals(UiResult.InProgress, awaitItem())
            assertEquals(successUiResult, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getLiveTimesFlowWithSuccessMultipleServicesGetsColours() = runTest {
        val successResult = LiveTimesResult.Success(
            LiveTimes(
                mapOf(
                    "123456".toNaptanStopIdentifier() to Stop(
                        "123456".toNaptanStopIdentifier(),
                        listOf(
                            Service(
                                service1,
                                emptyList()
                            ),
                            Service(
                                service2,
                                emptyList()
                            ),
                            Service(
                                service3,
                                emptyList()
                            )
                        )
                    )
                ),
                Instant.fromEpochMilliseconds(123L)
            )
        )
        val serviceColours = mapOf<ServiceDescriptor, ServiceColours>(
            service1 to ServiceColours(1, 10),
            service2 to ServiceColours(2, 20),
            service3 to ServiceColours(3, 30)
        )
        val serviceColoursFlow = flowOf(serviceColours)
        val successUiResult = UiResult.Success(
            123L,
            UiStop(
                "123456".toNaptanStopIdentifier(),
                listOf(
                    UiService(
                        service1,
                        ServiceColours(1, 10),
                        emptyList()
                    ),
                    UiService(
                        service2,
                        ServiceColours(2, 20),
                        emptyList()
                    ),
                    UiService(
                        service3,
                        ServiceColours(3, 30),
                        emptyList()
                    )
                )
            )
        )
        val liveTimesFlow = flowOf(LiveTimesResult.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4))
            .thenReturn(liveTimesFlow)
        whenever(servicesRepository.getColoursForServicesFlow(setOf(service1, service2, service3)))
            .thenReturn(serviceColoursFlow)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                LiveTimesResult.InProgress,
                null
            )
        ).thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                successResult,
                serviceColours
            )
        ).thenReturn(successUiResult)

        retriever.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4).test {
            assertEquals(UiResult.InProgress, awaitItem())
            assertEquals(successUiResult, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getLiveTimesFlowWithSuccessSingleServiceUpdatesColours() = runTest {
        val successResult = LiveTimesResult.Success(
            LiveTimes(
                mapOf(
                    "123456".toNaptanStopIdentifier() to Stop(
                        "123456".toNaptanStopIdentifier(),
                        listOf(
                            Service(
                                service1,
                                emptyList()
                            )
                        )
                    )
                ),
                Instant.fromEpochMilliseconds(123L)
            )
        )
        val serviceColours1 = mapOf<ServiceDescriptor, ServiceColours>(
            service1 to ServiceColours(1, 10)
        )
        val serviceColours2 = mapOf<ServiceDescriptor, ServiceColours>(
            service1 to ServiceColours(2, 20)
        )
        val serviceColoursFlow = flowOf(serviceColours1, serviceColours2)
        val successUiResult1 = UiResult.Success(
            123L,
            UiStop(
                "123456".toNaptanStopIdentifier(),
                listOf(
                    UiService(
                        service1,
                        ServiceColours(1, 10),
                        emptyList()
                    )
                )
            )
        )
        val successUiResult2 = UiResult.Success(
            123L,
            UiStop(
                "123456".toNaptanStopIdentifier(),
                listOf(
                    UiService(
                        service1,
                        ServiceColours(2, 20),
                        emptyList()
                    )
                )
            )
        )
        val liveTimesFlow = flowOf(LiveTimesResult.InProgress, successResult)
        whenever(liveTimesRepository.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4))
            .thenReturn(liveTimesFlow)
        whenever(servicesRepository.getColoursForServicesFlow(setOf(service1)))
            .thenReturn(serviceColoursFlow)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                LiveTimesResult.InProgress,
                null
            )
        ).thenReturn(UiResult.InProgress)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                successResult,
                serviceColours1
            )
        ).thenReturn(successUiResult1)
        whenever(liveTimesMapper
            .mapLiveTimesAndColoursToUiResult(
                "123456".toNaptanStopIdentifier(),
                successResult,
                serviceColours2
            )
        ).thenReturn(successUiResult2)

        retriever.getLiveTimesFlow("123456".toNaptanStopIdentifier(), 4).test {
            assertEquals(UiResult.InProgress, awaitItem())
            assertEquals(successUiResult1, awaitItem())
            assertEquals(successUiResult2, awaitItem())
            awaitComplete()
        }
    }

    private val service1 get() = FakeServiceDescriptor(
        serviceName = "1",
        operatorCode = "TEST1"
    )

    private val service2 get() = FakeServiceDescriptor(
        serviceName = "2",
        operatorCode = "TEST2"
    )

    private val service3 get() = FakeServiceDescriptor(
        serviceName = "3",
        operatorCode = "TEST3"
    )
}
