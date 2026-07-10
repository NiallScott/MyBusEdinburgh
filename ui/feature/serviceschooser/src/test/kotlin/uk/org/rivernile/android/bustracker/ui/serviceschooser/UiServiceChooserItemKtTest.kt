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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.operators.OperatorName
import uk.org.rivernile.android.bustracker.core.services.ServiceWithColour
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for `UiServiceChooserItem.kt`.
 *
 * @author Niall Scott
 */
class UiServiceChooserItemKtTest {

    @Test
    fun toOperatorServicesCollectionReturnsNullWhenServicesIsEmpty() {
        val result = emptyList<ServiceWithColour>()
            .toOperatorServicesMap(operators = null)

        assertNull(result)
    }

    @Test
    fun toOperatorServicesCollectionSingleServiceWithNullOperators() {
        val service1 = ServiceWithColour(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            colours = null
        )

        val result = listOf(service1)
            .toOperatorServicesMap(operators = null)

        assertEquals(
            mapOf<UiServiceChooserItem.Operator, List<ServiceWithColour>>(
                UiServiceChooserItem.Operator.Unknown to listOf(service1)
            ),
            result
        )
    }

    @Test
    fun toOperatorServicesCollectionSingleServiceWithEmptyOperators() {
        val service1 = ServiceWithColour(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            colours = null
        )

        val result = listOf(service1)
            .toOperatorServicesMap(operators = emptyMap())

        assertEquals(
            mapOf<UiServiceChooserItem.Operator, List<ServiceWithColour>>(
                UiServiceChooserItem.Operator.Unknown to listOf(service1)
            ),
            result
        )
    }

    @Test
    fun toOperatorServicesCollectionSingleWithBlankOperatorName() {
        val service1 = ServiceWithColour(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            colours = null
        )

        val result = listOf(service1)
            .toOperatorServicesMap(
                operators = mapOf(
                    "UNKNOWN" to OperatorName(displayName = "")
                )
            )

        assertEquals(
            mapOf<UiServiceChooserItem.Operator, List<ServiceWithColour>>(
                UiServiceChooserItem.Operator.Unknown to listOf(service1)
            ),
            result
        )
    }

    @Test
    fun toOperatorServicesCollectionSingleServiceWithNonMatchingOperator() {
        val service1 = ServiceWithColour(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            colours = null
        )

        val result = listOf(service1)
            .toOperatorServicesMap(
                operators = mapOf(
                    "UNKNOWN" to OperatorName(displayName = "Unknown")
                )
            )

        assertEquals(
            mapOf<UiServiceChooserItem.Operator, List<ServiceWithColour>>(
                UiServiceChooserItem.Operator.Unknown to listOf(service1)
            ),
            result
        )
    }

    @Test
    fun toOperatorServicesCollectionSingleServiceWithMatchingOperator() {
        val service1 = ServiceWithColour(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            colours = null
        )

        val result = listOf(service1)
            .toOperatorServicesMap(
                operators = mapOf(
                    "TEST1" to OperatorName(displayName = "Test 1")
                )
            )

        assertEquals(
            mapOf<UiServiceChooserItem.Operator, List<ServiceWithColour>>(
                UiServiceChooserItem.Operator.Named(
                    operatorId = "TEST1",
                    operatorName = "Test 1"
                ) to listOf(service1)
            ),
            result
        )
    }

