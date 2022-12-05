/*
 * Copyright (C) 2021 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites

import uk.org.rivernile.android.bustracker.core.database.settings.entities.FavouriteStop

/**
 * This represents a single favourite stop shown on the UI.
 *
 * @property favouriteStop The saved favourite data for this stop.
 * @property services The service listing for this stop code.
 * @property isSelected Is this stop currently selected on the UI?
 * @author Niall Scott
 */
data class UiFavouriteStop(
        val favouriteStop: FavouriteStop,
        val services: List<String>?,
        val isSelected: Boolean)