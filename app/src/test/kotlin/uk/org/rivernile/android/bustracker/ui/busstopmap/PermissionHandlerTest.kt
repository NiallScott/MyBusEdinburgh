/*
 * Copyright (C) 2022 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import androidx.lifecycle.SavedStateHandle
import app.cash.turbine.test
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.core.time.TimeUtils
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [PermissionHandler].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class PermissionHandlerTest {

    companion object {

        private const val STATE_REQUESTED_LOCATION_PERMISSIONS = "requestedLocationPermissions"
    }

    @Mock
    private lateinit var locationRepository: LocationRepository
    @Mock
    private lateinit var timeUtils: TimeUtils

    @Test
    fun permissionsStateInitiallyHasDefaultState() {
        val expected = PermissionsState()
        val handler = createPermissionHandler()

        val result = handler.permissionsState

        assertEquals(expected, result)
    }

    @Test
    fun permissionsStateReturnsSetState() {
        val expected = PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)
        val handler = createPermissionHandler()

        handler.permissionsState = expected
        val result = handler.permissionsState

        assertEquals(expected, result)
    }

    @Test
    fun permissionsStateFlowEmitsPermissionsState() = runTest {
        val handler = createPermissionHandler()
        val permissionStates = arrayOf(
            PermissionsState(PermissionState.UNGRANTED, PermissionState.UNGRANTED),
            PermissionsState(PermissionState.UNGRANTED, PermissionState.GRANTED),
            PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED)
        )

        handler.permissionsStateFlow.test {
            handler.permissionsState = permissionStates[0]
            handler.permissionsState = permissionStates[1]
            handler.permissionsState = permissionStates[2]

            assertNull(awaitItem())
            assertEquals(permissionStates[0], awaitItem())
            assertEquals(permissionStates[1], awaitItem())
            assertEquals(permissionStates[2], awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun requestLocationPermissionsFlowDoesNotEmitValidValueByDefault() = runTest {
        val handler = createPermissionHandler()

        handler.permissionsStateFlow.test {
            assertNull(awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun requestLocationPermissionsFlowDoesNotEmitWhenDoesNotHaveLocationFeature() = runTest {
        givenLocationFeatureAvailability(false)
        val handler = createPermissionHandler()

        handler.requestLocationPermissionsFlow.test {
            handler.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.UNGRANTED
            )

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun requestLocationPermissionsFlowDoesNotEmitWhenCoarseLocationGranted() = runTest {
        givenLocationFeatureAvailability(true)
        val handler = createPermissionHandler()

        handler.requestLocationPermissionsFlow.test {
            handler.permissionsState = PermissionsState(
                    PermissionState.UNGRANTED,
                    PermissionState.GRANTED
            )

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun requestLocationPermissionsFlowDoesNotEmitWhenFineLocationGranted() = runTest {
        givenLocationFeatureAvailability(true)
        val handler = createPermissionHandler()

        handler.requestLocationPermissionsFlow.test {
            handler.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.UNGRANTED
            )

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun requestLocationPermissionsFlowDoesNotEmitWhenCoarseAndFineLocationGranted() = runTest {
        givenLocationFeatureAvailability(true)
        val handler = createPermissionHandler()

        handler.requestLocationPermissionsFlow.test {
            handler.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.GRANTED
            )

            ensureAllEventsConsumed()
        }
    }

    @Test
    fun requestLocationPermissionsFlowRequestsPermissionsWhenCoarseAndFineAreUngranted() = runTest {
        givenLocationFeatureAvailability(true)
        whenever(timeUtils.currentTimeMills)
            .thenReturn(123L)
        val handler = createPermissionHandler()

        handler.requestLocationPermissionsFlow.test {
            handler.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.UNGRANTED
            )

            assertEquals(123L, awaitItem())
            ensureAllEventsConsumed()
        }
    }

    @Test
    fun requestLocationPermissionsFlowDoesNotRequestPermissionsWhenSavedStateIsTrue() = runTest {
        givenLocationFeatureAvailability(true)
        val handler = createPermissionHandler(
            SavedStateHandle(
                mapOf(STATE_REQUESTED_LOCATION_PERMISSIONS to true)
            )
        )

        handler.requestLocationPermissionsFlow.test {
            handler.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.UNGRANTED
            )

            ensureAllEventsConsumed()
        }
    }

    private fun givenLocationFeatureAvailability(hasFeature: Boolean) {
        whenever(locationRepository.hasLocationFeature)
            .thenReturn(hasFeature)
    }

    private fun createPermissionHandler(savedState: SavedStateHandle = SavedStateHandle()) =
        PermissionHandler(
            savedState,
            locationRepository,
            timeUtils
        )
}