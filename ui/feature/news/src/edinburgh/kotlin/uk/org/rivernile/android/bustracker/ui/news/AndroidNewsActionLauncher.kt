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

package uk.org.rivernile.android.bustracker.ui.news

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.scopes.FragmentScoped
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.diversions.DiversionsActionLauncher
import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.IncidentsActionLauncher
import javax.inject.Inject

/**
 * An implementation which launches actions for the News collection of screens.
 *
 * @param context The local [android.app.Activity] [Context].
 * @param exceptionLogger Used to log exceptions which occur while trying to launch actions.
 * @author Niall Scott
 */
@FragmentScoped
internal class AndroidNewsActionLauncher @Inject constructor(
    @ActivityContext private val context: Context,
    private val exceptionLogger: ExceptionLogger
): DiversionsActionLauncher, IncidentsActionLauncher {

    override fun launchUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
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