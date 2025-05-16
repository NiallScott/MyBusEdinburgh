/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.text

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.SemanticsNodeInteraction
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.assertWidthIsAtLeast
import androidx.compose.ui.test.getUnclippedBoundsInRoot
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onParent
import androidx.compose.ui.unit.height
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test

/**
 * Tests for `DecoratedServiceName.kt`.
 *
 * @author Niall Scott
 */
class DecoratedServiceNameKtTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun smallDecoratedServiceNameTextRendersItemWithNoColoursSpecified() {
        composeTestRule.setContent {
            MyBusTheme {
                SmallDecoratedServiceNameText(
                    service = UiServiceName(
                        serviceName = "1"
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithText(
                text = "1",
                useUnmergedTree = true
            )
            .onParent()
            .apply {
                assertIsDisplayed()
                assertWidthIsAtLeastHeight()
            }
    }

    @Test
    fun smallDecoratedServiceNameTextRendersItemWhenColoursAreSpecified() {
        composeTestRule.setContent {
            MyBusTheme {
                SmallDecoratedServiceNameText(
                    service = UiServiceName(
                        serviceName = "1",
                        colours = UiServiceColours(
                            backgroundColour = Color.Red.toArgb(),
                            textColour = Color.White.toArgb()
                        )
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithText(
                text = "1",
                useUnmergedTree = true
            )
            .onParent()
            .apply {
                assertIsDisplayed()
                assertWidthIsAtLeastHeight()
            }
    }

    @Test
    fun smallDecoratedServiceNamesListingTextDoesNotShowAnythingWhenServicesIsEmpty() {
        composeTestRule.setContent {
            MyBusTheme {
                SmallDecoratedServiceNamesListingText(
                    services = persistentListOf()
                )
            }
        }

        composeTestRule
            .onAllNodesWithTag(
                testTag = TEST_TAG_DECORATED_SERVICE_NAME,
                useUnmergedTree = true
            )
            .assertCountEquals(0)
    }

    @Test
    fun smallDecoratedServiceNamesListingTextShowsSingleItemWhenOneServiceIsPresent() {
        composeTestRule.setContent {
            MyBusTheme {
                SmallDecoratedServiceNamesListingText(
                    services = persistentListOf(
                        UiServiceName(
                            serviceName = "1"
                        )
                    )
                )
            }
        }

        composeTestRule
            .onAllNodesWithTag(
                testTag = TEST_TAG_DECORATED_SERVICE_NAME,
                useUnmergedTree = true
            )
            .apply {
                assertCountEquals(1)

                this[0].assertTextEquals("1")
                this[0].onParent().assertWidthIsAtLeastHeight()
            }
    }

    @Test
    fun smallDecoratedServiceNamesListingTextShows3ItemsWhen3ServicesArePresent() {
        val expectedText = listOf("1", "2", "3")

        composeTestRule.setContent {
            MyBusTheme {
                SmallDecoratedServiceNamesListingText(
                    services = persistentListOf(
                        UiServiceName(
                            serviceName = "1"
                        ),
                        UiServiceName(
                            serviceName = "2"
                        ),
                        UiServiceName(
                            serviceName = "3"
                        )
                    )
                )
            }
        }

        composeTestRule
            .onAllNodesWithTag(
                testTag = TEST_TAG_DECORATED_SERVICE_NAME,
                useUnmergedTree = true
            )
            .apply {
                assertCountEquals(3)

                expectedText.forEachIndexed { index, s ->
                    this[index].assertTextEquals(s)
                    this[index].onParent().assertWidthIsAtLeastHeight()
                }
            }
    }

    private fun SemanticsNodeInteraction.assertWidthIsAtLeastHeight() {
        assertWidthIsAtLeast(getUnclippedBoundsInRoot().height)
    }
}