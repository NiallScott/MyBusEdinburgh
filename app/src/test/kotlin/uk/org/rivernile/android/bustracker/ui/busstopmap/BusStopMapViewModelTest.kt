/*
 * Copyright (C) 2018 - 2022 Niall 'Rivernile' Scott
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

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateHandle
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.flow.flowOf
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.reset
import org.mockito.Mockito.spy
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.anyOrNull
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import uk.org.rivernile.android.bustracker.repositories.busstopmap.BusStopMapRepository
import uk.org.rivernile.android.bustracker.repositories.busstopmap.SelectedStop
import uk.org.rivernile.android.bustracker.repositories.busstopmap.Stop
import uk.org.rivernile.android.bustracker.utils.ClearableLiveData
import uk.org.rivernile.android.utils.TestableClearableLiveData

/**
 * Tests for [BusStopMapViewModel].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class BusStopMapViewModelTest {

    companion object {

        private const val DEFAULT_ZOOM = 14f
    }

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var locationRepository: LocationRepository
    @Mock
    private lateinit var isMyLocationEnabledDetector: IsMyLocationEnabledDetector
    @Mock
    private lateinit var repository: BusStopMapRepository
    @Mock
    private lateinit var preferenceManager: PreferenceManager

    @Mock
    lateinit var cameraLocationObserver: Observer<CameraLocation>
    @Mock
    lateinit var selectedStopObserver: Observer<SelectedStop>
    @Mock
    lateinit var showStopDetailsObserver: Observer<String>
    @Mock
    lateinit var voidObserver: Observer<Void>
    @Mock
    lateinit var mapTypeObserver: Observer<MapType>
    @Mock
    lateinit var busStopsObserver: Observer<Map<String, Stop>>
    @Mock
    lateinit var routeLinesObserver: Observer<Map<String, List<PolylineOptions>>>
    @Mock
    lateinit var servicesObserver: Observer<Array<String>>

    private lateinit var serviceNamesLiveData: ClearableLiveData<Array<String>>

    private lateinit var viewModel: BusStopMapViewModel

    @Before
    fun setUp() {
        serviceNamesLiveData = spy(ClearableLiveData())
        whenever(repository.getServiceNames())
                .thenReturn(serviceNamesLiveData)
        // TODO: temp fix for tests.
        whenever(isMyLocationEnabledDetector.getIsMyLocationFeatureEnabledFlow(any()))
                .thenReturn(flowOf(false))
        viewModel = BusStopMapViewModel(
                SavedStateHandle(),
                locationRepository,
                isMyLocationEnabledDetector,
                repository,
                preferenceManager)
    }

    @Test
    fun getsServiceNamesLiveDataOnConstruction() {
        verify(repository)
                .getServiceNames()
    }

    @Test
    fun selectedServicesIsNullByDefault() {
        val result = viewModel.selectedServices

        assertNull(result)
    }

    @Test
    fun loadsBusStopsOnFirstObserver() {
        viewModel.busStops.observeForever(busStopsObserver)

        verify(repository)
                .getBusStops(null)
    }

    @Test
    fun loadsRouteLinesOnFirstObserver() {
        viewModel.routeLines.observeForever(routeLinesObserver)

        verify(repository)
                .getRouteLines(null)
    }

    @Test
    fun onFirstCreateWithNoArgumentsMovesCameraToLastPosition() {
        whenever(preferenceManager.getLastMapLatitude())
                .thenReturn(1.0)
        whenever(preferenceManager.getLastMapLongitude())
                .thenReturn(2.0)
        whenever(preferenceManager.getLastMapZoomLevel())
                .thenReturn(3f)
        viewModel.cameraLocation.observeForever(cameraLocationObserver)

        viewModel.onFirstCreate()

        val expected = CameraLocation(1.0, 2.0, 3f, false)
        verify(cameraLocationObserver)
                .onChanged(expected)
    }

    @Test
    fun onFirstCreateWithNoArgumentsSetsMapTypeToLastType() {
        whenever(preferenceManager.getLastMapType())
                .thenReturn(2)
        viewModel.mapType.observeForever(mapTypeObserver)

        viewModel.onFirstCreate()

        verify(mapTypeObserver)
                .onChanged(MapType.SATELLITE)
    }

    @Test
    fun onFirstCreateWithStopCodeArgumentLoadsStopAndCentersCameraOnStop() {
        val selectedStop = SelectedStop("123456", 1.0, 2.0, "1, 2, 3, 4, 5")
        val selectedStopLiveData = TestableClearableLiveData<SelectedStop>()
        selectedStopLiveData.value = selectedStop
        whenever(repository.getBusStop("123456"))
                .thenReturn(selectedStopLiveData)
        viewModel.cameraLocation.observeForever(cameraLocationObserver)
        viewModel.showMapMarkerBubble.observeForever(selectedStopObserver)

        viewModel.onFirstCreate("123456")

        val expectedCameraLocation = CameraLocation(1.0, 2.0, 99f, false)
        verify(cameraLocationObserver)
                .onChanged(expectedCameraLocation)
        verify(selectedStopObserver)
                .onChanged(selectedStop)
    }

    @Test
    fun onFirstCreateWithStopCodeArgumentSetsMapTypeToLastType() {
        whenever(preferenceManager.getLastMapType())
                .thenReturn(2)
        viewModel.mapType.observeForever(mapTypeObserver)

        viewModel.onFirstCreate("123456")

        verify(mapTypeObserver)
                .onChanged(MapType.SATELLITE)
    }

    @Test
    fun onFirstCreateWithStopCodeArgumentDoesNotMoveCameraWhenStopIsNotFound() {
        val selectedStopLiveData = TestableClearableLiveData<SelectedStop>()
        selectedStopLiveData.value = null
        whenever(repository.getBusStop("123456"))
                .thenReturn(selectedStopLiveData)
        viewModel.cameraLocation.observeForever(cameraLocationObserver)
        viewModel.showMapMarkerBubble.observeForever(selectedStopObserver)

        viewModel.onFirstCreate("123456")

        val expectedCameraLocation = CameraLocation(1.0, 2.0, 99f, false)
        verify(cameraLocationObserver, never())
                .onChanged(expectedCameraLocation)
        verify(selectedStopObserver, never())
                .onChanged(any())
    }

    @Test
    fun onFirstCreateWithStopCodeArgumentDoesNotMoveCameraWhenStopCodeIsNotExpectedCode() {
        val selectedStop = SelectedStop("098765", 1.0, 2.0, "1, 2, 3, 4, 5")
        val selectedStopLiveData = TestableClearableLiveData<SelectedStop>()
        selectedStopLiveData.value = selectedStop
        whenever(repository.getBusStop("123456"))
                .thenReturn(selectedStopLiveData)
        viewModel.cameraLocation.observeForever(cameraLocationObserver)
        viewModel.showMapMarkerBubble.observeForever(selectedStopObserver)

        viewModel.onFirstCreate("123456")

        val expectedCameraLocation = CameraLocation(1.0, 2.0, 99f, false)
        verify(cameraLocationObserver, never())
                .onChanged(expectedCameraLocation)
        verify(selectedStopObserver)
                .onChanged(selectedStop)
    }

    @Test
    fun onFirstCreateWithLatitudeLongitudeArgumentsMovesCameraToSuppliedCoordinates() {
        viewModel.cameraLocation.observeForever(cameraLocationObserver)

        viewModel.onFirstCreate(1.0, 2.0)

        val expected = CameraLocation(1.0, 2.0, DEFAULT_ZOOM, false)
        verify(cameraLocationObserver)
                .onChanged(expected)
    }

    @Test
    fun onFirstCreateWithLatitudeAndLongitudeSetsMapTypeToLastType() {
        whenever(preferenceManager.getLastMapType())
                .thenReturn(2)
        viewModel.mapType.observeForever(mapTypeObserver)

        viewModel.onFirstCreate(1.0, 2.0)

        verify(mapTypeObserver)
                .onChanged(MapType.SATELLITE)
    }

    @Test
    fun persistingMapParametersWritesParametersToPreferences() {
        viewModel.onPersistMapParameters(1.0, 2.0, 3f, MapType.NORMAL)

        verify(preferenceManager)
                .setLastMapLatitude(1.0)
        verify(preferenceManager)
                .setLastMapLongitude(2.0)
        verify(preferenceManager)
                .setLastMapZoomLevel(3f)
        verify(preferenceManager)
                .setLastMapType(1)
    }

    @Test
    fun onMarkerBubbleClickShowDetailsForStop() {
        val stop = Stop("123456", "Stop name", 1.0, 2.0, 3)
        viewModel.showStopDetails.observeForever(showStopDetailsObserver)

        viewModel.onMarkerBubbleClicked(stop)

        verify(showStopDetailsObserver)
                .onChanged("123456")
    }

    @Test
    fun onSearchMenuItemClickedShowSearchUi() {
        viewModel.showSearch.observeForever(voidObserver)

        viewModel.onSearchMenuItemClicked()

        verify(voidObserver)
                .onChanged(anyOrNull())
    }

    @Test
    fun onServicesMenuItemClickedShowsServicesChooserUi() {
        viewModel.showServicesChooser.observeForever(voidObserver)

        viewModel.onServicesMenuItemClicked()

        verify(voidObserver)
                .onChanged(anyOrNull())
    }

    @Test
    fun onMapTypeMenuItemClickedShowMapTypeSelectionUi() {
        viewModel.showMapTypeSelection.observeForever(voidObserver)

        viewModel.onMapTypeMenuItemClicked()

        verify(voidObserver)
                .onChanged(anyOrNull())
    }

    @Test
    fun onTrafficViewMenuItemClickedShowTrafficViewOnMap() {
        viewModel.updateTrafficView.observeForever(voidObserver)

        viewModel.onTrafficViewMenuItemClicked()

        verify(voidObserver)
                .onChanged(anyOrNull())
    }

    @Test
    fun shouldShowZoomControlsWhenSetInPreferences() {
        whenever(preferenceManager.isMapZoomButtonsShown())
                .thenReturn(true)

        val result = viewModel.shouldShowZoomControls

        assertTrue(result)
    }

    @Test
    fun onMapTypeSelectedSetsMapType() {
        viewModel.mapType.observeForever(mapTypeObserver)

        viewModel.onMapTypeSelected(MapType.HYBRID)

        verify(mapTypeObserver)
                .onChanged(MapType.HYBRID)
    }

    @Test
    fun onServicesChosenAsNullCausesLoadOfStopsAndRouteLinesWithNoFilter() {
        viewModel.busStops.observeForever(busStopsObserver)
        viewModel.routeLines.observeForever(routeLinesObserver)
        reset(repository) // Reset as repository has already been called for initial stop load

        viewModel.onServicesChosen(null)

        verify(repository)
                .getBusStops(null)
        verify(repository)
                .getRouteLines(null)
    }

    @Test
    fun onServicesChosenCausesLoadOfStopsAndRouteLinesWithFilterSet() {
        viewModel.busStops.observeForever(busStopsObserver)
        viewModel.routeLines.observeForever(routeLinesObserver)
        reset(repository) // Reset as repository has already been called for initial stop load
        val filter = arrayOf("1", "2", "3")

        viewModel.onServicesChosen(filter)

        verify(repository)
                .getBusStops(filter)
        verify(repository)
                .getRouteLines(filter)
    }

    @Test
    fun loadingBusStopsClearsPreviousLiveData() {
        val liveData = spy(ClearableLiveData<Map<String, Stop>>())
        whenever(repository.getBusStops(anyOrNull()))
                .thenReturn(liveData)
        viewModel.busStops.observeForever(busStopsObserver)

        viewModel.onServicesChosen(arrayOf("1", "2", "3"))

        verify(liveData)
                .onCleared()
    }

    @Test
    fun loadingRouteLinesClearsPreviousLiveData() {
        val liveData = spy(ClearableLiveData<Map<String, List<PolylineOptions>>>())
        whenever(repository.getRouteLines(anyOrNull()))
                .thenReturn(liveData)
        viewModel.routeLines.observeForever(routeLinesObserver)

        viewModel.onServicesChosen(arrayOf("1", "2", "3"))

        verify(liveData)
                .onCleared()
    }

    @Test
    fun onStopSearchResultLoadsStopAndMovesCameraToStopAndShowsBubble() {
        val selectedStop = SelectedStop("123456", 1.0, 2.0, "1, 2, 3, 4, 5")
        val selectedStopLiveData = TestableClearableLiveData<SelectedStop>()
        selectedStopLiveData.value = selectedStop
        whenever(repository.getBusStop("123456"))
                .thenReturn(selectedStopLiveData)
        viewModel.cameraLocation.observeForever(cameraLocationObserver)
        viewModel.showMapMarkerBubble.observeForever(selectedStopObserver)

        viewModel.onStopSearchResult("123456")

        val expectedCameraLocation = CameraLocation(1.0, 2.0, 99f, false)
        verify(cameraLocationObserver)
                .onChanged(expectedCameraLocation)
        verify(selectedStopObserver)
                .onChanged(selectedStop)
    }

    @Test
    fun onStopSearchResultDoesNotMoveCameraWhenStopIsNotFound() {
        val selectedStopLiveData = TestableClearableLiveData<SelectedStop>()
        selectedStopLiveData.value = null
        whenever(repository.getBusStop("123456"))
                .thenReturn(selectedStopLiveData)
        viewModel.cameraLocation.observeForever(cameraLocationObserver)
        viewModel.showMapMarkerBubble.observeForever(selectedStopObserver)

        viewModel.onStopSearchResult("123456")

        val expectedCameraLocation = CameraLocation(1.0, 2.0, 99f, false)
        verify(cameraLocationObserver, never())
                .onChanged(expectedCameraLocation)
        verify(selectedStopObserver, never())
                .onChanged(any())
    }

    @Test
    fun onStopSearchResultDoesNotMoveCameraWhenStopCodeIsNotExpectedCode() {
        val selectedStop = SelectedStop("098765", 1.0, 2.0, "1, 2, 3, 4, 5")
        val selectedStopLiveData = TestableClearableLiveData<SelectedStop>()
        selectedStopLiveData.value = selectedStop
        whenever(repository.getBusStop("123456"))
                .thenReturn(selectedStopLiveData)
        viewModel.cameraLocation.observeForever(cameraLocationObserver)
        viewModel.showMapMarkerBubble.observeForever(selectedStopObserver)

        viewModel.onStopSearchResult("123456")

        val expectedCameraLocation = CameraLocation(1.0, 2.0, 99f, false)
        verify(cameraLocationObserver, never())
                .onChanged(expectedCameraLocation)
        verify(selectedStopObserver)
                .onChanged(selectedStop)
    }

    @Test
    fun onClusterMarkerClickedMovesCameraToClusterCenterAndZooms() {
        viewModel.cameraLocation.observeForever(cameraLocationObserver)

        viewModel.onClusterMarkerClicked(1.0, 2.0, 3f)

        val expected = CameraLocation(1.0, 2.0, 4f, true)
        verify(cameraLocationObserver)
                .onChanged(expected)
    }

    @Test
    fun onMapMarkerClickedMovesCameraToMarkerAndShowsBubble() {
        val selectedStop = SelectedStop("123456", 1.0, 2.0, "1, 2, 3, 4, 5")
        val selectedStopLiveData = TestableClearableLiveData<SelectedStop>()
        selectedStopLiveData.value = selectedStop
        whenever(repository.getBusStop("123456"))
                .thenReturn(selectedStopLiveData)
        viewModel.cameraLocation.observeForever(cameraLocationObserver)
        viewModel.showMapMarkerBubble.observeForever(selectedStopObserver)
        val stop = Stop("123456", "Sample name", 1.0, 2.0, 3)

        viewModel.onMapMarkerClicked(stop)

        val expectedCameraLocation = CameraLocation(1.0, 2.0, null, true)
        verify(cameraLocationObserver)
                .onChanged(expectedCameraLocation)
        verify(selectedStopObserver)
                .onChanged(selectedStop)
    }

    @Test
    fun onMapMarkerBubbleClosedSetsSelectedStopToNullWhenStopCodeMatches() {
        viewModel.showMapMarkerBubble.observeForever(selectedStopObserver)
        val stop = Stop("123456", "Stop name", 1.0, 2.0, 3)
        viewModel.onMapMarkerClicked(stop)

        viewModel.onMapMarkerBubbleClosed("123456")

        verify(selectedStopObserver)
                .onChanged(null)
    }

    @Test
    fun onMapMarkerBubbleClosedDoesNotSetSelectedStopToNullWhenStopCodeDoesNotMatch() {
        viewModel.showMapMarkerBubble.observeForever(selectedStopObserver)
        val stop = Stop("123456", "Stop name", 1.0, 2.0, 3)
        viewModel.onMapMarkerClicked(stop)

        viewModel.onMapMarkerBubbleClosed("098765")

        verify(selectedStopObserver, never())
                .onChanged(anyOrNull())
    }

    @Test
    fun onRequestCameraLocationSetsGivenCameraLocationAtDefaultZoom() {
        viewModel.cameraLocation.observeForever(cameraLocationObserver)

        viewModel.onRequestCameraLocation(1.0, 2.0)

        val expected = CameraLocation(1.0, 2.0, DEFAULT_ZOOM, false)
        verify(cameraLocationObserver)
                .onChanged(expected)
    }

    @Test
    fun onClearedClearsAllClearableLiveDataObjects() {
        val busStopsLiveData = spy(ClearableLiveData<Map<String, Stop>>())
        val routeLinesLiveData = spy(ClearableLiveData<Map<String, List<PolylineOptions>>>())
        val busStopLiveData = spy(ClearableLiveData<SelectedStop>())
        whenever(repository.getBusStops(anyOrNull()))
                .thenReturn(busStopsLiveData)
        whenever(repository.getRouteLines(anyOrNull()))
                .thenReturn(routeLinesLiveData)
        whenever(repository.getBusStop(any()))
                .thenReturn(busStopLiveData)
        viewModel.serviceNames.observeForever(servicesObserver)
        viewModel.busStops.observeForever(busStopsObserver)
        viewModel.routeLines.observeForever(routeLinesObserver)
        viewModel.showMapMarkerBubble.observeForever(selectedStopObserver)

        viewModel.onServicesChosen(arrayOf("1", "2", "3"))
        viewModel.onStopSearchResult("123456")
        reset(busStopsLiveData, routeLinesLiveData) // Reset mock interaction counters
        callOnCleared()

        verify(serviceNamesLiveData)
                .onCleared()
        verify(busStopsLiveData)
                .onCleared()
        verify(routeLinesLiveData)
                .onCleared()
        verify(busStopLiveData)
                .onCleared()
    }

    @Test
    fun onRestoreStateMovesCameraToPreferenceLocation() {
        whenever(preferenceManager.getLastMapLatitude())
                .thenReturn(1.0)
        whenever(preferenceManager.getLastMapLongitude())
                .thenReturn(2.0)
        whenever(preferenceManager.getLastMapZoomLevel())
                .thenReturn(3f)
        viewModel.cameraLocation.observeForever(cameraLocationObserver)

        viewModel.onRestoreState(null, null)

        val expected = CameraLocation(1.0, 2.0, 3f, false)
        verify(cameraLocationObserver)
                .onChanged(expected)
    }

    @Test
    fun onRestoreStateSetsMapTypeToLastType() {
        whenever(preferenceManager.getLastMapType())
                .thenReturn(2)
        viewModel.mapType.observeForever(mapTypeObserver)

        viewModel.onRestoreState(null, null)

        verify(mapTypeObserver)
                .onChanged(MapType.SATELLITE)
    }

    @Test
    fun onRestoreStateDoesNotReloadStopsAndRouteLinesWhenServicesIsSame() {
        viewModel.busStops.observeForever(busStopsObserver)
        viewModel.routeLines.observeForever(routeLinesObserver)

        viewModel.onRestoreState(null, null)

        verify(repository)
                .getBusStops(null)
        verify(repository)
                .getRouteLines(null)
    }

    @Test
    fun onRestoreStateReloadsStopsAndRouteLinesWhenServicesAreDifferent() {
        viewModel.busStops.observeForever(busStopsObserver)
        viewModel.routeLines.observeForever(routeLinesObserver)
        val filter = arrayOf("1", "2", "3")

        viewModel.onRestoreState(filter, null)

        verify(repository)
                .getBusStops(filter)
        verify(repository)
                .getRouteLines(filter)
    }

    @Test
    fun onRestoreStateDoesNotReloadSelectedStopWhenStopCodeIsSame() {
        viewModel.showMapMarkerBubble.observeForever(selectedStopObserver)

        viewModel.onRestoreState(null, null)

        verify(repository, never())
                .getBusStop(anyOrNull())
    }

    @Test
    fun onRestoreStateReloadsSelectedStopWhenStopCodeIsDifferent() {
        viewModel.showMapMarkerBubble.observeForever(selectedStopObserver)

        viewModel.onRestoreState(null, "123456")

        verify(repository)
                .getBusStop("123456")
    }

    private fun callOnCleared() {
        // This is required because Kotlin can't access the protected method.
        val method = viewModel.javaClass.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(viewModel)
    }
}