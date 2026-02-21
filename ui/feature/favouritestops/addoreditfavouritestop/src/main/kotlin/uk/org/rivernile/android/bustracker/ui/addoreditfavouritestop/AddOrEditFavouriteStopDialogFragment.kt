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

package uk.org.rivernile.android.bustracker.ui.addoreditfavouritestop

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import android.view.WindowManager
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.text.input.TextFieldLineLimits
import androidx.compose.foundation.text.input.rememberTextFieldState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toParcelableStopIdentifier
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.core.text.formatBusStopName
import uk.org.rivernile.android.bustracker.core.text.formatBusStopNameWithStopIdentifier
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore

/**
 * Show a [DialogFragment] which allows the user to add a new favourite stop, or edit the name
 * of an existing one. This [DialogFragment] will determine if the given stop identifier is already
 * a favourite stop and present the correct UI.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
public class AddOrEditFavouriteStopDialogFragment : DialogFragment() {

    public companion object {

        /**
         * Create a new instance of this [DialogFragment] with the given stop identifier.
         *
         * @param stopIdentifier The stop to add or edit the favourite details for.
         * @return A new instance of this [DialogFragment].
         */
        public fun newInstance(
            stopIdentifier: StopIdentifier
        ): AddOrEditFavouriteStopDialogFragment {
            return AddOrEditFavouriteStopDialogFragment().apply {
                arguments = bundleOf(
                    ARG_STOP_IDENTIFIER to stopIdentifier.toParcelableStopIdentifier()
                )
            }
        }
    }

    private val viewModel by viewModels<AddOrEditFavouriteStopViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.addeditfavouritestopdialog_title_add)
            .setView(
                content {
                    MyBusTheme {
                        AddOrEditFavouriteStopDialogContent(
                            onTitleStringResChanged = ::updateTitleStringRes,
                            onPositiveButtonEnabledStateChanged =
                                ::updatePositiveButtonEnabledState,
                            onDismissDialog = ::dismissAllowingStateLoss,
                            modifier = Modifier
                                .padding(
                                    start = 24.dp,
                                    top = dimensionResource(Rcore.dimen.padding_double),
                                    end = 24.dp
                                )
                                .fillMaxWidth(),
                            viewModel = viewModel
                        )
                    }
                }
            )
            .setPositiveButton(R.string.addeditfavouritestopdialog_button_add) { _, _ ->
                viewModel.onAddButtonClicked()
            }
            .setNegativeButton(Rcore.string.cancel, null)
            .create()
            .apply {
                setOnShowListener {
                    // The FLAG_ALT_FOCUSABLE_IM flag is cleared as this prevents the keyboard being
                    // shown when the stop name editable text field gains focus. This is because
                    // the ComposeView is not recognised as an editable text field, so AlertDialog
                    // has code to apply the FLAG_ALT_FOCUSABLE_IM flag when no editable text field
                    // can be found. But this is obviously not the behavior that we want, so we need
                    // to unset it.
                    dialog?.window?.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM)
                }
            }
    }

    override fun getDialog(): AlertDialog? = super.getDialog() as? AlertDialog

    private fun updateTitleStringRes(@StringRes titleStringRes: Int) {
        dialog?.setTitle(titleStringRes)
    }

    private fun updatePositiveButtonEnabledState(enabled: Boolean) {
        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled = enabled
    }
}

internal const val TEST_TAG_CONTENT_PROGRESS = "content-progress"
internal const val TEST_TAG_CONTENT = "content-content"
internal const val TEST_TAG_BLURB_TEXT = "blurb-text"
internal const val TEST_TAG_STOP_NAME_TEXT_FIELD = "stop-name-text-field"

@Composable
private fun AddOrEditFavouriteStopDialogContent(
    onTitleStringResChanged: (Int) -> Unit,
    onPositiveButtonEnabledStateChanged: (Boolean) -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AddOrEditFavouriteStopViewModel = viewModel(),
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    AddOrEditFavouriteStopDialogContentWithState(
        state = uiState,
        onStopNameTextChanged = viewModel::stopNameText::set,
        onTitleStringResChanged = onTitleStringResChanged,
        onPositiveButtonEnabledStateChanged = onPositiveButtonEnabledStateChanged,
        onKeyboardAction = viewModel::onKeyboardActionButtonPressed,
        onActionLaunched = viewModel::onActionLaunched,
        onDismissDialog = onDismissDialog,
        modifier = modifier
    )
}

@Composable
internal fun AddOrEditFavouriteStopDialogContentWithState(
    state: UiState,
    onStopNameTextChanged: (String) -> Unit,
    onTitleStringResChanged: (Int) -> Unit,
    onPositiveButtonEnabledStateChanged: (Boolean) -> Unit,
    onKeyboardAction: () -> Unit,
    onActionLaunched: () -> Unit,
    onDismissDialog: () -> Unit,
    modifier: Modifier = Modifier
) {
    val content = state.content

    when (content) {
        is UiContent.InProgress -> IndeterminateProgress(
            modifier = modifier
        )
        is UiContent.Mode -> ContentColumn(
            mode = content,
            onStopNameTextChanged = onStopNameTextChanged,
            onKeyboardAction = onKeyboardAction,
            modifier = modifier
        )
    }

    TitleStringResEffect(
        titleStringRes = content.titleStringRes,
        onTitleStringResChanged = onTitleStringResChanged
    )

    PositiveButtonEffect(
        isPositiveButtonEnabled = content.isPositiveButtonEnabled,
        onPositiveButtonEnabledStateChanged = onPositiveButtonEnabledStateChanged
    )

    state.action?.let {
        LaunchAction(
            action = it,
            onActionLaunched = onActionLaunched,
            onDismissDialog = onDismissDialog
        )
    }
}

