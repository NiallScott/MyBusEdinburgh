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

package uk.org.rivernile.android.bustracker.ui.search

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
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
internal const val TEST_TAG_ERROR_TEXT = "error-text"

/**
 * The entry point in to the search screen.
 *
 * @param modifier Any [Modifier]s which should be applied.
 * @param viewModel An instance of [SearchViewModel] to coordinate state.
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
 * @param onShowOnMap This is called when the stop search result should be shown on a map.
 * @author Niall Scott
 */
@Composable
internal fun SearchScreen(
    modifier: Modifier = Modifier,
    viewModel: SearchViewModel = viewModel(),
    onShowStopData: ((StopIdentifier) -> Unit)? = null,
    onShowAddFavouriteStop: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveFavouriteStop: ((StopIdentifier) -> Unit)? = null,
    onShowAddArrivalAlert: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveArrivalAlert: ((StopIdentifier) -> Unit)? = null,
    onShowAddProximityAlert: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveProximityAlert: ((StopIdentifier) -> Unit)? = null,
    onShowOnMap: ((StopIdentifier) -> Unit)? = null
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    SearchScreenWithState(
        state = uiState,
        onItemClick = viewModel::onItemClicked,
        onOpenDropdownMenuClick = viewModel::onOpenDropdownMenuClicked,
        onDropdownMenuDismissed = viewModel::onDropdownMenuDismissed,
        onAddFavouriteStopClick = viewModel::onAddFavouriteStopClicked,
        onRemoveFavouriteStopClick = viewModel::onRemoveFavouriteStopClicked,
        onAddArrivalAlertClick = viewModel::onAddArrivalAlertClicked,
        onRemoveArrivalAlertClick = viewModel::onRemoveArrivalAlertClicked,
        onAddProximityAlertClick = viewModel::onAddProximityAlertClicked,
        onRemoveProximityAlertClick = viewModel::onRemoveProximityAlertClicked,
        onShowOnMapClick = viewModel::onShowOnMapClicked,
        onActionLaunched = viewModel::onActionLaunched,
        modifier = modifier,
        onShowStopData = onShowStopData,
        onShowAddFavouriteStop = onShowAddFavouriteStop,
        onShowRemoveFavouriteStop = onShowRemoveFavouriteStop,
        onShowAddArrivalAlert = onShowAddArrivalAlert,
        onShowRemoveArrivalAlert = onShowRemoveArrivalAlert,
        onShowAddProximityAlert = onShowAddProximityAlert,
        onShowRemoveProximityAlert = onShowRemoveProximityAlert,
        onShowOnMap = onShowOnMap
    )
}

/**
 * The entry point in to the search screen when state is to be passed in directly, for example in
 * tests and previews.
 *
 * @param state The current [UiState].
 * @param onItemClick This is called when the user has clicked on the stop search result.
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
 * @param onShowOnMapClick This is called when the user clicks on the menu item to show the stop
 * search result on a map.
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
 * @param onShowOnMap This is called when the stop search result should be shown on a map.
 * @author Niall Scott
 */
@Composable
internal fun SearchScreenWithState(
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
    onActionLaunched: () -> Unit,
    modifier: Modifier = Modifier,
    onShowStopData: ((StopIdentifier) -> Unit)? = null,
    onShowAddFavouriteStop: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveFavouriteStop: ((StopIdentifier) -> Unit)? = null,
    onShowAddArrivalAlert: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveArrivalAlert: ((StopIdentifier) -> Unit)? = null,
    onShowAddProximityAlert: ((StopIdentifier) -> Unit)? = null,
    onShowRemoveProximityAlert: ((StopIdentifier) -> Unit)? = null,
    onShowOnMap: ((StopIdentifier) -> Unit)? = null
) {
    Box(
        modifier = modifier
    ) {
        when (val content = state.content) {
            is UiContent.EmptySearchTerm -> EmptySearchTermError(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(dimensionResource(Rcore.dimen.padding_double))
            )
            is UiContent.InProgress -> IndeterminateProgress(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(dimensionResource(Rcore.dimen.padding_double))
            )
            is UiContent.NoResults -> NoResultsError(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(dimensionResource(Rcore.dimen.padding_double))
            )
            is UiContent.Content -> Content(
                searchResults = content.results,
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
            onShowOnMap = onShowOnMap
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

@Composable
private fun EmptySearchTermError(
    modifier: Modifier = Modifier
) {
    ErrorLayout(
        iconResId = R.drawable.ic_error_search,
        textResId = R.string.search_error_empty,
        modifier = modifier
    )
}

@Composable
private fun NoResultsError(
    modifier: Modifier = Modifier
) {
    ErrorLayout(
        iconResId = R.drawable.ic_error_directions_bus,
        textResId = R.string.search_error_no_results,
        modifier = modifier
    )
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun Content(
    searchResults: ImmutableList<UiStopSearchResult>,
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
            items = searchResults,
            key = { it.stopIdentifier.toParcelableStopIdentifier() }
        ) {
            StopSearchResult(
                stopSearchResult = it,
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
private fun ErrorLayout(
    @DrawableRes iconResId: Int,
    @StringRes textResId: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_CONTENT_ERROR
            },
        verticalArrangement = Arrangement
            .spacedBy(
                space = dimensionResource(Rcore.dimen.padding_double),
                alignment = Alignment.CenterVertically
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(iconResId),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        PrimaryErrorText(
            text = stringResource(textResId),
            modifier = Modifier
                .semantics {
                    testTag = TEST_TAG_ERROR_TEXT
                }
        )
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
    onShowServicesChooser: ((Set<ServiceDescriptor>?) -> Unit)? = null,
    onShowLocationSettings: (() -> Unit)? = null,
    onShowAppPermissionSettings: (() -> Unit)? = null
) {
    SideEffect(action) {
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
        top = calculateTopPadding() + paddingDefault,
        end = calculateEndPadding(layoutDirection),
        bottom = calculateBottomPadding() + paddingDefault
    )
}

@Preview(
    name = "Search screen - light",
    group = "Search screen",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Search screen - dark",
    group = "Search screen",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun SearchScreenPreview(
    @PreviewParameter(UiStateProvider::class) state: UiState
) {
    MyBusTheme {
        SearchScreenWithState(
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
            onShowOnMapClick = { }
        )
    }
}

private class UiStateProvider : PreviewParameterProvider<UiState> {

    override val values = sequenceOf(
        UiState(
            content = UiContent.EmptySearchTerm
        ),
        UiState(
            content = UiContent.InProgress
        ),
        UiState(
            content = UiContent.NoResults
        ),
        UiState(
            content = UiContent.Content(
                results = persistentListOf(
                    UiStopSearchResult(
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
                        dropdownMenu = UiStopSearchResultDropdownMenu()
                    ),
                    UiStopSearchResult(
                        stopIdentifier = "2".toNaptanStopIdentifier(),
                        stopName = UiStopName(
                            name = "Name 2",
                            locality = "Locality 2"
                        ),
                        services = null,
                        orientation = StopOrientation.SOUTH_EAST,
                        dropdownMenu = UiStopSearchResultDropdownMenu()
                    ),
                    UiStopSearchResult(
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
                        dropdownMenu = UiStopSearchResultDropdownMenu()
                    )
                )
            )
        )
    )
}
