/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.about

import android.app.Activity
import android.app.Instrumentation
import android.content.Context
import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.test.core.app.ApplicationProvider
import androidx.test.core.app.launchActivity
import androidx.test.espresso.intent.Intents.intended
import androidx.test.espresso.intent.Intents.intending
import androidx.test.espresso.intent.matcher.IntentMatchers.hasAction
import androidx.test.espresso.intent.matcher.IntentMatchers.hasData
import androidx.test.espresso.intent.matcher.IntentMatchers.isInternal
import androidx.test.espresso.intent.rule.IntentsRule
import org.hamcrest.Matchers.allOf
import org.hamcrest.Matchers.not
import org.junit.Rule
import uk.org.rivernile.android.bustracker.core.log.FakeExceptionLogger
import kotlin.test.BeforeTest
import kotlin.test.Test

/**
 * Tests for [AboutActionLauncher].
 *
 * @author Niall Scott
 */
class AndroidAboutActionLauncherTest {

    @get:Rule
    val intentsRule = IntentsRule()

    private val exceptionLogger = FakeExceptionLogger()

    @BeforeTest
    fun setUp() {
        // By default Espresso Intents does not mock Intents, and will launch the targeted
        // components. This stops the launch from happening against external components.
        intending(not(isInternal()))
            .respondWith(Instrumentation.ActivityResult(Activity.RESULT_OK, null))
    }

    @Test
    fun launchStoreListingLaunchesIntent() {
        val myPackageName = ApplicationProvider.getApplicationContext<Context>().packageName

        launchActivity<ComponentActivity>().use { scenario ->
            scenario.onActivity {
                createAboutActionLauncher(it).launchStoreListing()
            }
        }

        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData("market://details?id=${myPackageName}")
            )
        )
    }

    @Test
    fun launchAuthorWebsiteLaunchesIntent() {
        val websiteUrl = ApplicationProvider
            .getApplicationContext<Context>()
            .getString(R.string.app_author_website)

        launchActivity<ComponentActivity>().use { scenario ->
            scenario.onActivity {
                createAboutActionLauncher(it).launchAuthorWebsite()
            }
        }

        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData(websiteUrl)
            )
        )
    }

    @Test
    fun launchAppWebsiteLaunchesIntent() {
        val websiteUrl = ApplicationProvider
            .getApplicationContext<Context>()
            .getString(R.string.app_website)

        launchActivity<ComponentActivity>().use { scenario ->
            scenario.onActivity {
                createAboutActionLauncher(it).launchAppWebsite()
            }
        }

        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData(websiteUrl)
            )
        )
    }

    @Test
    fun launchAppBlueskyLaunchesIntent() {
        val url = ApplicationProvider
            .getApplicationContext<Context>()
            .getString(R.string.app_bluesky)

        launchActivity<ComponentActivity>().use { scenario ->
            scenario.onActivity {
                createAboutActionLauncher(it).launchAppBluesky()
            }
        }

        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData(url)
            )
        )
    }

    @Test
    fun launchPrivacyPolicyLaunchesIntent() {
        val url = ApplicationProvider
            .getApplicationContext<Context>()
            .getString(R.string.app_privacy_policy)

        launchActivity<ComponentActivity>().use { scenario ->
            scenario.onActivity {
                createAboutActionLauncher(it).launchPrivacyPolicy()
            }
        }

        intended(
            allOf(
                hasAction(Intent.ACTION_VIEW),
                hasData(url)
            )
        )
    }

    private fun createAboutActionLauncher(context: Context): AndroidAboutActionLauncher {
        return AndroidAboutActionLauncher(context, exceptionLogger)
    }
}
