/*
 * Copyright (C) 2023 Niall 'Rivernile' Scott
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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * This is the legacy implementation of the [ConnectivityChecker]. It provides the way to detect the
 * presence of connectivity up until API level 24, where a new API became available.
 *
 * @param context The application [Context].
 * @param connectivityManager The Android [ConnectivityManager].
 * @author Niall Scott
 */
internal class LegacyConnectivityChecker @Inject constructor(
        private val context: Context,
        private val connectivityManager: ConnectivityManager) : ConnectivityChecker {

    @Suppress("DEPRECATION")
    override val hasInternetConnectivity get() =
        connectivityManager.activeNetworkInfo?.isConnected ?: false

    @Suppress("DEPRECATION")
    override val hasInternetConnectivityFlow get() = callbackFlow {
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
                    val pendingResult = goAsync()

                    launch {
                        try {
                            getAndSendHasInternetConnectivity()
                        } finally {
                            pendingResult.finish()
                        }
                    }
                }
            }
        }

        context.registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
        getAndSendHasInternetConnectivity()

        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    /**
     * Get the current connectivity status and send it to the [ProducerScope].
     */
    private suspend fun ProducerScope<Boolean>.getAndSendHasInternetConnectivity() {
        send(hasInternetConnectivity)
    }
}