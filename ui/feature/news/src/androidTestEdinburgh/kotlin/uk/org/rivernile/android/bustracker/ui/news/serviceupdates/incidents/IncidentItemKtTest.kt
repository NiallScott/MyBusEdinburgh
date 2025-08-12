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

@file:OptIn(ExperimentalTime::class)

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents

import androidx.activity.ComponentActivity
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertHasClickAction
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.formatters.LocalDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.formatters.rememberDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.news.R
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.TEST_TAG_ITEM_AFFECTED_SERVICES
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.TEST_TAG_ITEM_BUTTON_MORE_DETAILS
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.TEST_TAG_ITEM_LAST_UPDATED
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.TEST_TAG_ITEM_SUMMARY
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.TEST_TAG_ITEM_TITLE
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiMoreDetails
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Tests for `IncidentItem.kt`.
 *
 * @author Niall Scott
 */
class IncidentItemKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    private val dateFormat = SimpleDateFormat.getDateTimeInstance()

    @Test
    fun incidentItemDisplaysItemWithNoMoreDetailsButtonAndNoAffectedServices() {
        val lastUpdated = Instant.fromEpochMilliseconds(123L)

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                IncidentItem(
                    item = UiIncident(
                        id = "1",
                        lastUpdated = lastUpdated,
                        title = "Item title",
                        summary = "Item summary",
                        affectedServices = null,
                        moreDetails = null
                    ),
                    onMoreDetailsClicked = { }
                )
            }
        }

        assertTitleIsEqual("Item title")
        assertSummaryIsEqual("Item summary")
        assertLastUpdatedIsEqual(lastUpdated)
        assertAffectedServicesDoesNotExist()
        assertMoreDetailsButtonDoesNotExist()
    }

    @Test
    fun incidentItemDisplaysItemWithMoreDetailsButtonAndRespondsToClicks() {
        val lastUpdated = Instant.fromEpochMilliseconds(123L)
        val buttonClickedTracker = ButtonClickedTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                IncidentItem(
                    item = UiIncident(
                        id = "1",
                        lastUpdated = lastUpdated,
                        title = "Item title",
                        summary = "Item summary",
                        affectedServices = null,
                        moreDetails = UiMoreDetails(url = "https://google.com")
                    ),
                    onMoreDetailsClicked = buttonClickedTracker
                )
            }
        }

        assertTitleIsEqual("Item title")
        assertSummaryIsEqual("Item summary")
        assertLastUpdatedIsEqual(lastUpdated)
        assertAffectedServicesDoesNotExist()
        assertMoreDetailsButtonExistsAndIsClickable()
        assertEquals(1, buttonClickedTracker.numberOfInvocations)
    }

    @Test
    fun incidentItemDisplaysItemWithAffectedServices() {
        val lastUpdated = Instant.fromEpochMilliseconds(123L)

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                IncidentItem(
                    item = UiIncident(
                        id = "1",
                        lastUpdated = lastUpdated,
                        title = "Item title",
                        summary = "Item summary",
                        affectedServices = persistentListOf(
                            UiServiceName(serviceName = "1")
                        ),
                        moreDetails = null
                    ),
                    onMoreDetailsClicked = { }
                )
            }
        }

        assertTitleIsEqual("Item title")
        assertSummaryIsEqual("Item summary")
        assertLastUpdatedIsEqual(lastUpdated)
        assertAffectedServicesExists()
        assertMoreDetailsButtonDoesNotExist()
    }

    @Test
    fun incidentItemDisplaysItemWithAffectedServicesAndMoreDetailsButton() {
        val lastUpdated = Instant.fromEpochMilliseconds(123L)
        val buttonClickedTracker = ButtonClickedTracker()

        composeTestRule.setContent {
            MyBusThemeWithCompositionLocals {
                IncidentItem(
                    item = UiIncident(
                        id = "1",
                        lastUpdated = lastUpdated,
                        title = "Item title",
                        summary = "Item summary",
                        affectedServices = persistentListOf(
                            UiServiceName(serviceName = "1")
                        ),
                        moreDetails = UiMoreDetails(url = "https://google.com")
                    ),
                    onMoreDetailsClicked = buttonClickedTracker
                )
            }
        }

        assertTitleIsEqual("Item title")
        assertSummaryIsEqual("Item summary")
        assertLastUpdatedIsEqual(lastUpdated)
        assertAffectedServicesExists()
        assertMoreDetailsButtonExistsAndIsClickable()
        assertEquals(1, buttonClickedTracker.numberOfInvocations)
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

    @Suppress("SameParameterValue")
    private fun assertTitleIsEqual(expected: String) {
        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_TITLE)
            .assertTextEquals(expected)
    }

    @Suppress("SameParameterValue")
    private fun assertSummaryIsEqual(expected: String) {
        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_SUMMARY)
            .assertTextEquals(expected)
    }

    private fun assertLastUpdatedIsEqual(expectedTime: Instant) {
        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_LAST_UPDATED)
            .assertTextEquals(
                InstrumentationRegistry
                    .getInstrumentation()
                    .targetContext
                    .getString(
                        R.string.serviceupdates_item_last_updated,
                        dateFormat.format(Date(expectedTime.toEpochMilliseconds()))
                    )
            )
    }

    private fun assertAffectedServicesDoesNotExist() {
        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_AFFECTED_SERVICES)
            .assertDoesNotExist()
    }

    private fun assertAffectedServicesExists() {
        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_AFFECTED_SERVICES)
            .assertExists()
    }

    private fun assertMoreDetailsButtonDoesNotExist() {
        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_BUTTON_MORE_DETAILS)
            .assertDoesNotExist()
    }

    private fun assertMoreDetailsButtonExistsAndIsClickable() {
        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_BUTTON_MORE_DETAILS)
            .assertExists()
            .assertHasClickAction()
            .performClick()
    }

    private class ButtonClickedTracker : () -> Unit {

        var numberOfInvocations = 0
            private set

        override fun invoke() {
            numberOfInvocations++
        }
    }
}