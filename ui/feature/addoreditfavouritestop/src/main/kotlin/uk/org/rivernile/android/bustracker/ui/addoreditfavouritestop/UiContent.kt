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

package uk.org.rivernile.android.bustracker.ui.addoreditfavouritestop

import androidx.compose.runtime.Immutable
import uk.org.rivernile.android.bustracker.core.text.UiStopName

/**
 * This represents the current content state of the add or edit favourite bus stop UI.
 *
 * @author Niall Scott
 */
@Immutable
internal sealed interface UiContent {

    /**
     * Is the positive button on the dialog enabled?
     */
    val isPositiveButtonEnabled: Boolean

    /**
     * The data is currently loading.
     */
    data object InProgress : UiContent {

        override val isPositiveButtonEnabled get() = false
    }

    /**
     * This represents a mode for the UI - whether to add a new favourite bus stop or to edit an
     * existing one.
     */
    sealed interface Mode : UiContent {

        /**
         * The code of the stop we're concerned with.
         */
        val stopCode: String

        /**
         * The name data of the stop we're concerned with.
         */
        val stopName: UiStopName?

        /**
         * The content is in 'add' mode.
         *
         * @property stopCode See [Mode.stopCode].
         * @property stopName See [Mode.stopName].
         * @property isPositiveButtonEnabled Is the positive button on the dialog enabled?
         */
        data class Add(
            override val stopCode: String,
            override val stopName: UiStopName?,
            override val isPositiveButtonEnabled: Boolean
        ) : Mode

        /**
         * The content is in 'edit' mode.
         *
         * @property stopCode See [Mode.stopCode].
         * @property stopName See [Mode.stopName].
         * @property isPositiveButtonEnabled Is the positive button on the dialog enabled?
         * @property savedName The name the stop is currently saved as.
         */
        data class Edit(
            override val stopCode: String,
            override val stopName: UiStopName?,
            override val isPositiveButtonEnabled: Boolean,
            val savedName: String,
        ) : Mode
    }
}
