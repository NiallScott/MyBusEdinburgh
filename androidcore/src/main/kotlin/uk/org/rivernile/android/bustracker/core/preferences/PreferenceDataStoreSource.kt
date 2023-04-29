/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.preferences

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.MutablePreferences
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import kotlinx.coroutines.flow.catch
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import java.io.IOException
import javax.inject.Inject

/**
 * This class is used as the base and common way to access the preferences [DataStore].
 *
 * @param dataStore The actual [DataStore].
 * @param exceptionLogger Used to log handled exceptions.
 * @author Niall Scott
 */
class PreferenceDataStoreSource @Inject internal constructor(
    private val dataStore: DataStore<Preferences>,
    private val exceptionLogger: ExceptionLogger) {

    /**
     * This [kotlinx.coroutines.flow.Flow] should be used as the base to consume preferences. It
     * handles error states so that downstream doesn't have to.
     */
    val preferencesFlow get() = dataStore
        .data
        .catch { exception ->
            exceptionLogger.log(exception)

            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }

    /**
     * This method safely edits the preferences, by handling error conditions. If an error is
     * encountered, it is silently logged and otherwise ignored as there is no handling path for
     * the user.
     *
     * @param transform A lambda which is executed when the preferences are edited and passed in a
     * [MutablePreferences] instance.
     */
    suspend fun edit(transform: suspend (MutablePreferences) -> Unit) {
        try {
            dataStore.edit(transform)
        } catch (e: IOException) {
            exceptionLogger.log(e)
        }
    }
}