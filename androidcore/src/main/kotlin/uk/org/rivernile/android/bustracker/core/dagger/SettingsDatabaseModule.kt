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

import android.content.Context
import dagger.Module
import dagger.Provides
import uk.org.rivernile.android.bustracker.core.dagger.qualifiers.ForSettingsDatabase
import uk.org.rivernile.android.bustracker.core.database.settings.AlertsContract
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AlertsDao
import uk.org.rivernile.android.bustracker.core.database.settings.daos.AndroidAlertsDao
import javax.inject.Singleton

/**
 * This is a Dagger [Module] to provide dependencies for the settings database.
 *
 * @author Niall Scott
 */
@Module
internal class SettingsDatabaseModule {

    /**
     * Provide the settings database authority URI [String].
     *
     * @param context The application [Context].
     * @return The settings database authority URI [String].
     */
    @Provides
    @Singleton
    @ForSettingsDatabase
    fun provideAuthority(context: Context) = "${context.packageName}.provider.settings"

    /**
     * Provide the [AlertsDao].
     *
     * @param context The application [Context].
     * @param contract The table contract for the alerts table.
     * @return The [AlertsDao].
     */
    @Provides
    @Singleton
    fun provideAlertsDao(context: Context,
                         contract: AlertsContract): AlertsDao =
            AndroidAlertsDao(context, contract)
}