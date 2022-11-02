/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.ServicePoint
import uk.org.rivernile.android.bustracker.core.servicepoints.ServicePointsRepository
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import javax.inject.Inject

/**
 * This class is used to retrieve the route lines for services, to be displayed on the map.
 *
 * @param servicePointsRepository Used to retrieve the route lines.
 * @param servicesRepository Used to retrieve colours for services.
 * @author Niall Scott
 */
class RouteLineRetriever @Inject constructor(
        private val servicePointsRepository: ServicePointsRepository,
        private val servicesRepository: ServicesRepository) {

    /**
     * Given a [Set] of selected services, retrieve the route lines for these services. If
     * [selectedServices] is `null` or empty, `null` will be returned.
     *
     * @param selectedServices The user's selected services.
     * @return The [List] of [UiServiceRoute] for the given [selectedServices], or `null` if
     * [selectedServices] is `null` or empty, or `null` if no route lines could be found.
     */
    fun getRouteLinesFlow(selectedServices: Set<String>?) =
            selectedServices?.ifEmpty { null }?.let {
                combine(
                        servicePointsRepository.getServicePointsFlow(it),
                        servicesRepository.getColoursForServicesFlow(it),
                        this::mapToRouteLines)
            } ?: flowOf(null)

    /**
     * Given a loaded [List] of [ServicePoint]s and a loaded mapping of service colours, map this
     * to the output structure, to be consumed by the UI.
     *
     * @param servicePoints The input [List] of [ServicePoint]s to be mapped.
     * @param serviceColours A [Map]ping of service names to colour.
     * @return The inputs mapped to a [List] of [UiServiceRoute], or `null` if there were no
     * [servicePoints].
     */
    private fun mapToRouteLines(
            servicePoints: List<ServicePoint>?,
            serviceColours: Map<String, Int>?): List<UiServiceRoute>? {
        return servicePoints?.ifEmpty { null }?.let { points ->
            val result = mutableMapOf<String, MutableUiServiceRoute>()

            points.forEach { point ->
                val service = result.getOrPut(point.serviceName) {
                    MutableUiServiceRoute(
                            point.serviceName,
                            serviceColours?.get(point.serviceName))
                }

                val mutablePoints = service.lines.getOrPut(point.chainage) {
                    mutableListOf()
                }

                mutablePoints += UiLatLon(point.latitude, point.longitude)
            }

            result.values
                    .mapNotNull(MutableUiServiceRoute::build)
                    .ifEmpty { null }
        }
    }

    /**
     * This is a mutable version of [UiServiceRoute] used to store route line properties while it
     * is being built.
     *
     * @property serviceName The name of the service for this route line.
     * @property serviceColour An optional colour for this service. If `null`, a colour will be
     * assigned downstream.
     * @property lines The route lines for this service.
     */
    private data class MutableUiServiceRoute(
            val serviceName: String,
            val serviceColour: Int?,
            val lines: MutableMap<Int, MutableList<UiLatLon>> = mutableMapOf()) {

        /**
         * From this [MutableUiServiceRoute], build the non-mutable [UiServiceRoute] version. This
         * method will return `null` when no meaningful route can be build due to data structures
         * being empty.
         *
         * @return The [UiServiceRoute] version of this object, or `null` if data structures are
         * empty and no meaningful object can be built.
         */
        fun build(): UiServiceRoute? = lines
                .values
                .mapNotNull { points ->
                    points.ifEmpty { null }?.let(::UiServiceLine)
                }
                .ifEmpty { null }
                ?.let {
                    UiServiceRoute(
                            serviceName,
                            serviceColour,
                            it)
                }
    }
}