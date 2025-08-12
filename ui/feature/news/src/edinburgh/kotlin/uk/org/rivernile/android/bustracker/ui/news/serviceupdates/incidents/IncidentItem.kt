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

@file:OptIn(ExperimentalTime::class)

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ElevatedCard
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.ui.formatters.LocalDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.formatters.rememberDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.ItemAffectedServices
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.ItemLastUpdated
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.ItemMoreDetailsButton
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.ItemSummary
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.ItemTitle
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiMoreDetails
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * A [Composable] which renders an incident item.
 *
 * @param item The item to be rendered.
 * @param modifier Any [Modifier] to be applied.
 * @param onMoreDetailsClicked A lambda which is executed when the 'More details' button is clicked.
 * @author Niall Scott
 */
@Composable
internal fun IncidentItem(
    item: UiIncident,
    modifier: Modifier = Modifier,
    onMoreDetailsClicked: () -> Unit
) {
    ElevatedCard(
        modifier = modifier
    ) {
        val doublePadding = dimensionResource(Rcore.dimen.padding_double)

        Column(
            modifier = Modifier.padding(doublePadding)
        ) {
            ItemTitle(
                title = item.title,
                modifier = Modifier.fillMaxWidth()
            )

            ItemLastUpdated(
                lastUpdated = item.lastUpdated,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(doublePadding))

            ItemSummary(
                summary = item.summary,
                modifier = Modifier.fillMaxWidth()
            )

            item.affectedServices?.ifEmpty { null }?.let {
                Spacer(modifier = Modifier.height(doublePadding))

                ItemAffectedServices(
                    affectedServices = it,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (item.moreDetails != null) {
                Spacer(modifier = Modifier.height(doublePadding))

                ItemMoreDetailsButton(
                    onClick = onMoreDetailsClicked
                )
            }
        }
    }
}

@Preview(
    name = "Incident item - full - light",
    group = "Incident item - full",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Incident item - full - dark",
    group = "Incident item - full",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IncidentItemPreview() {
    MyBusTheme {
        CompositionLocalProvider(
            LocalDateTimeFormatter provides rememberDateTimeFormatter()
        ) {
            IncidentItem(
                item = UiIncident(
                    id = "abc123",
                    lastUpdated = Instant.fromEpochMilliseconds(1719063420000L),
                    title = "Princes Street",
                    summary = "Due to traffic congestion buses are being delayed on Princes Street.",
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
                ),
                modifier = Modifier.padding(16.dp),
                onMoreDetailsClicked = { }
            )
        }
    }
}

@Preview(
    name = "Incident item - no services - light",
    group = "Incident item - no services",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Incident item - no services - dark",
    group = "Incident item - no services",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IncidentItemNoServicesPreview() {
    MyBusTheme {
        CompositionLocalProvider(
            LocalDateTimeFormatter provides rememberDateTimeFormatter()
        ) {
            IncidentItem(
                item = UiIncident(
                    id = "abc123",
                    lastUpdated = Instant.fromEpochMilliseconds(1719063420000L),
                    title = "Princes Street",
                    summary = "Due to traffic congestion buses are being delayed on Princes Street.",
                    affectedServices = null,
                    moreDetails = UiMoreDetails(url = "https://some.url")
                ),
                modifier = Modifier.padding(16.dp),
                onMoreDetailsClicked = { }
            )
        }
    }
}

@Preview(
    name = "Incident item - no More Details button - light",
    group = "Incident item - no More Details button",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Incident item - no More Details button - dark",
    group = "Incident item - no More Details button",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IncidentItemNoMoreDetailsButtonPreview() {
    MyBusTheme {
        CompositionLocalProvider(
            LocalDateTimeFormatter provides rememberDateTimeFormatter()
        ) {
            IncidentItem(
                item = UiIncident(
                    id = "abc123",
                    lastUpdated = Instant.fromEpochMilliseconds(1719063420000L),
                    title = "Princes Street",
                    summary = "Due to traffic congestion buses are being delayed on Princes Street.",
                    affectedServices = null,
                    moreDetails = null
                ),
                modifier = Modifier.padding(16.dp),
                onMoreDetailsClicked = { }
            )
        }
    }
}