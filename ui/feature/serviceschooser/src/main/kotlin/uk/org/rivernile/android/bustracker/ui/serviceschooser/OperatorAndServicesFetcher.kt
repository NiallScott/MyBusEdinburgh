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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toStopIdentifier
import uk.org.rivernile.android.bustracker.core.operators.OperatorName
import uk.org.rivernile.android.bustracker.core.operators.OperatorsRepository
import uk.org.rivernile.android.bustracker.core.services.ServiceWithColour
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import javax.inject.Inject

/**
 * This fetches the operators and services used to populate the services chooser UI.
 *
 * @author Niall Scott
 */
internal interface OperatorAndServicesFetcher {

    /**
     * A [Flow] which emits a mapping of operators to services for the current arguments of the
     * services screen. When there are no items, `null` is emitted.
     */
    val operatorAndServicesFlow: Flow<Map<UiServiceChooserItem.Operator, List<ServiceWithColour>>?>
}

internal class RealOperatorAndServicesFetcher @Inject constructor(
    private val arguments: Arguments,
    private val operatorsRepository: OperatorsRepository,
    private val servicesRepository: ServicesRepository
) : OperatorAndServicesFetcher {

    @OptIn(ExperimentalCoroutinesApi::class)
    override val operatorAndServicesFlow get() = arguments
        .paramsFlow
        .flatMapLatest(::getUiContentFlow)

    private fun getUiContentFlow(
        params: ServicesChooserParams?
    ): Flow<Map<UiServiceChooserItem.Operator, List<ServiceWithColour>>?> {
        return when (params) {
            is ServicesChooserParams.AllServices -> allServicesFlow
            is ServicesChooserParams.Stop ->
                getServicesForStopFlow(params.stopIdentifier.toStopIdentifier())
            null -> flowOf(null)
        }
    }

    private val allServicesFlow get() = allOperatorNamesFlow
        .combine(
            servicesRepository.allServiceNamesWithColourFlow,
            ::combineOperatorNamesAndServices
        )

    private fun getServicesForStopFlow(stopIdentifier: StopIdentifier) = allOperatorNamesFlow
        .combine(
            servicesRepository.getServiceNamesWithColourFlow(stopIdentifier),
            ::combineOperatorNamesAndServices
        )

    private val allOperatorNamesFlow get() = operatorsRepository.allOperatorNamesFlow

    private fun combineOperatorNamesAndServices(
        operatorNames: Map<String, OperatorName>?,
        services: List<ServiceWithColour>?
    ): Map<UiServiceChooserItem.Operator, List<ServiceWithColour>>? {
        return services?.toOperatorServicesMap(operatorNames)
    }
}
