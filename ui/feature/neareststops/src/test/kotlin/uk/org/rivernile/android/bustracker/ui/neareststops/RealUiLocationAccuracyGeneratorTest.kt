/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.location.FakeLocationRepository
import uk.org.rivernile.android.bustracker.core.location.LocationRepository
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.core.preferences.FakePreferenceRepository
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceRepository
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [RealUiLocationAccuracyGenerator].
 *
 * @author Niall Scott
 */
class RealUiLocationAccuracyGeneratorTest {

    @Test
    fun getUiLocationAccuracyFlowEmitsNullWhenGpsPromptedIsDisabled() = runTest {
        val generator = createGenerator(
            locationRepository = FakeLocationRepository(
                onIsGpsLocationProviderEnabledFlow = { flowOf(false) },
                onHasGpsLocationProvider = { false }
            ),
            preferenceRepository = FakePreferenceRepository(
                onIsGpsPromptDisabledFlow = { flowOf(true) }
            )
        )

        generator.getUiLocationAccuracyFlow(coarseGrantedButFineUngrantedPermissionsState).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiLocationAccuracyFlowEmitsGpsNotPresentWhenDeviceHasNoGpsFeature() = runTest {
        val generator = createGenerator(
            locationRepository = FakeLocationRepository(
                onIsGpsLocationProviderEnabledFlow = { flowOf(false) },
                onHasGpsLocationProvider = { false }
            ),
            preferenceRepository = FakePreferenceRepository(
                onIsGpsPromptDisabledFlow = { flowOf(false) }
            )
        )

        generator.getUiLocationAccuracyFlow(coarseGrantedButFineUngrantedPermissionsState).test {
            assertEquals(UiLocationAccuracy.GPS_NOT_PRESENT, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiLocationAccuracyFlowEmitsPermissionsNotSufficientWhenOnlyFineLocation() = runTest {
        val generator = createGenerator(
            locationRepository = FakeLocationRepository(
                onIsGpsLocationProviderEnabledFlow = { flowOf(false) },
                onHasGpsLocationProvider = { true }
            ),
            preferenceRepository = FakePreferenceRepository(
                onIsGpsPromptDisabledFlow = { flowOf(false) }
            )
        )

        generator.getUiLocationAccuracyFlow(coarseGrantedButFineUngrantedPermissionsState).test {
            assertEquals(UiLocationAccuracy.PERMISSIONS_NOT_SUFFICIENT, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiLocationAccuracyFlowEmitsGpsDisabledWhenGpsProviderIsDisabled() = runTest {
        val generator = createGenerator(
            locationRepository = FakeLocationRepository(
                onIsGpsLocationProviderEnabledFlow = { flowOf(false) },
                onHasGpsLocationProvider = { true }
            ),
            preferenceRepository = FakePreferenceRepository(
                onIsGpsPromptDisabledFlow = { flowOf(false) }
            )
        )

        generator.getUiLocationAccuracyFlow(allPermissionsGrantedPermissionsState).test {
            assertEquals(UiLocationAccuracy.GPS_DISABLED, awaitItem())
            awaitComplete()
        }
    }

    @Test
    fun getUiLocationAccuracyFlowEmitsNullWhenAllConditionsAreSatisfied() = runTest {
        val generator = createGenerator(
            locationRepository = FakeLocationRepository(
                onIsGpsLocationProviderEnabledFlow = { flowOf(true) },
                onHasGpsLocationProvider = { true }
            ),
            preferenceRepository = FakePreferenceRepository(
                onIsGpsPromptDisabledFlow = { flowOf(false) }
            )
        )

        generator.getUiLocationAccuracyFlow(allPermissionsGrantedPermissionsState).test {
            assertNull(awaitItem())
            awaitComplete()
        }
    }

    private fun createGenerator(
        locationRepository: LocationRepository = FakeLocationRepository(),
        preferenceRepository: PreferenceRepository = FakePreferenceRepository()
    ): RealUiLocationAccuracyGenerator {
        return RealUiLocationAccuracyGenerator(
            locationRepository = locationRepository,
            preferenceRepository = preferenceRepository
        )
    }

    private val coarseGrantedButFineUngrantedPermissionsState: PermissionsState get() {
        return PermissionsState(
            fineLocationPermission = PermissionState.UNGRANTED,
            coarseLocationPermission = PermissionState.GRANTED
        )
    }

    private val allPermissionsGrantedPermissionsState: PermissionsState get() {
        return PermissionsState(
            fineLocationPermission = PermissionState.GRANTED,
            coarseLocationPermission = PermissionState.GRANTED
        )
    }
}
