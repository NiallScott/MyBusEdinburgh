/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.dagger

import dagger.Module
import dagger.Provides
import uk.org.rivernile.android.bustracker.core.alerts.AlertNotificationDispatcher
import uk.org.rivernile.android.bustracker.core.alerts.FakeAlertNotificationDispatcher
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.FakeArrivalAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.FakeGeofencingManager
import uk.org.rivernile.android.bustracker.core.alerts.proximity.FakeProximityAlertTaskLauncher
import uk.org.rivernile.android.bustracker.core.alerts.proximity.GeofencingManager
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertTaskLauncher

/**
 * A module for providing fake implementations of alert resources.
 *
 * @param arrivalAlertTaskLauncher An alternative [ArrivalAlertTaskLauncher] implementation.
 * Defaults to fake implementation.
 * @param proximityAlertTaskLauncher An alternative [ProximityAlertTaskLauncher] implementation.
 * Defaults to fake implementation.
 * @param geofencingManager An alternative [GeofencingManager] implementation. Defaults to fake
 * implementation.
 * @param alertNotificationDispatcher An alternative [AlertNotificationDispatcher] implementation.
 * Defaults to fake implementation.
 * @author Niall Scott
 */
@Module
class FakeAlertsModule(
        private val arrivalAlertTaskLauncher: ArrivalAlertTaskLauncher =
                FakeArrivalAlertTaskLauncher(),
        private val proximityAlertTaskLauncher: ProximityAlertTaskLauncher =
                FakeProximityAlertTaskLauncher(),
        private val geofencingManager: GeofencingManager = FakeGeofencingManager(),
        private val alertNotificationDispatcher: AlertNotificationDispatcher =
                FakeAlertNotificationDispatcher()) {

    @Provides
    fun provideArrivalAlertTaskLauncher() = arrivalAlertTaskLauncher

    @Provides
    fun provideProximityAlertTaskLauncher() = proximityAlertTaskLauncher

    @Provides
    fun provideGeofencingManager() = geofencingManager

    @Provides
    fun provideAlertNotificationDispatcher() = alertNotificationDispatcher
}