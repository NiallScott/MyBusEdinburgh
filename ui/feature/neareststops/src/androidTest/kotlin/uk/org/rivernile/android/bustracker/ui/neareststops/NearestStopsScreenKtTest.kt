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
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.persistentSetOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.text.UiStopName
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
        assertLocationAccuracyBarDoesNotExist()
    }

    @Test
    fun showsPopulatedContentWhenContentIsContent() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Content(
                            locationAccuracy = null,
                            nearestStops = nearestStops
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
        assertLocationAccuracyBarDoesNotExist()
    }

    @Test
    fun showsPopulatedContentWithGpsNotPresentLocationAccuracyBar() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Content(
                            locationAccuracy = UiLocationAccuracy.GPS_NOT_PRESENT,
                            nearestStops = nearestStops
                        )
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_GPS_NOT_PRESENT)
            .assertExists()
    }

    @Test
    fun showsPopulatedContentWithPermissionsNotSufficientLocationAccuracyBar() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Content(
                            locationAccuracy = UiLocationAccuracy.PERMISSIONS_NOT_SUFFICIENT,
                            nearestStops = nearestStops
                        )
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_PERMISSIONS_NOT_SUFFICIENT)
            .assertExists()
    }

    @Test
    fun showsPopulatedContentWithGpsDisabledLocationAccuracyBar() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Content(
                            locationAccuracy = UiLocationAccuracy.GPS_DISABLED,
                            nearestStops = nearestStops
                        )
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_GPS_DISABLED)
            .assertExists()
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
        assertLocationAccuracyBarDoesNotExist()
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
        assertLocationAccuracyBarDoesNotExist()
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
        assertLocationAccuracyBarDoesNotExist()
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
        assertLocationAccuracyBarDoesNotExist()
    }

    @Test
    fun showsNoNearestStopsErrorWhenContentIsNoNearestStopsError() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Error.NoNearestStops(
                            locationAccuracy = null
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
        assertLocationAccuracyBarDoesNotExist()
    }

    @Test
    fun showsNoNearestStopsErrorWithGpsNotPresentLocationAccuracyBar() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Error.NoNearestStops(
                            locationAccuracy = UiLocationAccuracy.GPS_NOT_PRESENT
                        )
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_GPS_NOT_PRESENT)
            .assertExists()
    }

    @Test
    fun showsNoNearestStopsErrorWithPermissionsNotSufficientLocationAccuracyBar() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Error.NoNearestStops(
                            locationAccuracy = UiLocationAccuracy.PERMISSIONS_NOT_SUFFICIENT
                        )
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_PERMISSIONS_NOT_SUFFICIENT)
            .assertExists()
    }

    @Test
    fun showsNoNearestStopsErrorWithGpsDisabledLocationAccuracyBar() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Error.NoNearestStops(
                            locationAccuracy = UiLocationAccuracy.GPS_DISABLED
                        )
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_GPS_DISABLED)
            .assertExists()
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
                            selectedServices = persistentSetOf(
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
        val actionTracker = Tracker<Set<ServiceDescriptor>?>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowServicesChooser(
                            selectedServices = persistentSetOf(
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
                persistentSetOf(
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
    fun showAppPermissionSettingsActionHandlesNullLambdaThenMarksActionAsLaunched() {
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowAppPermissionSettings
                    ),
                    onShowAppPermissionSettings = null,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showAppPermissionSettingsActionCallsLambdaThenMarksActionAsLaunched() {
        val actionCounter = InvocationCounter()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.ShowAppPermissionSettings
                    ),
                    onShowAppPermissionSettings = actionCounter,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionCounter.count)
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun clickingOnServiceFilterMenuItemExecutesShowServicesChooserLambda() {
        val invocationCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress,
                        actionButtons = UiActionButtons(
                            serviceFilterActionButton = UiServiceFilterActionButton(
                                isEnabled = true
                            )
                        )
                    ),
                    onShowServicesChooserClick = invocationCounter
                )
            }
        }

        openActionBarOverflowOrOptionsMenu(composeTestRule.activity)
        onView(withText(composeTestRule.activity.getString(R.string.neareststops_menu_filter)))
            .perform(click())

        assertEquals(1, invocationCounter.count)
    }

    @Composable
    private fun NearestStopsScreenWithStateWithDefaults(
        state: UiState,
        modifier: Modifier = Modifier,
        onItemClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onAddFavouriteStopClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRemoveFavouriteStopClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onAddArrivalAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRemoveArrivalAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onAddProximityAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRemoveProximityAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onShowOnMapClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onGrantPermissionClick: () -> Unit = { throw NotImplementedError() },
        onOpenSettingsClick: () -> Unit = { throw NotImplementedError() },
        onShowServicesChooserClick: () -> Unit = { throw NotImplementedError() },
        onLocationAccuracyOpenAppSettingsClick: () -> Unit = { throw NotImplementedError() },
        onLocationAccuracyOpenSystemLocationSettingsClick: () -> Unit =
            { throw NotImplementedError() },
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
        onShowServicesChooser: ((Set<ServiceDescriptor>?) -> Unit)? =
            { throw NotImplementedError() },
        onShowLocationSettings: (() -> Unit)? = { throw NotImplementedError() },
        onShowAppPermissionSettings: (() -> Unit)? = { throw NotImplementedError() }
    ) {
        NearestStopsScreenWithState(
            state = state,
            onItemClick = onItemClick,
            onAddFavouriteStopClick = onAddFavouriteStopClick,
            onRemoveFavouriteStopClick = onRemoveFavouriteStopClick,
            onAddArrivalAlertClick = onAddArrivalAlertClick,
            onRemoveArrivalAlertClick = onRemoveArrivalAlertClick,
            onAddProximityAlertClick = onAddProximityAlertClick,
            onRemoveProximityAlertClick = onRemoveProximityAlertClick,
            onShowOnMapClick = onShowOnMapClick,
            onGrantPermissionClick = onGrantPermissionClick,
            onOpenSettingsClick = onOpenSettingsClick,
            onShowServicesChooserClick = onShowServicesChooserClick,
            onLocationAccuracyOpenAppSettingsClick = onLocationAccuracyOpenAppSettingsClick,
            onLocationAccuracyOpenSystemLocationSettingsClick =
                onLocationAccuracyOpenSystemLocationSettingsClick,
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
            onShowAppPermissionSettings = onShowAppPermissionSettings
        )
    }

    private val nearestStops get() = persistentListOf(
        UiNearestStop(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            stopName = UiStopName(
                name = "Stop name",
                locality = "Locality"
            ),
            services = null,
            orientation = StopOrientation.NORTH_EAST,
            distanceMeters = 123,
            dropdownMenu = UiNearestStopDropdownMenu()
        )
    )

    private fun assertLocationAccuracyBarDoesNotExist() {
        composeTestRule
            .onNodeWithTag(TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_GPS_NOT_PRESENT)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_PERMISSIONS_NOT_SUFFICIENT)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_GPS_DISABLED)
            .assertDoesNotExist()
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
