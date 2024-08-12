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

package uk.org.rivernile.android.bustracker.ui.news

import android.os.Bundle
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.PrimaryTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.ui.news.diversions.DiversionsScreen
import uk.org.rivernile.android.bustracker.ui.news.incidents.IncidentsScreen
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

/**
 * A [Fragment] which displays news items within the updates, for example travel updates or news
 * about the app.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class NewsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ) = content {
        MyBusTheme {
            NewsScreen()
        }
    }
}

private const val TAB_INCIDENTS = 0
private const val TAB_DIVERSIONS = 1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun NewsScreen() {
    Column {
        var selectedTab by rememberSaveable { mutableIntStateOf(0) }

        PrimaryTabRow(
            selectedTabIndex = selectedTab
        ) {
            NewsTab(
                selected = selectedTab == TAB_INCIDENTS,
                text = stringResource(id = R.string.news_fragment_tab_incidents),
                iconRes = R.drawable.ic_bus_alert,
                onClick = { selectedTab = TAB_INCIDENTS }
            )

            NewsTab(
                selected = selectedTab == TAB_DIVERSIONS,
                text = stringResource(id = R.string.news_fragment_tab_diversions),
                iconRes = R.drawable.ic_fork_right,
                onClick = { selectedTab = TAB_DIVERSIONS }
            )
        }

        when (selectedTab) {
            TAB_INCIDENTS -> IncidentsScreen()
            TAB_DIVERSIONS -> DiversionsScreen()
        }
    }
}

@Composable
private fun NewsTab(
    selected: Boolean,
    text: String,
    @DrawableRes iconRes: Int,
    onClick: () -> Unit
) {
    Tab(
        selected = selected,
        text = {
            Text(
                text = text,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        icon = {
            Icon(
                painter = painterResource(id = iconRes),
                contentDescription = text
            )
        },
        onClick = onClick,
        unselectedContentColor = MaterialTheme.colorScheme.onSurfaceVariant
    )
}