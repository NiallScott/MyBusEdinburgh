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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.busstops.toContentDescriptionStringResId
import uk.org.rivernile.android.bustracker.core.domain.toAtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.ui.formatters.LocalNumberFormatter
import uk.org.rivernile.android.bustracker.ui.formatters.rememberNumberFormatter
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import java.text.NumberFormat
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 *  Tests for `StopDetailsItem.kt`.
 *
 * @author Niall Scott
 */
class StopDetailsItemTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun stopDetailsItemWithIsMapShowAsFalseDoesNotShowMap() {
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsItemWithDefaults(
                    stopDetails = UiStopDetails(
                        naptanCode = "123456".toNaptanStopIdentifier(),
                        atcoCode = "ATCO987654".toAtcoStopIdentifier(),
                        latLon = UiLatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        orientation = StopOrientation.NORTH_EAST,
                        stopDistance = null,
                        isMapShown = false
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_MAP)
            .assertDoesNotExist()
    }

    @Test
    fun stopDetailsItemWithIsMapShowAsTrueShowsMapAndIsClickable() {
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsItemWithDefaults(
                    stopDetails = UiStopDetails(
                        naptanCode = "123456".toNaptanStopIdentifier(),
                        atcoCode = "ATCO987654".toAtcoStopIdentifier(),
                        latLon = UiLatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        orientation = StopOrientation.NORTH_EAST,
                        stopDistance = null,
                        isMapShown = true
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_MAP)
            .assertExists()
    }

    @Test
    fun stopDetailsItemPopulatesNaptanCode() {
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsItemWithDefaults(
                    stopDetails = UiStopDetails(
                        naptanCode = "123456".toNaptanStopIdentifier(),
                        atcoCode = "ATCO987654".toAtcoStopIdentifier(),
                        latLon = UiLatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        orientation = StopOrientation.NORTH_EAST,
                        stopDistance = null,
                        isMapShown = true
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_NAPTAN)
            .apply {
                assertExists()

                onChildAt(0)
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.stopdetails_naptan_identifier_label)
                    )
                onChildAt(1)
                    .assertTextEquals("123456")
            }
    }

