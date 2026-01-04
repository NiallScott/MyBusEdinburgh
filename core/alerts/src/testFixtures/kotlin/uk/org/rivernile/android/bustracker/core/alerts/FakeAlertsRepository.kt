/*
 * Copyright (C) 2025 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts

import kotlinx.coroutines.flow.Flow
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRequest
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertRequest

/**
 * A fake [AlertsRepository] for testing.
 *
 * @author Niall Scott
 */
class FakeAlertsRepository(
    private val onRemoveArrivalAlertWithId: (Int) -> Unit =
        { throw NotImplementedError() },
    private val onRemoveAllArrivalAlerts: () -> Unit = { throw NotImplementedError() },
    private val onRemoveProximityAlertWithId: (Int) -> Unit = { throw NotImplementedError() },
    private val onRemoveAllProximityAlerts: () -> Unit = { throw NotImplementedError() },
    private val onGetAllArrivalAlerts: () -> List<ArrivalAlert>? = { throw NotImplementedError() },
    private val onGetAllArrivalAlertStopCodes: () -> Set<String>? = { throw NotImplementedError() },
    private val onGetProximityAlert: (Int) -> ProximityAlert? = { throw NotImplementedError() },
    private val onHasArrivalAlertFlow: (String) -> Flow<Boolean> = { throw NotImplementedError() },
    private val onHasProximityAlertFlow: (String) -> Flow<Boolean> =
        { throw NotImplementedError() },
    private val onGetArrivalAlertCount: () -> Int = { throw NotImplementedError() },
    private val onGetProximityAlertCount: () -> Int = { throw NotImplementedError() },
    private val onArrivalAlertCountFlow: () -> Flow<Int> = { throw NotImplementedError() },
    private val onArrivalAlertStopCodesFlow: () -> Flow<Set<String>?> =
        { throw NotImplementedError() },
    private val onAllProximityAlertsFlow: () -> Flow<List<ProximityAlert>?> =
        { throw NotImplementedError() },
    private val onProximityAlertStopCodesFlow: () -> Flow<Set<String>?> =
        { throw NotImplementedError() },
    private val onEnsureTasksRunning: () -> Unit = { throw NotImplementedError() }
) : AlertsRepository {

    override suspend fun addArrivalAlert(request: ArrivalAlertRequest) {
        throw NotImplementedError()
    }

    override suspend fun addProximityAlert(request: ProximityAlertRequest) {
        throw NotImplementedError()
    }

    override suspend fun removeArrivalAlert(stopCode: String) {
        throw NotImplementedError()
    }

    override suspend fun removeArrivalAlert(id: Int) {
        onRemoveArrivalAlertWithId(id)
    }

    override suspend fun removeAllArrivalAlerts() {
        onRemoveAllArrivalAlerts()
    }

    override suspend fun removeProximityAlert(stopCode: String) {
        throw NotImplementedError()
    }

    override suspend fun removeProximityAlert(id: Int) {
        onRemoveProximityAlertWithId(id)
    }

    override suspend fun removeAllProximityAlerts() {
        onRemoveAllProximityAlerts()
    }

    override suspend fun getAllArrivalAlerts() = onGetAllArrivalAlerts()

    override suspend fun getAllArrivalAlertStopCodes() = onGetAllArrivalAlertStopCodes()

    override suspend fun getProximityAlert(id: Int) = onGetProximityAlert(id)

    override fun hasArrivalAlertFlow(stopCode: String) = onHasArrivalAlertFlow(stopCode)

    override fun hasProximityAlertFlow(stopCode: String) = onHasProximityAlertFlow(stopCode)

    override suspend fun getArrivalAlertCount() = onGetArrivalAlertCount()

    override suspend fun getProximityAlertCount() = onGetProximityAlertCount()

    override val arrivalAlertCountFlow get() = onArrivalAlertCountFlow()

    override val arrivalAlertStopCodesFlow get() = onArrivalAlertStopCodesFlow()

    override val allProximityAlertsFlow get() = onAllProximityAlertsFlow()

    override val proximityAlertStopCodesFlow get() = onProximityAlertStopCodesFlow()

    override val allAlertsFlow: Flow<List<Alert>?>
        get() = throw NotImplementedError()

    override fun ensureTasksRunning() {
        onEnsureTasksRunning()
    }

    override suspend fun ensureTasksRunningIfAlertsExists() {
        throw NotImplementedError()
    }
}
