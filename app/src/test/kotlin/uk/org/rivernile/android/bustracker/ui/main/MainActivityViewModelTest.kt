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

package uk.org.rivernile.android.bustracker.ui.main

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [MainActivityViewModel].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class MainActivityViewModelTest {

    companion object {

        private const val STATE_HAS_SHOWN_INITIAL_ANIMATION = "hasShownInitialAnimation"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var featureRepository: FeatureRepository

    @Test
    fun hasShownInitialAnimationReturnsFalseByDefault() {
        val viewModel = createViewModel()

        val result = viewModel.hasShownInitialAnimation

        assertFalse(result)
    }

    @Test
    fun hasShownInitialAnimationReturnsTrueWhenTrueInSavedState() {
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(
                    STATE_HAS_SHOWN_INITIAL_ANIMATION to true)))

        val result = viewModel.hasShownInitialAnimation

        assertTrue(result)
    }

    @Test
    fun hasShownInitialAnimationReturnsTrueAfterCallingOnInitialAnimationFinished() {
        val viewModel = createViewModel()

        viewModel.onInitialAnimationFinished()
        val result = viewModel.hasShownInitialAnimation

        assertTrue(result)
    }

    @Test
    fun hasShownInitialAnimationReturnsTrueWithSubsequentFinishedCallAfterTrueState() {
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(
                    STATE_HAS_SHOWN_INITIAL_ANIMATION to true)))

        viewModel.onInitialAnimationFinished()
        val result = viewModel.hasShownInitialAnimation

        assertTrue(result)
    }

    @Test
    fun hasShownInitialAnimationReturnsTrueWithSubsequentFinishedCalls() {
        val viewModel = createViewModel()

        viewModel.onInitialAnimationFinished()
        viewModel.onInitialAnimationFinished()
        val result = viewModel.hasShownInitialAnimation

        assertTrue(result)
    }

    @Test
    fun isScanMenuItemVisibleEmitsFalseWhenNoCameraFeature() {
        whenever(featureRepository.hasCameraFeature)
            .thenReturn(false)
        val viewModel = createViewModel()

        val observer = viewModel.isScanMenuItemVisibleLiveData.test()

        observer.assertValues(false)
    }

    @Test
    fun isScanMenuItemVisibleEmitsTrueWhenHasCameraFeature() {
        whenever(featureRepository.hasCameraFeature)
            .thenReturn(true)
        val viewModel = createViewModel()

        val observer = viewModel.isScanMenuItemVisibleLiveData.test()

        observer.assertValues(true)
    }

    @Test
    fun onScanMenuItemClickedShowsQrCodeScanner() {
        val viewModel = createViewModel()

        val observer = viewModel.showQrCodeScannerLiveData.test()
        viewModel.onScanMenuItemClicked()

        observer.assertSize(1)
    }

    @Test
    fun onQrScannerNotFoundShowsInstallQrScannerDialog() {
        val viewModel = createViewModel()

        val observer = viewModel.showInstallQrScannerDialogLiveData.test()
        viewModel.onQrScannerNotFound()

        observer.assertSize(1)
    }

    @Test
    fun onQrScannedWithErrorPerformsNoAction() {
        val viewModel = createViewModel()

        val showStopObserver = viewModel.showStopLiveData.test()
        val showInvalidQrCodeErrorObserver = viewModel.showInvalidQrCodeErrorLiveData.test()
        viewModel.onQrScanned(ScanQrCodeResult.Error)

        showStopObserver.assertEmpty()
        showInvalidQrCodeErrorObserver.assertEmpty()
    }

    @Test
    fun onQrScannedWithNullStopCodeShowsInvalidQrCodeError() {
        val viewModel = createViewModel()

        val showStopObserver = viewModel.showStopLiveData.test()
        val showInvalidQrCodeErrorObserver = viewModel.showInvalidQrCodeErrorLiveData.test()
        viewModel.onQrScanned(ScanQrCodeResult.Success(null))

        showStopObserver.assertEmpty()
        showInvalidQrCodeErrorObserver.assertSize(1)
    }

    @Test
    fun onQrScannedWithEmptyStopCodeShowsInvalidQrCodeError() {
        val viewModel = createViewModel()

        val showStopObserver = viewModel.showStopLiveData.test()
        val showInvalidQrCodeErrorObserver = viewModel.showInvalidQrCodeErrorLiveData.test()
        viewModel.onQrScanned(ScanQrCodeResult.Success(""))

        showStopObserver.assertEmpty()
        showInvalidQrCodeErrorObserver.assertSize(1)
    }

    @Test
    fun onQrScannedWithPopulatedStopCodeShowsStopDetails() {
        val viewModel = createViewModel()

        val showStopObserver = viewModel.showStopLiveData.test()
        val showInvalidQrCodeErrorObserver = viewModel.showInvalidQrCodeErrorLiveData.test()
        viewModel.onQrScanned(ScanQrCodeResult.Success("123456"))

        showStopObserver.assertValues("123456")
        showInvalidQrCodeErrorObserver.assertEmpty()
    }

    @Test
    fun onSettingsMenuItemClickedShowsSettings() {
        val viewModel = createViewModel()
        val observer = viewModel.showSettingsLiveData.test()

        viewModel.onSettingsMenuItemClicked()

        observer.assertSize(1)
    }

    @Test
    fun onAboutMenuItemClickedShowsAbout() {
        val viewModel = createViewModel()
        val observer = viewModel.showAboutLiveData.test()

        viewModel.onAboutMenuItemClicked()

        observer.assertSize(1)
    }

    private fun createViewModel(savedState: SavedStateHandle = SavedStateHandle()) =
        MainActivityViewModel(
            savedState,
            featureRepository)
}