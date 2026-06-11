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

import android.app.Dialog
import android.content.DialogInterface
import android.content.res.Configuration
import android.os.Bundle
import androidx.annotation.StringRes
import androidx.appcompat.app.AlertDialog
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyGridItemSpanScope
import androidx.compose.foundation.lazy.grid.LazyGridState
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.grid.rememberLazyGridState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.core.os.BundleCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.fragment.compose.content
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.core.domain.ParcelableServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toParcelableServiceDescriptor
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

/**
 * This [DialogFragment] allows the user to select services from a list and then return the user's
 * selection back to the caller. This may be used to ask the user to filter services or to declare
 * which services they are interested in.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
public class ServicesChooserDialogFragment : DialogFragment() {

    public companion object {

        /** The request key used for the new Fragment result API.  */
        public const val REQUEST_KEY: String = "requestChosenServices"

        /**
         * The key to use on the [Bundle] returned from the new Fragment result API to get the
         * user chosen services.
         */
        public const val RESULT_CHOSEN_SERVICES: String = "chosenServices"

        /**
         * Create a new instance of this [DialogFragment], providing the [ServicesChooserParams].
         *
         * @param parameters The parameters to use to start this chooser.
         * @return A new instance of this [DialogFragment].
         */
        public fun newInstance(parameters: ServicesChooserParams): ServicesChooserDialogFragment {
            return ServicesChooserDialogFragment()
                .apply {
                    arguments = Bundle().apply {
                        putParcelable(ARG_PARAMS, parameters)
                    }
                }
        }
    }

    private val viewModel by viewModels<ServicesChooserViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val titleResId = arguments
            ?.let { BundleCompat.getParcelable(it, ARG_PARAMS, ServicesChooserParams::class.java) }
            ?.titleResId
            ?: 0

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(titleResId)
            .setView(
                content {
                    MyBusTheme {
                        ServicesChooserDialogContent(
                            onClearAllButtonEnabledStateChanged =
                                ::updateClearAllButtonEnabledState,
                            modifier = Modifier
                                .padding(
                                    top = dimensionResource(Rcore.dimen.padding_double)
                                ),
                            viewModel = viewModel
                        )
                    }
                }
            )
            .setPositiveButton(Rcore.string.close, null)
            .setNeutralButton(R.string.serviceschooserdialog_btn_clear_all) { _, _ ->
                viewModel.onClearAllClicked()
            }
            .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        dispatchSelectedServices()
    }

    override fun getDialog(): AlertDialog? = super.dialog as? AlertDialog

    private fun updateClearAllButtonEnabledState(enabled: Boolean) {
        dialog?.getButton(DialogInterface.BUTTON_NEUTRAL)?.isEnabled = enabled
    }

    private fun dispatchSelectedServices() {
        val selectedServices = viewModel.selectedServices
        val chosenServices = ArrayList<ParcelableServiceDescriptor>(selectedServices.size)
        selectedServices.mapTo(chosenServices, ServiceDescriptor::toParcelableServiceDescriptor)

        parentFragmentManager.setFragmentResult(
            REQUEST_KEY,
            Bundle().apply {
                putParcelableArrayList(RESULT_CHOSEN_SERVICES, chosenServices)
            }
        )
    }
}

internal const val TEST_TAG_CONTENT_PROGRESS = "content-progress"
internal const val TEST_TAG_CONTENT_CONTENT = "content-content"
internal const val TEST_TAG_ERROR_NO_SERVICES_GLOBAL = "error-no-services-global"
internal const val TEST_TAG_ERROR_NO_SERVICES_FOR_STOP = "error-no-services-for-stop"
internal const val TEST_TAG_CONTENT_GRID = "content-grid"
internal const val TEST_TAG_TOP_SCROLL_HORIZONTAL_DIVIDER = "top-scroll-horizontal-divider"
internal const val TEST_TAG_BOTTOM_SCROLL_HORIZONTAL_DIVIDER = "bottom-scroll-horizontal-divider"
private const val KEY_UNKNOWN_OPERATOR = "unknown-operator"
private const val CONTENT_TYPE_OPERATOR = "operator"
private const val CONTENT_TYPE_SERVICE = "service"

@Composable
private fun ServicesChooserDialogContent(
    onClearAllButtonEnabledStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ServicesChooserViewModel = viewModel()
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    ServicesChooserDialogContentWithState(
        state = uiState,
        onServiceClick = viewModel::onServiceClicked,
        onClearAllButtonEnabledStateChanged = onClearAllButtonEnabledStateChanged,
        modifier = modifier
    )
}

