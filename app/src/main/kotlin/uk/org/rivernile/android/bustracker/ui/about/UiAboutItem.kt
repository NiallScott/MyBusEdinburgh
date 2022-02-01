/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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
 */

package uk.org.rivernile.android.bustracker.ui.about

import java.util.*

/**
 * This sealed interface encapsulates the possible 'about' items shown.
 *
 * @author Niall Scott
 */
sealed interface UiAboutItem {

    companion object {

        private const val ITEM_ID_APP_VERSION = 1
        private const val ITEM_ID_AUTHOR = 2
        private const val ITEM_ID_WEBSITE = 3
        private const val ITEM_ID_TWITTER = 4
        private const val ITEM_ID_DATABASE_VERSION = 5
        private const val ITEM_ID_TOPOLOGY_VERSION = 6
        private const val ITEM_ID_CREDITS = 7
        private const val ITEM_ID_OPEN_SOURCE_LICENCES = 8
    }

    /**
     * The ID of the item.
     */
    val id :Int

    /**
     * Is the item clickable?
     */
    val isClickable: Boolean

    /**
     * This is a [UiAboutItem] which is only displayed on a single line.
     */
    sealed interface OneLineItem : UiAboutItem {

        /**
         * Credits item.
         */
        object Credits : OneLineItem {

            override val id get() = ITEM_ID_CREDITS

            override val isClickable get() = true
        }

        /**
         * Open source licences item.
         */
        object OpenSourceLicences : OneLineItem {

            override val id get() = ITEM_ID_OPEN_SOURCE_LICENCES

            override val isClickable get() = true
        }
    }

    /**
     * This is a [UiAboutItem] which is displayed over two lines.
     */
    sealed interface TwoLinesItem : UiAboutItem {

        /**
         * App version item.
         *
         * @property versionName The version name.
         * @property versionCode The version code.
         */
        data class AppVersion(
                val versionName: String,
                val versionCode: Long) : TwoLinesItem {

            override val id get() = ITEM_ID_APP_VERSION

            override val isClickable get() = true
        }

        /**
         * App author item.
         */
        object Author : TwoLinesItem {

            override val id get() = ITEM_ID_AUTHOR

            override val isClickable get() = true
        }

        /**
         * App website item.
         */
        object Website : TwoLinesItem {

            override val id get() = ITEM_ID_WEBSITE

            override val isClickable get() = true
        }

        /**
         * App Twitter link item.
         */
        object Twitter : TwoLinesItem {

            override val id get() = ITEM_ID_TWITTER

            override val isClickable get() = true
        }

        /**
         * Database version item.
         *
         * @property date The database version (the version is the date timestamp it was created).
         */
        data class DatabaseVersion(
                val date: Date?) : TwoLinesItem {

            override val id get() = ITEM_ID_DATABASE_VERSION

            override val isClickable get() = false
        }

        /**
         * Database topology version item.
         *
         * @property topologyId The topology ID string.
         */
        data class TopologyVersion(
                val topologyId: String?) : TwoLinesItem {

            override val id get() = ITEM_ID_TOPOLOGY_VERSION

            override val isClickable get() = false
        }
    }
}