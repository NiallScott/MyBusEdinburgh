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
                onHasSelectedServicesFlow = {
                    flow {
                        emit(false)
                        delay(2L)
                        emit(true)
                    }
                }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = {
                    flow {
                        emit(UiContent.InProgress)
                        delay(1L)
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
                    isClearAllButtonEnabled = true
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun selectedServicesReturnsValueFromState() = runTest {
        val selectedServices = setOf(
            ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            )
        )
        val viewModel = createViewModel(
            state = FakeState(
                onSelectedServices = { selectedServices },
                onHasSelectedServicesFlow = { flowOf(true) }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = { flowOf(UiContent.InProgress) }
            )
        )

        val result = viewModel.selectedServices

        assertEquals(selectedServices, result)
    }

    @Test
    fun onServiceClickedTogglesValueInState() = runTest {
        val clickedServices = mutableListOf<ServiceDescriptor>()
        val service = ServiceDescriptor(
            serviceName = "1",
            operatorCode = "TEST1"
        )
        val viewModel = createViewModel(
            state = FakeState(
                onToggleServiceSelectedState = { clickedServices += it },
                onHasSelectedServicesFlow = { flowOf(false) }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = { flowOf(UiContent.InProgress) }
            )
        )

        viewModel.onServiceClicked(service)

        assertEquals(
            listOf(service),
            clickedServices
        )
    }

    @Test
    fun onClearAllClickedClearsAllSelectedServicesInState() = runTest {
        var clearAllClickedCount = 0
        val viewModel = createViewModel(
            state = FakeState(
                onClearAllSelectedServices = { clearAllClickedCount++ },
                onHasSelectedServicesFlow = { flowOf(false) }
            ),
            uiContentFetcher = FakeUiContentFetcher(
                onUiContentFlow = { flowOf(UiContent.InProgress) }
            )
        )

        viewModel.onClearAllClicked()

        assertEquals(1, clearAllClickedCount)
    }

    private fun TestScope.createViewModel(
        state: State = FakeState(),
        uiContentFetcher: UiContentFetcher = FakeUiContentFetcher()
    ): ServicesChooserViewModel {
        return ServicesChooserViewModel(
            state = state,
            uiContentFetcher = uiContentFetcher,
            defaultCoroutineDispatcher = UnconfinedTestDispatcher(scheduler = testScheduler),
            viewModelCoroutineScope = backgroundScope
        )
    }
}
