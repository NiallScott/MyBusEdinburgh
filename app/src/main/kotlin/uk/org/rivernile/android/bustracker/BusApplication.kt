/*
 * Copyright (C) 2009 - 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker

import android.app.Application
import android.content.Context
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import uk.org.rivernile.android.bustracker.core.startup.StartUpTask
import uk.org.rivernile.android.bustracker.dagger.DaggerApplicationComponent
import javax.inject.Inject

/**
 * This code is the very first code that will be executed when the application is started. It is
 * used to register the BugSense handler and check for bus stop database updates.
 *
 * The Android developer documentation discourages the usage of this class, but as it is
 * unpredictable where the user will enter the application the code is put here as this class is
 * always instantiated when this application's process is created.
 *
 * @author Niall Scott
 */
class BusApplication : Application(), HasAndroidInjector {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    @Inject
    lateinit var startUpTask: StartUpTask

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)

        DaggerApplicationComponent.factory()
                .newApplicationComponent(this)
                .inject(this)
    }

    override fun onCreate() {
        super.onCreate()

        startUpTask.performStartUpTasks()
    }

    override fun androidInjector() = dispatchingAndroidInjector
}