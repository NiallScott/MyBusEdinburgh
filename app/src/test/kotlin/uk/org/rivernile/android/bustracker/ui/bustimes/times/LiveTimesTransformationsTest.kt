/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import uk.org.rivernile.android.bustracker.core.livetimes.IsNightServiceDetector
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Clock

/**
 * Tests for [LiveTimesTransformations].
 *
 * @author Niall Scott
 */
class LiveTimesTransformationsTest {

    @Test
    fun filterNightServicesCopesWithEmptyServicesWhenShowNightServicesIsEnabled() {
        val transformations = createLiveTimesTransformations()
        val result = transformations.filterNightServices(emptyList(), true)

        assertTrue(result.isEmpty())
    }

    @Test
    fun filterNightServicesCopesWithEmptyServicesWhenShowNightServicesIsNotEnabled() {
        val transformations = createLiveTimesTransformations()
        val result = transformations.filterNightServices(emptyList(), false)

        assertTrue(result.isEmpty())
    }

    @Test
    fun filterNightServicesDoesNotFilterOutServicesWhenShowingNightServicesAndNoNightBuses() {
        val transformations = createLiveTimesTransformations()
        val services = listOf(
            UiService(
                "1",
                null,
                emptyList()
            ),
            UiService(
                "2",
                null,
                emptyList()
            ),
            UiService(
                "3",
                null,
                emptyList()
            )
        )

        val result = transformations.filterNightServices(services, true)

        assertEquals(services, result)
    }

    @Test
    fun filterNightServiceFiltersOutNightServicesWhenNotShowingNightServices() {
        val transformations = createLiveTimesTransformations(
            isNightService = { it == "2" }
        )
        val services = listOf(
            UiService(
                "1",
                null,
                emptyList()
            ),
            UiService(
                "2",
                null,
                emptyList()
            ),
            UiService(
                "3",
                null,
                emptyList()
            )
        )
        val expected = listOf(
            UiService(
                "1",
                null,
                emptyList()
            ),
            UiService(
                "3",
                null,
                emptyList()
            )
        )

        val result = transformations.filterNightServices(services, false)

        assertEquals(expected, result)
    }

    @Test
    fun sortServicesCopesWithEmptyListWhenSortingByTime() {
        val transformations = createLiveTimesTransformations()
        val result = transformations.sortServices(emptyList(), true)

        assertTrue(result.isEmpty())
    }

    @Test
    fun sortServicesCopesWithEmptyListWhenSortingByService() {
        val transformations = createLiveTimesTransformations()
        val result = transformations.sortServices(emptyList(), false)

        assertTrue(result.isEmpty())
    }

