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

package uk.org.rivernile.android.bustracker.ui.stopdetails

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.MutableWindowInsets
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.exclude
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.onConsumedWindowInsetsChanged
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.plus
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfoV2
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.window.core.layout.WindowSizeClass
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberUpdatedMarkerState
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.busstops.toContentDescriptionStringResId
import uk.org.rivernile.android.bustracker.core.busstops.toIconDrawableResId
import uk.org.rivernile.android.bustracker.core.domain.AtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.NaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toAtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.ui.formatters.LocalNumberFormatter
import uk.org.rivernile.android.bustracker.ui.formatters.rememberNumberFormatter
import uk.org.rivernile.android.bustracker.ui.googlemaps.darkThemeAwareMapStyleOptions
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore

internal const val TEST_TAG_ITEM_MAP = "item-map"
internal const val TEST_TAG_ITEM_NAPTAN = "item-naptan"
internal const val TEST_TAG_ITEM_ATCO = "item-atco"
internal const val TEST_TAG_ITEM_ORIENTATION = "item-orientation"
internal const val TEST_TAG_ITEM_DISTANCE = "item-distance"

/**
 * The stop details item.
 *
 * @param stopDetails The stop details which render this item.
 * @param onStopMapClick A lambda which is called when the stop map is clicked.
 * @param onGrantPermissionClick A lambda which is called when the grant location permissions button
 * is clicked.
 * @param onTurnOnLocationClick A lambda which is called when the turn on location button is
 * clicked.
 * @param modifier Any [Modifier] which should be applied.
 * @author Niall Scott
 */
@Composable
internal fun StopDetailsItem(
    stopDetails: UiStopDetails,
    onStopMapClick: () -> Unit,
    onGrantPermissionClick: () -> Unit,
    onTurnOnLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val largeWidthMode = currentWindowAdaptiveInfoV2()
            .windowSizeClass
            .isWidthAtLeastBreakpoint(WindowSizeClass.WIDTH_DP_MEDIUM_LOWER_BOUND)
        val paddingDouble = dimensionResource(Rcore.dimen.padding_double)
        val (mapRef, stopDetailsPanelRef) = createRefs()
        val isMapShown = stopDetails.isMapShown

        if (isMapShown) {
            StopMapItem(
                latLon = stopDetails.latLon,
                orientation = stopDetails.orientation,
                onStopMapClick = onStopMapClick,
                modifier = Modifier
                    .constrainAs(mapRef) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)

                        if (largeWidthMode) {
                            width = Dimension.percent(0.4f)
                            height = Dimension.fillToConstraints
                            bottom.linkTo(stopDetailsPanelRef.bottom)
                        } else {
                            width = Dimension.fillToConstraints
                            height = Dimension.ratio("16:9")
                            end.linkTo(parent.end)
                        }
                    }
            )
        }

        StopDetailsPanel(
            naptanCode = stopDetails.naptanCode,
            atcoCode = stopDetails.atcoCode,
            orientation = stopDetails.orientation,
            stopDistance = stopDetails.stopDistance,
            modifier = Modifier
                .constrainAs(stopDetailsPanelRef) {
                    width = Dimension.fillToConstraints
                    height = Dimension.wrapContent
                    end.linkTo(parent.end)

                    if (isMapShown) {
                        if (largeWidthMode) {
                            top.linkTo(parent.top)
                            start.linkTo(mapRef.end)
                        } else {
                            top.linkTo(mapRef.bottom)
                            start.linkTo(parent.start)
                        }
                    } else {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                    }
                }
                .let {
                    if (isMapShown && largeWidthMode) {
                        it.consumeWindowInsets(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Start)
                        )
                    } else {
                        it
                    }
                }
                .safeDrawingPadding()
                .padding(
                    top = dimensionResource(Rcore.dimen.padding_default),
                    start = paddingDouble,
                    end = paddingDouble
                ),
            onGrantPermissionClick = onGrantPermissionClick,
            onTurnOnLocationClick = onTurnOnLocationClick
        )
    }
}

