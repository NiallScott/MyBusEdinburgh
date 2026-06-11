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

import androidx.compose.runtime.Immutable
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.operators.OperatorName
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServiceWithColour
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName

/**
 * This defines a service chooser item.
 *
 * @author Niall Scott
 */
@Immutable
internal sealed interface UiServiceChooserItem {

    /**
     * The represents an operator item.
     */
    sealed interface Operator : UiServiceChooserItem {

        /**
         * The operator is unknown.
         */
        data object Unknown : Operator

        /**
         * The operator is known.
         *
         * @param operatorId The ID of the operator.
         * @param operatorName The display name of the operator.
         */
        data class Named(
            val operatorId: String,
            val operatorName: String
        ) : Operator
    }

    /**
     * This represents a service item.
     *
     * @param serviceDescriptor The descriptor of the service.
     * @param serviceName The service name display properties.
     * @property isSelected Is the service currently selected?
     */
    data class Service(
        val serviceDescriptor: ServiceDescriptor,
        val serviceName: UiServiceName,
        val isSelected: Boolean
    ) : UiServiceChooserItem
}

/**
 * Converts this [List] of [ServiceWithColour]s to a [Map] of [UiServiceChooserItem.Operator] to
 * [List] of [ServiceWithColour]. Essentially, it groups services to operators.
 *
 * The [operators] [Map] is used to take an operator code mapping and convert this in to a
 * human-readable name for the operator.
 *
 * When the input [List] is empty then `null` will be returned. When the service is for an operator
 * for which no name is known, then this service will be grouped in to
 * [UiServiceChooserItem.Operator.Unknown].
 *
 * @param operators A mapping of the operator code to the operator name.
 * @return The services grouped to a map of operators to services, or `null` if the input [List] is
 * empty.
 */
internal fun List<ServiceWithColour>.toOperatorServicesMap(
    operators: Map<String, OperatorName>?
): Map<UiServiceChooserItem.Operator, List<ServiceWithColour>>? {
    if (isEmpty()) {
        return null
    }

    return groupBy {
        val operatorCode = it.serviceDescriptor.operatorCode
        val operatorName = operators
            ?.get(operatorCode)
            ?.displayName
            ?.ifBlank { null }

        if (operatorName != null) {
            UiServiceChooserItem.Operator.Named(
                operatorId = operatorCode,
                operatorName = operatorName
            )
        } else {
            UiServiceChooserItem.Operator.Unknown
        }
    }
}

/**
 * Converts this [Map] of operators to services in to a sorted [List] (sorted with [comparator]) of
 * [UiServiceChooserItem]s.
 *
 * @param selectedServices A [Set] of the currently selected services.
 * @param comparator The comparator used to sort operators and services.
 * @return A sorted [List] of operators and services, or `null` if the result is empty.
 */
internal fun Map<UiServiceChooserItem.Operator, List<ServiceWithColour>>.toUiServiceChooserItemList(
    selectedServices: Set<ServiceDescriptor>,
    comparator: Comparator<String>
): List<UiServiceChooserItem>? {
    return toSortedMap(createOperatorComparator(comparator))
        .flatMap { (operator, services) ->
            if (services.isNotEmpty()) {
                buildList {
                    add(operator)
                    addAll(
                        services
                            .map { it.toService(selectedServices) }
                            .sortedWith(
                                compareBy(comparator) {
                                    it.serviceDescriptor.serviceName
                                }
                            )
                    )
                }
            } else {
                emptyList()
            }
        }
        .ifEmpty { null }
}

private fun ServiceWithColour.toService(
    selectedServices: Set<ServiceDescriptor>
): UiServiceChooserItem.Service {
    return UiServiceChooserItem.Service(
        serviceDescriptor = serviceDescriptor,
        serviceName = toUiServiceName(
            serviceDescriptor = serviceDescriptor,
            serviceColours = colours
        ),
        isSelected = selectedServices.contains(serviceDescriptor)
    )
}

private fun toUiServiceName(
    serviceDescriptor: ServiceDescriptor,
    serviceColours: ServiceColours?
): UiServiceName {
    return UiServiceName(
        serviceName = serviceDescriptor.serviceName,
        colours = serviceColours?.toUiServiceColours()
    )
}

private fun ServiceColours.toUiServiceColours(): UiServiceColours {
    return UiServiceColours(
        backgroundColour = colourPrimary,
        textColour = colourOnPrimary
    )
}

private fun createOperatorComparator(
    nameComparator: Comparator<String>
): Comparator<UiServiceChooserItem.Operator> {
    return Comparator { a, b ->
        when {
            a is UiServiceChooserItem.Operator.Unknown &&
                b is UiServiceChooserItem.Operator.Unknown -> 0
            a is UiServiceChooserItem.Operator.Unknown -> 1
            b is UiServiceChooserItem.Operator.Unknown -> -1
            else -> nameComparator.compare(
                (a as UiServiceChooserItem.Operator.Named).operatorName,
                (b as UiServiceChooserItem.Operator.Named).operatorName
            )
        }
    }
}