    @Test
    fun sortServicesSortedByTimeMovesServiceWithEmptyVehiclesToEnd() {
        val transformations = createLiveTimesTransformations()
        val date = Clock.System.now()
        val services = listOf(
            UiService(
                "1",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        10,
                        false
                    )
                )
            ),
            UiService(
                "2",
                null,
                emptyList()
            ),
            UiService(
                "3",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        4,
                        false)
                )
            )
        )
        val expected = listOf(
            UiService(
                "3",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        4,
                        false
                    )
                )
            ),
            UiService(
                "1",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        10,
                        false
                    )
                )
            ),
            UiService(
                "2",
                null,
                emptyList()
            )
        )

        val result = transformations.sortServices(services, true)

        assertEquals(expected, result)
    }

    @Test
    fun sortServicesSortedByTimeSortsAsExpected() {
        val transformations = createLiveTimesTransformations()
        val date = Clock.System.now()
        val services = listOf(
            UiService(
                "2",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        10,
                        false
                    )
                )
            ),
            UiService(
                "1",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        10,
                        false
                    )
                )
            ),
            UiService(
                "3",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        4,
                        false
                    )
                )
            )
        )
        val expected = listOf(
            UiService(
                "3",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        4,
                        false
                    )
                )
            ),
            UiService(
                "1",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        10,
                        false
                    )
                )
            ),
            UiService(
                "2",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        10,
                        false
                    )
                )
            )
        )

        val result = transformations.sortServices(services, true)

        assertEquals(expected, result)
    }

    @Test
    fun applyExpansionsYieldsEmptyListWhenInputIsEmpty() {
        val transformations = createLiveTimesTransformations()
        val result = transformations.applyExpansions(emptyList(), emptySet())

        assertTrue(result.isEmpty())
    }

    @Test
    fun applyExpansionsDoesNotAddCollapsedServiceWithZeroVehicles() {
        val transformations = createLiveTimesTransformations()
        val date = Clock.System.now()
        val services = listOf(
            UiService(
                "1",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        10,
                        false
                    )
                )
            ),
            UiService(
                "2",
                null,
                emptyList()
            ),
            UiService(
                "3",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        4,
                        false
                    )
                )
            )
        )
        val expected = listOf(
            UiLiveTimesItem(
                "1",
                null,
                UiVehicle(
                    "Destination",
                    false,
                    date,
                    10,
                    false
                ),
                0,
                false
            ),
            UiLiveTimesItem(
                "3",
                null,
                UiVehicle(
                    "Destination",
                    false,
                    date,
                    4,
                    false
                ),
                0,
                false
            )
        )

        val result = transformations.applyExpansions(services, emptySet())

        assertEquals(expected, result)
    }

    @Test
    fun applyExpansionsExpandsServices() {
        val transformations = createLiveTimesTransformations()
        val date = Clock.System.now()
        val services = listOf(
            UiService(
                "1",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        10,
                        false
                    ),
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        12,
                        false
                    )
                )
            ),
            UiService(
                "3",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        4,
                        false
                    ),
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        6,
                        false
                    )
                )
            )
        )
        val expected = listOf(
            UiLiveTimesItem(
                "1",
                null,
                UiVehicle(
                    "Destination",
                    false,
                    date,
                    10,
                    false
                ),
                0,
                true
            ),
            UiLiveTimesItem(
                "1",
                null,
                UiVehicle(
                    "Destination",
                    false,
                    date,
                    12,
                    false
                ),
                1,
                true
            ),
            UiLiveTimesItem(
                "3",
                null,
                UiVehicle(
                    "Destination",
                    false,
                    date,
                    4,
                    false
                ),
                0,
                false
            )
        )

        val result = transformations.applyExpansions(services, setOf("1"))

        assertEquals(expected, result)
    }

    @Test
    fun applyExpansionsExpandsServicesWhenAllExpanded() {
        val transformations = createLiveTimesTransformations()
        val date = Clock.System.now()
        val services = listOf(
            UiService(
                "1",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        10,
                        false
                    ),
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        12,
                        false
                    )
                )
            ),
            UiService(
                "3",
                null,
                listOf(
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        4,
                        false
                    ),
                    UiVehicle(
                        "Destination",
                        false,
                        date,
                        6,
                        false
                    )
                )
            )
        )
        val expected = listOf(
            UiLiveTimesItem(
                "1",
                null,
                UiVehicle(
                    "Destination",
                    false,
                    date,
                    10,
                    false
                ),
                0,
                true
            ),
            UiLiveTimesItem(
                "1",
                null,
                UiVehicle(
                    "Destination",
                    false,
                    date,
                    12,
                    false
                ),
                1,
                true
            ),
            UiLiveTimesItem(
                "3",
                null,
                UiVehicle(
                    "Destination",
                    false,
                    date,
                    4,
                    false
                ),
                0,
                true
            ),
            UiLiveTimesItem(
                "3",
                null,
                UiVehicle(
                    "Destination",
                    false,
                    date,
                    6,
                    false
                ),
                1,
                true
            )
        )

        val result = transformations.applyExpansions(services, setOf("1", "3"))

        assertEquals(expected, result)
    }

    private fun createLiveTimesTransformations(
        isNightService: (String) -> Boolean = { false }
    ): LiveTimesTransformations {
        return LiveTimesTransformations(
            isNightServiceDetector = object : IsNightServiceDetector {
                override fun isNightService(serviceName: String) = isNightService(serviceName)
            },
            serviceComparator = naturalOrder()
        )
    }
}
