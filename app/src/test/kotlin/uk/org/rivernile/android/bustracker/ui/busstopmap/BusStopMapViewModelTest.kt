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
import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.ExperimentalCoroutinesApi
import org.junit.Before
import org.junit.Rule
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.busstops.BusStopsRepository
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule

/**
 * Tests for [BusStopMapViewModel].
 *
 * @author Niall Scott
 */
@OptIn(ExperimentalCoroutinesApi::class)
//@RunWith(MockitoJUnitRunner::class)
class BusStopMapViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var playServicesAvailabilityChecker: PlayServicesAvailabilityChecker
    @Mock
    private lateinit var locationRepository: LocationRepository
    @Mock
    private lateinit var servicesRepository: ServicesRepository
    @Mock
    private lateinit var busStopsRepository: BusStopsRepository
    @Mock
    private lateinit var serviceListingRetriever: ServiceListingRetriever
    @Mock
    private lateinit var routeLineRetriever: RouteLineRetriever
    @Mock
    private lateinit var isMyLocationEnabledDetector: IsMyLocationEnabledDetector
    @Mock
    private lateinit var preferenceRepository: PreferenceRepository
    @Mock
    private lateinit var preferenceManager: PreferenceManager

    private lateinit var viewModel: BusStopMapViewModel

    @Before
    fun setUp() {
        viewModel = BusStopMapViewModel(
                SavedStateHandle(),
                playServicesAvailabilityChecker,
                locationRepository,
                servicesRepository,
                busStopsRepository,
                serviceListingRetriever,
                routeLineRetriever,
                isMyLocationEnabledDetector,
                preferenceRepository,
                preferenceManager,
                coroutineRule.testDispatcher)
    }
}