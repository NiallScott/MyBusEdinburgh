/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.networking

import kotlinx.coroutines.flow.Flow

/**
 * This interface defines methods used to check internet connectivity.
 *
 * @author Niall Scott
 */
interface ConnectivityChecker {

    /**
     * Is there a route out to the internet that the app can use to make network calls?
     */
    val hasInternetConnectivity: Boolean

    /**
     * A [Flow] which emits whether the device has internet connectivity or not.
     */
    val hasInternetConnectivityFlow: Flow<Boolean>
}