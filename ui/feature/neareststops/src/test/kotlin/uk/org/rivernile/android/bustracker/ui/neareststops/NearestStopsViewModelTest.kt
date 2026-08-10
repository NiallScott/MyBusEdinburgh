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

package uk.org.rivernile.android.bustracker.ui.neareststops

import app.cash.turbine.test
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.Duration.Companion.milliseconds

/**
 * Tests for [NearestStopsViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class NearestStopsViewModelTest {

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
        val viewModel = createViewModel(
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = {
                    flow {
                        delay(1.milliseconds)
                        emit(UiContent.Error.InsufficientLocationPermissions)
                    }
                }
            ),
            uiActionButtonsGenerator = FakeUiActionButtonsGenerator(
                onUiActionButtonsFlow = {
                    flowOf(
                        UiActionButtons(
                            serviceFilterActionButton = UiServiceFilterActionButton(
                                isEnabled = true
                            )
                        )
                    )
                }
            ),
            state = FakeState(
                onActionFlow = { flowOf(UiAction.RequestLocationPermissions) },
                onSetSelectedStopIdentifier = { }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            assertEquals(
                UiState(
                    content = UiContent.Error.InsufficientLocationPermissions,
                    actionButtons = UiActionButtons(
                        serviceFilterActionButton = UiServiceFilterActionButton(
                            isEnabled = true
                        )
                    ),
                    action = UiAction.RequestLocationPermissions
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowEmitsWhenActionChanges() = runTest {
        val viewModel = createViewModel(
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = {
                    flow {
                        delay(1.milliseconds)
                        emit(UiContent.Error.InsufficientLocationPermissions)
                    }
                }
            ),
            uiActionButtonsGenerator = FakeUiActionButtonsGenerator(
                onUiActionButtonsFlow = {
                    flowOf(
                        UiActionButtons(
                            serviceFilterActionButton = UiServiceFilterActionButton(
                                isEnabled = true
                            )
                        )
                    )
                }
            ),
            state = FakeState(
                onActionFlow = {
                    flow {
                        emit(null)
                        delay(2.milliseconds)
                        emit(UiAction.RequestLocationPermissions)
                    }
                },
                onSetSelectedStopIdentifier = { }
            )
        )

        viewModel.uiStateFlow.test {
            assertEquals(UiState(), awaitItem())
            assertEquals(
                UiState(
                    content = UiContent.Error.InsufficientLocationPermissions,
                    actionButtons = UiActionButtons(
                        serviceFilterActionButton = UiServiceFilterActionButton(
                            isEnabled = true
                        )
                    ),
                    action = null
                ),
                awaitItem()
            )
            assertEquals(
                UiState(
                    content = UiContent.Error.InsufficientLocationPermissions,
                    actionButtons = UiActionButtons(
                        serviceFilterActionButton = UiServiceFilterActionButton(
                            isEnabled = true
                        )
                    ),
                    action = UiAction.RequestLocationPermissions
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun uiStateFlowWithInProgressStateClearsSelectedStopIdentifier() = runTest {
        val selectedStopIdentifiers = mutableListOf<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onSetSelectedStopIdentifier = { selectedStopIdentifiers += it }
            ),
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = { flowOf(UiContent.InProgress) }
            ),
            uiActionButtonsGenerator = defaultUiActionButtonsGenerator
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    content = UiContent.InProgress,
                    actionButtons = UiActionButtons(),
                    action = null
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
        assertEquals(listOf<StopIdentifier?>(null), selectedStopIdentifiers)
    }

    @Test
    fun uiStateFlowWithNoLocationFeatureClearsSelectedStopIdentifier() = runTest {
        val selectedStopIdentifiers = mutableListOf<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onSetSelectedStopIdentifier = { selectedStopIdentifiers += it }
            ),
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = { flowOf(UiContent.Error.NoLocationFeature) }
            ),
            uiActionButtonsGenerator = defaultUiActionButtonsGenerator
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    content = UiContent.Error.NoLocationFeature,
                    actionButtons = UiActionButtons(),
                    action = null
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
        assertEquals(listOf<StopIdentifier?>(null), selectedStopIdentifiers)
    }

    @Test
    fun uiStateFlowWithInsufficientLocationPermissionsClearsSelectedStopIdentifier() = runTest {
        val selectedStopIdentifiers = mutableListOf<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onSetSelectedStopIdentifier = { selectedStopIdentifiers += it }
            ),
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = { flowOf(UiContent.Error.InsufficientLocationPermissions) }
            ),
            uiActionButtonsGenerator = defaultUiActionButtonsGenerator
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    content = UiContent.Error.InsufficientLocationPermissions,
                    actionButtons = UiActionButtons(),
                    action = null
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
        assertEquals(listOf<StopIdentifier?>(null), selectedStopIdentifiers)
    }

    @Test
    fun uiStateFlowWithLocationOffClearsSelectedStopIdentifier() = runTest {
        val selectedStopIdentifiers = mutableListOf<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onSetSelectedStopIdentifier = { selectedStopIdentifiers += it }
            ),
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = { flowOf(UiContent.Error.LocationOff) }
            ),
            uiActionButtonsGenerator = defaultUiActionButtonsGenerator
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    content = UiContent.Error.LocationOff,
                    actionButtons = UiActionButtons(),
                    action = null
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
        assertEquals(listOf<StopIdentifier?>(null), selectedStopIdentifiers)
    }

    @Test
    fun uiStateFlowWithLocationUnknownClearsSelectedStopIdentifier() = runTest {
        val selectedStopIdentifiers = mutableListOf<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onSetSelectedStopIdentifier = { selectedStopIdentifiers += it }
            ),
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = { flowOf(UiContent.Error.LocationUnknown) }
            ),
            uiActionButtonsGenerator = defaultUiActionButtonsGenerator
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    content = UiContent.Error.LocationUnknown,
                    actionButtons = UiActionButtons(),
                    action = null
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
        assertEquals(listOf<StopIdentifier?>(null), selectedStopIdentifiers)
    }

    @Test
    fun uiStateFlowWithNoNearestStopsClearsSelectedStopIdentifier() = runTest {
        val selectedStopIdentifiers = mutableListOf<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onSetSelectedStopIdentifier = { selectedStopIdentifiers += it }
            ),
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = {
                    flowOf(
                        UiContent.Error.NoNearestStops(locationAccuracy = null)
                    )
                }
            ),
            uiActionButtonsGenerator = defaultUiActionButtonsGenerator
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    content = UiContent.Error.NoNearestStops(locationAccuracy = null),
                    actionButtons = UiActionButtons(),
                    action = null
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
        assertEquals(listOf<StopIdentifier?>(null), selectedStopIdentifiers)
    }

    @Test
    fun uiStateFlowWithContentClearsSelectedStopIdentifierWhenNotContainedWithinStops() = runTest {
        val selectedStopIdentifiers = mutableListOf<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onUpdateSelectedStopIdentifier = { function ->
                    selectedStopIdentifiers += function.invoke("987654".toNaptanStopIdentifier())
                }
            ),
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = {
                    flowOf(
                        UiContent.Content(
                            nearestStops = persistentListOf(nearestStop),
                            locationAccuracy = null
                        )
                    )
                }
            ),
            uiActionButtonsGenerator = defaultUiActionButtonsGenerator
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    content = UiContent.Content(
                        nearestStops = persistentListOf(nearestStop),
                        locationAccuracy = null
                    ),
                    actionButtons = UiActionButtons(),
                    action = null
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
        assertEquals(listOf<StopIdentifier?>(null), selectedStopIdentifiers)
    }

    @Test
    fun uiStateFlowWithContentPreservesSelectedStopIdentifierWhenContainedWithinStops() = runTest {
        val selectedStopIdentifiers = mutableListOf<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = { flowOf(null) },
                onUpdateSelectedStopIdentifier = { function ->
                    selectedStopIdentifiers += function.invoke("123456".toNaptanStopIdentifier())
                }
            ),
            uiContentRetriever = FakeUiContentRetriever(
                onUiContentFlow = {
                    flowOf(
                        UiContent.Content(
                            nearestStops = persistentListOf(nearestStop),
                            locationAccuracy = null
                        )
                    )
                }
            ),
            uiActionButtonsGenerator = defaultUiActionButtonsGenerator
        )

        viewModel.uiStateFlow.test {
            assertEquals(
                UiState(
                    content = UiContent.Content(
                        nearestStops = persistentListOf(nearestStop),
                        locationAccuracy = null
                    ),
                    actionButtons = UiActionButtons(),
                    action = null
                ),
                awaitItem()
            )
            ensureAllEventsConsumed()
        }
        assertEquals(
            listOf<StopIdentifier?>("123456".toNaptanStopIdentifier()),
            selectedStopIdentifiers
        )
    }

    @Test
    fun onItemClickedSetsShowsStopDataAction() = runTest {
        val itemTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = itemTracker
            )
        )

        viewModel.onItemClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowStopData(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            itemTracker.items
        )
    }

    @Test
    fun onOpenDropdownMenuClickedSetsSelectedStopIdentifier() = runTest {
        val itemTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetSelectedStopIdentifier = itemTracker
            )
        )

        viewModel.onOpenDropdownMenuClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
            itemTracker.items
        )
    }

    @Test
    fun onDropdownMenuDismissedSetsSelectedStopIdentifierToNull() = runTest {
        val itemTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetSelectedStopIdentifier = itemTracker
            )
        )

        viewModel.onDropdownMenuDismissed()

        assertEquals(
            listOf(null),
            itemTracker.items
        )
    }

    @Test
    fun onAddFavouriteStopClickedSetsShowAddFavouriteStopActionAndDismissesDropdown() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker,
                onSetSelectedStopIdentifier = selectedStopIdentifierTracker
            )
        )

        viewModel.onAddFavouriteStopClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowAddFavouriteStop(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopIdentifierTracker.items)
    }

    @Test
    fun onRemoveFavouriteClickedSetsShowRemoveFavouriteStopActionAndDismissesDropdown() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker,
                onSetSelectedStopIdentifier = selectedStopIdentifierTracker
            )
        )

        viewModel.onRemoveFavouriteStopClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowRemoveFavouriteStop(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopIdentifierTracker.items)
    }

    @Test
    fun onAddArrivalAlertClickedSetsShowAddArrivalAlertActionAndDismissesDropdown() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker,
                onSetSelectedStopIdentifier = selectedStopIdentifierTracker
            )
        )

        viewModel.onAddArrivalAlertClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowAddArrivalAlert(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopIdentifierTracker.items)
    }

    @Test
    fun onRemoveArrivalAlertClickedSetsShowRemoveArrivalAlertActionAndDismissesDropdown() =
        runTest {
            val actionTracker = ItemTracker<UiAction?>()
            val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
            val viewModel = createViewModel(
                state = FakeState(
                    onActionFlow = ::emptyFlow,
                    onSetAction = actionTracker,
                    onSetSelectedStopIdentifier = selectedStopIdentifierTracker
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
            assertEquals(listOf(null), selectedStopIdentifierTracker.items)
        }

    @Test
    fun onAddProxAlertClickedSetsShowAddProxAlertActionAndDismissesDropdown() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker,
                onSetSelectedStopIdentifier = selectedStopIdentifierTracker
            )
        )

        viewModel.onAddProximityAlertClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowAddProximityAlert(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopIdentifierTracker.items)
    }

    @Test
    fun onRemoveProxAlertClickedSetsShowRemoveProxAlertActionAndDismissesDropdown() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker,
                onSetSelectedStopIdentifier = selectedStopIdentifierTracker
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
        assertEquals(listOf(null), selectedStopIdentifierTracker.items)
    }

    @Test
    fun onShowOnMapClickedSetsShowOnMapActionAndDismissesDropdown() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val selectedStopIdentifierTracker = ItemTracker<StopIdentifier?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker,
                onSetSelectedStopIdentifier = selectedStopIdentifierTracker
            )
        )

        viewModel.onShowOnMapClicked("123456".toNaptanStopIdentifier())

        assertEquals(
            listOf(
                UiAction.ShowOnMap(stopIdentifier = "123456".toNaptanStopIdentifier())
            ),
            actionTracker.items
        )
        assertEquals(listOf(null), selectedStopIdentifierTracker.items)
    }

    @Test
    fun onGrantPermissionClickedSetsRequestLocationPermissionsAction() = runTest {
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
    fun onOpenSettingsClickedSetsShowLocationSettingsAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onOpenSettingsClicked()

        assertEquals(
            listOf(UiAction.ShowLocationSettings),
            actionTracker.items
        )
    }

    @Test
    fun onShowServicesChooserClickedSetsShowServicesChooserAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val service = FakeServiceDescriptor(
            serviceName = "1",
            operatorCode = "TEST1"
        )
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker,
                onGetSelectedServices = { setOf(service) }
            )
        )

        viewModel.onShowServicesChooserClicked()

        assertEquals(
            listOf(UiAction.ShowServicesChooser(selectedServices = persistentSetOf(service))),
            actionTracker.items
        )
    }

    @Test
    fun onLocationAccuracyOpenAppSettingsClickedSetsShowAppPermissionSettingsAction() = runTest {
        val actionTracker = ItemTracker<UiAction?>()
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetAction = actionTracker
            )
        )

        viewModel.onLocationAccuracyOpenAppSettingsClicked()

        assertEquals(
            listOf(UiAction.ShowAppPermissionSettings),
            actionTracker.items
        )
    }

    @Test
    fun onLocationAccuracyOpenSystemLocationSettingsClickedSetsShowLocationSettingsAction() =
        runTest {
            val actionTracker = ItemTracker<UiAction?>()
            val viewModel = createViewModel(
                state = FakeState(
                    onActionFlow = ::emptyFlow,
                    onSetAction = actionTracker
                )
            )

            viewModel.onLocationAccuracyOpenSystemLocationSettingsClicked()

            assertEquals(
                listOf(UiAction.ShowLocationSettings),
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

    @Test
    fun onUpdatePermissionsStateSetsPermissionsState() = runTest {
        val permissionsStateTracker = ItemTracker<PermissionsState?>()
        val permissionsState = PermissionsState(
            fineLocationPermission = PermissionState.UNGRANTED,
            coarseLocationPermission = PermissionState.GRANTED
        )
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetPermissionsState = permissionsStateTracker
            )
        )

        viewModel.onUpdatePermissionsState(permissionsState)

        assertEquals(listOf(permissionsState), permissionsStateTracker.items)
    }

    @Test
    fun onServicesSelectedSetsSelectedServices() = runTest {
        val selectedServicesTracker = ItemTracker<Set<ServiceDescriptor>?>()
        val service = FakeServiceDescriptor(
            serviceName = "1",
            operatorCode = "TEST1"
        )
        val viewModel = createViewModel(
            state = FakeState(
                onActionFlow = ::emptyFlow,
                onSetSelectedServices = selectedServicesTracker
            )
        )

        viewModel.onServicesSelected(setOf(service))

        assertEquals(listOf(setOf(service)), selectedServicesTracker.items)
    }

    private fun TestScope.createViewModel(
        state: State = FakeState(
            onActionFlow = ::emptyFlow
        ),
        uiContentRetriever: UiContentRetriever = FakeUiContentRetriever(
            onUiContentFlow = ::emptyFlow
        ),
        uiActionButtonsGenerator: UiActionButtonsGenerator = FakeUiActionButtonsGenerator(
            onUiActionButtonsFlow = ::emptyFlow
        )
    ): NearestStopsViewModel {
        return NearestStopsViewModel(
            state = state,
            uiContentRetriever = uiContentRetriever,
            uiActionButtonsGenerator = uiActionButtonsGenerator,
            defaultCoroutineDispatcher = UnconfinedTestDispatcher(scheduler = testScheduler),
            viewModelCoroutineScope = backgroundScope
        )
    }

    private val defaultUiActionButtonsGenerator get() = FakeUiActionButtonsGenerator(
        onUiActionButtonsFlow = { flowOf(UiActionButtons()) }
    )

    private val nearestStop get() = UiNearestStop(
        stopIdentifier = "123456".toNaptanStopIdentifier(),
        stopName = UiStopName(
            name = "Stop 1",
            locality = "Locality 1"
        ),
        services = null,
        orientation = StopOrientation.SOUTH_WEST,
        distanceMeters = 123,
        dropdownMenu = UiNearestStopDropdownMenu()
    )

    private class ItemTracker<T> : (T) -> Unit {

        val items get() = _items.toList()
        private val _items = mutableListOf<T>()

        override fun invoke(p1: T) {
            _items += p1
        }
    }
}