    @Test
    fun toOperatorServicesCollectionWithMultipleServices() {
        val service1 = ServiceWithColour(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "1",
                operatorCode = "TEST1"
            ),
            colours = null
        )
        val service2 = ServiceWithColour(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "2",
                operatorCode = "TEST2"
            ),
            colours = null
        )
        val service3 = ServiceWithColour(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "3",
                operatorCode = "TEST3"
            ),
            colours = null
        )
        val service4 = ServiceWithColour(
            serviceDescriptor = ServiceDescriptor(
                serviceName = "4",
                operatorCode = "TEST1"
            ),
            colours = null
        )

        val result = listOf(service1, service2, service3, service4)
            .toOperatorServicesMap(
                operators = mapOf(
                    "TEST1" to OperatorName(displayName = "Test 1"),
                    "TEST3" to OperatorName(displayName = "Test 3")
                )
            )

        assertEquals(
            mapOf(
                UiServiceChooserItem.Operator.Named(
                    operatorId = "TEST1",
                    operatorName = "Test 1"
                ) to listOf(service1, service4),
                UiServiceChooserItem.Operator.Named(
                    operatorId = "TEST3",
                    operatorName = "Test 3"
                ) to listOf(service3),
                UiServiceChooserItem.Operator.Unknown to listOf(service2)
            ),
            result
        )
    }

    @Test
    fun toUiServiceChooserItemListReturnsNullWhenCollectionIsEmpty() {
        val result = emptyMap<UiServiceChooserItem.Operator, List<ServiceWithColour>>()
            .toUiServiceChooserItemList(
                selectedServices = emptySet(),
                comparator = naturalOrder()
            )

        assertNull(result)
    }

    @Test
    fun toUiServiceChooserItemListReturnsNullWhenHasSingleOperatorWithNoServices() {
        val result = mapOf<UiServiceChooserItem.Operator, List<ServiceWithColour>>(
            UiServiceChooserItem.Operator.Named(
                operatorId = "TEST1",
                operatorName = "Test 1"
            ) to emptyList()
        ).toUiServiceChooserItemList(
            selectedServices = emptySet(),
            comparator = naturalOrder()
        )

        assertNull(result)
    }

    @Test
    fun toUiServiceChooserItemListReturnsListWithNoItemsSelectedWhenSelectedServicesIsEmpty() {
        val result = mapOf<UiServiceChooserItem.Operator, List<ServiceWithColour>>(
            UiServiceChooserItem.Operator.Named(
                operatorId = "TEST1",
                operatorName = "Test 1"
            ) to listOf(
                ServiceWithColour(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    colours = null
                )
            )
        ).toUiServiceChooserItemList(
            selectedServices = emptySet(),
            comparator = naturalOrder()
        )

        assertEquals(
            listOf(
                UiServiceChooserItem.Operator.Named(
                    operatorId = "TEST1",
                    operatorName = "Test 1"
                ),
                UiServiceChooserItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "1",
                        colours = null
                    ),
                    isSelected = false
                )
            ),
            result
        )
    }

    @Test
    fun toUiServiceChooserItemListReturnsListWithNoItemsSelectedWhenServiceNotSelected() {
        val result = mapOf<UiServiceChooserItem.Operator, List<ServiceWithColour>>(
            UiServiceChooserItem.Operator.Named(
                operatorId = "TEST1",
                operatorName = "Test 1"
            ) to listOf(
                ServiceWithColour(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    colours = null
                )
            )
        ).toUiServiceChooserItemList(
            selectedServices = setOf(
                ServiceDescriptor(
                    serviceName = "2",
                    operatorCode = "TEST2"
                )
            ),
            comparator = naturalOrder()
        )

        assertEquals(
            listOf(
                UiServiceChooserItem.Operator.Named(
                    operatorId = "TEST1",
                    operatorName = "Test 1"
                ),
                UiServiceChooserItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "1",
                        colours = null
                    ),
                    isSelected = false
                )
            ),
            result
        )
    }

    @Test
    fun toUiServiceChooserItemListReturnsListWithItemSelectedWhenServiceIsSelected() {
        val result = mapOf<UiServiceChooserItem.Operator, List<ServiceWithColour>>(
            UiServiceChooserItem.Operator.Named(
                operatorId = "TEST1",
                operatorName = "Test 1"
            ) to listOf(
                ServiceWithColour(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    colours = null
                )
            )
        ).toUiServiceChooserItemList(
            selectedServices = setOf(
                ServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST1"
                )
            ),
            comparator = naturalOrder()
        )

        assertEquals(
            listOf(
                UiServiceChooserItem.Operator.Named(
                    operatorId = "TEST1",
                    operatorName = "Test 1"
                ),
                UiServiceChooserItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "1",
                        colours = null
                    ),
                    isSelected = true
                )
            ),
            result
        )
    }

    @Test
    fun toUiServiceChooserItemListSortsItemsInCorrectOrder() {
        val result = mapOf(
            UiServiceChooserItem.Operator.Unknown to listOf(
                ServiceWithColour(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "100",
                        operatorCode = "UNKNOWN"
                    ),
                    colours = null
                )
            ),
            UiServiceChooserItem.Operator.Named(
                operatorId = "NOSERVICES",
                operatorName = "No services"
            ) to emptyList(),
            UiServiceChooserItem.Operator.Named(
                operatorId = "TEST2",
                operatorName = "Test 2"
            ) to listOf(
                ServiceWithColour(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "4",
                        operatorCode = "TEST2"
                    ),
                    colours = null
                ),
                ServiceWithColour(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "6",
                        operatorCode = "TEST2"
                    ),
                    colours = null
                ),
                ServiceWithColour(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "5",
                        operatorCode = "TEST2"
                    ),
                    colours = null
                )
            ),
            UiServiceChooserItem.Operator.Named(
                operatorId = "TEST1",
                operatorName = "Test 1"
            ) to listOf(
                ServiceWithColour(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "3",
                        operatorCode = "TEST1"
                    ),
                    colours = null
                ),
                ServiceWithColour(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "2",
                        operatorCode = "TEST1"
                    ),
                    colours = null
                ),
                ServiceWithColour(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    colours = null
                )
            )
        ).toUiServiceChooserItemList(
            selectedServices = setOf(
                ServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST1"
                ),
                ServiceDescriptor(
                    serviceName = "5",
                    operatorCode = "TEST2"
                ),
                ServiceDescriptor(
                    serviceName = "Gibberish",
                    operatorCode = "GIBBERISH"
                )
            ),
            comparator = naturalOrder()
        )

        assertEquals(
            listOf(
                UiServiceChooserItem.Operator.Named(
                    operatorId = "TEST1",
                    operatorName = "Test 1"
                ),
                UiServiceChooserItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "1",
                        operatorCode = "TEST1"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "1",
                        colours = null
                    ),
                    isSelected = true
                ),
                UiServiceChooserItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "2",
                        operatorCode = "TEST1"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "2",
                        colours = null
                    ),
                    isSelected = false
                ),
                UiServiceChooserItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "3",
                        operatorCode = "TEST1"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "3",
                        colours = null
                    ),
                    isSelected = false
                ),
                UiServiceChooserItem.Operator.Named(
                    operatorId = "TEST2",
                    operatorName = "Test 2"
                ),
                UiServiceChooserItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "4",
                        operatorCode = "TEST2"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "4",
                        colours = null
                    ),
                    isSelected = false
                ),
                UiServiceChooserItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "5",
                        operatorCode = "TEST2"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "5",
                        colours = null
                    ),
                    isSelected = true
                ),
                UiServiceChooserItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "6",
                        operatorCode = "TEST2"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "6",
                        colours = null
                    ),
                    isSelected = false
                ),
                UiServiceChooserItem.Operator.Unknown,
                UiServiceChooserItem.Service(
                    serviceDescriptor = ServiceDescriptor(
                        serviceName = "100",
                        operatorCode = "UNKNOWN"
                    ),
                    serviceName = UiServiceName(
                        serviceName = "100",
                        colours = null
                    ),
                    isSelected = false
                )
            ),
            result
        )
    }
}
