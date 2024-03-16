/*
 * Copyright (C) 2019 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes

import uk.org.rivernile.android.bustracker.core.endpoints.tracker.ServiceNameFixer
import uk.org.rivernile.edinburghbustrackerapi.bustimes.BusTime
import javax.inject.Inject

/**
 * This class is used to map the Edinburgh bus tracker API to [Service] objects.
 *
 * @param vehicleMapper An instance of the [VehicleMapper] implementation.
 * @param serviceNameFixer Used to fix service names.
 * @author Niall Scott
 */
internal class ServiceMapper @Inject constructor(
    private val vehicleMapper: VehicleMapper,
    private val serviceNameFixer: ServiceNameFixer) {

    /**
     * Given a [BusTime] object, extract the data from it required to build a [Service] object.
     * If the [BusTime] object has no extractable [Vehicle]s or the service name is missing, `null`
     * will be returned.
     *
     * @param busTime The object to extract data from.
     * @return A [Service] containing data extracted from the [BusTime] object, or `null` if there
     * was an issue parsing the object.
     */
    fun mapToService(busTime: BusTime) =
        busTime
            .timeDatas
            ?.mapNotNull(vehicleMapper::mapToVehicle)
            ?.sorted()
            ?.takeIf { it.isNotEmpty() }
            ?.let { services ->
                serviceNameFixer.correctServiceName(busTime.mnemoService)?.let { serviceName ->
                    Service(serviceName,
                        services,
                        busTime.operatorId,
                        busTime.nameService,
                        busTime.serviceDisruption ?: false,
                        busTime.serviceDiversion ?: false)
                }
            }
}