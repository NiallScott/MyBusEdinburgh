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
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsEnabled
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.v2.createAndroidComposeRule
import androidx.compose.ui.test.longClick
import androidx.compose.ui.test.onChild
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollToKey
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
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
    fun closeButtonHasContentDescription() {
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = null,
                        isClearAllButtonEnabled = false
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CLOSE_BUTTON)
            .assertContentDescriptionEquals(
                composeTestRule
                    .activity
                    .getString(Rcore.string.close)
            )
    }

    @Test
    fun closeButtonClickInvokesOnCloseClickLambda() {
        val clickCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = null,
                        isClearAllButtonEnabled = false
                    ),
                    onCloseClick = clickCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CLOSE_BUTTON)
            .performClick()

        assertEquals(1, clickCounter.count)
    }

    @Test
    fun closeButtonLongClickShowsTooltip() {
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = null,
                        isClearAllButtonEnabled = false
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CLOSE_BUTTON)
            .performTouchInput {
                longClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_CLOSE_BUTTON_TOOLTIP)
            .apply {
                assertExists()

                onChild()
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(Rcore.string.close)
                    )
                }
    }

    @Test
    fun titleShowsAppBarTitleText() {
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test title",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = null,
                        isClearAllButtonEnabled = false
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_TITLE)
            .assertTextEquals("Test title")
    }

    @Test
    fun clearAllButtonHasContentDescription() {
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = null,
                        isClearAllButtonEnabled = true
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CLEAR_ALL_BUTTON)
            .assertContentDescriptionEquals(
                composeTestRule
                    .activity
                    .getString(R.string.serviceschooserdialog_btn_clear_all)
            )
    }

    @Test
    fun clearAllButtonIsDisabledWhenStateIsDisabled() {
        val clickCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = null,
                        isClearAllButtonEnabled = false
                    ),
                    onClearAllClick = clickCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CLEAR_ALL_BUTTON)
            .apply {
                assertIsNotEnabled()
                performClick()
            }

        assertEquals(0, clickCounter.count)
    }

    @Test
    fun clearAllButtonDoesNotShowTooltipWhenDisabled() {
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = null,
                        isClearAllButtonEnabled = false
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CLEAR_ALL_BUTTON)
            .performTouchInput {
                longClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_CLEAR_ALL_BUTTON_TOOLTIP)
            .assertDoesNotExist()
    }

    @Test
    fun clearAllButtonIsEnabledWhenStateIsEnabled() {
        val clickCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = null,
                        isClearAllButtonEnabled = true
                    ),
                    onClearAllClick = clickCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CLEAR_ALL_BUTTON)
            .apply {
                assertIsEnabled()
                performClick()
            }

        assertEquals(1, clickCounter.count)
    }

    @Test
    fun clearAllButtonLongClickShowsToolTipWhenEnabled() {
        val clickCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = null,
                        isClearAllButtonEnabled = true
                    ),
                    onClearAllClick = clickCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CLEAR_ALL_BUTTON)
            .performTouchInput {
                longClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_CLEAR_ALL_BUTTON_TOOLTIP)
            .apply {
                assertExists()

                onChild()
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.serviceschooserdialog_btn_clear_all)
                    )
            }
    }

    @Test
    fun applyButtonHasContentDescription() {
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = null,
                        isClearAllButtonEnabled = false
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_APPLY_BUTTON)
            .assertContentDescriptionEquals(
                composeTestRule
                    .activity
                    .getString(R.string.serviceschooserdialog_btn_apply)
            )
    }

    @Test
    fun applyButtonClickInvokesOnApplyClickLambda() {
        val clickCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = null,
                        isClearAllButtonEnabled = false
                    ),
                    onApplyClick = clickCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_APPLY_BUTTON)
            .performClick()

        assertEquals(1, clickCounter.count)
    }

    @Test
    fun applyButtonLongClickShowsTooltip() {
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = null,
                        isClearAllButtonEnabled = false
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_APPLY_BUTTON)
            .performTouchInput {
                longClick()
            }
        composeTestRule
            .onNodeWithTag(TEST_TAG_APPLY_BUTTON_TOOLTIP)
            .apply {
                assertExists()

                onChild()
                    .assertTextEquals(
                        composeTestRule
                            .activity
                            .getString(R.string.serviceschooserdialog_btn_apply)
                    )
            }
    }

    @Test
    fun showsProgressWhenContentIsInProgress() {
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = null,
                        isClearAllButtonEnabled = false
                    )
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
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
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
                        action = null,
                        isClearAllButtonEnabled = false
                    )
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
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.Error.NoGlobalServices,
                        action = null,
                        isClearAllButtonEnabled = false
                    )
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
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.Error.NoServicesForStop,
                        action = null,
                        isClearAllButtonEnabled = false
                    )
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
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
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
                        action = null,
                        isClearAllButtonEnabled = false
                    ),
                    onServiceClick = { serviceItemClickCount++ }
                )
            }
        }

        composeTestRule
            .onNode(hasTestTag(TEST_TAG_SERVICE_ITEM) and hasText("1"))
            .performClick()
        assertEquals(1, serviceItemClickCount)
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
                    ServicesChooserDialogContentWithStateWithDefaults(
                        appBarTitle = "Test",
                        state = UiState(
                            content = scrollContent,
                            action = null,
                            isClearAllButtonEnabled = false
                        )
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
                    ServicesChooserDialogContentWithStateWithDefaults(
                        appBarTitle = "Test",
                        state = UiState(
                            content = scrollContent,
                            action = null,
                            isClearAllButtonEnabled = false
                        )
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_GRID)
            .performScrollToKey("op_TEST2")
        composeTestRule
            .onNodeWithTag(TEST_TAG_TOP_SCROLL_HORIZONTAL_DIVIDER)
            .assertIsDisplayed()
    }

    @Test
    fun closeActionCallsLambdaThenMarksActionAsLaunched() {
        val closeInvocationCounter = InvocationCounter()
        val actionLaunchedCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.Close,
                        isClearAllButtonEnabled = false
                    ),
                    onActionLaunched = actionLaunchedCounter,
                    onClose = closeInvocationCounter
                )
            }
        }

        assertEquals(1, closeInvocationCounter.count)
        assertEquals(1, actionLaunchedCounter.count)
    }

    @Test
    fun dispatchResultAndCloseCallsLambdaThenMarksActionAsLaunched() {
        val selectedServicesTracker = mutableListOf<Set<ServiceDescriptor>>()
        val actionLaunchedCounter = InvocationCounter()
        val service = ServiceDescriptor(
            serviceName = "1",
            operatorCode = "TEST1"
        )
        composeTestRule.setContent {
            MyBusTheme {
                ServicesChooserDialogContentWithStateWithDefaults(
                    appBarTitle = "Test",
                    state = UiState(
                        content = UiContent.InProgress,
                        action = UiAction.DispatchResultAndClose(
                            selectedServices = setOf(service)
                        ),
                        isClearAllButtonEnabled = false
                    ),
                    onActionLaunched = actionLaunchedCounter,
                    onDispatchResultAndClose = { selectedServicesTracker += it }
                )
            }
        }

        assertEquals(
            listOf(
                setOf(service)
            ),
            selectedServicesTracker
        )
        assertEquals(1, actionLaunchedCounter.count)
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

    @Composable
    private fun ServicesChooserDialogContentWithStateWithDefaults(
        appBarTitle: String,
        state: UiState,
        modifier: Modifier = Modifier,
        onCloseClick: () -> Unit = { throw NotImplementedError() },
        onClearAllClick: () -> Unit = { throw NotImplementedError() },
        onApplyClick: () -> Unit = { throw NotImplementedError() },
        onServiceClick: (ServiceDescriptor) -> Unit = { throw NotImplementedError() },
        onActionLaunched: () -> Unit = { throw NotImplementedError() },
        onClose: () -> Unit = { throw NotImplementedError() },
        onDispatchResultAndClose: (Set<ServiceDescriptor>) -> Unit =
            { throw NotImplementedError() }
    ) {
        ServicesChooserDialogContentWithState(
            appBarTitle = appBarTitle,
            state = state,
            onCloseClick = onCloseClick,
            onClearAllClick = onClearAllClick,
            onApplyClick = onApplyClick,
            onServiceClick = onServiceClick,
            onActionLaunched = onActionLaunched,
            onClose = onClose,
            onDispatchResultAndClose = onDispatchResultAndClose,
            modifier = modifier
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
