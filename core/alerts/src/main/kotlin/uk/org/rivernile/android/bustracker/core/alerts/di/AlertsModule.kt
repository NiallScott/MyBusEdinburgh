/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts.di

import dagger.Binds
import dagger.Module
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.RealAlertsRepository
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.CheckTimesTask
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.RealCheckTimesTask
import uk.org.rivernile.android.bustracker.core.alerts.proximity.AreaEnteredHandler
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertTracker
import uk.org.rivernile.android.bustracker.core.alerts.proximity.RealAreaEnteredHandler
import uk.org.rivernile.android.bustracker.core.alerts.proximity.RealProximityAlertTracker

/**
 * A [Module] for providing alerts dependencies.
 *
 * @author Niall Scott
 */
@Module(
    includes = [
        AlertsModule.Bindings::class
    ]
)
public class AlertsModule {

    @Module
    internal interface Bindings {

        @Suppress("unused")
        @Binds
        fun bindAlertsRepository(realAlertsRepository: RealAlertsRepository): AlertsRepository

        @Suppress("unused")
        @Binds
        fun bindAreaEnteredHandler(
            realAreaEnteredHandler: RealAreaEnteredHandler
        ): AreaEnteredHandler

        @Suppress("unused")
        @Binds
        fun bindCheckTimesTask(realCheckTimesTask: RealCheckTimesTask): CheckTimesTask

        @Suppress("unused")
        @Binds
        fun bindProximityAlertTracker(
            realProximityAlertTracker: RealProximityAlertTracker
        ): ProximityAlertTracker
    }
}