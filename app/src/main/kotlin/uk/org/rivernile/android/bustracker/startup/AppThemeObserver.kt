/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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

import android.app.UiModeManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatDelegate
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
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
 */
sealed interface AppThemeObserver {

    /**
     * Observe the app theme setting and respond to changes by applying the new theme.
     */
    fun observeAppTheme()
}

/**
 * This is the legacy implementation of [AppThemeObserver].
 *
 * @param preferenceRepository Used to retriever the app theme setting.
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @param mainDispatcher The main-thread [CoroutineDispatcher].
 * @author Niall Scott
 */
class LegacyAppThemeObserver @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @ForMainDispatcher private val mainDispatcher: CoroutineDispatcher) : AppThemeObserver {

    override fun observeAppTheme() {
        applicationCoroutineScope.launch(mainDispatcher) {
            preferenceRepository.appThemeFlow
                .map(this@LegacyAppThemeObserver::mapToUiMode)
                .distinctUntilChanged()
                .flowOn(defaultDispatcher)
                .collect {
                    AppCompatDelegate.setDefaultNightMode(it)
                }
        }
    }

    /**
     * Map the given [AppTheme] to the correct `MODE_*` constant value.
     *
     * @param appTheme The [AppTheme] to map.
     * @return The correct `MODE_*` for the [AppTheme].
     */
    private fun mapToUiMode(appTheme: AppTheme): Int {
        return when (appTheme) {
            AppTheme.SYSTEM_DEFAULT -> AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
            AppTheme.LIGHT -> AppCompatDelegate.MODE_NIGHT_NO
            AppTheme.DARK -> AppCompatDelegate.MODE_NIGHT_YES
        }
    }
}

/**
 * This is the Android S+ (API Level 31+) implementation of [AppThemeObserver].
 *
 * @param uiModeManager Used to change the UI mode.
 * @param preferenceRepository Used to retriever the app theme setting.
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @param mainDispatcher The main-thread [CoroutineDispatcher].
 * @author Niall Scott
 */
@RequiresApi(Build.VERSION_CODES.S)
class V31AppThemeObserver @Inject constructor(
    private val uiModeManager: UiModeManager,
    private val preferenceRepository: PreferenceRepository,
    @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @ForMainDispatcher private val mainDispatcher: CoroutineDispatcher) : AppThemeObserver {

    override fun observeAppTheme() {
        applicationCoroutineScope.launch(mainDispatcher) {
            preferenceRepository.appThemeFlow
                .map(this@V31AppThemeObserver::mapToUiMode)
                .distinctUntilChanged()
                .flowOn(defaultDispatcher)
                .collect {
                    uiModeManager.setApplicationNightMode(it)
                }
        }
    }

    /**
     * Map the given [AppTheme] to the correct `MODE_*` constant value.
     *
     * @param appTheme The [AppTheme] to map.
     * @return The correct `MODE_*` for the [AppTheme].
     */
    private fun mapToUiMode(appTheme: AppTheme): Int {
        return when (appTheme) {
            AppTheme.SYSTEM_DEFAULT -> UiModeManager.MODE_NIGHT_AUTO
            AppTheme.LIGHT -> UiModeManager.MODE_NIGHT_NO
            AppTheme.DARK -> UiModeManager.MODE_NIGHT_YES
        }
    }
}
