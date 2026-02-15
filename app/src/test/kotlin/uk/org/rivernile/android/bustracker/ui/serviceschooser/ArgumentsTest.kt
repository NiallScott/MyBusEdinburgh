/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import uk.org.rivernile.android.bustracker.core.domain.ParcelableServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.toParcelableNaptanStopIdentifier
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [Arguments].
 *
 * @author Niall Scott
 */
class ArgumentsTest {

    @Test
    fun paramsFlowIsNullByDefault() = runTest {
        val arguments = Arguments(SavedStateHandle())

        val result = arguments.paramsFlow.first()

        assertNull(result)
    }

    @Test
    fun paramsFlowEmitsAllServicesWhenSetAsArgument() = runTest {
        val allServices = ServicesChooserParams.AllServices(
            titleResId = 1,
            selectedServices = listOf(
                ParcelableServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST1"
                ),
                ParcelableServiceDescriptor(
                    serviceName = "2",
                    operatorCode = "TEST2"
                ),
                ParcelableServiceDescriptor(
                    serviceName = "3",
                    operatorCode = "TEST3"
                )
            )
        )
        val arguments = Arguments(
            SavedStateHandle(
                mapOf(
                    Arguments.STATE_PARAMS to allServices
                )
            )
        )

        val result = arguments.paramsFlow.first()

        assertEquals(allServices, result)
    }

    @Test
    fun paramsFlowEmitsStopWhenSetAsArgument() = runTest {
        val stop = ServicesChooserParams.Stop(
            titleResId = 1,
            selectedServices = listOf(
                ParcelableServiceDescriptor(
                    serviceName = "1",
                    operatorCode = "TEST1"
                ),
                ParcelableServiceDescriptor(
                    serviceName = "2",
                    operatorCode = "TEST2"
                ),
                ParcelableServiceDescriptor(
                    serviceName = "3",
                    operatorCode = "TEST3"
                )
            ),
            stopIdentifier = "123456".toParcelableNaptanStopIdentifier()
        )
        val arguments = Arguments(
            SavedStateHandle(
                mapOf(
                    Arguments.STATE_PARAMS to stop
                )
            )
        )

        val result = arguments.paramsFlow.first()

        assertEquals(stop, result)
    }
}
