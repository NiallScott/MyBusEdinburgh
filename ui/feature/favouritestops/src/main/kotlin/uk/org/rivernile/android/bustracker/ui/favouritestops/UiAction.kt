/*
 * Copyright (C) 2025 - 2026 Niall 'Rivernile' Scott
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

import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier

/**
 * This describes the possible actions that can be taken within the favourites screen.
 *
 * @author Niall Scott
 */
internal sealed interface UiAction {

    /**
     * Show stop data.
     *
     * @property stopIdentifier The stop identifier that data should be shown for.
     */
    data class ShowStopData(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Show UI which allows the user to edit a favourite stop.
     *
     * @property stopIdentifier The stop identifier to be edited.
     */
    data class ShowEditFavouriteStop(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Show UI where the user can confirm they wish to delete a favourite stop.
     *
     * @property stopIdentifier The stop identifier to be confirmed for deletion.
     */
    data class ShowConfirmRemoveFavourite(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Show the favourite stop on a map.
     *
     * @property stopIdentifier The code of the stop to show on a map.
     */
    data class ShowOnMap(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Show UI to allow the user to add an arrival alert for a favourite stop.
     *
     * @property stopIdentifier The code of the stop to add an arrival alert for.
     */
    data class ShowAddArrivalAlert(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Show UI to confirm with the user if they wish to remove an arrival alert for a favourite
     * stop.
     *
     * @property stopIdentifier The code of the stop to confirm removal of the arrival alert.
     */
    data class ShowConfirmRemoveArrivalAlert(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Show UI to allow the user to add a proximity alert for a favourite stop.
     *
     * @property stopIdentifier The code of the stop to add a proximity alert for.
     */
    data class ShowAddProximityAlert(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Show UI to confirm with the user if they wish to remove a proximity alert for a favourite
     * stop.
     *
     * @property stopIdentifier The code of the stop to confirm removal of a proximity alert.
     */
    data class ShowConfirmRemoveProximityAlert(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * A shortcut should be added for a favourite stop.
     *
     * @property stopIdentifier The code of the stop to add a shortcut for.
     * @property savedName The name of the stop which will have a shortcut created for.
     */
    data class AddShortcut(
        val stopIdentifier: StopIdentifier,
        val savedName: String
    ) : UiAction
}
