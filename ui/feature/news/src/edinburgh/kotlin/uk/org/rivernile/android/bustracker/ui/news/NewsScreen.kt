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

package uk.org.rivernile.android.bustracker.ui.news

import android.content.res.Configuration
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.window.core.layout.WindowSizeClass
import uk.org.rivernile.android.bustracker.ui.formatters.LocalNumberFormatter
import uk.org.rivernile.android.bustracker.ui.formatters.rememberNumberFormatter
import uk.org.rivernile.android.bustracker.ui.interop.MenuProvider
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.DiversionsScreen
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversion
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversionsState
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.IncidentsScreen
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncident
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncidentsState
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

internal const val TEST_TAG_TAB_INCIDENTS = "incidents-tab"
internal const val TEST_TAG_TAB_DIVERSIONS = "diversions-tab"
internal const val TEST_TAG_TAB_ICON = "tab-icon"
internal const val TEST_TAG_TAB_BADGE_COUNT = "count-tab-badge"

private const val TAB_INCIDENTS = 0
private const val TAB_DIVERSIONS = 1

/**
 * The main entry point to the News screen.
 *
 * @param modifier Any [Modifier]s which should be applied.
 * @param viewModel An instance of [NewsViewModel] to coordinate state.
 * @param windowSizeClass The size class of the window, used to adjust layout for available space.
 */
@Composable
internal fun NewsScreen(
    modifier: Modifier = Modifier,
    viewModel: NewsViewModel = viewModel(),
    windowSizeClass: WindowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
) {
    val uiState by viewModel.uiStateFlow.collectAsStateWithLifecycle()

    NewsScreenWithState(
        state = uiState,
        windowSizeClass = windowSizeClass,
        modifier = modifier,
        onRefresh = viewModel::onRefresh,
        onIncidentMoreDetailsClicked = viewModel::onIncidentMoreDetailsClicked,
        onIncidentActionLaunched = viewModel::onIncidentActionLaunched,
        onDiversionMoreDetailsClicked = viewModel::onDiversionMoreDetailsClicked,
        onDiversionActionLaunched = viewModel::onDiversionActionLaunched,
        onErrorSnackbarShown = viewModel::onServiceUpdatesTransientErrorShown
    )
}

/**
 * The entry point in to the News screen for tests which does not depend on [NewsViewModel] as the
 * state can be injected directly.
 *
 * @param state The [UiState] to render.
 * @param windowSizeClass The size class of the window, used to adjust layout for available space.
 * @param modifier Any [Modifier]s which should be applied.
 * @param onRefresh A lambda which is executed when the user performs a refresh action.
 * @param onIncidentMoreDetailsClicked A lambda which is executed when the user presses the
 * "More details" button on incidents.
 * @param onIncidentActionLaunched A lambda which is executed when any incidents actions have been
 * launched.
 * @param onDiversionMoreDetailsClicked A lambda which is executed when the user presses the
 * "More details" button on diversions.
 * @param onDiversionActionLaunched A lambda which is executed when any diversions actions have been
 * launched.
 * @param onErrorSnackbarShown A lambda which is executed when the error snackbar has been shown.
 */
