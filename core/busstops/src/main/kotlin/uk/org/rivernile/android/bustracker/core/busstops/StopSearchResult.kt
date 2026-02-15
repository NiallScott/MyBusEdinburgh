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
import uk.org.rivernile.android.bustracker.core.database.busstop.stop.StopSearchResult
    as DatabaseStopSearchResult

/**
 * This represents a stop search result.
 *
 * @author Niall Scott
 */
public interface StopSearchResult {

    /**
     * Used to identify the stop.
     */
    public val stopIdentifier: StopIdentifier

    /**
     * The stop name details.
     */
    public val stopName: StopName

    /**
     * The orientation of the stop.
     */
    public val orientation: StopOrientation

    /**
     * The [List]ing of the service for this stop.
     */
    public val serviceListing: List<ServiceDescriptor>?
}

internal fun List<DatabaseStopSearchResult>.toStopSearchResults() = map { it.toStopSearchResult() }

private fun DatabaseStopSearchResult.toStopSearchResult(): StopSearchResult =
    WrappedStopSearchResult(this)

@JvmInline
internal value class WrappedStopSearchResult(
    val databaseStopSearchResult: DatabaseStopSearchResult
) : StopSearchResult {

    override val stopIdentifier get() = databaseStopSearchResult.naptanStopIdentifier

    override val stopName get() = databaseStopSearchResult.stopName.toStopName()

    override val orientation get() = databaseStopSearchResult.orientation

    override val serviceListing get() = databaseStopSearchResult.serviceListing
}
