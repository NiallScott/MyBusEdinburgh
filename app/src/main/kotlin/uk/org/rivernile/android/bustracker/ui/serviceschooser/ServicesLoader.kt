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
import uk.org.rivernile.android.bustracker.core.services.ServiceWithColour
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import javax.inject.Inject

/**
 * This class performs loading operations for [ServicesChooserDialogFragmentViewModel].
 *
 * @param arguments The arguments.
 * @param state Our state.
 * @param servicesRepository Where to get service data from.
 * @author Niall Scott
 */
class ServicesLoader @Inject constructor(
    private val arguments: Arguments,
    private val state: State,
    private val servicesRepository: ServicesRepository) {

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

    /**
     * Given [params], load the appropriate services for these parameters.
     *
     * @param params The parameters we were started with.
     * @return A [Flow] which emits the service listings suitable for the parameters. If [params] is
     * `null`, a flow of `null` will be returned.
     */
    private fun loadServices(params: ServicesChooserParams?) = params?.let {
        when (it) {
            is ServicesChooserParams.AllServices -> servicesRepository.allServiceNamesWithColourFlow
            is ServicesChooserParams.Stop ->
                servicesRepository.getServiceNamesWithColourFlow(it.stopCode)
        }
    } ?: flowOf(null)

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
        services: List<ServiceWithColour>?,
        selectedServices: Set<String>): List<UiService> {
        return services?.map {
            UiService(
                it.name,
                it.colour,
                selectedServices.contains(it.name))
        } ?: emptyList()
    }
}