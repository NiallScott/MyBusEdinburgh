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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.font.FontWeight
import kotlinx.collections.immutable.ImmutableList
import kotlinx.datetime.Instant
import uk.org.rivernile.android.bustracker.ui.formatters.LocalDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.news.R
import uk.org.rivernile.android.bustracker.ui.text.SmallDecoratedServiceNamesListingText
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import java.util.Date

internal const val TEST_TAG_ITEM_TITLE = "item-title"
internal const val TEST_TAG_ITEM_SUMMARY = "item-summary"
internal const val TEST_TAG_ITEM_LAST_UPDATED = "item-last-updated"
internal const val TEST_TAG_ITEM_BUTTON_MORE_DETAILS = "item-button-more-details"
internal const val TEST_TAG_ITEM_AFFECTED_SERVICES = "item-affected-services"

/**
 * A Service Update item title.
 *
 * @param title The title text.
 * @param modifier The [Modifier] to be applied to the title.
 */
@Composable
internal fun ItemTitle(
    title: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = title,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ITEM_TITLE
            },
        style = MaterialTheme.typography.titleLarge
    )
}

/**
 * A Service Update item summary.
 *
 * @param summary The summary text.
 * @param modifier The [Modifier] to be applied to the summary.
 */
@Composable
internal fun ItemSummary(
    summary: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = summary,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ITEM_SUMMARY
            },
        style = MaterialTheme.typography.bodyLarge
    )
}

/**
 * A Service Update item "last updated" text.
 *
 * @param lastUpdated The [Instant] the item was last updated.
 * @param modifier The [Modifier] to be applied to the last updated text.
 */
@Composable
internal fun ItemLastUpdated(
    lastUpdated: Instant,
    modifier: Modifier = Modifier
) {
    Text(
        text = stringResource(
            R.string.serviceupdates_item_last_updated,
            LocalDateTimeFormatter.current.format(Date(lastUpdated.toEpochMilliseconds()))
        ),
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ITEM_LAST_UPDATED
            },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.labelSmall
    )
}

/**
 * A Service Update item affected services listing.
 *
 * @param affectedServices An ordered [ImmutableList] of [UiServiceName]s.
 * @param modifier The [Modifier] to be applied to the affected services listing.
 */
@Composable
internal fun ItemAffectedServices(
    affectedServices: ImmutableList<UiServiceName>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ITEM_AFFECTED_SERVICES
            },
        verticalArrangement = Arrangement.spacedBy(dimensionResource(Rcore.dimen.padding_double))
    ) {
        Text(
            text = stringResource(R.string.serviceupdates_item_heading_affected_services),
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleSmall
        )

        SmallDecoratedServiceNamesListingText(
            services = affectedServices,
            modifier = Modifier.fillMaxWidth(),
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.bodyMedium
        )
    }
}

/**
 * A Service Update "More details" button.
 *
 * @param modifier The [Modifier] to be applied to the "More details" button.
 * @param onClick A lambda which is invoked when this button is clicked.
 */
@Composable
internal fun ItemMoreDetailsButton(
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    TextButton(
        onClick = onClick,
        modifier = modifier
            .semantics {
                testTag = TEST_TAG_ITEM_BUTTON_MORE_DETAILS
            },
        content = {
            Text(text = stringResource(id = R.string.serviceupdates_item_btn_more_details))
        }
    )
}