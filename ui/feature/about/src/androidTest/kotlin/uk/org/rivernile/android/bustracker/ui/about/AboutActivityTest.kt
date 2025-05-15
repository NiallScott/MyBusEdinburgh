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

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [AboutActivity] and its Compose components.
 *
 * @author Niall Scott
 */
class AboutActivityTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun clickingOnNavigateUpIconNavigatesUp() {
        val navigateUpTracker = BasicEventTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutScreenWithState(
                    state = UiState(items = persistentListOf()),
                    onNavigateUp = navigateUpTracker,
                    onItemClicked = { },
                    onCreditsDialogDismissed = { },
                    onOpenSourceLicenceDialogDismissed = { },
                    onActionLaunched = { }
                )
            }
        }

        composeTestRule
            .onNodeWithContentDescription(
                composeTestRule.activity.getString(Rcore.string.navigate_up)
            )
            .performClick()

        assertEquals(1, navigateUpTracker.numberOfInvocations)
    }

    @Test
    fun clickingOnAboutItemCallsItemClickedListener() {
        val onItemClickedTracker = ItemClickedTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutScreenWithState(
                    state = UiState(
                        items = persistentListOf(UiAboutItem.OneLineItem.PrivacyPolicy)
                    ),
                    onNavigateUp = { },
                    onItemClicked = onItemClickedTracker,
                    onCreditsDialogDismissed = { },
                    onOpenSourceLicenceDialogDismissed = { },
                    onActionLaunched = { }
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(R.string.about_privacy_policy))
            .performClick()

        assertEquals(listOf(UiAboutItem.OneLineItem.PrivacyPolicy), onItemClickedTracker.itemClicks)
    }

    @Test
    fun creditsDialogIsShownWhenSetInState() {
        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutScreenWithState(
                    state = UiState(
                        items = persistentListOf(),
                        isCreditsShown = true
                    ),
                    onNavigateUp = { },
                    onItemClicked = { },
                    onCreditsDialogDismissed = { },
                    onOpenSourceLicenceDialogDismissed = { },
                    onActionLaunched = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_CREDITS_DIALOG)
            .assertIsDisplayed()
    }

    @Test
    fun clickingOnCloseInCreditsDialogCallsCreditsDialogDismissedCallback() {
        val onCreditsDialogDismissedTracker = BasicEventTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutScreenWithState(
                    state = UiState(
                        items = persistentListOf(),
                        isCreditsShown = true
                    ),
                    onNavigateUp = { },
                    onItemClicked = { },
                    onCreditsDialogDismissed = onCreditsDialogDismissedTracker,
                    onOpenSourceLicenceDialogDismissed = { },
                    onActionLaunched = { }
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(Rcore.string.close))
            .performClick()

        assertEquals(1, onCreditsDialogDismissedTracker.numberOfInvocations)
    }

    @Test
    fun openSourceLicenceDialogIsShownWhenSetInState() {
        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutScreenWithState(
                    state = UiState(
                        items = persistentListOf(),
                        isOpenSourceLicencesShown = true
                    ),
                    onNavigateUp = { },
                    onItemClicked = { },
                    onCreditsDialogDismissed = { },
                    onOpenSourceLicenceDialogDismissed = { },
                    onActionLaunched = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_OPEN_SOURCE_LICENCE_DIALOG)
            .assertIsDisplayed()
    }

    @Test
    fun clickingOnCloseInOpenSourceLicenceDialogCallsOpenSourceLicenceDialogDismissedCallback() {
        val onOpenSourceLicenceDialogDismissedTracker = BasicEventTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutScreenWithState(
                    state = UiState(
                        items = persistentListOf(),
                        isOpenSourceLicencesShown = true
                    ),
                    onNavigateUp = { },
                    onItemClicked = { },
                    onCreditsDialogDismissed = { },
                    onOpenSourceLicenceDialogDismissed = onOpenSourceLicenceDialogDismissedTracker,
                    onActionLaunched = { }
                )
            }
        }

        composeTestRule
            .onNodeWithText(composeTestRule.activity.getString(Rcore.string.close))
            .performClick()

        assertEquals(1, onOpenSourceLicenceDialogDismissedTracker.numberOfInvocations)
    }

    @Test
    fun showStoreListingActionCallsActionLauncherAndActionLaunchedCallback() {
        val onActionLaunchedTracker = BasicEventTracker()
        val actionLauncher = FakeAboutActionLauncher()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals(
                aboutActionLauncher = actionLauncher
            ) {
                AboutScreenWithState(
                    state = UiState(
                        items = persistentListOf(),
                        action = UiAction.ShowStoreListing
                    ),
                    onNavigateUp = { },
                    onItemClicked = { },
                    onCreditsDialogDismissed = { },
                    onOpenSourceLicenceDialogDismissed = { },
                    onActionLaunched = onActionLaunchedTracker
                )
            }
        }

        assertEquals(1, actionLauncher.launchStoreListingInvocationCount)
        assertEquals(1, onActionLaunchedTracker.numberOfInvocations)
    }

    @Test
    fun showAuthorWebsiteActionCallsActionLauncherAndActionLaunchedCallback() {
        val onActionLaunchedTracker = BasicEventTracker()
        val actionLauncher = FakeAboutActionLauncher()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals(
                aboutActionLauncher = actionLauncher
            ) {
                AboutScreenWithState(
                    state = UiState(
                        items = persistentListOf(),
                        action = UiAction.ShowAuthorWebsite
                    ),
                    onNavigateUp = { },
                    onItemClicked = { },
                    onCreditsDialogDismissed = { },
                    onOpenSourceLicenceDialogDismissed = { },
                    onActionLaunched = onActionLaunchedTracker
                )
            }
        }

        assertEquals(1, actionLauncher.launchAuthorWebsiteInvocationCount)
        assertEquals(1, onActionLaunchedTracker.numberOfInvocations)
    }

    @Test
    fun showAppWebsiteActionCallsActionLauncherAndActionLaunchedCallback() {
        val onActionLaunchedTracker = BasicEventTracker()
        val actionLauncher = FakeAboutActionLauncher()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals(
                aboutActionLauncher = actionLauncher
            ) {
                AboutScreenWithState(
                    state = UiState(
                        items = persistentListOf(),
                        action = UiAction.ShowAppWebsite
                    ),
                    onNavigateUp = { },
                    onItemClicked = { },
                    onCreditsDialogDismissed = { },
                    onOpenSourceLicenceDialogDismissed = { },
                    onActionLaunched = onActionLaunchedTracker
                )
            }
        }

        assertEquals(1, actionLauncher.launchAppWebsiteInvocationCount)
        assertEquals(1, onActionLaunchedTracker.numberOfInvocations)
    }

    @Test
    fun showAppTwitterActionCallsActionLauncherAndActionLaunchedCallback() {
        val onActionLaunchedTracker = BasicEventTracker()
        val actionLauncher = FakeAboutActionLauncher()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals(
                aboutActionLauncher = actionLauncher
            ) {
                AboutScreenWithState(
                    state = UiState(
                        items = persistentListOf(),
                        action = UiAction.ShowAppTwitter
                    ),
                    onNavigateUp = { },
                    onItemClicked = { },
                    onCreditsDialogDismissed = { },
                    onOpenSourceLicenceDialogDismissed = { },
                    onActionLaunched = onActionLaunchedTracker
                )
            }
        }

        assertEquals(1, actionLauncher.launchAppTwitterInvocationCount)
        assertEquals(1, onActionLaunchedTracker.numberOfInvocations)
    }

    @Test
    fun showPrivacyPolicyActionCallsActionLauncherAndActionLaunchedCallback() {
        val onActionLaunchedTracker = BasicEventTracker()
        val actionLauncher = FakeAboutActionLauncher()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals(
                aboutActionLauncher = actionLauncher
            ) {
                AboutScreenWithState(
                    state = UiState(
                        items = persistentListOf(),
                        action = UiAction.ShowPrivacyPolicy
                    ),
                    onNavigateUp = { },
                    onItemClicked = { },
                    onCreditsDialogDismissed = { },
                    onOpenSourceLicenceDialogDismissed = { },
                    onActionLaunched = onActionLaunchedTracker
                )
            }
        }

        assertEquals(1, actionLauncher.launchPrivacyPolicyInvocationCount)
        assertEquals(1, onActionLaunchedTracker.numberOfInvocations)
    }

    @Composable
    private fun MyBusThemeWithCompositionLocals(
        aboutActionLauncher: AboutActionLauncher = FakeAboutActionLauncher(),
        content: @Composable () -> Unit
    ) {
        MyBusTheme {
            CompositionLocalProvider(
                LocalAboutActionLauncher provides aboutActionLauncher,
                content = content
            )
        }
    }

    private class BasicEventTracker : () -> Unit {

        var numberOfInvocations = 0
            private set

        override fun invoke() {
            numberOfInvocations++
        }
    }

    private class ItemClickedTracker : (UiAboutItem) -> Unit {

        val itemClicks get() = _itemClicks.toList()
        private val _itemClicks = mutableListOf<UiAboutItem>()

        override fun invoke(p1: UiAboutItem) {
            _itemClicks += p1
        }
    }

    private class FakeAboutActionLauncher : AboutActionLauncher {

        var launchStoreListingInvocationCount = 0
            private set

        var launchAuthorWebsiteInvocationCount = 0
            private set

        var launchAppWebsiteInvocationCount = 0
            private set

        var launchAppTwitterInvocationCount = 0
            private set

        var launchPrivacyPolicyInvocationCount = 0
            private set

        override fun launchStoreListing() {
            launchStoreListingInvocationCount++
        }

        override fun launchAuthorWebsite() {
            launchAuthorWebsiteInvocationCount++
        }

        override fun launchAppWebsite() {
            launchAppWebsiteInvocationCount++
        }

        override fun launchAppTwitter() {
            launchAppTwitterInvocationCount++
        }

        override fun launchPrivacyPolicy() {
            launchPrivacyPolicyInvocationCount++
        }
    }
}