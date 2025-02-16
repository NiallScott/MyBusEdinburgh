/*
 * Copyright (C) 2024 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import uk.org.rivernile.android.bustracker.ui.news.serviceupdates.incidents.UiIncident

/**
 * This sealed interface represents the different presentation states for service updates.
 *
 * @param T The type of [UiServiceUpdate] to be displayed.
 * @author Niall Scott
 */
internal sealed interface ServiceUpdatesDisplay<out T : UiServiceUpdate> {

    /**
     * Show the 'in progress' layout.
     */
    data object InProgress : ServiceUpdatesDisplay<Nothing>

    /**
     * Show a happy-path content layout, which is a [List] of [UiIncident] items.
     *
     * @param T The type of [UiServiceUpdate] to be displayed.
     * @property isRefreshing Is the content currently refreshing?
     * @property items The [List] of [UiIncident] items to show.
     * @property error An optional error to show if there's old content to show but an error
     * occurred on the latest load.
     * @property loadTimeMillis The time the populated data was loaded at.
     * occurred on this reload attempt.
     */
    data class Populated<out T : UiServiceUpdate>(
        val isRefreshing: Boolean,
        val items: List<T>,
        val error: UiError?,
        val loadTimeMillis: Long
    ) : ServiceUpdatesDisplay<T>

    /**
     * Show an error layout, based on the given [error].
     *
     * @property error The type of error which occurred.
     */
    data class Error(
        val error: UiError
    ) : ServiceUpdatesDisplay<Nothing>
}