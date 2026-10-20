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

import android.content.res.Configuration
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.isSpecified
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.android.gms.maps.model.CameraPosition
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.text.LocalStopNameFormatter
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.googlemaps.calculateZoom
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.math.min
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore

internal const val TEST_TAG_PROXIMITY_ALERT_MAP = "proximity-alert-map"
internal const val TEST_TAG_PROXIMITY_ALERT_TITLE = "proximity-alert-title"
internal const val TEST_TAG_PROXIMITY_ALERT_DESCRIPTION = "proximity-alert-description"
internal const val TEST_TAG_PROXIMITY_ALERT_SUPPLEMENTARY_TEXT =
    "proximity-alert-supplementary-text"
internal const val TEST_TAG_PROXIMITY_ALERT_LOCATION_SETTINGS_BUTTON =
    "proximity-alert-location-settings-button"
internal const val TEST_TAG_PROXIMITY_ALERT_REMOVE_BUTTON = "proximity-alert-remove-button"

/**
 * This renders the proximity alert item.
 *
 * @param item The proximity alert to render.
 * @param onLocationSettingsClick This is called when the location settings button has been clicked.
 * @param onRemoveAlertClick This is called when the remove button has been clicked.
 * @param modifier Any [Modifier]s to be applied.
 * @author Niall Scott
 */
@Composable
internal fun ProximityAlertItem(
    item: UiAlert.ProximityAlert,
    onLocationSettingsClick: () -> Unit,
    onRemoveAlertClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
    ) {
        if (item.isMapShown) {
            item.stopDetails?.let {
                ProximityAlertMap(
                    latLon = it.latLon,
                    orientation = it.orientation,
                    distanceFromMeters = item.distanceFromMeters,
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(1.778f)
                )
            }
        }

        val paddingDouble = dimensionResource(Rcore.dimen.padding_double)

        Column(
            modifier = Modifier
                .padding(
                    top = 24.dp,
                    start = paddingDouble,
                    end = paddingDouble,
                    bottom = paddingDouble
                )
        ) {
            ProximityAlertTitleText()

            Spacer(
                modifier = Modifier
                    .height(12.dp)
            )

            ProximityAlertDescriptionText(
                distanceFromMeters = item.distanceFromMeters,
                stopIdentifier = item.stopIdentifier,
                stopName = item.stopDetails?.stopName
            )

            Spacer(
                modifier = Modifier
                    .height(paddingDouble)
            )

            ProximityAlertSupplementaryText()

            Spacer(
                modifier = Modifier
                    .height(24.dp)
            )

            ProximityAlertButtonBar(
                onLocationSettingsClick = onLocationSettingsClick,
                onRemoveAlertClick = onRemoveAlertClick
            )
        }
    }
}

@Composable
private fun ProximityAlertMap(
    latLon: UiLatLon,
    orientation: StopOrientation,
    distanceFromMeters: Int,
    modifier: Modifier = Modifier
) {
    if (!LocalInspectionMode.current) {
        ProximityAlertGoogleMap(
            latLon = latLon,
            orientation = orientation,
            distanceFromMeters = distanceFromMeters,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
                .background(
                    color = Color.Green
                )
                .semantics {
                    testTag = TEST_TAG_PROXIMITY_ALERT_MAP
                }
        )
    }
}

@Composable
private fun ProximityAlertGoogleMap(
    latLon: UiLatLon,
    orientation: StopOrientation,
    distanceFromMeters: Int,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = latLon.toCameraPosition()
    }
    var smallestDimension by remember { mutableStateOf(Dp.Unspecified) }
    val density = LocalDensity.current

    GoogleMap(
        modifier = modifier
            .onSizeChanged { size ->
                val min = min(size.width, size.height)

                smallestDimension = if (min > 0) {
                    with(density) { min.toDp() }
                } else {
                    Dp.Unspecified
                }
            }
            .semantics {
                testTag = TEST_TAG_PROXIMITY_ALERT_MAP
            },
        focusable = false,
        cameraPositionState = cameraPositionState,
        googleMapOptionsFactory = {
            GoogleMapOptions()
                .liteMode(true)
        },
        properties = rememberMapProperties(),
        uiSettings = rememberMapUiSettings(),
        mapViewFactory = { context, options ->
            MapView(context, options).also {
                it.isClickable = false
            }
        }
    ) {
        Circle(
            center = latLon.toGoogleMapsLatLng(),
            fillColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.1f),
            radius = distanceFromMeters.toDouble(),
            strokeColor = MaterialTheme.colorScheme.primary,
            strokeWidth = with(LocalDensity.current) {
                1.dp.toPx()
            }
        )

        StopMarker(
            latLon = latLon,
            orientation = orientation
        )
    }

    if (smallestDimension.isSpecified) {
        SideEffect(
            latLon,
            smallestDimension,
            distanceFromMeters
        ) {
            cameraPositionState.position = CameraPosition
                .fromLatLngZoom(
                    latLon.toGoogleMapsLatLng(),
                    calculateZoom(
                        latitude = latLon.latitude,
                        smallestDimension = smallestDimension,
                        spanMeters = distanceFromMeters * 2f,
                        paddingFactor = 1.2f
                    )
                )
        }
    }
}

