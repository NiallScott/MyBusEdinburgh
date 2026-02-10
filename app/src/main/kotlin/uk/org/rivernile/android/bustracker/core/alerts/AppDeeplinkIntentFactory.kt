/*
 * Copyright (C) 2020 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.alerts

import android.content.Context
import android.content.Intent
import uk.org.rivernile.android.bustracker.core.domain.NaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.features.FeatureRepository
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapActivity
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity
import uk.org.rivernile.android.bustracker.ui.main.MainActivity
import javax.inject.Inject

/**
 * The app specific implementation of [DeeplinkIntentFactory].
 *
 * @param context The application [Context].
 * @param featureRepository Used to determine if specific features are available.
 * @author Niall Scott
 */
class AppDeeplinkIntentFactory @Inject constructor(
    private val context: Context,
    private val featureRepository: FeatureRepository
) : DeeplinkIntentFactory {

    override fun createShowBusTimesIntent(stopIdentifier: StopIdentifier): Intent =
        Intent(context, DisplayStopDataActivity::class.java)
            .setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA)
            .putExtra(
                DisplayStopDataActivity.EXTRA_STOP_CODE,
                stopIdentifier.toNaptanStopCodeOrThrow()
            )

    override fun createShowStopOnMapIntent(stopIdentifier: StopIdentifier): Intent? {
        return if (featureRepository.hasStopMapUiFeature) {
            Intent(context, BusStopMapActivity::class.java)
                .putExtra(
                    BusStopMapActivity.EXTRA_STOP_CODE,
                    stopIdentifier.toNaptanStopCodeOrThrow()
                )
        } else {
            null
        }
    }

    override fun createManageAlertsIntent() =
        Intent(MainActivity.ACTION_MANAGE_ALERTS)
            .setPackage(context.packageName)

    private fun StopIdentifier.toNaptanStopCodeOrThrow(): String {
        return if (this is NaptanStopIdentifier) {
            naptanStopCode
        } else {
            throw UnsupportedOperationException("Atco codes are not yet supported.")
        }
    }
}