@Composable
private fun StopMapItem(
    latLon: UiLatLon,
    orientation: StopOrientation,
    onStopMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    if (!LocalInspectionMode.current) {
        GoogleStopMapItem(
            latLon = latLon,
            orientation = orientation,
            onStopMapClick = onStopMapClick,
            modifier = modifier
        )
    } else {
        Box(
            modifier = modifier
                .background(
                    color = Color.Green
                )
                .clickable(
                    onClick = onStopMapClick
                )
                .semantics {
                    testTag = TEST_TAG_ITEM_MAP
                }
        )
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun GoogleStopMapItem(
    latLon: UiLatLon,
    orientation: StopOrientation,
    onStopMapClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val safeDrawingInsets = WindowInsets.safeDrawing
    val insetsForContentPadding = remember { MutableWindowInsets() }
    val cameraPositionState = rememberCameraPositionState {
        position = latLon.toCameraPosition()
    }

    GoogleMap(
        modifier = modifier
            .onConsumedWindowInsetsChanged { consumedWindowInsets ->
                insetsForContentPadding.insets = safeDrawingInsets
                    .exclude(consumedWindowInsets)
            }
            .consumeWindowInsets(insetsForContentPadding)
            .semantics {
                testTag = TEST_TAG_ITEM_MAP
            },
        cameraPositionState = cameraPositionState,
        googleMapOptionsFactory = {
            GoogleMapOptions()
                .liteMode(true)
        },
        properties = rememberMapProperties(),
        uiSettings = remember {
            MapUiSettings(
                compassEnabled = false,
                mapToolbarEnabled = false
            )
        },
        onMapClick = { onStopMapClick() },
        contentPadding = insetsForContentPadding.asPaddingValues() +
            PaddingValues(dimensionResource(Rcore.dimen.padding_default))
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

    SideEffect(latLon) {
        cameraPositionState.position = latLon.toCameraPosition()
    }
}

@Composable
private fun rememberMapProperties(): MapProperties {
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

@Composable
private fun StopDetailsPanel(
    naptanCode: NaptanStopIdentifier,
    atcoCode: AtcoStopIdentifier,
    orientation: StopOrientation,
    stopDistance: UiStopDistance?,
    onGrantPermissionClick: () -> Unit,
    onTurnOnLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement
            .spacedBy(
                dimensionResource(Rcore.dimen.padding_default)
            )
    ) {
        StopIdentifiersRow(
            naptanCode = naptanCode,
            atcoCode = atcoCode,
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Min)
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
        )

        StopOrientationItem(
            stopOrientation = orientation,
            modifier = Modifier
                .fillMaxWidth()
        )

        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
        )

        stopDistance
            ?.let {
                StopDistanceItem(
                    stopDistance = it,
                    onGrantPermissionClick = onGrantPermissionClick,
                    onTurnOnLocationClick = onTurnOnLocationClick,
                    modifier = Modifier
                        .fillMaxWidth()
                )

                HorizontalDivider(
                    modifier = Modifier
                        .fillMaxWidth()
                )
            }
    }
}

@Composable
private fun StopIdentifiersRow(
    naptanCode: NaptanStopIdentifier,
    atcoCode: AtcoStopIdentifier,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement
            .spacedBy(dimensionResource(Rcore.dimen.padding_double))
    ) {
        NaptanStopIdentifierItem(
            naptanStopIdentifier = naptanCode,
            modifier = Modifier.weight(1f)
        )

        VerticalDivider(
            modifier = Modifier
                .fillMaxHeight()
        )

        AtcoStopIdentifierItem(
            atcoStopIdentifier = atcoCode,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun NaptanStopIdentifierItem(
    naptanStopIdentifier: NaptanStopIdentifier,
    modifier: Modifier = Modifier
) {
    StopIdentifierItem(
        labelStringRes = R.string.stopdetails_naptan_identifier_label,
        stopIdentifier = naptanStopIdentifier,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ITEM_NAPTAN
            }
    )
}

@Composable
private fun AtcoStopIdentifierItem(
    atcoStopIdentifier: AtcoStopIdentifier,
    modifier: Modifier = Modifier
) {
    StopIdentifierItem(
        labelStringRes = R.string.stopdetails_atco_identifier_label,
        stopIdentifier = atcoStopIdentifier,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ITEM_ATCO
            }
    )
}

@Composable
private fun StopIdentifierItem(
    @StringRes labelStringRes: Int,
    stopIdentifier: StopIdentifier,
    modifier: Modifier = Modifier
) {
    TwoLineListItem(
        line1 = stringResource(labelStringRes),
        line2 = stopIdentifier.toHumanReadableString(),
        modifier = modifier
    )
}

@Composable
private fun StopOrientationItem(
    stopOrientation: StopOrientation,
    modifier: Modifier = Modifier
) {
    TwoLineListItem(
        line1 = stringResource(R.string.stopdetails_stop_orientation_label),
        line2 = stringResource(stopOrientation.toContentDescriptionStringResId()),
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ITEM_ORIENTATION
            }
    )
}

