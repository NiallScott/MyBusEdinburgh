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
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.MapView
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.text.LocalStopNameFormatter
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.SmallDecoratedServiceNamesListingText
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore

internal const val TEST_TAG_ARRIVAL_ALERT_MAP = "arrival-alert-map"
internal const val TEST_TAG_ARRIVAL_ALERT_TITLE = "arrival-alert-title"
internal const val TEST_TAG_ARRIVAL_ALERT_DESCRIPTION = "arrival-alert-description"
internal const val TEST_TAG_ARRIVAL_ALERT_SERVICE_LISTING = "arrival-alert-service-listing"
internal const val TEST_TAG_ARRIVAL_ALERT_SUPPLEMENTARY_TEXT = "arrival-alert-supplementary-text"
internal const val TEST_TAG_ARRIVAL_ALERT_REMOVE_BUTTON = "arrival-alert-remove-button"

/**
 * This renders the arrival alert item.
 *
 * @param item The arrival alert to render.
 * @param onRemoveAlertClick This is called when the remove button has been clicked.
 * @param modifier Any [Modifier]s to be applied.
 * @author Niall Scott
 */
@Composable
internal fun ArrivalAlertItem(
    item: UiAlert.ArrivalAlert,
    onRemoveAlertClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ElevatedCard(
        modifier = modifier
    ) {
        if (item.isMapShown) {
            item.stopDetails?.let {
                ArrivalAlertMap(
                    latLon = it.latLon,
                    orientation = it.orientation,
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
            ArrivalAlertTitleText()

            Spacer(
                modifier = Modifier
                    .height(12.dp)
            )

            ArrivalAlertDescriptionText(
                timeTriggerMinutes = item.timeTriggerMinutes,
                stopIdentifier = item.stopIdentifier,
                stopName = item.stopDetails?.stopName
            )

            Spacer(
                modifier = Modifier
                    .height(paddingDouble)
            )

            SmallDecoratedServiceNamesListingText(
                services = item.services,
                modifier = Modifier
                    .semantics {
                        testTag = TEST_TAG_ARRIVAL_ALERT_SERVICE_LISTING
                    }
            )

            Spacer(
                modifier = Modifier
                    .height(paddingDouble)
            )

            ArrivalAlertSupplementaryText()

            Spacer(
                modifier = Modifier
                    .height(24.dp)
            )

            AlertRemoveButton(
                onClick = onRemoveAlertClick,
                modifier = Modifier
                    .semantics {
                        testTag = TEST_TAG_ARRIVAL_ALERT_REMOVE_BUTTON
                    }
            )
        }
    }
}

@Composable
private fun ArrivalAlertMap(
    latLon: UiLatLon,
    orientation: StopOrientation,
    modifier: Modifier = Modifier
) {
    if (!LocalInspectionMode.current) {
        ArrivalAlertGoogleMap(
            latLon = latLon,
            orientation = orientation,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
                .background(
                    color = Color.Green
                )
                .semantics {
                    testTag = TEST_TAG_ARRIVAL_ALERT_MAP
                }
        )
    }
}

@Composable
private fun ArrivalAlertGoogleMap(
    latLon: UiLatLon,
    orientation: StopOrientation,
    modifier: Modifier = Modifier
) {
    val cameraPositionState = rememberCameraPositionState {
        position = latLon.toCameraPosition()
    }

    GoogleMap(
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ARRIVAL_ALERT_MAP
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
        StopMarker(
            latLon = latLon,
            orientation = orientation
        )
    }

    SideEffect(latLon) {
        cameraPositionState.position = latLon.toCameraPosition()
    }
}

@Composable
private fun ArrivalAlertTitleText(
    modifier: Modifier = Modifier
) {
    AlertTitleText(
        text = stringResource(R.string.alertmanager_arrival_title),
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ARRIVAL_ALERT_TITLE
            }
    )
}

@Composable
private fun ArrivalAlertDescriptionText(
    timeTriggerMinutes: Int,
    stopIdentifier: StopIdentifier,
    stopName: UiStopName?,
    modifier: Modifier = Modifier
) {
    val stopNameText = LocalStopNameFormatter
        .current
        .formatBusStopNameWithStopIdentifier(
            stopIdentifier = stopIdentifier,
            stopName = stopName
        )

    val text = if (timeTriggerMinutes >= 2) {
        pluralStringResource(
            R.plurals.alertmanager_arrival_description_minutes,
            timeTriggerMinutes,
            timeTriggerMinutes,
            stopNameText
        )
    } else {
        stringResource(
            R.string.alertmanager_arrival_description_imminent,
            stopNameText
        )
    }

    AlertDescriptionText(
        text = text,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ARRIVAL_ALERT_DESCRIPTION
            }
    )
}

@Composable
private fun ArrivalAlertSupplementaryText(
    modifier: Modifier = Modifier
) {
    AlertSupplementaryText(
        text = stringResource(R.string.alertmanager_arrival_supplementary),
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ARRIVAL_ALERT_SUPPLEMENTARY_TEXT
            }
    )
}

@Preview(
    name = "Arrival alert item - light",
    group = "Arrival alert item",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Arrival alert item - dark",
    group = "Arrival alert item",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ArrivalAlertItemPreview(
    @PreviewParameter(UiArrivalAlertProvider::class) arrivalAlertItem: UiAlert.ArrivalAlert
) {
    MyBusTheme {
        Box(
            modifier = Modifier
                .fillMaxWidth()
        ) {
            ArrivalAlertItem(
                item = arrivalAlertItem,
                onRemoveAlertClick = { },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
            )
        }
    }
}

private class UiArrivalAlertProvider : PreviewParameterProvider<UiAlert.ArrivalAlert> {

    override val values = sequenceOf(
        UiAlert.ArrivalAlert(
            id = 1,
            stopIdentifier = "123456".toNaptanStopIdentifier(),
            stopDetails = null,
            services = persistentListOf(
                UiServiceName(
                    serviceName = "1",
                    colours = null
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
                        backgroundColour = Color.Blue.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                )
            ),
            timeTriggerMinutes = 5,
            isMapShown = false
        ),
        UiAlert.ArrivalAlert(
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
            services = persistentListOf(
                UiServiceName(
                    serviceName = "1",
                    colours = null
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
                        backgroundColour = Color.Blue.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                )
            ),
            timeTriggerMinutes = 5,
            isMapShown = false
        ),
        UiAlert.ArrivalAlert(
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
            services = persistentListOf(
                UiServiceName(
                    serviceName = "1",
                    colours = null
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
                        backgroundColour = Color.Blue.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                )
            ),
            timeTriggerMinutes = 5,
            isMapShown = true
        )
    )
}
