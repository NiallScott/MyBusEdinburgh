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
import uk.org.rivernile.android.bustracker.core.busstops.StopOrientation
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.text.UiStopName
import uk.org.rivernile.android.bustracker.ui.text.UiServiceName

/**
 * A nearest stop which is displayed on the UI.
 *
 * @property stopIdentifier The stop identifier of the nearest stop.
 * @property stopName The stop name details.
 * @property services An immutable list of services which serve this stop.
 * @property orientation The orientation of the stop.
 * @property distanceMeters The distance between this device and the stop in meters.
 * @property dropdownMenu The dropdown menu for this nearest stop.
 * @author Niall Scott
 */
internal data class UiNearestStop(
    val stopIdentifier: StopIdentifier,
    val stopName: UiStopName?,
    val services: ImmutableList<UiServiceName>?,
    val orientation: StopOrientation,
    val distanceMeters: Int,
    val dropdownMenu: UiNearestStopDropdownMenu
)
