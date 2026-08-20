/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.search

import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopSearchResult
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.busstops.StopSearchResult
import uk.org.rivernile.android.bustracker.core.domain.FakeServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Tests for `UiStopSearchResult.kt`.
 *
 * @author Niall Scott
 */
class UiStopSearchResultKtTest {

    @Test
    fun toUiStopSearchResultsWithEmptyListMapsToEmptyList() {
        val result = emptyList<StopSearchResult>().toUiStopSearchResults(
            serviceColours = null,
            dropdownMenus = null,
            stopNameComparator = naturalOrder(),
            serviceNameComparator = naturalOrder()
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun toUiStopSearchResultsMapsNonOptionalFields() {
        val stopSearchResults = listOf(
            FakeStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = null
            )
        )
        val expected = listOf(
            UiStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = null,
                orientation = StopOrientation.NORTH,
                dropdownMenu = UiStopSearchResultDropdownMenu()
            )
        )

        val result = stopSearchResults.toUiStopSearchResults(
            serviceColours = null,
            dropdownMenus = null,
            stopNameComparator = naturalOrder(),
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiStopSearchResultsMapsWithEmptyServices() {
        val stopSearchResults = listOf(
            FakeStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = emptyList()
            )
        )
        val expected = listOf(
            UiStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = null,
                orientation = StopOrientation.NORTH,
                dropdownMenu = UiStopSearchResultDropdownMenu()
            )
        )

        val result = stopSearchResults.toUiStopSearchResults(
            serviceColours = null,
            dropdownMenus = null,
            stopNameComparator = naturalOrder(),
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiStopSearchResultsMapsColoursWithNullColoursMap() {
        val stopSearchResults = listOf(
            FakeStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = listOf(service(1), service(2), service(3))
            )
        )
        val expected = listOf(
            UiStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = persistentListOf(
                    UiServiceName(
                        serviceName = "1",
                        colours = null
                    ),
                    UiServiceName(
                        serviceName = "2",
                        colours = null
                    ),
                    UiServiceName(
                        serviceName = "3",
                        colours = null
                    )
                ),
                orientation = StopOrientation.NORTH,
                dropdownMenu = UiStopSearchResultDropdownMenu()
            )
        )

        val result = stopSearchResults.toUiStopSearchResults(
            serviceColours = null,
            dropdownMenus = null,
            stopNameComparator = naturalOrder(),
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiStopSearchResultsMapsColoursWithEmptyColoursMap() {
        val stopSearchResults = listOf(
            FakeStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = listOf(service(1), service(2), service(3))
            )
        )
        val expected = listOf(
            UiStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = persistentListOf(
                    UiServiceName(
                        serviceName = "1",
                        colours = null
                    ),
                    UiServiceName(
                        serviceName = "2",
                        colours = null
                    ),
                    UiServiceName(
                        serviceName = "3",
                        colours = null
                    )
                ),
                orientation = StopOrientation.NORTH,
                dropdownMenu = UiStopSearchResultDropdownMenu()
            )
        )

        val result = stopSearchResults.toUiStopSearchResults(
            serviceColours = emptyMap(),
            dropdownMenus = null,
            stopNameComparator = naturalOrder(),
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiStopSearchResultsMapsColoursWithPopulatedColoursMap() {
        val stopSearchResults = listOf(
            FakeStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = listOf(service(1), service(2), service(3))
            )
        )
        val expected = listOf(
            UiStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = persistentListOf(
                    UiServiceName(
                        serviceName = "1",
                        colours = UiServiceColours(
                            backgroundColour = 100,
                            textColour = 200
                        )
                    ),
                    UiServiceName(
                        serviceName = "2",
                        colours = null
                    ),
                    UiServiceName(
                        serviceName = "3",
                        colours = UiServiceColours(
                            backgroundColour = 300,
                            textColour = 400
                        )
                    )
                ),
                orientation = StopOrientation.NORTH,
                dropdownMenu = UiStopSearchResultDropdownMenu()
            )
        )

        val result = stopSearchResults.toUiStopSearchResults(
            serviceColours = mapOf(
                service(1) to ServiceColours(
                    colourPrimary = 100,
                    colourOnPrimary = 200
                ),
                service(3) to ServiceColours(
                    colourPrimary = 300,
                    colourOnPrimary = 400
                )
            ),
            dropdownMenus = null,
            stopNameComparator = naturalOrder(),
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiStopSearchResultsMapsServicesAsSorted() {
        val stopSearchResults = listOf(
            FakeStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = listOf(service(1), service(3), service(2))
            )
        )
        val expected = listOf(
            UiStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = persistentListOf(
                    UiServiceName(
                        serviceName = "1",
                        colours = UiServiceColours(
                            backgroundColour = 100,
                            textColour = 200
                        )
                    ),
                    UiServiceName(
                        serviceName = "2",
                        colours = null
                    ),
                    UiServiceName(
                        serviceName = "3",
                        colours = UiServiceColours(
                            backgroundColour = 300,
                            textColour = 400
                        )
                    )
                ),
                orientation = StopOrientation.NORTH,
                dropdownMenu = UiStopSearchResultDropdownMenu()
            )
        )

        val result = stopSearchResults.toUiStopSearchResults(
            serviceColours = mapOf(
                service(1) to ServiceColours(
                    colourPrimary = 100,
                    colourOnPrimary = 200
                ),
                service(3) to ServiceColours(
                    colourPrimary = 300,
                    colourOnPrimary = 400
                )
            ),
            dropdownMenus = null,
            stopNameComparator = naturalOrder(),
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiStopSearchResultsUsesDefaultDropdownMenuWhenDropdownMenusIsEmptyMap() {
        val stopSearchResults = listOf(
            FakeStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = null
            )
        )
        val expected = listOf(
            UiStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = null,
                orientation = StopOrientation.NORTH,
                dropdownMenu = UiStopSearchResultDropdownMenu()
            )
        )

        val result = stopSearchResults.toUiStopSearchResults(
            serviceColours = null,
            dropdownMenus = emptyMap(),
            stopNameComparator = naturalOrder(),
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiStopSearchResultsUsesDefaultDropdownMenuWhenDropdownMenusContainedUnrecognisedItem() {
        val stopSearchResults = listOf(
            FakeStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = null
            )
        )
        val expected = listOf(
            UiStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = null,
                orientation = StopOrientation.NORTH,
                dropdownMenu = UiStopSearchResultDropdownMenu()
            )
        )

        val result = stopSearchResults.toUiStopSearchResults(
            serviceColours = null,
            dropdownMenus = mapOf(
                "987654".toNaptanStopIdentifier() to UiStopSearchResultDropdownMenu(
                    isStopMapItemShown = true
                )
            ),
            stopNameComparator = naturalOrder(),
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiStopSearchResultsIncludesDropdownMenuWhenMapContainsExpectedStop() {
        val stopSearchResults = listOf(
            FakeStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = null
            )
        )
        val expected = listOf(
            UiStopSearchResult(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = null,
                orientation = StopOrientation.NORTH,
                dropdownMenu = UiStopSearchResultDropdownMenu(
                    isStopMapItemShown = true
                )
            )
        )

        val result = stopSearchResults.toUiStopSearchResults(
            serviceColours = null,
            dropdownMenus = mapOf(
                "123456".toNaptanStopIdentifier() to UiStopSearchResultDropdownMenu(
                    isStopMapItemShown = true
                )
            ),
            stopNameComparator = naturalOrder(),
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiStopSearchResultsSortsStopsByName() {
        val stopSearchResults = listOf(
            FakeStopSearchResult(
                stopIdentifier = "2".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name 2",
                    locality = "Locality 2"
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = null
            ),
            FakeStopSearchResult(
                stopIdentifier = "3".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name 3",
                    locality = "Locality 3"
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = null
            ),
            FakeStopSearchResult(
                stopIdentifier = "1".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name 1",
                    locality = "Locality 1"
                ),
                orientation = StopOrientation.NORTH,
                serviceListing = null
            )
        )
        val expected = listOf(
            UiStopSearchResult(
                stopIdentifier = "1".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name 1",
                    locality = "Locality 1"
                ),
                orientation = StopOrientation.NORTH,
                services = null,
                dropdownMenu = UiStopSearchResultDropdownMenu()
            ),
            UiStopSearchResult(
                stopIdentifier = "2".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name 2",
                    locality = "Locality 2"
                ),
                orientation = StopOrientation.NORTH,
                services = null,
                dropdownMenu = UiStopSearchResultDropdownMenu()
            ),
            UiStopSearchResult(
                stopIdentifier = "3".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name 3",
                    locality = "Locality 3"
                ),
                orientation = StopOrientation.NORTH,
                services = null,
                dropdownMenu = UiStopSearchResultDropdownMenu()
            )
        )

        val result = stopSearchResults.toUiStopSearchResults(
            serviceColours = null,
            dropdownMenus = null,
            stopNameComparator = naturalOrder(),
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    private fun service(id: Int): ServiceDescriptor {
        return FakeServiceDescriptor(
            serviceName = id.toString(),
            operatorCode = "TEST$id"
        )
    }
}
