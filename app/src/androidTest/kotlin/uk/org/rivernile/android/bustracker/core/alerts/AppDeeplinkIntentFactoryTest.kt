/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import uk.org.rivernile.android.bustracker.core.domain.toAtcoStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toNaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FakeFeatureRepository
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapActivity
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity
import uk.org.rivernile.android.bustracker.ui.main.MainActivity
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

/**
 * Tests for [AppDeeplinkIntentFactory].
 *
 * @author Niall Scott
 */
class AppDeeplinkIntentFactoryTest {

    @Test(expected = UnsupportedOperationException::class)
    fun createShowBusTimesIntentThrowsExceptionWhenIdentifierIsAtco() {
        val factory = createAppDeeplinkIntentFactory()

        factory.createShowBusTimesIntent("123456".toAtcoStopIdentifier())
    }

    @Test
    fun createShowBusTimesIntentCreatesExpectedIntent() {
        val factory = createAppDeeplinkIntentFactory()
        val context = ApplicationProvider.getApplicationContext<Context>()

        val result = factory.createShowBusTimesIntent("123456".toNaptanStopIdentifier())

        val componentName = result.component
        assertEquals(context.packageName, componentName?.packageName)
        assertEquals(DisplayStopDataActivity::class.java.name, componentName?.className)
        assertEquals(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA, result.action)
        assertEquals("123456", result.getStringExtra(DisplayStopDataActivity.EXTRA_STOP_CODE))
    }

    @Test(expected = UnsupportedOperationException::class)
    fun createShowStopOnMapIntentThrowsExceptionWhenIdentifierIsAtco() {
        val factory = createAppDeeplinkIntentFactory(
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { true }
            )
        )

        factory.createShowStopOnMapIntent("123456".toAtcoStopIdentifier())
    }

    @Test
    fun createShowStopOnMapIntentReturnsNullWhenStopMapUiFeatureIsNotEnabled() {
        val factory = createAppDeeplinkIntentFactory(
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { false }
            )
        )

        val result = factory.createShowStopOnMapIntent("123456".toNaptanStopIdentifier())

        assertNull(result)
    }

    @Test
    fun createShowStopOnMapIntentCreatesExpectedIntentWhenStopMapUiFeatureIsEnabled() {
        val factory = createAppDeeplinkIntentFactory(
            featureRepository = FakeFeatureRepository(
                onHasStopMapUiFeature = { true }
            )
        )
        val context = ApplicationProvider.getApplicationContext<Context>()

        val result = factory.createShowStopOnMapIntent("123456".toNaptanStopIdentifier())

        val componentName = result?.component
        assertEquals(context.packageName, componentName?.packageName)
        assertEquals(BusStopMapActivity::class.java.name, componentName?.className)
        assertEquals("123456", result?.getStringExtra(BusStopMapActivity.EXTRA_STOP_CODE))
    }

    @Test
    fun createManageAlertsIntentCreatesExpectedIntent() {
        val factory = createAppDeeplinkIntentFactory()
        val context = ApplicationProvider.getApplicationContext<Context>()

        val result = factory.createManageAlertsIntent()

        assertNull(result.component)
        assertEquals(context.packageName, result.`package`)
        assertEquals(MainActivity.ACTION_MANAGE_ALERTS, result.action)
    }

    private fun createAppDeeplinkIntentFactory(
        featureRepository: FeatureRepository = FakeFeatureRepository()
    ): AppDeeplinkIntentFactory {
        return AppDeeplinkIntentFactory(
            ApplicationProvider.getApplicationContext(),
            featureRepository
        )
    }
}
