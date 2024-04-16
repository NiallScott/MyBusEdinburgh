/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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
import android.net.Uri
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import javax.inject.Inject

/**
 * An implementation which launches actions for the 'About' screen.
 *
 * @param exceptionLogger Used to log exception events.
 * @author Niall Scott
 */
internal class AboutActionLauncher @Inject constructor(
    private val exceptionLogger: ExceptionLogger
) {

    /**
     * Launch the app's store listing, i.e. the Play Store.
     *
     * @param context The [android.app.Activity] [Context].
     */
    fun launchStoreListing(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse("market://details?id=${context.packageName}")
        }

        launchIntent(context, intent)
    }

    /**
     * Launch the app author's website in the default web browser.
     *
     * @param context The [android.app.Activity] [Context].
     */
    fun launchAuthorWebsite(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(context.getString(R.string.app_author_website))
        }

        launchIntent(context, intent)
    }

    /**
     * Launch the app's website in the default web browser.
     *
     * @param context The [android.app.Activity] [Context].
     */
    fun launchAppWebsite(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(context.getString(R.string.app_website))
        }

        launchIntent(context, intent)
    }

    /**
     * Launch the app's Twitter account either with the installed app or the default web browser.
     *
     * @param context The [android.app.Activity] [Context].
     */
    fun launchAppTwitter(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(context.getString(R.string.app_twitter))
        }

        launchIntent(context, intent)
    }

    /**
     * Launch the app's privacy policy in the default web browser.
     */
    fun launchPrivacyPolicy(context: Context) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(context.getString(R.string.app_privacy_policy))
        }

        launchIntent(context, intent)
    }

    /**
     * Launch a given [intent]. If no handling [android.app.Activity] can be found, this will be
     * caught and the exception logger will be informed.
     *
     * @param context The [android.app.Activity] [Context].
     * @param intent The [Intent] to launch.
     */
    private fun launchIntent(context: Context, intent: Intent) {
        try {
            context.startActivity(intent)
        } catch (e: ActivityNotFoundException) {
            exceptionLogger.log(e)
            // Fail silently.
        }
    }
}