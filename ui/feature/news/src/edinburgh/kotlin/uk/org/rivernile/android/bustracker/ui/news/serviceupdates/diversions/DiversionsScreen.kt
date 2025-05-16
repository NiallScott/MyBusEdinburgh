/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions

import android.content.res.Configuration
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import uk.org.rivernile.android.bustracker.ui.datetime.LocalDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.datetime.rememberDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.ServiceUpdatesScreen
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiContent
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiError
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiLastRefreshed
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiMoreDetails
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

/**
 * A [Composable] which renders the root of the diversions screen with state passed in.
 *
 * @param state The current [UiDiversionsState] to render.
 * @param modifier Any [Modifier]s which need to be applied.
 * @param onRefresh A lambda which is executed when the content should be refreshed.
 * @param onMoreDetailsClicked A lambda which is executed when the 'More details' button is clicked.
 * @param onActionLaunched A lambda which is executed when an action has been launched.
 * @author Niall Scott
 */
@Composable
internal fun DiversionsScreen(
    state: UiDiversionsState,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onMoreDetailsClicked: (UiDiversion) -> Unit,
    onActionLaunched: () -> Unit
) {
    val doublePadding = dimensionResource(id = Rcore.dimen.padding_double)

    ServiceUpdatesScreen(
        content = state.content,
        modifier = modifier,
        onRefresh = onRefresh,
        itemContent = { item ->
            DiversionItem(
                item = item,
                modifier = Modifier
                    .widthIn(0.dp, 568.dp)
                    .padding(start = doublePadding, end = doublePadding),
                onMoreDetailsClicked = { onMoreDetailsClicked(item) }
            )
        }
    )

    state.action?.let {
        LaunchAction(
            action = it,
            onActionLaunched = onActionLaunched
        )
    }
}

internal val LocalDiversionsActionLauncher = staticCompositionLocalOf<DiversionsActionLauncher> {
    error("LocalDiversionsActionLauncher has not been set with a value.")
}

@Composable
private fun LaunchAction(
    action: UiDiversionAction,
    onActionLaunched: () -> Unit
) {
    val actionLauncher = LocalDiversionsActionLauncher.current

    LaunchedEffect(action) {
        when (action) {
            is UiDiversionAction.ShowUrl -> actionLauncher.launchUrl(action.url)
        }

        onActionLaunched()
    }
}

@Preview(
    name = "Diversions screen - content - light",
    group = "Diversions screen - content",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Diversions screen - content - dark",
    group = "Diversions screen - content",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DiversionsScreenContentPreview() {
    val diversions = persistentListOf(
        UiDiversion(
            id = "1",
            lastUpdated = Instant.fromEpochMilliseconds(1719063420000L),
            title = "Princes Street",
            summary = "Due to road works buses are being diverted from Princes Street.",
            affectedServices = persistentListOf(
                UiServiceName(
                    serviceName = "1",
                    UiServiceColours(
                        backgroundColour = Color.Blue.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                ),
                UiServiceName(
                    serviceName = "26",
                    UiServiceColours(
                        backgroundColour = Color.Red.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                ),
                UiServiceName(
                    serviceName = "44",
                    UiServiceColours(
                        backgroundColour = Color.Yellow.toArgb(),
                        textColour = Color.Black.toArgb()
                    )
                )
            ),
            moreDetails = UiMoreDetails(url = "https://some.url")
        ),
        UiDiversion(
            id = "2",
            lastUpdated = Instant.fromEpochMilliseconds(1719063420000L),
            title = "Princes Street",
            summary = "Due to road works buses are being diverted from Princes Street.",
            affectedServices = persistentListOf(
                UiServiceName(
                    serviceName = "1",
                    colours = UiServiceColours(
                        backgroundColour = Color.Blue.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                ),
                UiServiceName(
                    serviceName = "26",
                    colours = UiServiceColours(
                        backgroundColour = Color.Red.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                ),
                UiServiceName(
                    serviceName = "44",
                    colours = UiServiceColours(
                        backgroundColour = Color.Yellow.toArgb(),
                        textColour = Color.Black.toArgb()
                    )
                )
            ),
            moreDetails = null
        ),
        UiDiversion(
            id = "3",
            lastUpdated = Instant.fromEpochMilliseconds(1719063420000L),
            title = "Princes Street",
            summary = "Due to road works buses are being diverted from Princes Street.",
            affectedServices = persistentListOf(
                UiServiceName(
                    serviceName = "1",
                    colours = UiServiceColours(
                        backgroundColour = Color.Blue.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                ),
                UiServiceName(
                    serviceName = "26",
                    colours = UiServiceColours(
                        backgroundColour = Color.Red.toArgb(),
                        textColour = Color.White.toArgb()
                    )
                ),
                UiServiceName(
                    serviceName = "44",
                    colours = UiServiceColours(
                        backgroundColour = Color.Yellow.toArgb(),
                        textColour = Color.Black.toArgb()
                    )
                )
            ),
            moreDetails = UiMoreDetails(url = "https://some.url")
        )
    )

    MyBusTheme {
        CompositionLocalProvider(
            LocalDateTimeFormatter provides rememberDateTimeFormatter()
        ) {
            DiversionsScreen(
                state = UiDiversionsState(
                    content = UiContent.Populated(
                        isRefreshing = false,
                        items = diversions,
                        error = null,
                        hasInternetConnectivity = true,
                        lastRefreshTime = UiLastRefreshed.Minutes(minutes = 5)
                    )
                ),
                onRefresh = { },
                onMoreDetailsClicked = { },
                onActionLaunched = { }
            )
        }
    }
}

@Preview(
    name = "Diversions screen - progress - light",
    group = "Diversions screen - progress",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Diversions screen - progress - dark",
    group = "Diversions screen - progress",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DiversionsScreenProgressPreview() {
    MyBusTheme {
        CompositionLocalProvider(
            LocalDateTimeFormatter provides rememberDateTimeFormatter()
        ) {
            DiversionsScreen(
                state = UiDiversionsState(
                    content = UiContent.InProgress
                ),
                onRefresh = { },
                onMoreDetailsClicked = { },
                onActionLaunched = { }
            )
        }
    }
}

@Preview(
    name = "Diversions screen - error - empty - light",
    group = "Diversions screen - error - empty",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Diversions screen - error - empty - dark",
    group = "Diversions screen - error - empty",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun DiversionsScreenEmptyErrorPreview() {
    MyBusTheme {
        CompositionLocalProvider(
            LocalDateTimeFormatter provides rememberDateTimeFormatter()
        ) {
            DiversionsScreen(
                state = UiDiversionsState(
                    content = UiContent.Error(
                        error = UiError.EMPTY
                    )
                ),
                onRefresh = { },
                onMoreDetailsClicked = { },
                onActionLaunched = { }
            )
        }
    }
}