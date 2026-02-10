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

package uk.org.rivernile.android.bustracker.core.database.settings.alerts.arrival

import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier

/**
 * A fake [ArrivalAlertDao] for testing.
 *
 * @author Niall Scott
 */
class FakeArrivalAlertDao(
    private val onAddArrivalAlert: (ArrivalAlert) -> Unit = { throw NotImplementedError() },
    private val onRemoveArrivalAlertById: (Int) -> Unit = { throw NotImplementedError() },
    private val onRemovalArrivalAlertByStopIdentifier: (StopIdentifier) -> Unit =
        { throw NotImplementedError() },
    private val onRemoveAllArrivalAlerts: () -> Unit = { throw NotImplementedError() },
    private val onGetHasArrivalAlertFlow: (StopIdentifier) -> Flow<Boolean> =
        { throw NotImplementedError() },
    private val onGetAllArrivalAlerts: () -> List<ArrivalAlert>? = { throw NotImplementedError() },
    private val onGetAllArrivalAlertsFlow: () -> Flow<List<ArrivalAlert>?> =
        { throw NotImplementedError() },
    private val onGetAllArrivalAlertStops: () -> Set<StopIdentifier>? =
        { throw NotImplementedError() },
    private val onGetArrivalAlertCount: () -> Int = { throw NotImplementedError() },
    private val onArrivalAlertCountFlow: () -> Flow<Int> = { throw NotImplementedError() }
) : ArrivalAlertDao {

    override suspend fun addArrivalAlert(arrivalAlert: ArrivalAlert) {
        onAddArrivalAlert(arrivalAlert)
    }

    override suspend fun removeArrivalAlert(id: Int) {
        onRemoveArrivalAlertById(id)
    }

    override suspend fun removeArrivalAlert(stopIdentifier: StopIdentifier) {
        onRemovalArrivalAlertByStopIdentifier(stopIdentifier)
    }

    override suspend fun removeAllArrivalAlerts() {
        onRemoveAllArrivalAlerts()
    }

    override fun getHasArrivalAlertFlow(stopIdentifier: StopIdentifier) =
        onGetHasArrivalAlertFlow(stopIdentifier)

    override suspend fun getAllArrivalAlerts() = onGetAllArrivalAlerts()

    override val allArrivalAlertsFlow get() = onGetAllArrivalAlertsFlow()

    override suspend fun getAllArrivalAlertStops() = onGetAllArrivalAlertStops()

    override suspend fun getArrivalAlertCount() = onGetArrivalAlertCount()

    override val arrivalAlertCountFlow get() = onArrivalAlertCountFlow()
}
