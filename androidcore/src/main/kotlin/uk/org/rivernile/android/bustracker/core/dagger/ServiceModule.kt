/*
 * Copyright (C) 2019 - 2020 Niall 'Rivernile' Scott
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
import dagger.android.ContributesAndroidInjector
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRunnerService
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertRunnerService
import uk.org.rivernile.android.bustracker.core.database.busstop.DatabaseUpdateJobService

/**
 * This [Module] is used to inject [android.app.Service] instances in this application.
 *
 * @author Niall Scott
 */
@Suppress("unused")
@Module
internal abstract class ServiceModule {

    /**
     * Presents an instance of [ArrivalAlertRunnerService] as an item to be injected.
     *
     * @return An instance of [ArrivalAlertRunnerService] to be injected.
     */
    @ContributesAndroidInjector
    abstract fun contributeArrivalAlertRunnerService(): ArrivalAlertRunnerService

    /**
     * Presents an instance of [DatabaseUpdateJobService] as an item to be injected.
     *
     * @return An instance of [DatabaseUpdateJobService] to be injected.
     */
    @ContributesAndroidInjector
    abstract fun contributeDatabaseUpdateJobService(): DatabaseUpdateJobService

    /**
     * Presents an instance of [ProximityAlertRunnerService] as an item to be injected.
     *
     * @return An instance of [ProximityAlertRunnerService] to be injected.
     */
    @ContributesAndroidInjector
    abstract fun contributeProximityAlertRunnerService(): ProximityAlertRunnerService
}