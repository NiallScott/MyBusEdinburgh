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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onChildAt
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performClick
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.busstops.toContentDescriptionStringResId
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `NearestStopItem.kt`.
 *
 * @author Niall Scott
 */
class NearestStopItemKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun markerIconHasContentDescription() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemWithDefaults(
                    nearestStop = UiNearestStop(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        stopName = null,
                        services = null,
                        orientation = StopOrientation.NORTH_EAST,
                        distanceMeters = 123,
                        dropdownMenu = UiNearestStopDropdownMenu()
                    )
                )
            }
        }
        val expectedContentDescription = composeTestRule
            .activity
            .getString(StopOrientation.NORTH_EAST.toContentDescriptionStringResId())

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_STOP_MARKER_ICON,
                useUnmergedTree = true
            )
            .assertContentDescriptionEquals(expectedContentDescription)
    }

    @Test
    fun stopNameTextIsPopulatedWithStopNameAndCode() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemWithDefaults(
                    nearestStop = UiNearestStop(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        stopName = UiStopName(
                            name = "Stop Name",
                            locality = "Locality"
                        ),
                        services = null,
                        orientation = StopOrientation.NORTH_EAST,
                        distanceMeters = 123,
                        dropdownMenu = UiNearestStopDropdownMenu()
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_NEAREST_STOP_NAME,
                useUnmergedTree = true
            )
            .assertTextEquals("Stop Name, Locality (123456)")
    }

    @Test
    fun servicesListingNotShownWhenServicesINull() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemWithDefaults(
                    nearestStop = UiNearestStop(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        stopName = UiStopName(
                            name = "Stop Name",
                            locality = "Locality"
                        ),
                        services = null,
                        orientation = StopOrientation.NORTH_EAST,
                        distanceMeters = 123,
                        dropdownMenu = UiNearestStopDropdownMenu()
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_SERVICES_LISTING,
                useUnmergedTree = true
            )
            .assertDoesNotExist()
    }

    @Test
    fun servicesListingShownWhenServicesIsNotNull() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemWithDefaults(
                    nearestStop = UiNearestStop(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        stopName = UiStopName(
                            name = "Stop Name",
                            locality = "Locality"
                        ),
                        services = persistentListOf(
                            UiServiceName(
                                serviceName = "1",
                                colours = UiServiceColours(
                                    backgroundColour = Color.Red.toArgb(),
                                    textColour = Color.White.toArgb()
                                )
                            ),
                            UiServiceName(
                                serviceName = "2",
                                colours = null
                            ),
                            UiServiceName(
                                serviceName = "3",
                                colours = UiServiceColours(
                                    backgroundColour = Color.Yellow.toArgb(),
                                    textColour = Color.Black.toArgb()
                                )
                            )
                        ),
                        orientation = StopOrientation.NORTH_EAST,
                        distanceMeters = 123,
                        dropdownMenu = UiNearestStopDropdownMenu()
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_SERVICES_LISTING,
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
    fun distanceTextIsPopulatedWithCorrectText() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemWithDefaults(
                    nearestStop = UiNearestStop(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        stopName = UiStopName(
                            name = "Stop Name",
                            locality = "Locality"
                        ),
                        services = null,
                        orientation = StopOrientation.NORTH_EAST,
                        distanceMeters = 123,
                        dropdownMenu = UiNearestStopDropdownMenu()
                    )
                )
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(R.string.neareststops_distance_format, 123)

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_DISTANCE_TEXT,
                useUnmergedTree = true
            )
            .assertTextEquals(expectedText)
    }

    @Test
    fun dropdownIndicatorHasContentDescription() {
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemWithDefaults(
                    nearestStop = UiNearestStop(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        stopName = UiStopName(
                            name = "Stop Name",
                            locality = "Locality"
                        ),
                        services = null,
                        orientation = StopOrientation.NORTH_EAST,
                        distanceMeters = 123,
                        dropdownMenu = UiNearestStopDropdownMenu()
                    )
                )
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(R.string.neareststops_dropdown_content_description)

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_DROPDOWN_INDICATOR,
                useUnmergedTree = true
            )
            .apply {
                assertExists()

                onChild()
                    .assertContentDescriptionEquals(expectedText)
            }
    }

    @Test
    fun dropdownIndicatorInvokesCallbackOnClick() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemWithDefaults(
                    nearestStop = UiNearestStop(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        stopName = UiStopName(
                            name = "Stop Name",
                            locality = "Locality"
                        ),
                        services = null,
                        orientation = StopOrientation.NORTH_EAST,
                        distanceMeters = 123,
                        dropdownMenu = UiNearestStopDropdownMenu()
                    ),
                    onOpenDropdownMenuClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_DROPDOWN_INDICATOR,
                useUnmergedTree = true
            )
            .performClick()

        assertEquals(1, itemClickedCounter.count)
    }

    @Test
    fun nearestStopItemClickInvokesClickHandler() {
        val itemClickedCounter = ItemClickedCounter()
        composeTestRule.setContent {
            MyBusTheme {
                NearestStopItemWithDefaults(
                    nearestStop = UiNearestStop(
                        stopIdentifier = "123456".toNaptanStopIdentifier(),
                        stopName = UiStopName(
                            name = "Stop Name",
                            locality = "Locality"
                        ),
                        services = null,
                        orientation = StopOrientation.NORTH_EAST,
                        distanceMeters = 123,
                        dropdownMenu = UiNearestStopDropdownMenu()
                    ),
                    onClick = itemClickedCounter
                )
            }
        }

        composeTestRule
            .onRoot()
            .performClick()

        assertEquals(1, itemClickedCounter.count)
    }

    @Composable
    private fun NearestStopItemWithDefaults(
        nearestStop: UiNearestStop,
        onClick: () -> Unit = { throw NotImplementedError() },
        onOpenDropdownMenuClick: () -> Unit = { throw NotImplementedError() },
        onDropdownMenuDismissed: () -> Unit = { throw NotImplementedError() },
        onAddFavouriteStopClick: () -> Unit = { throw NotImplementedError() },
        onRemoveFavouriteStopClick: () -> Unit = { throw NotImplementedError() },
        onAddArrivalAlertClick: () -> Unit = { throw NotImplementedError() },
        onRemoveArrivalAlertClick: () -> Unit = { throw NotImplementedError() },
        onAddProximityAlertClick: () -> Unit = { throw NotImplementedError() },
        onRemoveProximityAlertClick: () -> Unit = { throw NotImplementedError() },
        onShowOnMapClick: () -> Unit = { throw NotImplementedError() }
    ) {
        NearestStopItem(
            nearestStop = nearestStop,
            onClick = onClick,
            onOpenDropdownMenuClick = onOpenDropdownMenuClick,
            onDropdownMenuDismissed = onDropdownMenuDismissed,
            onAddFavouriteStopClick = onAddFavouriteStopClick,
            onRemoveFavouriteStopClick = onRemoveFavouriteStopClick,
            onAddArrivalAlertClick = onAddArrivalAlertClick,
            onRemoveArrivalAlertClick = onRemoveArrivalAlertClick,
            onAddProximityAlertClick = onAddProximityAlertClick,
            onRemoveProximityAlertClick = onRemoveProximityAlertClick,
            onShowOnMapClick = onShowOnMapClick
        )
    }

    private class ItemClickedCounter : () -> Unit {

        var count = 0
            private set

        override fun invoke() {
            count++
        }
    }
}