@Composable
internal fun NewsScreenWithState(
    state: UiState,
    windowSizeClass: WindowSizeClass,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onIncidentMoreDetailsClicked: (UiIncident) -> Unit,
    onIncidentActionLaunched: () -> Unit,
    onDiversionMoreDetailsClicked: (UiDiversion) -> Unit,
    onDiversionActionLaunched: () -> Unit,
    onErrorSnackbarShown: (Long) -> Unit
) {
    Column (
        modifier = modifier
    ) {
        var selectedTabIndex by rememberSaveable { mutableIntStateOf(0) }
        val tabCompactMode = !windowSizeClass
            .isHeightAtLeastBreakpoint(WindowSizeClass.HEIGHT_DP_MEDIUM_LOWER_BOUND)

        NewsTabBar(
            selectedTabIndex = selectedTabIndex,
            tabBadges = state.tabBadges,
            isCompactMode = tabCompactMode,
            onTabClicked = { selectedTabIndex = it }
        )

        NewsContent(
            selectedTabIndex = selectedTabIndex,
            incidentsState = state.incidentsState,
            diversionsState = state.diversionsState,
            onRefresh = onRefresh,
            onIncidentMoreDetailsClicked = onIncidentMoreDetailsClicked,
            onIncidentActionLaunched = onIncidentActionLaunched,
            onDiversionMoreDetailsClicked = onDiversionMoreDetailsClicked,
            onDiversionActionLaunched = onDiversionActionLaunched,
            onErrorSnackbarShown = onErrorSnackbarShown
        )
    }

    if (!LocalInspectionMode.current) {
        NewsMenuItems(
            actionButtons = state.actionButtons,
            onRefresh = onRefresh
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsTabBar(
    selectedTabIndex: Int,
    tabBadges: UiTabBadges,
    modifier: Modifier = Modifier,
    isCompactMode: Boolean = false,
    onTabClicked: (Int) -> Unit
) {
    PrimaryTabRow(
        selectedTabIndex = selectedTabIndex,
        modifier = modifier
    ) {
        NewsTab(
            selected = selectedTabIndex == TAB_INCIDENTS,
            text = stringResource(id = R.string.news_fragment_tab_incidents),
            iconRes = R.drawable.ic_bus_alert,
            modifier = Modifier
                .semantics {
                    testTag = TEST_TAG_TAB_INCIDENTS
                },
            isCompactMode = isCompactMode,
            countForBadge = tabBadges.incidentsCount,
            onClick = { onTabClicked(TAB_INCIDENTS) }
        )

        NewsTab(
            selected = selectedTabIndex == TAB_DIVERSIONS,
            text = stringResource(id = R.string.news_fragment_tab_diversions),
            iconRes = R.drawable.ic_fork_right,
            modifier = Modifier
                .semantics {
                    testTag = TEST_TAG_TAB_DIVERSIONS
                },
            isCompactMode = isCompactMode,
            countForBadge = tabBadges.diversionsCount,
            onClick = { onTabClicked(TAB_DIVERSIONS) }
        )
    }
}

@Composable
private fun NewsTab(
    selected: Boolean,
    text: String,
    @DrawableRes iconRes: Int,
    modifier: Modifier = Modifier,
    isCompactMode: Boolean = false,
    countForBadge: Int? = null,
    onClick: () -> Unit
) {
    Tab(
        selected = selected,
        onClick = onClick,
        modifier = modifier,
        text = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = if (!isCompactMode) {
            {
                NewsTabIconWithBadge(
                    iconRes = iconRes,
                    contentDescription = text,
                    countForBadge = countForBadge
                )
            }
        } else {
            null
        },
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun NewsTabIcon(
    @DrawableRes iconRes: Int,
    contentDescription: String?
) {
    Icon(
        painter = painterResource(id = iconRes),
        contentDescription = contentDescription,
        modifier = Modifier
            .semantics {
                testTag = TEST_TAG_TAB_ICON
            }
    )
}

@Composable
private fun NewsTabIconWithBadge(
    @DrawableRes iconRes: Int,
    contentDescription: String?,
    countForBadge: Int?
) {
    if (countForBadge != null) {
        BadgedBox(
            badge = {
                Badge {
                    Text(
                        text = LocalNumberFormatter.current.format(countForBadge),
                        modifier = Modifier
                            .semantics {
                                testTag = TEST_TAG_TAB_BADGE_COUNT
                            }
                    )
                }
            }
        ) {
            NewsTabIcon(
                iconRes = iconRes,
                contentDescription = contentDescription
            )
        }
    } else {
        NewsTabIcon(
            iconRes = iconRes,
            contentDescription = contentDescription
        )
    }
}

@Composable
private fun NewsMenuItems(
    actionButtons: UiActionButtons,
    onRefresh: () -> Unit
) {
    MenuProvider(
        onCreateMenu = { menu, menuInflater ->
            menuInflater.inflate(R.menu.news_option_menu, menu)
        },
        onPrepareMenu = { menu ->
            val refreshMenuItem = checkNotNull(menu.findItem(R.id.news_option_menu_refresh))
            val refreshActionButton = actionButtons.refresh
            refreshMenuItem.isEnabled = refreshActionButton.isEnabled

            if (refreshActionButton.isRefreshing) {
                refreshMenuItem.setActionView(R.layout.news_option_menu_indeterminate_progress)
            } else {
                refreshMenuItem.actionView = null
            }
        },
        onMenuItemSelected = { item ->
            when (item.itemId) {
                R.id.news_option_menu_refresh -> {
                    onRefresh()
                    true
                }
                else -> false
            }
        }
    )
}

@Composable
private fun NewsContent(
    selectedTabIndex: Int,
    incidentsState: UiIncidentsState,
    diversionsState: UiDiversionsState,
    modifier: Modifier = Modifier,
    onRefresh: () -> Unit,
    onIncidentMoreDetailsClicked: (UiIncident) -> Unit,
    onIncidentActionLaunched: () -> Unit,
    onDiversionMoreDetailsClicked: (UiDiversion) -> Unit,
    onDiversionActionLaunched: () -> Unit,
    onErrorSnackbarShown: (Long) -> Unit
) {
    when (selectedTabIndex) {
        TAB_INCIDENTS -> IncidentsScreen(
            state = incidentsState,
            modifier = modifier,
            onRefresh = onRefresh,
            onMoreDetailsClicked = onIncidentMoreDetailsClicked,
            onActionLaunched = onIncidentActionLaunched,
            onErrorSnackbarShown = onErrorSnackbarShown
        )
        TAB_DIVERSIONS -> DiversionsScreen(
            state = diversionsState,
            modifier = modifier,
            onRefresh = onRefresh,
            onMoreDetailsClicked = onDiversionMoreDetailsClicked,
            onActionLaunched = onDiversionActionLaunched,
            onErrorSnackbarShown = onErrorSnackbarShown
        )
    }
}

@Preview(
    name = "News screen - light",
    group = "News screen",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "News screen - dark",
    group = "News screen",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun NewsScreenPreview() {
    MyBusThemeWithCompositionLocals {
        NewsScreenWithState(
            state = UiState(
                tabBadges = UiTabBadges(
                    diversionsCount = 5
                )
            ),
            windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass,
            onRefresh = { },
            onIncidentMoreDetailsClicked = { },
            onIncidentActionLaunched = { },
            onDiversionMoreDetailsClicked = { },
            onDiversionActionLaunched = { },
            onErrorSnackbarShown = { }
        )
    }
}

@Preview(
    name = "News screen - tab - selected - no count - light",
    group = "News screen - tab",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "News screen - tab - selected - no count - dark",
    group = "News screen - tab",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun NewsTabSelectedWithNoCountPreview() {
    NewsTabPreview(
        selected = true,
        countForBadge = null
    )
}

@Preview(
    name = "News screen - tab - unselected - no count - light",
    group = "News screen - tab",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "News screen - tab - unselected - no count - dark",
    group = "News screen - tab",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun NewsTabUnselectedWithNoCountPreview() {
    NewsTabPreview(
        selected = false,
        countForBadge = null
    )
}

@Preview(
    name = "News screen - tab - unselected - has count - light",
    group = "News screen - tab",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "News screen - tab - unselected - has count - dark",
    group = "News screen - tab",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun NewsTabUnselectedWithCountPreview() {
    NewsTabPreview(
        selected = false,
        countForBadge = 5
    )
}

@Preview(
    name = "News screen - tab - selected - has count - light",
    group = "News screen - tab",
    showBackground = true,
    backgroundColor = 0xFFFFFFFF,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Preview(
    name = "News screen - tab - selected - has count - dark",
    group = "News screen - tab",
    showBackground = true,
    backgroundColor = 0xFF000000,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun NewsTabSelectedWithCountPreview() {
    NewsTabPreview(
        selected = true,
        countForBadge = 5
    )
}

@Composable
private fun NewsTabPreview(
    selected: Boolean,
    countForBadge: Int?
) {
    MyBusThemeWithCompositionLocals {
        NewsTab(
            selected = selected,
            text = stringResource(id = R.string.news_fragment_tab_incidents),
            iconRes = R.drawable.ic_bus_alert,
            countForBadge = countForBadge,
            onClick = { }
        )
    }
}

@Composable
private fun MyBusThemeWithCompositionLocals(
    content: @Composable () -> Unit
) {
    MyBusTheme {
        CompositionLocalProvider(
            LocalNumberFormatter provides rememberNumberFormatter(),
            content = content
        )
    }
}
