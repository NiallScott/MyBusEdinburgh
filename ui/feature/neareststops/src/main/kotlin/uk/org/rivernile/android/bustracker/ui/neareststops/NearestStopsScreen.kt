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

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
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
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toParcelableStopIdentifier
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.PrimaryErrorText
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore

internal const val TEST_TAG_CONTENT_PROGRESS = "content-progress"
internal const val TEST_TAG_CONTENT_POPULATED = "content-populated"
internal const val TEST_TAG_CONTENT_ERROR = "content-error"
internal const val TEST_TAG_ERROR_TITLE = "error-title"
internal const val TEST_TAG_ERROR_BLURB = "error-blurb"

/**
 * The entry point in to the nearest stops screen.
 *
 * @param modifier Any [Modifier]s which should be applied.
 * @param viewModel An instance of [NearestStopsViewModel] to coordinate state.
 * @param onShowStopData This is called when stop data should be shown.
 * @param onShowAddFavouriteStop This is called when the UI to add a favourite stop should be shown.
 * @param onShowRemoveFavouriteStop This is called when the UI to remove a favourite stop should be
 * shown.
 * @param onShowAddArrivalAlert This is called when the UI to add an arrival alert should be shown.
 * @param onShowRemoveArrivalAlert This is called when it should be confirmed with the user if an
 * arrival alert should be removed.
 * @param onShowAddProximityAlert This is called when the UI to add a proximity alert should be
 * shown.
 * @param onShowRemoveProximityAlert This is called when it should be confirmed with the user if a
 * proximity alert should be removed.
 * @param onShowOnMap This is called when the nearest stop should be shown on a map.
 * @param onRequestLocationPermissions This is called when location permissions should be requested.
 * @param onShowServicesChooser This is called when the services chooser should be shown.
 * @param onShowLocationSettings This is called when the system location settings should be shown.
 * @param onShowTurnOnGps This is called when UI to turn on GPS should be shown.
 * @author Niall Scott
 */
@Composable
internal fun NearestStopsScreen(
    modifier: Modifier = Modifier,
    viewModel: NearestStopsViewModel = viewModel(),
    onShowStopData: ((StopIdentifier) -> Unit)? = null,
    onShowAddFavouriteStop: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveFavouriteStop: ((StopIdentifier) -> Unit)? = null,
    onShowAddArrivalAlert: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveArrivalAlert: ((StopIdentifier) -> Unit)? = null,
    onShowAddProximityAlert: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveProximityAlert: ((StopIdentifier) -> Unit)? = null,
    onShowOnMap: ((StopIdentifier) -> Unit)? = null,
    onRequestLocationPermissions: (() -> Unit)? = null,
    onShowServicesChooser: ((ImmutableList<ServiceDescriptor>?) -> Unit)? = null,
    onShowLocationSettings: (() -> Unit)? = null,
    onShowTurnOnGps: (() -> Unit)? = null
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    NearestStopsScreenWithState(
        state = uiState,
        onActionLaunched = viewModel::onActionLaunched,
        onItemClick = viewModel::onItemClicked,
        onOpenDropdownMenuClick = viewModel::onOpenDropdownMenuClicked,
        onDropdownMenuDismissed = viewModel::onDropdownMenuDismissed,
        onAddFavouriteStopClick = viewModel::onAddFavouriteStopClicked,
        onRemoveFavouriteStopClick = viewModel::onRemoveFavouriteStopClicked,
        onAddArrivalAlertClick = viewModel::onAddArrivalAlertClicked,
        onRemoveArrivalAlertClick = viewModel::onRemoveArrivalAlertClicked,
        onAddProximityAlertClick = viewModel::onAddProximityAlertClicked,
        onRemoveProximityAlertClick = viewModel::onRemoveProximityAlertCLicked,
        onShowOnMapClick = viewModel::onShowOnMapClicked,
        onGrantPermissionClick = viewModel::onGrantPermissionClicked,
        onOpenSettingsClick = viewModel::onOpenSettingsClicked,
        onShowStopData = onShowStopData,
        onShowAddFavouriteStop = onShowAddFavouriteStop,
        onShowRemoveFavouriteStop = onShowRemoveFavouriteStop,
        onShowAddArrivalAlert = onShowAddArrivalAlert,
        onShowRemoveArrivalAlert = onShowRemoveArrivalAlert,
        onShowAddProximityAlert = onShowAddProximityAlert,
        onShowRemoveProximityAlert = onShowRemoveProximityAlert,
        onShowOnMap = onShowOnMap,
        onRequestLocationPermissions = onRequestLocationPermissions,
        onShowServicesChooser = onShowServicesChooser,
        onShowLocationSettings = onShowLocationSettings,
        onShowTurnOnGps = onShowTurnOnGps
    )
}

