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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [PermissionHandler].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class PermissionHandlerTest {

    companion object {

        private const val STATE_REQUESTED_LOCATION_PERMISSIONS = "requestedLocationPermissions"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()

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
                PermissionsState(PermissionState.GRANTED, PermissionState.GRANTED))

        val observer = handler.permissionsStateFlow.test(this)
        handler.permissionsState = permissionStates[0]
        advanceUntilIdle()
        handler.permissionsState = permissionStates[1]
        advanceUntilIdle()
        handler.permissionsState = permissionStates[2]
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(*permissionStates)
    }

    @Test
    fun requestLocationPermissionsFlowDoesNotEmitValidValueByDefault() = runTest {
        val handler = createPermissionHandler()

        val observer = handler.requestLocationPermissionsFlow.test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertNoValues()
    }

    @Test
    fun requestLocationPermissionsFlowDoesNotEmitWhenDoesNotHaveLocationFeature() = runTest {
        givenLocationFeatureAvailability(false)
        val handler = createPermissionHandler()

        val observer = handler.requestLocationPermissionsFlow.test(this)
        handler.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.UNGRANTED)
        advanceUntilIdle()
        observer.finish()

        observer.assertNoValues()
    }

    @Test
    fun requestLocationPermissionsFlowDoesNotEmitWhenCoarseLocationGranted() = runTest {
        givenLocationFeatureAvailability(true)
        val handler = createPermissionHandler()

        val observer = handler.requestLocationPermissionsFlow.test(this)
        handler.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.GRANTED)
        advanceUntilIdle()
        observer.finish()

        observer.assertNoValues()
    }

    @Test
    fun requestLocationPermissionsFlowDoesNotEmitWhenFineLocationGranted() = runTest {
        givenLocationFeatureAvailability(true)
        val handler = createPermissionHandler()

        val observer = handler.requestLocationPermissionsFlow.test(this)
        handler.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.UNGRANTED)
        advanceUntilIdle()
        observer.finish()

        observer.assertNoValues()
    }

    @Test
    fun requestLocationPermissionsFlowDoesNotEmitWhenCoarseAndFineLocationGranted() = runTest {
        givenLocationFeatureAvailability(true)
        val handler = createPermissionHandler()

        val observer = handler.requestLocationPermissionsFlow.test(this)
        handler.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.GRANTED)
        advanceUntilIdle()
        observer.finish()

        observer.assertNoValues()
    }

    @Test
    fun requestLocationPermissionsFlowRequestsPermissionsWhenCoarseAndFineAreUngranted() = runTest {
        givenLocationFeatureAvailability(true)
        whenever(timeUtils.getCurrentTimeMillis())
                .thenReturn(123L)
        val handler = createPermissionHandler()

        val observer = handler.requestLocationPermissionsFlow.test(this)
        handler.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.UNGRANTED)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(123L)
    }

    @Test
    fun requestLocationPermissionsFlowDoesNotRequestPermissionsWhenSavedStateIsTrue() = runTest {
        givenLocationFeatureAvailability(true)
        val handler = createPermissionHandler(
                SavedStateHandle(mapOf(STATE_REQUESTED_LOCATION_PERMISSIONS to true)))

        val observer = handler.requestLocationPermissionsFlow.test(this)
        handler.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.UNGRANTED)
        advanceUntilIdle()
        observer.finish()

        observer.assertNoValues()
    }

    private fun givenLocationFeatureAvailability(hasFeature: Boolean) {
        whenever(locationRepository.hasLocationFeature)
                .thenReturn(hasFeature)
    }

    private fun createPermissionHandler(savedState: SavedStateHandle = SavedStateHandle()) =
            PermissionHandler(
                    savedState,
                    locationRepository,
                    timeUtils)
}