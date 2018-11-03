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

package uk.org.rivernile.android.bustracker.ui.about

import androidx.test.annotation.UiThreadTest
import androidx.test.filters.LargeTest
import androidx.test.rule.ActivityTestRule
import org.junit.Assert.assertNotNull
import org.junit.Rule
import org.junit.Test

/**
 * [android.app.Activity] test cases for [AboutActivity].
 *
 * @author Niall Scott
 */
@LargeTest
class AboutActivityTest {

    @Rule
    @JvmField
    val activityRule = ActivityTestRule(AboutActivity::class.java)

    @Test
    @UiThreadTest
    fun showCreditsCallbackPresentsCreditsDialog() {
        val activity = activityRule.activity

        activity.onShowCredits()

        val fragmentManager = activity.supportFragmentManager
        fragmentManager.executePendingTransactions()
        val fragment = fragmentManager.findFragmentByTag("creditsDialog")
        assertNotNull(fragment as? CreditsDialogFragment)
    }

    @Test
    @UiThreadTest
    fun showLicencesCallbackPresentsLicencesDialog() {
        val activity = activityRule.activity

        activity.onShowLicences()

        val fragmentManager = activity.supportFragmentManager
        fragmentManager.executePendingTransactions()
        val fragment = fragmentManager.findFragmentByTag("licencesDialog")
        assertNotNull(fragment as? OpenSourceLicenceDialogFragment)
    }
}