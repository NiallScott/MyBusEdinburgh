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

import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.channels.SendChannel
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import javax.inject.Inject
import javax.inject.Singleton

/**
 * This repository is used to access device connectivity information.
 *
 * @param connectivityChecker Used to check device connectivity, and to register for events to
 * listen for connectivity changes.
 * @param defaultDispatcher A [CoroutineDispatcher] for processing in the background.
 * @author Niall Scott
 */
@Singleton
class ConnectivityRepository @Inject internal constructor(
        private val connectivityChecker: ConnectivityChecker,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher) {

    /**
     * Get a [Flow] which returns the device connectivity status. Any updates to the status will
     * be emitted from the returned [Flow] until cancelled.
     *
     * @return The [Flow] which emits device connectivity status.
     */
    fun hasInternetConnectivityFlow(): Flow<Boolean> = callbackFlow {
        val listener = object : ConnectivityChecker.OnConnectivityChangedListener {
            override fun onConnectivityChanged() {
                launch {
                    getAndSendHasInternetConnectivity(channel)
                }
            }
        }

        connectivityChecker.addOnConnectivityChangedListener(listener)
        getAndSendHasInternetConnectivity(channel)

        awaitClose {
            connectivityChecker.removeOnConnectivityChangedListener(listener)
        }
    }

    /**
     * A suspend function which obtains the device connectivity status and then sends it to the
     * given [channel].
     *
     * @param channel The [SendChannel] that emissions should be sent to.
     */
    private suspend fun getAndSendHasInternetConnectivity(
            channel: SendChannel<Boolean>) = withContext(defaultDispatcher) {
        channel.send(connectivityChecker.hasInternetConnectivity())
    }
}