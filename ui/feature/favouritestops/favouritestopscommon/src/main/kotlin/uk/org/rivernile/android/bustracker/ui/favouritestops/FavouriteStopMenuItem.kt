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
import uk.org.rivernile.android.bustracker.core.favourites.R as Rfavourites
import uk.org.rivernile.android.bustracker.ui.favouritestops.common.R
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

/** The test tag used for the add favourite stop menu item. */
public const val TEST_TAG_MENU_ITEM_ADD_FAVOURITE_STOP: String = "menu-item-add-favourite-stop"
/** The test tag used for the remove favourite stop menu item. */
public const val TEST_TAG_MENU_ITEM_REMOVE_FAVOURITE_STOP: String =
    "menu-item-remove-favourite-stop"

/**
 * A favourite stop [DropdownMenuItem].
 *
 * @param menuItem This contains the data required to render this menu item.
 * @param onAddFavouriteStopClick The action which should be performed when the user wishes to add a
 * favourite stop.
 * @param onRemoveFavouriteStopClick The action which should be performed when the user wishes to
 * remove a favourite stop.
 * @param modifier Any [Modifier]s which should be applied.
 * @author Niall Scott
 */
@Composable
public fun FavouriteStopMenuItem(
    menuItem: UiFavouriteStopDropdownMenuItem,
    onAddFavouriteStopClick: () -> Unit,
    onRemoveFavouriteStopClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (menuItem.isFavouriteStop) {
        RemoveFavouriteStopMenuItem(
            onClick = onRemoveFavouriteStopClick,
            modifier = modifier
        )
    } else {
        AddFavouriteStopMenuItem(
            onClick = onAddFavouriteStopClick,
            modifier = modifier
        )
    }
}

@Composable
private fun AddFavouriteStopMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FavouriteStopDropdownMenuItem(
        textStringResId = Rfavourites.string.favourite_stop_add,
        iconResId = R.drawable.ic_action_star_border,
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_MENU_ITEM_ADD_FAVOURITE_STOP
            }
    )
}

@Composable
private fun RemoveFavouriteStopMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    FavouriteStopDropdownMenuItem(
        textStringResId = Rfavourites.string.favourite_stop_remove,
        iconResId = R.drawable.ic_action_star,
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_MENU_ITEM_REMOVE_FAVOURITE_STOP
            }
    )
}

@Composable
private fun FavouriteStopDropdownMenuItem(
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
    name = "Favourite stop dropdown menu items - light",
    group = "Favourite stop dropdown menu items",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Favourite stop dropdown menu items - dark",
    group = "Favourite stop dropdown menu items",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun FavouriteStopMenuItemPreview() {
    MyBusTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { }
            ) {
                AddFavouriteStopMenuItem(
                    onClick = { }
                )

                RemoveFavouriteStopMenuItem(
                    onClick = { }
                )
            }
        }
    }
}