@Composable
private fun ProximityAlertTitleText(
    modifier: Modifier = Modifier
) {
    AlertTitleText(
        text = stringResource(R.string.alertmanager_proximity_title),
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_PROXIMITY_ALERT_TITLE
            }
    )
}

@Composable
private fun ProximityAlertDescriptionText(
    distanceFromMeters: Int,
    stopIdentifier: StopIdentifier,
    stopName: UiStopName?,
    modifier: Modifier = Modifier
) {
    AlertDescriptionText(
        text = stringResource(
            R.string.alertmanager_proximity_description,
            distanceFromMeters,
            LocalStopNameFormatter
                .current
                .formatBusStopNameWithStopIdentifier(
                    stopIdentifier = stopIdentifier,
                    stopName = stopName
                )
        ),
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_PROXIMITY_ALERT_DESCRIPTION
            }
    )
}

@Composable
private fun ProximityAlertSupplementaryText(
    modifier: Modifier = Modifier
) {
    AlertSupplementaryText(
        text = stringResource(R.string.alertmanager_proximity_supplementary),
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_PROXIMITY_ALERT_SUPPLEMENTARY_TEXT
            }
    )
}

@Composable
private fun ProximityAlertButtonBar(
    onLocationSettingsClick: () -> Unit,
    onRemoveAlertClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement
            .spacedBy(dimensionResource(Rcore.dimen.padding_default)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ProximityAlertLocationSettingsButton(
            onClick = onLocationSettingsClick,
            modifier = Modifier
                .semantics {
                    testTag = TEST_TAG_PROXIMITY_ALERT_LOCATION_SETTINGS_BUTTON
                }
        )

        AlertRemoveButton(
            onClick = onRemoveAlertClick,
            modifier = Modifier
                .semantics {
                    testTag = TEST_TAG_PROXIMITY_ALERT_REMOVE_BUTTON
                }
        )
    }
}

@Composable
private fun ProximityAlertLocationSettingsButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.alertmanager_proximity_btn_location_settings)
        )
    }
}

@Preview(
    name = "Proximity alert item - light",
    group = "Proximity alert item",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Proximity alert item - dark",
    group = "Proximity alert item",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ProximityAlertItemPreview(
    @PreviewParameter(UiProximityAlertProvider::class) proximityAlertItem: UiAlert.ProximityAlert
) {
    MyBusTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ProximityAlertItem(
                item = proximityAlertItem,
                onLocationSettingsClick = { },
                onRemoveAlertClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

private class UiProximityAlertProvider : PreviewParameterProvider<UiAlert.ProximityAlert> {

    override val values = sequenceOf(
        UiAlert.ProximityAlert(
            id = 1,
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            stopDetails = null,
            distanceFromMeters = 250,
            isMapShown = false
        ),
        UiAlert.ProximityAlert(
            id = 1,
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            stopDetails = UiStopDetails(
                stopName = UiStopName(
                    name = "Stop Name",
                    locality = "Locality"
                ),
                latLon = UiLatLon(
                    latitude = 1.1,
                    longitude = 2.2
                ),
                orientation = StopOrientation.NORTH_EAST
            ),
            distanceFromMeters = 250,
            isMapShown = false
        ),
        UiAlert.ProximityAlert(
            id = 1,
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            stopDetails = UiStopDetails(
                stopName = UiStopName(
                    name = "Stop Name",
                    locality = "Locality"
                ),
                latLon = UiLatLon(
                    latitude = 1.1,
                    longitude = 2.2
                ),
                orientation = StopOrientation.NORTH_EAST
            ),
            distanceFromMeters = 250,
            isMapShown = true
        )
    )
}