@Composable
private fun ContentColumn(
    mode: UiContent.Mode,
    onStopNameTextChanged: (String) -> Unit,
    onKeyboardAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .semantics {
                testTag = TEST_TAG_CONTENT
            },
        verticalArrangement = Arrangement.spacedBy(dimensionResource(Rcore.dimen.padding_double))
    ) {
        BlurbText(
            mode = mode
        )

        FavouriteStopNameTextField(
            mode = mode,
            onTextChanged = onStopNameTextChanged,
            onKeyboardAction = onKeyboardAction,
            modifier = Modifier
                .fillMaxWidth()
        )
    }
}

@Composable
private fun IndeterminateProgress(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_CONTENT_PROGRESS
            },
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}

@Composable
private fun BlurbText(
    mode: UiContent.Mode,
    modifier: Modifier = Modifier
) {
    Text(
        text = blurbString(mode),
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_BLURB_TEXT
            },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun FavouriteStopNameTextField(
    mode: UiContent.Mode,
    onTextChanged: (String) -> Unit,
    onKeyboardAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    val state = rememberTextFieldState(
        initialText = stopNameDefaultTextString(mode)
    )

    val focusRequester = remember { FocusRequester() }

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    LaunchedEffect(state) {
        snapshotFlow { state.text.toString() }
            .collectLatest {
                onTextChanged(it)
            }
    }

    OutlinedTextField(
        state = state,
        modifier = modifier
            .focusRequester(focusRequester)
            .semantics {
                testTag = TEST_TAG_STOP_NAME_TEXT_FIELD
            },
        lineLimits = TextFieldLineLimits.SingleLine,
        label = {
            Text(
                text = stringResource(R.string.addeditfavouritestopdialog_edit_name_hint)
            )
        },
        keyboardOptions = KeyboardOptions(
            capitalization = KeyboardCapitalization.Sentences,
            keyboardType = KeyboardType.Text,
            imeAction = ImeAction.Done,
            showKeyboardOnFocus = true
        ),
        onKeyboardAction = {
            onKeyboardAction()
        }
    )
}

@Composable
private fun TitleStringResEffect(
    @StringRes titleStringRes: Int,
    onTitleStringResChanged: (Int) -> Unit
) {
    LaunchedEffect(titleStringRes) {
        onTitleStringResChanged(titleStringRes)
    }
}

@Composable
private fun PositiveButtonEffect(
    isPositiveButtonEnabled: Boolean,
    onPositiveButtonEnabledStateChanged: (Boolean) -> Unit
) {
    LaunchedEffect(isPositiveButtonEnabled) {
        onPositiveButtonEnabledStateChanged(isPositiveButtonEnabled)
    }
}

@Composable
private fun LaunchAction(
    action: UiAction,
    onActionLaunched: () -> Unit,
    onDismissDialog: () -> Unit
) {
    LaunchedEffect(action) {
        when (action) {
            is UiAction.DismissDialog -> onDismissDialog()
        }

        onActionLaunched()
    }
}

@Composable
private fun blurbString(mode: UiContent.Mode): String {
    val stopName = formatBusStopNameWithStopIdentifier(mode.stopIdentifier, mode.stopName)

    return when (mode) {
        is UiContent.Mode.Add ->
            stringResource(R.string.addeditfavouritestopdialog_blurb_add, stopName)
        is UiContent.Mode.Edit ->
            stringResource(R.string.addeditfavouritestopdialog_blurb_edit, stopName)
    }
}

@Composable
private fun stopNameDefaultTextString(mode: UiContent.Mode): String {
    return when (mode) {
        is UiContent.Mode.Add -> mode
            .stopName
            ?.let { stopName ->
                formatBusStopName(stopName)
            }
            ?: mode.stopIdentifier.toHumanReadableString()
        is UiContent.Mode.Edit -> mode.savedName
    }
}

@get:StringRes
private val UiContent.titleStringRes: Int get() {
    return if (this is UiContent.Mode.Edit) {
        R.string.addeditfavouritestopdialog_title_edit
    } else {
        R.string.addeditfavouritestopdialog_title_add
    }
}

@Preview(
    name = "Add or edit favourite stop content - light",
    group = "Add or edit favourite stop content",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Add or edit favourite stop content - dark",
    group = "Add or edit favourite stop content",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AddOrEditFavouriteStopDialogContentPreview(
    @PreviewParameter(UiStateProvider::class) state: UiState
) {
    MyBusTheme {
        AddOrEditFavouriteStopDialogContentWithState(
            state = state,
            onStopNameTextChanged = { },
            onTitleStringResChanged = { },
            onPositiveButtonEnabledStateChanged = { },
            onKeyboardAction = { },
            onActionLaunched = { },
            onDismissDialog = { },
            modifier = Modifier.padding(16.dp)
        )
    }
}

private class UiStateProvider : PreviewParameterProvider<UiState> {

    override val values = sequenceOf(
        UiState(
            content = UiContent.InProgress
        ),
        UiState(
            content = UiContent.Mode.Add(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "A Stop",
                    locality = "Some Locality"
                ),
                isPositiveButtonEnabled = false
            )
        ),
        UiState(
            content = UiContent.Mode.Edit(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "A Stop",
                    locality = "Some Locality"
                ),
                isPositiveButtonEnabled = false,
                savedName = "Saved Name"
            )
        )
    )
}