/**
 * This composes the services chooser dialog content.
 *
 * @param state The state to be rendered.
 * @param onServiceClick This is called when a service has been clicked.
 * @param onClearAllButtonEnabledStateChanged This is called when the clear all button state has
 * changed.
 * @param modifier Any [Modifier]s which should be applied.
 */
@Composable
internal fun ServicesChooserDialogContentWithState(
    state: UiState,
    onServiceClick: (ServiceDescriptor) -> Unit,
    onClearAllButtonEnabledStateChanged: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        when (val content = state.content) {
            is UiContent.InProgress -> IndeterminateProgress(
                modifier = Modifier
                    .padding(dimensionResource(Rcore.dimen.padding_double))
            )
            is UiContent.Content -> LazyContentGrid(
                items = content.items,
                onServiceClick = onServiceClick,
                modifier = Modifier
                    .fillMaxWidth()
            )
            is UiContent.Error.NoGlobalServices -> NoGlobalServicesErrorText(
                modifier = Modifier
                    .padding(dimensionResource(Rcore.dimen.padding_double))
            )
            is UiContent.Error.NoServicesForStop -> NoServicesForStopErrorText(
                modifier = Modifier
                    .padding(dimensionResource(Rcore.dimen.padding_double))
            )
        }
    }

    ClearAllButtonEffect(
        isClearAllButtonEnabled = state.isClearAllButtonEnabled,
        onClearAllButtonEnabledStateChanged = onClearAllButtonEnabledStateChanged
    )
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
private fun LazyContentGrid(
    items: ImmutableList<UiServiceChooserItem>,
    onServiceClick: (ServiceDescriptor) -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_CONTENT_CONTENT
            }
    ) {
        val paddingDefault = dimensionResource(Rcore.dimen.padding_default)
        val lazyGridState = rememberLazyGridState()

        LazyVerticalGrid(
            columns = GridCells.Adaptive(minSize = 76.dp),
            modifier = Modifier
                .semantics {
                    testTag = TEST_TAG_CONTENT_GRID
                },
            state = lazyGridState,
            contentPadding = PaddingValues(
                bottom = paddingDefault,
                start = 24.dp,
                end = 24.dp
            ),
            verticalArrangement = Arrangement.spacedBy(paddingDefault),
            horizontalArrangement = Arrangement.spacedBy(paddingDefault)
        ) {
            items(
                items = items,
                key = ::calculateKeyForItem,
                span = LazyGridItemSpanScope::calculateSpanForItem,
                contentType = ::calculateContentTypeForItem
            ) {
                when (it) {
                    is UiServiceChooserItem.Operator.Unknown -> OperatorItem(
                        operatorName = stringResource(
                            R.string.serviceschooserdialog_item_unknown_operator
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = paddingDefault,
                                bottom = paddingDefault
                            )
                    )
                    is UiServiceChooserItem.Operator.Named -> OperatorItem(
                        operatorName = it.operatorName,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(
                                top = paddingDefault,
                                bottom = paddingDefault
                            )
                    )
                    is UiServiceChooserItem.Service -> ServiceItem(
                        service = it,
                        onClick = { onServiceClick(it.serviceDescriptor) },
                        modifier = Modifier
                            .fillMaxWidth()
                    )
                }
            }
        }

        TopScrollHorizontalDivider(
            lazyGridState = lazyGridState,
            modifier = Modifier.align(Alignment.TopStart)
        )

        BottomScrollHorizontalDivider(
            lazyGridState = lazyGridState,
            modifier = Modifier.align(Alignment.BottomStart)
        )
    }
}

@Composable
private fun TopScrollHorizontalDivider(
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier
) {
    if (lazyGridState.canScrollBackward) {
        HorizontalDivider(
            modifier = modifier
                .semantics {
                    testTag = TEST_TAG_TOP_SCROLL_HORIZONTAL_DIVIDER
                }
        )
    }
}

@Composable
private fun BottomScrollHorizontalDivider(
    lazyGridState: LazyGridState,
    modifier: Modifier = Modifier
) {
    if (lazyGridState.canScrollForward) {
        HorizontalDivider(
            modifier = modifier
                .semantics {
                    testTag = TEST_TAG_BOTTOM_SCROLL_HORIZONTAL_DIVIDER
                }
        )
    }
}

