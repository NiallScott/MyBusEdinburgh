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
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.datetime.Instant
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.news.R
import uk.org.rivernile.android.bustracker.ui.news.UiAffectedService
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import java.text.DateFormat
import java.util.Date

/**
 * A [Composable] which renders an incident item.
 *
 * @param item The item to be rendered.
 * @param dateFormat A [DateFormat] instance to format the display of dates/times.
 * @param modifier Any [Modifier] to be applied.
 * @param onMoreDetailsClicked A lambda which is executed when the 'More details' button is clicked.
 * @author Niall Scott
 */
@Composable
internal fun IncidentItem(
    item: UiIncident,
    dateFormat: DateFormat,
    modifier: Modifier = Modifier,
    onMoreDetailsClicked: () -> Unit
) {
    Card(
        modifier = modifier
    ) {
        Column(
            modifier = Modifier.padding(dimensionResource(id = Rcore.dimen.padding_double)),
            verticalArrangement = Arrangement.spacedBy(
                dimensionResource(id = Rcore.dimen.padding_default)
            )
        ) {
            IncidentItemTitle(
                title = item.title,
                modifier = Modifier.fillMaxWidth()
            )

            IncidentItemSummary(
                summary = item.summary,
                modifier = Modifier.fillMaxWidth()
            )

            IncidentItemLastUpdated(
                lastUpdated = item.lastUpdated,
                dateFormat = dateFormat,
                modifier = Modifier.fillMaxWidth()
            )

            item.affectedServices?.ifEmpty { null }?.let {
                IncidentItemAffectedServices(
                    affectedServices = it,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (item.showMoreDetailsButton) {
                IncidentItemMoreDetailsButton(
                    onClick = onMoreDetailsClicked
                )
            }
        }
    }
}

@Composable
private fun IncidentItemTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier,
        style = MaterialTheme.typography.titleLarge
    )
}

@Composable
private fun IncidentItemSummary(
    summary: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = summary,
        modifier = modifier,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun IncidentItemLastUpdated(
    lastUpdated: Instant,
    dateFormat: DateFormat,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(
            R.string.incident_item_last_updated,
            dateFormat.format(Date(lastUpdated.toEpochMilliseconds()))
        ),
        modifier = modifier,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall
    )
}

@Composable
private fun IncidentItemAffectedServices(
    affectedServices: List<UiAffectedService>,
    modifier: Modifier = Modifier
) {
    Text(
        text = affectedServices.joinToString { it.serviceName },
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun IncidentItemMoreDetailsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier,
        content = {
            Text(text = stringResource(id = R.string.incident_item_btn_more_details))
        }
    )
}

@Preview(
    name = "Incident item (light)",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Incident item (dark)",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IncidentItemPreview() {
    MyBusTheme {
        IncidentItem(
            item = UiIncident(
                id = "abc123",
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
            dateFormat = DateFormat.getDateTimeInstance(),
            modifier = Modifier.padding(16.dp),
            onMoreDetailsClicked = { }
        )
    }
}