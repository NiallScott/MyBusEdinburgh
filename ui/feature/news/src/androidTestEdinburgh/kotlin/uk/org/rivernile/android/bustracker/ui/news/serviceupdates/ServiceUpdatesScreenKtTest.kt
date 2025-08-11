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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import androidx.activity.ComponentActivity
import androidx.compose.foundation.lazy.LazyItemScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.test.ExperimentalTestApi
import androidx.compose.ui.test.assertContentDescriptionEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsNotDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.hasAnyDescendant
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onRoot
import androidx.compose.ui.test.performTouchInput
import androidx.compose.ui.test.swipeDown
import androidx.test.platform.app.InstrumentationRegistry
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import kotlinx.datetime.Instant
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.news.R
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncident
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme
import kotlin.test.Test
import kotlin.test.assertEquals

private const val SNACKBAR_TIMEOUT = 5000L

/**
 * Tests for `ServiceUpdatesScreen.kt`.
 *
 * @author Niall Scott
 */
class ServiceUpdatesScreenKtTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<ComponentActivity>()

    @Test
    fun showsEmptyProgressWhenContentIsInProgress() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.InProgress
                )
            }
        }

        assertIsDisplayed(TEST_TAG_EMPTY_PROGRESS)
        assertIsNotDisplayed(TEST_TAG_POPULATED_CONTENT)
        assertIsNotDisplayed(TEST_TAG_INLINE_ERROR)
    }

    @Test
    fun showsPopulatedContentWhenContentIsPopulated() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Populated(
                        isRefreshing = false,
                        items = persistentListOf(
                            createUiIncident()
                        ),
                        error = null,
                        hasInternetConnectivity = true,
                        lastRefreshTime = UiLastRefreshed.Now,
                        loadTimeMillis = 123L
                    )
                )
            }
        }

        assertIsNotDisplayed(TEST_TAG_EMPTY_PROGRESS)
        assertIsDisplayed(TEST_TAG_POPULATED_CONTENT)
        assertIsNotDisplayed(TEST_TAG_INLINE_ERROR)
    }

    @Test
    fun showsInlineErrorWhenContentIsError() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Error(
                        error = UiError.EMPTY
                    )
                )
            }
        }

        assertIsNotDisplayed(TEST_TAG_EMPTY_PROGRESS)
        assertIsNotDisplayed(TEST_TAG_POPULATED_CONTENT)
        assertIsDisplayed(TEST_TAG_INLINE_ERROR)
        assertIsNotDisplayed(TEST_TAG_ERROR_SNACKBAR)
    }

    @Test
    fun showsNoConnectivityInlineErrorWhenContentIsNoConnectivityError() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Error(
                        error = UiError.NO_CONNECTIVITY
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_INLINE_ERROR_TEXT)
            .assertTextEquals(context.getString(R.string.serviceupdates_error_noconnectivity))
        assertIsNotDisplayed(TEST_TAG_ERROR_SNACKBAR)
    }

    @Test
    fun showsEmptyInlineErrorWhenContentIsEmptyError() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Error(
                        error = UiError.EMPTY
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_INLINE_ERROR_TEXT)
            .assertTextEquals(context.getString(R.string.serviceupdates_error_empty))
        assertIsNotDisplayed(TEST_TAG_ERROR_SNACKBAR)
    }

    @Test
    fun showsIoInlineErrorWhenContentIsIoError() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Error(
                        error = UiError.IO
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_INLINE_ERROR_TEXT)
            .assertTextEquals(context.getString(R.string.serviceupdates_error_io))
        assertIsNotDisplayed(TEST_TAG_ERROR_SNACKBAR)
    }

    @Test
    fun showsServerInlineErrorWhenContentIsServerError() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Error(
                        error = UiError.SERVER
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_INLINE_ERROR_TEXT)
            .assertTextEquals(context.getString(R.string.serviceupdates_error_server))
        assertIsNotDisplayed(TEST_TAG_ERROR_SNACKBAR)
    }

    @Test
    fun lastRefreshedTextIsNeverWhenLastRefreshedStateIsNever() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Populated(
                        isRefreshing = false,
                        items = persistentListOf(
                            createUiIncident()
                        ),
                        error = null,
                        hasInternetConnectivity = true,
                        lastRefreshTime = UiLastRefreshed.Never,
                        loadTimeMillis = 123L
                    )
                )
            }
        }
        val timeComponent = context.getString(R.string.serviceupdates_last_updated_never)
        val expected = context.getString(R.string.serviceupdates_last_updated, timeComponent)

        composeTestRule
            .onNodeWithTag(TEST_TAG_LAST_REFRESHED_TEXT)
            .assertTextEquals(expected)
    }

    @Test
    fun lastRefreshedTextIsNowWhenLastRefreshedStateIsNow() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Populated(
                        isRefreshing = false,
                        items = persistentListOf(
                            createUiIncident()
                        ),
                        error = null,
                        hasInternetConnectivity = true,
                        lastRefreshTime = UiLastRefreshed.Now,
                        loadTimeMillis = 123L
                    )
                )
            }
        }
        val timeComponent = context.getString(R.string.serviceupdates_last_updated_now)
        val expected = context.getString(R.string.serviceupdates_last_updated, timeComponent)

        composeTestRule
            .onNodeWithTag(TEST_TAG_LAST_REFRESHED_TEXT)
            .assertTextEquals(expected)
    }

    @Test
    fun lastRefreshedTextIsNumberOfMinutesAgoWhenLastRefreshedStateIsMinutes() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Populated(
                        isRefreshing = false,
                        items = persistentListOf(
                            createUiIncident()
                        ),
                        error = null,
                        hasInternetConnectivity = true,
                        lastRefreshTime = UiLastRefreshed.Minutes(minutes = 5),
                        loadTimeMillis = 123L
                    )
                )
            }
        }
        val timeComponent = context.resources.getQuantityString(
            R.plurals.serviceupdates_last_updated_minsago,
            5,
            5
        )
        val expected = context.getString(R.string.serviceupdates_last_updated, timeComponent)

        composeTestRule
            .onNodeWithTag(TEST_TAG_LAST_REFRESHED_TEXT)
            .assertTextEquals(expected)
    }

    @Test
    fun lastRefreshedTextIsMoreThanOneHourWhenLastRefreshedStateIsMoreThanOneHour() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Populated(
                        isRefreshing = false,
                        items = persistentListOf(
                            createUiIncident()
                        ),
                        error = null,
                        hasInternetConnectivity = true,
                        lastRefreshTime = UiLastRefreshed.MoreThanOneHour,
                        loadTimeMillis = 123L
                    )
                )
            }
        }
        val timeComponent = context.getString(R.string.serviceupdates_last_updated_greaterthanhour)
        val expected = context.getString(R.string.serviceupdates_last_updated, timeComponent)

        composeTestRule
            .onNodeWithTag(TEST_TAG_LAST_REFRESHED_TEXT)
            .assertTextEquals(expected)
    }

    @Test
    fun doesNotShowNoConnectivityIconWhenHasInternet() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Populated(
                        isRefreshing = false,
                        items = persistentListOf(
                            createUiIncident()
                        ),
                        error = null,
                        hasInternetConnectivity = true,
                        lastRefreshTime = UiLastRefreshed.Now,
                        loadTimeMillis = 123L
                    )
                )
            }
        }

        assertIsNotDisplayed(TEST_TAG_NO_CONNECTIVITY_ICON)
    }

    @Test
    fun showsNoConnectivityIconWhenHasNoInternet() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Populated(
                        isRefreshing = false,
                        items = persistentListOf(
                            createUiIncident()
                        ),
                        error = null,
                        hasInternetConnectivity = false,
                        lastRefreshTime = UiLastRefreshed.Now,
                        loadTimeMillis = 123L
                    )
                )
            }
        }

        composeTestRule
            .onNodeWithTag(TEST_TAG_NO_CONNECTIVITY_ICON)
            .apply {
                assertIsDisplayed()
                assertContentDescriptionEquals(
                    context.getString(R.string.serviceupdates_no_connectivity_content_description)
                )
            }
    }

    @Test
    fun showsNoConnectivitySnackbarErrorWhenPopulatedHasNoConnectivityError() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Populated(
                        isRefreshing = false,
                        items = persistentListOf(
                            createUiIncident()
                        ),
                        error = UiError.NO_CONNECTIVITY,
                        hasInternetConnectivity = false,
                        lastRefreshTime = UiLastRefreshed.Now,
                        loadTimeMillis = 123L
                    )
                )
            }
        }
        val expectedText = context.getString(R.string.serviceupdates_error_noconnectivity)

        composeTestRule
            .onNode(hasSnackbarWithExpectedText(expectedText))
            .assertIsDisplayed()
    }

    @Test
    fun showsEmptySnackbarErrorWhenPopulatedHasEmptyError() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Populated(
                        isRefreshing = false,
                        items = persistentListOf(
                            createUiIncident()
                        ),
                        error = UiError.EMPTY,
                        hasInternetConnectivity = false,
                        lastRefreshTime = UiLastRefreshed.Now,
                        loadTimeMillis = 123L
                    )
                )
            }
        }
        val expectedText = context.getString(R.string.serviceupdates_error_empty)

        composeTestRule
            .onNode(hasSnackbarWithExpectedText(expectedText))
            .assertIsDisplayed()
    }

    @Test
    fun showsIoSnackbarErrorWhenPopulatedHasIoError() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Populated(
                        isRefreshing = false,
                        items = persistentListOf(
                            createUiIncident()
                        ),
                        error = UiError.IO,
                        hasInternetConnectivity = false,
                        lastRefreshTime = UiLastRefreshed.Now,
                        loadTimeMillis = 123L
                    )
                )
            }
        }
        val expectedText = context.getString(R.string.serviceupdates_error_io)

        composeTestRule
            .onNode(hasSnackbarWithExpectedText(expectedText))
            .assertIsDisplayed()
    }

    @Test
    fun showsServerSnackbarErrorWhenPopulatedHasServerError() {
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Populated(
                        isRefreshing = false,
                        items = persistentListOf(
                            createUiIncident()
                        ),
                        error = UiError.SERVER,
                        hasInternetConnectivity = false,
                        lastRefreshTime = UiLastRefreshed.Now,
                        loadTimeMillis = 123L
                    )
                )
            }
        }
        val expectedText = context.getString(R.string.serviceupdates_error_server)

        composeTestRule
            .onNode(hasSnackbarWithExpectedText(expectedText))
            .assertIsDisplayed()
    }

    @OptIn(ExperimentalTestApi::class)
    @Test
    fun callsOnErrorSnackbarShownWhenSnackbarIsDismissed() = runTest {
        var errorSnackbarShownCount = 0
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Populated(
                        isRefreshing = false,
                        items = persistentListOf(
                            createUiIncident()
                        ),
                        error = UiError.SERVER,
                        hasInternetConnectivity = false,
                        lastRefreshTime = UiLastRefreshed.Now,
                        loadTimeMillis = 123L
                    ),
                    onErrorSnackbarShown = { errorSnackbarShownCount++ }
                )
            }
        }
        val expectedText = context.getString(R.string.serviceupdates_error_server)

        composeTestRule.apply {
            waitUntilDoesNotExist(
                matcher = hasSnackbarWithExpectedText(expectedText),
                timeoutMillis = SNACKBAR_TIMEOUT
            )
        }

        assertEquals(1, errorSnackbarShownCount)
    }

    @Test
    fun swipeDownGestureOnScreenCausesPullToRefresh() = runTest {
        var refreshInvocationCount = 0
        composeTestRule.setContent {
            MyBusTheme {
                ServiceUpdatesScreenWithDefaultEventHandlers(
                    content = UiContent.Error(
                        error = UiError.EMPTY
                    ),
                    onRefresh = { refreshInvocationCount++ }
                )
            }
        }

        composeTestRule
            .onRoot()
            .performTouchInput {
                swipeDown()
            }
        composeTestRule.awaitIdle()

        assertEquals(1, refreshInvocationCount)
    }

    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext

    @Composable
    private fun <T : UiServiceUpdate> ServiceUpdatesScreenWithDefaultEventHandlers(
        content: UiContent<T>,
        modifier: Modifier = Modifier,
        onRefresh: () -> Unit = { },
        onErrorSnackbarShown: (Long) -> Unit = { },
        itemContent: @Composable LazyItemScope.(item: T, modifier: Modifier) -> Unit = { _, _ -> }
    ) {
        ServiceUpdatesScreen(
            content = content,
            modifier = modifier,
            onRefresh = onRefresh,
            onErrorSnackbarShown = onErrorSnackbarShown,
            itemContent = itemContent
        )
    }

    private fun hasSnackbarWithExpectedText(expectedText: String) =
        hasTestTag(TEST_TAG_ERROR_SNACKBAR) and hasAnyDescendant(hasText(expectedText))

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

    private fun createUiIncident(id: Int = 1): UiIncident {
        return UiIncident(
            id = id.toString(),
            lastUpdated = Instant.fromEpochMilliseconds(id.toLong()),
            title = "Title $id",
            summary = "Summary $id",
            affectedServices = null,
            moreDetails = null
        )
    }
}