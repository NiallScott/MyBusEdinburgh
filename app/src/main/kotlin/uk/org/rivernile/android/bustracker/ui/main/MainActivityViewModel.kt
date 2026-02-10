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
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import javax.inject.Inject

/**
 * This is the [ViewModel] for [MainActivity].
 *
 * @author Niall Scott
 */
@HiltViewModel
class MainActivityViewModel @Inject constructor(
    private val savedState: SavedStateHandle,
    featureRepository: FeatureRepository) : ViewModel() {

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
     * This [LiveData] emits when the stop details should be shown.
     */
    val showStopLiveData: LiveData<StopIdentifier> get() = showStop
    private val showStop = SingleLiveEvent<StopIdentifier>()

    /**
     * This [LiveData] emits the current visibility status of the scan menu item.
     */
    val isScanMenuItemVisibleLiveData: LiveData<Boolean> =
        MutableLiveData(featureRepository.hasCameraFeature)

    /**
     * This [LiveData] emits when the QR code scanner should be shown.
     */
    val showQrCodeScannerLiveData: LiveData<Unit> get() = showQrCodeScanner
    private val showQrCodeScanner = SingleLiveEvent<Unit>()

    /**
     * This [LiveData] emits when the install QR scanner dialog should be shown.
     */
    val showInstallQrScannerDialogLiveData: LiveData<Unit> get() = showInstallQrScannerDialog
    private val showInstallQrScannerDialog = SingleLiveEvent<Unit>()

    /**
     * This [LiveData] emits when the invalid QR code error should be shown.
     */
    val showInvalidQrCodeErrorLiveData: LiveData<Unit> get() = showInvalidQrCodeError
    private val showInvalidQrCodeError = SingleLiveEvent<Unit>()

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
     * This is called when the scan menu item has been clicked.
     */
    fun onScanMenuItemClicked() {
        showQrCodeScanner.call()
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

    /**
     * This is called when the QR scanner application was not found.
     */
    fun onQrScannerNotFound() {
        showInstallQrScannerDialog.call()
    }

    /**
     * This is called when the QR code has been scanned, with the resulting stop code.
     *
     * @param result The result from scanning the QR code.
     */
    fun onQrScanned(result: ScanQrCodeResult) {
        if (result is ScanQrCodeResult.Success) {
            result.stopCode
                ?.ifBlank { null }
                ?.let {
                    showStop.value = it.toNaptanStopIdentifier()
                }
                ?: showInvalidQrCodeError.call()
        }
    }
}
