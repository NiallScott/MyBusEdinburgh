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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

/**
 * A composable which shows a dropdown menu of items to perform against the favourite stop.
 *
 * @param items The items to show in the dropdown menu.
 * @param onDropdownMenuDismissed This is called when the dropdown menu has been dismissed.
 * @param onEditFavouriteNameClick This is called when the user clicks on the menu item to edit
 * their favourite stop.
 * @param onRemoveFavouriteClick This is called when the user clicks on the menu item to remove a
 * favourite stop.
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
 * @param modifier Any [Modifier]s which should be applied.
 * @author Niall Scott
 */
@Composable
internal fun FavouriteStopItemDropdownMenu(
    items: ImmutableList<UiFavouriteDropdownItem>,
    onDropdownMenuDismissed: () -> Unit,
    onEditFavouriteNameClick: () -> Unit,
    onRemoveFavouriteClick: () -> Unit,
    onAddArrivalAlertClick: () -> Unit,
    onRemoveArrivalAlertClick: () -> Unit,
    onAddProximityAlertClick: () -> Unit,
    onRemoveProximityAlertClick: () -> Unit,
    onShowOnMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenu(
        expanded = true,
        onDismissRequest = onDropdownMenuDismissed,
        modifier = modifier
    ) {
        items.forEach { item ->
            when (item) {
                is UiFavouriteDropdownItem.EditFavouriteName ->
                    EditFavouriteNameMenuItem(
                        onClick = onEditFavouriteNameClick
                    )
                is UiFavouriteDropdownItem.RemoveFavourite ->
                    RemoveFavouriteMenuItem(
                        onClick = onRemoveFavouriteClick
                    )
                is UiFavouriteDropdownItem.AddArrivalAlert ->
                    AddArrivalAlertMenuItem(
                        enabled = item.isEnabled,
                        onClick = onAddArrivalAlertClick
                    )
                is UiFavouriteDropdownItem.RemoveArrivalAlert ->
                    RemoveArrivalAlertMenuItem(
                        onClick = onRemoveArrivalAlertClick
                    )
                is UiFavouriteDropdownItem.AddProximityAlert ->
                    AddProximityAlertMenuItem(
                        enabled = item.isEnabled,
                        onClick = onAddProximityAlertClick
                    )
                is UiFavouriteDropdownItem.RemoveProximityAlert ->
                    RemoveProximityAlertMenuItem(
                        onClick = onRemoveProximityAlertClick
                    )
                is UiFavouriteDropdownItem.ShowOnMap ->
                    ShowOnMapMenuItem(
                        onClick = onShowOnMapClick
                    )
            }
        }
    }
}

@Composable
private fun EditFavouriteNameMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.favouritestops_menu_edit)
            )
        },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_edit),
                contentDescription = null
            )
        }
    )
}

@Composable
private fun RemoveFavouriteMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.favouritestops_menu_delete)
            )
        },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            Icon(
                painter = painterResource(R.drawable.ic_delete),
                contentDescription = null
            )
        }
    )
}

@Composable
private fun AddArrivalAlertMenuItem(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.favouritestops_menu_time_add)
            )
        },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            Icon(
                painterResource(R.drawable.ic_alarm_add),
                contentDescription = null
            )
        },
        enabled = enabled
    )
}

@Composable
private fun RemoveArrivalAlertMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.favouritestops_menu_time_rem)
            )
        },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            Icon(
                painterResource(R.drawable.ic_alarm_off),
                contentDescription = null
            )
        }
    )
}

@Composable
private fun AddProximityAlertMenuItem(
    enabled: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.favouritestops_menu_prox_add)
            )
        },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            Icon(
                painterResource(R.drawable.ic_location_on),
                contentDescription = null
            )
        },
        enabled = enabled
    )
}

@Composable
private fun RemoveProximityAlertMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.favouritestops_menu_prox_rem)
            )
        },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            Icon(
                painterResource(R.drawable.ic_location_off),
                contentDescription = null
            )
        }
    )
}

@Composable
private fun ShowOnMapMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.favouritestops_menu_showonmap)
            )
        },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            Icon(
                painterResource(R.drawable.ic_map),
                contentDescription = null
            )
        }
    )
}

@Preview(
    name = "Favourite stop item dropdown menu - light",
    group = "Favourite stop item dropdown menu",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Favourite stop item dropdown menu - dark",
    group = "Favourite stop item dropdown menu",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FavouriteStopItemDropdownMenuPreview(
    @PreviewParameter(UiFavouriteDropdownItemsProvider::class)
    items: ImmutableList<UiFavouriteDropdownItem>
) {
    MyBusTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            FavouriteStopItemDropdownMenu(
                items = items,
                onDropdownMenuDismissed = { },
                onEditFavouriteNameClick = { },
                onRemoveFavouriteClick = { },
                onAddArrivalAlertClick = { },
                onRemoveArrivalAlertClick = { },
                onAddProximityAlertClick = { },
                onRemoveProximityAlertClick = { },
                onShowOnMapClick = { }
            )
        }
    }
}

private class UiFavouriteDropdownItemsProvider
    : PreviewParameterProvider<ImmutableList<UiFavouriteDropdownItem>> {

    override val values = sequenceOf(
        persistentListOf(
            UiFavouriteDropdownItem.EditFavouriteName,
            UiFavouriteDropdownItem.RemoveFavourite,
            UiFavouriteDropdownItem.AddArrivalAlert(
                isEnabled = true
            ),
            UiFavouriteDropdownItem.AddProximityAlert(
                isEnabled = true
            ),
            UiFavouriteDropdownItem.ShowOnMap
        ),
        persistentListOf(
            UiFavouriteDropdownItem.EditFavouriteName,
            UiFavouriteDropdownItem.RemoveFavourite,
            UiFavouriteDropdownItem.RemoveArrivalAlert,
            UiFavouriteDropdownItem.RemoveProximityAlert,
            UiFavouriteDropdownItem.ShowOnMap
        )
    )
}