@Composable
private fun NoGlobalServicesErrorText(
    modifier: Modifier = Modifier
) {
    ErrorText(
        stringResId = R.string.serviceschooserdialog_error_no_services_global,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ERROR_NO_SERVICES_GLOBAL
            }
    )
}

@Composable
private fun NoServicesForStopErrorText(
    modifier: Modifier = Modifier
) {
    ErrorText(
        stringResId = R.string.serviceschooserdialog_error_no_services_stop,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ERROR_NO_SERVICES_FOR_STOP
            }
    )
}

@Composable
private fun ErrorText(
    @StringRes stringResId: Int,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(stringResId),
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface,
        textAlign = TextAlign.Center,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun ClearAllButtonEffect(
    isClearAllButtonEnabled: Boolean,
    onClearAllButtonEnabledStateChanged: (Boolean) -> Unit
) {
    LaunchedEffect(isClearAllButtonEnabled) {
        onClearAllButtonEnabledStateChanged(isClearAllButtonEnabled)
    }
}

private fun calculateKeyForItem(item: UiServiceChooserItem): Any {
    return when (item) {
        is UiServiceChooserItem.Operator.Unknown -> KEY_UNKNOWN_OPERATOR
        is UiServiceChooserItem.Operator.Named -> item.operatorId
        is UiServiceChooserItem.Service ->
            item.serviceDescriptor.toParcelableServiceDescriptor()
    }
}

private fun LazyGridItemSpanScope.calculateSpanForItem(item: UiServiceChooserItem): GridItemSpan {
    return when (item) {
        is UiServiceChooserItem.Operator -> GridItemSpan(maxLineSpan)
        is UiServiceChooserItem.Service -> GridItemSpan(1)
    }
}

private fun calculateContentTypeForItem(item: UiServiceChooserItem): String {
    return when (item) {
        is UiServiceChooserItem.Operator -> CONTENT_TYPE_OPERATOR
        is UiServiceChooserItem.Service -> CONTENT_TYPE_SERVICE
    }
}

@Preview(
    name = "Services chooser content - light",
    group = "Services chooser content",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Services chooser content - dark",
    group = "Services chooser content",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ServicesChooserDialogContentPreview(
    @PreviewParameter(UiStateProvider::class) state: UiState
) {
    MyBusTheme {
        ServicesChooserDialogContentWithState(
            state = state,
            onServiceClick = { },
            onClearAllButtonEnabledStateChanged = { },
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    top = 16.dp,
                    bottom = 16.dp
                )
        )
    }
}

private class UiStateProvider : PreviewParameterProvider<UiState> {

    override val values = sequenceOf(
        UiState(
            content = UiContent.InProgress
        ),
        UiState(
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
                                backgroundColour = Color.Green.toArgb(),
                                textColour = Color.White.toArgb()
                            )
                        ),
                        isSelected = false
                    ),
                    UiServiceChooserItem.Service(
                        serviceDescriptor = ServiceDescriptor("3", "TEST2"),
                        serviceName = UiServiceName(
                            serviceName = "3",
                            colours = UiServiceColours(
                                backgroundColour = Color.Blue.toArgb(),
                                textColour = Color.White.toArgb()
                            )
                        ),
                        isSelected = true
                    ),
                    UiServiceChooserItem.Service(
                        serviceDescriptor = ServiceDescriptor("4", "TEST2"),
                        serviceName = UiServiceName(
                            serviceName = "4",
                            colours = UiServiceColours(
                                backgroundColour = Color.Cyan.toArgb(),
                                textColour = Color.White.toArgb()
                            )
                        ),
                        isSelected = false
                    ),
                    UiServiceChooserItem.Service(
                        serviceDescriptor = ServiceDescriptor("5", "TEST2"),
                        serviceName = UiServiceName(
                            serviceName = "5",
                            colours = UiServiceColours(
                                backgroundColour = Color.DarkGray.toArgb(),
                                textColour = Color.White.toArgb()
                            )
                        ),
                        isSelected = true
                    ),
                    UiServiceChooserItem.Operator.Unknown,
                    UiServiceChooserItem.Service(
                        serviceDescriptor = ServiceDescriptor("6", "TEST3"),
                        serviceName = UiServiceName(
                            serviceName = "6",
                            colours = UiServiceColours(
                                backgroundColour = Color.Magenta.toArgb(),
                                textColour = Color.White.toArgb()
                            )
                        ),
                        isSelected = false
                    ),
                )
            )
        ),
        UiState(
            content = UiContent.Error.NoGlobalServices
        ),
        UiState(
            content = UiContent.Error.NoServicesForStop
        )
    )
}
