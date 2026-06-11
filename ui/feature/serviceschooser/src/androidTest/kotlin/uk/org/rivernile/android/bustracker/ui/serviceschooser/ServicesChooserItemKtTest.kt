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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import androidx.activity.ComponentActivity
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertHasNoClickAction
import androidx.compose.ui.test.assertIsOff
import androidx.compose.ui.test.assertIsOn
import androidx.compose.ui.test.assertIsToggleable
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `ServicesChooserItem.kt`.
 *
 * @author Niall Scott
 */
class ServicesChooserItemKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun operatorItemDisplaysCorrectTextAndIsNotClickable() {
        composeTestRule.setContent {
            MyBusTheme {
                OperatorItem(
                    operatorName = "Operator Name"
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_OPERATOR_ITEM)
            .apply {
                assertTextEquals("Operator Name")
                assertHasNoClickAction()
            }
    }

    @Test
    fun serviceItemDisplaysCorrectText() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceItem(
                    service = UiServiceChooserItem.Service(
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
                        isSelected = false
                    ),
                    onClick = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_SERVICE_ITEM)
            .assertTextEquals("1")
    }

    @Test
    fun serviceItemIsClickable() {
        val clickCounter = ClickCounter()
        composeTestRule.setContent {
            MyBusTheme {
                ServiceItem(
                    service = UiServiceChooserItem.Service(
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
                        isSelected = false
                    ),
                    onClick = clickCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_SERVICE_ITEM)
            .apply {
                assertHasClickAction()
                performClick()
            }
        assertEquals(1, clickCounter.count)
    }

    @Test
    fun serviceItemIsNotCheckedWhenServiceIsNotSelected() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceItem(
                    service = UiServiceChooserItem.Service(
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
                        isSelected = false
                    ),
                    onClick = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_SERVICE_ITEM)
            .apply {
                assertIsToggleable()
                assertIsOff()
            }
    }

    @Test
    fun serviceItemIsNotCheckedWhenServiceIsSelected() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceItem(
                    service = UiServiceChooserItem.Service(
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
                        isSelected = true
                    ),
                    onClick = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_SERVICE_ITEM)
            .apply {
                assertIsToggleable()
                assertIsOn()
            }
    }

    private class ClickCounter : () -> Unit {

        var count = 0
            private set

        override fun invoke() {
            count++
        }
    }
}
