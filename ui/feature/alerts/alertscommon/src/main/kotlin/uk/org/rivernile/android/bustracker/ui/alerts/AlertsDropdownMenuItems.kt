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

package uk.org.rivernile.android.bustracker.ui.alerts

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
import uk.org.rivernile.android.bustracker.ui.alerts.common.R
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import uk.org.rivernile.android.bustracker.core.alerts.R as Ralert

/** The test tag used for the add arrival alert menu item. */
public const val TEST_TAG_MENU_ITEM_ADD_ARRIVAL_ALERT: String = "menu-item-add-arrival-alert"
/** The test tag used for the remove arrival alert menu item. */
public const val TEST_TAG_MENU_ITEM_REMOVE_ARRIVAL_ALERT: String = "menu-item-remove-arrival-alert"
/** The test tag used for the add proximity alert menu item. */
public const val TEST_TAG_MENU_ITEM_ADD_PROXIMITY_ALERT: String = "menu-item-add-proximity-alert"
/** The test tag used for the remove proximity alert menu item. */
public const val TEST_TAG_MENU_ITEM_REMOVE_PROXIMITY_ALERT: String =
    "menu-item-remove-proximity-alert"

/**
 * An arrival alert [DropdownMenuItem].
 *
 * @param menuItem This contains the data required to render this menu item.
 * @param onAddArrivalAlertClick The action which should be performed when the user wishes to add an
 * arrival alert.
 * @param onRemoveArrivalAlertClick The action which should be performed when the user wishes to
 * remove an arrival alert.
 * @param modifier Any [Modifier]s which should be applied.
 * @author Niall Scott
 */
@Composable
public fun ArrivalAlertMenuItem(
    menuItem: UiArrivalAlertDropdownMenuItem,
    onAddArrivalAlertClick: () -> Unit,
    onRemoveArrivalAlertClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (menuItem.hasArrivalAlert) {
        RemoveArrivalAlertMenuItem(
            onClick = onRemoveArrivalAlertClick,
            modifier = modifier
        )
    } else {
        AddArrivalAlertMenuItem(
            onClick = onAddArrivalAlertClick,
            modifier = modifier
        )
    }
}

/**
 * A proximity alert [DropdownMenuItem].
 *
 * @param menuItem This contains the data required to render this menu item.
 * @param onAddProximityAlertClick The action which should be performed when the user wishes to add
 * a proximity alert.
 * @param onRemoveProximityAlertClick The action which should be performed when the user wishes to
 * remove a proximity alert.
 * @param modifier Any [Modifier]s which should be applied.
 * @author Niall Scott
 */
@Composable
public fun ProximityAlertMenuItem(
    menuItem: UiProximityAlertDropdownMenuItem,
    onAddProximityAlertClick: () -> Unit,
    onRemoveProximityAlertClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (menuItem.hasProximityAlert) {
        RemoveProximityAlertMenuItem(
            onClick = onRemoveProximityAlertClick,
            modifier = modifier
        )
    } else {
        AddProximityAlertMenuItem(
            onClick = onAddProximityAlertClick,
            modifier = modifier
        )
    }
}

@Composable
private fun AddArrivalAlertMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDropdownMenuItem(
        textStringResId = Ralert.string.time_alert_add,
        iconResId = R.drawable.ic_alarm_add,
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_MENU_ITEM_ADD_ARRIVAL_ALERT
            }
    )
}

@Composable
private fun RemoveArrivalAlertMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDropdownMenuItem(
        textStringResId = Ralert.string.time_alert_rem,
        iconResId = R.drawable.ic_alarm_off,
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_MENU_ITEM_REMOVE_ARRIVAL_ALERT
            }
    )
}

@Composable
private fun AddProximityAlertMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDropdownMenuItem(
        textStringResId = Ralert.string.prox_alert_add,
        iconResId = R.drawable.ic_location_on,
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_MENU_ITEM_ADD_PROXIMITY_ALERT
            }
    )
}

@Composable
private fun RemoveProximityAlertMenuItem(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    AlertDropdownMenuItem(
        textStringResId = Ralert.string.prox_alert_rem,
        iconResId = R.drawable.ic_location_off,
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_MENU_ITEM_REMOVE_PROXIMITY_ALERT
            }
    )
}

@Composable
private fun AlertDropdownMenuItem(
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
    name = "Alerts dropdown menu items - light",
    group = "Alerts dropdown menu items",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Alerts dropdown menu items - dark",
    group = "Alerts dropdown menu items",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AlertsDropdownMenuItemsPreview() {
    MyBusTheme {
        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            DropdownMenu(
                expanded = true,
                onDismissRequest = { }
            ) {
                AddArrivalAlertMenuItem(
                    onClick = { }
                )

                RemoveArrivalAlertMenuItem(
                    onClick = { }
                )

                AddProximityAlertMenuItem(
                    onClick = { }
                )

                RemoveProximityAlertMenuItem(
                    onClick = { }
                )
            }
        }
    }
}
