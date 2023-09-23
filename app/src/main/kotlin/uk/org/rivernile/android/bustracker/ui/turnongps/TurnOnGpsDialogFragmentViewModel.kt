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

package uk.org.rivernile.android.bustracker.ui.turnongps

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is the [ViewModel] for [TurnOnGpsDialogFragment].
 *
 * @param preferenceRepository The preference repository.
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@HiltViewModel
class TurnOnGpsDialogFragmentViewModel @Inject constructor(
    private val preferenceRepository: PreferenceRepository,
    @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) : ViewModel() {

    /**
     * This [LiveData] is fired when the user should be presented with the system location settings
     * UI.
     */
    val showSystemLocationSettingsLiveData: LiveData<Unit> get() = showSystemLocationSettings
    private val showSystemLocationSettings = SingleLiveEvent<Unit>()

    /**
     * This is called when the "Do not remind me again" checkbox check status changes.
     *
     * @param isChecked `true` when the user does not want to be reminded again, otherwise `false`.
     */
    fun onDoNotRemindCheckChanged(isChecked: Boolean) {
        applicationCoroutineScope.launch(defaultDispatcher) {
            preferenceRepository.setIsGpsPromptDisabled(isChecked)
        }
    }

    /**
     * This is called when the user clicks on the "Yes" button.
     */
    fun onYesClicked() {
        showSystemLocationSettings.call()
    }
}