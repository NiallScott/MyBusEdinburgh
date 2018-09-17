/*
 * Copyright (C) 2018 Niall 'Rivernile' Scott
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

import android.content.Intent
import android.support.test.espresso.intent.Intents.intended
import android.support.test.espresso.intent.matcher.IntentMatchers.hasComponent
import android.support.test.espresso.intent.matcher.IntentMatchers.hasExtra
import android.support.test.espresso.intent.rule.IntentsTestRule
import android.support.test.filters.LargeTest
import android.support.test.runner.AndroidJUnit4
import org.hamcrest.CoreMatchers.allOf
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity
import uk.org.rivernile.edinburghbustracker.android.R

/**
 * [android.app.Activity] test cases for [BusStopMapActivity].
 *
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4::class)
@LargeTest
class BusStopMapActivityTest {

    @Rule
    @JvmField
    val activityRule = IntentsTestRule(BusStopMapActivity::class.java, false, false)

    @Test
    fun startingActivityWithNoArgumentsInIntentDoesNotSetStopCodeOrLatLon() {
        startActivity(null)

        val fragment = getBusStopMapFragment()
        assertNull(fragment.arguments)
    }

    @Test
    fun startingActivityWithStopCodeArgumentSetsStopCodeInFragment() {
        val intent = Intent()
                .putExtra(BusStopMapActivity.EXTRA_STOP_CODE, "123456")

        startActivity(intent)

        val fragment = getBusStopMapFragment()
        val arguments = fragment.arguments
        assertNotNull(arguments)
        assertEquals("123456", arguments!!.getString("stopCode"))
        assertFalse(arguments.containsKey("latitude"))
        assertFalse(arguments.containsKey("longitude"))
    }

    @Test
    fun startActivityWithLatLonArgumentsSetsLatLonInFragment() {
        val intent = Intent()
                .putExtra(BusStopMapActivity.EXTRA_LATITUDE, 1.0)
                .putExtra(BusStopMapActivity.EXTRA_LONGITUDE, 2.0)

        startActivity(intent)

        val fragment = getBusStopMapFragment()
        val arguments = fragment.arguments
        assertNotNull(arguments)
        assertFalse(arguments!!.containsKey("stopCode"))
        assertEquals(1.0, arguments.getDouble("latitude"), 0.0000000001)
        assertEquals(2.0, arguments.getDouble("longitude"), 0.0000000001)
    }

    @Test
    fun startingActivityWithLatitudeButNoLongitudeArgumentsIgnoresArguments() {
        val intent = Intent()
                .putExtra(BusStopMapActivity.EXTRA_LATITUDE, 1.0)

        startActivity(intent)

        val fragment = getBusStopMapFragment()
        assertNull(fragment.arguments)
    }

    @Test
    fun startingActivityWithLongitudeButNoLatitudeArgumentsIgnoresArguments() {
        val intent = Intent()
                .putExtra(BusStopMapActivity.EXTRA_LONGITUDE, 2.0)

        startActivity(intent)

        val fragment = getBusStopMapFragment()
        assertNull(fragment.arguments)
    }

    @Test
    fun showBusTimesLaunchesBusTimesActivity() {
        startActivity(null)

        activityRule.activity.onShowBusTimes("123456")

        intended(allOf(
                hasComponent(DisplayStopDataActivity::class.java.name),
                hasExtra(DisplayStopDataActivity.EXTRA_STOP_CODE, "123456")))
    }

    private fun startActivity(intent: Intent?) {
        activityRule.apply {
            launchActivity(intent)
            runOnUiThread {
                activity.supportFragmentManager.executePendingTransactions()
            }
        }
    }

    private fun getBusStopMapFragment() = activityRule
            .activity
            .supportFragmentManager
            .findFragmentById(R.id.fragmentContainer) as BusStopMapFragment
}