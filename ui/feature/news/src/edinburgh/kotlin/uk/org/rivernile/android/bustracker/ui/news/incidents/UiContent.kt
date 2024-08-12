/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.news.incidents

/**
 * This sealed interface represents the different possible types of content which can be displayed.
 *
 * @author Niall Scott
 */
internal sealed interface UiContent {

    /**
     * Show the 'in progress' layout.
     */
    data object InProgress : UiContent

    /**
     * Show a happy-path content layout, which is a [List] of [UiIncident] items.
     *
     * @property items The [List] of [UiIncident] items to show.
     */
    data class Success(
        val items: List<UiIncident>
    ) : UiContent

    /**
     * This sealed interface represents the different possible error types.
     */
    sealed interface Error : UiContent {

        /**
         * The error has been caused due to lack of internet connectivity.
         */
        data object NoConnectivity : Error

        /**
         * The error has been caused due to the endpoint returning empty data.
         */
        data object Empty : Error

        /**
         * The error has been caused by an IO error while trying to load the data.
         */
        data object Io : Error

        /**
         * The error has been caused by an error on the server-side while trying to load the data.
         */
        data object Server : Error
    }
}