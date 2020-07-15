/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.deeplinking

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapActivity
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity
import uk.org.rivernile.android.bustracker.ui.main.MainActivity

/**
 * Tests for [AppDeeplinkIntentFactory].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AppDeeplinkIntentFactoryTest {

    @Mock
    private lateinit var featureRepository: FeatureRepository

    private lateinit var factory: AppDeeplinkIntentFactory

    @Before
    fun setUp() {
        factory = AppDeeplinkIntentFactory(
                ApplicationProvider.getApplicationContext(),
                featureRepository)
    }

    @Test
    fun createShowBusTimesIntentCreatesExpectedIntent() {
        val context: Context = ApplicationProvider.getApplicationContext()

        val result = factory.createShowBusTimesIntent("123456")

        val componentName = result.component
        assertEquals(context.packageName, componentName?.packageName)
        assertEquals(DisplayStopDataActivity::class.java.name, componentName?.className)
        assertEquals(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA, result.action)
        assertEquals("123456", result.getStringExtra(DisplayStopDataActivity.EXTRA_STOP_CODE))
    }

    @Test
    fun createShowStopOnMapIntentReturnsNullWhenStopMapUiFeatureIsNotEnabled() {
        whenever(featureRepository.hasStopMapUiFeature())
                .thenReturn(false)

        val result = factory.createShowStopOnMapIntent("123456")

        assertNull(result)
    }

    @Test
    fun createShowStopOnMapIntentCreatesExpectedIntentWhenStopMapUiFeatureIsEnabled() {
        val context: Context = ApplicationProvider.getApplicationContext()
        whenever(featureRepository.hasStopMapUiFeature())
                .thenReturn(true)

        val result = factory.createShowStopOnMapIntent("123456")

        val componentName = result?.component
        assertEquals(context.packageName, componentName?.packageName)
        assertEquals(BusStopMapActivity::class.java.name, componentName?.className)
        assertEquals("123456", result?.getStringExtra(BusStopMapActivity.EXTRA_STOP_CODE))
    }

    @Test
    fun createManageAlertsIntentCreatesExpectedIntent() {
        val context: Context = ApplicationProvider.getApplicationContext()

        val result = factory.createManageAlertsIntent()

        assertNull(result.component)
        assertEquals(context.packageName, result.`package`)
        assertEquals(MainActivity.ACTION_MANAGE_ALERTS, result.action)
    }
}