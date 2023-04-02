/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.database.settings.daos

import uk.org.rivernile.android.bustracker.core.database.settings.entities.Alert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert

/**
 * A fake implementation of [AlertsDao].
 *
 * @author Niall Scott
 */
class FakeAlertsDao : AlertsDao {

    override fun addOnAlertsChangedListener(listener: AlertsDao.OnAlertsChangedListener) {

    }

    override fun removeOnAlertsChangedListener(listener: AlertsDao.OnAlertsChangedListener) {

    }

    override suspend fun addArrivalAlert(arrivalAlert: ArrivalAlert) = 0L

    override suspend fun addProximityAlert(proximityAlert: ProximityAlert) {

    }

    override suspend fun removeArrivalAlert(id: Int) {

    }

    override suspend fun removeArrivalAlert(stopCode: String) {

    }

    override suspend fun removeAllArrivalAlerts() {

    }

    override suspend fun removeProximityAlert(id: Int) {

    }

    override suspend fun removeProximityAlert(stopCode: String) {

    }

    override suspend fun removeAllProximityAlerts() {

    }

    override suspend fun getAllAlerts(): List<Alert>? = null

    override suspend fun getProximityAlert(id: Int): ProximityAlert? = null

    override suspend fun getAllArrivalAlerts(): List<ArrivalAlert>? = null

    override suspend fun getAllArrivalAlertStopCodes(): Set<String>? = null

    override suspend fun getArrivalAlertCount() = 0

    override fun getAllProximityAlerts(): List<ProximityAlert>? = null

    override suspend fun getProximityAlertCount() = 0

    override suspend fun hasArrivalAlert(stopCode: String) = false

    override suspend fun hasProximityAlert(stopCode: String) = false
}