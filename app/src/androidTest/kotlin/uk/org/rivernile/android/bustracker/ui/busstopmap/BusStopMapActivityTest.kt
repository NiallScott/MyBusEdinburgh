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

import android.Manifest
import android.content.Context
import android.content.Intent
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.intent.Intents
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.matcher.IntentMatchers.hasExtra
import androidx.test.filters.LargeTest
import androidx.test.rule.GrantPermissionRule
import org.hamcrest.CoreMatchers.allOf
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.org.rivernile.android.bustracker.core.bundle.getParcelableCompat
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity
import uk.org.rivernile.edinburghbustracker.android.R

/**
 * [android.app.Activity] test cases for [BusStopMapActivity].
 *
 * @author Niall Scott
 */
@LargeTest
class BusStopMapActivityTest {

    @get:Rule
    val permissionRule: GrantPermissionRule =
            GrantPermissionRule.grant(Manifest.permission.ACCESS_FINE_LOCATION)

    @Before
    fun setUp() {
        Intents.init()
    }

    @After
    fun tearDown() {
        Intents.release()
    }

    @Test
    fun startingActivityWithNoArgumentsInIntentDoesNotSetStopCodeOrLatLon() {
        launchActivity<BusStopMapActivity>().use { scenario ->
            scenario.onActivity { activity ->
                assertNull(activity.busStopMapFragment.arguments)
            }
        }
    }

    @Test
    fun startingActivityWithStopCodeArgumentSetsStopCodeInFragment() {
        val intent = Intent(applicationContext, BusStopMapActivity::class.java)
                .putExtra(BusStopMapActivity.EXTRA_STOP_CODE, "123456")

        launchActivity<BusStopMapActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val arguments = requireNotNull(activity.busStopMapFragment.arguments)
                assertEquals("123456", arguments.getString("stopCode"))
                assertFalse(arguments.containsKey("latitude"))
                assertFalse(arguments.containsKey("longitude"))
            }
        }
    }

    @Test
    fun startActivityWithLatLonArgumentsSetsLatLonInFragment() {
        val intent = Intent(applicationContext, BusStopMapActivity::class.java)
                .putExtra(BusStopMapActivity.EXTRA_LATITUDE, 1.0)
                .putExtra(BusStopMapActivity.EXTRA_LONGITUDE, 2.0)
        val expectedLocation = UiLatLon(1.0, 2.0)

        launchActivity<BusStopMapActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                val arguments = requireNotNull(activity.busStopMapFragment.arguments)
                assertFalse(arguments.containsKey("stopCode"))
                assertEquals(expectedLocation, arguments.getParcelableCompat("location"))
            }
        }
    }

    @Test
    fun startingActivityWithLatitudeButNoLongitudeArgumentsIgnoresArguments() {
        val intent = Intent(applicationContext, BusStopMapActivity::class.java)
                .putExtra(BusStopMapActivity.EXTRA_LATITUDE, 1.0)

        launchActivity<BusStopMapActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertNull(activity.busStopMapFragment.arguments)
            }
        }
    }

    @Test
    fun startingActivityWithLongitudeButNoLatitudeArgumentsIgnoresArguments() {
        val intent = Intent(applicationContext, BusStopMapActivity::class.java)
                .putExtra(BusStopMapActivity.EXTRA_LONGITUDE, 2.0)

        launchActivity<BusStopMapActivity>(intent).use { scenario ->
            scenario.onActivity { activity ->
                assertNull(activity.busStopMapFragment.arguments)
            }
        }
    }

    @Test
    fun showBusTimesLaunchesBusTimesActivity() {
        launchActivity<BusStopMapActivity>().use { scenario ->
            scenario.onActivity { activity ->
                activity.onShowBusTimes("123456")
            }
        }

        intended(allOf(
                hasComponent(DisplayStopDataActivity::class.java.name),
                hasExtra(DisplayStopDataActivity.EXTRA_STOP_CODE, "123456")))
    }

    private val applicationContext get() =ApplicationProvider.getApplicationContext<Context>()

    private val BusStopMapActivity.busStopMapFragment get() =
            supportFragmentManager.findFragmentById(R.id.fragmentContainer) as BusStopMapFragment
}