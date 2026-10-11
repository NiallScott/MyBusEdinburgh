/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts.manager

import app.cash.turbine.test
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests for [AlertManagerViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class AlertManagerViewModelTest {

    @Test
    fun uiStateFlowInitiallyEmitsDefaultFlow() = runTest {
        val viewModel = createViewModel()

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsState() = runTest {
        val viewmodel = createViewModel(
            state = FakeState(
                onActionFlow = {
                    flowOf(UiAction.ShowLocationSettings)
                }
            ),
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = {
                    flow {
                        delay(1.milliseconds)
                        emit(UiContent.EmptyAlerts)
                    }
                }
            )
        )

        viewmodel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            assertEquals(
                UiState(
                    content = UiContent.EmptyAlerts,
                    action = UiAction.ShowLocationSettings
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsWhenActionChanges() = runTest {
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = {
                    flow {
                        emit(null)
                        delay(2.milliseconds)
                        emit(UiAction.ShowLocationSettings)
                    }
                }
            ),
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = {
                    flow {
                        delay(1.milliseconds)
                        emit(UiContent.EmptyAlerts)
                    }
                }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            assertEquals(
                UiState(
                    content = UiContent.EmptyAlerts,
                    action = null
                ),
                awaitItem()
            )
            assertEquals(
                UiState(
                    content = UiContent.EmptyAlerts,
                    action = UiAction.ShowLocationSettings
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onRemoveArrivalAlertClickedSetsShowRemoveArrivalAlertAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onRemoveArrivalAlertClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowRemoveArrivalAlert(
                    stopIdentifier = "123456".toNaptanStopIdentifier()
                )
            ),
            actionTracker.items
        )
    }

    @Test
    fun onRemoveProximityAlertClickedSetsShowRemoveArrivalAlertAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onRemoveProximityAlertClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowRemoveProximityAlert(
                    stopIdentifier = "123456".toNaptanStopIdentifier()
                )
            ),
            actionTracker.items
        )
    }

    @Test
    fun onLocationSettingsClickedSetsShowLocationSettingsAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onLocationSettingsClicked()

        assertEquals(
            listOf(
                UiAction.ShowLocationSettings
            ),
            actionTracker.items
        )
    }

    @Test
    fun onActionLaunchedSetsNullAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onActionLaunched()

        assertEquals(listOf(null), actionTracker.items)
    }

    private fun TestScope.createViewModel(
        state: State = FakeState(
            onActionFlow = ::emptyFlow
        ),
        uiContentRetriever: UiContentRetriever = FakeUiContentRetriever(
            onUiContentFlow = ::emptyFlow
        )
    ): AlertManagerViewModel {
        return AlertManagerViewModel(
            state = state,
            uiContentRetriever = uiContentRetriever,
            defaultCoroutineDispatcher = UnconfinedTestDispatcher(scheduler = testScheduler),
            viewModelCoroutineScope = backgroundScope
        )
    }

    private class ItemTracker<T> : (T) -> Unit {

        val items get() = _items.toList()
        private val _items = mutableListOf<T>()

        override fun invoke(p1: T) {
            _items += p1
        }
    }
}
