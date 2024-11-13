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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents

import kotlinx.datetime.Instant
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.updates.IncidentServiceUpdate
import uk.org.rivernile.android.bustracker.core.updates.PlannedServiceUpdate
import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdate
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.UiMoreDetails
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for `UiIncident.kt`.
 *
 * @author Niall Scott
 */
class UiIncidentKtTest {

    @Test
    fun toUiIncidentsOrNullReturnsNullWhenListIsEmpty() {
        val result = emptyList<ServiceUpdate>()
            .toUiIncidentsOrNull(
                coloursForServices = emptyMap(),
                serviceNamesComparator = naturalOrder()
            )

        assertNull(result)
    }

    @Test
    fun toIncidentsOrNullReturnsNullWhenContainsNonIncidentItem() {
        val result = listOf<ServiceUpdate>(
            PlannedServiceUpdate(
                id = "id",
                lastUpdated = Instant.fromEpochMilliseconds(123L),
                title = "Title",
                summary = "Summary",
                affectedServices = null,
                url = null
            )
        ).toUiIncidentsOrNull(
            coloursForServices = emptyMap(),
            serviceNamesComparator = naturalOrder()
        )

        assertNull(result)
    }

    @Test
    fun toIncidentsOrNullReturnsListWithItemWhenItemIsIncident() {
        val expected = listOf(
            createUiIncident(
                moreDetails = UiMoreDetails(url = "https://google.com")
            )
        )

        val result = listOf(
            createIncidentServiceUpdate(
                affectedServices = setOf("1", "2", "3"),
                url = "https://google.com"
            )
        ).toUiIncidentsOrNull(
            coloursForServices = coloursForServices,
            serviceNamesComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toIncidentsOrNullReturnsListWithItemWhenItemIsIncidentAndUrlIsNull() {
        val expected = listOf(
            createUiIncident(
                moreDetails = null
            )
        )

        val result = listOf(
            createIncidentServiceUpdate(
                affectedServices = setOf("1", "2", "3"),
                url = null
            )
        ).toUiIncidentsOrNull(
            coloursForServices = coloursForServices,
            serviceNamesComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toIncidentsOrNullReturnsListWithItemWhenItemIsIncidentAndUrlIsEmpty() {
        val expected = listOf(
            createUiIncident(
                moreDetails = null
            )
        )

        val result = listOf(
            createIncidentServiceUpdate(
                affectedServices = setOf("1", "2", "3"),
                url = ""
            )
        ).toUiIncidentsOrNull(
            coloursForServices = coloursForServices,
            serviceNamesComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    private fun createIncidentServiceUpdate(
        affectedServices: Set<String>?,
        url: String?
    ): ServiceUpdate {
        return IncidentServiceUpdate(
            id = "id",
            lastUpdated = Instant.fromEpochMilliseconds(123L),
            title = "Title",
            summary = "Summary",
            affectedServices = affectedServices,
            url = url
        )
    }

    private fun createUiIncident(
        moreDetails: UiMoreDetails?
    ): UiIncident {
        return UiIncident(
            id = "id",
            lastUpdated = Instant.fromEpochMilliseconds(123L),
            title = "Title",
            summary = "Summary",
            affectedServices = listOf(
                UiServiceName(
                    serviceName = "1",
                    colours = UiServiceColours(
                        backgroundColour = 1,
                        textColour = 2
                    )
                ),
                UiServiceName(
                    serviceName = "2",
                    colours = null
                ),
                UiServiceName(
                    serviceName = "3",
                    colours = UiServiceColours(
                        backgroundColour = 3,
                        textColour = 4
                    )
                )
            ),
            moreDetails = moreDetails
        )
    }

    private val coloursForServices get() = mapOf(
        "1" to ServiceColours(
            primaryColour = 1,
            colourOnPrimary = 2
        ),
        "3" to ServiceColours(
            primaryColour = 3,
            colourOnPrimary = 4
        )
    )
}