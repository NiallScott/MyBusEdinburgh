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

package uk.org.rivernile.android.bustracker.ui.stopdetails

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
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.fail
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests for [StopDetailsViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class StopDetailsViewModelTest {

    @Test
    fun uiStateFlowEmitsValuesFromUiContentAndActionFlows() = runTest {
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = {
                    flow {
                        emit(null)
                        delay(1.milliseconds)
                        emit(UiAction.ShowLocationSettings)
                    }
                }
            ),
            contentRetriever = FakeUiContentRetriever(
                onUiContentFlow = {
                    flowOf(UiContent.NoStopDetailsError)
                }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    content = UiContent.NoStopDetailsError,
                    action = null
                ),
                awaitItem()
            )
            assertEquals(
                UiState(
                    content = UiContent.NoStopDetailsError,
                    action = UiAction.ShowLocationSettings
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onStopMapClickedDoesNotPerformActionWhenStopIdentifierIsNull() = runTest {
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onGetStopIdentifier = { null }
            ),
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = { fail("Not expecting action to be set.") }
            )
        )

        viewModel.onStopMapClicked()
    }

    @Test
    fun onStopMapClickedPerformsActionWhenStopIdentifierIsNotNull() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            arguments = FakeArguments(
                onGetStopIdentifier = { "123456".toNaptanStopIdentifier() }
            ),
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onStopMapClicked()

        assertEquals(
            listOf(
                UiAction.ShowOnMap(
                    stopIdentifier = "123456".toNaptanStopIdentifier()
                )
            ),
            actionTracker.items
        )
    }

    @Test
    fun onGrantPermissionClickedPerformsRequestLocationPermissionsAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onGrantPermissionClicked()

        assertEquals(
            listOf(UiAction.RequestLocationPermissions),
            actionTracker.items
        )
    }

    @Test
    fun onTurnOnLocationClickedPerformsShowLocationSettingsAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onTurnOnLocationClicked()

        assertEquals(
            listOf(UiAction.ShowLocationSettings),
            actionTracker.items
        )
    }

    @Test
    fun onActionLaunchedSetsActionAsNull() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onActionLaunched()

        assertEquals(
            listOf(null),
            actionTracker.items
        )
    }

    @Test
    fun onResumeStateChangedSetsResumedState() = runTest {
        val resumedStateTracker = ItemTracker<Boolean>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetIsResumed = resumedStateTracker
            )
        )

        viewModel.onResumeStateChanged(isResumed = false)
        viewModel.onResumeStateChanged(isResumed = true)

        assertEquals(
            listOf(false, true),
            resumedStateTracker.items
        )
    }

    @Test
    fun onUpdatePermissionsStateSetsPermissionsState() = runTest {
        val permissionsStatesTracker = ItemTracker<PermissionsState?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetPermissionsState = permissionsStatesTracker
            )
        )

        viewModel.onUpdatePermissionsState(
            permissionsState = PermissionsState(
                fineLocationPermission = PermissionState.GRANTED,
                coarseLocationPermission = PermissionState.GRANTED
            )
        )

        assertEquals(
            listOf(
                PermissionsState(
                    fineLocationPermission = PermissionState.GRANTED,
                    coarseLocationPermission = PermissionState.GRANTED
                )
            ),
            permissionsStatesTracker.items
        )
    }

    private fun TestScope.createViewModel(
        arguments: Arguments = FakeArguments(),
        state: State = FakeState(
            onActionFlow = ::emptyFlow
        ),
        contentRetriever: UiContentRetriever = FakeUiContentRetriever(
            onUiContentFlow = ::emptyFlow
        )
    ): StopDetailsViewModel {
        return StopDetailsViewModel(
            arguments = arguments,
            state = state,
            contentRetriever = contentRetriever,
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
