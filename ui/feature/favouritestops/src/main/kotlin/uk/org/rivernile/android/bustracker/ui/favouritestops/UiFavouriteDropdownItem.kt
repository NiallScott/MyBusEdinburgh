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

/**
 * This represents an item which can appear in the dropdown context menu for a favourite stop.
 *
 * @author Niall Scott
 */
internal sealed interface UiFavouriteDropdownItem {

    /**
     * A dropdown item which takes the user to editing a favourite stop name.
     */
    data object EditFavouriteName : UiFavouriteDropdownItem

    /**
     * A dropdown item which takes the user to removing a favourite stop.
     */
    data object RemoveFavourite : UiFavouriteDropdownItem

    /**
     * A dropdown item which takes the user to adding a new arrival alert.
     *
     * @property isEnabled Should the dropdown item be enabled?
     */
    data class AddArrivalAlert(
        val isEnabled: Boolean
    ) : UiFavouriteDropdownItem

    /**
     * A dropdown item which takes the user to removing a previously set arrival alert.
     */
    data object RemoveArrivalAlert : UiFavouriteDropdownItem

    /**
     * A dropdown item which takes the user to adding a new proximity alert.
     *
     * @property isEnabled Should the dropdown item be enabled?
     */
    data class AddProximityAlert(
        val isEnabled: Boolean
    ) : UiFavouriteDropdownItem

    /**
     * A dropdown item which takes the user to removing a previously set proximity alert.
     */
    data object RemoveProximityAlert : UiFavouriteDropdownItem

    /**
     * A dropdown item which takes the user to the map, highlighting the favourite stop.
     */
    data object ShowOnMap : UiFavouriteDropdownItem
}