@Composable
private fun StopDistanceItem(
    stopDistance: UiStopDistance,
    onGrantPermissionClick: () -> Unit,
    onTurnOnLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ITEM_DISTANCE
            }
    ) {
        ListLine1Text(
            text = stringResource(R.string.stopdetails_stop_distance_label)
        )

        when (stopDistance) {
            is UiStopDistance.InsufficientLocationPermissions ->
                StopDistanceInsufficientPermissions(
                    onGrantPermissionClick = onGrantPermissionClick,
                    modifier = Modifier
                        .padding(
                            top = dimensionResource(Rcore.dimen.padding_default)
                        )
                )
            is UiStopDistance.LocationOff -> StopDistanceLocationOff(
                onTurnOnLocationClick = onTurnOnLocationClick,
                modifier = Modifier
                    .padding(
                        top = dimensionResource(Rcore.dimen.padding_default)
                    )
            )
            is UiStopDistance.ObtainingLocation -> StopDistanceObtainingLocation()
            is UiStopDistance.LocationUnknown -> StopDistanceLocationUnknown()
            is UiStopDistance.Distance -> StopDistanceWithDistanceValue(
                distance = stopDistance
            )
        }
    }
}

@Composable
private fun StopDistanceInsufficientPermissions(
    onGrantPermissionClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onGrantPermissionClick,
        modifier = modifier
    ) {
        Text(
            text = stringResource(
                R.string.stopdetails_stop_distance_insufficient_permissions_btn_grant
            )
        )
    }
}

@Composable
private fun StopDistanceLocationOff(
    onTurnOnLocationClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    OutlinedButton(
        onClick = onTurnOnLocationClick,
        modifier = modifier
    ) {
        Text(
            text = stringResource(R.string.stopdetails_stop_distance_location_off_btn_turn_on)
        )
    }
}

@Composable
private fun StopDistanceObtainingLocation(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement
            .spacedBy(dimensionResource(Rcore.dimen.padding_double)),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ListLine2Text(
            text = stringResource(R.string.stopdetails_stop_distance_obtaining_location)
        )

        LinearProgressIndicator(
            modifier = Modifier
                .weight(1f)
        )
    }
}

@Composable
private fun StopDistanceLocationUnknown(
    modifier: Modifier = Modifier
) {
    ListLine2Text(
        text = stringResource(R.string.stopdetails_stop_distance_unknown),
        modifier = modifier
    )
}

@Composable
private fun StopDistanceWithDistanceValue(
    distance: UiStopDistance.Distance,
    modifier: Modifier = Modifier
) {
    ListLine2Text(
        text = when (distance) {
            is UiStopDistance.Distance.Meters -> stringResource(
                R.string.stopdetails_stop_distance_format_ms,
                LocalNumberFormatter.current.format(distance.distance)
            )
            is UiStopDistance.Distance.Kilometers -> stringResource(
                R.string.stopdetails_stop_distance_format_kms,
                LocalNumberFormatter.current.format(distance.distance)
            )
        },
        modifier = modifier
    )
}

@Composable
private fun TwoLineListItem(
    line1: String,
    line2: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
    ) {
        ListLine1Text(
            text = line1
        )

        ListLine2Text(
            text = line2
        )
    }
}

@Composable
private fun ListLine1Text(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.titleMedium
    )
}

