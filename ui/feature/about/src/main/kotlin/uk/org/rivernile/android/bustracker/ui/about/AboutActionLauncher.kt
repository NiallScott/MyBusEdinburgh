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

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.ActivityScoped
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import javax.inject.Inject
import androidx.core.net.toUri

/**
 * Launches actions for the 'about' screen.
 *
 * @author Niall Scott
 */
internal interface AboutActionLauncher {

    /**
     * Launch the app's store listing, i.e. the Play Store.
     */
    fun launchStoreListing()

    /**
     * Launch the app author's website in the default web browser.
     */
    fun launchAuthorWebsite()

    /**
     * Launch the app's website in the default web browser.
     */
    fun launchAppWebsite()

    /**
     * Launch the app's Twitter account either with the installed app or the default web browser.
     */
    fun launchAppTwitter()

    /**
     * Launch the app's privacy policy in the default web browser.
     */
    fun launchPrivacyPolicy()
}

/**
 * An implementation which launches actions for the 'About' screen.
 *
 * @param context The [android.app.Activity] [Context].
 * @param exceptionLogger Used to log exception events.
 * @author Niall Scott
 */
@ActivityScoped
internal class AndroidAboutActionLauncher @Inject constructor(
    @param:ActivityContext private val context: Context,
    private val exceptionLogger: ExceptionLogger
) : AboutActionLauncher {

    /**
     * Launch the app's store listing, i.e. the Play Store.
     */
    override fun launchStoreListing() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = "market://details?id=${context.packageName}".toUri()
        }

        launchIntent(intent)
    }

    /**
     * Launch the app author's website in the default web browser.
     */
    override fun launchAuthorWebsite() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = context.getString(R.string.app_author_website).toUri()
        }

        launchIntent(intent)
    }

    /**
     * Launch the app's website in the default web browser.
     */
    override fun launchAppWebsite() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = context.getString(R.string.app_website).toUri()
        }

        launchIntent(intent)
    }

    /**
     * Launch the app's Twitter account either with the installed app or the default web browser.
     */
    override fun launchAppTwitter() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = context.getString(R.string.app_twitter).toUri()
        }

        launchIntent(intent)
    }

    /**
     * Launch the app's privacy policy in the default web browser.
     */
    override fun launchPrivacyPolicy() {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = context.getString(R.string.app_privacy_policy).toUri()
        }

        launchIntent(intent)
    }

    /**
     * Launch a given [intent]. If no handling [android.app.Activity] can be found, this will be
     * caught and the exception logger will be informed.
     *
     * @param intent The [Intent] to launch.
     */
    private fun launchIntent(intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            exceptionLogger.log(e)
            // Fail silently.
        }
    }
}