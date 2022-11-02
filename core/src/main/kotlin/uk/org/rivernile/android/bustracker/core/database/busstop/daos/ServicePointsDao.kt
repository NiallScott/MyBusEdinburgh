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

package uk.org.rivernile.android.bustracker.core.database.busstop.daos

import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.ServicePoint

/**
 * This DAO is used to access service points.
 *
 * @author Niall Scott
 */
interface ServicePointsDao {

    /**
     * This is a [Flow] which emits a [List] of [ServicePoint]s for the given [serviceNames]. `null`
     * may be omitted if there are no results.
     *
     * Ordering is in the following precedence;
     *
     * - [ServicePoint.serviceName]
     * - [ServicePoint.chainage]
     * - Then ordered by an internal ordering value, so the points are in the correct order.
     *
     * @param serviceNames Only [ServicePoint]s for the supplied [Set] of service names are
     * returned. `null` means all [ServicePoint]s are returned - this could be an expensive
     * operation.
     * @return A [List] of [ServicePoint]s for the given [serviceNames].
     */
    fun getServicePointsFlow(serviceNames: Set<String>?): Flow<List<ServicePoint>?>
}