/*
 * Copyright (C) 2019 Niall 'Rivernile' Scott
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

import uk.org.rivernile.android.bustracker.core.database.settings.entities.ArrivalAlert
import uk.org.rivernile.android.bustracker.core.database.settings.entities.ProximityAlert

/**
 * This DAO is used to access alerts created in the app.
 *
 * @author Niall Scott
 */
interface AlertsDao {

    /**
     * Add a new arrival alert to the database.
     *
     * @param arrivalAlert The alert to add.
     */
    fun addArrivalAlert(arrivalAlert: ArrivalAlert)

    /**
     * Add a new proximity alert to the database.
     *
     * @param proximityAlert The alert to add.
     */
    fun addProximityAlert(proximityAlert: ProximityAlert)

    /**
     * Remove an arrival alert.
     *
     * @param id The ID of the arrival alert to remove.
     */
    fun removeArrivalAlert(id: Int)

    /**
     * Remove a proximity alert.
     *
     * @param id The ID of the proximity alert to remove.
     */
    fun removeProximityAlert(id: Int)
}