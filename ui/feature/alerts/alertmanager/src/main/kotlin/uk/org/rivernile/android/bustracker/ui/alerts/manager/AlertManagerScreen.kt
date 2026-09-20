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
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewParameter
import androidx.compose.ui.tooling.preview.PreviewParameterProvider
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.PrimaryErrorText
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore

internal const val TEST_TAG_CONTENT_PROGRESS = "content-progress"
internal const val TEST_TAG_CONTENT_CONTENT = "content-content"
internal const val TEST_TAG_ERROR_EMPTY_ALERTS = "content-error-empty-alerts"
internal const val TEST_TAG_ERROR_NO_ALERTS_FEATURE = "content-error-no-alerts-feature"
private const val CONTENT_TYPE_ARRIVAL_ALERT = 1
private const val CONTENT_TYPE_PROXIMITY_ALERT = 2

/**
 * Displays the alert manager content. This could be an error layout or a list of currently set
 * alerts.
 *
 * @param onShowRemoveArrivalAlert This is called when the remove arrival alert UI should be shown.
 * @param onShowRemoveProximityAlert This is called when the remove proximity alert UI should be
 * shown.
 * @param onShowLocationSettings This is called when the location settings UI should be shown.
 * @param modifier Any [Modifier]s to be applied.
 * @param viewModel The [AlertManagerViewModel].
 * @author Niall Scott
 */
@Composable
internal fun AlertManagerScreen(
    onShowRemoveArrivalAlert: (StopIdentifier) -> Unit,
    onShowRemoveProximityAlert: (StopIdentifier) -> Unit,
    onShowLocationSettings: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: AlertManagerViewModel = viewModel()
) {
    val state by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    AlertManagerScreenWithState(
        state = state,
        onRemoveArrivalAlertClick = viewModel::onRemoveArrivalAlertClicked,
        onRemoveProximityAlertClick = viewModel::onRemoveProximityAlertClicked,
        onLocationSettingsClick = viewModel::onLocationSettingsClicked,
        onActionLaunched = viewModel::onActionLaunched,
        onShowRemoveArrivalAlert = onShowRemoveArrivalAlert,
        onShowRemoveProximityAlert = onShowRemoveProximityAlert,
        onShowLocationSettings = onShowLocationSettings,
        modifier = modifier
    )
}

/**
 * Displays the alert manager content. This could be an error layout or a list of currently set
 * alerts.
 *
 * @param state The state to render the UI with.
 * @param onRemoveArrivalAlertClick A lambda which is called when the remove arrival alert button is
 * clicked.
 * @param onRemoveProximityAlertClick A lambda which ia called when the remove proximity alert
 * button is clicked.
 * @param onLocationSettingsClick A lambda which is called when the location settings button is
 * clicked.
 * @param onActionLaunched A lambda which is called when a [UiAction] has been launched.
 * @param onShowRemoveArrivalAlert This is called when the remove arrival alert UI should be shown.
 * @param onShowRemoveProximityAlert This is called when the remove proximity alert UI should be
 * shown.
 * @param onShowLocationSettings This is called when the location settings UI should be shown.
 * @param modifier Any [Modifier]s to be applied.
 * @author Niall Scott
 */
@Composable
internal fun AlertManagerScreenWithState(
    state: UiState,
    onRemoveArrivalAlertClick: (StopIdentifier) -> Unit,
    onRemoveProximityAlertClick: (StopIdentifier) -> Unit,
    onLocationSettingsClick: () -> Unit,
    onActionLaunched: () -> Unit,
    onShowRemoveArrivalAlert: (StopIdentifier) -> Unit,
    onShowRemoveProximityAlert: (StopIdentifier) -> Unit,
    onShowLocationSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
    ) {
        when (val content = state.content) {
            is UiContent.InProgress -> IndeterminateProgress(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(dimensionResource(Rcore.dimen.padding_double))
            )
            is UiContent.Content -> Content(
                alerts = content.alerts,
                onRemoveArrivalAlertClick = onRemoveArrivalAlertClick,
                onRemoveProximityAlertClick = onRemoveProximityAlertClick,
                onLocationSettingsClick = onLocationSettingsClick,
                modifier = Modifier
                    .fillMaxSize()
            )
            is UiContent.EmptyAlerts -> EmptyAlertsError(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(dimensionResource(Rcore.dimen.padding_double))
            )
            is UiContent.NoAlertsFeature -> NoAlertsFeatureError(
                modifier = Modifier
                    .fillMaxSize()
                    .safeDrawingPadding()
                    .padding(dimensionResource(Rcore.dimen.padding_double))
            )
        }
    }

    state.action?.let {
        LaunchAction(
            action = it,
            onShowRemoveArrivalAlert = onShowRemoveArrivalAlert,
            onShowRemoveProximityAlert = onShowRemoveProximityAlert,
            onShowLocationSettings = onShowLocationSettings,
            onActionLaunched = onActionLaunched
        )
    }
}

