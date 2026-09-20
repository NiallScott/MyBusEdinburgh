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

package uk.org.rivernile.android.bustracker.ui.alerts.manager

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
import androidx.compose.ui.Modifier
import androidx.fragment.app.Fragment
import androidx.fragment.compose.content
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmRemoveArrivalAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmRemoveProximityAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowSystemLocationPreferencesListener
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

/**
 * This [Fragment] allows the users to view what arrival and proximity alerts they have set and
 * allow them to delete single alerts.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
public class AlertManagerFragment : Fragment() {

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
            AlertManagerScreen(
                onShowRemoveArrivalAlert = ::handleOnShowRemoveArrivalAlert,
                onShowRemoveProximityAlert = ::handleOnShowRemoveProximityAlert,
                onShowLocationSettings = ::handleOnShowLocationSettings,
                modifier = Modifier
                    .consumeWindowInsets(
                        WindowInsets.safeDrawing.only(WindowInsetsSides.Top)
                    )
            )
        }
    }

    override fun onDetach() {
        super.onDetach()

        callbacks = null
    }

    private fun handleOnShowRemoveArrivalAlert(stopIdentifier: StopIdentifier) {
        callbacks?.onShowConfirmRemoveArrivalAlert(stopIdentifier = stopIdentifier)
    }

    private fun handleOnShowRemoveProximityAlert(stopIdentifier: StopIdentifier) {
        callbacks?.onShowConfirmRemoveProximityAlert(stopIdentifier = stopIdentifier)
    }

    private fun handleOnShowLocationSettings() {
        callbacks?.onShowSystemLocationPreferences()
    }

    /**
     * Any [android.app.Activity] which host this [Fragment] must implement this interface to handle
     * navigation events.
     */
    public interface Callbacks :
        OnShowConfirmRemoveArrivalAlertListener,
        OnShowConfirmRemoveProximityAlertListener,
        OnShowSystemLocationPreferencesListener
}
