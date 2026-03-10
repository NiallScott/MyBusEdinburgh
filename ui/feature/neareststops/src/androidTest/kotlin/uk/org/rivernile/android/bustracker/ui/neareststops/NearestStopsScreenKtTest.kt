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

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `NearestStopsScreen.kt`.
 *
 * @author Niall Scott
 */
class NearestStopsScreenKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsIndeterminateProgressWhenContentIsProgress() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertExists()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR)
            .assertDoesNotExist()
    }

    @Test
    fun showsPopulatedContentWhenContentIsContent() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Content(
                            nearestStops = persistentListOf(
                                UiNearestStop(
                                    stopIdentifier = "123456".toNaptanStopIdentifier(),
                                    stopName = null,
                                    services = null,
                                    orientation = StopOrientation.NORTH_EAST,
                                    distanceMeters = 123,
                                    dropdownMenu = UiNearestStopDropdownMenu()
                                )
                            )
                        )
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertExists()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR)
            .assertDoesNotExist()
    }

    @Test
    fun showsNoLocationFeatureErrorWhenContentIsNoLocationFeatureError() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Error.NoLocationFeature
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR)
            .apply {
                assertExists()
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_ERROR_TITLE))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.neareststops_error_no_location_feature_title)
                    )
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_ERROR_BLURB))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.neareststops_error_no_location_feature_blurb)
                    )
            }
    }

    @Test
    fun insufficientLocationPermissionsErrorWhenContentIsInsufficientLocationPermissionsError() {
        val invocationCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Error.InsufficientLocationPermissions
                    ),
                    onGrantPermissionClick = invocationCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR)
            .apply {
                assertExists()
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_ERROR_TITLE))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.neareststops_error_permission_required_title)
                    )
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_ERROR_BLURB))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.neareststops_error_permission_required_blurb)
                    )
                onChildren()
                    .filterToOne(
                        hasText(
                            composeTestRule
                                .activity
                                .getString(R.string.neareststops_error_permission_required_button)
                        )
                    )
                    .performClick()
            }
        assertEquals(1, invocationCounter.count)
    }

    @Test
    fun showsLocationOffErrorWhenContentIsLocationOffError() {
        val invocationCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Error.LocationOff
                    ),
                    onOpenSettingsClick = invocationCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR)
            .apply {
                assertExists()
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_ERROR_TITLE))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.neareststops_error_location_sources_title)
                    )
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_ERROR_BLURB))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.neareststops_error_location_sources_blurb)
                    )
                onChildren()
                    .filterToOne(
                        hasText(
                            composeTestRule
                                .activity
                                .getString(R.string.neareststops_error_location_sources_button)
                        )
                    )
                    .performClick()
            }
        assertEquals(1, invocationCounter.count)
    }

    @Test
    fun showsLocationUnknownErrorWhenContentIsLocationUnknownError() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Error.LocationUnknown
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR)
            .apply {
                assertExists()
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_ERROR_TITLE))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.neareststops_error_location_unknown_title)
                    )
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_ERROR_BLURB))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.neareststops_error_location_unknown_blurb)
                    )
            }
    }

    @Test
    fun showsNoNearestStopsErrorWhenContentIsNoNearestStopsError() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Error.NoNearestStops
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_POPULATED)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR)
            .apply {
                assertExists()
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_ERROR_TITLE))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.neareststops_error_empty_title)
                    )
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_ERROR_BLURB))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.neareststops_error_empty_blurb)
                    )
            }
    }

    @Test
    fun showStopDataActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowStopData(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowStopData = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showStopDataActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowStopData(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowStopData = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showAddFavouriteStopActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowAddFavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowAddFavouriteStop = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showAddFavouriteStopActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowAddFavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowAddFavouriteStop = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showRemoveFavouriteStopActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowRemoveFavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowRemoveFavouriteStop = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showRemoveFavouriteStopActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowRemoveFavouriteStop(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowRemoveFavouriteStop = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showAddArrivalAlertActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowAddArrivalAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowAddArrivalAlert = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showAddArrivalAlertActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowAddArrivalAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowAddArrivalAlert = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showRemoveArrivalAlertActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowRemoveArrivalAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowRemoveArrivalAlert = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showRemoveArrivalAlertActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowRemoveArrivalAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowRemoveArrivalAlert = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showAddProximityAlertActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowAddProximityAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowAddProximityAlert = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showAddProximityAlertActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowAddProximityAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowAddProximityAlert = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showRemoveProximityAlertActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowRemoveProximityAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowRemoveProximityAlert = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showRemoveProximityAlertActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowRemoveProximityAlert(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowRemoveProximityAlert = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showOnMapActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowOnMap(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowOnMap = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showOnMapActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowOnMap(
                            stopIdentifier = "123456".toNaptanStopIdentifier()
                        )
                    ),
                    onShowOnMap = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun requestLocationPermissionsActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.RequestLocationPermissions
                    ),
                    onRequestLocationPermissions = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun requestLocationPermissionsActionCallsLambdaThenMarksActionAsLaunched() {
        val actionCounter = InvocationCounter()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.RequestLocationPermissions
                    ),
                    onRequestLocationPermissions = actionCounter,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionCounter.count)
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showServicesChooserActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowServicesChooser(
                            selectedServices = persistentListOf(
                                FakeServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                )
                            )
                        )
                    ),
                    onShowServicesChooser = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showServicesChooserActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<List<ServiceDescriptor>?>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowServicesChooser(
                            selectedServices = persistentListOf(
                                FakeServiceDescriptor(
                                    serviceName = "1",
                                    operatorCode = "TEST1"
                                )
                            )
                        )
                    ),
                    onShowServicesChooser = actionTracker,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(
            listOf(
                persistentListOf(
                    FakeServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    )
                )
            ),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showLocationSettingsActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowLocationSettings
                    ),
                    onShowLocationSettings = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showLocationSettingsActionCallsLambdaThenMarksActionAsLaunched() {
        val actionCounter = InvocationCounter()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowLocationSettings
                    ),
                    onShowLocationSettings = actionCounter,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionCounter.count)
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showTurnOnGpsActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowTurnOnGps
                    ),
                    onShowTurnOnGps = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showTurnOnGpsActionCallsLambdaThenMarksActionAsLaunched() {
        val actionCounter = InvocationCounter()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowTurnOnGps
                    ),
                    onShowTurnOnGps = actionCounter,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionCounter.count)
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Composable
    private fun NearestStopsScreenWithStateWithDefaults(
        state: UiState,
        modifier: Modifier = Modifier,
        onItemClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onOpenDropdownMenuClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onDropdownMenuDismissed: () -> Unit = { throw NotImplementedError() },
        onAddFavouriteStopClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRemoveFavouriteStopClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onAddArrivalAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRemoveArrivalAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onAddProximityAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRemoveProximityAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onShowOnMapClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onGrantPermissionClick: () -> Unit = { throw NotImplementedError() },
        onOpenSettingsClick: () -> Unit = { throw NotImplementedError() },
        onActionLaunched: () -> Unit = { throw NotImplementedError() },
        onShowStopData: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowAddFavouriteStop: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowRemoveFavouriteStop: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowAddArrivalAlert: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowRemoveArrivalAlert: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowAddProximityAlert: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowRemoveProximityAlert: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onShowOnMap: ((StopIdentifier) -> Unit)? = { throw NotImplementedError() },
        onRequestLocationPermissions: (() -> Unit)? = { throw NotImplementedError() },
        onShowServicesChooser: ((ImmutableList<ServiceDescriptor>?) -> Unit)? =
            { throw NotImplementedError() },
        onShowLocationSettings: (() -> Unit)? = { throw NotImplementedError() },
        onShowTurnOnGps: (() -> Unit)? = { throw NotImplementedError() }
    ) {
        NearestStopsScreenWithState(
            state = state,
            onItemClick = onItemClick,
            onOpenDropdownMenuClick = onOpenDropdownMenuClick,
            onDropdownMenuDismissed = onDropdownMenuDismissed,
            onAddFavouriteStopClick = onAddFavouriteStopClick,
            onRemoveFavouriteStopClick = onRemoveFavouriteStopClick,
            onAddArrivalAlertClick = onAddArrivalAlertClick,
            onRemoveArrivalAlertClick = onRemoveArrivalAlertClick,
            onAddProximityAlertClick = onAddProximityAlertClick,
            onRemoveProximityAlertClick = onRemoveProximityAlertClick,
            onShowOnMapClick = onShowOnMapClick,
            onGrantPermissionClick = onGrantPermissionClick,
            onOpenSettingsClick = onOpenSettingsClick,
            onActionLaunched = onActionLaunched,
            modifier = modifier,
            onShowStopData = onShowStopData,
            onShowAddFavouriteStop = onShowAddFavouriteStop,
            onShowRemoveFavouriteStop = onShowRemoveFavouriteStop,
            onShowAddArrivalAlert = onShowAddArrivalAlert,
            onShowRemoveArrivalAlert = onShowRemoveArrivalAlert,
            onShowAddProximityAlert = onShowAddProximityAlert,
            onShowRemoveProximityAlert = onShowRemoveProximityAlert,
            onShowOnMap = onShowOnMap,
            onRequestLocationPermissions = onRequestLocationPermissions,
            onShowServicesChooser = onShowServicesChooser,
            onShowLocationSettings = onShowLocationSettings,
            onShowTurnOnGps = onShowTurnOnGps
        )
    }
}

private class Tracker<T> : (T) -> Unit {

    val observedValues get() = _observedValues.toList()
    private val _observedValues = mutableListOf<T>()

    override fun invoke(p1: T) {
        _observedValues += p1
    }
}

private class InvocationCounter : () -> Unit {

    var count = 0
        private set

    override fun invoke() {
        count++
    }
}
