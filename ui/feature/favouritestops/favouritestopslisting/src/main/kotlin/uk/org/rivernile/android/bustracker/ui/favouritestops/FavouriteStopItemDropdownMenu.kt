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
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
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
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import uk.org.rivernile.android.bustracker.ui.alerts.ArrivalAlertMenuItem
import uk.org.rivernile.android.bustracker.ui.alerts.ProximityAlertMenuItem
import uk.org.rivernile.android.bustracker.ui.alerts.UiArrivalAlertDropdownMenuItem
import uk.org.rivernile.android.bustracker.ui.alerts.UiProximityAlertDropdownMenuItem
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

internal const val TEST_TAG_MENU_ITEM_EDIT_FAVOURITE_NAME = "menu-item-edit-favourite-name"
internal const val TEST_TAG_MENU_ITEM_REMOVE_FAVOURITE = "menu-item-remove-favourite"
internal const val TEST_TAG_MENU_ITEM_SHOW_ON_MAP = "menu-item-show-on-map"
internal const val TEST_TAG_MENU_ITEM_ADD_SHORTCUT = "menu-item-add-shortcut"

/**
 * A composable which shows a dropdown menu of items to perform against the favourite stop.
 *
 * @param menu The menu data.
 * @param onDropdownMenuDismissed This is called when the dropdown menu has been dismissed.
 * @param onEditFavouriteNameClick This is called when the user clicks on the menu item to edit
 * their favourite stop.
 * @param onRemoveFavouriteClick This is called when the user clicks on the menu item to remove a
 * favourite stop.
 * @param onAddShortcutClick This is called when the user clicks on the menu item to add the
 * favourite stop as a shortcut.
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
    menu: UiFavouriteDropdownMenu,
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
    DropdownMenu(
        expanded = menu.isShown,
        onDismissRequest = onDropdownMenuDismissed,
        modifier = modifier
    ) {
        EditFavouriteNameMenuItem(
            onClick = onEditFavouriteNameClick
        )

        RemoveFavouriteMenuItem(
            onClick = onRemoveFavouriteClick
        )

        if (menu.isShortcutItemShown) {
            AddShortcutMenuItem(
                onClick = onAddShortcutClick
            )
        }

        menu.arrivalAlertDropdownItem?.let { arrivalAlertDropdownItem ->
            ArrivalAlertMenuItem(
                menuItem = arrivalAlertDropdownItem,
                onAddArrivalAlertClick = onAddArrivalAlertClick,
                onRemoveArrivalAlertClick = onRemoveArrivalAlertClick
            )
        }

        menu.proximityAlertDropdownItem?.let { proximityAlertDropdownItem ->
            ProximityAlertMenuItem(
                menuItem = proximityAlertDropdownItem,
                onAddProximityAlertClick = onAddProximityAlertClick,
                onRemoveProximityAlertClick = onRemoveProximityAlertClick
            )
        }

        if (menu.isStopMapItemShown) {
            ShowOnMapMenuItem(
                onClick = onShowOnMapClick
            )
        }
    }
}

@Composable
private fun EditFavouriteNameMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FavouriteDropdownMenuItem(
        textStringResId = R.string.favouritestops_menu_edit,
        iconResId = R.drawable.ic_edit,
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_MENU_ITEM_EDIT_FAVOURITE_NAME
            }
    )
}

@Composable
private fun RemoveFavouriteMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FavouriteDropdownMenuItem(
        textStringResId = R.string.favouritestops_menu_delete,
        iconResId = R.drawable.ic_delete,
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_MENU_ITEM_REMOVE_FAVOURITE
            }
    )
}

@Composable
private fun AddShortcutMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FavouriteDropdownMenuItem(
        textStringResId = R.string.favouritestops_menu_add_shortcut,
        iconResId = R.drawable.ic_apps,
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_MENU_ITEM_ADD_SHORTCUT
            }
    )
}

@Composable
private fun ShowOnMapMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FavouriteDropdownMenuItem(
        textStringResId = R.string.favouritestops_menu_showonmap,
        iconResId = R.drawable.ic_map,
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_MENU_ITEM_SHOW_ON_MAP
            }
    )
}

@Composable
private fun FavouriteDropdownMenuItem(
    @StringRes textStringResId: Int,
    @DrawableRes iconResId: Int,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(textStringResId)
            )
        },
        onClick = onClick,
        modifier = modifier,
        leadingIcon = {
            Icon(
                painterResource(iconResId),
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
    @PreviewParameter(UiFavouriteDropdownMenuProvider::class)
    menu: UiFavouriteDropdownMenu
) {
    MyBusTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            FavouriteStopItemDropdownMenu(
                menu = menu,
                onDropdownMenuDismissed = { },
                onEditFavouriteNameClick = { },
                onRemoveFavouriteClick = { },
                onAddShortcutClick = { },
                onAddArrivalAlertClick = { },
                onRemoveArrivalAlertClick = { },
                onAddProximityAlertClick = { },
                onRemoveProximityAlertClick = { },
                onShowOnMapClick = { }
            )
        }
    }
}

private class UiFavouriteDropdownMenuProvider : PreviewParameterProvider<UiFavouriteDropdownMenu> {

    override val values = sequenceOf(
        UiFavouriteDropdownMenu(
            isShown = true,
            isShortcutItemShown = true,
            arrivalAlertDropdownItem = UiArrivalAlertDropdownMenuItem(
                hasArrivalAlert = false
            ),
            proximityAlertDropdownItem = UiProximityAlertDropdownMenuItem(
                hasProximityAlert = false
            ),
            isStopMapItemShown = true
        ),
        UiFavouriteDropdownMenu(
            isShown = true,
            isShortcutItemShown = true,
            arrivalAlertDropdownItem = UiArrivalAlertDropdownMenuItem(
                hasArrivalAlert = true
            ),
            proximityAlertDropdownItem = UiProximityAlertDropdownMenuItem(
                hasProximityAlert = true
            ),
            isStopMapItemShown = true
        )
    )
}