/**
 * The entry point in to the nearest stops screen when state is to be passed in directly, for
 * example in tests and previews.
 *
 * @param state The current [UiState].
 * @param onItemClick This is called when the user has clicked on the nearest stop.
 * @param onOpenDropdownMenuClick This is called when the user has clicked on the button to show
 * the dropdown menu.
 * @param onDropdownMenuDismissed This is called when the dropdown meny has been dismissed.
 * @param onAddFavouriteStopClick This is called when the user clicks on the menu item to add a
 * favourite stop.
 * @param onRemoveFavouriteStopClick This is called when the user clicks on the menu item to remove
 * a favourite stop.
 * @param onAddArrivalAlertClick This is called when the user clicks on the menu item to add an
 * arrival alert.
 * @param onRemoveArrivalAlertClick This is called when the user clicks on the menu item to remove
 * an arrival alert.
 * @param onAddProximityAlertClick This is called when the user clicks on the menu item to add a
 * proximity alert.
 * @param onRemoveProximityAlertClick This is called when the user clicks on the menu item to remove
 * a proximity alert.
 * @param onShowOnMapClick This is called when the user clicks on the menu item to show the nearest
 * stop on a map.
 * @param onGrantPermissionClick This is called when the user clicks on the grant permission error
 * resolution button.
 * @param onOpenSettingsClick This is called when the user clicks on the open settings error
 * resolution button.
 * @param onActionLaunched This is called when an action has been launched.
 * @param modifier Any [Modifier]s which should be applied.
 * @param onShowStopData This is called when stop data should be shown.
 * @param onShowAddFavouriteStop This is called when the UI to add a favourite stop should be shown.
 * @param onShowRemoveFavouriteStop This is called when the UI to remove a favourite stop should be
 * shown.
 * @param onShowAddArrivalAlert This is called when the UI to add an arrival alert should be shown.
 * @param onShowRemoveArrivalAlert This is called when it should be confirmed with the user if an
 * arrival alert should be removed.
 * @param onShowAddProximityAlert This is called when the UI to add a proximity alert should be
 * shown.
 * @param onShowRemoveProximityAlert This is called when it should be confirmed with the user if a
 * proximity alert should be removed.
 * @param onShowOnMap This is called when the nearest stop should be shown on a map.
 * @param onRequestLocationPermissions This is called when location permissions should be requested.
 * @param onShowServicesChooser This is called when the services chooser should be shown.
 * @param onShowLocationSettings This is called when the system location settings should be shown.
 * @param onShowTurnOnGps This is called when UI to turn on GPS should be shown.
 * @author Niall Scott
 */
