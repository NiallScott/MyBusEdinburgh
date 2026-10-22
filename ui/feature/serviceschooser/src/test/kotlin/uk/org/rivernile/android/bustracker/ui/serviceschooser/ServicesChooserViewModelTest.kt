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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import app.cash.turbine.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests for [ServicesChooserViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class ServicesChooserViewModelTest {

    @Test
    fun uiStateFlowEmitsValues() = runTest {
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = {
                    flowOf(UiAction.Close)
                },
                onHasSelectedServicesFlow = {
                    flow {
                        emit(false)
                        delay(2L.milliseconds)
                        emit(true)
                    }
                }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = {
                    flow {
                        emit(UiContent.InProgress)
                        delay(1L.milliseconds)
                        emit(
                            UiContent.Content(
                                items = persistentListOf(
                                    UiServiceChooserItem.Operator.Named(
                                        operatorId = "TEST1",
                                        operatorName = "Test 1"
                                    ),
                                    UiServiceChooserItem.Service(
                                        serviceDescriptor = ServiceDescriptor(
                                            serviceName = "1",
                                            operatorCode = "TEST1"
                                        ),
                                        serviceName = UiServiceName(
                                            serviceName = "1",
                                            colours = null
                                        ),
                                        isSelected = false
                                    )
                                )
                            )
                        )
                    }
                }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    content = UiContent.InProgress,
                    action = UiAction.Close,
                    isClearAllButtonEnabled = false
                ),
                awaitItem()
            )
            assertEquals(
                UiState(
                    content = UiContent.Content(
                        items = persistentListOf(
                            UiServiceChooserItem.Operator.Named(
                                operatorId = "TEST1",
                                operatorName = "Test 1"
                            ),
                            UiServiceChooserItem.Service(
                                serviceDescriptor = ServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                ),
                                serviceName = UiServiceName(
                                    serviceName = "1",
                                    colours = null
                                ),
                                isSelected = false
                            )
                        )
                    ),
                    action = UiAction.Close,
                    isClearAllButtonEnabled = false
                ),
                awaitItem()
            )
            assertEquals(
                UiState(
                    content = UiContent.Content(
                        items = persistentListOf(
                            UiServiceChooserItem.Operator.Named(
                                operatorId = "TEST1",
                                operatorName = "Test 1"
                            ),
                            UiServiceChooserItem.Service(
                                serviceDescriptor = ServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                ),
                                serviceName = UiServiceName(
                                    serviceName = "1",
                                    colours = null
                                ),
                                isSelected = false
                            )
                        )
                    ),
                    action = UiAction.Close,
                    isClearAllButtonEnabled = true
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun onServiceClickedTogglesValueInState() = runTest {
        val serviceClickTracker = ItemTracker<ServiceDescriptor>()
        val service = ServiceDescriptor(
            serviceName = "1",
            operatorCode = "TEST1"
        )
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onToggleServiceSelectedState = serviceClickTracker,
                onHasSelectedServicesFlow = { flowOf(false) }
            )
        )

        viewModel.onServiceClicked(service)

        assertEquals(
            listOf(service),
            serviceClickTracker.items
        )
    }

    @Test
    fun onCloseClickedSetsCloseAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onSetAction = actionTracker,
                onHasSelectedServicesFlow = { flowOf(false) }
            )
        )

        viewModel.onCloseClicked()

        assertEquals(
            listOf(UiAction.Close),
            actionTracker.items
        )
    }

    @Test
    fun onClearAllClickedClearsAllSelectedServicesInState() = runTest {
        var clearAllClickedCount = 0
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onClearAllSelectedServices = { clearAllClickedCount++ },
                onHasSelectedServicesFlow = { flowOf(false) }
            )
        )

        viewModel.onClearAllClicked()

        assertEquals(1, clearAllClickedCount)
    }

    @Test
    fun onApplyClickedSetsDispatchResultAndCloseActionWhenSelectedServicesIsEmpty() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onSetAction = actionTracker,
                onSelectedServices = { emptySet() },
                onHasSelectedServicesFlow = { flowOf(false) }
            )
        )

        viewModel.onApplyClicked()

        assertEquals(
            listOf(
                UiAction.DispatchResultAndClose(
                    selectedServices = emptySet()
                )
            ),
            actionTracker.items
        )
    }

    @Test
    fun onApplyClickedSetsDispatchResultAndCloseActionWhenSelectedServicesIsPopulated() = runTest {
        val service = ServiceDescriptor(
            serviceName = "1",
            operatorCode = "TEST1"
        )
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onSetAction = actionTracker,
                onSelectedServices = { setOf(service) },
                onHasSelectedServicesFlow = { flowOf(false) }
            )
        )

        viewModel.onApplyClicked()

        assertEquals(
            listOf(
                UiAction.DispatchResultAndClose(
                    selectedServices = setOf(service)
                )
            ),
            actionTracker.items
        )
    }

    @Test
    fun onActionLaunchedSetsNullAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onSetAction = actionTracker,
                onHasSelectedServicesFlow = { flowOf(false) }
            )
        )

        viewModel.onActionLaunched()

        assertEquals(
            listOf(null),
            actionTracker.items
        )
    }

    private fun TestScope.createViewModel(
        state: State = FakeState(
            onActionFlow = { flowOf(null) },
            onHasSelectedServicesFlow = { flowOf(false) }
        ),
        uiContentFetcher: UiContentFetcher = FakeUiContentFetcher(
            onUiContentFlow = { flowOf(UiContent.InProgress) }
        )
    ): ServicesChooserViewModel {
        return ServicesChooserViewModel(
            state = state,
            uiContentFetcher = uiContentFetcher,
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
