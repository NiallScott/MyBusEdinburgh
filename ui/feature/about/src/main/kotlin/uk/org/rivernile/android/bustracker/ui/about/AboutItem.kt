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

package uk.org.rivernile.android.bustracker.ui.about

import androidx.annotation.StringRes
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import java.text.DateFormat

/**
 * Provides an 'about' item to be displayed.
 *
 * @param item The [UiAboutItem] to display.
 * @param dateFormat An instance of [DateFormat] used to format dates for display.
 * @param modifier Any [Modifier] to be applied.
 * @param onItemClicked A lambda which is executed when an item is clicked.
 * @author Niall Scott
 */
@Composable
internal fun AboutItem(
    item: UiAboutItem,
    dateFormat: DateFormat,
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
            dateFormat = dateFormat,
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
    AboutItemTitle(
        text = stringResource(id = item.toTitleStringRes()),
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
            .padding(
                top = dimensionResource(id = R.dimen.padding_default),
                bottom = dimensionResource(id = R.dimen.padding_default),
                start = dimensionResource(id = R.dimen.padding_double),
                end = dimensionResource(id = R.dimen.padding_double)
            )
    )
}

@Composable
private fun AboutItem2Lines(
    item: UiAboutItem.TwoLinesItem,
    dateFormat: DateFormat,
    modifier: Modifier = Modifier,
    onItemClicked: (UiAboutItem) -> Unit
) {
    val captionText = when (item) {
        is UiAboutItem.TwoLinesItem.AppVersion -> stringResource(
            id = R.string.about_version_format,
            item.versionName,
            item.versionCode
        )
        is UiAboutItem.TwoLinesItem.Author -> stringResource(id = R.string.app_author)
        is UiAboutItem.TwoLinesItem.DatabaseVersion -> {
            item.date?.let {
                stringResource(
                    id = R.string.about_database_version_format,
                    it.time,
                    dateFormat.format(it)
                )
            } ?: stringResource(id = R.string.about_database_version_loading)
        }
        is UiAboutItem.TwoLinesItem.TopologyVersion -> {
            item.topologyId ?: stringResource(id = R.string.about_topology_version_loading)
        }
        is UiAboutItem.TwoLinesItem.Twitter -> stringResource(id = R.string.app_twitter)
        is UiAboutItem.TwoLinesItem.Website -> stringResource(id = R.string.app_website)
    }

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
            .padding(
                top = dimensionResource(id = R.dimen.padding_default),
                bottom = dimensionResource(id = R.dimen.padding_default),
                start = dimensionResource(id = R.dimen.padding_double),
                end = dimensionResource(id = R.dimen.padding_double)
            )
    ) {
        AboutItemTitle(text = stringResource(id = item.toTitleStringRes()))
        AboutItemCaption(text = captionText)
    }
}

@Composable
private fun AboutItemTitle(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier.fillMaxWidth(),
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
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        style = MaterialTheme.typography.bodyMedium
    )
}

@StringRes
private fun UiAboutItem.toTitleStringRes(): Int {
    return when (this) {
        is UiAboutItem.OneLineItem.Credits -> R.string.about_credits
        is UiAboutItem.OneLineItem.OpenSourceLicences -> R.string.about_open_source
        is UiAboutItem.OneLineItem.PrivacyPolicy -> R.string.about_privacy_policy
        is UiAboutItem.TwoLinesItem.AppVersion -> R.string.about_version
        is UiAboutItem.TwoLinesItem.Author -> R.string.about_author
        is UiAboutItem.TwoLinesItem.DatabaseVersion -> R.string.about_database_version
        is UiAboutItem.TwoLinesItem.TopologyVersion -> R.string.about_topology_version
        is UiAboutItem.TwoLinesItem.Twitter -> R.string.about_twitter
        is UiAboutItem.TwoLinesItem.Website -> R.string.about_website
    }
}

@Preview(name = "One line about item")
@Composable
private fun AboutItem1LinePreview() {
    AboutItem1Line(
        item = UiAboutItem.OneLineItem.PrivacyPolicy,
        onItemClicked = { }
    )
}

@Preview(name = "Two lines about item")
@Composable
private fun AboutItem2LinePreview() {
    AboutItem2Lines(
        item = UiAboutItem.TwoLinesItem.Website,
        dateFormat = DateFormat.getDateTimeInstance(),
        onItemClicked = { }
    )
}