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

package uk.org.rivernile.android.bustracker.ui.stopdetails

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusStopMapWithStopIdentifierListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowSystemLocationPreferencesListener
import uk.org.rivernile.android.bustracker.ui.formatters.LocalNumberFormatter
import uk.org.rivernile.android.bustracker.ui.formatters.rememberNumberFormatter
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

/**
 * This [Fragment] shows the user details pertaining to a given stop code.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
public class StopDetailsFragment : Fragment() {

    private var callbacks: Callbacks? = null

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = context as? Callbacks
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = content {
        MyBusTheme {
            CompositionLocalProvider(
                LocalNumberFormatter provides rememberNumberFormatter(
                    maximumFractionDigits = 2
                )
            ) {
                StopDetailsScreen(
                    onShowOnMap = ::handleOnShowOnMap,
                    onRequestLocationPermissions = ::handleOnRequestLocationPermissions,
                    onShowLocationSettings = ::handleOnShowLocationSettings,
                    modifier = Modifier
                        .consumeWindowInsets(
                            WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                        )
                )
            }
        }
    }

    override fun onDetach() {
        super.onDetach()

        callbacks = null
    }

    private fun handleOnShowOnMap(stopIdentifier: StopIdentifier) {
        callbacks?.onShowBusStopMapWithStopIdentifier(stopIdentifier)
    }

    private fun handleOnRequestLocationPermissions() {

    }

    private fun handleOnShowLocationSettings() {
        callbacks?.onShowSystemLocationPreferences()
    }

    /**
     * Activities which host this [Fragment] should implement this interface.
     */
    public interface Callbacks :
        OnShowBusStopMapWithStopIdentifierListener,
        OnShowSystemLocationPreferencesListener
}