@Composable
internal fun NearestStopsScreenWithState(
    state: UiState,
    onItemClick: (StopIdentifier) -> Unit,
    onOpenDropdownMenuClick: (StopIdentifier) -> Unit,
    onDropdownMenuDismissed: () -> Unit,
    onAddFavouriteStopClick: (StopIdentifier) -> Unit,
    onRemoveFavouriteStopClick: (StopIdentifier) -> Unit,
    onAddArrivalAlertClick: (StopIdentifier) -> Unit,
    onRemoveArrivalAlertClick: (StopIdentifier) -> Unit,
    onAddProximityAlertClick: (StopIdentifier) -> Unit,
    onRemoveProximityAlertClick: (StopIdentifier) -> Unit,
    onShowOnMapClick: (StopIdentifier) -> Unit,
    onGrantPermissionClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    onActionLaunched: () -> Unit,
    modifier: Modifier = Modifier,
    onShowStopData: ((StopIdentifier) -> Unit)? = null,
    onShowAddFavouriteStop: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveFavouriteStop: ((StopIdentifier) -> Unit)? = null,
    onShowAddArrivalAlert: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveArrivalAlert: ((StopIdentifier) -> Unit)? = null,
    onShowAddProximityAlert: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveProximityAlert: ((StopIdentifier) -> Unit)? = null,
    onShowOnMap: ((StopIdentifier) -> Unit)? = null,
    onRequestLocationPermissions: (() -> Unit)? = null,
    onShowServicesChooser: ((ImmutableList<ServiceDescriptor>?) -> Unit)? = null,
    onShowLocationSettings: (() -> Unit)? = null,
    onShowTurnOnGps: (() -> Unit)? = null
) {
    val paddingDouble = dimensionResource(Rcore.dimen.padding_double)

    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        val nestedScrollInterop = rememberNestedScrollInteropConnection()

        when (state.content) {
            is UiContent.InProgress -> IndeterminateProgress(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(paddingDouble)
                    .nestedScroll(nestedScrollInterop)
                    .verticalScroll(rememberScrollState())
            )
            is UiContent.Content -> Content(
                nearestStops = state.content.nearestStops,
                onItemClick = onItemClick,
                onOpenDropdownMenuClick = onOpenDropdownMenuClick,
                onDropdownMenuDismissed = onDropdownMenuDismissed,
                onAddFavouriteStopClick = onAddFavouriteStopClick,
                onRemoveFavouriteStopClick = onRemoveFavouriteStopClick,
                onAddArrivalAlertClick = onAddArrivalAlertClick,
                onRemoveArrivalAlertClick = onRemoveArrivalAlertClick,
                onAddProximityAlertClick = onAddProximityAlertClick,
                onRemoveProximityAlertClick = onRemoveProximityAlertClick,
                onShowOnMapClick = onShowOnMapClick,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollInterop)
            )
            is UiContent.Error -> NearestStopsError(
                error = state.content,
                onGrantPermissionClick = onGrantPermissionClick,
                onOpenSettingsClick = onOpenSettingsClick,
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(paddingDouble)
                    .nestedScroll(nestedScrollInterop)
                    .verticalScroll(rememberScrollState())
            )
        }
    }

    state.action?.let {
        LaunchAction(
            action = it,
            onActionLaunched = onActionLaunched,
            onShowStopData = onShowStopData,
            onShowAddFavouriteStop = onShowAddFavouriteStop,
            onShowRemoveFavouriteStop = onShowRemoveFavouriteStop,
            onShowAddArrivalAlert = onShowAddArrivalAlert,
            onShowRemoveArrivalAlert = onShowRemoveArrivalAlert,
            onShowAddProximityAlert = onShowAddProximityAlert,
            onShowRemoveProximityAlert = onShowRemoveProximityAlert,
            onShowOnMap = onShowOnMap,
            onRequestLocationPermissions = onRequestLocationPermissions,
            onShowServicesChooser = onShowServicesChooser,
            onShowLocationSettings = onShowLocationSettings,
            onShowTurnOnGps = onShowTurnOnGps
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
    nearestStops: ImmutableList<UiNearestStop>,
    onItemClick: (StopIdentifier) -> Unit,
    onOpenDropdownMenuClick: (StopIdentifier) -> Unit,
    onDropdownMenuDismissed: () -> Unit,
    onAddFavouriteStopClick: (StopIdentifier) -> Unit,
    onRemoveFavouriteStopClick: (StopIdentifier) -> Unit,
    onAddArrivalAlertClick: (StopIdentifier) -> Unit,
    onRemoveArrivalAlertClick: (StopIdentifier) -> Unit,
    onAddProximityAlertClick: (StopIdentifier) -> Unit,
    onRemoveProximityAlertClick: (StopIdentifier) -> Unit,
    onShowOnMapClick: (StopIdentifier) -> Unit,
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
        items(
            items = nearestStops,
            key = { it.stopIdentifier.toParcelableStopIdentifier() }
        ) {
            NearestStopItem(
                nearestStop = it,
                onClick = { onItemClick(it.stopIdentifier) },
                onOpenDropdownMenuClick = { onOpenDropdownMenuClick(it.stopIdentifier) },
                onDropdownMenuDismissed = onDropdownMenuDismissed,
                onAddFavouriteStopClick = { onAddFavouriteStopClick(it.stopIdentifier) },
                onRemoveFavouriteStopClick = { onRemoveFavouriteStopClick(it.stopIdentifier) },
                onAddArrivalAlertClick = { onAddArrivalAlertClick(it.stopIdentifier) },
                onRemoveArrivalAlertClick = { onRemoveArrivalAlertClick(it.stopIdentifier) },
                onAddProximityAlertClick = { onAddProximityAlertClick(it.stopIdentifier) },
                onRemoveProximityAlertClick = { onRemoveProximityAlertClick(it.stopIdentifier) },
                onShowOnMapClick = { onShowOnMapClick(it.stopIdentifier) },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem()
            )
        }
    }
}

