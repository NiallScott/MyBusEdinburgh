/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.startup

import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForMainDispatcher
import uk.org.rivernile.android.bustracker.core.preferences.AppTheme
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import javax.inject.Inject

/**
 * The [observeAppTheme] method observes the app theme preference setting and responds to this
 * setting changing to apply the new theme.
 *
 * @param preferenceRepository Used to retriever the app theme setting.
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @param mainDispatcher The main-thread [CoroutineDispatcher].
 * @author Niall Scott
 */
class AppThemeObserver @Inject constructor(
        private val preferenceRepository: PreferenceRepository,
        @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
        @ForMainDispatcher private val mainDispatcher: CoroutineDispatcher) {

    /**
     * Observe the app theme setting and respond to changes by applying the new theme.
     */
    fun observeAppTheme() {
        applicationCoroutineScope.launch(mainDispatcher) {
            preferenceRepository.appThemeFlow
                    .flowOn(defaultDispatcher)
                    .collect {
                        when (it) {
                            AppTheme.SYSTEM_DEFAULT -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                            AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
                            AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
                        }.let(AppCompatDelegate::setDefaultNightMode)
                    }
        }
    }
}