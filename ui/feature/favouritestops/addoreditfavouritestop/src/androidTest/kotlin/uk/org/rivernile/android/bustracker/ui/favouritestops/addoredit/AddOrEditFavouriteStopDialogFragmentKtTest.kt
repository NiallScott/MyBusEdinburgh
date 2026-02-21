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

package uk.org.rivernile.android.bustracker.ui.favouritestops.addoredit

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performImeAction
import androidx.compose.ui.test.performTextInput
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.text.FakeStopNameFormatter
import uk.org.rivernile.android.bustracker.core.text.LocalStopNameFormatter
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `AddOrEditFavouriteStopDialogFragment.kt`.
 *
 * @author Niall Scott
 */
class AddOrEditFavouriteStopDialogFragmentKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsIndeterminateProgressWhenContentIsInProgress() {
        val titleTracker = IntTracker()
        val positiveButtonTracker = BooleanTracker()
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.InProgress
                    ),
                    onTitleStringResChanged = titleTracker,
                    onPositiveButtonEnabledStateChanged = positiveButtonTracker
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertExists()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT)
            .assertDoesNotExist()
        assertEquals(
            listOf(
                R.string.addeditfavouritestopdialog_title_add
            ),
            titleTracker.observedValues
        )
        assertEquals(
            listOf(false),
            positiveButtonTracker.observedValues
        )
    }

    @Test
    fun showsAddContentWhenModeIsAdd() {
        val stopNameTextTracker = StringTracker()
        val titleTracker = IntTracker()
        val positiveButtonTracker = BooleanTracker()
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Add(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Some Name",
                                locality = "Some Locality"
                            ),
                            isPositiveButtonEnabled = false
                        )
                    ),
                    onStopNameTextChanged = stopNameTextTracker,
                    onTitleStringResChanged = titleTracker,
                    onPositiveButtonEnabledStateChanged = positiveButtonTracker
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT)
            .assertExists()
        assertEquals(
            listOf(
                UiStopName(
                    name = "Some Name",
                    locality = "Some Locality"
                ).toString()
            ),
            stopNameTextTracker.observedValues
        )
        assertEquals(
            listOf(
                R.string.addeditfavouritestopdialog_title_add
            ),
            titleTracker.observedValues
        )
        assertEquals(
            listOf(false),
            positiveButtonTracker.observedValues
        )
    }

    @Test
    fun showsEditContentWhenModeIsEdit() {
        val stopNameTextTracker = StringTracker()
        val titleTracker = IntTracker()
        val positiveButtonTracker = BooleanTracker()
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Edit(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Some Name",
                                locality = "Some Locality"
                            ),
                            isPositiveButtonEnabled = false,
                            savedName = "Saved Name"
                        )
                    ),
                    onStopNameTextChanged = stopNameTextTracker,
                    onTitleStringResChanged = titleTracker,
                    onPositiveButtonEnabledStateChanged = positiveButtonTracker
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT_PROGRESS)
            .assertDoesNotExist()
        composeTestRule
            .onNodeWithTag(TEST_TAG_CONTENT)
            .assertExists()
        assertEquals(
            listOf("Saved Name"),
            stopNameTextTracker.observedValues
        )
        assertEquals(
            listOf(
                R.string.addeditfavouritestopdialog_title_edit
            ),
            titleTracker.observedValues
        )
        assertEquals(
            listOf(false),
            positiveButtonTracker.observedValues
        )
    }

    @Test
    fun addModeShowsCorrectBlurb() {
        val stopName = UiStopName(
            name = "Some Name",
            locality = "Some Locality"
        )
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Add(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = stopName,
                            isPositiveButtonEnabled = false
                        )
                    ),
                    onStopNameTextChanged = { },
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = { }
                )
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(
                R.string.addeditfavouritestopdialog_blurb_add,
                "stopIdentifier=123456;stopName=$stopName"
            )

        composeTestRule
            .onNodeWithTag(TEST_TAG_BLURB_TEXT)
            .assertTextEquals(expectedText)
    }

    @Test
    fun editModeShowsCorrectBlurb() {
        val stopName = UiStopName(
            name = "Some Name",
            locality = "Some Locality"
        )
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Edit(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = stopName,
                            isPositiveButtonEnabled = false,
                            savedName = "Saved Name"
                        )
                    ),
                    onStopNameTextChanged = { },
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = { }
                )
            }
        }
        val expectedText = composeTestRule
            .activity
            .getString(
                R.string.addeditfavouritestopdialog_blurb_edit,
                "stopIdentifier=123456;stopName=$stopName"
            )

        composeTestRule
            .onNodeWithTag(TEST_TAG_BLURB_TEXT)
            .assertTextEquals(expectedText)
    }

    @Test
    fun addModeFocusesStopNameTextFieldByDefault() {
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Add(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Some Name",
                                locality = "Some Locality"
                            ),
                            isPositiveButtonEnabled = false
                        )
                    ),
                    onStopNameTextChanged = { },
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_STOP_NAME_TEXT_FIELD)
            .assertIsFocused()
    }

    @Test
    fun editModeFocusesStopNameTextFieldByDefault() {
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Edit(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Some Name",
                                locality = "Some Locality"
                            ),
                            isPositiveButtonEnabled = false,
                            savedName = "Saved Name"
                        )
                    ),
                    onStopNameTextChanged = { },
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_STOP_NAME_TEXT_FIELD)
            .assertIsFocused()
    }

    @Test
    fun addModeDefaultsStopNameTextFieldToStopIdentifierWhenStopNameIsNull() {
        val stopNameTextTracker = StringTracker()
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Add(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = null,
                            isPositiveButtonEnabled = false
                        )
                    ),
                    onStopNameTextChanged = stopNameTextTracker,
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_STOP_NAME_TEXT_FIELD, useUnmergedTree = true)
            .assertTextEquals("123456")
        assertEquals(
            listOf("123456"),
            stopNameTextTracker.observedValues
        )
    }

    @Test
    fun addModeDefaultsStopNameTextFieldToStopNameWhenStopNameIsNotNull() {
        val stopName = UiStopName(
            name = "Some Name",
            locality = "Some Locality"
        )
        val stopNameTextTracker = StringTracker()
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Add(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = stopName,
                            isPositiveButtonEnabled = false
                        )
                    ),
                    onStopNameTextChanged = stopNameTextTracker,
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_STOP_NAME_TEXT_FIELD, useUnmergedTree = true)
            .assertTextEquals(stopName.toString())
        assertEquals(
            listOf(stopName.toString()),
            stopNameTextTracker.observedValues
        )
    }

    @Test
    fun editModeDefaultsStopNameTextFieldToSavedName() {
        val stopNameTextTracker = StringTracker()
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Edit(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Some Name",
                                locality = "Some Locality"
                            ),
                            isPositiveButtonEnabled = false,
                            savedName = "Saved Name"
                        )
                    ),
                    onStopNameTextChanged = stopNameTextTracker,
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_STOP_NAME_TEXT_FIELD, useUnmergedTree = true)
            .assertTextEquals("Saved Name")
        assertEquals(
            listOf("Saved Name"),
            stopNameTextTracker.observedValues
        )
    }

    @Test
    fun addModeEditingStopNameTextFieldCallsChangedLambda() {
        val stopName = UiStopName(
            name = "Some Name",
            locality = "Some Locality"
        )
        val stopNameTextTracker = StringTracker()
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Add(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = stopName,
                            isPositiveButtonEnabled = false
                        )
                    ),
                    onStopNameTextChanged = stopNameTextTracker,
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_STOP_NAME_TEXT_FIELD, useUnmergedTree = true)
            .performTextInput(" - Test")
        composeTestRule.waitForIdle()

        assertEquals(
            listOf(
                stopName.toString(),
                "$stopName - Test"
            ),
            stopNameTextTracker.observedValues
        )
    }

    @Test
    fun editModeEditingStopNameTextFieldCallsChangedLambda() {
        val stopNameTextTracker = StringTracker()
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Edit(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Some Name",
                                locality = "Some Locality"
                            ),
                            isPositiveButtonEnabled = false,
                            savedName = "Saved Name"
                        )
                    ),
                    onStopNameTextChanged = stopNameTextTracker,
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_STOP_NAME_TEXT_FIELD, useUnmergedTree = true)
            .performTextInput(" - Test")
        composeTestRule.waitForIdle()

        assertEquals(
            listOf(
                "Saved Name",
                "Saved Name - Test"
            ),
            stopNameTextTracker.observedValues
        )
    }

    @Test
    fun addModePerformKeyboardActionCallsKeyboardActionLambda() {
        val invocationCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Add(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Some Name",
                                locality = "Some Locality"
                            ),
                            isPositiveButtonEnabled = false
                        )
                    ),
                    onStopNameTextChanged = { },
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = { },
                    onKeyboardAction = invocationCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_STOP_NAME_TEXT_FIELD, useUnmergedTree = true)
            .performImeAction()
        composeTestRule.waitForIdle()

        assertEquals(1, invocationCounter.count)
    }

    @Test
    fun editModePerformKeyboardActionCallsKeyboardActionLambda() {
        val invocationCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Edit(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Some Name",
                                locality = "Some Locality"
                            ),
                            isPositiveButtonEnabled = false,
                            savedName = "Saved Name"
                        )
                    ),
                    onStopNameTextChanged = { },
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = { },
                    onKeyboardAction = invocationCounter
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_STOP_NAME_TEXT_FIELD, useUnmergedTree = true)
            .performImeAction()
        composeTestRule.waitForIdle()

        assertEquals(1, invocationCounter.count)
    }

    @Test
    fun addModeEnablesPositiveButtonWhenPositiveButtonEnabled() {
        val positiveButtonTracker = BooleanTracker()
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Add(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Some Name",
                                locality = "Some Locality"
                            ),
                            isPositiveButtonEnabled = true
                        )
                    ),
                    onStopNameTextChanged = { },
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = positiveButtonTracker
                )
            }
        }

        assertEquals(
            listOf(true),
            positiveButtonTracker.observedValues
        )
    }

    @Test
    fun editModeEnablesPositiveButtonWhenPositiveButtonEnabled() {
        val positiveButtonTracker = BooleanTracker()
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Edit(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Some Name",
                                locality = "Some Locality"
                            ),
                            isPositiveButtonEnabled = true,
                            savedName = "Saved Name"
                        )
                    ),
                    onStopNameTextChanged = { },
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = positiveButtonTracker
                )
            }
        }

        assertEquals(
            listOf(true),
            positiveButtonTracker.observedValues
        )
    }

    @Test
    fun dismissDialogActionDismissesDialogAndCallsActionLaunchedLambda() {
        val dismissDialogInvocationCounter = InvocationCounter()
        val actionLaunchedInvocationCounter = InvocationCounter()
        composeTestRule.setContent {
            MyBusTheme {
                AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
                    state = UiState(
                        content = UiContent.Mode.Add(
                            stopIdentifier = "123456".toNaptanStopIdentifier(),
                            stopName = UiStopName(
                                name = "Some Name",
                                locality = "Some Locality"
                            ),
                            isPositiveButtonEnabled = true
                        ),
                        action = UiAction.DismissDialog
                    ),
                    onStopNameTextChanged = { },
                    onTitleStringResChanged = { },
                    onPositiveButtonEnabledStateChanged = { },
                    onDismissDialog = dismissDialogInvocationCounter,
                    onActionLaunched = actionLaunchedInvocationCounter
                )
            }
        }

        assertEquals(1, dismissDialogInvocationCounter.count)
        assertEquals(1, actionLaunchedInvocationCounter.count)
    }

    @Composable
    private fun AddOrEditFavouriteStopDialogContentWithStateWithDefaults(
        state: UiState,
        onStopNameTextChanged: (String) -> Unit = { throw NotImplementedError() },
        onTitleStringResChanged: (Int) -> Unit = { throw NotImplementedError() },
        onPositiveButtonEnabledStateChanged: (Boolean) -> Unit = { throw NotImplementedError() },
        onKeyboardAction: () -> Unit = { throw NotImplementedError() },
        onActionLaunched: () -> Unit = { throw NotImplementedError() },
        onDismissDialog: () -> Unit = { throw NotImplementedError() }
    ) {
        CompositionLocalProvider(
            LocalStopNameFormatter provides stopNameFormatter
        ) {
            AddOrEditFavouriteStopDialogContentWithState(
                state = state,
                onStopNameTextChanged = onStopNameTextChanged,
                onTitleStringResChanged = onTitleStringResChanged,
                onPositiveButtonEnabledStateChanged = onPositiveButtonEnabledStateChanged,
                onKeyboardAction = onKeyboardAction,
                onActionLaunched = onActionLaunched,
                onDismissDialog = onDismissDialog
            )
        }
    }

    private val stopNameFormatter get() = FakeStopNameFormatter(
        onFormatBusStopName = { it.toString() },
        onFormatBusStopNameWithStopIdentifier = { stopIdentifier, stopName ->
            "stopIdentifier=${stopIdentifier.toHumanReadableString()};stopName=$stopName"
        }
    )
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

private typealias IntTracker = Tracker<Int>
private typealias BooleanTracker = Tracker<Boolean>
private typealias StringTracker = Tracker<String>