@Composable
private fun NearestStopsError(
    error: UiContent.Error,
    onGrantPermissionClick: () -> Unit,
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (error) {
        is UiContent.Error.NoLocationFeature -> NoLocationFeatureError(
            modifier = modifier
        )
        is UiContent.Error.InsufficientLocationPermissions -> InsufficientLocationPermissionsError(
            onGrantPermissionClick = onGrantPermissionClick,
            modifier = modifier
        )
        is UiContent.Error.LocationOff -> LocationOffError(
            onOpenSettingsClick = onOpenSettingsClick,
            modifier = modifier
        )
        is UiContent.Error.LocationUnknown -> LocationUnknownError(
            modifier = modifier
        )
        is UiContent.Error.NoNearestStops -> NoNearestStopsError(
            modifier = modifier
        )
    }
}

@Composable
private fun NoLocationFeatureError(
    modifier: Modifier = Modifier
) {
    ErrorLayout(
        iconResId = R.drawable.ic_error_location_disabled,
        titleTextResId = R.string.neareststops_error_no_location_feature_title,
        blurbTextResId = R.string.neareststops_error_no_location_feature_blurb,
        modifier = modifier
    )
}

@Composable
private fun InsufficientLocationPermissionsError(
    onGrantPermissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorLayout(
        iconResId = R.drawable.ic_error_perm_device_information,
        titleTextResId = R.string.neareststops_error_permission_required_title,
        blurbTextResId = R.string.neareststops_error_permission_required_blurb,
        modifier = modifier
    ) {
        TextButton(
            onClick = onGrantPermissionClick
        ) {
            Text(
                text = stringResource(R.string.neareststops_error_permission_required_button)
            )
        }
    }
}

@Composable
private fun LocationOffError(
    onOpenSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ErrorLayout(
        iconResId = R.drawable.ic_error_location_disabled,
        titleTextResId = R.string.neareststops_error_location_sources_title,
        blurbTextResId = R.string.neareststops_error_location_sources_blurb,
        modifier = modifier
    ) {
        TextButton(
            onClick = onOpenSettingsClick
        ) {
            Text(
                text = stringResource(R.string.neareststops_error_location_sources_button)
            )
        }
    }
}

@Composable
private fun LocationUnknownError(
    modifier: Modifier = Modifier
) {
    ErrorLayout(
        iconResId = R.drawable.ic_error_location_disabled,
        titleTextResId = R.string.neareststops_error_location_unknown_title,
        blurbTextResId = R.string.neareststops_error_location_unknown_blurb,
        modifier = modifier
    )
}

