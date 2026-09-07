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

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toAtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.ui.formatters.LocalNumberFormatter
import uk.org.rivernile.android.bustracker.ui.formatters.rememberNumberFormatter
import uk.org.rivernile.android.bustracker.ui.text.PrimaryErrorText
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore

internal const val TEST_TAG_CONTENT_PROGRESS = "content-progress"
internal const val TEST_TAG_CONTENT_POPULATED = "content-populated"
internal const val TEST_TAG_CONTENT_ERROR_NO_STOP_DETAILS = "conten-error-no-stop-details"
private const val KEY_STOP_DETAILS = "stop_details"
private const val KEY_OPERATOR_UNKNOWN = "operator_unknown"
private const val KEY_NO_SERVICES = "no_services"
private const val CONTENT_TYPE_STOP_DETAILS = "stop_details"
private const val CONTENT_TYPE_OPERATOR = "operator"
private const val CONTENT_TYPE_SERVICE = "service"
private const val CONTENT_TYPE_NO_SERVICES = "no_services"

/**
 * This shows the stop details screen.
 *
 * @param onShowOnMap This lambda is called when a stop should be shown on the map.
 * @param onRequestLocationPermissions This lambda is called when location permissions should be
 * requested.
 * @param onShowLocationSettings This lambda is called when location settings should be requested.
 * @param modifier Any [Modifier] which should be applied.
 * @param viewModel The [androidx.lifecycle.ViewModel] for this screen.
 * @author Niall Scott
 */
@Composable
internal fun StopDetailsScreen(
    onShowOnMap: (StopIdentifier) -> Unit,
    onRequestLocationPermissions: () -> Unit,
    onShowLocationSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: StopDetailsViewModel = viewModel()
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle(
        // The minActiveState is set to RESUMED so that locations aren't continually monitored while
        // this screen is not the user's active focus.
        minActiveState = Lifecycle.State.RESUMED
    )

    StopDetailsScreenWithState(
        state = uiState,
        onStopMapClick = viewModel::onStopMapClicked,
        onGrantPermissionClick = viewModel::onGrantPermissionClicked,
        onTurnOnLocationClick = viewModel::onTurnOnLocationClicked,
        onActionLaunched = viewModel::onActionLaunched,
        onShowOnMap = onShowOnMap,
        onRequestLocationPermissions = onRequestLocationPermissions,
        onShowLocationSettings = onShowLocationSettings,
        modifier = modifier
    )
}

/**
 * This shows the stop details screen.
 *
 * @param state The state from which this screen should be rendered.
 * @param onStopMapClick This lambda is called when the stop map has been clicked.
 * @param onGrantPermissionClick This lambda is called when the grant permissions button has been
 * clicked.
 * @param onTurnOnLocationClick This lambda is called when the turn on location button has been
 * clicked.
 * @param onActionLaunched This lambda is called when an action has been launched.
 * @param onShowOnMap This lambda is called when a stop should be shown on the map.
 * @param onRequestLocationPermissions This lambda is called when location permissions should be
 * requested.
 * @param onShowLocationSettings This lambda is called when location settings should be requested.
 * @param modifier Any [Modifier] which should be applied.
 */
@Composable
internal fun StopDetailsScreenWithState(
    state: UiState,
    onStopMapClick: () -> Unit,
    onGrantPermissionClick: () -> Unit,
    onTurnOnLocationClick: () -> Unit,
    onActionLaunched: () -> Unit,
    onShowOnMap: (StopIdentifier) -> Unit,
    onRequestLocationPermissions: () -> Unit,
    onShowLocationSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        val nestedScrollInterop = rememberNestedScrollInteropConnection()

        when (val content = state.content) {
            is UiContent.InProgress -> IndeterminateProgress(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(dimensionResource(Rcore.dimen.padding_double))
                    .nestedScroll(nestedScrollInterop)
                    .verticalScroll(rememberScrollState())
            )
            is UiContent.Content -> Content(
                stopDetails = content.stopDetails,
                servicesItems = content.servicesItems,
                onStopMapClick = onStopMapClick,
                onGrantPermissionClick = onGrantPermissionClick,
                onTurnOnLocationClick = onTurnOnLocationClick,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollInterop)
            )
            is UiContent.NoStopDetailsError -> NoStopDetailsError(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(dimensionResource(Rcore.dimen.padding_double))
                    .nestedScroll(nestedScrollInterop)
                    .verticalScroll(rememberScrollState())
            )
        }
    }

    state.action?.let {
        LaunchAction(
            action = it,
            onShowOnMap = onShowOnMap,
            onRequestLocationPermissions = onRequestLocationPermissions,
            onShowLocationSettings = onShowLocationSettings,
            onActionLaunched = onActionLaunched
        )
    }
}

