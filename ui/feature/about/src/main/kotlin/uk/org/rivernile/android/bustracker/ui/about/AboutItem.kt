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

package uk.org.rivernile.android.bustracker.ui.about

import android.content.res.Configuration
import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.org.rivernile.android.bustracker.ui.formatters.LocalDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

internal const val TEST_TAG_TITLE = "title"
internal const val TEST_TAG_CAPTION = "caption"

/**
 * Provides an 'about' item to be displayed.
 *
 * @param item The [UiAboutItem] to display.
 * @param modifier Any [Modifier] to be applied.
 * @param onItemClicked A lambda which is executed when an item is clicked.
 * @author Niall Scott
 */
@Composable
internal fun AboutItem(
    item: UiAboutItem,
    modifier: Modifier = Modifier,
    onItemClicked: (UiAboutItem) -> Unit
) {
    when (item) {
        is UiAboutItem.OneLineItem -> AboutItem1Line(
            item = item,
            modifier = modifier,
            onItemClicked = onItemClicked
        )
        is UiAboutItem.TwoLinesItem -> AboutItem2Lines(
            item = item,
            modifier = modifier,
            onItemClicked = onItemClicked
        )
    }
}

@Composable
private fun AboutItem1Line(
    item: UiAboutItem.OneLineItem,
    modifier: Modifier = Modifier,
    onItemClicked: (UiAboutItem) -> Unit
) {
    val verticalPadding = dimensionResource(id = Rcore.dimen.padding_default)
    val horizontalPadding = dimensionResource(id = Rcore.dimen.padding_double)

    AboutItemTitle(
        text = stringResource(id = item.titleStringRes),
        modifier = modifier
            .clickable(
                enabled = item.isClickable,
                onClick = {
                    onItemClicked(item)
                }
            )
            .focusable(item.isClickable)
            .defaultMinSize(minHeight = 56.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .safeDrawingPadding()
            .padding(
                top = verticalPadding,
                bottom = verticalPadding,
                start = horizontalPadding,
                end = horizontalPadding
            )
    )
}

@Composable
private fun AboutItem2Lines(
    item: UiAboutItem.TwoLinesItem,
    modifier: Modifier = Modifier,
    onItemClicked: (UiAboutItem) -> Unit
) {
    val verticalPadding = dimensionResource(id = Rcore.dimen.padding_default)
    val horizontalPadding = dimensionResource(id = Rcore.dimen.padding_double)

    Column(
        modifier = modifier
            .clickable(
                enabled = item.isClickable,
                onClick = {
                    onItemClicked(item)
                }
            )
            .focusable(item.isClickable)
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .wrapContentHeight(Alignment.CenterVertically)
            .safeDrawingPadding()
            .padding(
                top = verticalPadding,
                bottom = verticalPadding,
                start = horizontalPadding,
                end = horizontalPadding
            )
    ) {
        AboutItemTitle(text = stringResource(id = item.titleStringRes))
        AboutItemCaption(text = item.captionText)
    }
}

@Composable
private fun AboutItemTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                testTag = TEST_TAG_TITLE
            },
        color = MaterialTheme.colorScheme.onSurface,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun AboutItemCaption(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier
            .fillMaxWidth()
            .semantics {
                testTag = TEST_TAG_CAPTION
            },
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium
    )
}

@get:StringRes
private val UiAboutItem.titleStringRes: Int get() {
    return when (this) {
        is UiAboutItem.OneLineItem.Credits -> R.string.about_credits
        is UiAboutItem.OneLineItem.OpenSourceLicences -> R.string.about_open_source
        is UiAboutItem.OneLineItem.PrivacyPolicy -> R.string.about_privacy_policy
        is UiAboutItem.TwoLinesItem.AppVersion -> R.string.about_version
        is UiAboutItem.TwoLinesItem.Author -> R.string.about_author
        is UiAboutItem.TwoLinesItem.DatabaseVersion -> R.string.about_database_version
        is UiAboutItem.TwoLinesItem.TopologyVersion -> R.string.about_topology_version
        is UiAboutItem.TwoLinesItem.Bluesky -> R.string.about_bluesky
        is UiAboutItem.TwoLinesItem.Website -> R.string.about_website
    }
}

private val UiAboutItem.TwoLinesItem.captionText: String @Composable get() {
    return when (this) {
        is UiAboutItem.TwoLinesItem.AppVersion -> stringResource(
            id = R.string.about_version_format,
            versionName ?: "null",
            versionCode
        )
        is UiAboutItem.TwoLinesItem.Author -> stringResource(id = R.string.app_author)
        is UiAboutItem.TwoLinesItem.DatabaseVersion -> {
            date?.let {
                stringResource(
                    id = R.string.about_database_version_format,
                    it.time,
                    LocalDateTimeFormatter.current.format(it)
                )
            } ?: stringResource(id = R.string.about_database_version_loading)
        }
        is UiAboutItem.TwoLinesItem.TopologyVersion -> {
            topologyId ?: stringResource(id = R.string.about_topology_version_loading)
        }
        is UiAboutItem.TwoLinesItem.Bluesky -> stringResource(id = R.string.app_bluesky)
        is UiAboutItem.TwoLinesItem.Website -> stringResource(id = R.string.app_website)
    }
}

@Preview(
    name = "One line about item - light",
    group = "One line about item",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "One line about item - dark",
    group = "One line about item",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AboutItem1LinePreview() {
    MyBusTheme {
        AboutItem1Line(
            item = UiAboutItem.OneLineItem.PrivacyPolicy,
            onItemClicked = { }
        )
    }
}

@Preview(
    name = "Two lines about item - light",
    group = "Two lines about item",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Two lines about item - dark",
    group = "Two lines about item",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AboutItem2LinePreview() {
    MyBusTheme {
        AboutItem2Lines(
            item = UiAboutItem.TwoLinesItem.Website,
            onItemClicked = { }
        )
    }
}