    @Test
    fun stopDetailsItemPopulatesAtcoCode() {
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsItemWithDefaults(
                    stopDetails = UiStopDetails(
                        naptanCode = "123456".toNaptanStopIdentifier(),
                        atcoCode = "ATCO987654".toAtcoStopIdentifier(),
                        latLon = UiLatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        orientation = StopOrientation.NORTH_EAST,
                        stopDistance = null,
                        isMapShown = true
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_ATCO)
            .apply {
                assertExists()

                onChildAt(0)
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.stopdetails_atco_identifier_label)
                    )
                onChildAt(1)
                    .assertTextEquals("ATCO987654")
            }
    }

    @Test
    fun stopDetailsItemPopulatesOrientation() {
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsItemWithDefaults(
                    stopDetails = UiStopDetails(
                        naptanCode = "123456".toNaptanStopIdentifier(),
                        atcoCode = "ATCO987654".toAtcoStopIdentifier(),
                        latLon = UiLatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        orientation = StopOrientation.NORTH_EAST,
                        stopDistance = null,
                        isMapShown = true
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_ORIENTATION)
            .apply {
                assertExists()

                onChildAt(0)
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.stopdetails_stop_orientation_label)
                    )
                onChildAt(1)
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(StopOrientation.NORTH_EAST.toContentDescriptionStringResId())
                    )
            }
    }

    @Test
    fun stopDetailsItemDoesNotPopulateDistanceWhenDistanceIsNull() {
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsItemWithDefaults(
                    stopDetails = UiStopDetails(
                        naptanCode = "123456".toNaptanStopIdentifier(),
                        atcoCode = "ATCO987654".toAtcoStopIdentifier(),
                        latLon = UiLatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        orientation = StopOrientation.NORTH_EAST,
                        stopDistance = null,
                        isMapShown = true
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_DISTANCE)
            .assertDoesNotExist()
    }

    @Test
    fun stopDetailsItemPopulatesDistanceItemWithInsufficientLocationPermissions() {
        val invocationCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsItemWithDefaults(
                    stopDetails = UiStopDetails(
                        naptanCode = "123456".toNaptanStopIdentifier(),
                        atcoCode = "ATCO987654".toAtcoStopIdentifier(),
                        latLon = UiLatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        orientation = StopOrientation.NORTH_EAST,
                        stopDistance = UiStopDistance.InsufficientLocationPermissions,
                        isMapShown = true
                    ),
                    onGrantPermissionClick = invocationCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_DISTANCE)
            .apply {
                assertExists()

                onChildAt(0)
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.stopdetails_stop_distance_label)
                    )

                onChildAt(1)
                    .apply {
                        assertTextEquals(
                            composeTestRule
                                .activity
                                .getString(R.string.stopdetails_stop_distance_insufficient_permissions_btn_grant)
                        )
                        assertHasClickAction()
                        performClick()
                    }
            }
        assertEquals(1, invocationCounter.count)
    }

    @Test
    fun stopDetailsItemPopulatesDistanceItemWithLocationOff() {
        val invocationCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsItemWithDefaults(
                    stopDetails = UiStopDetails(
                        naptanCode = "123456".toNaptanStopIdentifier(),
                        atcoCode = "ATCO987654".toAtcoStopIdentifier(),
                        latLon = UiLatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        orientation = StopOrientation.NORTH_EAST,
                        stopDistance = UiStopDistance.LocationOff,
                        isMapShown = true
                    ),
                    onTurnOnLocationClick = invocationCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_DISTANCE)
            .apply {
                assertExists()

                onChildAt(0)
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.stopdetails_stop_distance_label)
                    )

                onChildAt(1)
                    .apply {
                        assertTextEquals(
                            composeTestRule
                                .activity
                                .getString(R.string.stopdetails_stop_distance_location_off_btn_turn_on)
                        )
                        assertHasClickAction()
                        performClick()
                    }
            }
        assertEquals(1, invocationCounter.count)
    }

    @Test
    fun stopDetailsItemPopulatesDistanceItemWithObtainingLocation() {
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsItemWithDefaults(
                    stopDetails = UiStopDetails(
                        naptanCode = "123456".toNaptanStopIdentifier(),
                        atcoCode = "ATCO987654".toAtcoStopIdentifier(),
                        latLon = UiLatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        orientation = StopOrientation.NORTH_EAST,
                        stopDistance = UiStopDistance.ObtainingLocation,
                        isMapShown = true
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_DISTANCE)
            .apply {
                assertExists()

                onChildAt(0)
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.stopdetails_stop_distance_label)
                    )

                onChildAt(1)
                    .apply {
                        assertTextEquals(
                            composeTestRule
                                .activity
                                .getString(R.string.stopdetails_stop_distance_obtaining_location)
                        )
                        assertHasNoClickAction()
                    }
            }
    }

    @Test
    fun stopDetailsItemPopulatesDistanceItemWithLocationUnknown() {
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsItemWithDefaults(
                    stopDetails = UiStopDetails(
                        naptanCode = "123456".toNaptanStopIdentifier(),
                        atcoCode = "ATCO987654".toAtcoStopIdentifier(),
                        latLon = UiLatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        orientation = StopOrientation.NORTH_EAST,
                        stopDistance = UiStopDistance.LocationUnknown,
                        isMapShown = true
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_DISTANCE)
            .apply {
                assertExists()

                onChildAt(0)
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.stopdetails_stop_distance_label)
                    )

                onChildAt(1)
                    .apply {
                        assertTextEquals(
                            composeTestRule
                                .activity
                                .getString(R.string.stopdetails_stop_distance_unknown)
                        )
                        assertHasNoClickAction()
                    }
            }
    }

    @Test
    fun stopDetailsItemPopulatesDistanceItemWithDistanceInMeters() {
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsItemWithDefaults(
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
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_DISTANCE)
            .apply {
                assertExists()

                onChildAt(0)
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.stopdetails_stop_distance_label)
                    )

                onChildAt(1)
                    .apply {
                        assertTextEquals(
                            composeTestRule
                                .activity
                                .getString(
                                    R.string.stopdetails_stop_distance_format_ms,
                                    NumberFormat
                                        .getInstance()
                                        .apply {
                                            maximumFractionDigits = 2
                                        }
                                        .format(123)
                                )
                        )
                        assertHasNoClickAction()
                    }
            }
    }

    @Test
    fun stopDetailsItemPopulatesDistanceItemWithDistanceInKilometers() {
        composeTestRule.setContent {
            MyBusTheme {
                StopDetailsItemWithDefaults(
                    stopDetails = UiStopDetails(
                        naptanCode = "123456".toNaptanStopIdentifier(),
                        atcoCode = "ATCO987654".toAtcoStopIdentifier(),
                        latLon = UiLatLon(
                            latitude = 1.1,
                            longitude = 2.2
                        ),
                        orientation = StopOrientation.NORTH_EAST,
                        stopDistance = UiStopDistance.Distance.Kilometers(
                            distance = 1.23456f
                        ),
                        isMapShown = true
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_DISTANCE)
            .apply {
                assertExists()

                onChildAt(0)
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.stopdetails_stop_distance_label)
                    )

                onChildAt(1)
                    .apply {
                        assertTextEquals(
                            composeTestRule
                                .activity
                                .getString(
                                    R.string.stopdetails_stop_distance_format_kms,
                                    NumberFormat
                                        .getInstance()
                                        .apply {
                                            maximumFractionDigits = 2
                                        }
                                        .format(1.23456f)
                                )
                        )
                        assertHasNoClickAction()
                    }
            }
    }

    @Composable
    private fun StopDetailsItemWithDefaults(
        stopDetails: UiStopDetails,
        modifier: Modifier = Modifier,
        onStopMapClick: () -> Unit = { throw NotImplementedError() },
        onGrantPermissionClick: () -> Unit = { throw NotImplementedError() },
        onTurnOnLocationClick: () -> Unit = { throw NotImplementedError() }
    ) {
        CompositionLocalProvider(
            LocalNumberFormatter provides rememberNumberFormatter(
                maximumFractionDigits = 2
            ),
            // This is set so the real Google Map is not used during the test.
            LocalInspectionMode provides true
        ) {
            StopDetailsItem(
                stopDetails = stopDetails,
                onStopMapClick = onStopMapClick,
                onGrantPermissionClick = onGrantPermissionClick,
                onTurnOnLocationClick = onTurnOnLocationClick,
                modifier = modifier
                    .fillMaxWidth()
            )
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
