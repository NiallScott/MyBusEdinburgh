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

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.operators.OperatorName
import uk.org.rivernile.android.bustracker.core.operators.OperatorsRepository
import uk.org.rivernile.android.bustracker.core.services.ServiceDetails
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import javax.inject.Inject

/**
 * This retrieves the service items to be displayed on the stop details screen.
 *
 * @author Niall Scott
 */
internal interface UiServicesItemsRetriever {

    /**
     * A [Flow] which emits the [List] of [UiServicesItem]s to be displayed on the stop details
     * screen.
     *
     * @param stopIdentifier The identifier of the stop to get services for.
     * @return A [Flow] which emits the [List] of [UiServicesItem]s.
     */
    fun getUiServicesItemsFlow(stopIdentifier: StopIdentifier): Flow<List<UiServicesItem>>
}

internal class RealUiServicesItemsRetriever @Inject constructor(
    private val operatorsRepository: OperatorsRepository,
    private val servicesRepository: ServicesRepository,
    private val alphanumericComparator: Comparator<String>
) : UiServicesItemsRetriever {

    override fun getUiServicesItemsFlow(
        stopIdentifier: StopIdentifier
    ): Flow<List<UiServicesItem>> {
        return operatorsRepository
            .allOperatorNamesFlow
            .combine(
                servicesRepository.getServiceDetailsFlow(stopIdentifier),
                ::createOperatorServicesMap
            )
            .map { operatorServicesMap ->
                operatorServicesMap
                    ?.toUiServicesItemList(
                        comparator = alphanumericComparator
                    )
                    ?: listOf(UiServicesItem.NoServices)
            }
    }

    private fun createOperatorServicesMap(
        operators: Map<String, OperatorName>?,
        serviceDetails: Collection<ServiceDetails>?
    ): Map<UiServicesItem.Operator, Collection<ServiceDetails>>? {
        return serviceDetails?.toOperatorServicesMap(
            operators = operators
        )
    }
}
