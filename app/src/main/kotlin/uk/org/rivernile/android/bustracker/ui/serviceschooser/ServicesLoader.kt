/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.core.servicestops.ServiceStopsRepository
import javax.inject.Inject

/**
 * This class performs loading operations for [ServicesChooserDialogFragmentViewModel].
 *
 * @param arguments The arguments.
 * @param state Our state.
 * @param servicesRepository Where to get service data from.
 * @param serviceStopsRepository Where to get service stop data from.
 * @author Niall Scott
 */
class ServicesLoader @Inject constructor(
    private val arguments: Arguments,
    private val state: State,
    private val servicesRepository: ServicesRepository,
    private val serviceStopsRepository: ServiceStopsRepository) {

    /**
     * Emits [List]s of [UiService]s.
     */
    val servicesFlow get() =
        combine(
            servicesWithColoursFlow,
            state.selectedServicesFlow,
            this::combineServicesWithSelected)

    /**
     * This [Flow] emits services with colours. The service listing is based on the parameters which
     * were set.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    private val servicesWithColoursFlow get() = arguments
        .paramsFlow
        .flatMapLatest(this::loadServices)
        .flatMapLatest(this::loadColoursForServices)

    /**
     * Given [params], load the appropriate services for these parameters.
     *
     * @param params The parameters we were started with.
     * @return A [Flow] which emits the service listings suitable for the parameters. If [params] is
     * `null`, a flow of `null` will be returned.
     */
    private fun loadServices(params: ServicesChooserParams?) = params?.let {
        when (it) {
            is ServicesChooserParams.AllServices -> servicesRepository.allServiceNamesFlow
            is ServicesChooserParams.Stop ->
                serviceStopsRepository.getServicesForStopFlow(it.stopCode)
        }
    } ?: flowOf(null)

    /**
     * Given a [List] of services, load colours for these services.
     *
     * @param services The services to get colours for.
     * @return A [Flow] which matches services to colours. If [services] is `null`, a flow of `null`
     * will be returned.
     */
    private fun loadColoursForServices(services: List<String>?): Flow<List<ServiceColour>?> {
        return services?.ifEmpty { null }?.let { s ->
            servicesRepository.getColoursForServicesFlow(s.toSet())
                .map {
                    associateServicesToColours(s, it)
                }
        } ?: flowOf(null)
    }

    /**
     * Given a [List] of services and a [Map] of colurs, pair services up with their colours.
     *
     * @param services The services which will be shown for selection.
     * @param colours The mapping of services to colours.
     * @return A [List] where the services have been matched with their colours, if available.
     */
    private fun associateServicesToColours(
        services: List<String>,
        colours: Map<String, Int>?): List<ServiceColour> {
        return services.map {
            ServiceColour(it, colours?.get(it))
        }
    }

    /**
     * Given [services] and the [selectedServices], pair these up to produce each service's
     * [UiService] state.
     *
     * @param services The services with their colour.
     * @param selectedServices The user's selected services.
     * @return The combination of paring services up with their selected state, as a [List] of
     * [UiService].
     */
    private fun combineServicesWithSelected(
        services: List<ServiceColour>?,
        selectedServices: Set<String>): List<UiService> {
        return services?.map {
            UiService(
                it.serviceName,
                it.colour,
                selectedServices.contains(it.serviceName))
        } ?: emptyList()
    }

    /**
     * This is used a temporary holder of service data until it is matched up with its selected
     * state.
     *
     * @property serviceName The name of the service.
     * @property colour The colour of the service.
     */
    private data class ServiceColour(
        val serviceName: String,
        val colour: Int?)
}