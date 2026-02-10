/*
 * Copyright (C) 2022 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.main

import androidx.lifecycle.LiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is the [ViewModel] for [MainActivity].
 *
 * @author Niall Scott
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val savedState: SavedStateHandle
) : ViewModel() {

    companion object {

        private const val STATE_HAS_SHOWN_INITIAL_ANIMATION = "hasShownInitialAnimation"
    }

    /**
     * This property exposes if the initial animation has been shown.
     */
    var hasShownInitialAnimation: Boolean
        get() = savedState[STATE_HAS_SHOWN_INITIAL_ANIMATION] ?: false
        private set(value) {
            savedState[STATE_HAS_SHOWN_INITIAL_ANIMATION] = value
        }

    /**
     * This [LiveData] emits when settings should be shown.
     */
    val showSettingsLiveData: LiveData<Unit> get() = showSettings
    private val showSettings = SingleLiveEvent<Unit>()

    /**
     * This [LiveData] emits when about should be shown.
     */
    val showAboutLiveData: LiveData<Unit> get() = showAbout
    private val showAbout = SingleLiveEvent<Unit>()

    /**
     * This is called when the initial animation has finished.
     */
    fun onInitialAnimationFinished() {
        hasShownInitialAnimation = true
    }

    /**
     * This is called when the settings menu item has been clicked.
     */
    fun onSettingsMenuItemClicked() {
        showSettings.call()
    }

    /**
     * This is called when the about menu item has been clicked.
     */
    fun onAboutMenuItemClicked() {
        showAbout.call()
    }
}
