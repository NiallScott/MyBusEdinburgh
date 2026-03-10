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

package uk.org.rivernile.android.bustracker.ui.neareststops

import kotlinx.collections.immutable.ImmutableList
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier

/**
 * This describes the possible actions that can be taken with the nearest stops screen.
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
     * Show UI which allows the user to add a favourite stop.
     *
     * @property stopIdentifier The stop identifier to be added as a favourite.
     */
    data class ShowAddFavouriteStop(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Show UI which allows the user to remove a favourite stop.
     *
     * @property stopIdentifier The stop identifier to be removed as a favourite.
     */
    data class ShowRemoveFavouriteStop(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Show UI which allows the user to add an arrival alert.
     *
     * @property stopIdentifier The stop identifier to add an arrival alert for.
     */
    data class ShowAddArrivalAlert(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Show UI which allows the user to remove an arrival alert.
     *
     * @property stopIdentifier The stop identifier to remove an arrival alert for.
     */
    data class ShowRemoveArrivalAlert(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Show UI which allows the user to add a proximity alert.
     *
     * @property stopIdentifier The stop identifier to add a proximity alert for.
     */
    data class ShowAddProximityAlert(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Show UI which allows the user to remove a proximity alert.
     *
     * @property stopIdentifier The stop identifier to remove a proximity alert for.
     */
    data class ShowRemoveProximityAlert(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Show the nearest stop on a map.
     *
     * @property stopIdentifier The stop identifier to show on a map.
     */
    data class ShowOnMap(
        val stopIdentifier: StopIdentifier
    ) : UiAction

    /**
     * Request location permissions.
     */
    data object RequestLocationPermissions : UiAction

    /**
     * Show the services chooser.
     *
     * @property selectedServices The currently selected services, if any.
     */
    data class ShowServicesChooser(
        val selectedServices: ImmutableList<ServiceDescriptor>?
    ) : UiAction

    /**
     * Show the system location settings.
     */
    data object ShowLocationSettings : UiAction

    /**
     * Show UI which allows the user to turn on GPS.
     */
    data object ShowTurnOnGps : UiAction
}
