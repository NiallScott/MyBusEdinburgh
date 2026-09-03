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

package uk.org.rivernile.android.bustracker.ui.stopdetails

import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.operators.OperatorName
import uk.org.rivernile.android.bustracker.core.services.ServiceDetails
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.collections.Set
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for `UiServicesItem.kt`.
 *
 * @author Niall Scott
 */
class UiServicesItemKtTest {

    @Test
    fun toOperatorServicesMapReturnsNullWhenServicesIsEmpty() {
        val result = emptyList<ServiceDetails>()
            .toOperatorServicesMap(operators = null)

        assertNull(result)
    }

    @Test
    fun toOperatorServicesMapSingleServiceWithNullOperators() {
        val service1 = ServiceDetails(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            description = "Description",
            colours = null
        )

        val result = setOf(service1)
            .toOperatorServicesMap(operators = null)

        assertEquals(
            mapOf<UiServicesItem.Operator, Set<ServiceDetails>>(
                UiServicesItem.Operator.Unknown to setOf(service1)
            ),
            result
        )
    }

    @Test
    fun toOperatorServicesMapSingleServiceWithEmptyOperators() {
        val service1 = ServiceDetails(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            description = "Description",
            colours = null
        )

        val result = setOf(service1)
            .toOperatorServicesMap(operators = emptyMap())

        assertEquals(
            mapOf<UiServicesItem.Operator, Set<ServiceDetails>>(
                UiServicesItem.Operator.Unknown to setOf(service1)
            ),
            result
        )
    }

    @Test
    fun toOperatorServicesMapSingleWithBlankOperatorName() {
        val service1 = ServiceDetails(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            description = "Description",
            colours = null
        )

        val result = setOf(service1)
            .toOperatorServicesMap(
                operators = mapOf(
                    "UNKNOWN" to OperatorName(displayName = "")
                )
            )

        assertEquals(
            mapOf<UiServicesItem.Operator, Set<ServiceDetails>>(
                UiServicesItem.Operator.Unknown to setOf(service1)
            ),
            result
        )
    }

    @Test
    fun toOperatorServicesMapSingleServiceWithNonMatchingOperator() {
        val service1 = ServiceDetails(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            description = "Description",
            colours = null
        )

        val result = setOf(service1)
            .toOperatorServicesMap(
                operators = mapOf(
                    "UNKNOWN" to OperatorName(displayName = "Unknown")
                )
            )

        assertEquals(
            mapOf<UiServicesItem.Operator, Set<ServiceDetails>>(
                UiServicesItem.Operator.Unknown to setOf(service1)
            ),
            result
        )
    }

    @Test
    fun toOperatorServicesMapSingleServiceWithMatchingOperator() {
        val service1 = ServiceDetails(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            description = "Description",
            colours = null
        )

        val result = setOf(service1)
            .toOperatorServicesMap(
                operators = mapOf(
                    "TEST1" to OperatorName(displayName = "Test 1")
                )
            )

        assertEquals(
            mapOf<UiServicesItem.Operator, Set<ServiceDetails>>(
                UiServicesItem.Operator.Named(
                    operatorId = "TEST1",
                    operatorName = "Test 1"
                ) to setOf(service1)
            ),
            result
        )
    }

