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

import kotlinx.collections.immutable.ImmutableList

/**
 * This describes the possible content layouts of the favourite stops screen.
 *
 * @author Niall Scott
 */
internal sealed interface UiContent {

    /**
     * Show the 'In progress' layout.
     */
    data object InProgress : UiContent

    /**
     * Show the content layout. This is only shown when the favourite stops are loaded and are not
     * empty.
     *
     * @property favouriteStops The [ImmutableList] of [UiFavouriteStop]s to be shown.
     */
    data class Content(
        val favouriteStops: ImmutableList<UiFavouriteStop>
    ) : UiContent

    /**
     * Show the empty layout. This is shown when there are not favourite stops.
     */
    data object Empty : UiContent
}