@Composable
private fun ListLine2Text(
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

@Preview(
    name = "Stop details item - light",
    group = "Stop details item",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Stop details item - dark",
    group = "Stop details item",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun StopDetailsItemPreview(
    @PreviewParameter(UiStopDetailsProvider::class) stopDetails: UiStopDetails
) {
    MyBusTheme {
        CompositionLocalProvider(
            LocalNumberFormatter provides rememberNumberFormatter(
                maximumFractionDigits = 2
            )
        ) {
            StopDetailsItem(
                stopDetails = stopDetails,
                onStopMapClick = { },
                onGrantPermissionClick = { },
                onTurnOnLocationClick = { },
                modifier = Modifier
                    .fillMaxWidth()
            )
        }
    }
}

private class UiStopDetailsProvider : PreviewParameterProvider<UiStopDetails> {

    override val values = sequenceOf(
        UiStopDetails(
            naptanCode = "123456".toNaptanStopIdentifier(),
            atcoCode = "ATCO987654".toAtcoStopIdentifier(),
            latLon = UiLatLon(
                latitude = 1.1,
                longitude = 2.2
            ),
            orientation = StopOrientation.NORTH_EAST,
            stopDistance = null,
            isMapShown = false
        ),
        UiStopDetails(
            naptanCode = "123456".toNaptanStopIdentifier(),
            atcoCode = "ATCO987654".toAtcoStopIdentifier(),
            latLon = UiLatLon(
                latitude = 1.1,
                longitude = 2.2
            ),
            orientation = StopOrientation.NORTH_EAST,
            stopDistance = null,
            isMapShown = true
        ),
        UiStopDetails(
            naptanCode = "123456".toNaptanStopIdentifier(),
            atcoCode = "ATCO987654".toAtcoStopIdentifier(),
            latLon = UiLatLon(
                latitude = 1.1,
                longitude = 2.2
            ),
            orientation = StopOrientation.NORTH_EAST,
            stopDistance = UiStopDistance.InsufficientLocationPermissions,
            isMapShown = true
        ),
        UiStopDetails(
            naptanCode = "123456".toNaptanStopIdentifier(),
            atcoCode = "ATCO987654".toAtcoStopIdentifier(),
            latLon = UiLatLon(
                latitude = 1.1,
                longitude = 2.2
            ),
            orientation = StopOrientation.NORTH_EAST,
            stopDistance = UiStopDistance.LocationOff,
            isMapShown = true
        ),
        UiStopDetails(
            naptanCode = "123456".toNaptanStopIdentifier(),
            atcoCode = "ATCO987654".toAtcoStopIdentifier(),
            latLon = UiLatLon(
                latitude = 1.1,
                longitude = 2.2
            ),
            orientation = StopOrientation.NORTH_EAST,
            stopDistance = UiStopDistance.ObtainingLocation,
            isMapShown = true
        ),
        UiStopDetails(
            naptanCode = "123456".toNaptanStopIdentifier(),
            atcoCode = "ATCO987654".toAtcoStopIdentifier(),
            latLon = UiLatLon(
                latitude = 1.1,
                longitude = 2.2
            ),
            orientation = StopOrientation.NORTH_EAST,
            stopDistance = UiStopDistance.LocationUnknown,
            isMapShown = true
        ),
        UiStopDetails(
            naptanCode = "123456".toNaptanStopIdentifier(),
            atcoCode = "ATCO987654".toAtcoStopIdentifier(),
            latLon = UiLatLon(
                latitude = 1.1,
                longitude = 2.2
            ),
            orientation = StopOrientation.NORTH_EAST,
            stopDistance = UiStopDistance.Distance.Meters(
                distance = 123
            ),
            isMapShown = true
        ),
        UiStopDetails(
            naptanCode = "123456".toNaptanStopIdentifier(),
            atcoCode = "ATCO987654".toAtcoStopIdentifier(),
            latLon = UiLatLon(
                latitude = 1.1,
                longitude = 2.2
            ),
            orientation = StopOrientation.NORTH_EAST,
            stopDistance = UiStopDistance.Distance.Kilometers(
                distance = 1.2345f
            ),
            isMapShown = true
        )
    )
}