    @Test
    fun toOperatorServicesMapWithMultipleServices() {
        val service1 = ServiceDetails(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            description = "Description 1",
            colours = null
        )
        val service2 = ServiceDetails(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "2",
                operatorCode = "TEST2"
            ),
            description = "Description 2",
            colours = null
        )
        val service3 = ServiceDetails(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "3",
                operatorCode = "TEST3"
            ),
            description = "Description 3",
            colours = null
        )
        val service4 = ServiceDetails(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "4",
                operatorCode = "TEST1"
            ),
            description = "Description 4",
            colours = null
        )

        val result = setOf(service1, service2, service3, service4)
            .toOperatorServicesMap(
                operators = mapOf(
                    "TEST1" to OperatorName(displayName = "Test 1"),
                    "TEST3" to OperatorName(displayName = "Test 3")
                )
            )

        assertEquals(
            mapOf(
                UiServicesItem.Operator.Named(
                    operatorId = "TEST1",
                    operatorName = "Test 1"
                ) to setOf(service1, service4),
                UiServicesItem.Operator.Named(
                    operatorId = "TEST3",
                    operatorName = "Test 3"
                ) to setOf(service3),
                UiServicesItem.Operator.Unknown to setOf(service2)
            ),
            result
        )
    }

    @Test
    fun toUiServicesItemListReturnsNullWhenCollectionIsEmpty() {
        val result = emptyMap<UiServicesItem.Operator, Set<ServiceDetails>>()
            .toUiServicesItemList(
                comparator = naturalOrder()
            )

        assertNull(result)
    }

    @Test
    fun toUiServicesItemListReturnsNullWhenHasSingleOperatorWithNoServices() {
        val result = mapOf<UiServicesItem.Operator, Set<ServiceDetails>>(
            UiServicesItem.Operator.Named(
                operatorId = "TEST1",
                operatorName = "Test 1"
            ) to emptySet()
        ).toUiServicesItemList(
            comparator = naturalOrder()
        )

        assertNull(result)
    }

    @Test
    fun toUiServicesItemListReturnsPopulatedListWhenInputHasItems() {
        val result = mapOf<UiServicesItem.Operator, Set<ServiceDetails>>(
            UiServicesItem.Operator.Named(
                operatorId = "TEST1",
                operatorName = "Test 1"
            ) to setOf(
                ServiceDetails(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    description = "Description",
                    colours = null
                )
            )
        ).toUiServicesItemList(
            comparator = naturalOrder()
        )

        assertEquals(
            listOf(
                UiServicesItem.Operator.Named(
                    operatorId = "TEST1",
                    operatorName = "Test 1"
                ),
                UiServicesItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "1",
                        colours = null
                    ),
                    description = "Description"
                )
            ),
            result
        )
    }

    @Test
    fun toUiServicesItemListSortsItemsInCorrectOrder() {
        val result = mapOf(
            UiServicesItem.Operator.Unknown to setOf(
                ServiceDetails(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "100",
                        operatorCode = "UNKNOWN"
                    ),
                    description = "Description 100",
                    colours = null
                )
            ),
            UiServicesItem.Operator.Named(
                operatorId = "NOSERVICES",
                operatorName = "No services"
            ) to emptySet(),
            UiServicesItem.Operator.Named(
                operatorId = "TEST2",
                operatorName = "Test 2"
            ) to setOf(
                ServiceDetails(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "4",
                        operatorCode = "TEST2"
                    ),
                    description = "Description 4",
                    colours = null
                ),
                ServiceDetails(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "6",
                        operatorCode = "TEST2"
                    ),
                    description = null,
                    colours = null
                ),
                ServiceDetails(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "5",
                        operatorCode = "TEST2"
                    ),
                    description = "Description 5",
                    colours = null
                )
            ),
            UiServicesItem.Operator.Named(
                operatorId = "TEST1",
                operatorName = "Test 1"
            ) to setOf(
                ServiceDetails(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "3",
                        operatorCode = "TEST1"
                    ),
                    description = "Description 3",
                    colours = null
                ),
                ServiceDetails(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "2",
                        operatorCode = "TEST1"
                    ),
                    description = "Description 2",
                    colours = null
                ),
                ServiceDetails(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    description = "Description 1",
                    colours = null
                )
            )
        ).toUiServicesItemList(
            comparator = naturalOrder()
        )

        assertEquals(
            listOf(
                UiServicesItem.Operator.Named(
                    operatorId = "TEST1",
                    operatorName = "Test 1"
                ),
                UiServicesItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "1",
                        colours = null
                    ),
                    description = "Description 1"
                ),
                UiServicesItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "2",
                        operatorCode = "TEST1"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "2",
                        colours = null
                    ),
                    description = "Description 2"
                ),
                UiServicesItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "3",
                        operatorCode = "TEST1"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "3",
                        colours = null
                    ),
                    description = "Description 3"
                ),
                UiServicesItem.Operator.Named(
                    operatorId = "TEST2",
                    operatorName = "Test 2"
                ),
                UiServicesItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "4",
                        operatorCode = "TEST2"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "4",
                        colours = null
                    ),
                    description = "Description 4"
                ),
                UiServicesItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "5",
                        operatorCode = "TEST2"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "5",
                        colours = null
                    ),
                    description = "Description 5"
                ),
                UiServicesItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "6",
                        operatorCode = "TEST2"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "6",
                        colours = null
                    ),
                    description = null
                ),
                UiServicesItem.Operator.Unknown,
                UiServicesItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "100",
                        operatorCode = "UNKNOWN"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "100",
                        colours = null
                    ),
                    description = "Description 100"
                )
            ),
            result
        )
    }
}
