/*
 * Copyright (C) 2022 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.main

import android.content.Intent
import android.os.Build
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.matcher.IntentMatchers.hasComponent
import androidx.test.espresso.intent.rule.IntentsRule
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import androidx.test.filters.LargeTest
import androidx.test.filters.SdkSuppress
import org.junit.Rule
import uk.org.rivernile.android.bustracker.ui.about.AboutActivity
import uk.org.rivernile.android.bustracker.ui.alerts.AlertManagerFragment
import uk.org.rivernile.android.bustracker.ui.explore.ExploreFragment
import uk.org.rivernile.android.bustracker.ui.favourites.FavouriteStopsFragment
import uk.org.rivernile.android.bustracker.ui.news.TwitterUpdatesFragment
import uk.org.rivernile.android.bustracker.ui.settings.SettingsActivity
import uk.org.rivernile.edinburghbustracker.android.R
import kotlin.test.Test
import kotlin.test.assertIs

/**
 * [android.app.Activity] tests cases for [MainActivity].
 *
 * @author Niall Scott
 */
@LargeTest
class MainActivityTest {

    @get:Rule
    val intentsRule = IntentsRule()

    @Test
    fun showsFavouriteStopsByDefault() {
        launchActivity<MainActivity>().use { scenario ->
            scenario.onActivity { activity ->
                activity.supportFragmentManager.executePendingTransactions()

                val topLevelFragment = activity.currentFragment as ExploreFragment
                val currentFragment = topLevelFragment
                    .childFragmentManager
                    .findFragmentById(R.id.fragmentContainer)
                assertIs<FavouriteStopsFragment>(currentFragment)
            }
        }
    }

    @Test
    fun showsAlertManagerWhenHasIntentActionManageAlerts() {
        launchActivity<MainActivity>(Intent(MainActivity.ACTION_MANAGE_ALERTS)).use { scenario ->
            scenario.onActivity { activity ->
                activity.supportFragmentManager.executePendingTransactions()

                assertIs<AlertManagerFragment>(activity.currentFragment)
            }
        }
    }

    // TODO: remove suppression after updating testing dependencies.
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.S)
    @Test
    fun showsSettingsActivityWhenSettingsMenuItemClicked() {
        launchActivity<MainActivity>().use {
            openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
            onView(withText(R.string.preferences_title))
                .perform(click())
        }

        intended(hasComponent(SettingsActivity::class.java.name))
    }

    // TODO: remove suppression after updating testing dependencies.
    @SdkSuppress(maxSdkVersion = Build.VERSION_CODES.S)
    @Test
    fun showsAboutActivityWhenAboutMenuItemClicked() {
        launchActivity<MainActivity>().use {
            openActionBarOverflowOrOptionsMenu(ApplicationProvider.getApplicationContext())
            onView(withText(R.string.about_title))
                .perform(click())
        }

        intended(hasComponent(AboutActivity::class.java.name))
    }

    @Test
    fun showsExploreFragmentWhenExploreNavigationItemClicked() {
        launchActivity<MainActivity>().use { scenario ->
            scenario.onActivity { activity ->
                activity.supportFragmentManager.executePendingTransactions()
            }

            onView(withId(R.id.main_navigation_explore))
                .perform(click())

            scenario.onActivity { activity ->
                activity.supportFragmentManager.executePendingTransactions()

                assertIs<ExploreFragment>(activity.currentFragment)
            }
        }
    }

    @Test
    fun showsTwitterUpdatesFragmentWhenUpdatesNavigationItemClicked() {
        launchActivity<MainActivity>().use { scenario ->
            scenario.onActivity { activity ->
                activity.supportFragmentManager.executePendingTransactions()
            }

            onView(withId(R.id.main_navigation_updates))
                .perform(click())

            scenario.onActivity { activity ->
                activity.supportFragmentManager.executePendingTransactions()

                assertIs<TwitterUpdatesFragment>(activity.currentFragment)
            }
        }
    }

    @Test
    fun showsAlertManagerFragmentWhenAlertsNavigationItemClicked() {
        launchActivity<MainActivity>().use { scenario ->
            scenario.onActivity { activity ->
                activity.supportFragmentManager.executePendingTransactions()
            }

            onView(withId(R.id.main_navigation_alerts))
                .perform(click())

            scenario.onActivity { activity ->
                activity.supportFragmentManager.executePendingTransactions()

                assertIs<AlertManagerFragment>(activity.currentFragment)
            }
        }
    }

    private val MainActivity.currentFragment get() =
        supportFragmentManager.findFragmentById(R.id.fragmentContainer)
}