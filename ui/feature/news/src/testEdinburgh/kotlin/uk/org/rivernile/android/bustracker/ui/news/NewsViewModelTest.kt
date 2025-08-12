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

@file:OptIn(ExperimentalTime::class)

package uk.org.rivernile.android.bustracker.ui.news

import app.cash.turbine.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.FakeServiceUpdatesErrorTracker
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.FakeUiContentFetcher
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.ServiceUpdatesErrorTracker
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiContent
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiContentFetcher
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiLastRefreshed
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiMoreDetails
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.DiversionsViewModelState
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.FakeDiversionsViewModelState
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversion
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversionAction
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversionsState
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.FakeIncidentsViewModelState
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.IncidentsViewModelState
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncident
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncidentAction
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncidentsState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Tests for [NewsViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NewsViewModelTest {

    @Test
    fun uiStateFlowEmitsInProgressStatesByDefault() = runTest {
        val viewModel = createViewModel(
            contentFetcher = FakeUiContentFetcher(
                onDiversionsContentFlow = { emptyFlow() },
                onIncidentsContentFlow = { emptyFlow() }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsWithRefreshingWhenDiversionIsStillInProgress() = runTest {
        val incidentsContent = UiContent.Populated(
            isRefreshing = false,
            items = persistentListOf(
                UiIncident(
                    id = "1",
                    lastUpdated = Instant.fromEpochMilliseconds(123L),
                    title = "Title",
                    summary = "Summary",
                    affectedServices = null,
                    moreDetails = null
                )
            ),
            error = null,
            hasInternetConnectivity = true,
            lastRefreshTime = UiLastRefreshed.Now,
            loadTimeMillis = 123L
        )
        val viewModel = createViewModel(
            contentFetcher = FakeUiContentFetcher(
                onDiversionsContentFlow = { flowOf(UiContent.InProgress) },
                onIncidentsContentFlow = { flowOf(incidentsContent) }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    incidentsState = UiIncidentsState(
                        content = incidentsContent
                    ),
                    tabBadges = UiTabBadges(
                        incidentsCount = 1
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsWithRefreshingWhenIncidentsIsStillInProgress() = runTest {
        val diversionsContent = UiContent.Populated(
            isRefreshing = false,
            items = persistentListOf(
                UiDiversion(
                    id = "1",
                    lastUpdated = Instant.fromEpochMilliseconds(123L),
                    title = "Title",
                    summary = "Summary",
                    affectedServices = null,
                    moreDetails = null
                )
            ),
            error = null,
            hasInternetConnectivity = true,
            lastRefreshTime = UiLastRefreshed.Now,
            loadTimeMillis = 123L
        )
        val viewModel = createViewModel(
            contentFetcher = FakeUiContentFetcher(
                onDiversionsContentFlow = { flowOf(diversionsContent) },
                onIncidentsContentFlow = { flowOf(UiContent.InProgress) }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    diversionsState = UiDiversionsState(
                        content = diversionsContent
                    ),
                    tabBadges = UiTabBadges(
                        diversionsCount = 1
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsExpectedStateWithCorrectCountsAndActions() = runTest {
        val diversionsContent = UiContent.Populated(
            isRefreshing = false,
            items = persistentListOf(
                UiDiversion(
                    id = "1",
                    lastUpdated = Instant.fromEpochMilliseconds(123L),
                    title = "Title 1",
                    summary = "Summary 1",
                    affectedServices = null,
                    moreDetails = null
                )
            ),
            error = null,
            hasInternetConnectivity = true,
            lastRefreshTime = UiLastRefreshed.Now,
            loadTimeMillis = 123L
        )
        val incidentsContent = UiContent.Populated(
            isRefreshing = false,
            items = persistentListOf(
                UiIncident(
                    id = "2",
                    lastUpdated = Instant.fromEpochMilliseconds(456L),
                    title = "Title 2",
                    summary = "Summary 2",
                    affectedServices = null,
                    moreDetails = null
                ),
                UiIncident(
                    id = "3",
                    lastUpdated = Instant.fromEpochMilliseconds(789L),
                    title = "Title 3",
                    summary = "Summary 3",
                    affectedServices = null,
                    moreDetails = null
                )
            ),
            error = null,
            hasInternetConnectivity = true,
            lastRefreshTime = UiLastRefreshed.Minutes(minutes = 5),
            loadTimeMillis = 123L
        )
        val viewModel = createViewModel(
            incidentsViewModelState = FakeIncidentsViewModelState(
                onActionFlow = { flowOf(UiIncidentAction.ShowUrl(url = "https://url.one")) }
            ),
            diversionsViewModelState = FakeDiversionsViewModelState(
                onActionFlow = { flowOf(UiDiversionAction.ShowUrl(url = "https://url.two")) }
            ),
            contentFetcher = FakeUiContentFetcher(
                onIncidentsContentFlow = { flowOf(incidentsContent) },
                onDiversionsContentFlow = { flowOf(diversionsContent) }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    incidentsState = UiIncidentsState(
                        content = incidentsContent,
                        action = UiIncidentAction.ShowUrl(url = "https://url.one")
                    ),
                    diversionsState = UiDiversionsState(
                        content = diversionsContent,
                        action = UiDiversionAction.ShowUrl(url = "https://url.two")
                    ),
                    actionButtons = UiActionButtons(
                        refresh = UiActionButton.Refresh(
                            isEnabled = true,
                            isRefreshing = false
                        )
                    ),
                    tabBadges = UiTabBadges(
                        incidentsCount = 2,
                        diversionsCount = 1
                    )
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onRefreshWithNoRefreshablesDoesNotCauseException() = runTest {
        val viewModel = createViewModel(
            refreshables = emptySet()
        )

        viewModel.onRefresh()
    }

    @Test
    fun onRefreshWithSingleRefreshableCausesRefresh() = runTest {
        var refreshCounter = 0
        val viewModel = createViewModel(
            refreshables = setOf(
                FakeRefreshable(
                    onRefresh = { refreshCounter++ }
                )
            )
        )

        viewModel.onRefresh()

        assertEquals(1, refreshCounter)
    }

    @Test
    fun onRefreshWithMultipleRefreshablesCausesRefresh() = runTest {
        val refreshCounters = Array(3) { 0 }
        val viewModel = createViewModel(
            refreshables = setOf(
                FakeRefreshable(
                    onRefresh = { refreshCounters[0]++ }
                ),
                FakeRefreshable(
                    onRefresh = { refreshCounters[1]++ }
                ),
                FakeRefreshable(
                    onRefresh = { refreshCounters[2]++ }
                )
            )
        )

        viewModel.onRefresh()
        viewModel.onRefresh()

        assertEquals(2, refreshCounters[0])
        assertEquals(2, refreshCounters[1])
        assertEquals(2, refreshCounters[2])
    }

    @Test
    fun onIncidentMoreDetailsClickedPerformsNoActionWhenMoreDetailsIsNull() = runTest {
        val viewModel = createViewModel(
            incidentsViewModelState = FakeIncidentsViewModelState(
                onActionFlow = { flowOf(null) },
                onSetAction = { fail("Not expecting the setter to be called.") }
            )
        )

        viewModel.onIncidentMoreDetailsClicked(
            item = UiIncident(
                id = "1",
                lastUpdated = Instant.fromEpochMilliseconds(123L),
                title = "Title",
                summary = "Summary",
                affectedServices = null,
                moreDetails = null
            )
        )
    }

    @Test
    fun onIncidentMoreDetailsClickedSetsUrlWhenMoreDetailsIsNotNull() = runTest {
        val actions = mutableListOf<UiIncidentAction?>()
        val viewModel = createViewModel(
            incidentsViewModelState = FakeIncidentsViewModelState(
                onActionFlow = { flowOf(null) },
                onSetAction = { actions += it }
            )
        )
        val expected = listOf<UiIncidentAction?>(
            UiIncidentAction.ShowUrl(url = "https://example.url/test")
        )

        viewModel.onIncidentMoreDetailsClicked(
            item = UiIncident(
                id = "1",
                lastUpdated = Instant.fromEpochMilliseconds(123L),
                title = "Title",
                summary = "Summary",
                affectedServices = null,
                moreDetails = UiMoreDetails(url = "https://example.url/test")
            )
        )

        assertEquals(expected, actions)
    }

    @Test
    fun onDiversionMoreDetailsClickedPerformsNoActionWhenMoreDetailsIsNull() = runTest {
        val viewModel = createViewModel(
            diversionsViewModelState = FakeDiversionsViewModelState(
                onActionFlow = { flowOf(null) },
                onSetAction = { fail("Not expecting the setter to be called.") }
            )
        )

        viewModel.onDiversionMoreDetailsClicked(
            item = UiDiversion(
                id = "1",
                lastUpdated = Instant.fromEpochMilliseconds(123L),
                title = "Title",
                summary = "Summary",
                affectedServices = null,
                moreDetails = null
            )
        )
    }

    @Test
    fun onDiversionMoreDetailsClickedSetsUrlWhenMoreDetailsIsNotNull() = runTest {
        val actions = mutableListOf<UiDiversionAction?>()
        val viewModel = createViewModel(
            diversionsViewModelState = FakeDiversionsViewModelState(
                onActionFlow = { flowOf(null) },
                onSetAction = { actions += it }
            )
        )
        val expected = listOf<UiDiversionAction?>(
            UiDiversionAction.ShowUrl(url = "https://example.url/test")
        )

        viewModel.onDiversionMoreDetailsClicked(
            item = UiDiversion(
                id = "1",
                lastUpdated = Instant.fromEpochMilliseconds(123L),
                title = "Title",
                summary = "Summary",
                affectedServices = null,
                moreDetails = UiMoreDetails(url = "https://example.url/test")
            )
        )

        assertEquals(expected, actions)
    }

    @Test
    fun onIncidentActionLaunchedSetsTheActionAsNull() = runTest {
        val actions = mutableListOf<UiIncidentAction?>()
        val viewModel = createViewModel(
            incidentsViewModelState = FakeIncidentsViewModelState(
                onActionFlow = { flowOf(null) },
                onSetAction = { actions += it }
            )
        )
        val expected = listOf<UiIncidentAction?>(null)

        viewModel.onIncidentActionLaunched()

        assertEquals(expected, actions)
    }

    @Test
    fun onDiversionActionLaunchedSetsTheActionAsNull() = runTest {
        val actions = mutableListOf<UiDiversionAction?>()
        val viewModel = createViewModel(
            diversionsViewModelState = FakeDiversionsViewModelState(
                onActionFlow = { flowOf(null) },
                onSetAction = { actions += it }
            )
        )
        val expected = listOf<UiDiversionAction?>(null)

        viewModel.onDiversionActionLaunched()

        assertEquals(expected, actions)
    }

    @Test
    fun onServiceUpdatesTransientErrorShownCallsServiceUpdatesErrorTracker() = runTest {
        val timestamps = mutableListOf<Long>()
        val viewModel = createViewModel(
            serviceUpdatesErrorTracker = FakeServiceUpdatesErrorTracker(
                onUpdateErrorShownTimestamp = { timestamps += it }
            )
        )
        val expected = listOf(123L)

        viewModel.onServiceUpdatesTransientErrorShown(123L)

        assertEquals(expected, timestamps)
    }

    private fun TestScope.createViewModel(
        incidentsViewModelState: IncidentsViewModelState = FakeIncidentsViewModelState(
            onActionFlow = { flowOf(null) }
        ),
        diversionsViewModelState: DiversionsViewModelState = FakeDiversionsViewModelState(
            onActionFlow = { flowOf(null) }
        ),
        refreshables: Set<Refreshable> = setOf(FakeRefreshable()),
        contentFetcher: UiContentFetcher = FakeUiContentFetcher(
            onDiversionsContentFlow = { flowOf(UiContent.InProgress) },
            onIncidentsContentFlow = { flowOf(UiContent.InProgress) }
        ),
        serviceUpdatesErrorTracker: ServiceUpdatesErrorTracker = FakeServiceUpdatesErrorTracker()
    ): NewsViewModel {
        return NewsViewModel(
            incidentsViewModelState = incidentsViewModelState,
            diversionsViewModelState = diversionsViewModelState,
            refreshables = refreshables,
            contentFetcher = contentFetcher,
            serviceUpdatesErrorTracker = serviceUpdatesErrorTracker,
            defaultCoroutineDispatcher = UnconfinedTestDispatcher(testScheduler),
            viewModelCoroutineScope = backgroundScope
        )
    }
}