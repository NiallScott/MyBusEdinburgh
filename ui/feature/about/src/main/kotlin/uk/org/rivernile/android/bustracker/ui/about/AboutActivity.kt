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
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.LocalActivity
import androidx.activity.compose.ReportDrawn
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.dimensionResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.ui.formatters.LocalDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.formatters.rememberDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import java.util.Date
import javax.inject.Inject

/**
 * This [android.app.Activity] shows the user 'about' information for the application as a list of
 * items.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class AboutActivity : ComponentActivity() {

    @Inject
    internal lateinit var actionLauncher: AboutActionLauncher

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            MyBusTheme {
                CompositionLocalProvider(
                    LocalAboutActionLauncher provides actionLauncher,
                    LocalDateTimeFormatter provides rememberDateTimeFormatter()
                ) {
                    EdgeToEdge()
                    AboutScreen(onNavigateUp = this::onNavigateUp)
                    ReportDrawn()
                }
            }
        }
    }
}

private const val CONTENT_TYPE_ONE_LINE = 1
private const val CONTENT_TYPE_TWO_LINES = 2

internal val LocalAboutActionLauncher = staticCompositionLocalOf<AboutActionLauncher> {
    error("LocalAboutActionLauncher has not been set with a value.")
}

@Composable
private fun EdgeToEdge() {
    val activity = LocalActivity.current as ComponentActivity
    val isDarkMode = isSystemInDarkTheme()
    val surfaceColour = MaterialTheme
        .colorScheme
        .surfaceContainer
        .toArgb()

    LaunchedEffect(Unit) {
        if (isDarkMode) {
            activity.enableEdgeToEdge(
                navigationBarStyle = SystemBarStyle.dark(surfaceColour)
            )
        } else {
            activity.enableEdgeToEdge(
                navigationBarStyle = SystemBarStyle.light(
                    scrim = surfaceColour,
                    darkScrim = surfaceColour
                )
            )
        }
    }
}

@Composable
private fun AboutScreen(
    viewModel: AboutViewModel = viewModel(),
    onNavigateUp: () -> Unit
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    AboutScreenWithState(
        state = uiState,
        onNavigateUp = onNavigateUp,
        onItemClicked = viewModel::onItemClicked,
        onCreditsDialogDismissed = viewModel::onCreditsDialogDismissed,
        onOpenSourceLicenceDialogDismissed = viewModel::onOpenSourceDialogDismissed,
        onActionLaunched = viewModel::onActionLaunched
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun AboutScreenWithState(
    state: UiState,
    onNavigateUp: () -> Unit,
    onItemClicked: (UiAboutItem) -> Unit,
    onCreditsDialogDismissed: () -> Unit,
    onOpenSourceLicenceDialogDismissed: () -> Unit,
    onActionLaunched: () -> Unit
) {
    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior()

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            AboutTopAppBar(
                scrollBehavior = scrollBehavior,
                onNavigateUp = onNavigateUp
            )
        }
    ) { innerPadding ->
        AboutItemsList(
            aboutItems = state.items,
            modifier = Modifier.padding(innerPadding),
            onItemClicked = onItemClicked
        )
    }

    if (state.isCreditsShown) {
        CreditsDialog(onDismissRequest = onCreditsDialogDismissed)
    }

    if (state.isOpenSourceLicencesShown) {
        OpenSourceLicenceDialog(onDismissRequest = onOpenSourceLicenceDialogDismissed)
    }

    state.action?.let {
        LaunchAction(
            action = it,
            onActionLaunched = onActionLaunched
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AboutTopAppBar(
    scrollBehavior: TopAppBarScrollBehavior,
    onNavigateUp: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            scrolledContainerColor = MaterialTheme.colorScheme.surfaceContainer
        ),
        title = {
            Text(
                text = stringResource(id = R.string.about_title),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            IconButton(
                onClick = onNavigateUp
            ) {
                Icon(
                    painter = painterResource(id = R.drawable.ic_arrow_back),
                    contentDescription = stringResource(id = Rcore.string.navigate_up)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
private fun AboutItemsList(
    aboutItems: ImmutableList<UiAboutItem>,
    modifier: Modifier = Modifier,
    onItemClicked: (UiAboutItem) -> Unit
) {
    LazyColumn(
        modifier = modifier.fillMaxSize(),
        contentPadding = PaddingValues(
            vertical = dimensionResource(id = Rcore.dimen.padding_default)
        )
    ) {
        items(
            items = aboutItems,
            contentType = UiAboutItem::contentType
        ) {
            AboutItem(
                item = it,
                modifier = Modifier.animateItem(),
                onItemClicked = onItemClicked
            )
            HorizontalDivider()
        }
    }
}

@Composable
private fun LaunchAction(
    action: UiAction,
    onActionLaunched: () -> Unit
) {
    val actionLauncher = LocalAboutActionLauncher.current

    LaunchedEffect(action) {
        when (action) {
            is UiAction.ShowStoreListing ->
                actionLauncher.launchStoreListing()
            is UiAction.ShowAuthorWebsite ->
                actionLauncher.launchAuthorWebsite()
            is UiAction.ShowAppWebsite ->
                actionLauncher.launchAppWebsite()
            is UiAction.ShowAppBluesky ->
                actionLauncher.launchAppBluesky()
            is UiAction.ShowPrivacyPolicy ->
                actionLauncher.launchPrivacyPolicy()
        }

        onActionLaunched()
    }
}

private val UiAboutItem.contentType: Int get() {
    return when (this) {
        is UiAboutItem.OneLineItem -> CONTENT_TYPE_ONE_LINE
        is UiAboutItem.TwoLinesItem -> CONTENT_TYPE_TWO_LINES
    }
}

@Preview(
    name = "About screen (light)",
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "About screen (dark)",
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun AboutScreenPreview() {
    MyBusTheme {
        CompositionLocalProvider(
            LocalDateTimeFormatter provides rememberDateTimeFormatter()
        ) {
            AboutScreenWithState(
                state = UiState(
                    items = persistentListOf(
                        UiAboutItem.TwoLinesItem.AppVersion("1.2.3", 4),
                        UiAboutItem.TwoLinesItem.Author,
                        UiAboutItem.TwoLinesItem.Website,
                        UiAboutItem.TwoLinesItem.Bluesky,
                        UiAboutItem.TwoLinesItem.DatabaseVersion(Date(1712498400000L)),
                        UiAboutItem.TwoLinesItem.TopologyVersion("abc123"),
                        UiAboutItem.OneLineItem.Credits,
                        UiAboutItem.OneLineItem.PrivacyPolicy,
                        UiAboutItem.OneLineItem.OpenSourceLicences
                    )
                ),
                onNavigateUp = { },
                onItemClicked = { },
                onCreditsDialogDismissed = { },
                onOpenSourceLicenceDialogDismissed = { },
                onActionLaunched = { }
            )
        }
    }
}
