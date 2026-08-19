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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.busstops.toContentDescriptionStringResId
import uk.org.rivernile.android.bustracker.core.busstops.toIconDrawableResId
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.text.LocalStopNameFormatter
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.SmallDecoratedServiceNamesListingText
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore

internal const val TEST_TAG_STOP_MARKER_ICON = "icon-stop-marker"
internal const val TEST_TAG_STOP_NAME = "stop-name"
internal const val TEST_TAG_SERVICES_LISTING = "services-listing"
internal const val TEST_TAG_DROPDOWN_INDICATOR = "dropdown-indicator"
internal const val TEST_TAG_DROPDOWN_MENU = "dropdown-menu"

/**
 * This composes a stop search result item.
 *
 * @param stopSearchResult The stop search result to render,
 * @param onClick This is called when the user has clicked on the stop search result.
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
 * @param modifier Any [Modifier]s which should be applied.
 */
@Composable
internal fun StopSearchResult(
    stopSearchResult: UiStopSearchResult,
    onClick: () -> Unit,
    onOpenDropdownMenuClick: () -> Unit,
    onDropdownMenuDismissed: () -> Unit,
    onAddFavouriteStopClick: () -> Unit,
    onRemoveFavouriteStopClick: () -> Unit,
    onAddArrivalAlertClick: () -> Unit,
    onRemoveArrivalAlertClick: () -> Unit,
    onAddProximityAlertClick: () -> Unit,
    onRemoveProximityAlertClick: () -> Unit,
    onShowOnMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paddingDouble = dimensionResource(Rcore.dimen.padding_double)

    Row(
        modifier = modifier
            .clickable(onClick = onClick)
            .focusable(true)
            .heightIn(
                min = if (stopSearchResult.services != null) 72.dp else 56.dp
            )
            .safeDrawingPadding()
            .padding(
                horizontal = paddingDouble,
                vertical = dimensionResource(Rcore.dimen.padding_default)
            ),
        horizontalArrangement = Arrangement.spacedBy(paddingDouble),
        verticalAlignment = Alignment.CenterVertically
    ) {
        StopMarkerIcon(
            orientation = stopSearchResult.orientation
        )

        DetailsColumn(
            stopIdentifier = stopSearchResult.stopIdentifier,
            stopName = stopSearchResult.stopName,
            services = stopSearchResult.services,
            modifier = Modifier
                .weight(1f)
        )

        DropdownMenuBox(
            dropdownMenu = stopSearchResult.dropdownMenu,
            onOpenDropdownMenuClick = onOpenDropdownMenuClick,
            onDropdownMenuDismissed = onDropdownMenuDismissed,
            onAddFavouriteStopClick = onAddFavouriteStopClick,
            onRemoveFavouriteStopClick = onRemoveFavouriteStopClick,
            onAddArrivalAlertClick = onAddArrivalAlertClick,
            onRemoveArrivalAlertClick = onRemoveArrivalAlertClick,
            onAddProximityAlertClick = onAddProximityAlertClick,
            onRemoveProximityAlertClick = onRemoveProximityAlertClick,
            onShowOnMapClick = onShowOnMapClick
        )
    }
}

@Composable
private fun StopMarkerIcon(
    orientation: StopOrientation,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(orientation.toIconDrawableResId()),
        contentDescription = stringResource(orientation.toContentDescriptionStringResId()),
        modifier = modifier
            .defaultMinSize(
                minWidth = 32.dp,
                minHeight = 32.dp
            )
            .semantics {
                testTag = TEST_TAG_STOP_MARKER_ICON
            },
        tint = Color.Unspecified
    )
}

@Composable
private fun DetailsColumn(
    stopIdentifier: StopIdentifier,
    stopName: UiStopName,
    services: ImmutableList<UiServiceName>?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(Rcore.dimen.padding_half))
    ) {
        StopName(
            stopIdentifier = stopIdentifier,
            stopName = stopName
        )

        if (services != null) {
            StopServices(
                services = services
            )
        }
    }
}

@Composable
private fun StopName(
    stopIdentifier: StopIdentifier,
    stopName: UiStopName,
    modifier: Modifier = Modifier
) {
    Text(
        text = LocalStopNameFormatter.current.formatBusStopNameWithStopIdentifier(
            stopIdentifier = stopIdentifier,
            stopName = stopName
        ),
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_STOP_NAME
            },
        color = MaterialTheme.colorScheme.onSurface,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun StopServices(
    services: ImmutableList<UiServiceName>,
    modifier: Modifier = Modifier
) {
    SmallDecoratedServiceNamesListingText(
        services = services,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_SERVICES_LISTING
            },
        itemPadding = PaddingValues(dimensionResource(Rcore.dimen.padding_half))
    )
}

@Composable
private fun DropdownMenuBox(
    dropdownMenu: UiStopSearchResultDropdownMenu,
    onOpenDropdownMenuClick: () -> Unit,
    onDropdownMenuDismissed: () -> Unit,
    onAddFavouriteStopClick: () -> Unit,
    onRemoveFavouriteStopClick: () -> Unit,
    onAddArrivalAlertClick: () -> Unit,
    onRemoveArrivalAlertClick: () -> Unit,
    onAddProximityAlertClick: () -> Unit,
    onRemoveProximityAlertClick: () -> Unit,
    onShowOnMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    // A Box is required as the DropdownMenu requires a correctly positioned parent to anchor to.
    Box(
        modifier = modifier
    ) {
        DropdownMenuIconButton(
            onClick = onOpenDropdownMenuClick
        )

        StopSearchResultDropdownMenu(
            menu = dropdownMenu,
            onDropdownMenuDismissed = onDropdownMenuDismissed,
            onAddFavouriteStopClick = onAddFavouriteStopClick,
            onRemoveFavouriteStopClick = onRemoveFavouriteStopClick,
            onAddArrivalAlertClick = onAddArrivalAlertClick,
            onRemoveArrivalAlertClick = onRemoveArrivalAlertClick,
            onAddProximityAlertClick = onAddProximityAlertClick,
            onRemoveProximityAlertClick = onRemoveProximityAlertClick,
            onShowOnMapClick = onShowOnMapClick,
            modifier = Modifier
                .semantics {
                    testTag = TEST_TAG_DROPDOWN_MENU
                }
        )
    }
}

@Composable
private fun DropdownMenuIconButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    IconButton(
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_DROPDOWN_INDICATOR
            }
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_more_vert),
            contentDescription = stringResource(R.string.search_stop_dropdown_content_description),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(
    name = "Stop search result - light",
    group = "Stop search result",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Stop search result - dark",
    group = "Stop search result",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun StopSearchResultPreview(
    @PreviewParameter(UiStopSearchResultProvider::class) stopSearchResult: UiStopSearchResult
) {
    MyBusTheme {
        StopSearchResult(
            stopSearchResult = stopSearchResult,
            onClick = { },
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

private class UiStopSearchResultProvider : PreviewParameterProvider<UiStopSearchResult> {

    override val values = sequenceOf(
        UiStopSearchResult(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            stopName = UiStopName(
                name = "Stop",
                locality = "Locality"
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
                )
            ),
            orientation = StopOrientation.NORTH_EAST,
            dropdownMenu = UiStopSearchResultDropdownMenu()
        ),
        UiStopSearchResult(
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            stopName = UiStopName(
                name = "Stop",
                locality = "Locality"
            ),
            services = null,
            orientation = StopOrientation.NORTH_EAST,
            dropdownMenu = UiStopSearchResultDropdownMenu()
        )
    )
}

