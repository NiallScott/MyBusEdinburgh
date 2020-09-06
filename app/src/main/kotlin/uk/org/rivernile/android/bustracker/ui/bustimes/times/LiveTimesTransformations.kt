/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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
import javax.inject.Inject

/**
 * This class contains individual transformations which can be applied to live times, as used by
 * [LiveTimesTransform]. This class exists to make [LiveTimesTransform] more easily testable.
 *
 * @param isNightServiceDetector Detect whether the service is a night service or not. This is only
 * used when the user has set a preference to say they wish for night services to be filtered out
 * of the results.
 * @author Niall Scott
 */
class LiveTimesTransformations @Inject constructor(
        private val isNightServiceDetector: IsNightServiceDetector,
        private val serviceComparator: Comparator<String>) {

    /**
     * Given a [List] of [UiService]s, remove night services if the user has set this preference,
     * otherwise just return the input [List].
     *
     * @param services The [List] of [UiService]s to remove night services from.
     * @param showNightServices `true` if night services should be included, otherwise `false`.
     * @return The original input [List] if night services should be shown, or a new instance of the
     * [List] with night services filtered out if they should not be shown.
     */
    fun filterNightServices(
            services: List<UiService>,
            showNightServices: Boolean) = if (!showNightServices) {
        services.filterNot {
            isNightServiceDetector.isNightService(it.serviceName)
        }
    } else {
        services
    }

    /**
     * Given a [List] of [UiService]s, sort the services depending on the user's sorting preference.
     *
     * @param services The [List] of [UiService]s to sort.
     * @param sortByTime `true` if the user wants to sort by time, otherwise `false`.
     */
    fun sortServices(
            services: List<UiService>,
            sortByTime: Boolean): List<UiService> {
        val uiServiceComparator = compareBy(serviceComparator, UiService::serviceName)

        return if (sortByTime) {
            val minutesComparator = compareBy<UiService> {
                it.vehicles.firstOrNull()?.departureMinutes ?: Int.MAX_VALUE
            }

            services.sortedWith(minutesComparator.then(uiServiceComparator))
        } else {
            services.sortedWith(uiServiceComparator)
        }
    }

    /**
     * Given a [List] of [UiService] objects, convert this in to a [List] of [UiLiveTimesItem]
     * objects, and at the same time apply any service expansions.
     *
     * @param services The [List] of [UiService]s as the source data.
     * @param expandedServices The services to be expanded.
     * @return The input services mapped to a [List] of [UiLiveTimesItem]s, with any expansions
     * applied.
     */
    fun applyExpansions(
            services: List<UiService>,
            expandedServices: Set<String>): List<UiLiveTimesItem> {
        val mappedServices = mutableListOf<UiLiveTimesItem>()

        services.forEach { service ->
            val serviceName = service.serviceName
            val serviceColour = service.serviceColour

            if (expandedServices.contains(service.serviceName)) {
                service.vehicles.mapIndexedTo(mappedServices) { index, vehicle ->
                    UiLiveTimesItem(serviceName, serviceColour, vehicle, index, true)
                }
            } else {
                service.vehicles.firstOrNull()?.let {
                    mappedServices += UiLiveTimesItem(serviceName, serviceColour, it, 0, false)
                }
            }
        }

        return mappedServices
    }
}