@Composable
private fun IndeterminateProgress(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier
                .semantics {
                    testTag = TEST_TAG_CONTENT_PROGRESS
                }
        )
    }
}

@Composable
private fun Content(
    alerts: ImmutableList<UiAlert>,
    onRemoveArrivalAlertClick: (StopIdentifier) -> Unit,
    onRemoveProximityAlertClick: (StopIdentifier) -> Unit,
    onLocationSettingsClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn(
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_CONTENT_CONTENT
            },
        contentPadding = PaddingValues(
            top = 12.dp,
            bottom = 36.dp
        ),
        verticalArrangement = Arrangement
            .spacedBy(dimensionResource(Rcore.dimen.padding_default)),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(
            items = alerts,
            key = { it.id },
            contentType = { it.toContentType() }
        ) {
            val modifier = Modifier
                .widthIn(max = 568.dp)
                .safeDrawingPadding()
                .padding(
                    horizontal = dimensionResource(Rcore.dimen.padding_double)
                )
                .animateItem()

            when (it) {
                is UiAlert.ArrivalAlert -> ArrivalAlertItem(
                    item = it,
                    onRemoveAlertClick = { onRemoveArrivalAlertClick(it.stopIdentifier) },
                    modifier = modifier
                )
                is UiAlert.ProximityAlert -> ProximityAlertItem(
                    item = it,
                    onLocationSettingsClick = onLocationSettingsClick,
                    onRemoveAlertClick = { onRemoveProximityAlertClick(it.stopIdentifier) },
                    modifier = modifier
                )
            }
        }
    }
}

@Composable
private fun EmptyAlertsError(
    modifier: Modifier = Modifier
) {
    ErrorLayout(
        textResId = R.string.alertmanager_error_no_alerts,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ERROR_EMPTY_ALERTS
            }
    )
}

@Composable
private fun NoAlertsFeatureError(
    modifier: Modifier = Modifier
) {
    ErrorLayout(
        textResId = R.string.alertmanager_error_no_feature,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ERROR_NO_ALERTS_FEATURE
            }
    )
}

@Composable
private fun ErrorLayout(
    @StringRes textResId: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement
            .spacedBy(
                space = dimensionResource(Rcore.dimen.padding_double),
                alignment = Alignment.CenterVertically
            ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(R.drawable.ic_error_notifications),
            contentDescription = null,
            modifier = Modifier
                .size(72.dp),
            tint = MaterialTheme.colorScheme.primary
        )

        PrimaryErrorText(
            text = stringResource(textResId)
        )
    }
}

@Composable
private fun LaunchAction(
    action: UiAction,
    onActionLaunched: () -> Unit,
    onShowRemoveArrivalAlert: (StopIdentifier) -> Unit,
    onShowRemoveProximityAlert: (StopIdentifier) -> Unit,
    onShowLocationSettings: () -> Unit
) {
    SideEffect(action) {
        when (action) {
            is UiAction.ShowRemoveArrivalAlert ->
                onShowRemoveArrivalAlert(action.stopIdentifier)
            is UiAction.ShowRemoveProximityAlert ->
                onShowRemoveProximityAlert(action.stopIdentifier)
            is UiAction.ShowLocationSettings -> onShowLocationSettings()
        }

        onActionLaunched()
    }
}

private fun UiAlert.toContentType(): Int {
    return when (this) {
        is UiAlert.ArrivalAlert -> CONTENT_TYPE_ARRIVAL_ALERT
        is UiAlert.ProximityAlert -> CONTENT_TYPE_PROXIMITY_ALERT
    }
}

@Preview(
    name = "Alert manager screen - light",
    group = "Alert manager screen",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Alert manager screen - dark",
    group = "Alert manager screen",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AlertManagerPreview(
    @PreviewParameter(UiStateProvider::class) state: UiState
) {
    MyBusTheme {
        AlertManagerScreenWithState(
            state = state,
            onRemoveArrivalAlertClick = { },
            onRemoveProximityAlertClick = { },
            onLocationSettingsClick = { },
            onActionLaunched = { },
            onShowRemoveArrivalAlert = { },
            onShowRemoveProximityAlert = { },
            onShowLocationSettings = { },
            modifier = Modifier.fillMaxSize()
        )
    }
}

private class UiStateProvider : PreviewParameterProvider<UiState> {

    override val values = sequenceOf(
        UiState(
            content = UiContent.InProgress
        ),
        UiState(
            content = UiContent.EmptyAlerts
        ),
        UiState(
            content = UiContent.NoAlertsFeature
        ),
        UiState(
            content = UiContent.Content(
                alerts = persistentListOf(
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
                    ),
                    UiAlert.ProximityAlert(
                        id = 2,
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
            )
        )
    )
}
