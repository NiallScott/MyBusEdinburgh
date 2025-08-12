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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import androidx.activity.ComponentActivity
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onChildren
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import kotlinx.collections.immutable.persistentListOf
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.formatters.LocalDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.formatters.rememberDateTimeFormatter
import uk.org.rivernile.android.bustracker.ui.news.R
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import java.text.SimpleDateFormat
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.time.ExperimentalTime
import kotlin.time.Instant

/**
 * Tests for `ServiceUpdateItem.kt`.
 *
 * @author Niall Scott
 */
class ServiceUpdateItemKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun itemTitleDisplaysGivenTitleString() {
        composeTestRule.setContent {
            MyBusTheme {
                ItemTitle(title = "Item title")
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_TITLE)
            .assertTextEquals("Item title")
    }

    @Test
    fun itemSummaryDisplaysGivenSummaryString() {
        composeTestRule.setContent {
            MyBusTheme {
                ItemSummary(summary = "Item summary")
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_SUMMARY)
            .assertTextEquals("Item summary")
    }

    @Test
    fun itemLastUpdatedDisplaysTimestampWithFormat() {
        val dateFormat = SimpleDateFormat.getDateTimeInstance()
        val instant = Instant.fromEpochMilliseconds(123L)
        val expected = composeTestRule.activity.getString(
            R.string.serviceupdates_item_last_updated,
            dateFormat.format(Date(instant.toEpochMilliseconds()))
        )

        composeTestRule.setContent {
            MyBusTheme {
                CompositionLocalProvider(
                    LocalDateTimeFormatter provides rememberDateTimeFormatter()
                ) {
                    ItemLastUpdated(
                        lastUpdated = instant
                    )
                }
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_LAST_UPDATED)
            .assertTextEquals(expected)
    }


    @Test
    fun itemAffectedServicesHandlesEmptyAffectedServices() {
        composeTestRule.setContent {
            MyBusTheme {
                ItemAffectedServices(affectedServices = persistentListOf())
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_AFFECTED_SERVICES)
            .onChildren()
            .apply {
                assertCountEquals(1)
                this[0].assertTextEquals(
                    composeTestRule
                        .activity
                        .getString(R.string.serviceupdates_item_heading_affected_services)
                )
            }
    }

    @Test
    fun itemAffectedServicesDisplaysAffectedServices() {
        composeTestRule.setContent {
            MyBusTheme {
                ItemAffectedServices(
                    affectedServices = persistentListOf(
                        UiServiceName(serviceName = "1"),
                        UiServiceName(serviceName = "2"),
                        UiServiceName(serviceName = "3")
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_AFFECTED_SERVICES)
            .onChildren()
            .apply {
                assertCountEquals(4)
                this[0].assertTextEquals(
                    composeTestRule
                        .activity
                        .getString(R.string.serviceupdates_item_heading_affected_services)
                )
                this[1].assertTextEquals("1")
                this[2].assertTextEquals("2")
                this[3].assertTextEquals("3")
            }
    }

    @Test
    fun itemMoreDetailsButtonRespondsToClickEvents() {
        val buttonClickedTracker = ButtonClickedTracker()

        composeTestRule.setContent {
            MyBusTheme {
                ItemMoreDetailsButton(
                    onClick = buttonClickedTracker
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_ITEM_BUTTON_MORE_DETAILS)
            .assertTextEquals(
                composeTestRule.activity.getString(R.string.serviceupdates_item_btn_more_details)
            )
            .performClick()

        assertEquals(1, buttonClickedTracker.numberOfInvocations)
    }

    private class ButtonClickedTracker : () -> Unit {

        var numberOfInvocations = 0
            private set

        override fun invoke() {
            numberOfInvocations++
        }
    }
}