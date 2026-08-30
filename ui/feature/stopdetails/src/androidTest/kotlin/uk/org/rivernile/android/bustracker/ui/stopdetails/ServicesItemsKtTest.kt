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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test

/**
 * Tests for `ServicesItems.kt`.
 *
 * @author Niall Scott
 */
class ServicesItemsKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun operatorItemIsPopulatedWithOperatorName() {
        composeTestRule.setContent {
            MyBusTheme {
                OperatorItem(
                    operatorName = "Operator Name"
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_OPERATOR_ITEM)
            .assertTextEquals("Operator Name")
    }

    @Test
    fun serviceItemDisplaysServiceName() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceItem(
                    service = UiServicesItem.Service(
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
                        description = null
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_SERVICE_NAME)
            .assertTextEquals("1")
    }

    @Test
    fun serviceItemDisplaysServiceDescriptionWhenPopulated() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceItem(
                    service = UiServicesItem.Service(
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
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_SERVICE_DESCRIPTION)
            .assertTextEquals("Service description")
    }

    @Test
    fun serviceItemDisplaysFallbackTextWhenServiceDescriptionIsNull() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceItem(
                    service = UiServicesItem.Service(
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
                        description = null
                    )
                )
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(R.string.stopdetails_item_service_unknown_description)

        composeTestRule
            .onNodeWithTag(TEST_TAG_SERVICE_DESCRIPTION)
            .assertTextEquals(expectedText)
    }

    @Test
    fun serviceItemDisplaysFallbackTextWhenServiceDescriptionIsEmpty() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceItem(
                    service = UiServicesItem.Service(
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
                        description = ""
                    )
                )
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(R.string.stopdetails_item_service_unknown_description)

        composeTestRule
            .onNodeWithTag(TEST_TAG_SERVICE_DESCRIPTION)
            .assertTextEquals(expectedText)
    }

    @Test
    fun serviceItemDisplaysFallbackTextWhenServiceDescriptionIsBlank() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceItem(
                    service = UiServicesItem.Service(
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
                        description = " "
                    )
                )
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(R.string.stopdetails_item_service_unknown_description)

        composeTestRule
            .onNodeWithTag(TEST_TAG_SERVICE_DESCRIPTION)
            .assertTextEquals(expectedText)
    }

    @Test
    fun serviceItemDisplaysTrimmedTextWhenDescriptionHasWhitespace() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceItem(
                    service = UiServicesItem.Service(
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
                        description = " Service description "
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_SERVICE_DESCRIPTION)
            .assertTextEquals("Service description")
    }

    @Test
    fun noServicesItemDisplaysCorrectText() {
        composeTestRule.setContent {
            MyBusTheme {
                NoServicesItem()
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(R.string.stopdetails_no_services)

        composeTestRule
            .onNodeWithTag(TEST_TAG_NO_SERVICES_ITEM)
            .assertTextEquals(expectedText)
    }
}
