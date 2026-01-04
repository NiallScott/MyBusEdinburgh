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

package uk.org.rivernile.android.bustracker.core.database.settings.alerts

import kotlinx.coroutines.flow.Flow

/**
 * A fake [AlertsDao] for testing.
 *
 * @author Niall Scott
 */
class FakeAlertsDao(
    private val onAddArrivalAlert: (ArrivalAlertEntity) -> Unit = { throw NotImplementedError() },
    private val onAddProximityAlert: (ProximityAlertEntity) -> Unit =
        { throw NotImplementedError() },
    private val onRemoveArrivalAlertById: (Int) -> Unit = { throw NotImplementedError() },
    private val onRemoveArrivalAlertByStopCode: (String) -> Unit = { throw NotImplementedError() },
    private val onRemoveAllArrivalAlerts: () -> Unit = { throw NotImplementedError() },
    private val onRemoveProximityAlertById: (Int) -> Unit = { throw NotImplementedError() },
    private val onRemoveProximityAlertByStopCode: (String) -> Unit =
        { throw NotImplementedError() },
    private val onRemoveAllProximityAlerts: () -> Unit = { throw NotImplementedError() },
    private val onGetHasArrivalAlertFlow: (String) -> Flow<Boolean> =
        { throw NotImplementedError() },
    private val onGetHasProximityAlertFlow: (String) -> Flow<Boolean> =
        { throw NotImplementedError() },
    private val onAllAlertsFlow: () -> Flow<List<AlertEntity>?> = { throw NotImplementedError() },
    private val onGetProximityAlert: (Int) -> ProximityAlertEntity? =
        { throw NotImplementedError() },
    private val onGetAllArrivalAlerts: () -> List<ArrivalAlertEntity>? =
        { throw NotImplementedError() },
    private val onGetAllArrivalAlertStopCodes: () -> List<String>? =
        { throw NotImplementedError() },
    private val onGetArrivalAlertCount: () -> Int = { throw NotImplementedError() },
    private val onArrivalAlertStopCodesFlow: () -> Flow<List<String>?> =
        { throw NotImplementedError() },
    private val onArrivalAlertCountFlow: () -> Flow<Int> = { throw NotImplementedError() },
    private val onProximityAlertStopCodesFlow: () -> Flow<List<String>?> =
        { throw NotImplementedError() },
    private val onAllProximityAlertsFlow: () -> Flow<List<ProximityAlertEntity>?> =
        { throw NotImplementedError() },
    private val onGetProximityAlertCount: () -> Int = { throw NotImplementedError() }
) : AlertsDao {

    override suspend fun addArrivalAlert(arrivalAlert: ArrivalAlertEntity) {
        onAddArrivalAlert(arrivalAlert)
    }

    override suspend fun addProximityAlert(proximityAlert: ProximityAlertEntity) {
        onAddProximityAlert(proximityAlert)
    }

    override suspend fun removeArrivalAlert(id: Int) {
        onRemoveArrivalAlertById(id)
    }

    override suspend fun removeArrivalAlert(stopCode: String) {
        onRemoveArrivalAlertByStopCode(stopCode)
    }

    override suspend fun removeAllArrivalAlerts() {
        onRemoveAllArrivalAlerts()
    }

    override suspend fun removeProximityAlert(id: Int) {
        onRemoveProximityAlertById(id)
    }

    override suspend fun removeProximityAlert(stopCode: String) {
        onRemoveProximityAlertByStopCode(stopCode)
    }

    override suspend fun removeAllProximityAlerts() {
        onRemoveAllProximityAlerts()
    }

    override fun getHasArrivalAlertFlow(stopCode: String) = onGetHasArrivalAlertFlow(stopCode)

    override fun getHasProximityAlertFlow(stopCode: String) = onGetHasProximityAlertFlow(stopCode)

    override val allAlertsFlow get() = onAllAlertsFlow()

    override suspend fun getProximityAlert(id: Int) = onGetProximityAlert(id)

    override suspend fun getAllArrivalAlerts() = onGetAllArrivalAlerts()

    override suspend fun getAllArrivalAlertStopCodes() = onGetAllArrivalAlertStopCodes()

    override suspend fun getArrivalAlertCount() = onGetArrivalAlertCount()

    override val arrivalAlertStopCodesFlow get() = onArrivalAlertStopCodesFlow()

    override val arrivalAlertCountFlow get() = onArrivalAlertCountFlow()

    override val proximityAlertStopCodesFlow get() = onProximityAlertStopCodesFlow()

    override val allProximityAlertsFlow get() = onAllProximityAlertsFlow()

    override suspend fun getProximityAlertCount() = onGetProximityAlertCount()
}
