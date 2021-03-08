/*
 * Copyright (C) 2021 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.coroutines.test

/**
 * Tests for [UiStateCalculator].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class UiStateCalculatorTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()

    private lateinit var calculator: UiStateCalculator

    @Before
    fun setUp() {
        calculator = UiStateCalculator()
    }

    @Test
    fun createUiStateFlowEmitsNoStopCodeWhenStopCodeIsNull() = coroutineRule.runBlockingTest {
        val stopCodeFlow = flowOf<String?>(null)
        val stopDetailsFlow = flowOf<StopDetails?>(null)
        val availableServicesFlow = flowOf<List<String>?>(null)

        val observer = calculator.createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow).test(this)
        observer.finish()

        observer.assertValues(UiState.ERROR_NO_STOP_CODE)
    }

    @Test
    fun createUiStateFlowEmitsNoStopCodeWhenStopCodeIsEmpty() = coroutineRule.runBlockingTest {
        val stopCodeFlow = flowOf<String?>("")
        val stopDetailsFlow = flowOf<StopDetails?>(null)
        val availableServicesFlow = flowOf<List<String>?>(null)

        val observer = calculator.createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow).test(this)
        observer.finish()

        observer.assertValues(UiState.ERROR_NO_STOP_CODE)
    }

    @Test
    fun createUiStateFlowEmitsProgressWhenStopDetailsIsNullAndAvailableServicesIsNull() =
            coroutineRule.runBlockingTest {
        val stopCodeFlow = flowOf<String?>("123456")
        val stopDetailsFlow = flowOf<StopDetails?>(null)
        val availableServicesFlow = flowOf<List<String>?>(null)

        val observer = calculator.createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow).test(this)
        observer.finish()

        observer.assertValues(UiState.PROGRESS)
    }

    @Test
    fun createUiStateFlowEmitsProgressWhenStopDetailsIsNullAndAvailableServicesIsNotNull() =
            coroutineRule.runBlockingTest {
        val stopCodeFlow = flowOf<String?>("123456")
        val stopDetailsFlow = flowOf<StopDetails?>(null)
        val availableServicesFlow = flowOf(listOf("1", "2", "3"))

        val observer = calculator.createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow).test(this)
        observer.finish()

        observer.assertValues(UiState.PROGRESS)
    }

    @Test
    fun createUiStateFlowEmitsProgressWhenStopDetailsIsNotNullAndAvailableServicesIsNull() =
            coroutineRule.runBlockingTest {
        val stopCodeFlow = flowOf<String?>("123456")
        val stopDetailsFlow = flowOf(StopDetails("123456", null))
        val availableServicesFlow = flowOf<List<String>?>(null)

        val observer = calculator.createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow).test(this)
        observer.finish()

        observer.assertValues(UiState.PROGRESS)
    }

    @Test
    fun createUiStateFlowEmitsNoServicesWhenServicesIsEmpty() = coroutineRule.runBlockingTest {
        val stopCodeFlow = flowOf<String?>("123456")
        val stopDetailsFlow = flowOf(StopDetails("123456", null))
        val availableServicesFlow = flowOf<List<String>?>(emptyList())

        val observer = calculator.createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow).test(this)
        observer.finish()

        observer.assertValues(UiState.ERROR_NO_SERVICES)
    }

    @Test
    fun createUiStateFlowEmitsContentWhenConditionsAreMet() = coroutineRule.runBlockingTest {
        val stopCodeFlow = flowOf<String?>("123456")
        val stopDetailsFlow = flowOf(StopDetails("123456", null))
        val availableServicesFlow = flowOf(listOf("1", "2", "3"))

        val observer = calculator.createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow).test(this)
        observer.finish()

        observer.assertValues(UiState.CONTENT)
    }

    @Test
    fun createUiStateFlowEmitsCorrectItemsWithRepresentativeExample() = coroutineRule.runBlockingTest {
        val stopCodeFlow = flow {
            emit(null)
            delay(100L)
            emit("123456")
        }
        val stopDetailsFlow = flow {
            emit(null)
            delay(200L)
            emit(StopDetails("123456", null))
        }
        val availableServicesFlow = flow {
            emit(null)
            delay(300L)
            emit(emptyList<String>())
            delay(100L)
            emit(listOf("1", "2", "3"))
        }

        val observer = calculator.createUiStateFlow(
                stopCodeFlow,
                stopDetailsFlow,
                availableServicesFlow).test(this)
        advanceUntilIdle()
        observer.finish()

        observer.assertValues(
                UiState.ERROR_NO_STOP_CODE,
                UiState.PROGRESS,
                UiState.ERROR_NO_SERVICES,
                UiState.CONTENT)
    }
}