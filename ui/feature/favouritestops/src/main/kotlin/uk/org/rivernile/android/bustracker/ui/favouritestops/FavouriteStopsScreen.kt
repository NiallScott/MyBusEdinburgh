/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favouritestops

import android.content.res.Configuration
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
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.text.PrimaryErrorText
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

internal const val TEST_TAG_CONTENT_PROGRESS = "content-progress"
internal const val TEST_TAG_CONTENT_POPULATED = "content-populated"
internal const val TEST_TAG_CONTENT_EMPTY_ERROR = "content-empty-error"
internal const val TEST_TAG_EMPTY_ERROR_TITLE_TEXT = "empty-error-title-text"
internal const val TEST_TAG_EMPTY_ERROR_BODY_TEXT = "empty-error-body-text"

/**
 * The entry point in to the favourite stops screen.
 *
 * @param modifier Anu [Modifier]s which should be applied.
 * @param viewModel An instance of [FavouriteStopsViewModel] to coordinate state.
 * @author Niall Scott
 */
@Composable
internal fun FavouriteStopsScreen(
    modifier: Modifier = Modifier,
    viewModel: FavouriteStopsViewModel = viewModel()
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    FavouriteStopsScreenWithState(
        state = uiState,
        onItemClicked = viewModel::onItemClicked,
        onOpenDropdownClicked = viewModel::onItemOpenDropdownClicked,
        onDropdownMenuDismissed = viewModel::onDropdownMenuDismissed,
        onEditFavouriteNameClick = viewModel::onEditFavouriteNameClicked,
        onRemoveFavouriteClick = viewModel::onRemoveFavouriteClicked,
        onAddArrivalAlertClick = viewModel::onAddArrivalAlertClicked,
        onRemoveArrivalAlertClick = viewModel::onRemoveArrivalAlertClicked,
        onAddProximityAlertClick = viewModel::onAddProximityAlertClicked,
        onRemoveProximityAlertClick = viewModel::onRemoveProximityAlertClicked,
        onShowOnMapClick = viewModel::onShowOnMapClicked,
        onActionLaunched = viewModel::onActionLaunched,
        modifier = modifier
    )
}

/**
 * The entry point in to the favourite stops screen when state is to be passed in directly, for
 * example in tests and previews.
 *
 * @param state The current [UiState].
 * @param onItemClicked A lambda to handle the favourite stop being clicked.
 * @param onOpenDropdownClicked A lambda to handle the favourite stop dropdown button being clicked.
 * @param onDropdownMenuDismissed A lambda to handle the favourite stop dropdown being dismissed.
 * @param onEditFavouriteNameClick A lambda to handle a favourite stop edit item being clicked.
 * @param onRemoveFavouriteClick A lambda to handle a favourite stop remove item being clicked.
 * @param onAddArrivalAlertClick A lambda to handle a favourite stop add arrival alert being
 * clicked.
 * @param onRemoveArrivalAlertClick A lambda to handle a favourite stop remove arrival alert being
 * clicked.
 * @param onAddProximityAlertClick A lambda to handle a favourite stop add proximity alert being
 * clicked.
 * @param onRemoveProximityAlertClick A lambda to handle a favourite stop remove proximity alert
 * being clicked.
 * @param onShowOnMapClick A lambda to handle a favourite stop show on map item being clicked.
 * @param onActionLaunched A lambda to handle an action being launched.
 * @param modifier Any [Modifier]s which should be applied.
 * @author Niall Scott
 */
