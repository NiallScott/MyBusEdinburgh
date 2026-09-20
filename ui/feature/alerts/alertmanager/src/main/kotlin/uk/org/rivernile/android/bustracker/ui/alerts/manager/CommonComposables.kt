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

package uk.org.rivernile.android.bustracker.ui.alerts.manager

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberUpdatedMarkerState
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.busstops.toIconDrawableResId
import uk.org.rivernile.android.bustracker.ui.googlemaps.darkThemeAwareMapStyleOptions

/**
 * Displays the alert title.
 *
 * @param text The title text to display.
 * @param modifier Any [Modifier]s to be applied.
 * @author Niall Scott
 */
@Composable
internal fun AlertTitleText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .semantics {
                heading()
            },
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.headlineSmall
    )
}

/**
 * Displays the description text.
 *
 * @param text The description text to display.
 * @param modifier Any [Modifier]s to be applied.
 * @author Niall Scott
 */
@Composable
internal fun AlertDescriptionText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * Displays the supplementary text.
 *
 * @param text The supplementary text to display.
 * @param modifier Any [Modifier]s to be applied.
 * @author Niall Scott
 */
@Composable
internal fun AlertSupplementaryText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyMedium
    )
}

/**
 * Provides the remove alert button.
 *
 * @param onClick A lambda which is called when the button is clicked.
 * @param modifier Any [Modifier]s to be applied.
 * @author Niall Scott
 */
@Composable
internal fun AlertRemoveButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.alertmanager_btn_remove)
        )
    }
}

/**
 * Remembers and provides the [MapProperties] for alert maps.
 *
 * @return The instance of [MapProperties] to apply to the map.
 * @author Niall Scott
 */
@Composable
internal fun rememberMapProperties(): MapProperties {
    val context = LocalContext.current
    val isDark = isSystemInDarkTheme()

    return remember(isDark) {
        MapProperties(
            mapStyleOptions = darkThemeAwareMapStyleOptions(
                context = context,
                isDark = isDark
            )
        )
    }
}

/**
 * Remembers and provides the [MapUiSettings] for alert maps.
 *
 * @return The instance of [MapUiSettings] to apply to the map.
 * @author Niall Scott
 */
@Composable
internal fun rememberMapUiSettings() = remember {
    MapUiSettings(
        compassEnabled = false,
        mapToolbarEnabled = false
    )
}

/**
 * Provides a [Marker] to be drawn on the map.
 *
 * @param latLon The latitude/longitude of the marker.
 * @param orientation The orientation the marker icon faces.
 * @author Niall Scott
 */
@Composable
internal fun StopMarker(
    latLon: UiLatLon,
    orientation: StopOrientation
) {
    Marker(
        state = rememberUpdatedMarkerState(
            position = latLon.toGoogleMapsLatLng()
        ),
        icon = remember(orientation) {
            BitmapDescriptorFactory
                .fromResource(orientation.toIconDrawableResId())
        }
    )
}
