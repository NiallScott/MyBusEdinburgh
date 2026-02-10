/*
 * Copyright (C) 2022 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.servicepoints

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.database.busstop.servicepoint.ServicePointDao
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import javax.inject.Inject

/**
 * This repository is used to access service point data.
 *
 * @author Niall Scott
 */
public interface ServicePointsRepository {

    /**
     * This is a [Flow] which emits a [List] of [ServicePoint]s for the given [serviceDescriptors].
     * `null` may be omitted if there are no results.
     *
     * Ordering is in the following precedence;
     *
     * - [ServicePoint.serviceDescriptor]
     * - [ServicePoint.routeSection]
     * - Then ordered by an internal ordering value, so the points are in the correct order.
     *
     * @param serviceDescriptors Only [ServicePoint]s for the supplied [Set] of services are
     * returned. `null` means all [ServicePoint]s are returned - this could be an expensive
     * operation.
     * @return A [List] of [ServicePoint]s for the given [serviceDescriptors].
     */
    public fun getServicePointsFlow(
        serviceDescriptors: Set<ServiceDescriptor>?
    ): Flow<List<ServicePoint>?>
}

internal class RealServicePointsRepository @Inject constructor(
    private val servicePointsDao: ServicePointDao
) : ServicePointsRepository {

    override fun getServicePointsFlow(
        serviceDescriptors: Set<ServiceDescriptor>?
    ): Flow<List<ServicePoint>?> {
        return servicePointsDao
            .getServicePointsFlow(serviceDescriptors)
            .map { it?.toServicePoints() }
    }
}
