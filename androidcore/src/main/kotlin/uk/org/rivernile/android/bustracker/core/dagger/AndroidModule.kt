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

package uk.org.rivernile.android.bustracker.core.dagger

import android.app.Application
import android.app.job.JobScheduler
import android.content.Context
import android.content.SharedPreferences
import dagger.Module
import dagger.Provides
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import javax.inject.Singleton

/**
 * This [Module] provides resources owned by the Android platform.
 *
 * @author Niall Scott
 */
@Module
class AndroidModule {

    /**
     * Provide the [Application] [Context] to Dagger.
     *
     * @param application The [Application] instance.
     * @return The [Application] [Context].
     */
    @Provides
    fun provideApplicationContext(application: Application): Context = application

    /**
     * Provide the [JobScheduler].
     *
     * @param context The application [Context].
     * @return The [JobScheduler] instance.
     */
    @Provides
    @Singleton
    fun provideJobScheduler(context: Context): JobScheduler =
            context.getSystemService(Context.JOB_SCHEDULER_SERVICE) as JobScheduler

    /**
     * Provide the [SharedPreferences].
     *
     * @param context The application [Context].
     * @return The [SharedPreferences] instance.
     */
    @Provides
    @Singleton
    fun provideSharedPreferences(context: Context): SharedPreferences =
            context.getSharedPreferences(PreferenceManager.PREF_FILE, Context.MODE_PRIVATE)
}