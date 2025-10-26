/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favouritestops

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
import uk.org.rivernile.android.bustracker.ui.theme.MyBusTheme

/**
 * This [Fragment] shows the user a list of their favourite stops.
 *
 * How this [Fragment] behaves depends on if the hosting [android.app.Activity] implements
 * [CreateShortcutCallbacks] or not. When a user selects a favourite stop, if this interface is
 * implemented, it asks this interface to create a shortcut. If this interface is not implemented,
 * it instead asks the hosting [android.app.Activity] to show the live times instead.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
public class FavouriteStopsFragment : Fragment() {

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = content {
        MyBusTheme {
            FavouriteStopsScreen(
                modifier = Modifier
                    .consumeWindowInsets(WindowInsets.safeDrawing.only(WindowInsetsSides.Vertical))
            )
        }
    }

    /**
     * Activities which host this [Fragment] in the create shortcut mode should implement this
     * interface. When this is the case, this [Fragment] will run in create shortcut mode, and pass
     * create shortcut events through this interface back to the [android.app.Activity] when the
     * user has selected a stop.
     */
    internal interface CreateShortcutCallbacks {

        /**
         * This is called when the user has selected a stop and a shortcut should be created for it.
         *
         * @param stopCode The stop code to create a shortcut for.
         * @param stopName The user's name for the stop at the time they requested the shortcut be
         * created.
         */
        fun onCreateShortcut(stopCode: String, stopName: String)
    }
}
