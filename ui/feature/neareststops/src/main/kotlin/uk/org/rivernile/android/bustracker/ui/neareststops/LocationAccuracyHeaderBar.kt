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
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore

internal const val TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_GPS_NOT_PRESENT =
    "location-accuracy-header-bar-gps-not-present"
internal const val TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_PERMISSIONS_NOT_SUFFICIENT =
    "location-accuracy-header-bar-permissions-not-sufficient"
internal const val TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_GPS_DISABLED =
    "location-accuracy-header-bar-gps-disabled"
internal const val TEST_TAG_LOCATION_ACCURACY_HEADER_TEXT = "location-accuracy-header-text"
internal const val TEST_TAG_LOCATION_ACCURACY_HEADER_RESOLVE_BUTTON =
    "location-accuracy-header-resolve-button"

/**
 * This shows a bar which displays text to the user warning them about imprecise device location,
 * depending on the state of [UiLocationAccuracy]. Some states may also show a resolution button.
 *
 * @param locationAccuracy The [UiLocationAccuracy] - this is used to determine the error to show to
 * the user and what resolution, if any, can be taken.
 * @param onShowAppSettingsClick This lambda is called when the user clicks on the resolution button
 * to show app settings.
 * @param onShowSystemLocationSettingsClick This lambda is called when the user clicks on the
 * resolution button to show the system location settings.
 * @param modifier Any [Modifier]s which should be applied.
 * @author Niall Scott
 */
@Composable
internal fun LocationAccuracyHeaderBar(
    locationAccuracy: UiLocationAccuracy,
    onShowAppSettingsClick: () -> Unit,
    onShowSystemLocationSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (locationAccuracy) {
        UiLocationAccuracy.GPS_NOT_PRESENT -> {
            LocationAccuracyHeaderBar(
                textResId = R.string.neareststops_location_accuracy_no_gps,
                modifier = modifier
                    .semantics {
                        testTag = TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_GPS_NOT_PRESENT
                    }
            )
        }
        UiLocationAccuracy.PERMISSIONS_NOT_SUFFICIENT -> {
            LocationAccuracyHeaderBar(
                textResId = R.string.neareststops_location_accuracy_insufficient_permission,
                modifier = modifier
                    .semantics {
                        testTag = TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_PERMISSIONS_NOT_SUFFICIENT
                    }
            ) {
                LocationAccuracyResolutionButton(
                    text = stringResource(
                        R.string.neareststops_location_accuracy_button_open_settings
                    ),
                    onClick = onShowAppSettingsClick
                )
            }
        }
        UiLocationAccuracy.GPS_DISABLED -> {
            LocationAccuracyHeaderBar(
                textResId = R.string.neareststops_location_accuracy_gps_disabled,
                modifier = modifier
                    .semantics {
                        testTag = TEST_TAG_LOCATION_ACCURACY_HEADER_BAR_GPS_DISABLED
                    }
            ) {
                LocationAccuracyResolutionButton(
                    text = stringResource(
                        R.string.neareststops_location_accuracy_button_open_settings
                    ),
                    onClick = onShowSystemLocationSettingsClick
                )
            }
        }
    }
}

@Composable
private fun LocationAccuracyHeaderBar(
    @StringRes textResId: Int,
    modifier: Modifier = Modifier,
    resolveButton: (@Composable () -> Unit)? = null
) {
    val paddingDouble = dimensionResource(id = Rcore.dimen.padding_double)

    Row(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.inverseSurface)
            .safeDrawingPadding()
            .padding(
                horizontal = paddingDouble,
                vertical = dimensionResource(id = Rcore.dimen.padding_default)
            ),
        horizontalArrangement = Arrangement.spacedBy(paddingDouble),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_satellite),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.inverseOnSurface
        )

        LocationAccuracyText(
            text = stringResource(textResId),
            modifier = Modifier
                .weight(1f)
        )

        resolveButton?.invoke()
    }
}

@Composable
private fun LocationAccuracyText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_LOCATION_ACCURACY_HEADER_TEXT
            },
        color = MaterialTheme.colorScheme.inverseOnSurface,
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun LocationAccuracyResolutionButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_LOCATION_ACCURACY_HEADER_RESOLVE_BUTTON
            },
        colors = ButtonDefaults.textButtonColors(
            contentColor = MaterialTheme.colorScheme.inversePrimary
        )
    ) {
        Text(
            text = text
        )
    }
}

@Preview(
    name = "Location accuracy header bar - light",
    group = "Location accuracy header bar",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Location accuracy header bar - dark",
    group = "Location accuracy header bar",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun LocationAccuracyHeaderBarPreview(
    @PreviewParameter(UiLocationAccuracyProvider::class) locationAccuracy: UiLocationAccuracy
) {
    MyBusTheme {
        LocationAccuracyHeaderBar(
            locationAccuracy = locationAccuracy,
            onShowAppSettingsClick = { },
            onShowSystemLocationSettingsClick = { },
            modifier = Modifier.fillMaxWidth()
        )
    }
}

private class UiLocationAccuracyProvider : PreviewParameterProvider<UiLocationAccuracy> {

    override val values = sequenceOf(
        UiLocationAccuracy.GPS_NOT_PRESENT,
        UiLocationAccuracy.PERMISSIONS_NOT_SUFFICIENT,
        UiLocationAccuracy.GPS_DISABLED
    )
}
