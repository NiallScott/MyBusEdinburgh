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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import uk.org.rivernile.android.bustracker.ui.news.R
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore

/**
 * A reusable [Composable] used to render the content layout for Service Updates screens.
 *
 * @param content The content to render.
 * @param modifier A [Modifier] to be applied to the Service Updates screen.
 * @param onRefresh A lambda which is called when a refresh request is performed.
 * @param itemContent A lambda which generates individual Service Update items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T : UiServiceUpdate> ServiceUpdatesScreen(
    content: UiContent<T>,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = content.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        when (content) {
            is UiContent.InProgress -> EmptyProgress()
            is UiContent.Populated -> ItemsList(
                items = content.items,
                itemContent = itemContent
            )
            is UiContent.Error -> Error(error = content.error)
        }
    }
}

@Composable
private fun EmptyProgress(
    modifier: Modifier = Modifier
) {
    CircularProgressIndicator(
        modifier = modifier
    )
}

@Composable
private fun <T : UiServiceUpdate> ItemsList(
    items: List<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    val nestedScrollInterop = rememberNestedScrollInteropConnection()

    LazyColumn(
        modifier = modifier
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
            itemContent(item)
        }
    }
}

@Composable
private fun Error(
    error: UiError,
    modifier: Modifier = Modifier
) {
    @StringRes val titleRes: Int
    @DrawableRes val iconRes: Int

    when (error) {
        UiError.NO_CONNECTIVITY -> {
            titleRes = R.string.serviceupdates_error_noconnectivity
            iconRes = R.drawable.ic_error_cloud_off
        }
        UiError.EMPTY -> {
            titleRes = R.string.serviceupdates_error_empty
            iconRes = R.drawable.ic_error_newspaper
        }
        UiError.IO -> {
            titleRes = R.string.serviceupdates_error_io
            iconRes = R.drawable.ic_error_generic
        }
        UiError.SERVER -> {
            titleRes = R.string.serviceupdates_error_server
            iconRes = R.drawable.ic_error_generic
        }
    }

    Column(
        modifier = modifier.padding(dimensionResource(id = Rcore.dimen.padding_double)),
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

@Preview(
    name = "Service Updates - progress - light",
    group = "Service Updates - progress",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Service Updates - progress - dark",
    group = "Service Updates - progress",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun EmptyProgressPreview() {
    MyBusTheme {
        EmptyProgress()
    }
}

@Preview(
    name = "Service Updates - error - no connectivity - light",
    group = "Service Updates - error - no connectivity",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Service Updates - error - no connectivity - dark",
    group = "Service Updates - error - no connectivity",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun NoConnectivityErrorPreview() {
    MyBusTheme {
        Error(UiError.NO_CONNECTIVITY)
    }
}

@Preview(
    name = "Service Updates - error - empty - light",
    group = "Service Updates - error - empty",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Service Updates - error - empty - dark",
    group = "Service Updates - error - empty",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun EmptyErrorPreview() {
    MyBusTheme {
        Error(UiError.EMPTY)
    }
}

@Preview(
    name = "Service Updates - error - IO - light",
    group = "Service Updates - error - IO",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Service Updates - error - IO - dark",
    group = "Service Updates - error - IO",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun IoErrorPreview() {
    MyBusTheme {
        Error(UiError.IO)
    }
}

@Preview(
    name = "Service Updates - error - server - light",
    group = "Service Updates - error - server",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Service Updates - error - server - dark",
    group = "Service Updates - error - server",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ServerErrorPreview() {
    MyBusTheme {
        Error(UiError.SERVER)
    }
}