/*
 * Copyright (C) 2022 - 2024 Niall 'Rivernile' Scott
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

import androidx.compose.runtime.Immutable
import java.util.Date

/**
 * This sealed interface encapsulates the possible 'about' items shown.
 *
 * @author Niall Scott
 */
internal sealed interface UiAboutItem {

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
        @Immutable
        data object Credits : OneLineItem {

            override val isClickable get() = true
        }

        /**
         * Privacy policy item.
         */
        @Immutable
        data object PrivacyPolicy : OneLineItem {

            override val isClickable get() = true
        }

        /**
         * Open source licences item.
         */
        @Immutable
        data object OpenSourceLicences : OneLineItem {

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
        @Immutable
        data class AppVersion(
            val versionName: String,
            val versionCode: Long
        ) : TwoLinesItem {

            override val isClickable get() = true
        }

        /**
         * App author item.
         */
        @Immutable
        data object Author : TwoLinesItem {

            override val isClickable get() = true
        }

        /**
         * App website item.
         */
        @Immutable
        data object Website : TwoLinesItem {

            override val isClickable get() = true
        }

        /**
         * App Twitter link item.
         */
        @Immutable
        data object Twitter : TwoLinesItem {

            override val isClickable get() = true
        }

        /**
         * Database version item.
         *
         * @property date The database version (the version is the date timestamp it was created).
         */
        @Immutable
        data class DatabaseVersion(
            val date: Date?
        ) : TwoLinesItem {

            override val isClickable get() = false
        }

        /**
         * Database topology version item.
         *
         * @property topologyId The topology ID string.
         */
        @Immutable
        data class TopologyVersion(
            val topologyId: String?
        ) : TwoLinesItem {

            override val isClickable get() = false
        }
    }
}