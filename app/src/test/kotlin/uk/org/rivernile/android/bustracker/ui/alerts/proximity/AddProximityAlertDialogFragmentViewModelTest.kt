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

package uk.org.rivernile.android.bustracker.ui.alerts.proximity

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopName
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.coroutines.FlowTestObserver
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.LiveDataTestObserver

/**
 * Tests for [AddProximityAlertDialogFragmentViewModel].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AddProximityAlertDialogFragmentViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var uiStateCalculator: UiStateCalculator

    private lateinit var viewModel: AddProximityAlertDialogFragmentViewModel

    @Before
    fun setUp() {
        viewModel = AddProximityAlertDialogFragmentViewModel(
                busStopsRepository,
                uiStateCalculator,
                coroutineRule.testDispatcher)
    }

    @Test
    fun stopDetailsLiveDataEmitsNullWhenNoStopCodeIsSet() = coroutineRule.runBlockingTest {
        val observer = LiveDataTestObserver<StopDetails?>()
        viewModel.stopDetailsLiveData.observeForever(observer)

        observer.assertValues(null)
    }

    @Test
    fun stopDetailsLiveDataEmitsNullWhenStopCodeIsSetAsNull() = coroutineRule.runBlockingTest {
        val observer = LiveDataTestObserver<StopDetails?>()
        viewModel.stopDetailsLiveData.observeForever(observer)

        viewModel.stopCode = null

        observer.assertValues(null)
    }

    @Test
    fun stopDetailsLiveDataEmitsStopDetailsWithNullNameWhenRepositoryReturnsNullName() =
            coroutineRule.runBlockingTest {
        val observer = LiveDataTestObserver<StopDetails?>()
        viewModel.stopDetailsLiveData.observeForever(observer)
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(null))

        viewModel.stopCode = "123456"

        observer.assertValues(null, StopDetails("123456", null))
    }

    @Test
    fun stopDetailsLiveDataEmitsStopDetailsWithNameWhenRepositoryReturnsName() =
            coroutineRule.runBlockingTest {
        val observer = LiveDataTestObserver<StopDetails?>()
        viewModel.stopDetailsLiveData.observeForever(observer)
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(StopName("Name", "Locality")))

        viewModel.stopCode = "123456"

        observer.assertValues(null, StopDetails("123456", StopName("Name", "Locality")))
    }

    @Test
    fun stopDetailsLiveDataEmitsCorrectDataOnChanges() = coroutineRule.runBlockingTest {
        val observer = LiveDataTestObserver<StopDetails?>()
        viewModel.stopDetailsLiveData.observeForever(observer)
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(
                        StopName("Name", "Locality"),
                        StopName("Name 2", null)))
        whenever(busStopsRepository.getNameForStopFlow("987654"))
                .thenReturn(flowOf(StopName("Name 3", "Locality 3")))

        viewModel.stopCode = "123456"
        viewModel.stopCode = "987654"

        observer.assertValues(
                null,
                StopDetails("123456", StopName("Name", "Locality")),
                StopDetails("123456", StopName("Name 2", null)),
                null,
                StopDetails("987654", StopName("Name 3", "Locality 3")))
    }

    @Test
    fun uiStateLiveDataEmitsCalculatedState() = coroutineRule.runBlockingTest {
        whenever(uiStateCalculator.createUiStateFlow(any(), any()))
                .thenReturn(flowOf(
                        UiState.ERROR_PERMISSION_UNGRANTED,
                        UiState.PROGRESS,
                        UiState.CONTENT))
        val observer = LiveDataTestObserver<UiState>()
        viewModel.uiStateLiveData.observeForever(observer)

        observer.assertValues(
                UiState.ERROR_PERMISSION_UNGRANTED,
                UiState.PROGRESS,
                UiState.CONTENT)
    }

    @Test
    fun uiStateCalculatorIsPassedCorrectFlowObjects() = coroutineRule.runBlockingTest{
        val locationPermissionObserver = FlowTestObserver<PermissionState>(this)
        val stopDetailsObserver = FlowTestObserver<StopDetails?>(this)
        doAnswer {
            locationPermissionObserver.observe(it.getArgument(0))
            stopDetailsObserver.observe(it.getArgument(1))

            flowOf(UiState.CONTENT)
        }.whenever(uiStateCalculator).createUiStateFlow(any(), any())
        whenever(busStopsRepository.getNameForStopFlow("123456"))
                .thenReturn(flowOf(null))
        whenever(busStopsRepository.getNameForStopFlow("987654"))
                .thenReturn(flowOf(null))

        val uiStateObserver = LiveDataTestObserver<UiState>()
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.locationPermissionState = PermissionState.DENIED
        viewModel.locationPermissionState = PermissionState.GRANTED
        viewModel.stopCode = "123456"
        viewModel.stopCode = "987654"
        locationPermissionObserver.finish()
        stopDetailsObserver.finish()

        locationPermissionObserver.assertValues(
                PermissionState.UNGRANTED,
                PermissionState.DENIED,
                PermissionState.GRANTED)
        stopDetailsObserver.assertValues(
                null,
                StopDetails("123456", null),
                null,
                StopDetails("987654", null))
    }

    @Test
    fun addButtonEnabledLiveDataOnlyEmitsTrueWhenShowingContent() = coroutineRule.runBlockingTest {
        whenever(uiStateCalculator.createUiStateFlow(any(), any()))
                .thenReturn(flowOf(
                        UiState.ERROR_NO_LOCATION_FEATURE,
                        UiState.ERROR_PERMISSION_UNGRANTED,
                        UiState.ERROR_PERMISSION_DENIED,
                        UiState.ERROR_LOCATION_DISABLED,
                        UiState.PROGRESS,
                        UiState.CONTENT,
                        UiState.PROGRESS))
        val observer = LiveDataTestObserver<Boolean>()

        viewModel.addButtonEnabledLiveData.observeForever(observer)

        observer.assertValues(false, true, false)
    }

    @Test
    fun onLimitationsButtonClickedShowsLimitations() {
        val observer = LiveDataTestObserver<Nothing?>()

        viewModel.showLimitationsLiveData.observeForever(observer)
        viewModel.onLimitationsButtonClicked()

        observer.assertValues(null)
    }

    @Test
    fun onResolveErrorButtonClickedDoesNotTriggerEventWhenNotHandled() {
        whenever(uiStateCalculator.createUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.CONTENT))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val locationSettingsObserver = LiveDataTestObserver<Nothing?>()
        val requestLocationPermissionObserver = LiveDataTestObserver<Nothing?>()
        val showAppSettingsObserver = LiveDataTestObserver<Nothing?>()
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.showLocationSettingsLiveData.observeForever(locationSettingsObserver)
        viewModel.requestLocationPermissionLiveData
                .observeForever(requestLocationPermissionObserver)
        viewModel.showAppSettingsLiveData.observeForever(showAppSettingsObserver)

        viewModel.onResolveErrorButtonClicked()

        uiStateObserver.assertValues(UiState.CONTENT)
        locationSettingsObserver.assertEmpty()
        requestLocationPermissionObserver.assertEmpty()
        showAppSettingsObserver.assertEmpty()
    }

    @Test
    fun onResolveErrorButtonClickedShowsLocationSettingsWhenLocationDisabled() {
        whenever(uiStateCalculator.createUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.ERROR_LOCATION_DISABLED))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val locationSettingsObserver = LiveDataTestObserver<Nothing?>()
        val requestLocationPermissionObserver = LiveDataTestObserver<Nothing?>()
        val showAppSettingsObserver = LiveDataTestObserver<Nothing?>()
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.showLocationSettingsLiveData.observeForever(locationSettingsObserver)
        viewModel.requestLocationPermissionLiveData
                .observeForever(requestLocationPermissionObserver)
        viewModel.showAppSettingsLiveData.observeForever(showAppSettingsObserver)

        viewModel.onResolveErrorButtonClicked()

        uiStateObserver.assertValues(UiState.ERROR_LOCATION_DISABLED)
        locationSettingsObserver.assertValues(null)
        requestLocationPermissionObserver.assertEmpty()
        showAppSettingsObserver.assertEmpty()
    }

    @Test
    fun onResolveErrorButtonClickedShowsRequestLocationPermissionWhenLocationPermissionUngranted() {
        whenever(uiStateCalculator.createUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.ERROR_PERMISSION_UNGRANTED))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val locationSettingsObserver = LiveDataTestObserver<Nothing?>()
        val requestLocationPermissionObserver = LiveDataTestObserver<Nothing?>()
        val showAppSettingsObserver = LiveDataTestObserver<Nothing?>()
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.showLocationSettingsLiveData.observeForever(locationSettingsObserver)
        viewModel.requestLocationPermissionLiveData
                .observeForever(requestLocationPermissionObserver)
        viewModel.showAppSettingsLiveData.observeForever(showAppSettingsObserver)

        viewModel.onResolveErrorButtonClicked()

        uiStateObserver.assertValues(UiState.ERROR_PERMISSION_UNGRANTED)
        locationSettingsObserver.assertEmpty()
        requestLocationPermissionObserver.assertValues(null)
        showAppSettingsObserver.assertEmpty()
    }

    @Test
    fun onResolveErrorButtonClickedShowsAppSettingsWhenLocationPermissionDenied() {
        whenever(uiStateCalculator.createUiStateFlow(any(), any()))
                .thenReturn(flowOf(UiState.ERROR_PERMISSION_DENIED))
        val uiStateObserver = LiveDataTestObserver<UiState>()
        val locationSettingsObserver = LiveDataTestObserver<Nothing?>()
        val requestLocationPermissionObserver = LiveDataTestObserver<Nothing?>()
        val showAppSettingsObserver = LiveDataTestObserver<Nothing?>()
        viewModel.uiStateLiveData.observeForever(uiStateObserver)
        viewModel.showLocationSettingsLiveData.observeForever(locationSettingsObserver)
        viewModel.requestLocationPermissionLiveData
                .observeForever(requestLocationPermissionObserver)
        viewModel.showAppSettingsLiveData.observeForever(showAppSettingsObserver)

        viewModel.onResolveErrorButtonClicked()

        uiStateObserver.assertValues(UiState.ERROR_PERMISSION_DENIED)
        locationSettingsObserver.assertEmpty()
        requestLocationPermissionObserver.assertEmpty()
        showAppSettingsObserver.assertValues(null)
    }
}