@Composable
private fun IndeterminateProgress(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .semantics {
                    testTag = TEST_TAG_CONTENT_PROGRESS
                }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Content(
    stopDetails: UiStopDetails,
    servicesItems: ImmutableList<UiServicesItem>,
    onStopMapClick: () -> Unit,
    onGrantPermissionClick: () -> Unit,
    onTurnOnLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safeDrawingInsets = WindowInsets.safeDrawing
    val insetsForContentPadding = remember { MutableWindowInsets() }

    LazyColumn(
        modifier = modifier
            .onConsumedWindowInsetsChanged { consumedInsets ->
                insetsForContentPadding.insets = safeDrawingInsets
                    .exclude(consumedInsets)
                    .only(WindowInsetsSides.Vertical)
            }
            .consumeWindowInsets(insetsForContentPadding)
            .semantics {
                testTag = TEST_TAG_CONTENT_POPULATED
            },
        contentPadding = insetsForContentPadding
            .asPaddingValues()
            .toPaddingValuesWithListVerticalPadding()
    ) {
        item(
            key = KEY_STOP_DETAILS,
            contentType = CONTENT_TYPE_STOP_DETAILS
        ) {
            StopDetailsItem(
                stopDetails = stopDetails,
                onStopMapClick = onStopMapClick,
                onGrantPermissionClick = onGrantPermissionClick,
                onTurnOnLocationClick = onTurnOnLocationClick,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        items(
            items = servicesItems,
            key = { it.listKey },
            contentType = { it.contentType }
        ) {
            val modifier = Modifier
                .fillMaxWidth()
                .animateItem()

            when (it) {
                is UiServicesItem.Operator.Unknown -> OperatorItem(
                    operatorName = stringResource(R.string.stopdetails_item_unknown_operator),
                    modifier = modifier
                )
                is UiServicesItem.Operator.Named -> OperatorItem(
                    operatorName = it.operatorName,
                    modifier = modifier
                )
                is UiServicesItem.Service -> ServiceItem(
                    service = it,
                    modifier = modifier
                )
                is UiServicesItem.NoServices -> NoServicesItem(
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun NoStopDetailsError(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_CONTENT_ERROR_NO_STOP_DETAILS
            },
        verticalArrangement = Arrangement
            .spacedBy(
                space = dimensionResource(Rcore.dimen.padding_double),
                alignment = Alignment.CenterVertically
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_no_transfer),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        PrimaryErrorText(
            text = stringResource(R.string.stopdetails_not_found)
        )
    }
}

@Composable
private fun LaunchAction(
    action: UiAction,
    onActionLaunched: () -> Unit,
    onShowOnMap: (StopIdentifier) -> Unit,
    onRequestLocationPermissions: () -> Unit,
    onShowLocationSettings: () -> Unit
) {
    SideEffect(action) {
        when (action) {
            is UiAction.ShowOnMap -> onShowOnMap(action.stopIdentifier)
            is UiAction.RequestLocationPermissions -> onRequestLocationPermissions()
            is UiAction.ShowLocationSettings -> onShowLocationSettings()
        }

        onActionLaunched()
    }
}

@Composable
@ReadOnlyComposable
private fun PaddingValues.toPaddingValuesWithListVerticalPadding(): PaddingValues {
    val paddingDefault = dimensionResource(Rcore.dimen.padding_default)
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = calculateStartPadding(layoutDirection),
        top = calculateTopPadding(),
        end = calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + paddingDefault
    )
}

private val UiServicesItem.listKey: String get() {
    return when (this) {
        is UiServicesItem.Operator.Unknown -> KEY_OPERATOR_UNKNOWN
        is UiServicesItem.Operator.Named -> "operator_$operatorId"
        is UiServicesItem.Service ->
            "service_${serviceDescriptor.operatorCode}_${serviceDescriptor.serviceName}"
        is UiServicesItem.NoServices -> KEY_NO_SERVICES
    }
}

private val UiServicesItem.contentType: String get() {
    return when (this) {
        is UiServicesItem.Operator -> CONTENT_TYPE_OPERATOR
        is UiServicesItem.Service -> CONTENT_TYPE_SERVICE
        is UiServicesItem.NoServices -> CONTENT_TYPE_NO_SERVICES
    }
}

@Preview(
    name = "Stop details screen - light",
    group = "Stop details screen",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Stop details screen - dark",
    group = "Stop details screen",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun StopDetailsScreenPreview(
    @PreviewParameter(UiStateProvider::class) state: UiState
) {
    MyBusTheme {
        CompositionLocalProvider(
            LocalNumberFormatter provides rememberNumberFormatter(
                maximumFractionDigits = 2
            )
        ) {
            StopDetailsScreenWithState(
                state = state,
                onStopMapClick = { },
                onGrantPermissionClick = { },
                onTurnOnLocationClick = { },
                onActionLaunched = { },
                onShowOnMap = { },
                onRequestLocationPermissions = { },
                onShowLocationSettings = { },
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private class UiStateProvider : PreviewParameterProvider<UiState> {

    override val values = sequenceOf(
        UiState(
            content = UiContent.InProgress
        ),
        UiState(
            content = UiContent.Content(
                stopDetails = UiStopDetails(
                    naptanCode = "123456".toNaptanStopIdentifier(),
                    atcoCode = "ATCO987654".toAtcoStopIdentifier(),
                    latLon = UiLatLon(
                        latitude = 1.1,
                        longitude = 2.2
                    ),
                    orientation = StopOrientation.NORTH_EAST,
                    stopDistance = UiStopDistance.Distance.Meters(
                        distance = 123
                    ),
                    isMapShown = true
                ),
                servicesItems = persistentListOf(
                    UiServicesItem.Operator.Named(
                        operatorId = "TEST1",
                        operatorName = "Operator 1"
                    ),
                    UiServicesItem.Service(
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
                    ),
                    UiServicesItem.Service(
                        serviceDescriptor = ServiceDescriptor(
                            serviceName = "2",
                            operatorCode = "TEST1"
                        ),
                        serviceName = UiServiceName(
                            serviceName = "2",
                            colours = null
                        ),
                        description = null
                    ),
                    UiServicesItem.Operator.Unknown,
                    UiServicesItem.Service(
                        serviceDescriptor = ServiceDescriptor(
                            serviceName = "3",
                            operatorCode = "TEST2"
                        ),
                        serviceName = UiServiceName(
                            serviceName = "3",
                            UiServiceColours(
                                backgroundColour = Color.Blue.toArgb(),
                                textColour = Color.White.toArgb()
                            )
                        ),
                        description = "Another service description"
                    )
                )
            )
        ),
        UiState(
            content = UiContent.NoStopDetailsError
        )
    )
}
