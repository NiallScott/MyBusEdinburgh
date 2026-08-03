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
import uk.org.rivernile.android.bustracker.core.services.FakeServicesRepository
import uk.org.rivernile.android.bustracker.core.services.ServicesRepository
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [RealUiActionButtonsGenerator].
 *
 * @author Niall Scott
 */
class RealUiActionButtonsGeneratorTest {

    @Test
    fun uiActionButtonsFlowEmitsDisabledServiceFilterWhenNoServicesOrNoLocationFeature() = runTest {
        val generator = createGenerator(
            locationRepository = FakeLocationRepository(
                onHasLocationFeature = { false }
            ),
            servicesRepository = FakeServicesRepository(
                onHasServicesFlow = { flowOf(false) }
            )
        )

        generator.uiActionButtonsFlow.test {
            assertEquals(
                UiActionButtons(
                    serviceFilterActionButton = UiServiceFilterActionButton(
                        isEnabled = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiActionButtonsFlowEmitsDisabledServiceFilterWhenNoServices() = runTest {
        val generator = createGenerator(
            locationRepository = FakeLocationRepository(
                onHasLocationFeature = { true }
            ),
            servicesRepository = FakeServicesRepository(
                onHasServicesFlow = { flowOf(false) }
            )
        )

        generator.uiActionButtonsFlow.test {
            assertEquals(
                UiActionButtons(
                    serviceFilterActionButton = UiServiceFilterActionButton(
                        isEnabled = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiActionButtonsFlowEmitsDisabledServiceFilterWhenNoLocationFeature() = runTest {
        val generator = createGenerator(
            locationRepository = FakeLocationRepository(
                onHasLocationFeature = { false }
            ),
            servicesRepository = FakeServicesRepository(
                onHasServicesFlow = { flowOf(true) }
            )
        )

        generator.uiActionButtonsFlow.test {
            assertEquals(
                UiActionButtons(
                    serviceFilterActionButton = UiServiceFilterActionButton(
                        isEnabled = false
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    @Test
    fun uiActionButtonsFlowEmitsEnabledServiceFilterWhenHasServicesAndLocationFeature() = runTest {
        val generator = createGenerator(
            locationRepository = FakeLocationRepository(
                onHasLocationFeature = { true }
            ),
            servicesRepository = FakeServicesRepository(
                onHasServicesFlow = { flowOf(true) }
            )
        )

        generator.uiActionButtonsFlow.test {
            assertEquals(
                UiActionButtons(
                    serviceFilterActionButton = UiServiceFilterActionButton(
                        isEnabled = true
                    )
                ),
                awaitItem()
            )
            awaitComplete()
        }
    }

    private fun createGenerator(
        locationRepository: LocationRepository = FakeLocationRepository(),
        servicesRepository: ServicesRepository = FakeServicesRepository()
    ): RealUiActionButtonsGenerator {
        return RealUiActionButtonsGenerator(
            locationRepository = locationRepository,
            servicesRepository = servicesRepository
        )
    }
}
