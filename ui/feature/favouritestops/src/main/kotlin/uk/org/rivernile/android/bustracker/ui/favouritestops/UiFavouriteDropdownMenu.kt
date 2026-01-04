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

/**
 * A dropdown menu for a favourite stop item.
 *
 * @property isShown Is the menu being shown?
 * @property arrivalAlertDropdownItem Properties related to the arrival alert dropdown item. `null`
 * denotes that the item should not be shown.
 * @property proximityAlertDropdownItem Properties related to the proximity alert dropdown item.
 * `null` denotes that the item should not be shown.
 * @property isStopMapItemShown Is the stop map available?
 * @author Niall Scott
 */
internal data class UiFavouriteDropdownMenu(
    val isShown: Boolean = false,
    val arrivalAlertDropdownItem: UiArrivalAlertDropdownItem? = null,
    val proximityAlertDropdownItem: UiProximityAlertDropdownItem? = null,
    val isStopMapItemShown: Boolean = false
)

/**
 * Attributed data for the arrival alert dropdown item.
 *
 * @property hasArrivalAlert Is an arrival alert set for the stop this item represents?
 * @author Niall Scott
 */
internal data class UiArrivalAlertDropdownItem(
    val hasArrivalAlert: Boolean = false
)

/**
 * Attributed data for the proximity alert dropdown item.
 *
 * @property hasProximityAlert Is a proximity alert set for the stop this item represents?
 * @author Niall Scott
 */
internal data class UiProximityAlertDropdownItem(
    val hasProximityAlert: Boolean = false
)
