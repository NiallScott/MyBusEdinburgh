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

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import androidx.constraintlayout.compose.atLeast
import kotlinx.collections.immutable.ImmutableList
import uk.org.rivernile.android.bustracker.ui.news.R
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore

/**
 * A reusable [Composable] used to render the content layout for Service Updates screens.
 *
 * @param content The content to render.
 * @param modifier A [Modifier] to be applied to the Service Updates screen.
 * @param onRefresh A lambda which is called when a refresh request is performed.
 * @param onErrorSnackbarShown A lambda which is called when the snackbar transient error has been
 * shown.
 * @param itemContent A lambda which generates individual Service Update items.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun <T : UiServiceUpdate> ServiceUpdatesScreen(
    content: UiContent<T>,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onErrorSnackbarShown: (Long) -> Unit,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    PullToRefreshBox(
        isRefreshing = content.isRefreshing,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Content(
            content = content,
            onErrorSnackbarShown = onErrorSnackbarShown,
            itemContent = itemContent
        )
    }
}

@Composable
private fun <T : UiServiceUpdate> BoxScope.Content(
    content: UiContent<T>,
    onErrorSnackbarShown: (Long) -> Unit,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    val nestedScrollInterop = rememberNestedScrollInteropConnection()

    when (content) {
        is UiContent.InProgress -> EmptyProgress(
            modifier = Modifier.align(Alignment.Center)
        )
        is UiContent.Populated -> PopulatedContent(
            content = content,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollInterop),
            onErrorSnackbarShown = onErrorSnackbarShown,
            itemContent = itemContent
        )
        is UiContent.Error -> InlineError(
            error = content.error,
            modifier = Modifier
                .fillMaxSize()
                .nestedScroll(nestedScrollInterop)
        )
    }
}

@Composable
private fun <T : UiServiceUpdate> PopulatedContent(
    content: UiContent.Populated<T>,
    modifier: Modifier = Modifier,
    onErrorSnackbarShown: (Long) -> Unit,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    ConstraintLayout(
        modifier = modifier
    ) {
        val (contentHeaderBarRef, itemsListRef, errorSnackbarRef) = createRefs()

        ItemsList(
            items = content.items,
            modifier = Modifier
                .constrainAs(itemsListRef) {
                    width = Dimension.fillToConstraints
                    height = Dimension.fillToConstraints
                    top.linkTo(contentHeaderBarRef.bottom)
                    bottom.linkTo(parent.bottom)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                },
            itemContent = itemContent
        )

        ContentHeaderBar(
            lastRefreshed = content.lastRefreshTime,
            hasInternetConnectivity = content.hasInternetConnectivity,
            modifier = Modifier
                .constrainAs(contentHeaderBarRef) {
                    width = Dimension.fillToConstraints
                    height = Dimension.preferredWrapContent.atLeast(48.dp)
                    top.linkTo(parent.top)
                    start.linkTo(parent.start)
                    end.linkTo(parent.end)
                }
        )

        ErrorSnackbar(
            error = content.error,
            loadTimeMillis = content.loadTimeMillis,
            modifier = Modifier
                .constrainAs(errorSnackbarRef) {
                    top.linkTo(contentHeaderBarRef.bottom, margin = 16.dp)
                },
            onErrorSnackbarShown = onErrorSnackbarShown
        )
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
private fun ContentHeaderBar(
    lastRefreshed: UiLastRefreshed,
    hasInternetConnectivity: Boolean,
    modifier: Modifier = Modifier
) {
    val paddingDefault = dimensionResource(id = Rcore.dimen.padding_default)
    val paddingDouble = dimensionResource(id = Rcore.dimen.padding_double)

    Row(
        modifier = modifier
            .background(color = MaterialTheme.colorScheme.inverseSurface)
            .padding(
                top = paddingDefault,
                bottom = paddingDefault,
                start = paddingDouble,
                end = paddingDouble
            ),
        horizontalArrangement = Arrangement.spacedBy(paddingDouble),
        verticalAlignment = Alignment.CenterVertically
    ) {
        ContentHeaderBarLastRefreshedText(
            lastRefreshed = lastRefreshed,
            modifier = Modifier.weight(1f)
        )

        if (hasInternetConnectivity) {
            Spacer(
                modifier = Modifier.size(24.dp)
            )
        } else {
            ContentHeaderBarNoInternetConnectivityIcon(
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun <T : UiServiceUpdate> ItemsList(
    items: ImmutableList<T>,
    modifier: Modifier = Modifier,
    itemContent: @Composable LazyItemScope.(item: T) -> Unit
) {
    LazyColumn(
        modifier = modifier,
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
private fun InlineError(
    error: UiError,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .padding(dimensionResource(id = Rcore.dimen.padding_double))
            .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(
            space = dimensionResource(id = Rcore.dimen.padding_default),
            alignment = Alignment.CenterVertically
        ),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ErrorIcon(
            iconRes = error.iconResId
        )

        ErrorText(
            text = stringResource(id = error.titleResId),
        )
    }
}

@Composable
private fun ContentHeaderBarLastRefreshedText(
    lastRefreshed: UiLastRefreshed,
    modifier: Modifier = Modifier
) {
    Text(
        text = getLastRefreshedString(lastRefreshed),
        modifier = modifier,
        color = MaterialTheme.colorScheme.inverseOnSurface,
        overflow = TextOverflow.Ellipsis,
        maxLines = 1,
        style = MaterialTheme.typography.bodyMedium
    )
}

@Composable
private fun ContentHeaderBarNoInternetConnectivityIcon(
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(id = R.drawable.ic_error_cloud_off),
        contentDescription = stringResource(
            id = R.string.serviceupdates_no_connectivity_content_description
        ),
        modifier = modifier,
        tint = MaterialTheme.colorScheme.inverseOnSurface
    )
}

@Composable
private fun ErrorSnackbar(
    error: UiError?,
    loadTimeMillis: Long,
    modifier: Modifier = Modifier,
    onErrorSnackbarShown: (Long) -> Unit
) {
    // This is a temporary workaround because the main app UI is still using Views. When this is
    // converted to Compose and uses the scaffolding, we'll use a central Snackbar instead.
    val snackbarHostState = remember { SnackbarHostState() }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = modifier
    )

    if (error != null) {
        val errorText = stringResource(error.titleResId)

        LaunchedEffect(loadTimeMillis) {
            snackbarHostState.showSnackbar(message = errorText)
            onErrorSnackbarShown(loadTimeMillis)
        }
    }
}

@Composable
private fun ErrorIcon(
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier
) {
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = null,
        modifier = modifier,
        tint = MaterialTheme.colorScheme.primary
    )
}

@Composable
private fun ErrorText(
    text: String,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        modifier = modifier,
        textAlign = TextAlign.Center,
        color = MaterialTheme.colorScheme.onSurface,
        overflow = TextOverflow.Ellipsis,
        style = MaterialTheme.typography.bodyLarge
    )
}

@Composable
private fun getLastRefreshedString(lastRefreshed: UiLastRefreshed): String {
    val timeComponent = when (lastRefreshed) {
        is UiLastRefreshed.Never -> stringResource(R.string.serviceupdates_last_updated_never)
        is UiLastRefreshed.Now -> stringResource(R.string.serviceupdates_last_updated_now)
        is UiLastRefreshed.Minutes -> pluralStringResource(
            R.plurals.serviceupdates_last_updated_minsago,
            lastRefreshed.minutes,
            lastRefreshed.minutes
        )
        is UiLastRefreshed.MoreThanOneHour ->
            stringResource(R.string.serviceupdates_last_updated_greaterthanhour)
    }

    return stringResource(R.string.serviceupdates_last_updated, timeComponent)
}

@get:StringRes
private val UiError.titleResId: Int get() {
    return when (this) {
        UiError.NO_CONNECTIVITY -> R.string.serviceupdates_error_noconnectivity
        UiError.EMPTY -> R.string.serviceupdates_error_empty
        UiError.IO -> R.string.serviceupdates_error_io
        UiError.SERVER -> R.string.serviceupdates_error_server
    }
}

@get:DrawableRes
private val UiError.iconResId: Int get() {
    return when (this) {
        UiError.NO_CONNECTIVITY -> R.drawable.ic_error_cloud_off
        UiError.EMPTY -> R.drawable.ic_error_newspaper
        UiError.IO, UiError.SERVER -> R.drawable.ic_error_generic
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
    name = "Service Updates - content header bar - light",
    group = "Service Updates - content header bar",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "Service Updates - content header bar - dark",
    group = "Service Updates - content header bar",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ContentHeaderBarPreview() {
    ContentHeaderBar(
        lastRefreshed = UiLastRefreshed.Minutes(minutes = 5),
        hasInternetConnectivity = false,
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 48.dp)
    )
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
        InlineError(UiError.NO_CONNECTIVITY)
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
        InlineError(UiError.EMPTY)
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
        InlineError(UiError.IO)
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
        InlineError(UiError.SERVER)
    }
}