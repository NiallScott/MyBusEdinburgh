/*
 * Copyright (C) 2025 - 2026 Niall 'Rivernile' Scott
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
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
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
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.text.SmallDecoratedServiceNamesListingText
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

internal const val TEST_TAG_FAVOURITE_ICON = "favourite-icon"
internal const val TEST_TAG_SAVED_NAME = "saved-name"
internal const val TEST_TAG_SERVICES_LISTING = "services-listing"
internal const val TEST_TAG_DROPDOWN_INDICATOR = "dropdown-indicator"
internal const val TEST_TAG_DROPDOWN_MENU = "dropdown-menu"

/**
 * This composes a favourite stop item.
 *
 * @param favouriteStop The favourite stop to render.
 * @param onFavouriteClick This is called when the user has clicked on the favourite stop.
 * @param onOpenDropdownClick This is called when the user has clicked on the button to show the
 * dropdown menu.
 * @param onDropdownMenuDismissed This is called when the dropdown menu has been dismissed.
 * @param onEditFavouriteNameClick This is called when the user clicks on the menu item to edit
 * their favourite stop.
 * @param onRemoveFavouriteClick This is called when the user clicks on the menu item to remove a
 * favourite stop.
 * @param onAddShortcutClick This is called when the user clicks on the menu item to add a shortcut
 * for a favourite stop.
 * @param onAddArrivalAlertClick This is called when the user clicks on the menu item to add an
 * arrival alert.
 * @param onRemoveArrivalAlertClick This is called when the user clicks on the menu item to remove
 * an arrival alert.
 * @param onAddProximityAlertClick This is called when the user clicks on the menu item to add a
 * proximity alert.
 * @param onRemoveProximityAlertClick This is called when the user clicks on the menu item to remove
 * a proximity alert.
 * @param onShowOnMapClick This is called when the user clicks on the menu item to show the
 * favourite stop on a map.
 * @param modifier Any [Modifier]s which should be applied this this composable.
 * @author Niall Scott
 */
@Composable
internal fun FavouriteStopItem(
    favouriteStop: UiFavouriteStop,
    onFavouriteClick: () -> Unit,
    onOpenDropdownClick: () -> Unit,
    onDropdownMenuDismissed: () -> Unit,
    onEditFavouriteNameClick: () -> Unit,
    onRemoveFavouriteClick: () -> Unit,
    onAddShortcutClick: () -> Unit,
    onAddArrivalAlertClick: () -> Unit,
    onRemoveArrivalAlertClick: () -> Unit,
    onAddProximityAlertClick: () -> Unit,
    onRemoveProximityAlertClick: () -> Unit,
    onShowOnMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val paddingDefault = dimensionResource(Rcore.dimen.padding_default)
    val paddingDouble = dimensionResource(Rcore.dimen.padding_double)

    Row(
        modifier = modifier
            .clickable(onClick = onFavouriteClick)
            .focusable(true)
            .heightIn(
                min = if (favouriteStop.services != null) 72.dp else 56.dp
            )
            .safeDrawingPadding()
            .padding(
                horizontal = paddingDouble,
                vertical = paddingDefault
            ),
        horizontalArrangement = Arrangement.spacedBy(paddingDouble),
        verticalAlignment = Alignment.CenterVertically
    ) {
        FavouriteStopIcon()

        DetailsColumn(
            savedName = favouriteStop.savedName,
            services = favouriteStop.services,
            modifier = Modifier
                .weight(1f)
        )

        favouriteStop.dropdownMenu?.let { dropdownMenu ->
            DropdownMenuBox(
                dropdownMenu = dropdownMenu,
                onOpenDropdownClick = onOpenDropdownClick,
                onDropdownMenuDismissed = onDropdownMenuDismissed,
                onEditFavouriteNameClick = onEditFavouriteNameClick,
                onRemoveFavouriteClick = onRemoveFavouriteClick,
                onAddShortcutClick = onAddShortcutClick,
                onAddArrivalAlertClick = onAddArrivalAlertClick,
                onRemoveArrivalAlertClick = onRemoveArrivalAlertClick,
                onAddProximityAlertClick = onAddProximityAlertClick,
                onRemoveProximityAlertClick = onRemoveProximityAlertClick,
                onShowOnMapClick = onShowOnMapClick
            )
        }
    }
}

@Composable
private fun FavouriteStopIcon(
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(R.drawable.ic_star_favourite),
        contentDescription = stringResource(R.string.favouritestops_star_content_description),
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_FAVOURITE_ICON
            },
        tint = MaterialTheme.colorScheme.tertiary
    )
}

@Composable
private fun DetailsColumn(
    savedName: String,
    services: ImmutableList<UiServiceName>?,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(dimensionResource(Rcore.dimen.padding_half))
    ) {
        FavouriteStopName(
            name = savedName
        )

        if (services != null) {
            FavouriteStopServices(
                services = services
            )
        }
    }
}

@Composable
private fun FavouriteStopName(
    name: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = name,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_SAVED_NAME
            },
        color = MaterialTheme.colorScheme.onSurface,
        overflow = TextOverflow.Ellipsis,
        maxLines = 2,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun FavouriteStopServices(
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
    dropdownMenu: UiFavouriteDropdownMenu,
    onOpenDropdownClick: () -> Unit,
    onDropdownMenuDismissed: () -> Unit,
    onEditFavouriteNameClick: () -> Unit,
    onRemoveFavouriteClick: () -> Unit,
    onAddShortcutClick: () -> Unit,
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
            onClick = onOpenDropdownClick
        )

        FavouriteStopItemDropdownMenu(
            menu = dropdownMenu,
            onDropdownMenuDismissed = onDropdownMenuDismissed,
            onEditFavouriteNameClick = onEditFavouriteNameClick,
            onRemoveFavouriteClick = onRemoveFavouriteClick,
            onAddShortcutClick = onAddShortcutClick,
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
            contentDescription = stringResource(
                R.string.favouritestops_dropdown_content_description
            ),
            modifier = Modifier
                .minimumInteractiveComponentSize(),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Preview(
    name = "Favourite stop item - light",
    group = "Favourite stop item",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Favourite stop item - dark",
    group = "Favourite stop item",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FavouriteStopItemPreview(
    @PreviewParameter(UiFavouriteStopProvider::class) favouriteStop: UiFavouriteStop
) {
    MyBusTheme {
        FavouriteStopItem(
            favouriteStop = favouriteStop,
            onFavouriteClick = { },
            onOpenDropdownClick = { },
            onDropdownMenuDismissed = { },
            onEditFavouriteNameClick = { },
            onRemoveFavouriteClick = { },
            onAddShortcutClick = { },
            onAddArrivalAlertClick = { },
            onRemoveArrivalAlertClick = { },
            onAddProximityAlertClick = { },
            onRemoveProximityAlertClick = { },
            onShowOnMapClick = { },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private class UiFavouriteStopProvider : PreviewParameterProvider<UiFavouriteStop> {

    override val values = sequenceOf(
        UiFavouriteStop(
            stopIdentifier = "12345678".toNaptanStopIdentifier(),
            savedName = "My favourite stop",
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
            dropdownMenu = null
        ),
        UiFavouriteStop(
            stopIdentifier = "12345678".toNaptanStopIdentifier(),
            savedName = "My favourite stop",
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
            dropdownMenu = UiFavouriteDropdownMenu()
        ),
        UiFavouriteStop(
            stopIdentifier = "12345678".toNaptanStopIdentifier(),
            savedName = "My favourite stop",
            services = null,
            dropdownMenu = UiFavouriteDropdownMenu()
        )
    )
}
