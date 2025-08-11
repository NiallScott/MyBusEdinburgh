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

package uk.org.rivernile.android.bustracker.ui.news

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertIsNotSelected
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.filterToOne
import androidx.compose.ui.test.hasParent
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.StateRestorationTester
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.formatters.LocalNumberFormatter
import uk.org.rivernile.android.bustracker.ui.formatters.rememberNumberFormatter
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.TEST_TAG_DIVERSIONS_SCREEN
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversion
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.TEST_TAG_INCIDENTS_SCREEN
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncident
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import java.text.NumberFormat
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `NewsScreen.kt`.
 *
 * @author Niall Scott
 */
class NewsScreenKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun defaultTabIsIncidents() {
        composeTestRule.setContent {
            MyBusTheme {
                NewsScreenWithStateWithDefaultEventHandlers(
                    state = UiState()
                )
            }
        }

        assertIsSelected(TEST_TAG_TAB_INCIDENTS)
        assertIsDisplayed(TEST_TAG_INCIDENTS_SCREEN)
        assertIsNotSelected(TEST_TAG_TAB_DIVERSIONS)
        assertIsNotDisplayed(TEST_TAG_DIVERSIONS_SCREEN)
    }

    @Test
    fun showsDiversionsContentWhenDiversionsTabIsSelected() {
        composeTestRule.setContent {
            MyBusTheme {
                NewsScreenWithStateWithDefaultEventHandlers(
                    state = UiState()
                )
            }
        }

        performClick(TEST_TAG_TAB_DIVERSIONS)

        assertIsNotSelected(TEST_TAG_TAB_INCIDENTS)
        assertIsNotDisplayed(TEST_TAG_INCIDENTS_SCREEN)
        assertIsSelected(TEST_TAG_TAB_DIVERSIONS)
        assertIsDisplayed(TEST_TAG_DIVERSIONS_SCREEN)
    }

    @Test
    fun showsIncidentsContentWhenIncidentsTabIsSelected() {
        composeTestRule.setContent {
            MyBusTheme {
                NewsScreenWithStateWithDefaultEventHandlers(
                    state = UiState()
                )
            }
        }

        performClick(TEST_TAG_TAB_DIVERSIONS)
        performClick(TEST_TAG_TAB_INCIDENTS)

        assertIsSelected(TEST_TAG_TAB_INCIDENTS)
        assertIsDisplayed(TEST_TAG_INCIDENTS_SCREEN)
        assertIsNotSelected(TEST_TAG_TAB_DIVERSIONS)
        assertIsNotDisplayed(TEST_TAG_DIVERSIONS_SCREEN)
    }

    @Test
    fun selectedTabIsRestored() {
        val restorationTester = StateRestorationTester(composeTestRule).apply {
            setContent {
                MyBusTheme {
                    NewsScreenWithStateWithDefaultEventHandlers(
                        state = UiState()
                    )
                }
            }
        }

        performClick(TEST_TAG_TAB_DIVERSIONS)
        restorationTester.emulateSavedInstanceStateRestore()

        assertIsNotSelected(TEST_TAG_TAB_INCIDENTS)
        assertIsNotDisplayed(TEST_TAG_INCIDENTS_SCREEN)
        assertIsSelected(TEST_TAG_TAB_DIVERSIONS)
        assertIsDisplayed(TEST_TAG_DIVERSIONS_SCREEN)
    }

    @Test
    fun tabCountBadgeIsNotShownWhenCountIsNull() {
        composeTestRule.setContent {
            MyBusTheme {
                NewsScreenWithStateWithDefaultEventHandlers(
                    state = UiState()
                )
            }
        }

        composeTestRule
            .onAllNodesWithTag(testTag = TEST_TAG_TAB_BADGE_COUNT, useUnmergedTree = true)
            .assertCountEquals(0)
    }

    @Test
    fun tabCountBadgeIsShownWithCorrectCountWhenCountIsNotNull() {
        val numberFormat = NumberFormat.getInstance()
        composeTestRule.setContent {
            MyBusTheme {
                NewsScreenWithStateWithDefaultEventHandlers(
                    state = UiState(
                        tabBadges = UiTabBadges(
                            incidentsCount = 1,
                            diversionsCount = 1024
                        )
                    )
                )
            }
        }

        composeTestRule
            .onAllNodesWithTag(testTag = TEST_TAG_TAB_BADGE_COUNT, useUnmergedTree = true)
            .apply {
                filterToOne(hasParent(hasTestTag(TEST_TAG_TAB_INCIDENTS)))
                    .assertTextEquals(numberFormat.format(1))
                filterToOne(hasParent(hasTestTag(TEST_TAG_TAB_DIVERSIONS)))
                    .assertTextEquals(numberFormat.format(1024))
            }
    }

    @Test
    fun clickingOnRefreshMenuItemExecutesRefreshLambda() {
        var refreshCount = 0
        composeTestRule.setContent {
            MyBusTheme {
                NewsScreenWithStateWithDefaultEventHandlers(
                    state = UiState(
                        actionButtons = UiActionButtons(
                            refresh = UiActionButton.Refresh(
                                isEnabled = true,
                                isRefreshing = false
                            )
                        )
                    ),
                    onRefresh = { refreshCount++ }
                )
            }
        }

        openActionBarOverflowOrOptionsMenu(context)
        onView(withText(context.getString(R.string.news_fragment_option_menu_refresh)))
            .perform(click())

        assertEquals(1, refreshCount)
    }

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Composable
    private fun NewsScreenWithStateWithDefaultEventHandlers(
        state: UiState,
        modifier: Modifier = Modifier,
        onRefresh: () -> Unit = { },
        onIncidentMoreDetailsClicked: (UiIncident) -> Unit = { },
        onIncidentActionLaunched: () -> Unit = { },
        onDiversionMoreDetailsClicked: (UiDiversion) -> Unit = { },
        onDiversionActionLaunched: () -> Unit = { },
        onErrorSnackbarShown: (Long) -> Unit = { }
    ) {
        CompositionLocalProvider(
            LocalNumberFormatter provides rememberNumberFormatter()
        ) {
            NewsScreenWithState(
                state = state,
                modifier = modifier,
                onRefresh = onRefresh,
                onIncidentMoreDetailsClicked = onIncidentMoreDetailsClicked,
                onIncidentActionLaunched = onIncidentActionLaunched,
                onDiversionMoreDetailsClicked = onDiversionMoreDetailsClicked,
                onDiversionActionLaunched = onDiversionActionLaunched,
                onErrorSnackbarShown = onErrorSnackbarShown
            )
        }
    }

    private fun performClick(tag: String) {
        composeTestRule
            .onNodeWithTag(tag)
            .assertHasClickAction()
            .performClick()
    }

    private fun assertIsSelected(tag: String) {
        composeTestRule
            .onNodeWithTag(tag)
            .assertIsSelected()
    }

    private fun assertIsNotSelected(tag: String) {
        composeTestRule
            .onNodeWithTag(tag)
            .assertIsNotSelected()
    }

    private fun assertIsDisplayed(tag: String) {
        composeTestRule
            .onNodeWithTag(tag)
            .assertIsDisplayed()
    }

    private fun assertIsNotDisplayed(tag: String) {
        composeTestRule
            .onNodeWithTag(tag)
            .assertIsNotDisplayed()
    }
}