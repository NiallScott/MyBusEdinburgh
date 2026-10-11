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

package uk.org.rivernile.android.bustracker.ui.alerts.manager

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.busstops.StopDetails
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import javax.inject.Inject

/**
 * This retrieves [UiStopDetails] to be matched up with alerts.
 *
 * @author Niall Scott
 */
internal interface UiStopDetailsRetriever {

    /**
     * Get a [Flow] which emits mappings of [StopIdentifier] to [UiStopDetails], based on the
     * requested [stopIdentifiers].
     *
     * @param stopIdentifiers The identifiers of the stops to request details of.
     */
    fun getUiStopDetailsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, UiStopDetails>?>
}

internal class RealUiStopDetailsRetriever @Inject constructor(
    private val busStopsRepository: BusStopsRepository
) : UiStopDetailsRetriever {

    override fun getUiStopDetailsFlow(
        stopIdentifiers: Set<StopIdentifier>
    ): Flow<Map<StopIdentifier, UiStopDetails>?> {
        return busStopsRepository
            .getBusStopDetailsFlow(stopIdentifiers)
            .map { stopDetails ->
                stopDetails
                    ?.ifEmpty { null }
                    ?.mapValuesToUiStopDetails()
            }
    }

    private fun Map<StopIdentifier, StopDetails>.mapValuesToUiStopDetails() = mapValues {
        it.value.toUiStopDetails()
    }
}
