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
import android.app.backup.BackupManager
import android.content.Context
import android.content.SharedPreferences
import com.bugsense.trace.BugSenseHandler
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import uk.org.rivernile.android.bustracker.alerts.AlertManager
import uk.org.rivernile.android.bustracker.core.startup.StartUpTask
import uk.org.rivernile.android.bustracker.dagger.DaggerApplicationComponent
import uk.org.rivernile.android.bustracker.endpoints.BusTrackerEndpoint
import uk.org.rivernile.android.bustracker.endpoints.TwitterEndpoint
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import uk.org.rivernile.edinburghbustracker.android.ApiKey
import uk.org.rivernile.edinburghbustracker.android.BuildConfig
import javax.inject.Inject

/**
 * This code is the very first code that will be executed when the application is started. It is
 * used to register the BugSense handler, put a listener on the [SharedPreferences] for Google
 * Backup, and check for bus stop database updates.
 *
 * The Android developer documentation discourages the usage of this class, but as it is
 * unpredictable where the user will enter the application the code is put here as this class is
 * always instantiated when this application's process is created.
 *
 * @author Niall Scott
 */
abstract class BusApplication : Application(), HasAndroidInjector,
        SharedPreferences.OnSharedPreferenceChangeListener {

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    @Inject
    lateinit var startUpTask: StartUpTask
    @Inject
    lateinit var preferenceManager: PreferenceManager
    @Inject
    lateinit var alertManager: AlertManager

    override fun onCreate() {
        super.onCreate()

        DaggerApplicationComponent.factory()
                .newApplicationComponent(this)
                .inject(this)

        // Register the BugSense handler.
        if (BuildConfig.BUGSENSE_ENABLED) {
            BugSenseHandler.initAndStartSession(this, ApiKey.BUGSENSE_KEY)
        }

        getSharedPreferences(PreferenceManager.PREF_FILE, Context.MODE_PRIVATE)
                .registerOnSharedPreferenceChangeListener(this)

        startUpTask.performStartUpTasks()
    }

    override fun androidInjector() = dispatchingAndroidInjector

    override fun onSharedPreferenceChanged(sp: SharedPreferences, key: String) {
        BackupManager.dataChanged(packageName)
    }

    /**
     * Get the bus tracker endpoint.
     *
     * @return The BusTrackerEndpoint instance for this application.
     */
    abstract fun getBusTrackerEndpoint(): BusTrackerEndpoint

    /**
     * Get the Twitter endpoint, used for loading a list of Tweets to show the
     * user updates.
     *
     * @return The TwitterEndpoint instance for this application.
     */
    abstract fun getTwitterEndpoint(): TwitterEndpoint
}