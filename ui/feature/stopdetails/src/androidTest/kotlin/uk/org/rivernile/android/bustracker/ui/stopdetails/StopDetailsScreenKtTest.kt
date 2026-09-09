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

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toAtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `StopDetailsScreen.kt`.
 *
 * @author Niall Scott
 */
class StopDetailsScreenKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsIndeterminateProgressWhenContentIsProgress() {
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsScreenWithStateWithDefaults(
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
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR_NO_STOP_DETAILS)
            .assertDoesNotExist()
    }

    @Test
    fun showsPopulatedContentWhenContentIsContent() {
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Content(
                            stopDetails = UiStopDetails(
                                naptanCode = "123456".toNaptanStopIdentifier(),
                                atcoCode = "ATCO987654".toAtcoStopIdentifier(),
                                latLon = UiLatLon(
                                    latitude = 1.1,
                                    longitude = 2.2
                                ),
                                orientation = StopOrientation.NORTH_EAST,
                                stopDistance = UiStopDistance.Distance.Meters(
                                    distance = 123
                                ),
                                isMapShown = true
                            ),
                            servicesItems = persistentListOf(
                                UiServicesItem.Operator.Named(
                                    operatorId = "TEST1",
                                    operatorName = "Operator 1"
                                ),
                                UiServicesItem.Service(
                                    serviceDescriptor = ServiceDescriptor(
                                        serviceName = "1",
                                        operatorCode = "TEST1"
                                    ),
                                    serviceName = UiServiceName(
                                        serviceName = "1",
                                        colours = UiServiceColours(
                                            backgroundColour = Color.Red.toArgb(),
                                            textColour = Color.White.toArgb()
                                        )
                                    ),
                                    description = "Service description"
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
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR_NO_STOP_DETAILS)
            .assertDoesNotExist()
    }

    @Test
    fun showsNoStopDetailsErrorWhenContentIsNoStopDetailsError() {
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.NoStopDetailsError
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
            .onNodeWithTag(TEST_TAG_CONTENT_ERROR_NO_STOP_DETAILS)
            .assertExists()
    }

    @Test
    fun showOnMapActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.NoStopDetailsError,
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
    fun requestLocationPermissionsActionCallsLambdaThenMarksActionAsLaunched() {
        val actionCounter = InvocationCounter()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.NoStopDetailsError,
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
    fun showLocationSettingsActionCallsLambdaThenMarksActionAsLaunched() {
        val actionCounter = InvocationCounter()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.NoStopDetailsError,
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
    fun showAppPermissionsSettingsActionCallsLambdaThenMarksActionAsLaunched() {
        val actionCounter = InvocationCounter()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsScreenWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.NoStopDetailsError,
                        action = UiAction.ShowAppPermissionsSettings
                    ),
                    onShowAppPermissionSettings = actionCounter,
                    onActionLaunched = actionLaunchedCounter
                )
            }
        }

        assertEquals(1, actionCounter.count)
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Composable
    private fun StopDetailsScreenWithStateWithDefaults(
        state: UiState,
        modifier: Modifier = Modifier,
        onStopMapClick: () -> Unit = { throw NotImplementedError() },
        onGrantPermissionsClick: () -> Unit = { throw NotImplementedError() },
        onTurnOnLocationClick: () -> Unit = { throw NotImplementedError() },
        onActionLaunched: () -> Unit = { throw NotImplementedError() },
        onShowOnMap: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRequestLocationPermissions: () -> Unit = { throw NotImplementedError() },
        onShowLocationSettings: () -> Unit = { throw NotImplementedError() },
        onShowAppPermissionSettings: () -> Unit = { throw NotImplementedError() }
    ) {
        StopDetailsScreenWithState(
            state = state,
            onStopMapClick = onStopMapClick,
            onGrantPermissionClick = onGrantPermissionsClick,
            onTurnOnLocationClick = onTurnOnLocationClick,
            onActionLaunched = onActionLaunched,
            onShowOnMap = onShowOnMap,
            onRequestLocationPermissions = onRequestLocationPermissions,
            onShowLocationSettings = onShowLocationSettings,
            onShowAppPermissionSettings = onShowAppPermissionSettings,
            modifier = modifier
        )
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
}
