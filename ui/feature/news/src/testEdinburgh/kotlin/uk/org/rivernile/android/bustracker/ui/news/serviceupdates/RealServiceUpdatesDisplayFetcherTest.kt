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
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import uk.org.rivernile.android.bustracker.core.services.FakeServicesRepository
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.core.updates.IncidentServiceUpdate
import uk.org.rivernile.android.bustracker.core.updates.PlannedServiceUpdate
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdatesResult
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversion
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncident
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.test.fail

/**
 * Tests for [RealServiceUpdatesDisplayFetcher].
 *
 * @author Niall Scott
 */
class RealServiceUpdatesDisplayFetcherTest {

    @Test
    fun diversionsDisplayFlowInitiallyEmitsInProgress() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesFetcher = FakeServiceUpdatesFetcher(
                onServiceUpdatesFlow = { emptyFlow() }
            ),
            servicesRepository = createServicesRepositoryWithDefaultColours(),
            serviceUpdatesDisplayCalculator = FakeServiceUpdatesDisplayCalculator(
                onCalculateServiceUpdatesDisplayForDiversions = { _, result ->
                    assertIs<UiServiceUpdatesResult.InProgress>(result)
                    ServiceUpdatesDisplay.InProgress
                }
            )
        )

        fetcher.diversionsDisplayFlow.test {
            assertEquals(ServiceUpdatesDisplay.InProgress, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun diversionsDisplayFlowHandlesUpstreamSuccess() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesFetcher = FakeServiceUpdatesFetcher(
                onServiceUpdatesFlow = {
                    flowOf(
                        ServiceUpdatesResult.InProgress,
                        ServiceUpdatesResult.Success(
                            serviceUpdates = listOf(
                                PlannedServiceUpdate(
                                    id = "1",
                                    lastUpdated = Instant.fromEpochMilliseconds(123L),
                                    title = "Title",
                                    summary = "Summary",
                                    affectedServices = setOf("1", "2", "3"),
                                    url = null
                                )
                            ),
                            loadTimeMillis = 123L
                        )
                    )
                }
            ),
            servicesRepository = createServicesRepositoryWithDefaultColours(),
            serviceUpdatesDisplayCalculator = FakeServiceUpdatesDisplayCalculator(
                onCalculateServiceUpdatesDisplayForDiversions = { _, result ->
                    when (result) {
                        is UiServiceUpdatesResult.InProgress -> ServiceUpdatesDisplay.InProgress
                        is UiServiceUpdatesResult.Success -> ServiceUpdatesDisplay.Populated(
                            isRefreshing = false,
                            items = listOf(
                                UiDiversion(
                                    id = "1",
                                    lastUpdated = Instant.fromEpochMilliseconds(123L),
                                    title = "Title",
                                    summary = "Summary",
                                    affectedServices = listOf(
                                        UiServiceName(
                                            serviceName = "1",
                                            colours = UiServiceColours(
                                                backgroundColour = 1,
                                                textColour = 2
                                            )
                                        ),
                                        UiServiceName(
                                            serviceName = "2",
                                            colours = UiServiceColours(
                                                backgroundColour = 3,
                                                textColour = 4
                                            )
                                        ),
                                        UiServiceName(
                                            serviceName = "3",
                                            colours = null
                                        )
                                    ),
                                    moreDetails = null
                                )
                            ),
                            error = null,
                            loadTimeMillis = 123L
                        )
                        else -> fail()
                    }
                }
            )
        )

        fetcher.diversionsDisplayFlow.test {
            assertEquals(ServiceUpdatesDisplay.InProgress, awaitItem())
            assertEquals(
                ServiceUpdatesDisplay.Populated(
                    isRefreshing = false,
                    items = listOf(
                        UiDiversion(
                            id = "1",
                            lastUpdated = Instant.fromEpochMilliseconds(123L),
                            title = "Title",
                            summary = "Summary",
                            affectedServices = listOf(
                                UiServiceName(
                                    serviceName = "1",
                                    colours = UiServiceColours(
                                        backgroundColour = 1,
                                        textColour = 2
                                    )
                                ),
                                UiServiceName(
                                    serviceName = "2",
                                    colours = UiServiceColours(
                                        backgroundColour = 3,
                                        textColour = 4
                                    )
                                ),
                                UiServiceName(
                                    serviceName = "3",
                                    colours = null
                                )
                            ),
                            moreDetails = null
                        )
                    ),
                    error = null,
                    loadTimeMillis = 123L
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun diversionsDisplayFlowHandlesUpstreamError() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesFetcher = FakeServiceUpdatesFetcher(
                onServiceUpdatesFlow = {
                    flowOf(
                        ServiceUpdatesResult.InProgress,
                        ServiceUpdatesResult.Error.NoConnectivity
                    )
                }
            ),
            servicesRepository = createServicesRepositoryWithDefaultColours(),
            serviceUpdatesDisplayCalculator = FakeServiceUpdatesDisplayCalculator(
                onCalculateServiceUpdatesDisplayForDiversions = { _, result ->
                    when (result) {
                        is UiServiceUpdatesResult.InProgress -> ServiceUpdatesDisplay.InProgress
                        is UiServiceUpdatesResult.Error ->
                            ServiceUpdatesDisplay.Error(error = UiError.NO_CONNECTIVITY)
                        else -> fail()
                    }
                }
            )
        )

        fetcher.diversionsDisplayFlow.test {
            assertEquals(ServiceUpdatesDisplay.InProgress, awaitItem())
            assertEquals(
                ServiceUpdatesDisplay.Error(error = UiError.NO_CONNECTIVITY),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun incidentsDisplayFlowInitiallyEmitsInProgress() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesFetcher = FakeServiceUpdatesFetcher(
                onServiceUpdatesFlow = { emptyFlow() }
            ),
            servicesRepository = createServicesRepositoryWithDefaultColours(),
            serviceUpdatesDisplayCalculator = FakeServiceUpdatesDisplayCalculator(
                onCalculateServiceUpdatesDisplayForIncidents = { _, result ->
                    assertIs<UiServiceUpdatesResult.InProgress>(result)
                    ServiceUpdatesDisplay.InProgress
                }
            )
        )

        fetcher.incidentsDisplayFlow.test {
            assertEquals(ServiceUpdatesDisplay.InProgress, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun incidentsDisplayFlowHandlesUpstreamSuccess() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesFetcher = FakeServiceUpdatesFetcher(
                onServiceUpdatesFlow = {
                    flowOf(
                        ServiceUpdatesResult.InProgress,
                        ServiceUpdatesResult.Success(
                            serviceUpdates = listOf(
                                IncidentServiceUpdate(
                                    id = "1",
                                    lastUpdated = Instant.fromEpochMilliseconds(123L),
                                    title = "Title",
                                    summary = "Summary",
                                    affectedServices = setOf("1", "2", "3"),
                                    url = null
                                )
                            ),
                            loadTimeMillis = 123L
                        )
                    )
                }
            ),
            servicesRepository = createServicesRepositoryWithDefaultColours(),
            serviceUpdatesDisplayCalculator = FakeServiceUpdatesDisplayCalculator(
                onCalculateServiceUpdatesDisplayForIncidents = { _, result ->
                    when (result) {
                        is UiServiceUpdatesResult.InProgress -> ServiceUpdatesDisplay.InProgress
                        is UiServiceUpdatesResult.Success -> ServiceUpdatesDisplay.Populated(
                            isRefreshing = false,
                            items = listOf(
                                UiIncident(
                                    id = "1",
                                    lastUpdated = Instant.fromEpochMilliseconds(123L),
                                    title = "Title",
                                    summary = "Summary",
                                    affectedServices = listOf(
                                        UiServiceName(
                                            serviceName = "1",
                                            colours = UiServiceColours(
                                                backgroundColour = 1,
                                                textColour = 2
                                            )
                                        ),
                                        UiServiceName(
                                            serviceName = "2",
                                            colours = UiServiceColours(
                                                backgroundColour = 3,
                                                textColour = 4
                                            )
                                        ),
                                        UiServiceName(
                                            serviceName = "3",
                                            colours = null
                                        )
                                    ),
                                    moreDetails = null
                                )
                            ),
                            error = null,
                            loadTimeMillis = 123L
                        )
                        else -> fail()
                    }
                }
            )
        )

        fetcher.incidentsDisplayFlow.test {
            assertEquals(ServiceUpdatesDisplay.InProgress, awaitItem())
            assertEquals(
                ServiceUpdatesDisplay.Populated(
                    isRefreshing = false,
                    items = listOf(
                        UiIncident(
                            id = "1",
                            lastUpdated = Instant.fromEpochMilliseconds(123L),
                            title = "Title",
                            summary = "Summary",
                            affectedServices = listOf(
                                UiServiceName(
                                    serviceName = "1",
                                    colours = UiServiceColours(
                                        backgroundColour = 1,
                                        textColour = 2
                                    )
                                ),
                                UiServiceName(
                                    serviceName = "2",
                                    colours = UiServiceColours(
                                        backgroundColour = 3,
                                        textColour = 4
                                    )
                                ),
                                UiServiceName(
                                    serviceName = "3",
                                    colours = null
                                )
                            ),
                            moreDetails = null
                        )
                    ),
                    error = null,
                    loadTimeMillis = 123L
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun incidentsDisplayFlowHandlesUpstreamError() = runTest {
        val fetcher = createFetcher(
            serviceUpdatesFetcher = FakeServiceUpdatesFetcher(
                onServiceUpdatesFlow = {
                    flowOf(
                        ServiceUpdatesResult.InProgress,
                        ServiceUpdatesResult.Error.NoConnectivity
                    )
                }
            ),
            servicesRepository = createServicesRepositoryWithDefaultColours(),
            serviceUpdatesDisplayCalculator = FakeServiceUpdatesDisplayCalculator(
                onCalculateServiceUpdatesDisplayForIncidents = { _, result ->
                    when (result) {
                        is UiServiceUpdatesResult.InProgress -> ServiceUpdatesDisplay.InProgress
                        is UiServiceUpdatesResult.Error ->
                            ServiceUpdatesDisplay.Error(error = UiError.NO_CONNECTIVITY)
                        else -> fail()
                    }
                }
            )
        )

        fetcher.incidentsDisplayFlow.test {
            assertEquals(ServiceUpdatesDisplay.InProgress, awaitItem())
            assertEquals(
                ServiceUpdatesDisplay.Error(error = UiError.NO_CONNECTIVITY),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun refreshCallsRefreshOnServiceUpdatesFetcher() = runTest {
        var refreshInvocationCount = 0
        val fetcher = createFetcher(
            serviceUpdatesFetcher = FakeServiceUpdatesFetcher(
                onRefresh = { refreshInvocationCount++ }
            ),
            servicesRepository = createServicesRepositoryWithNullColours()
        )

        fetcher.refresh()

        assertEquals(1, refreshInvocationCount)
    }

    @Test
    fun closeCallsCloseOnServiceUpdatesFetcher() = runTest {
        var closeInvocationCount = 0
        val fetcher = createFetcher(
            serviceUpdatesFetcher = FakeServiceUpdatesFetcher(
                onClose = { closeInvocationCount++ }
            ),
            servicesRepository = createServicesRepositoryWithNullColours()
        )

        fetcher.close()

        assertEquals(1, closeInvocationCount)
    }

    private fun TestScope.createFetcher(
        serviceUpdatesFetcher: ServiceUpdatesFetcher = FakeServiceUpdatesFetcher(),
        servicesRepository: ServicesRepository = FakeServicesRepository(),
        serviceNamesComparator: Comparator<String> = naturalOrder(),
        serviceUpdatesDisplayCalculator: ServiceUpdatesDisplayCalculator =
            FakeServiceUpdatesDisplayCalculator()
    ): RealServiceUpdatesDisplayFetcher {
        return RealServiceUpdatesDisplayFetcher(
            serviceUpdatesFetcher = serviceUpdatesFetcher,
            servicesRepository = servicesRepository,
            serviceNamesComparator = serviceNamesComparator,
            serviceUpdatesDisplayCalculator = serviceUpdatesDisplayCalculator,
            viewModelCoroutineScope = backgroundScope
        )
    }

    private fun createServicesRepositoryWithNullColours(): ServicesRepository {
        return FakeServicesRepository(
            onGetColoursForServicesFlow = { flowOf(null) }
        )
    }

    private fun createServicesRepositoryWithDefaultColours(): ServicesRepository {
        return FakeServicesRepository(
            onGetColoursForServicesFlow = {
                flowOf(
                    mapOf(
                        "1" to ServiceColours(1, 2),
                        "2" to ServiceColours(3, 4)
                    )
                )
            }
        )
    }
}