/*
 * Copyright (C) 2023 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.alerts.proximity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.testutils.test
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [PermissionsTracker].
 *
 * @author Niall Scott
 */
class PermissionsTrackerTest {

    companion object {

        private const val STATE_REQUESTED_PERMISSIONS = "requestedPermissions"
        private const val STATE_REQUESTED_BACKGROUND_LOCATION_PERMISSION =
            "requestedBackgroundLocationPermission"
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Test
    fun permissionsStateFlowEmitsPermissionUngrantedByDefault() = runTest {
        val permissionsTracker = createPermissionsTracker()

        permissionsTracker.permissionsStateFlow.test {
            assertEquals(PermissionState.UNGRANTED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun permissionsStateFlowEmitsPermissionGrantedWhenUiPermissionIsGranted() = runTest {
        val permissionsTracker = createPermissionsTracker()
        permissionsTracker.permissionsState = UiPermissionsState(
            hasCoarseLocationPermission = true,
            hasFineLocationPermission = true,
            hasPostNotificationsPermission = true
        )

        permissionsTracker.permissionsStateFlow.test {
            assertEquals(PermissionState.GRANTED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun permissionsStateFlowEmitsPermissionUngrantedWhenUiPermissionIsNotGranted() = runTest {
        val permissionsTracker = createPermissionsTracker()
        permissionsTracker.permissionsState = UiPermissionsState(
            hasCoarseLocationPermission = false,
            hasFineLocationPermission = false,
            hasPostNotificationsPermission = false
        )

        permissionsTracker.permissionsStateFlow.test {
            assertEquals(PermissionState.UNGRANTED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun permissionsStateFlowEmitsPermissionUngrantedWhenOnlyCoarseLocationGranted() = runTest {
        val permissionsTracker = createPermissionsTracker()
        permissionsTracker.permissionsState = UiPermissionsState(
            hasCoarseLocationPermission = true,
            hasFineLocationPermission = false,
            hasPostNotificationsPermission = false
        )

        permissionsTracker.permissionsStateFlow.test {
            assertEquals(PermissionState.UNGRANTED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun permissionsStateFlowEmitsPermissionUngrantedWhenOnlyFineLocationGranted() = runTest {
        val permissionsTracker = createPermissionsTracker()
        permissionsTracker.permissionsState = UiPermissionsState(
            hasCoarseLocationPermission = false,
            hasFineLocationPermission = true,
            hasPostNotificationsPermission = false
        )

        permissionsTracker.permissionsStateFlow.test {
            assertEquals(PermissionState.UNGRANTED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun permissionsStateFlowEmitsPermissionUngrantedWhenOnlyPostNotificationGranted() = runTest {
        val permissionsTracker = createPermissionsTracker()
        permissionsTracker.permissionsState = UiPermissionsState(
            hasCoarseLocationPermission = false,
            hasFineLocationPermission = false,
            hasPostNotificationsPermission = true
        )

        permissionsTracker.permissionsStateFlow.test {
            assertEquals(PermissionState.UNGRANTED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun permissionsStateFlowEmitsPermissionUngrantedWhenHasCoarseLocationAndPostNotification() =
            runTest {
        val permissionsTracker = createPermissionsTracker()
        permissionsTracker.permissionsState = UiPermissionsState(
            hasCoarseLocationPermission = true,
            hasFineLocationPermission = false,
            hasPostNotificationsPermission = true
        )

        permissionsTracker.permissionsStateFlow.test {
            assertEquals(PermissionState.UNGRANTED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun permissionsStateFlowEmitsPermissionGrantedWhenHasFineLocationAndPostNotification() =
            runTest {
        val permissionsTracker = createPermissionsTracker()
        permissionsTracker.permissionsState = UiPermissionsState(
            hasCoarseLocationPermission = false,
            hasFineLocationPermission = true,
            hasPostNotificationsPermission = true
        )

        permissionsTracker.permissionsStateFlow.test {
            assertEquals(PermissionState.GRANTED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun permissionsStateFlowEmitsPermissionDeniedDefaultHasAskedForPermission() = runTest {
        val permissionsTracker = createPermissionsTracker(
            SavedStateHandle(
                mapOf(STATE_REQUESTED_PERMISSIONS to true)
            )
        )

        permissionsTracker.permissionsStateFlow.test {
            assertEquals(PermissionState.DENIED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun permissionsStateFlowEmitsPermissionGrantedWhenGrantedAndHasAskedForPermission() = runTest {
        val permissionsTracker = createPermissionsTracker(
            SavedStateHandle(
                mapOf(STATE_REQUESTED_PERMISSIONS to true)
            )
        )
        permissionsTracker.permissionsState = UiPermissionsState(
            hasCoarseLocationPermission = true,
            hasFineLocationPermission = true,
            hasPostNotificationsPermission = true
        )

        permissionsTracker.permissionsStateFlow.test {
            assertEquals(PermissionState.GRANTED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun permissionsStateFlowEmitsPermissionDeniedWhenUngrantedAndHasAskedForPermission() = runTest {
        val permissionsTracker = createPermissionsTracker(
            SavedStateHandle(
                mapOf(STATE_REQUESTED_PERMISSIONS to true)
            )
        )
        permissionsTracker.permissionsState = UiPermissionsState(
            hasCoarseLocationPermission = false,
            hasFineLocationPermission = false,
            hasPostNotificationsPermission = false
        )

        permissionsTracker.permissionsStateFlow.test {
            assertEquals(PermissionState.DENIED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun backgroundLocationPermissionStateFlowEmitsUngrantedWhenNotGranted() = runTest {
        val permissionsTracker = createPermissionsTracker()
        permissionsTracker.permissionsState = UiPermissionsState(
            hasBackgroundLocationPermission = false
        )

        permissionsTracker.permissionsStateFlow.test {
            assertEquals(PermissionState.UNGRANTED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun backgroundLocationPermissionStateFlowEmitsGrantedWhenGranted() = runTest {
        val permissionsTracker = createPermissionsTracker()
        permissionsTracker.permissionsState = UiPermissionsState(
            hasBackgroundLocationPermission = true
        )

        permissionsTracker.backgroundLocationPermissionStateFlow.test {
            assertEquals(PermissionState.GRANTED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun backgroundLocationPermissionStateFlowEmitsDeniedDefaultWhenAskedForPermissions() = runTest {
        val permissionsTracker = createPermissionsTracker(
            SavedStateHandle(
                mapOf(STATE_REQUESTED_BACKGROUND_LOCATION_PERMISSION to true)
            )
        )

        permissionsTracker.backgroundLocationPermissionStateFlow.test {
            assertEquals(PermissionState.DENIED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun backgroundLocationPermissionStateFlowEmitsDeniedWhenUngrantedAndAskedAlready() = runTest {
        val permissionsTracker = createPermissionsTracker(
            SavedStateHandle(
                mapOf(STATE_REQUESTED_BACKGROUND_LOCATION_PERMISSION to true)
            )
        )
        permissionsTracker.permissionsState = UiPermissionsState(
            hasBackgroundLocationPermission = false
        )

        permissionsTracker.backgroundLocationPermissionStateFlow.test {
            assertEquals(PermissionState.DENIED, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun backgroundLocationPermissionStateFlowEmitsGrantedWhenGrantedAndAskedAlready() = runTest {
        val permissionsTracker = createPermissionsTracker(
            SavedStateHandle(
                mapOf(STATE_REQUESTED_BACKGROUND_LOCATION_PERMISSION to true)
            )
        )
        permissionsTracker.permissionsState = UiPermissionsState(
            hasBackgroundLocationPermission = true
        )

        permissionsTracker.backgroundLocationPermissionStateFlow.test {
            assertEquals(PermissionState.GRANTED, awaitItem())
            ensureAllEventsConsumed()
        }
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
                mapOf(STATE_REQUESTED_PERMISSIONS to true)
            )
        )

        val observer = permissionsTracker.requestPermissionsLiveData.test()
        permissionsTracker.onRequestPermissionsClicked()

        observer.assertEmpty()
    }

    @Test
    fun onRequestBackgroundLocationPermissionsClickedRequestsPermissionsWhenPermissionsRequested() {
        val permissionsTracker = createPermissionsTracker()

        val observer = permissionsTracker.requestBackgroundLocationPermissionLiveData.test()
        permissionsTracker.onRequestBackgroundLocationPermissionClicked()

        observer.assertSize(1)
    }

    @Test
    fun onRequestBgLocationPermissionsClickedRequestsPermissionsOnceOnlyWhenPermissionsRequested() {
        val permissionsTracker = createPermissionsTracker()

        val observer = permissionsTracker.requestBackgroundLocationPermissionLiveData.test()
        permissionsTracker.onRequestBackgroundLocationPermissionClicked()
        permissionsTracker.onRequestBackgroundLocationPermissionClicked()

        observer.assertSize(1)
    }

    @Test
    fun onRequestBgLocationPermissionsClickedDoesNotRequestPermissionsWhenPreviouslyRequested() {
        val permissionsTracker = createPermissionsTracker(
            SavedStateHandle(
                mapOf(STATE_REQUESTED_BACKGROUND_LOCATION_PERMISSION to true)
            )
        )

        val observer = permissionsTracker.requestBackgroundLocationPermissionLiveData.test()
        permissionsTracker.onRequestBackgroundLocationPermissionClicked()

        observer.assertEmpty()
    }

    private fun createPermissionsTracker(savedState: SavedStateHandle = SavedStateHandle()) =
        PermissionsTracker(savedState)
}