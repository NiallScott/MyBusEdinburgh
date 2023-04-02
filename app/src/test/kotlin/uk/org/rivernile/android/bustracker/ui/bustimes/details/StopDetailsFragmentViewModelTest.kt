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

package uk.org.rivernile.android.bustracker.ui.bustimes.details

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.test

/**
 * Tests for [StopDetailsFragmentViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(MockitoJUnitRunner::class)
class StopDetailsFragmentViewModelTest {

    companion object {

        private const val STATE_STOP_CODE = "stopCode"
        private const val STATE_ASKED_FOR_PERMISSIONS = "askedForPermissions"
    }

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var uiItemRetriever: UiItemRetriever

    @Test
    fun askForLocationPermissionsLiveDataDoesNotEmitWhenHasBothLocationPermissions() {
        val viewModel = createViewModel()

        val observer = viewModel.askForLocationPermissionsLiveData.test()
        viewModel.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.GRANTED)

        observer.assertEmpty()
    }

    @Test
    fun askForLocationPermissionsLiveDataDoesNotEmitWhenHasCoarseLocationPermission() {
        val viewModel = createViewModel()

        val observer = viewModel.askForLocationPermissionsLiveData.test()
        viewModel.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.GRANTED)

        observer.assertEmpty()
    }

    @Test
    fun askForLocationPermissionsLiveDataDoesNotEmitWhenHasFineLocationPermission() {
        val viewModel = createViewModel()

        val observer = viewModel.askForLocationPermissionsLiveData.test()
        viewModel.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.UNGRANTED)

        observer.assertEmpty()
    }

    @Test
    fun askForLocationPermissionsLiveDataEmitsWhenDoesNotHaveLocationPermissions() {
        val viewModel = createViewModel()

        val observer = viewModel.askForLocationPermissionsLiveData.test()
        viewModel.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.UNGRANTED)

        observer.assertSize(1)
    }

    @Test
    fun askForLocationPermissionsLiveDataDoesNotEmitWhenAlreadyAskedForPermissions() {
        val viewModel = createViewModel(
                SavedStateHandle(
                       mapOf(STATE_ASKED_FOR_PERMISSIONS to true)))

        val observer = viewModel.askForLocationPermissionsLiveData.test()
        viewModel.permissionsState = PermissionsState(
                PermissionState.UNGRANTED,
                PermissionState.UNGRANTED)

        observer.assertEmpty()
    }

    @Test
    fun itemsLiveDataEmitsItemsFromUiItemRetriever() = runTest {
        val items1 = listOf(
                UiItem.Distance.Unknown,
                UiItem.NoServices)
        val items2 = listOf(
                UiItem.Map(
                        1.1,
                        2.2,
                        2),
                UiItem.Distance.Known(3.4f),
                UiItem.Service(
                        1,
                        "1",
                        "One",
                        null),
                UiItem.Service(
                        2,
                        "2",
                        "Two",
                        null),
                UiItem.Service(
                        3,
                        "3",
                        "Three",
                        null))
        whenever(uiItemRetriever.createUiItemFlow(
                any(),
                any(),
                any()))
                .thenReturn(flowOf(items1, items2))
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(STATE_STOP_CODE to "123456")))

        val observer = viewModel.itemsLiveData.test()
        viewModel.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.GRANTED)
        advanceUntilIdle()

        observer.assertValues(null, items1, items2)
    }

    @Test
    fun uiStateLiveDataEmitsCorrectUiState() = runTest {
        val items1 = listOf(
                UiItem.Distance.Unknown,
                UiItem.NoServices)
        val items2 = listOf(
                UiItem.Map(
                        1.1,
                        2.2,
                        2),
                UiItem.Distance.Known(3.4f),
                UiItem.Service(
                        1,
                        "1",
                        "One",
                        null),
                UiItem.Service(
                        2,
                        "2",
                        "Two",
                        null),
                UiItem.Service(
                        3,
                        "3",
                        "Three",
                        null))
        whenever(uiItemRetriever.createUiItemFlow(
                any(),
                any(),
                any()))
                .thenReturn(flowOf(items1, items2))
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(
                    STATE_STOP_CODE to "123456")))

        val observer = viewModel.uiStateLiveData.test()
        viewModel.permissionsState = PermissionsState(
                PermissionState.GRANTED,
                PermissionState.GRANTED)
        advanceUntilIdle()

        observer.assertValues(UiState.PROGRESS, UiState.CONTENT)
    }

    @Test
    fun showStopMapLiveDataDoesNotEmitWhenStopCodeIsNull() {
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(
                    STATE_STOP_CODE to null)))

        val observer = viewModel.showStopMapLiveData.test()
        viewModel.onMapClicked()

        observer.assertEmpty()
    }

    @Test
    fun showStopMapLiveDataEmitsWhenStopCodeIsNotNull() {
        val viewModel = createViewModel(
            SavedStateHandle(
                mapOf(
                    STATE_STOP_CODE to "123456")))

        val observer = viewModel.showStopMapLiveData.test()
        viewModel.onMapClicked()

        observer.assertValues("123456")
    }

    private fun createViewModel(savedState: SavedStateHandle = SavedStateHandle()) =
            StopDetailsFragmentViewModel(
                    savedState,
                    uiItemRetriever,
                    coroutineRule.testDispatcher)
}