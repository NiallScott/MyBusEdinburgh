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

import kotlinx.collections.immutable.persistentListOf
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for `UiContent.kt`.
 *
 * @author Niall Scott
 */
class UiContentKtTest {

    @Test
    fun toUiContentInProgressReturnsUiContentInProgress() {
        assertEquals(UiContent.InProgress, toUiContentInProgress())
    }

    @Test
    fun serviceUpdatesDisplayPopulatedToUiContentPopulatedHandlesNullError() {
        val serviceUpdatesDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = true,
            items = persistentListOf(),
            error = null,
            successLoadTimeMillis = 123L,
            lastLoadTimeMillis = 987L
        )
        val expected = UiContent.Populated(
            isRefreshing = true,
            items = persistentListOf(),
            error = null,
            hasInternetConnectivity = true,
            lastRefreshTime = UiLastRefreshed.Minutes(minutes = 5),
            loadTimeMillis = 987L
        )

        val result = serviceUpdatesDisplay.toUiContentPopulated(
            hasInternetConnectivity = true,
            lastErrorTimestampShown = 0L,
            lastRefreshTime = UiLastRefreshed.Minutes(minutes = 5)
        )

        assertEquals(expected, result)
    }

    @Test
    fun serviceUpdatesDisplayPopulatedToUiContentPopulatedWithErrorAndOldLastShownTimestamp() {
        val serviceUpdatesDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = true,
            items = persistentListOf(),
            error = UiError.NO_CONNECTIVITY,
            successLoadTimeMillis = 123L,
            lastLoadTimeMillis = 987L
        )
        val expected = UiContent.Populated(
            isRefreshing = true,
            items = persistentListOf(),
            error = UiError.NO_CONNECTIVITY,
            hasInternetConnectivity = true,
            lastRefreshTime = UiLastRefreshed.Minutes(minutes = 5),
            loadTimeMillis = 987L
        )

        val result = serviceUpdatesDisplay.toUiContentPopulated(
            hasInternetConnectivity = true,
            lastErrorTimestampShown = 986L,
            lastRefreshTime = UiLastRefreshed.Minutes(minutes = 5)
        )

        assertEquals(expected, result)
    }

    @Test
    fun serviceUpdatesDisplayPopulatedToUiContentPopulatedWithErrorAndCurrentLastShownTimestamp() {
        val serviceUpdatesDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = true,
            items = persistentListOf(),
            error = UiError.NO_CONNECTIVITY,
            successLoadTimeMillis = 123L,
            lastLoadTimeMillis = 987L
        )
        val expected = UiContent.Populated(
            isRefreshing = true,
            items = persistentListOf(),
            error = null,
            hasInternetConnectivity = true,
            lastRefreshTime = UiLastRefreshed.Minutes(minutes = 5),
            loadTimeMillis = 987L
        )

        val result = serviceUpdatesDisplay.toUiContentPopulated(
            hasInternetConnectivity = true,
            lastErrorTimestampShown = 987L,
            lastRefreshTime = UiLastRefreshed.Minutes(minutes = 5)
        )

        assertEquals(expected, result)
    }

    @Test
    fun serviceUpdatesDisplayPopulatedToUiContentPopulatedWithErrorAndFutureLastShownTimestamp() {
        val serviceUpdatesDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = true,
            items = persistentListOf(),
            error = UiError.NO_CONNECTIVITY,
            successLoadTimeMillis = 123L,
            lastLoadTimeMillis = 987L
        )
        val expected = UiContent.Populated(
            isRefreshing = true,
            items = persistentListOf(),
            error = null,
            hasInternetConnectivity = true,
            lastRefreshTime = UiLastRefreshed.Minutes(minutes = 5),
            loadTimeMillis = 987L
        )

        val result = serviceUpdatesDisplay.toUiContentPopulated(
            hasInternetConnectivity = true,
            lastErrorTimestampShown = 988L,
            lastRefreshTime = UiLastRefreshed.Minutes(minutes = 5)
        )

        assertEquals(expected, result)
    }

    @Test
    fun serviceUpdatesDisplayErrorToUiContentErrorReturnUiContentError() {
        assertEquals(
            UiContent.Error(
                error = UiError.NO_CONNECTIVITY
            ),
            ServiceUpdatesDisplay.Error(
                error = UiError.NO_CONNECTIVITY
            ).toUiContentError()
        )
    }
}