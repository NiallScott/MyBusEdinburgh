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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `ArrivalAlertItem.kt`.
 *
 * @author Niall Scott
 */
class ArrivalAlertItemKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun mapIsNotShownWhenMapShownIsFalse() {
        composeTestRule.setContent {
            MyBusTheme {
                ArrivalAlertItemWithDefaults(
                    item = createUiArrivalAlert(
                        isMapShown = false
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ARRIVAL_ALERT_MAP)
            .assertDoesNotExist()
    }

    @Test
    fun mapIsNotShownWhenMapShowIsTrueButStopDetailsIsNull() {
        composeTestRule.setContent {
            MyBusTheme {
                ArrivalAlertItemWithDefaults(
                    item = createUiArrivalAlert(
                        stopDetails = null
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ARRIVAL_ALERT_MAP)
            .assertDoesNotExist()
    }

    @Test
    fun mapIsShownWhenMapShowIsTrueAndStopDetailsIsNotNull() {
        composeTestRule.setContent {
            MyBusTheme {
                ArrivalAlertItemWithDefaults(
                    item = createUiArrivalAlert()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ARRIVAL_ALERT_MAP)
            .assertExists()
    }

    @Test
    fun titleContainsCorrectText() {
        composeTestRule.setContent {
            MyBusTheme {
                ArrivalAlertItemWithDefaults(
                    item = createUiArrivalAlert()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ARRIVAL_ALERT_TITLE)
            .assertTextEquals(
                composeTestRule
                    .activity
                    .getString(R.string.alertmanager_arrival_title)
            )
    }

    @Test
    fun descriptionContainsCorrectTextWhenTimeTriggerIsImminent() {
        composeTestRule.setContent {
            MyBusTheme {
                ArrivalAlertItemWithDefaults(
                    item = createUiArrivalAlert(
                        timeTriggerMinutes = 1
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ARRIVAL_ALERT_DESCRIPTION)
            .assertTextEquals(
                composeTestRule
                    .activity
                    .getString(
                        R.string.alertmanager_arrival_description_imminent,
                        "Stop Name, Locality (123456)"
                    )
            )
    }

    @Test
    fun descriptionContainsCorrectTextWhenTimeTriggerIsMultipleMinutes() {
        composeTestRule.setContent {
            MyBusTheme {
                ArrivalAlertItemWithDefaults(
                    item = createUiArrivalAlert(
                        timeTriggerMinutes = 2
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ARRIVAL_ALERT_DESCRIPTION)
            .assertTextEquals(
                composeTestRule
                    .activity
                    .resources
                    .getQuantityString(
                        R.plurals.alertmanager_arrival_description_minutes,
                        2,
                        2,
                        "Stop Name, Locality (123456)"
                    )
            )
    }

    @Test
    fun servicesListingShowsServices() {
        composeTestRule.setContent {
            MyBusTheme {
                ArrivalAlertItemWithDefaults(
                    item = createUiArrivalAlert(
                        timeTriggerMinutes = 2
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_ARRIVAL_ALERT_SERVICE_LISTING,
                useUnmergedTree = true
            )
            .apply {
                onChildAt(0)
                    .assertTextEquals("1")
                onChildAt(1)
                    .assertTextEquals("2")
                onChildAt(2)
                    .assertTextEquals("3")
            }
    }

    @Test
    fun supplementaryTextContainsCorrectText() {
        composeTestRule.setContent {
            MyBusTheme {
                ArrivalAlertItemWithDefaults(
                    item = createUiArrivalAlert()
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ARRIVAL_ALERT_SUPPLEMENTARY_TEXT)
            .assertTextEquals(
                composeTestRule
                    .activity
                    .getString(R.string.alertmanager_arrival_supplementary)
            )
    }

    @Test
    fun removeButtonContainsCorrectTextAndFiresClickLambda() {
        var removeButtonClickCounter = 0
        composeTestRule.setContent {
            MyBusTheme {
                ArrivalAlertItemWithDefaults(
                    item = createUiArrivalAlert(),
                    onRemoveAlertClick = { removeButtonClickCounter++ }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ARRIVAL_ALERT_REMOVE_BUTTON)
            .apply {
                assertTextEquals(
                    composeTestRule
                        .activity
                        .getString(R.string.alertmanager_btn_remove)
                )
                performClick()
            }
        assertEquals(1, removeButtonClickCounter)
    }

    @Composable
    private fun ArrivalAlertItemWithDefaults(
        item: UiAlert.ArrivalAlert,
        onRemoveAlertClick: () -> Unit = { throw NotImplementedError() }
    ) {
        CompositionLocalProvider(
            // Set so that the real Google Map is not used in tests.
            LocalInspectionMode provides true
        ) {
            ArrivalAlertItem(
                item = item,
                onRemoveAlertClick = onRemoveAlertClick,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }

    private fun createUiArrivalAlert(
        id: Int = 1,
        stopIdentifier: StopIdentifier = "123456".toNaptanStopIdentifier(),
        stopDetails: UiStopDetails? = UiStopDetails(
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
        services: ImmutableList<UiServiceName> = persistentListOf(
            UiServiceName(
                serviceName = "1",
                colours = null
            ),
            UiServiceName(
                serviceName = "2",
                colours = null
            ),
            UiServiceName(
                serviceName = "3",
                colours = null
            )
        ),
        timeTriggerMinutes: Int = 5,
        isMapShown: Boolean = true
    ): UiAlert.ArrivalAlert {
        return UiAlert.ArrivalAlert(
            id = id,
            stopIdentifier = stopIdentifier,
            stopDetails = stopDetails,
            services = services,
            timeTriggerMinutes = timeTriggerMinutes,
            isMapShown = isMapShown
        )
    }
}
