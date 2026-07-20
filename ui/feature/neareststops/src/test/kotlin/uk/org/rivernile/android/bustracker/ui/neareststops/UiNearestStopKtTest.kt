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

package uk.org.rivernile.android.bustracker.ui.neareststops

import kotlinx.collections.immutable.persistentListOf
import uk.org.rivernile.android.bustracker.core.busstops.FakeStopName
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
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
 * Tests for `UiNearestStop.kt`.
 *
 * @author Niall Scott
 */
class UiNearestStopKtTest {

    @Test
    fun toUiNearestStopsWithEmptyListMapsToEmptyList() {
        val result = emptyList<NearestStop>().toUiNearestStops(
            serviceColours = null,
            dropdownMenus = null,
            serviceNameComparator = naturalOrder()
        )

        assertTrue(result.isEmpty())
    }

    @Test
    fun toUiNearestStopsMapsNonOptionalFields() {
        val nearestStops = listOf(
            NearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                distanceMeters = 123,
                orientation = StopOrientation.NORTH,
                serviceListing = null
            )
        )
        val expected = listOf(
            UiNearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = null,
                orientation = StopOrientation.NORTH,
                distanceMeters = 123,
                dropdownMenu = UiNearestStopDropdownMenu()
            )
        )

        val result = nearestStops.toUiNearestStops(
            serviceColours = null,
            dropdownMenus = null,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiNearestStopsMapsWithEmptyServices() {
        val nearestStops = listOf(
            NearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                distanceMeters = 123,
                orientation = StopOrientation.NORTH,
                serviceListing = emptyList()
            )
        )
        val expected = listOf(
            UiNearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = null,
                orientation = StopOrientation.NORTH,
                distanceMeters = 123,
                dropdownMenu = UiNearestStopDropdownMenu()
            )
        )

        val result = nearestStops.toUiNearestStops(
            serviceColours = null,
            dropdownMenus = null,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiNearestStopsMapsColoursWithNullColoursMap() {
        val nearestStops = listOf(
            NearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                distanceMeters = 123,
                orientation = StopOrientation.NORTH,
                serviceListing = listOf(service(1), service(2), service(3))
            )
        )
        val expected = listOf(
            UiNearestStop(
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
                distanceMeters = 123,
                dropdownMenu = UiNearestStopDropdownMenu()
            )
        )

        val result = nearestStops.toUiNearestStops(
            serviceColours = null,
            dropdownMenus = null,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiNearestStopsMapsColoursWithEmptyColoursMap() {
        val nearestStops = listOf(
            NearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                distanceMeters = 123,
                orientation = StopOrientation.NORTH,
                serviceListing = listOf(service(1), service(2), service(3))
            )
        )
        val expected = listOf(
            UiNearestStop(
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
                distanceMeters = 123,
                dropdownMenu = UiNearestStopDropdownMenu()
            )
        )

        val result = nearestStops.toUiNearestStops(
            serviceColours = emptyMap(),
            dropdownMenus = null,
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiNearestStopsMapsColoursWithPopulatedColoursMap() {
        val nearestStops = listOf(
            NearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                distanceMeters = 123,
                orientation = StopOrientation.NORTH,
                serviceListing = listOf(service(1), service(2), service(3))
            )
        )
        val expected = listOf(
            UiNearestStop(
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
                distanceMeters = 123,
                dropdownMenu = UiNearestStopDropdownMenu()
            )
        )

        val result = nearestStops.toUiNearestStops(
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
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiNearestStopsMapsServicesAsSorted() {
        val nearestStops = listOf(
            NearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                distanceMeters = 123,
                orientation = StopOrientation.NORTH,
                serviceListing = listOf(service(1), service(3), service(2))
            )
        )
        val expected = listOf(
            UiNearestStop(
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
                distanceMeters = 123,
                dropdownMenu = UiNearestStopDropdownMenu()
            )
        )

        val result = nearestStops.toUiNearestStops(
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
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiNearestStopsUsesDefaultDropdownMenuWhenDropdownMenusIsEmptyMap() {
        val nearestStops = listOf(
            NearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                distanceMeters = 123,
                orientation = StopOrientation.NORTH,
                serviceListing = null
            )
        )
        val expected = listOf(
            UiNearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = null,
                orientation = StopOrientation.NORTH,
                distanceMeters = 123,
                dropdownMenu = UiNearestStopDropdownMenu()
            )
        )

        val result = nearestStops.toUiNearestStops(
            serviceColours = null,
            dropdownMenus = emptyMap(),
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiNearestStopsUsesDefaultDropdownMenuWhenDropdownMenusContainedUnrecognisedItem() {
        val nearestStops = listOf(
            NearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                distanceMeters = 123,
                orientation = StopOrientation.NORTH,
                serviceListing = null
            )
        )
        val expected = listOf(
            UiNearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = null,
                orientation = StopOrientation.NORTH,
                distanceMeters = 123,
                dropdownMenu = UiNearestStopDropdownMenu()
            )
        )

        val result = nearestStops.toUiNearestStops(
            serviceColours = null,
            dropdownMenus = mapOf(
                "987654".toNaptanStopIdentifier() to UiNearestStopDropdownMenu(
                    isStopMapItemShown = true
                )
            ),
            serviceNameComparator = naturalOrder()
        )

        assertEquals(expected, result)
    }

    @Test
    fun toUiNearestStopsIncludesDropdownMenuWhenMapContainsExpectedStop() {
        val nearestStops = listOf(
            NearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = FakeStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                distanceMeters = 123,
                orientation = StopOrientation.NORTH,
                serviceListing = null
            )
        )
        val expected = listOf(
            UiNearestStop(
                stopIdentifier = "123456".toNaptanStopIdentifier(),
                stopName = UiStopName(
                    name = "Stop name",
                    locality = "Locality"
                ),
                services = null,
                orientation = StopOrientation.NORTH,
                distanceMeters = 123,
                dropdownMenu = UiNearestStopDropdownMenu(
                    isStopMapItemShown = true
                )
            )
        )

        val result = nearestStops.toUiNearestStops(
            serviceColours = null,
            dropdownMenus = mapOf(
                "123456".toNaptanStopIdentifier() to UiNearestStopDropdownMenu(
                    isStopMapItemShown = true
                )
            ),
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