@Composable
private fun NoNearestStopsError(
    modifier: Modifier = Modifier
) {
    ErrorLayout(
        iconResId = R.drawable.ic_error_my_location,
        titleTextResId = R.string.neareststops_error_empty_title,
        blurbTextResId = R.string.neareststops_error_empty_blurb,
        modifier = modifier
    )
}

@Composable
private fun ErrorLayout(
    @DrawableRes iconResId: Int,
    @StringRes titleTextResId: Int,
    @StringRes blurbTextResId: Int,
    modifier: Modifier = Modifier,
    resolveButton: (@Composable () -> Unit)? = null
) {
    Column(
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_CONTENT_ERROR
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        Spacer(
            modifier = Modifier
                .height(dimensionResource(Rcore.dimen.padding_double))
        )

        PrimaryErrorText(
            text = stringResource(titleTextResId),
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .semantics {
                    testTag = TEST_TAG_ERROR_TITLE
                }
        )

        PrimaryErrorText(
            text = stringResource(blurbTextResId),
            modifier = Modifier
                .semantics {
                    testTag = TEST_TAG_ERROR_BLURB
                }
        )

        if (resolveButton != null) {
            Spacer(
                modifier = Modifier
                    .height(dimensionResource(Rcore.dimen.padding_default))
            )

            resolveButton()
        }
    }
}

@Composable
private fun LaunchAction(
    action: UiAction,
    onActionLaunched: () -> Unit,
    onShowStopData: ((StopIdentifier) -> Unit)? = null,
    onShowAddFavouriteStop: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveFavouriteStop: ((StopIdentifier) -> Unit)? = null,
    onShowAddArrivalAlert: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveArrivalAlert: ((StopIdentifier) -> Unit)? = null,
    onShowAddProximityAlert: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveProximityAlert: ((StopIdentifier) -> Unit)? = null,
    onShowOnMap: ((StopIdentifier) -> Unit)? = null,
    onRequestLocationPermissions: (() -> Unit)? = null,
    onShowServicesChooser: ((ImmutableList<ServiceDescriptor>?) -> Unit)? = null,
    onShowLocationSettings: (() -> Unit)? = null,
    onShowTurnOnGps: (() -> Unit)? = null
) {
    LaunchedEffect(action) {
        when (action) {
            is UiAction.ShowStopData -> onShowStopData?.invoke(action.stopIdentifier)
            is UiAction.ShowAddFavouriteStop ->
                onShowAddFavouriteStop?.invoke(action.stopIdentifier)
            is UiAction.ShowRemoveFavouriteStop ->
                onShowRemoveFavouriteStop?.invoke(action.stopIdentifier)
            is UiAction.ShowAddArrivalAlert ->
                onShowAddArrivalAlert?.invoke(action.stopIdentifier)
            is UiAction.ShowRemoveArrivalAlert ->
                onShowRemoveArrivalAlert?.invoke(action.stopIdentifier)
            is UiAction.ShowAddProximityAlert ->
                onShowAddProximityAlert?.invoke(action.stopIdentifier)
            is UiAction.ShowRemoveProximityAlert ->
                onShowRemoveProximityAlert?.invoke(action.stopIdentifier)
            is UiAction.ShowOnMap ->
                onShowOnMap?.invoke(action.stopIdentifier)
            is UiAction.RequestLocationPermissions -> onRequestLocationPermissions?.invoke()
            is UiAction.ShowServicesChooser ->
                onShowServicesChooser?.invoke(action.selectedServices)
            is UiAction.ShowLocationSettings -> onShowLocationSettings?.invoke()
            is UiAction.ShowTurnOnGps -> onShowTurnOnGps?.invoke()
        }

        onActionLaunched()
    }
}

@Composable
private fun PaddingValues.toPaddingValuesWithListVerticalPadding(): PaddingValues {
    val paddingDefault = dimensionResource(Rcore.dimen.padding_default)
    val layoutDirection = LocalLayoutDirection.current

    return PaddingValues(
        start = calculateStartPadding(layoutDirection),
        top = calculateTopPadding() + paddingDefault,
        end = calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + paddingDefault
    )
}

