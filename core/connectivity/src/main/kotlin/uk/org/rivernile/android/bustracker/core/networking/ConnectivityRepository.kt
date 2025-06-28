/*
 * Copyright (C) 2020 - 2025 Niall 'Rivernile' Scott
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

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.shareIn
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForApplicationCoroutineScope
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This repository is used to access device connectivity information.
 *
 * @param connectivityChecker Used to check device connectivity, and to register for events to
 * listen for connectivity changes.
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @author Niall Scott
 */
@Singleton
class ConnectivityRepository @Inject internal constructor(
    private val connectivityChecker: ConnectivityChecker,
    @param:ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope
) {

    /**
     * Is there internet connectivity available?
     */
    val hasInternetConnectivity: Boolean get() = connectivityChecker.hasInternetConnectivity

    /**
     * A [Flow] which emits the device internet connectivity status.
     */
    val hasInternetConnectivityFlow: Flow<Boolean> by lazy {
        connectivityChecker.hasInternetConnectivityFlow
            .distinctUntilChanged()
            .shareIn(
                scope = applicationCoroutineScope,
                started = SharingStarted.WhileSubscribed(replayExpirationMillis = 0L),
                replay = 1
            )
    }
}