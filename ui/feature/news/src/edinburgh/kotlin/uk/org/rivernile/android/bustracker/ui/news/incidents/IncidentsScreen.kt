/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.incidents

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.datetime.Instant
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.news.R
import uk.org.rivernile.android.bustracker.ui.news.UiAffectedService
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import java.text.SimpleDateFormat

/**
 * A [Composable] which renders the root of the incidents screen.
 *
 * @author Niall Scott
 */
@Composable
internal fun IncidentsScreen(
    viewModel: IncidentsViewModel = viewModel()
) {
    val uiState by viewModel.uiStateFlow.collectAsState()

    IncidentsScreenWithState(
        state = uiState,
        onMoreDetailsClicked = viewModel::onMoreDetailsButtonClicked,
        onActionLaunched = viewModel::onActionLaunched,
    )
}

/**
 * A [Composable] which renders the root of the incidents screen with state passed in.
 *
 * @param state The current [UiState] to render.
 * @param onMoreDetailsClicked A lambda which is executed when the 'More details' button is clicked.
 * @param onActionLaunched A lambda which is executed when an action has been launched.
 * @author Niall Scott
 */
@Composable
internal fun IncidentsScreenWithState(
    state: UiState,
    onMoreDetailsClicked: (UiIncident) -> Unit,
    onActionLaunched: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        when (val content = state.content) {
            is UiContent.InProgress -> IncidentEmptyProgress()
            is UiContent.Success -> IncidentItemsList(
                items = content.items,
                onMoreDetailsClicked = onMoreDetailsClicked
            )
            is UiContent.Error -> IncidentError(error = content)
        }
    }

    state.action?.let {
        LaunchAction(
            action = it,
            onActionLaunched = onActionLaunched
        )
    }
}

@Composable
private fun IncidentEmptyProgress(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier
    )
}

