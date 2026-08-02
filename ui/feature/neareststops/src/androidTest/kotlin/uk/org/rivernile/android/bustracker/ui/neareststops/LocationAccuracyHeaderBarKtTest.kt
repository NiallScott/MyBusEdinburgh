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
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.filter
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `LocationAccuracyHeaderBar.kt`.
 *
 * @author Niall Scott
 */
class LocationAccuracyHeaderBarKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun locationAccuracyHeaderBarWithGpsNotPresentShowsCorrectContent() {
        composeTestRule.setContent {
            MyBusTheme {
                LocationAccuracyHeaderBarWithDefaults(
                    locationAccuracy = UiLocationAccuracy.GPS_NOT_PRESENT
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_GPS_NOT_PRESENT)
            .apply {
                assertExists()
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_LOCATION_ACCURACY_HEADER_TEXT))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.neareststops_location_accuracy_no_gps)
                    )

                onChildren()
                    .filter(hasTestTag(TEST_TAG_LOCATION_ACCURACY_HEADER_RESOLVE_BUTTON))
                    .assertCountEquals(0)
            }
    }

    @Test
    fun locationAccuracyHeaderBarWithPermissionsNotSufficientShowsCorrectContent() {
        val invocationCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                LocationAccuracyHeaderBarWithDefaults(
                    locationAccuracy = UiLocationAccuracy.PERMISSIONS_NOT_SUFFICIENT,
                    onShowAppSettingsClick = invocationCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_PERMISSIONS_NOT_SUFFICIENT)
            .apply {
                assertExists()
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_LOCATION_ACCURACY_HEADER_TEXT))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(
                                R.string.neareststops_location_accuracy_insufficient_permission
                            )
                    )

                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_LOCATION_ACCURACY_HEADER_RESOLVE_BUTTON))
                    .apply {
                        assertTextEquals(
                            composeTestRule
                                .activity
                                .getString(
                                    R.string.neareststops_location_accuracy_button_open_settings
                                )
                        )
                        performClick()
                    }
            }
        assertEquals(1, invocationCounter.count)
    }

    @Test
    fun locationAccuracyHeaderBarWithGpsDisabledShowsCorrectContent() {
        val invocationCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                LocationAccuracyHeaderBarWithDefaults(
                    locationAccuracy = UiLocationAccuracy.GPS_DISABLED,
                    onShowSystemLocationSettingsClick = invocationCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_GPS_DISABLED)
            .apply {
                assertExists()
                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_LOCATION_ACCURACY_HEADER_TEXT))
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(
                                R.string.neareststops_location_accuracy_gps_disabled
                            )
                    )

                onChildren()
                    .filterToOne(hasTestTag(TEST_TAG_LOCATION_ACCURACY_HEADER_RESOLVE_BUTTON))
                    .apply {
                        assertTextEquals(
                            composeTestRule
                                .activity
                                .getString(
                                    R.string.neareststops_location_accuracy_button_open_settings
                                )
                        )
                        performClick()
                    }
            }
        assertEquals(1, invocationCounter.count)
    }

    @Composable
    private fun LocationAccuracyHeaderBarWithDefaults(
        locationAccuracy: UiLocationAccuracy,
        onShowAppSettingsClick: () -> Unit = { throw NotImplementedError() },
        onShowSystemLocationSettingsClick: () -> Unit = { throw NotImplementedError() }
    ) {
        LocationAccuracyHeaderBar(
            locationAccuracy = locationAccuracy,
            onShowAppSettingsClick = onShowAppSettingsClick,
            onShowSystemLocationSettingsClick = onShowSystemLocationSettingsClick
        )
    }

    private class InvocationCounter : () -> Unit {

        var count = 0
            private set

        override fun invoke() {
            count++
        }
    }
}