@Composable
internal fun FavouriteStopsScreenWithState(
    state: UiState,
    onItemClicked: (String) -> Unit,
    onOpenDropdownClicked: (String) -> Unit,
    onDropdownMenuDismissed: () -> Unit,
    onEditFavouriteNameClick: (String) -> Unit,
    onRemoveFavouriteClick: (String) -> Unit,
    onAddArrivalAlertClick: (String) -> Unit,
    onRemoveArrivalAlertClick: (String) -> Unit,
    onAddProximityAlertClick: (String) -> Unit,
    onRemoveProximityAlertClick: (String) -> Unit,
    onShowOnMapClick: (String) -> Unit,
    onActionLaunched: () -> Unit,
    modifier: Modifier = Modifier
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
                favouriteStops = state.content.favouriteStops,
                onItemClicked = onItemClicked,
                onOpenDropdownClicked = onOpenDropdownClicked,
                onDropdownMenuDismissed = onDropdownMenuDismissed,
                onEditFavouriteNameClick = onEditFavouriteNameClick,
                onRemoveFavouriteClick = onRemoveFavouriteClick,
                onAddArrivalAlertClick = onAddArrivalAlertClick,
                onRemoveArrivalAlertClick = onRemoveArrivalAlertClick,
                onAddProximityAlertClick = onAddProximityAlertClick,
                onRemoveProximityAlertClick = onRemoveProximityAlertClick,
                onShowOnMapClick = onShowOnMapClick,
                modifier = Modifier
                    .fillMaxSize()
                    .nestedScroll(nestedScrollInterop)
            )
            is UiContent.Empty -> EmptyError(
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
    favouriteStops: ImmutableList<UiFavouriteStop>,
    onItemClicked: (String) -> Unit,
    onOpenDropdownClicked: (String) -> Unit,
    onDropdownMenuDismissed: () -> Unit,
    onEditFavouriteNameClick: (String) -> Unit,
    onRemoveFavouriteClick: (String) -> Unit,
    onAddArrivalAlertClick: (String) -> Unit,
    onRemoveArrivalAlertClick: (String) -> Unit,
    onAddProximityAlertClick: (String) -> Unit,
    onRemoveProximityAlertClick: (String) -> Unit,
    onShowOnMapClick: (String) -> Unit,
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
            items = favouriteStops,
            key = { it.stopCode }
        ) {
            FavouriteStopItem(
                favouriteStop = it,
                onFavouriteClick = { onItemClicked(it.stopCode) },
                onOpenDropdownClick = { onOpenDropdownClicked(it.stopCode) },
                onDropdownMenuDismissed = onDropdownMenuDismissed,
                onEditFavouriteNameClick = { onEditFavouriteNameClick(it.stopCode) },
                onRemoveFavouriteClick = { onRemoveFavouriteClick(it.stopCode) },
                onAddArrivalAlertClick = { onAddArrivalAlertClick(it.stopCode) },
                onRemoveArrivalAlertClick = { onRemoveArrivalAlertClick(it.stopCode) },
                onAddProximityAlertClick = { onAddProximityAlertClick(it.stopCode) },
                onRemoveProximityAlertClick = { onRemoveProximityAlertClick(it.stopCode) },
                onShowOnMapClick = { onShowOnMapClick(it.stopCode) },
                modifier = Modifier
                    .fillMaxWidth()
                    .animateItem(),
            )
        }
    }
}

@Composable
private fun EmptyError(
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_CONTENT_EMPTY_ERROR
            },
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_star_favourite),
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
            text = stringResource(R.string.favouritestops_nosavedstops_title),
            modifier = Modifier
                .semantics {
                    testTag = TEST_TAG_EMPTY_ERROR_TITLE_TEXT
                },
            fontWeight = FontWeight.Bold
        )

        PrimaryErrorText(
            text = stringResource(R.string.favouritestops_nosavedstops_summary),
            modifier = Modifier
                .semantics {
                    testTag = TEST_TAG_EMPTY_ERROR_BODY_TEXT
                }
        )
    }
}

@Composable
private fun LaunchAction(
    action: UiAction,
    onActionLaunched: () -> Unit
) {
    LaunchedEffect(action) {
        when (action) {
            is UiAction.ShowStopData -> { }
            is UiAction.ShowEditFavouriteStop -> { }
            is UiAction.ShowConfirmRemoveFavourite -> { }
            is UiAction.ShowOnMap -> { }
            is UiAction.ShowAddArrivalAlert -> { }
            is UiAction.ShowConfirmRemoveArrivalAlert -> { }
            is UiAction.ShowAddProximityAlert -> { }
            is UiAction.ShowConfirmRemoveProximityAlert -> { }
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
    name = "Favourite stops screen - light",
    group = "Favourite stops screen",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Favourite stops screen - dark",
    group = "Favourite stops screen",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FavouriteStopsScreenPreview(
    @PreviewParameter(UiStateProvider::class) state: UiState
) {
    MyBusTheme {
        FavouriteStopsScreenWithState(
            state = state,
            modifier = Modifier.fillMaxSize(),
            onItemClicked = { },
            onOpenDropdownClicked = { },
            onDropdownMenuDismissed = { },
            onEditFavouriteNameClick = { },
            onRemoveFavouriteClick = { },
            onAddArrivalAlertClick = { },
            onRemoveArrivalAlertClick = { },
            onAddProximityAlertClick = { },
            onRemoveProximityAlertClick = { },
            onShowOnMapClick = { },
            onActionLaunched = { }
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
                favouriteStops = persistentListOf(
                    UiFavouriteStop(
                        stopCode = "1",
                        savedName = "Favourite 1",
                        services = persistentListOf(
                            UiServiceName(
                                serviceName = "1",
                                colours = UiServiceColours(
                                    backgroundColour = Color.Black.toArgb(),
                                    textColour = Color.White.toArgb()
                                )
                            )
                        ),
                        dropdownMenu = UiFavouriteDropdownMenu()
                    ),
                    UiFavouriteStop(
                        stopCode = "2",
                        savedName = "Favourite 2",
                        services = null,
                        dropdownMenu = UiFavouriteDropdownMenu()
                    ),
                    UiFavouriteStop(
                        stopCode = "3",
                        savedName = "Favourite 3",
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
                            ),
                        ),
                        dropdownMenu = UiFavouriteDropdownMenu()
                    )
                )
            )
        ),
        UiState(
            content = UiContent.Empty
        )
    )
}
