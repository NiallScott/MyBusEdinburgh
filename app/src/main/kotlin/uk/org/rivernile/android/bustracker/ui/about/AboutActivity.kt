/*
 * Copyright (C) 2015 - 2018 Niall 'Rivernile' Scott
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
 */

package uk.org.rivernile.android.bustracker.ui.about

import android.os.Bundle
import androidx.fragment.app.Fragment
import androidx.appcompat.app.AppCompatActivity
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.support.HasSupportFragmentInjector
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject

/**
 * This [android.app.Activity] hosts [AboutFragment] to show application 'about' information.
 *
 * @author Niall Scott
 */
class AboutActivity : AppCompatActivity(), HasSupportFragmentInjector, AboutFragment.Callbacks {

    companion object {

        private const val DIALOG_CREDITS = "creditsDialog"
        private const val DIALOG_LICENCES = "licencesDialog"
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Fragment>

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.about)
    }

    override fun supportFragmentInjector() = dispatchingAndroidInjector

    override fun onShowCredits() {
        CreditsDialogFragment().show(supportFragmentManager, DIALOG_CREDITS)
    }

    override fun onShowLicences() {
        OpenSourceLicenceDialogFragment().show(supportFragmentManager, DIALOG_LICENCES)
    }
}