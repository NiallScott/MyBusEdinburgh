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

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `AlertManagerScreen.kt`.
 *
 * @author Niall Scott
 */
class AlertManagerScreenKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsInProgressWhenContentIsInProgress() {
        composeTestRule.setContent {
            AlertManagerScreenWithStateWithDefaults(
                state = UiState(
                    content = UiContent.InProgress
                )
            )
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertExists()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_CONTENT)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_EMPTY_ALERTS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_NO_ALERTS_FEATURE)
            .assertDoesNotExist()
    }

    @Test
    fun showsContentWhenContentIsContent() {
        composeTestRule.setContent {
            AlertManagerScreenWithStateWithDefaults(
                state = UiState(
                    content = UiContent.Content(
                        persistentListOf(
                            UiAlert.ProximityAlert(
                                id = 2,
                                stopIdentifier = "123456".toNaptanStopIdentifier(),
                                stopDetails = UiStopDetails(
                                    stopName = UiStopName(
                                        name = "Stop Name",
                                        locality = "Locality"
                                    ),
                                    latLon = UiLatLon(
                                        latitude = 1.1,
                                        longitude = 2.2
                                    ),
                                    orientation = StopOrientation.NORTH_EAST
                                ),
                                distanceFromMeters = 250,
                                isMapShown = true
                            )
                        )
                    )
                )
            )
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_CONTENT)
            .assertExists()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_EMPTY_ALERTS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_NO_ALERTS_FEATURE)
            .assertDoesNotExist()
    }

    @Test
    fun showsEmptyAlertsErrorWhenContentIsEmptyAlerts() {
        composeTestRule.setContent {
            AlertManagerScreenWithStateWithDefaults(
                state = UiState(
                    content = UiContent.EmptyAlerts
                )
            )
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_CONTENT)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_EMPTY_ALERTS)
            .assertExists()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_NO_ALERTS_FEATURE)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_TITLE)
            .assertTextEquals(
                composeTestRule
                    .activity
                    .getString(R.string.alertmanager_error_no_alerts_title)
            )
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_DESCRIPTION)
            .assertTextEquals(
                composeTestRule
                    .activity
                    .getString(R.string.alertmanager_error_no_alerts_description)
            )
    }

    @Test
    fun showsNoAlertsFeatureErrorWhenContentIsNoAlertsFeature() {
        composeTestRule.setContent {
            AlertManagerScreenWithStateWithDefaults(
                state = UiState(
                    content = UiContent.NoAlertsFeature
                )
            )
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_CONTENT)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_EMPTY_ALERTS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_NO_ALERTS_FEATURE)
            .assertExists()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_TITLE)
            .assertTextEquals(
                composeTestRule
                    .activity
                    .getString(R.string.alertmanager_error_no_feature_title)
            )
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_DESCRIPTION)
            .assertTextEquals(
                composeTestRule
                    .activity
                    .getString(R.string.alertmanager_error_no_feature_description)
            )
    }

    @Test
    fun showRemoveArrivalAlertActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            AlertManagerScreenWithStateWithDefaults(
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

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showRemoveProximityAlertActionCallsLambdaThenMarksActionAsLaunched() {
        val actionTracker = Tracker<StopIdentifier>()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            AlertManagerScreenWithStateWithDefaults(
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

        assertEquals(
            listOf("123456".toNaptanStopIdentifier()),
            actionTracker.observedValues
        )
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun showLocationSettingsActionCallsLambdaThenMarksActionAsLaunched() {
        val actionCounter = InvocationCounter()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            AlertManagerScreenWithStateWithDefaults(
                state = UiState(
                    content = UiContent.InProgress,
                    action = UiAction.ShowLocationSettings
                ),
                onShowLocationSettings = actionCounter,
                onActionLaunched = actionLaunchedCounter
            )
        }

        assertEquals(1, actionCounter.count)
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Composable
    private fun AlertManagerScreenWithStateWithDefaults(
        state: UiState,
        onRemoveArrivalAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onRemoveProximityAlertClick: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onLocationSettingsClick: () -> Unit = { throw NotImplementedError() },
        onActionLaunched: () -> Unit = { throw NotImplementedError() },
        onShowRemoveArrivalAlert: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onShowRemoveProximityAlert: (StopIdentifier) -> Unit = { throw NotImplementedError() },
        onShowLocationSettings: () -> Unit = { throw NotImplementedError() }
    ) {
        CompositionLocalProvider(
            // Set so that the real Google Map is not used in tests.
            LocalInspectionMode provides true
        ) {
            AlertManagerScreenWithState(
                state = state,
                onRemoveArrivalAlertClick = onRemoveArrivalAlertClick,
                onRemoveProximityAlertClick = onRemoveProximityAlertClick,
                onLocationSettingsClick = onLocationSettingsClick,
                onActionLaunched = onActionLaunched,
                onShowRemoveArrivalAlert = onShowRemoveArrivalAlert,
                onShowRemoveProximityAlert = onShowRemoveProximityAlert,
                onShowLocationSettings = onShowLocationSettings
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
}
