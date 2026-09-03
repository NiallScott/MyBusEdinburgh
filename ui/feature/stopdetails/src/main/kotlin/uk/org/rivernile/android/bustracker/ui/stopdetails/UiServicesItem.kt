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
import uk.org.rivernile.android.bustracker.core.services.ServiceColours
import uk.org.rivernile.android.bustracker.core.services.ServiceDetails
import uk.org.rivernile.android.bustracker.ui.text.UiServiceColours
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName

/**
 * This represents a service item to be displayed on the stop details screen.
 *
 * @author Niall Scott
 */
internal sealed interface UiServicesItem {

    /**
     * The represents an operator item.
     */
    sealed interface Operator : UiServicesItem {

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
     * @property serviceDescriptor A descriptor for the service.
     * @property serviceName The service name details.
     * @property description A description of the service.
     */
    data class Service(
        val serviceDescriptor: ServiceDescriptor,
        val serviceName: UiServiceName,
        val description: String?
    ) : UiServicesItem

    /**
     * This represents an item which informs the user that no services are available for the stop.
     */
    data object NoServices : UiServicesItem
}

/**
 * Converts this [Collection] of [ServiceDetails]s to a [Map] of [UiServicesItem.Operator]
 * to [Set] of [ServiceDetails]. Essentially, it groups services to operators.
 *
 * The [operators] [Map] is used to take an operator code mapping and convert this in to a
 * human-readable name for the operator.
 *
 * When the input [Collection] is empty then `null` will be returned. When the service is for an
 * operator for which no name is known, then this service will be grouped in to
 * [UiServicesItem.Operator.Unknown].
 *
 * @param operators A mapping of the operator code to the operator name.
 * @return The services grouped to a map of operators to services, or `null` if the input
 * [Collection] is empty.
 */
internal fun Collection<ServiceDetails>.toOperatorServicesMap(
    operators: Map<String, OperatorName>?
): Map<UiServicesItem.Operator, Set<ServiceDetails>>? {
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
            UiServicesItem.Operator.Named(
                operatorId = operatorCode,
                operatorName = operatorName
            )
        } else {
            UiServicesItem.Operator.Unknown
        }
    }.mapValues { it.value.toSet() }
}

/**
 * Converts this [Map] of operators to services in to a sorted [List] (sorted with [comparator]) of
 * [UiServicesItem]s.
 *
 * @param comparator The comparator used to sort operators and services.
 * @return A sorted [List] of operators and services, or `null` if the result is empty.
 */
internal fun Map<UiServicesItem.Operator, Collection<ServiceDetails>>.toUiServicesItemList(
    comparator: Comparator<String>
): List<UiServicesItem>? {
    return toSortedMap(createOperatorComparator(comparator))
        .flatMap { (operator, services) ->
            if (services.isNotEmpty()) {
                buildList {
                    add(operator)
                    addAll(
                        services
                            .map { it.toService() }
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

private fun ServiceDetails.toService(): UiServicesItem.Service {
    return UiServicesItem.Service(
        serviceDescriptor = serviceDescriptor,
        serviceName = toUiServiceName(
            serviceDescriptor = serviceDescriptor,
            serviceColours = colours
        ),
        description = description
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
): Comparator<UiServicesItem.Operator> {
    return Comparator { a, b ->
        when {
            a is UiServicesItem.Operator.Unknown &&
                b is UiServicesItem.Operator.Unknown -> 0
            a is UiServicesItem.Operator.Unknown -> 1
            b is UiServicesItem.Operator.Unknown -> -1
            else -> nameComparator.compare(
                (a as UiServicesItem.Operator.Named).operatorName,
                (b as UiServicesItem.Operator.Named).operatorName
            )
        }
    }
}
