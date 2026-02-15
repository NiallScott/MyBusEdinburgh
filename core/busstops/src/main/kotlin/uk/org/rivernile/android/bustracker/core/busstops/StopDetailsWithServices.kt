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

package uk.org.rivernile.android.bustracker.core.busstops

import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopDetailsWithServices
    as DatabaseStopDetailsWithServices

/**
 * Like [StopDetails], this contains details for a single stop, except it also contains the service
 * listing for the stop.
 *
 * @author Niall Scott
 */
public interface StopDetailsWithServices {

    /**
     * The identifier for the stop.
     */
    public val stopIdentifier: StopIdentifier

    /**
     * The name details of the stop.
     */
    public val stopName: StopName

    /**
     * The location details of the stop.
     */
    public val location: StopLocation

    /**
     * The orientation of the stop.
     */
    public val orientation: StopOrientation

    /**
     * The [List]ing of the service for this stop.
     */
    public val serviceListing: List<ServiceDescriptor>?
}

internal fun List<DatabaseStopDetailsWithServices>.toStopDetailsWithServicesList() =
    map { it.toStopDetailsWithServices() }

private fun DatabaseStopDetailsWithServices.toStopDetailsWithServices(): StopDetailsWithServices {
    return WrappedStopDetailsWithServices(this)
}

@JvmInline
internal value class WrappedStopDetailsWithServices(
    val databaseStopDetailsWithServices: DatabaseStopDetailsWithServices
) : StopDetailsWithServices {

    override val stopIdentifier get() = databaseStopDetailsWithServices.naptanStopIdentifier

    override val stopName get() = databaseStopDetailsWithServices.stopName.toStopName()

    override val location get() = databaseStopDetailsWithServices.location.toStopLocation()

    override val orientation get() = databaseStopDetailsWithServices.orientation

    override val serviceListing get() = databaseStopDetailsWithServices.serviceListing
}
