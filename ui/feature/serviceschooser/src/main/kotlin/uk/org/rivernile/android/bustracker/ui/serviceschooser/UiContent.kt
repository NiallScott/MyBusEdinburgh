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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import androidx.compose.runtime.Immutable
import kotlinx.collections.immutable.ImmutableList

/**
 * This represents the current content state of the services chooser UI.
 *
 * @author Niall Scott
 */
@Immutable
internal sealed interface UiContent {

    /**
     * The data is currently loading.
     */
    data object InProgress : UiContent

    /**
     * The data is loaded and there are items to show.
     *
     * @property items The items to show.
     */
    data class Content(
        val items: ImmutableList<UiServiceChooserItem>
    ) : UiContent

    /**
     * The data is loaded and there was an error.
     */
    sealed interface Error : UiContent {

        /**
         * There are no services to show. This is shown when the services chooser is in the all
         * services mode.
         */
        data object NoGlobalServices : Error

        /**
         * There are no services to show. This is shown when the services chooser is in the stop
         * mode.
          */
        data object NoServicesForStop : Error
    }
}
