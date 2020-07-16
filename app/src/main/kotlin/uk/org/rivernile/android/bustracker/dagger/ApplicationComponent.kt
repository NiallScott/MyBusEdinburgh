/*
 * Copyright (C) 2018 - 2020 Niall 'Rivernile' Scott
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
 */

package uk.org.rivernile.android.bustracker.dagger

import android.app.Application
import dagger.BindsInstance
import dagger.Component
import dagger.android.AndroidInjectionModule
import uk.org.rivernile.android.bustracker.BusApplication
import uk.org.rivernile.android.bustracker.core.dagger.CoreModule
import javax.inject.Singleton

/**
 * This [Component] is the root component of the application.
 *
 * @author Niall Scott
 */
@Singleton
@Component(modules = [
        AndroidInjectionModule::class,
        ApplicationModule::class,
        CoreModule::class,
        ActivityModule::class
])
interface ApplicationComponent {

    /**
     * Inject an instance of the application-specific [Application] instance.
     *
     * @param application The [Application] instance to inject.
     */
    fun inject(application: BusApplication)

    /**
     * @see [Component.Factory].
     */
    @Component.Factory
    interface Factory {

        /**
         * Create a new [ApplicationComponent].
         *
         * @param application The Android [Application] instance.
         * @return A new instance of [ApplicationComponent].
         */
        fun newApplicationComponent(
                @BindsInstance application: Application): ApplicationComponent
    }
}