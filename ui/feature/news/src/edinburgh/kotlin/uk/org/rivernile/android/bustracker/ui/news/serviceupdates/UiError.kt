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

package uk.org.rivernile.android.bustracker.ui.news.serviceupdates

import uk.org.rivernile.android.bustracker.core.updates.ServiceUpdatesResult

/**
 * This enum defines types of errors to be displayed to the user which occur while trying to load
 * the content.
 *
 * @author Niall Scott
 */
internal enum class UiError {

    /** The content could not be loaded due to lack of connectivity. */
    NO_CONNECTIVITY,
    /** The content was loaded but it was empty. */
    EMPTY,
    /** The content could not be loaded due to an IO error. */
    IO,
    /** The content could not be loaded due to a server error. */
    SERVER
}

/**
 * Map a [ServiceUpdatesResult.Error] to an [UiError].
 *
 * @return This [ServiceUpdatesResult.Error] mapped to an [UiError].
 */
internal fun ServiceUpdatesResult.Error.toUiError(): UiError {
    return when (this) {
        is ServiceUpdatesResult.Error.NoConnectivity -> UiError.NO_CONNECTIVITY
        is ServiceUpdatesResult.Error.Io -> UiError.IO
        is ServiceUpdatesResult.Error.Server -> UiError.SERVER
    }
}