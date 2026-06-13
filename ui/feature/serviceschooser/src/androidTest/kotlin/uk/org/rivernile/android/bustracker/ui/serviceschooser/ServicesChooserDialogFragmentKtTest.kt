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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toParcelableServiceDescriptor
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `ServicesChooserDialogFragment.kt`.
 *
 * @author Niall Scott
 */
class ServicesChooserDialogFragmentKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsProgressWhenContentIsInProgress() {
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithState(
                    state = UiState(
                        content = UiContent.InProgress,
                        isClearAllButtonEnabled = false
                    ),
                    onServiceClick = { },
                    onClearAllButtonEnabledStateChanged = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_CONTENT)
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_NO_SERVICES_GLOBAL)
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_NO_SERVICES_FOR_STOP)
            .assertIsNotDisplayed()
    }

    @Test
    fun showsContentWhenContentIsContent() {
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithState(
                    state = UiState(
                        content = UiContent.Content(
                            items = persistentListOf(
                                UiServiceChooserItem.Operator.Named(
                                    operatorId = "TEST1",
                                    operatorName = "First Operator"
                                ),
                                UiServiceChooserItem.Service(
                                    serviceDescriptor = ServiceDescriptor("1", "TEST1"),
                                    serviceName = UiServiceName(
                                        serviceName = "1",
                                        colours = UiServiceColours(
                                            backgroundColour = Color.Red.toArgb(),
                                            textColour = Color.White.toArgb()
                                        )
                                    ),
                                    isSelected = false
                                )
                            )
                        ),
                        isClearAllButtonEnabled = false
                    ),
                    onServiceClick = { },
                    onClearAllButtonEnabledStateChanged = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_CONTENT)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_NO_SERVICES_GLOBAL)
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_NO_SERVICES_FOR_STOP)
            .assertIsNotDisplayed()
    }

    @Test
    fun showsNoGlobalServicesErrorWhenContentIsNoGlobalServicesError() {
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithState(
                    state = UiState(
                        content = UiContent.Error.NoGlobalServices,
                        isClearAllButtonEnabled = false
                    ),
                    onServiceClick = { },
                    onClearAllButtonEnabledStateChanged = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_CONTENT)
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_NO_SERVICES_GLOBAL)
            .assertIsDisplayed()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_NO_SERVICES_FOR_STOP)
            .assertIsNotDisplayed()
    }

    @Test
    fun showsNoServicesForStopErrorWhenContentIsNoServicesForStopError() {
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithState(
                    state = UiState(
                        content = UiContent.Error.NoServicesForStop,
                        isClearAllButtonEnabled = false
                    ),
                    onServiceClick = { },
                    onClearAllButtonEnabledStateChanged = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_CONTENT)
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_NO_SERVICES_GLOBAL)
            .assertIsNotDisplayed()
        composeTestRule
            .onNodeWithTag(TEST_TAG_ERROR_NO_SERVICES_FOR_STOP)
            .assertIsDisplayed()
    }

    @Test
    fun clickingOnServiceInvokesOnServiceClickLambda() {
        var serviceItemClickCount = 0
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithState(
                    state = UiState(
                        content = UiContent.Content(
                            items = persistentListOf(
                                UiServiceChooserItem.Operator.Named(
                                    operatorId = "TEST1",
                                    operatorName = "First Operator"
                                ),
                                UiServiceChooserItem.Service(
                                    serviceDescriptor = ServiceDescriptor("1", "TEST1"),
                                    serviceName = UiServiceName(
                                        serviceName = "1",
                                        colours = UiServiceColours(
                                            backgroundColour = Color.Red.toArgb(),
                                            textColour = Color.White.toArgb()
                                        )
                                    ),
                                    isSelected = false
                                )
                            )
                        ),
                        isClearAllButtonEnabled = false
                    ),
                    onServiceClick = { serviceItemClickCount++ },
                    onClearAllButtonEnabledStateChanged = { }
                )
            }
        }

        composeTestRule
            .onNode(hasTestTag(TEST_TAG_SERVICE_ITEM) and hasText("1"))
            .performClick()
        assertEquals(1, serviceItemClickCount)
    }

    @Test
    fun clearAllButtonStateIsPropagated() {
        val clearAllStates = mutableListOf<Boolean>()
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithState(
                    state = UiState(
                        content = UiContent.InProgress,
                        isClearAllButtonEnabled = false
                    ),
                    onServiceClick = { },
                    onClearAllButtonEnabledStateChanged = { clearAllStates += it }
                )
            }
        }

        assertEquals(listOf(false), clearAllStates)
    }

    @Test
    fun topScrollHorizontalDividerIsNotShownWhenContentIsNotScrolled() {
        composeTestRule.setContent {
            MyBusTheme {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    ServicesChooserDialogContentWithState(
                        state = UiState(
                            content = scrollContent,
                            isClearAllButtonEnabled = false
                        ),
                        onServiceClick = { },
                        onClearAllButtonEnabledStateChanged = { }
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_TOP_SCROLL_HORIZONTAL_DIVIDER)
            .assertIsNotDisplayed()
    }

    @Test
    fun topScrollHorizontalDividerIsShownWhenContentIsScrolled() {
        composeTestRule.setContent {
            MyBusTheme {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    ServicesChooserDialogContentWithState(
                        state = UiState(
                            content = scrollContent,
                            isClearAllButtonEnabled = false
                        ),
                        onServiceClick = { },
                        onClearAllButtonEnabledStateChanged = { }
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_GRID)
            .performScrollToKey("TEST2")
        composeTestRule
            .onNodeWithTag(TEST_TAG_TOP_SCROLL_HORIZONTAL_DIVIDER)
            .assertIsDisplayed()
    }

    @Test
    fun bottomScrollHorizontalDividerIsShownWhenContentCanBeScrolledDown() {
        composeTestRule.setContent {
            MyBusTheme {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    ServicesChooserDialogContentWithState(
                        state = UiState(
                            content = scrollContent,
                            isClearAllButtonEnabled = false
                        ),
                        onServiceClick = { },
                        onClearAllButtonEnabledStateChanged = { }
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_BOTTOM_SCROLL_HORIZONTAL_DIVIDER)
            .assertIsDisplayed()
    }

    @Test
    fun bottomScrollHorizontalDividerIsNotShownWhenContentCanNotBeScrolledDown() {
        composeTestRule.setContent {
            MyBusTheme {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp)
                ) {
                    ServicesChooserDialogContentWithState(
                        state = UiState(
                            content = scrollContent,
                            isClearAllButtonEnabled = false
                        ),
                        onServiceClick = { },
                        onClearAllButtonEnabledStateChanged = { }
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_GRID)
            .performScrollToKey(ServiceDescriptor("2", "TEST2").toParcelableServiceDescriptor())
        composeTestRule
            .onNodeWithTag(TEST_TAG_BOTTOM_SCROLL_HORIZONTAL_DIVIDER)
            .assertIsNotDisplayed()
    }

    private val scrollContent get() = UiContent.Content(
        items = persistentListOf(
            UiServiceChooserItem.Operator.Named(
                operatorId = "TEST1",
                operatorName = "First Operator"
            ),
            UiServiceChooserItem.Service(
                serviceDescriptor = ServiceDescriptor("1", "TEST1"),
                serviceName = UiServiceName(
                    serviceName = "1",
                    colours = UiServiceColours(
                        backgroundColour = Color.Red.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                ),
                isSelected = false
            ),
            UiServiceChooserItem.Operator.Named(
                operatorId = "TEST2",
                operatorName = "Second Operator"
            ),
            UiServiceChooserItem.Service(
                serviceDescriptor = ServiceDescriptor("2", "TEST2"),
                serviceName = UiServiceName(
                    serviceName = "2",
                    colours = UiServiceColours(
                        backgroundColour = Color.Red.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                ),
                isSelected = false
            )
        )
    )
}
