/*
 * Copyright (C) 2021 - 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites.addedit

import uk.org.rivernile.android.bustracker.core.busstops.StopName
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.favourites.FavouriteStop

/**
 * This class represents the current state of the UI.
 *
 * @author Niall Scott
 */
sealed interface UiState {

    /**
     * The progress layout should be shown.
     */
    data object InProgress : UiState

    /**
     * This is the base class for a favourite stop mode.
     */
    sealed interface Mode : UiState {

        /**
         * The stop identifier.
         */
        val stopIdentifier: StopIdentifier
        /**
         * The stop name properties.
         */
        val stopName: StopName?

        /**
         * The 'Add' layout should be shown.
         *
         * @property stopIdentifier The stop identifier.
         * @property stopName The name of the stop.
         */
        data class Add(
            override val stopIdentifier: StopIdentifier,
            override val stopName: StopName?
        ) : Mode

        /**
         * The 'Edit' layout should be shown.
         *
         * @property stopName The stop identifier.
         * @property stopName The name of the stop.
         * @property favouriteStop The currently saved [FavouriteStop].
         */
        data class Edit(
            override val stopIdentifier: StopIdentifier,
            override val stopName: StopName?,
            val favouriteStop: FavouriteStop
        ) : Mode
    }
}