@Composable
private fun IncidentItemsList(
    items: List<UiIncident>,
    onMoreDetailsClicked: (UiIncident) -> Unit
) {
    val nestedScrollInterop = rememberNestedScrollInteropConnection()
    val dateFormat = remember { SimpleDateFormat.getDateTimeInstance() }
    val doublePadding = dimensionResource(id = Rcore.dimen.padding_double)

    LazyColumn(
        modifier = Modifier
            .nestedScroll(nestedScrollInterop)
            .fillMaxSize(),
        contentPadding = PaddingValues(top = 12.dp, bottom = 44.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        items(
            items = items,
            key = { it.id }
        ) { item ->
            IncidentItem(
                item = item,
                dateFormat = dateFormat,
                modifier = Modifier
                    .widthIn(0.dp, 568.dp)
                    .padding(start = doublePadding, end = doublePadding),
                onMoreDetailsClicked = { onMoreDetailsClicked(item) }
            )
        }
    }
}

@Composable
private fun IncidentError(error: UiContent.Error) {
    @StringRes val titleRes: Int
    @DrawableRes val iconRes: Int

    when (error) {
        is UiContent.Error.NoConnectivity -> {
            titleRes = R.string.incident_error_noconnectivity
            iconRes = R.drawable.ic_error_cloud_off
        }
        is UiContent.Error.Empty -> {
            titleRes = R.string.incident_error_empty
            iconRes = R.drawable.ic_error_newspaper
        }
        is UiContent.Error.Io -> {
            titleRes = R.string.incident_error_io
            iconRes = R.drawable.ic_error_generic
        }
        is UiContent.Error.Server -> {
            titleRes = R.string.incident_error_server
            iconRes = R.drawable.ic_error_generic
        }
    }

    Column(
        modifier = Modifier.padding(dimensionResource(id = Rcore.dimen.padding_double)),
        verticalArrangement = Arrangement.spacedBy(
            dimensionResource(id = Rcore.dimen.padding_default)
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Icon(
            painter = painterResource(id = iconRes),
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary
        )

        Text(
            text = stringResource(id = titleRes),
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurface,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}

@Composable
private fun LaunchAction(
    action: UiAction,
    onActionLaunched: () -> Unit
) {
    LaunchedEffect(action) {
        when (action) {
            is UiAction.ShowUrl -> { }
        }

        onActionLaunched()
    }
}

@Preview(
    name = "Incidents screen - content - light",
    group = "Incidents screen - content",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Incidents screen - content - dark",
    group = "Incidents screen - content",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IncidentsScreenContentPreview() {
    val incidents = listOf(
        UiIncident(
            id = "1",
            lastUpdated = Instant.fromEpochMilliseconds(1719063420000L),
            title = "Princes Street",
            summary = "Due to traffic congestion buses are being delayed on Princes Street.",
            affectedServices = listOf(
                UiAffectedService(
                    "1",
                    Color.Blue.toArgb(),
                    Color.White.toArgb()
                ),
                UiAffectedService(
                    "26",
                    Color.Red.toArgb(),
                    Color.White.toArgb()
                ),
                UiAffectedService(
                    "44",
                    Color.Yellow.toArgb(),
                    Color.Black.toArgb()
                )
            ),
            url = "https://some.url",
            showMoreDetailsButton = true
        ),
        UiIncident(
            id = "2",
            lastUpdated = Instant.fromEpochMilliseconds(1719063420000L),
            title = "Princes Street",
            summary = "Due to traffic congestion buses are being delayed on Princes Street.",
            affectedServices = listOf(
                UiAffectedService(
                    "1",
                    Color.Blue.toArgb(),
                    Color.White.toArgb()
                ),
                UiAffectedService(
                    "26",
                    Color.Red.toArgb(),
                    Color.White.toArgb()
                ),
                UiAffectedService(
                    "44",
                    Color.Yellow.toArgb(),
                    Color.Black.toArgb()
                )
            ),
            url = "https://some.url",
            showMoreDetailsButton = false
        ),
        UiIncident(
            id = "3",
            lastUpdated = Instant.fromEpochMilliseconds(1719063420000L),
            title = "Princes Street",
            summary = "Due to traffic congestion buses are being delayed on Princes Street.",
            affectedServices = listOf(
                UiAffectedService(
                    "1",
                    Color.Blue.toArgb(),
                    Color.White.toArgb()
                ),
                UiAffectedService(
                    "26",
                    Color.Red.toArgb(),
                    Color.White.toArgb()
                ),
                UiAffectedService(
                    "44",
                    Color.Yellow.toArgb(),
                    Color.Black.toArgb()
                )
            ),
            url = "https://some.url",
            showMoreDetailsButton = true
        )
    )

    MyBusTheme {
        IncidentsScreenWithState(
            state = UiState(
                content = UiContent.Success(incidents)
            ),
            onMoreDetailsClicked = { },
            onActionLaunched = { }
        )
    }
}

@Preview(
    name = "Incidents screen - progress - light",
    group = "Incidents screen - progress",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Incidents screen - progress - dark",
    group = "Incidents screen - progress",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IncidentsScreenProgressPreview() {
    MyBusTheme {
        IncidentsScreenWithState(
            state = UiState(
                content = UiContent.InProgress
            ),
            onMoreDetailsClicked = { },
            onActionLaunched = { }
        )
    }
}

@Preview(
    name = "Incidents screen - error - no connectivity - light",
    group = "Incidents screen - error - no connectivity",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Incidents screen - error - no connectivity - dark",
    group = "Incidents screen - error - no connectivity",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IncidentsScreenNoConnectivityErrorPreview() {
    MyBusTheme {
        IncidentsScreenWithState(
            state = UiState(
                content = UiContent.Error.NoConnectivity
            ),
            onMoreDetailsClicked = { },
            onActionLaunched = { }
        )
    }
}

@Preview(
    name = "Incidents screen - error - empty - light",
    group = "Incidents screen - error - empty",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Incidents screen - error - empty - dark",
    group = "Incidents screen - error - empty",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IncidentsScreenEmptyErrorPreview() {
    MyBusTheme {
        IncidentsScreenWithState(
            state = UiState(
                content = UiContent.Error.Empty
            ),
            onMoreDetailsClicked = { },
            onActionLaunched = { }
        )
    }
}

@Preview(
    name = "Incidents screen - error - IO - light",
    group = "Incidents screen - error - IO",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Incidents screen - error - IO - dark",
    group = "Incidents screen - error - IO",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IncidentsScreenIoErrorPreview() {
    MyBusTheme {
        IncidentsScreenWithState(
            state = UiState(
                content = UiContent.Error.Io
            ),
            onMoreDetailsClicked = { },
            onActionLaunched = { }
        )
    }
}

@Preview(
    name = "Incidents screen - error - server - light",
    group = "Incidents screen - error - server",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Incidents screen - error - server - dark",
    group = "Incidents screen - error - server",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IncidentsScreenServerErrorPreview() {
    MyBusTheme {
        IncidentsScreenWithState(
            state = UiState(
                content = UiContent.Error.Server
            ),
            onMoreDetailsClicked = { },
            onActionLaunched = { }
        )
    }
}