@Preview(
    name = "Nearest stops screen - light",
    group = "Nearest stops screen",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Nearest stops screen - dark",
    group = "Nearest stops screen",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun NearestStopsScreenPreview(
    @PreviewParameter(UiStateProvider::class) state: UiState
) {
    MyBusTheme {
        NearestStopsScreenWithState(
            state = state,
            modifier = Modifier.fillMaxSize(),
            onActionLaunched = { },
            onItemClick = { },
            onOpenDropdownMenuClick = { },
            onDropdownMenuDismissed = { },
            onAddFavouriteStopClick = { },
            onRemoveFavouriteStopClick = { },
            onAddArrivalAlertClick = { },
            onRemoveArrivalAlertClick = { },
            onAddProximityAlertClick = { },
            onRemoveProximityAlertClick = { },
            onShowOnMapClick = { },
            onGrantPermissionClick = { },
            onOpenSettingsClick = { }
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
                persistentListOf(
                    UiNearestStop(
                        stopIdentifier = "1".toNaptanStopIdentifier(),
                        stopName = UiStopName(
                            name = "Name 1",
                            locality = "Locality 1"
                        ),
                        services = persistentListOf(
                            UiServiceName(
                                serviceName = "1",
                                colours = UiServiceColours(
                                    backgroundColour = Color.Black.toArgb(),
                                    textColour = Color.White.toArgb()
                                )
                            )
                        ),
                        orientation = StopOrientation.NORTH,
                        distanceMeters = 123,
                        dropdownMenu = UiNearestStopDropdownMenu()
                    ),
                    UiNearestStop(
                        stopIdentifier = "2".toNaptanStopIdentifier(),
                        stopName = UiStopName(
                            name = "Name 2",
                            locality = "Locality 2"
                        ),
                        services = null,
                        orientation = StopOrientation.SOUTH_EAST,
                        distanceMeters = 456,
                        dropdownMenu = UiNearestStopDropdownMenu()
                    ),
                    UiNearestStop(
                        stopIdentifier = "3".toNaptanStopIdentifier(),
                        stopName = UiStopName(
                            name = "Name 3",
                            locality = "Locality 3"
                        ),
                        services = persistentListOf(
                            UiServiceName(
                                serviceName = "1",
                                colours = UiServiceColours(
                                    backgroundColour = Color.Black.toArgb(),
                                    textColour = Color.White.toArgb()
                                )
                            ),
                            UiServiceName(
                                serviceName = "2",
                                colours = UiServiceColours(
                                    backgroundColour = Color.Red.toArgb(),
                                    textColour = Color.White.toArgb()
                                )
                            ),
                            UiServiceName(
                                serviceName = "3",
                                colours = UiServiceColours(
                                    backgroundColour = Color.Yellow.toArgb(),
                                    textColour = Color.Black.toArgb()
                                )
                            ),
                            UiServiceName(
                                serviceName = "4",
                                colours = UiServiceColours(
                                    backgroundColour = Color.Green.toArgb(),
                                    textColour = Color.White.toArgb()
                                )
                            ),
                            UiServiceName(
                                serviceName = "5",
                                colours = UiServiceColours(
                                    backgroundColour = Color.LightGray.toArgb(),
                                    textColour = Color.Black.toArgb()
                                )
                            )
                        ),
                        orientation = StopOrientation.WEST,
                        distanceMeters = 789,
                        dropdownMenu = UiNearestStopDropdownMenu()
                    )
                )
            )
        ),
        UiState(
            content = UiContent.Error.NoLocationFeature
        ),
        UiState(
            content = UiContent.Error.InsufficientLocationPermissions
        ),
        UiState(
            content = UiContent.Error.LocationOff
        ),
        UiState(
            content = UiContent.Error.LocationUnknown
        ),
        UiState(
            content = UiContent.Error.NoNearestStops
        )
    )
}
