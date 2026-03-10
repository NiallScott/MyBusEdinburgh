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
import uk.org.rivernile.android.bustracker.ui.favouritestops.FavouriteStopMenuItem
import uk.org.rivernile.android.bustracker.ui.favouritestops.UiFavouriteStopDropdownMenuItem
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

internal const val TEST_TAG_MENU_ITEM_SHOW_ON_MAP = "menu-item-show-on-map"

/**
 * A composable which shows a dropdown menu of items to perform against the nearest stop.
 *
 * @param menu The menu data.
 * @param onDropdownMenuDismissed This is called when the dropdown menu has been dismissed.
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
 * @param modifier Any [Modifier]s which should be applied.
 * @author Niall Scott
 */
@Composable
internal fun NearestStopItemDropdownMenu(
    menu: UiNearestStopDropdownMenu,
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
    DropdownMenu(
        expanded = menu.isShown,
        onDismissRequest = onDropdownMenuDismissed,
        modifier = modifier
    ) {
        menu.favouriteStopDropdownItem?.let {
            FavouriteStopMenuItem(
                menuItem = it,
                onAddFavouriteStopClick = onAddFavouriteStopClick,
                onRemoveFavouriteStopClick = onRemoveFavouriteStopClick
            )
        }

        menu.arrivalAlertDropdownItem?.let {
            ArrivalAlertMenuItem(
                menuItem = it,
                onAddArrivalAlertClick = onAddArrivalAlertClick,
                onRemoveArrivalAlertClick = onRemoveArrivalAlertClick
            )
        }

        menu.proximityAlertDropdownItem?.let {
            ProximityAlertMenuItem(
                menuItem = it,
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
private fun ShowOnMapMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    DropdownMenuItem(
        text = {
            Text(
                text = stringResource(R.string.neareststops_menu_showonmap)
            )
        },
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_MENU_ITEM_SHOW_ON_MAP
            },
        leadingIcon = {
            Icon(
                painterResource(R.drawable.ic_map),
                contentDescription = null
            )
        }
    )
}

@Preview(
    name = "Nearest stop item dropdown menu - light",
    group = "Nearest stop item dropdown menu",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Nearest stop item dropdown menu - dark",
    group = "Nearest stop item dropdown menu",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun NearestStopItemDropdownMenuPreview(
    @PreviewParameter(UiNearestStopDropdownMenuProvider::class) menu: UiNearestStopDropdownMenu
) {
    MyBusTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            NearestStopItemDropdownMenu(
                menu = menu,
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
}

private class UiNearestStopDropdownMenuProvider
    : PreviewParameterProvider<UiNearestStopDropdownMenu> {

    override val values = sequenceOf(
        UiNearestStopDropdownMenu(
            isShown = true,
            favouriteStopDropdownItem = UiFavouriteStopDropdownMenuItem(
                isFavouriteStop = false
            ),
            arrivalAlertDropdownItem = UiArrivalAlertDropdownMenuItem(
                hasArrivalAlert = false
            ),
            proximityAlertDropdownItem = UiProximityAlertDropdownMenuItem(
                hasProximityAlert = false
            ),
            isStopMapItemShown = true
        ),
        UiNearestStopDropdownMenu(
            isShown = true,
            favouriteStopDropdownItem = UiFavouriteStopDropdownMenuItem(
                isFavouriteStop = true
            ),
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
