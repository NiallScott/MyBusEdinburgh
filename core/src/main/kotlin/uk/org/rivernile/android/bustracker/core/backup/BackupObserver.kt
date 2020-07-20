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

package uk.org.rivernile.android.bustracker.core.backup

import uk.org.rivernile.android.bustracker.core.database.settings.daos.FavouritesDao
import uk.org.rivernile.android.bustracker.core.preferences.OnPreferenceChangedListener
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceKey
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceListener
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This class observes relevant data sources and signals a backup should happen when user data
 * changes.
 *
 * @param favouritesDao The [FavouritesDao] to access user favourite stops.
 * @param preferenceManager The [PreferenceManager].
 * @param backupInvoker A platform specific implementation to signal a backup should occur.
 * @author Niall Scott
 */
@Singleton
class BackupObserver @Inject constructor(
        private val favouritesDao: FavouritesDao,
        private val preferenceManager: PreferenceManager,
        private val backupInvoker: BackupInvoker) {

    /**
     * Begin observing data sources and cause a backup to happen when data changes.
     */
    fun beginObserving() {
        favouritesDao.addOnFavouritesChangedListener(favouritesChangedListener)
        PreferenceListener(preferencesChangedListener, null)
                .let(preferenceManager::addOnPreferenceChangedListener)
    }

    /**
     * A single entry point to signal data has changed.
     */
    private fun onDataChanged() {
        backupInvoker.performBackup()
    }

    private val favouritesChangedListener = object : FavouritesDao.OnFavouritesChangedListener {
        override fun onFavouritesChanged() {
            onDataChanged()
        }
    }

    private val preferencesChangedListener = object : OnPreferenceChangedListener {
        override fun onPreferenceChanged(preference: PreferenceKey?) {
            onDataChanged()
        }
    }
}