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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [MainActivityViewModel].
 *
 * @author Niall Scott
 */
class MainActivityViewModelTest {

    companion object {

        private const val STATE_HAS_SHOWN_INITIAL_ANIMATION = "hasShownInitialAnimation"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

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
        MainActivityViewModel(savedState)
}
