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

package uk.org.rivernile.android.bustracker.ui.alerts.time

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [PermissionsTracker].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
class PermissionsTrackerTest {

    companion object {

        private const val STATE_REQUESTED_PERMISSIONS = "requestedPermissions"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun permissionsStateFlowEmitsPermissionUngrantedByDefault() = runTest {
        val permissionsTracker = createPermissionsTracker()

        val observer = permissionsTracker.permissionsStateFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(PermissionsState(PermissionState.UNGRANTED))
    }

    @Test
    fun permissionsStateFlowEmitsPermissionGrantedWhenUiPermissionIsGranted() = runTest {
        val permissionsTracker = createPermissionsTracker()
        permissionsTracker.permissionsState = UiPermissionsState(true)

        val observer = permissionsTracker.permissionsStateFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(PermissionsState(PermissionState.GRANTED))
    }

    @Test
    fun permissionsStateFlowEmitsPermissionUngrantedWhenUiPermissionIsNotGranted() = runTest {
        val permissionsTracker = createPermissionsTracker()
        permissionsTracker.permissionsState = UiPermissionsState(false)

        val observer = permissionsTracker.permissionsStateFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(PermissionsState(PermissionState.UNGRANTED))
    }

    @Test
    fun permissionsStateFlowEmitsPermissionDeniedDefaultHasAskedForPermission() = runTest {
        val permissionsTracker = createPermissionsTracker(
                SavedStateHandle(
                        mapOf(STATE_REQUESTED_PERMISSIONS to true)))

        val observer = permissionsTracker.permissionsStateFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(PermissionsState(PermissionState.DENIED))
    }

    @Test
    fun permissionsStateFlowEmitsPermissionGrantedWhenGrantedAndHasAskedForPermission() = runTest {
        val permissionsTracker = createPermissionsTracker(
                SavedStateHandle(
                        mapOf(STATE_REQUESTED_PERMISSIONS to true)))
        permissionsTracker.permissionsState = UiPermissionsState(true)

        val observer = permissionsTracker.permissionsStateFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(PermissionsState(PermissionState.GRANTED))
    }

    @Test
    fun permissionsStateFlowEmitsPermissionDeniedWhenUngrantedAndHasAskedForPermission() = runTest {
        val permissionsTracker = createPermissionsTracker(
                SavedStateHandle(
                        mapOf(STATE_REQUESTED_PERMISSIONS to true)))
        permissionsTracker.permissionsState = UiPermissionsState(false)

        val observer = permissionsTracker.permissionsStateFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(PermissionsState(PermissionState.DENIED))
    }

    @Test
    fun onRequestPermissionsClickedRequestsPermissionsWhenPermissionsRequested() {
        val permissionsTracker = createPermissionsTracker()

        val observer = permissionsTracker.requestPermissionsLiveData.test()
        permissionsTracker.onRequestPermissionsClicked()

        observer.assertSize(1)
    }

    @Test
    fun onRequestPermissionsClickedRequestsPermissionsOnceOnlyWhenPermissionsRequested() {
        val permissionsTracker = createPermissionsTracker()

        val observer = permissionsTracker.requestPermissionsLiveData.test()
        permissionsTracker.onRequestPermissionsClicked()
        permissionsTracker.onRequestPermissionsClicked()

        observer.assertSize(1)
    }

    @Test
    fun onRequestPermissionsClickedDoesNotRequestPermissionsWhenPreviouslyRequested() {
        val permissionsTracker = createPermissionsTracker(
                SavedStateHandle(
                        mapOf(STATE_REQUESTED_PERMISSIONS to true)))

        val observer = permissionsTracker.requestPermissionsLiveData.test()
        permissionsTracker.onRequestPermissionsClicked()

        observer.assertEmpty()
    }

    private fun createPermissionsTracker(savedState: SavedStateHandle = SavedStateHandle()) =
            PermissionsTracker(savedState)
}