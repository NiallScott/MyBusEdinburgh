/*
 * Copyright (C) 2023 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import android.os.Parcelable
import androidx.annotation.StringRes
import kotlinx.parcelize.Parcelize

/**
 * This sealed interface defines the parameter types for [ServicesChooserDialogFragment].
 *
 * @author Niall Scott
 */
sealed interface ServicesChooserParams : Parcelable {

    /**
     * The string resource ID to use as the title.
     */
    @get:StringRes
    val titleResId: Int

    /**
     * The existing selected services.
     */
    val selectedServices: List<String>?

    /**
     * All services should be shown for selection.
     *
     * @property titleResId The resource ID to use for the title.
     * @property selectedServices The existing selected services.
     */
    @Parcelize
    data class AllServices(
        @StringRes override val titleResId: Int,
        override val selectedServices: List<String>?
    ) : ServicesChooserParams

    /**
     * Only services for the given [stopCode] should be shown for selection.
     *
     * @property titleResId The resource ID to use for the title.
     * @property selectedServices The existing selected services.
     * @property stopCode The stop code to show services for.
     */
    @Parcelize
    data class Stop(
        @StringRes override val titleResId: Int,
        override val selectedServices: List<String>?,
        val stopCode: String
    ) : ServicesChooserParams
}