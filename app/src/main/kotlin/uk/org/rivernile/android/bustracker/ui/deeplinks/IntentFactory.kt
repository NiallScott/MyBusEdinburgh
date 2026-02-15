/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.deeplinks

import android.content.Context
import android.content.Intent
import dagger.hilt.android.qualifiers.ApplicationContext
import uk.org.rivernile.android.bustracker.core.domain.NaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity
import javax.inject.Inject

/**
 * The implementation of any [Intent] factories which exist in the app.
 *
 * @param context The application [Context].
 * @author Niall Scott
 */
internal class IntentFactory @Inject constructor(
    @param:ApplicationContext private val context: Context
) : BusTimesIntentFactory {

    override fun createBusTimesIntent(stopIdentifier: StopIdentifier): Intent {
        return Intent(context, DisplayStopDataActivity::class.java)
            .setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA)
            .putExtra(
                DisplayStopDataActivity.EXTRA_STOP_CODE,
                stopIdentifier.toNaptanStopCodeOrThrow()
            )
    }

    private fun StopIdentifier.toNaptanStopCodeOrThrow(): String {
        return if (this is NaptanStopIdentifier) {
            naptanStopCode
        } else {
            throw UnsupportedOperationException("Only Naptan identifiers are supported for now.")
        }
    }
}
