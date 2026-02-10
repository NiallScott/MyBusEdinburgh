/*
 * Copyright (C) 2024 - 2026 Niall 'Rivernile' Scott
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
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertIsNotEnabled
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onParent
import androidx.compose.ui.test.performClick
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.formatters.LocalDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.formatters.rememberDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for [AboutItem].
 *
 * @author Niall Scott
 */
class AboutItemKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun creditsAboutItemDisplaysCorrectText() {
        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = UiAboutItem.OneLineItem.Credits,
                    onItemClicked = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_TITLE)
            .assertTextEquals(composeTestRule.activity.getString(R.string.about_credits))
    }

    @Test
    fun creditsAboutItemHandlesItemClicked() {
        val itemClickedTracker = ItemClickedTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = UiAboutItem.OneLineItem.Credits,
                    onItemClicked = itemClickedTracker
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_TITLE)
            .apply {
                assertHasClickAction()
                performClick()
            }

        assertEquals(
            listOf(UiAboutItem.OneLineItem.Credits),
            itemClickedTracker.invocations
        )
    }

    @Test
    fun openSourceLicencesAboutItemDisplaysCorrectText() {
        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = UiAboutItem.OneLineItem.OpenSourceLicences,
                    onItemClicked = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_TITLE)
            .assertTextEquals(composeTestRule.activity.getString(R.string.about_open_source))
    }

    @Test
    fun openSourceLicencesAboutItemHandlesItemClicked() {
        val itemClickedTracker = ItemClickedTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = UiAboutItem.OneLineItem.OpenSourceLicences,
                    onItemClicked = itemClickedTracker
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_TITLE)
            .apply {
                assertHasClickAction()
                performClick()
            }

        assertEquals(
            listOf(UiAboutItem.OneLineItem.OpenSourceLicences),
            itemClickedTracker.invocations
        )
    }

    @Test
    fun privacyPolicyAboutItemDisplaysCorrectText() {
        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = UiAboutItem.OneLineItem.PrivacyPolicy,
                    onItemClicked = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_TITLE)
            .assertTextEquals(composeTestRule.activity.getString(R.string.about_privacy_policy))
    }

    @Test
    fun privacyPolicyAboutItemHandlesItemClicked() {
        val itemClickedTracker = ItemClickedTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = UiAboutItem.OneLineItem.PrivacyPolicy,
                    onItemClicked = itemClickedTracker
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_TITLE)
            .apply {
                assertHasClickAction()
                performClick()
            }

        assertEquals(
            listOf(UiAboutItem.OneLineItem.PrivacyPolicy),
            itemClickedTracker.invocations
        )
    }

    @Test
    fun appVersionAboutItemDisplaysCorrectText() {
        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = UiAboutItem.TwoLinesItem.AppVersion(
                        versionName = "1.2.3",
                        versionCode = 4
                    ),
                    onItemClicked = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_TITLE,
                useUnmergedTree = true
            )
            .assertTextEquals(composeTestRule.activity.getString(R.string.about_version))
        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_CAPTION,
                useUnmergedTree = true
            )
            .assertTextEquals("1.2.3 (#4)")
    }

    @Test
    fun appVersionAboutItemHandlesItemClicked() {
        val item = UiAboutItem.TwoLinesItem.AppVersion(
            versionName = "1.2.3",
            versionCode = 4
        )
        val itemClickedTracker = ItemClickedTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = item,
                    onItemClicked = itemClickedTracker
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_TITLE,
                useUnmergedTree = true
            )
            .onParent()
            .apply {
                assertHasClickAction()
                performClick()
            }

        assertEquals(
            listOf(item),
            itemClickedTracker.invocations
        )
    }

    @Test
    fun authorAboutItemDisplaysCorrectText() {
        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = UiAboutItem.TwoLinesItem.Author,
                    onItemClicked = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_TITLE,
                useUnmergedTree = true
            )
            .assertTextEquals(composeTestRule.activity.getString(R.string.about_author))
        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_CAPTION,
                useUnmergedTree = true
            )
            .assertTextEquals(composeTestRule.activity.getString(R.string.app_author))
    }

    @Test
    fun authorAboutItemHandlesItemClicked() {
        val itemClickedTracker = ItemClickedTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = UiAboutItem.TwoLinesItem.Author,
                    onItemClicked = itemClickedTracker
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_TITLE,
                useUnmergedTree = true
            )
            .onParent()
            .apply {
                assertHasClickAction()
                performClick()
            }

        assertEquals(
            listOf(UiAboutItem.TwoLinesItem.Author),
            itemClickedTracker.invocations
        )
    }

    @Test
    fun databaseVersionAboutItemDisplaysCorrectTextWhenNotLoaded() {
        val item = UiAboutItem.TwoLinesItem.DatabaseVersion(null)

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = item,
                    onItemClicked = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_TITLE,
                useUnmergedTree = true
            )
            .assertTextEquals(composeTestRule.activity.getString(R.string.about_database_version))
        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_CAPTION,
                useUnmergedTree = true
            )
            .assertTextEquals(
                composeTestRule.activity.getString(R.string.about_database_version_loading)
            )
    }

    @Test
    fun databaseVersionAboutItemDisplaysCorrectTextWhenLoaded() {
        val item = UiAboutItem.TwoLinesItem.DatabaseVersion(Date(123L))
        val dateTimeFormat = SimpleDateFormat.getDateTimeInstance()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals(
                dateTimeFormat = dateTimeFormat
            ) {
                AboutItem(
                    item = item,
                    onItemClicked = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_TITLE,
                useUnmergedTree = true
            )
            .assertTextEquals(composeTestRule.activity.getString(R.string.about_database_version))
        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_CAPTION,
                useUnmergedTree = true
            )
            .assertTextEquals("123 (${dateTimeFormat.format(Date(123L))})")
    }

    @Test
    fun databaseVersionAboutItemDoesNotHandleClicks() {
        val item = UiAboutItem.TwoLinesItem.DatabaseVersion(Date(123L))
        val itemClickedTracker = ItemClickedTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = item,
                    onItemClicked = itemClickedTracker
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_TITLE,
                useUnmergedTree = true
            )
            .onParent()
            .apply {
                assertIsNotEnabled()
                performClick()
            }

        assertTrue(itemClickedTracker.invocations.isEmpty())
    }

    @Test
    fun blueskyAboutItemDisplaysCorrectText() {
        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = UiAboutItem.TwoLinesItem.Bluesky,
                    onItemClicked = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_TITLE,
                useUnmergedTree = true
            )
            .assertTextEquals(composeTestRule.activity.getString(R.string.about_bluesky))
        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_CAPTION,
                useUnmergedTree = true
            )
            .assertTextEquals(composeTestRule.activity.getString(R.string.app_bluesky))
    }

    @Test
    fun blueskyAboutItemHandlesItemClicked() {
        val itemClickedTracker = ItemClickedTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = UiAboutItem.TwoLinesItem.Bluesky,
                    onItemClicked = itemClickedTracker
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_TITLE,
                useUnmergedTree = true
            )
            .onParent()
            .apply {
                assertHasClickAction()
                performClick()
            }

        assertEquals(
            listOf(UiAboutItem.TwoLinesItem.Bluesky),
            itemClickedTracker.invocations
        )
    }

    @Test
    fun websiteAboutItemDisplaysCorrectText() {
        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = UiAboutItem.TwoLinesItem.Website,
                    onItemClicked = { }
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_TITLE,
                useUnmergedTree = true
            )
            .assertTextEquals(composeTestRule.activity.getString(R.string.about_website))
        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_CAPTION,
                useUnmergedTree = true
            )
            .assertTextEquals(composeTestRule.activity.getString(R.string.app_website))
    }

    @Test
    fun websiteAboutItemHandlesItemClicked() {
        val itemClickedTracker = ItemClickedTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                AboutItem(
                    item = UiAboutItem.TwoLinesItem.Website,
                    onItemClicked = itemClickedTracker
                )
            }
        }

        composeTestRule
            .onNodeWithTag(
                testTag = TEST_TAG_TITLE,
                useUnmergedTree = true
            )
            .onParent()
            .apply {
                assertHasClickAction()
                performClick()
            }

        assertEquals(
            listOf(UiAboutItem.TwoLinesItem.Website),
            itemClickedTracker.invocations
        )
    }

    @Composable
    private fun MyBusThemeWithCompositionLocals(
        dateTimeFormat: DateFormat = rememberDateTimeFormatter(),
        content: @Composable () -> Unit
    ) {
        MyBusTheme {
            CompositionLocalProvider(
                LocalDateTimeFormatter provides dateTimeFormat,
                content = content
            )
        }
    }

    private class ItemClickedTracker : (UiAboutItem) -> Unit {

        val invocations get() = _invocations.toList()
        private val _invocations = mutableListOf<UiAboutItem>()

        override fun invoke(p1: UiAboutItem) {
            _invocations += p1
        }
    }
}
