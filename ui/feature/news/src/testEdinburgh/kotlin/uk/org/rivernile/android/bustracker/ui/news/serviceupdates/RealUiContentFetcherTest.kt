/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import app.cash.turbine.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.networking.ConnectivityRepository
import uk.org.rivernile.android.bustracker.core.networking.FakeConnectivityRepository
import uk.org.rivernile.android.bustracker.core.time.ElapsedTimeCalculator
import uk.org.rivernile.android.bustracker.core.time.ElapsedTimeMinutes
import uk.org.rivernile.android.bustracker.core.time.FakeElapsedTimeCalculator
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [RealUiContentFetcher].
 *
 * @author Niall Scott
 */
class RealUiContentFetcherTest {

    @Test
    fun diversionsContentFlowEmitsInProgress() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesDisplayFetcher = FakeServiceUpdatesDisplayFetcher(
                onDiversionsDisplayFlow = {
                    flowOf(ServiceUpdatesDisplay.InProgress)
                }
            ),
            connectivityRepository = createAlwaysConnectedConnectivityRepository()
        )

        fetcher.diversionsContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun diversionsContentFlowEmitsPopulated() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesDisplayFetcher = FakeServiceUpdatesDisplayFetcher(
                onDiversionsDisplayFlow = {
                    flowOf(
                        ServiceUpdatesDisplay.Populated(
                            isRefreshing = false,
                            items = emptyList(),
                            error = null,
                            successLoadTimeMillis = 1000L,
                            lastLoadTimeMillis = 1001L
                        )
                    )
                }
            ),
            connectivityRepository = createAlwaysConnectedConnectivityRepository(),
            elapsedTimeCalculator = FakeElapsedTimeCalculator(
                onGetElapsedTimeMinutesFlow = {
                    assertEquals(1000L, it)
                    flowOf(
                        ElapsedTimeMinutes.Now,
                        ElapsedTimeMinutes.Minutes(1),
                        ElapsedTimeMinutes.Minutes(1),
                        ElapsedTimeMinutes.Minutes(2),
                        ElapsedTimeMinutes.Minutes(2),
                        ElapsedTimeMinutes.Minutes(3),
                        ElapsedTimeMinutes.Minutes(3),
                        ElapsedTimeMinutes.MoreThanOneHour
                    )
                }
            )
        )

        fetcher.diversionsContentFlow.test {
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Now,
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(1),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(3),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.MoreThanOneHour,
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun diversionsContentFlowPopulatedEmitsNewValuesWhenInternetConnectivityChanged() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesDisplayFetcher = FakeServiceUpdatesDisplayFetcher(
                onDiversionsDisplayFlow = {
                    flowOf(
                        ServiceUpdatesDisplay.Populated(
                            isRefreshing = false,
                            items = emptyList(),
                            error = null,
                            successLoadTimeMillis = 1000L,
                            lastLoadTimeMillis = 1001L
                        )
                    )
                }
            ),
            connectivityRepository = FakeConnectivityRepository(
                onHasInternetConnectivityFlow = {
                    intervalFlowOf(
                        initialDelay = 0L,
                        interval = 10L,
                        true, false, false, true
                    )
                }
            ),
            elapsedTimeCalculator = FakeElapsedTimeCalculator(
                onGetElapsedTimeMinutesFlow = {
                    assertEquals(1000L, it)
                    flowOf(ElapsedTimeMinutes.Minutes(2))
                }
            )
        )

        fetcher.diversionsContentFlow.test {
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = false,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun diversionsContentFlowPopulatedEmitsNewValuesWhenLastErrorTimestampShownChanged() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesDisplayFetcher = FakeServiceUpdatesDisplayFetcher(
                onDiversionsDisplayFlow = {
                    flowOf(
                        ServiceUpdatesDisplay.Populated(
                            isRefreshing = false,
                            items = emptyList(),
                            error = UiError.NO_CONNECTIVITY,
                            successLoadTimeMillis = 1000L,
                            lastLoadTimeMillis = 1001L
                        )
                    )
                }
            ),
            connectivityRepository = createAlwaysConnectedConnectivityRepository(),
            serviceUpdatesErrorTracker = FakeServiceUpdatesErrorTracker(
                onLastErrorTimestampShownFlow = {
                    intervalFlowOf(
                        initialDelay = 0L,
                        interval = 10L,
                        1000L, 1001L, 1002L
                    )
                }
            ),
            elapsedTimeCalculator = FakeElapsedTimeCalculator(
                onGetElapsedTimeMinutesFlow = {
                    assertEquals(1000L, it)
                    flowOf(ElapsedTimeMinutes.Minutes(2))
                }
            )
        )

        fetcher.diversionsContentFlow.test {
            assertEquals(
                createUiContentPopulated(
                    error = UiError.NO_CONNECTIVITY,
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    error = null,
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun diversionsContentFlowEmitsError() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesDisplayFetcher = FakeServiceUpdatesDisplayFetcher(
                onDiversionsDisplayFlow = {
                    flowOf(ServiceUpdatesDisplay.Error(UiError.SERVER))
                }
            ),
            connectivityRepository = createAlwaysConnectedConnectivityRepository()
        )

        fetcher.diversionsContentFlow.test {
            assertEquals(UiContent.Error(UiError.SERVER), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun diversionsContentFlowWithRepresentativeExample() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesDisplayFetcher = FakeServiceUpdatesDisplayFetcher(
                onDiversionsDisplayFlow = {
                    flow {
                        emit(ServiceUpdatesDisplay.InProgress)
                        emit(ServiceUpdatesDisplay.Error(error = UiError.SERVER))
                        emit(ServiceUpdatesDisplay.InProgress)
                        emit(
                            ServiceUpdatesDisplay.Populated(
                                isRefreshing = false,
                                items = emptyList(),
                                error = null,
                                successLoadTimeMillis = 1000L,
                                lastLoadTimeMillis = 1001L
                            )
                        )
                    }
                }
            ),
            connectivityRepository = createAlwaysConnectedConnectivityRepository(),
            elapsedTimeCalculator = FakeElapsedTimeCalculator(
                onGetElapsedTimeMinutesFlow = {
                    assertEquals(1000L, it)
                    flowOf(
                        ElapsedTimeMinutes.Minutes(1),
                        ElapsedTimeMinutes.Minutes(2),
                        ElapsedTimeMinutes.Minutes(3),
                    )
                }
            )
        )

        fetcher.diversionsContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(UiContent.Error(error = UiError.SERVER), awaitItem())
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(1),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(3),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun incidentsContentFlowEmitsInProgress() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesDisplayFetcher = FakeServiceUpdatesDisplayFetcher(
                onIncidentsDisplayFlow = {
                    flowOf(ServiceUpdatesDisplay.InProgress)
                }
            ),
            connectivityRepository = createAlwaysConnectedConnectivityRepository()
        )

        fetcher.incidentsContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun incidentsContentFlowEmitsPopulated() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesDisplayFetcher = FakeServiceUpdatesDisplayFetcher(
                onIncidentsDisplayFlow = {
                    flowOf(
                        ServiceUpdatesDisplay.Populated(
                            isRefreshing = false,
                            items = emptyList(),
                            error = null,
                            successLoadTimeMillis = 1000L,
                            lastLoadTimeMillis = 1001L
                        )
                    )
                }
            ),
            connectivityRepository = createAlwaysConnectedConnectivityRepository(),
            elapsedTimeCalculator = FakeElapsedTimeCalculator(
                onGetElapsedTimeMinutesFlow = {
                    assertEquals(1000L, it)
                    flowOf(
                        ElapsedTimeMinutes.Now,
                        ElapsedTimeMinutes.Minutes(1),
                        ElapsedTimeMinutes.Minutes(1),
                        ElapsedTimeMinutes.Minutes(2),
                        ElapsedTimeMinutes.Minutes(2),
                        ElapsedTimeMinutes.Minutes(3),
                        ElapsedTimeMinutes.Minutes(3),
                        ElapsedTimeMinutes.MoreThanOneHour
                    )
                }
            )
        )

        fetcher.incidentsContentFlow.test {
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Now,
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(1),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(3),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.MoreThanOneHour,
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun incidentsContentFlowPopulatedEmitsNewValuesWhenInternetConnectivityChanged() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesDisplayFetcher = FakeServiceUpdatesDisplayFetcher(
                onIncidentsDisplayFlow = {
                    flowOf(
                        ServiceUpdatesDisplay.Populated(
                            isRefreshing = false,
                            items = emptyList(),
                            error = null,
                            successLoadTimeMillis = 1000L,
                            lastLoadTimeMillis = 1001L
                        )
                    )
                }
            ),
            connectivityRepository = FakeConnectivityRepository(
                onHasInternetConnectivityFlow = {
                    intervalFlowOf(
                        initialDelay = 0L,
                        interval = 10L,
                        true, false, false, true
                    )
                }
            ),
            elapsedTimeCalculator = FakeElapsedTimeCalculator(
                onGetElapsedTimeMinutesFlow = {
                    assertEquals(1000L, it)
                    flowOf(ElapsedTimeMinutes.Minutes(2))
                }
            )
        )

        fetcher.incidentsContentFlow.test {
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = false,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun incidentsContentFlowPopulatedEmitsNewValuesWhenLastErrorTimestampShownChanged() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesDisplayFetcher = FakeServiceUpdatesDisplayFetcher(
                onIncidentsDisplayFlow = {
                    flowOf(
                        ServiceUpdatesDisplay.Populated(
                            isRefreshing = false,
                            items = emptyList(),
                            error = UiError.NO_CONNECTIVITY,
                            successLoadTimeMillis = 1000L,
                            lastLoadTimeMillis = 1001L
                        )
                    )
                }
            ),
            connectivityRepository = createAlwaysConnectedConnectivityRepository(),
            serviceUpdatesErrorTracker = FakeServiceUpdatesErrorTracker(
                onLastErrorTimestampShownFlow = {
                    intervalFlowOf(
                        initialDelay = 0L,
                        interval = 10L,
                        1000L, 1001L, 1002L
                    )
                }
            ),
            elapsedTimeCalculator = FakeElapsedTimeCalculator(
                onGetElapsedTimeMinutesFlow = {
                    assertEquals(1000L, it)
                    flowOf(ElapsedTimeMinutes.Minutes(2))
                }
            )
        )

        fetcher.incidentsContentFlow.test {
            assertEquals(
                createUiContentPopulated(
                    error = UiError.NO_CONNECTIVITY,
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    error = null,
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun incidentsContentFlowEmitsError() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesDisplayFetcher = FakeServiceUpdatesDisplayFetcher(
                onIncidentsDisplayFlow = {
                    flowOf(ServiceUpdatesDisplay.Error(UiError.SERVER))
                }
            ),
            connectivityRepository = createAlwaysConnectedConnectivityRepository()
        )

        fetcher.incidentsContentFlow.test {
            assertEquals(UiContent.Error(UiError.SERVER), awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun incidentsContentFlowWithRepresentativeExample() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesDisplayFetcher = FakeServiceUpdatesDisplayFetcher(
                onIncidentsDisplayFlow = {
                    flow {
                        emit(ServiceUpdatesDisplay.InProgress)
                        emit(ServiceUpdatesDisplay.Error(error = UiError.SERVER))
                        emit(ServiceUpdatesDisplay.InProgress)
                        emit(
                            ServiceUpdatesDisplay.Populated(
                                isRefreshing = false,
                                items = emptyList(),
                                error = null,
                                successLoadTimeMillis = 1000L,
                                lastLoadTimeMillis = 1001L
                            )
                        )
                    }
                }
            ),
            connectivityRepository = createAlwaysConnectedConnectivityRepository(),
            elapsedTimeCalculator = FakeElapsedTimeCalculator(
                onGetElapsedTimeMinutesFlow = {
                    assertEquals(1000L, it)
                    flowOf(
                        ElapsedTimeMinutes.Minutes(1),
                        ElapsedTimeMinutes.Minutes(2),
                        ElapsedTimeMinutes.Minutes(3),
                    )
                }
            )
        )

        fetcher.incidentsContentFlow.test {
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(UiContent.Error(error = UiError.SERVER), awaitItem())
            assertEquals(UiContent.InProgress, awaitItem())
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(1),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(2),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            assertEquals(
                createUiContentPopulated(
                    hasInternetConnectivity = true,
                    lastRefreshTime = UiLastRefreshed.Minutes(3),
                    loadTimeMillis = 1001L
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun closeCallsCloseOnServiceUpdatesDisplayFetcher() {
        var closeInvocationCount = 0
        val fetcher = createFetcher(
            serviceUpdatesDisplayFetcher = FakeServiceUpdatesDisplayFetcher(
                onClose = { closeInvocationCount++ }
            )
        )

        fetcher.close()

        assertEquals(1, closeInvocationCount)
    }

    private fun createFetcher(
        serviceUpdatesDisplayFetcher: ServiceUpdatesDisplayFetcher =
            FakeServiceUpdatesDisplayFetcher(),
        connectivityRepository: ConnectivityRepository = FakeConnectivityRepository(),
        serviceUpdatesErrorTracker: ServiceUpdatesErrorTracker = FakeServiceUpdatesErrorTracker(
            onLastErrorTimestampShownFlow = { flowOf(0L) }
        ),
        elapsedTimeCalculator: ElapsedTimeCalculator = FakeElapsedTimeCalculator()
    ): RealUiContentFetcher {
        return RealUiContentFetcher(
            serviceUpdatesDisplayFetcher = serviceUpdatesDisplayFetcher,
            connectivityRepository = connectivityRepository,
            serviceUpdatesErrorTracker = serviceUpdatesErrorTracker,
            elapsedTimeCalculator = elapsedTimeCalculator
        )
    }

    private fun createAlwaysConnectedConnectivityRepository(): ConnectivityRepository {
        return FakeConnectivityRepository(
            onHasInternetConnectivityFlow = {
                flowOf(true)
            }
        )
    }

    @Suppress("SameParameterValue")
    private fun <T : UiServiceUpdate> createUiContentPopulated(
        error: UiError? = null,
        hasInternetConnectivity: Boolean,
        lastRefreshTime: UiLastRefreshed,
        loadTimeMillis: Long
    ): UiContent.Populated<T> {
        return UiContent.Populated(
            isRefreshing = false,
            items = persistentListOf(),
            error = error,
            hasInternetConnectivity = hasInternetConnectivity,
            lastRefreshTime = lastRefreshTime,
            loadTimeMillis = loadTimeMillis
        )
    }
}