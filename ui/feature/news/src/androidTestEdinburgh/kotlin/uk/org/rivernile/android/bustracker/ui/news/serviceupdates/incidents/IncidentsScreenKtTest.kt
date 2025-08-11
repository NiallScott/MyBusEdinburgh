/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiContent
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiLastRefreshed
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for `IncidentsScreen.kt`.
 *
 * @author Niall Scott
 */
class IncidentsScreenKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun nullActionDoesNotLaunchAnyActions() {
        val launchUrlTracker = LaunchUrlTracker()
        val onActionLaunchedTracker = InvocationCountTracker()
        val incidentsActionLauncher = FakeIncidentsActionLauncher(
            onLaunchUrl = launchUrlTracker
        )
        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals(
                incidentsActionLauncher = incidentsActionLauncher
            ) {
                IncidentsScreen(
                    state = UiIncidentsState(
                        content = UiContent.Populated(
                            isRefreshing = false,
                            items = persistentListOf(),
                            error = null,
                            hasInternetConnectivity = false,
                            lastRefreshTime = UiLastRefreshed.Now,
                            loadTimeMillis = 123L
                        ),
                        action = null
                    ),
                    onRefresh = { },
                    onMoreDetailsClicked = { },
                    onActionLaunched = onActionLaunchedTracker,
                    onErrorSnackbarShown = { }
                )
            }
        }

        assertTrue(launchUrlTracker.launchedUrls.isEmpty())
        assertEquals(0, onActionLaunchedTracker.invocationCount)
    }

    @Test
    fun showUrlActionLaunchesUrl() {
        val launchUrlTracker = LaunchUrlTracker()
        val onActionLaunchedTracker = InvocationCountTracker()
        val incidentsActionLauncher = FakeIncidentsActionLauncher(
            onLaunchUrl = launchUrlTracker
        )
        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals(
                incidentsActionLauncher = incidentsActionLauncher
            ) {
                IncidentsScreen(
                    state = UiIncidentsState(
                        content = UiContent.Populated(
                            isRefreshing = false,
                            items = persistentListOf(),
                            error = null,
                            hasInternetConnectivity = false,
                            lastRefreshTime = UiLastRefreshed.Now,
                            loadTimeMillis = 123L
                        ),
                        action = UiIncidentAction.ShowUrl(url = "https://test.com/path")
                    ),
                    onRefresh = { },
                    onMoreDetailsClicked = { },
                    onActionLaunched = onActionLaunchedTracker,
                    onErrorSnackbarShown = { }
                )
            }
        }

        assertEquals(listOf("https://test.com/path"), launchUrlTracker.launchedUrls)
        assertEquals(1, onActionLaunchedTracker.invocationCount)
    }

    @Composable
    private fun MyBusThemeWithCompositionLocals(
        incidentsActionLauncher: IncidentsActionLauncher = FakeIncidentsActionLauncher(),
        content: @Composable () -> Unit
    ) {
        MyBusTheme {
            CompositionLocalProvider(
                LocalIncidentsActionLauncher provides incidentsActionLauncher,
                content = content
            )
        }
    }

    private class LaunchUrlTracker : (String) -> Unit {

        val launchedUrls get() = _launchedUrls.toList()
        private val _launchedUrls = mutableListOf<String>()

        override fun invoke(p1: String) {
            _launchedUrls += p1
        }
    }

    private class InvocationCountTracker : () -> Unit {

        var invocationCount = 0
            private set

        override fun invoke() {
            invocationCount++
        }
    }
}