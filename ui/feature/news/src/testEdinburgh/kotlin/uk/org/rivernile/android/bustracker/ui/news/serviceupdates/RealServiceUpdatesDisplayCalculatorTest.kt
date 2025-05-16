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

import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.datetime.Instant
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.UiDiversion
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncident
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [RealServiceUpdatesDisplayCalculator].
 *
 * @author Niall Scott
 */
class RealServiceUpdatesDisplayCalculatorTest {

    @Test
    fun diversionWithOldStateInProgressAndNewStateInProgressReturnsInProgress() {
        val calculator = createCalculator()

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = ServiceUpdatesDisplay.InProgress,
            result = UiServiceUpdatesResult.InProgress
        )

        assertEquals(ServiceUpdatesDisplay.InProgress, result)
    }

    @Test
    fun diversionWithOldStateInProgressAndNewStateErrorReturnsError() {
        val calculator = createCalculator()
        val expected = createServiceUpdatesDisplayError(error = UiError.SERVER)

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = ServiceUpdatesDisplay.InProgress,
            result = createUiServiceUpdatesResultError(error = UiError.SERVER)
        )

        assertEquals(expected, result)
    }

    @Test
    fun diversionWithOldStateInProgressAndNewStateSuccessReturnsPopulated() {
        val calculator = createCalculator()
        val diversions = persistentListOf(createUiDiversion())
        val expected = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = diversions,
            error = null,
            loadTimeMillis = 123L
        )

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = ServiceUpdatesDisplay.InProgress,
            result = createUiServiceUpdatesResultSuccessForDiversions(diversions)
        )

        assertEquals(expected, result)
    }

    @Test
    fun diversionWithOldStateInProgressAndNewStateSuccessWithNullItemsReturnsError() {
        val calculator = createCalculator()
        val expected = createServiceUpdatesDisplayError(error = UiError.EMPTY)

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = ServiceUpdatesDisplay.InProgress,
            result = createUiServiceUpdatesResultSuccessForDiversions(diversions = null)
        )

        assertEquals(expected, result)
    }

    @Test
    fun diversionWithOldStateInProgressAndNewStateSuccessWithEmptyItemsReturnsError() {
        val calculator = createCalculator()
        val expected = createServiceUpdatesDisplayError(error = UiError.EMPTY)

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = ServiceUpdatesDisplay.InProgress,
            result = createUiServiceUpdatesResultSuccessForDiversions(
                diversions = persistentListOf()
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun diversionWithOldStateErrorAndNewStateInProgressReturnsInProgress() {
        val calculator = createCalculator()

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = createServiceUpdatesDisplayError(error = UiError.SERVER),
            result = UiServiceUpdatesResult.InProgress
        )

        assertEquals(ServiceUpdatesDisplay.InProgress, result)
    }

    @Test
    fun diversionWithOldStateErrorAndNewStateErrorReturnsError() {
        val calculator = createCalculator()
        val expected = createServiceUpdatesDisplayError(error = UiError.IO)

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = createServiceUpdatesDisplayError(error = UiError.SERVER),
            result = createUiServiceUpdatesResultError(error = UiError.IO)
        )

        assertEquals(expected, result)
    }

    @Test
    fun diversionWithOldStateErrorAndNewStateSuccessReturnsPopulated() {
        val calculator = createCalculator()
        val diversions = persistentListOf(createUiDiversion())
        val expected = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = diversions,
            error = null,
            loadTimeMillis = 123L
        )

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = createServiceUpdatesDisplayError(error = UiError.SERVER),
            result = createUiServiceUpdatesResultSuccessForDiversions(diversions)
        )

        assertEquals(expected, result)
    }

    @Test
    fun diversionWithOldStateErrorAndNewStateSuccessWithNullItemsReturnsError() {
        val calculator = createCalculator()
        val expected = createServiceUpdatesDisplayError(error = UiError.EMPTY)

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = createServiceUpdatesDisplayError(error = UiError.SERVER),
            result = createUiServiceUpdatesResultSuccessForDiversions(diversions = null)
        )

        assertEquals(expected, result)
    }

    @Test
    fun diversionWithOldStateErrorAndNewStateSuccessWithEmptyItemsReturnsError() {
        val calculator = createCalculator()
        val expected = createServiceUpdatesDisplayError(error = UiError.EMPTY)

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = createServiceUpdatesDisplayError(error = UiError.SERVER),
            result = createUiServiceUpdatesResultSuccessForDiversions(
                diversions = persistentListOf()
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun diversionWithOldStatePopulatedAndNewStateInProgressReturnsPopulatedWithRefreshingFlagSet() {
        val calculator = createCalculator()
        val diversions = listOf(createUiDiversion())
        val previousDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = diversions,
            error = null,
            loadTimeMillis = 123L
        )
        val expected = ServiceUpdatesDisplay.Populated(
            isRefreshing = true,
            items = diversions,
            error = null,
            loadTimeMillis = 123L
        )

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = previousDisplay,
            result = UiServiceUpdatesResult.InProgress
        )

        assertEquals(expected, result)
    }

    @Test
    fun diversionWithOldStatePopulatedAndNewStateErrorReturnsPopulatedWithErrorFieldSet() {
        val calculator = createCalculator()
        val diversions = listOf(createUiDiversion())
        val previousDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = diversions,
            error = null,
            loadTimeMillis = 123L
        )
        val expected = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = diversions,
            error = UiError.SERVER,
            loadTimeMillis = 123L
        )

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = previousDisplay,
            result = UiServiceUpdatesResult.Error(error = UiError.SERVER)
        )

        assertEquals(expected, result)
    }

    @Test
    fun diversionWithOldStatePopulatedAndNewStateSuccessReturnsPopulated() {
        val calculator = createCalculator()
        val diversions1 = persistentListOf(createUiDiversion())
        val diversions2 = persistentListOf(createUiDiversion(), createUiDiversion())
        val previousDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = diversions1,
            error = null,
            loadTimeMillis = 123L
        )
        val expected = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = diversions2,
            error = null,
            loadTimeMillis = 456L
        )

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = previousDisplay,
            result = createUiServiceUpdatesResultSuccessForDiversions(
                diversions = diversions2,
                loadTimeMillis = 456L
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun diversionWithOldStatePopulatedAndNewStateSuccessWithNullItemsReturnsError() {
        val calculator = createCalculator()
        val diversions = listOf(createUiDiversion())
        val previousDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = diversions,
            error = null,
            loadTimeMillis = 123L
        )
        val expected = createServiceUpdatesDisplayError(error = UiError.EMPTY)

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = previousDisplay,
            result = createUiServiceUpdatesResultSuccessForDiversions(diversions = null)
        )

        assertEquals(expected, result)
    }

    @Test
    fun diversionWithOldStatePopulatedAndNewStateSuccessWithEmptyItemsReturnsError() {
        val calculator = createCalculator()
        val diversions = listOf(createUiDiversion())
        val previousDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = diversions,
            error = null,
            loadTimeMillis = 123L
        )
        val expected = createServiceUpdatesDisplayError(error = UiError.EMPTY)

        val result = calculator.calculateServiceUpdatesDisplayForDiversions(
            previousDisplay = previousDisplay,
            result = createUiServiceUpdatesResultSuccessForDiversions(
                diversions = persistentListOf()
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun incidentWithOldStateInProgressAndNewStateInProgressReturnsInProgress() {
        val calculator = createCalculator()

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = ServiceUpdatesDisplay.InProgress,
            result = UiServiceUpdatesResult.InProgress
        )

        assertEquals(ServiceUpdatesDisplay.InProgress, result)
    }

    @Test
    fun incidentWithOldStateInProgressAndNewStateErrorReturnsError() {
        val calculator = createCalculator()
        val expected = createServiceUpdatesDisplayError(error = UiError.SERVER)

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = ServiceUpdatesDisplay.InProgress,
            result = createUiServiceUpdatesResultError(error = UiError.SERVER)
        )

        assertEquals(expected, result)
    }

    @Test
    fun incidentWithOldStateInProgressAndNewStateSuccessReturnsPopulated() {
        val calculator = createCalculator()
        val incidents = persistentListOf(createUiIncident())
        val expected = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = incidents,
            error = null,
            loadTimeMillis = 123L
        )

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = ServiceUpdatesDisplay.InProgress,
            result = createUiServiceUpdatesResultSuccessForIncidents(incidents)
        )

        assertEquals(expected, result)
    }

    @Test
    fun incidentWithOldStateInProgressAndNewStateSuccessWithNullItemsReturnsError() {
        val calculator = createCalculator()
        val expected = createServiceUpdatesDisplayError(error = UiError.EMPTY)

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = ServiceUpdatesDisplay.InProgress,
            result = createUiServiceUpdatesResultSuccessForIncidents(incidents = null)
        )

        assertEquals(expected, result)
    }

    @Test
    fun incidentWithOldStateInProgressAndNewStateSuccessWithEmptyItemsReturnsError() {
        val calculator = createCalculator()
        val expected = createServiceUpdatesDisplayError(error = UiError.EMPTY)

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = ServiceUpdatesDisplay.InProgress,
            result = createUiServiceUpdatesResultSuccessForIncidents(incidents = persistentListOf())
        )

        assertEquals(expected, result)
    }

    @Test
    fun incidentWithOldStateErrorAndNewStateInProgressReturnsInProgress() {
        val calculator = createCalculator()

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = createServiceUpdatesDisplayError(error = UiError.SERVER),
            result = UiServiceUpdatesResult.InProgress
        )

        assertEquals(ServiceUpdatesDisplay.InProgress, result)
    }

    @Test
    fun incidentWithOldStateErrorAndNewStateErrorReturnsError() {
        val calculator = createCalculator()
        val expected = createServiceUpdatesDisplayError(error = UiError.IO)

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = createServiceUpdatesDisplayError(error = UiError.SERVER),
            result = createUiServiceUpdatesResultError(error = UiError.IO)
        )

        assertEquals(expected, result)
    }

    @Test
    fun incidentWithOldStateErrorAndNewStateSuccessReturnsPopulated() {
        val calculator = createCalculator()
        val incidents = persistentListOf(createUiIncident())
        val expected = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = incidents,
            error = null,
            loadTimeMillis = 123L
        )

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = createServiceUpdatesDisplayError(error = UiError.SERVER),
            result = createUiServiceUpdatesResultSuccessForIncidents(incidents)
        )

        assertEquals(expected, result)
    }

    @Test
    fun incidentWithOldStateErrorAndNewStateSuccessWithNullItemsReturnsError() {
        val calculator = createCalculator()
        val expected = createServiceUpdatesDisplayError(error = UiError.EMPTY)

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = createServiceUpdatesDisplayError(error = UiError.SERVER),
            result = createUiServiceUpdatesResultSuccessForIncidents(incidents = null)
        )

        assertEquals(expected, result)
    }

    @Test
    fun incidentWithOldStateErrorAndNewStateSuccessWithEmptyItemsReturnsError() {
        val calculator = createCalculator()
        val expected = createServiceUpdatesDisplayError(error = UiError.EMPTY)

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = createServiceUpdatesDisplayError(error = UiError.SERVER),
            result = createUiServiceUpdatesResultSuccessForIncidents(incidents = persistentListOf())
        )

        assertEquals(expected, result)
    }

    @Test
    fun incidentWithOldStatePopulatedAndNewStateInProgressReturnsPopulatedWithRefreshingFlagSet() {
        val calculator = createCalculator()
        val incidents = listOf(createUiIncident())
        val previousDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = incidents,
            error = null,
            loadTimeMillis = 123L
        )
        val expected = ServiceUpdatesDisplay.Populated(
            isRefreshing = true,
            items = incidents,
            error = null,
            loadTimeMillis = 123L
        )

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = previousDisplay,
            result = UiServiceUpdatesResult.InProgress
        )

        assertEquals(expected, result)
    }

    @Test
    fun incidentWithOldStatePopulatedAndNewStateErrorReturnsPopulatedWithErrorFieldSet() {
        val calculator = createCalculator()
        val incidents = listOf(createUiIncident())
        val previousDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = incidents,
            error = null,
            loadTimeMillis = 123L
        )
        val expected = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = incidents,
            error = UiError.SERVER,
            loadTimeMillis = 123L
        )

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = previousDisplay,
            result = UiServiceUpdatesResult.Error(error = UiError.SERVER)
        )

        assertEquals(expected, result)
    }

    @Test
    fun incidentWithOldStatePopulatedAndNewStateSuccessReturnsPopulated() {
        val calculator = createCalculator()
        val incidents1 = persistentListOf(createUiIncident())
        val incidents2 = persistentListOf(createUiIncident(), createUiIncident())
        val previousDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = incidents1,
            error = null,
            loadTimeMillis = 123L
        )
        val expected = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = incidents2,
            error = null,
            loadTimeMillis = 456L
        )

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = previousDisplay,
            result = createUiServiceUpdatesResultSuccessForIncidents(
                incidents = incidents2,
                loadTimeMillis = 456L
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun incidentWithOldStatePopulatedAndNewStateSuccessWithNullItemsReturnsError() {
        val calculator = createCalculator()
        val incidents = listOf(createUiIncident())
        val previousDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = incidents,
            error = null,
            loadTimeMillis = 123L
        )
        val expected = createServiceUpdatesDisplayError(error = UiError.EMPTY)

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = previousDisplay,
            result = createUiServiceUpdatesResultSuccessForIncidents(incidents = null)
        )

        assertEquals(expected, result)
    }

    @Test
    fun incidentWithOldStatePopulatedAndNewStateSuccessWithEmptyItemsReturnsError() {
        val calculator = createCalculator()
        val incidents = listOf(createUiIncident())
        val previousDisplay = ServiceUpdatesDisplay.Populated(
            isRefreshing = false,
            items = incidents,
            error = null,
            loadTimeMillis = 123L
        )
        val expected = createServiceUpdatesDisplayError(error = UiError.EMPTY)

        val result = calculator.calculateServiceUpdatesDisplayForIncidents(
            previousDisplay = previousDisplay,
            result = createUiServiceUpdatesResultSuccessForIncidents(incidents = persistentListOf())
        )

        assertEquals(expected, result)
    }

    private fun createCalculator() = RealServiceUpdatesDisplayCalculator()

    private fun createUiServiceUpdatesResultError(error: UiError) =
        UiServiceUpdatesResult.Error(error = error)

    private fun createUiServiceUpdatesResultSuccessForDiversions(
        diversions: ImmutableList<UiDiversion>? = null,
        loadTimeMillis: Long = 123L
    ) = UiServiceUpdatesResult.Success(
        serviceUpdates = diversions,
        loadTimeMillis = loadTimeMillis
    )

    private fun createUiServiceUpdatesResultSuccessForIncidents(
        incidents: ImmutableList<UiIncident>? = null,
        loadTimeMillis: Long = 123L
    ) = UiServiceUpdatesResult.Success(
        serviceUpdates = incidents,
        loadTimeMillis = loadTimeMillis
    )

    private fun createServiceUpdatesDisplayError(error: UiError) =
        ServiceUpdatesDisplay.Error(
            error = error
        )

    private fun createUiDiversion(): UiDiversion {
        return UiDiversion(
            id = "id",
            lastUpdated = Instant.fromEpochMilliseconds(123L),
            title = "Title",
            summary = "Summary",
            affectedServices = null,
            moreDetails = null
        )
    }

    private fun createUiIncident(): UiIncident {
        return UiIncident(
            id = "id",
            lastUpdated = Instant.fromEpochMilliseconds(123L),
            title = "Title",
            summary = "Summary",
            affectedServices = null,
            moreDetails = null
        )
    }
}