/*
 * Copyright (C) 2021 - 2024 Niall 'Rivernile' Scott
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

import app.cash.turbine.test
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.coroutines.intervalFlowOf
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [UiStateCalculator].
 *
 * @author Niall Scott
 */
class UiStateCalculatorTest {

    private lateinit var calculator: UiStateCalculator

    @BeforeTest
    fun setUp() {
        calculator = UiStateCalculator()
    }

    @Test
    fun createUiStateFlowEmitsNoStopCodeWhenStopCodeIsNull() = runTest {
        val stopCodeFlow = flowOf<String?>(null)
        val stopDetailsFlow = flowOf<StopDetails?>(null)
        val availableServicesFlow = flowOf<List<String>?>(null)
        val permissionsFlow = flowOf(PermissionsState())

        calculator
            .createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow,
                permissionsFlow
            )
            .test {
                assertEquals(UiState.ERROR_NO_STOP_CODE, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsNoStopCodeWhenStopCodeIsEmpty() = runTest {
        val stopCodeFlow = flowOf<String?>("")
        val stopDetailsFlow = flowOf<StopDetails?>(null)
        val availableServicesFlow = flowOf<List<String>?>(null)
        val permissionsFlow = flowOf(PermissionsState())

        calculator
            .createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow,
                permissionsFlow
            )
            .test {
                assertEquals(UiState.ERROR_NO_STOP_CODE, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsPermissionDeniedWhenPermissionIsDenied() = runTest {
        val stopCodeFlow = flowOf<String?>("123456")
        val stopDetailsFlow = flowOf<StopDetails?>(null)
        val availableServicesFlow = flowOf<List<String>?>(null)
        val permissionsFlow = flowOf(PermissionsState(PermissionState.DENIED))

        calculator
            .createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow,
                permissionsFlow
            ).test {
                assertEquals(UiState.ERROR_PERMISSION_DENIED, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsPermissionRequiredWhenPermissionIsUngranted() = runTest {
        val stopCodeFlow = flowOf<String?>("123456")
        val stopDetailsFlow = flowOf<StopDetails?>(null)
        val availableServicesFlow = flowOf<List<String>?>(null)
        val permissionsFlow = flowOf(PermissionsState(PermissionState.UNGRANTED))

        calculator
            .createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow,
                permissionsFlow
            ).test {
                assertEquals(UiState.ERROR_PERMISSION_REQUIRED, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsPermissionRequiredWhenPermissionIsShowRationale() = runTest {
        val stopCodeFlow = flowOf<String?>("123456")
        val stopDetailsFlow = flowOf<StopDetails?>(null)
        val availableServicesFlow = flowOf<List<String>?>(null)
        val permissionsFlow = flowOf(PermissionsState(PermissionState.SHOW_RATIONALE))

        calculator
            .createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow,
                permissionsFlow
            )
            .test {
                assertEquals(UiState.ERROR_PERMISSION_REQUIRED, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsProgressWhenStopDetailsIsNullAndAvailableServicesIsNull() = runTest {
        val stopCodeFlow = flowOf<String?>("123456")
        val stopDetailsFlow = flowOf<StopDetails?>(null)
        val availableServicesFlow = flowOf<List<String>?>(null)
        val permissionsFlow = flowOf(PermissionsState(PermissionState.GRANTED))

        calculator
            .createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow,
                permissionsFlow
            )
            .test {
                assertEquals(UiState.PROGRESS, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsProgressWhenStopDetailsIsNullAndAvailableServicesIsNotNull() =
            runTest {
        val stopCodeFlow = flowOf<String?>("123456")
        val stopDetailsFlow = flowOf<StopDetails?>(null)
        val availableServicesFlow = flowOf(listOf("1", "2", "3"))
        val permissionsFlow = flowOf(PermissionsState(PermissionState.GRANTED))

        calculator
            .createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow,
                permissionsFlow
            )
            .test {
                assertEquals(UiState.PROGRESS, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsProgressWhenStopDetailsIsNotNullAndAvailableServicesIsNull() =
            runTest {
        val stopCodeFlow = flowOf<String?>("123456")
        val stopDetailsFlow = flowOf(StopDetails("123456", null))
        val availableServicesFlow = flowOf<List<String>?>(null)
        val permissionsFlow = flowOf(PermissionsState(PermissionState.GRANTED))

        calculator
            .createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow,
                permissionsFlow
            )
            .test {
                assertEquals(UiState.PROGRESS, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsNoServicesWhenServicesIsEmpty() = runTest {
        val stopCodeFlow = flowOf<String?>("123456")
        val stopDetailsFlow = flowOf(StopDetails("123456", null))
        val availableServicesFlow = flowOf<List<String>?>(emptyList())
        val permissionsFlow = flowOf(PermissionsState(PermissionState.GRANTED))

        calculator
            .createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow,
                permissionsFlow
            )
            .test {
                assertEquals(UiState.ERROR_NO_SERVICES, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsContentWhenConditionsAreMet() = runTest {
        val stopCodeFlow = flowOf<String?>("123456")
        val stopDetailsFlow = flowOf(StopDetails("123456", null))
        val availableServicesFlow = flowOf(listOf("1", "2", "3"))
        val permissionsFlow = flowOf(PermissionsState(PermissionState.GRANTED))

        calculator
            .createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow,
                permissionsFlow
            )
            .test {
                assertEquals(UiState.CONTENT, awaitItem())
                awaitComplete()
            }
    }

    @Test
    fun createUiStateFlowEmitsCorrectItemsWithRepresentativeExample() = runTest {
        val stopCodeFlow = intervalFlowOf(0L, 100L, null, "123456")
        val stopDetailsFlow = intervalFlowOf(0L, 200L, null, StopDetails("123456", null))
        val availableServicesFlow = flow {
            emit(null)
            delay(300L)
            emit(emptyList())
            delay(100L)
            emit(listOf("1", "2", "3"))
        }
        val permissionFlow = intervalFlowOf(
            0L,
            150L,
            PermissionsState(PermissionState.UNGRANTED),
            PermissionsState(PermissionState.GRANTED)
        )

        calculator
            .createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow,
                permissionFlow
            )
            .test {
                assertEquals(UiState.ERROR_NO_STOP_CODE, awaitItem())
                assertEquals(UiState.ERROR_PERMISSION_REQUIRED, awaitItem())
                assertEquals(UiState.PROGRESS, awaitItem())
                assertEquals(UiState.ERROR_NO_SERVICES, awaitItem())
                assertEquals(UiState.CONTENT, awaitItem())
                awaitComplete()
            }
    }
}