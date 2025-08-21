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

package uk.org.rivernile.android.bustracker.core.networking

import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import kotlinx.coroutines.channels.ProducerScope
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * This is the Android implementation of [ConnectivityChecker].
 *
 * @param connectivityManager The Android [ConnectivityManager].
 * @author Niall Scott
 */
internal class AndroidConnectivityChecker @Inject constructor(
    private val connectivityManager: ConnectivityManager
) : ConnectivityChecker {

    override val hasInternetConnectivity get() =
        isNetworkInternetCapable(connectivityManager.activeNetwork)

    override val hasInternetConnectivityFlow get() = callbackFlow {
        val networkCallback = object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                launch {
                    handleNetworkEventForConnectivity(network)
                }
            }

            override fun onLost(network: Network) {
                launch {
                    send(false)
                }
            }
        }

        connectivityManager.registerDefaultNetworkCallback(networkCallback)
        handleNetworkEventForConnectivity(connectivityManager.activeNetwork)

        awaitClose {
            connectivityManager.unregisterNetworkCallback(networkCallback)
        }
    }

    /**
     * Get the internet capability status of the supplied [Network] and send it to the
     * [ProducerScope]. If the [network] is `null`, then `false` will be sent.
     *
     * @param network The [Network] to test for internet connectivity.
     */
    private suspend fun ProducerScope<Boolean>.handleNetworkEventForConnectivity(
        network: Network?
    ) {
        send(isNetworkInternetCapable(network))
    }

    private fun isNetworkInternetCapable(network: Network?) = network
        ?.let(connectivityManager::getNetworkCapabilities)
        ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        ?: false
}
