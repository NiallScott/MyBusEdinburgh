/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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
import android.os.Build
import androidx.annotation.RequiresApi

/**
 * This is the implementation of [ConnectivityChecker] available from API level 29 onwards.
 *
 * @param connectivityManager The Android [ConnectivityManager].
 * @author Niall Scott
 */
@RequiresApi(Build.VERSION_CODES.Q)
internal class V29ConnectivityChecker(
        private val connectivityManager: ConnectivityManager) : ConnectivityChecker {

    private val listeners = mutableListOf<ConnectivityChecker.OnConnectivityChangedListener>()

    override fun addOnConnectivityChangedListener(
            listener: ConnectivityChecker.OnConnectivityChangedListener) {
        synchronized(listeners) {
            listeners += listener

            if (listeners.size == 1) {
                connectivityManager.registerDefaultNetworkCallback(networkCallback)
            }
        }
    }

    override fun removeOnConnectivityChangedListener(
            listener: ConnectivityChecker.OnConnectivityChangedListener) {
        synchronized(listeners) {
            listeners -= listener

            if (listeners.isEmpty()) {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            }
        }
    }

    override fun hasInternetConnectivity(): Boolean {
        return connectivityManager.activeNetwork?.let { network ->
            connectivityManager.getNetworkCapabilities(network)
                    ?.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
        } ?: false
    }

    /**
     * This is called to dispatch a connectivity changed event to the listeners.
     */
    private fun dispatchConnectivityChangedEvent() {
        synchronized(listeners) {
            listeners.forEach {
                it.onConnectivityChanged()
            }
        }
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            dispatchConnectivityChangedEvent()
        }

        override fun onBlockedStatusChanged(network: Network, blocked: Boolean) {
            dispatchConnectivityChangedEvent()
        }

        override fun onCapabilitiesChanged(network: Network,
                networkCapabilities: NetworkCapabilities) {
            dispatchConnectivityChangedEvent()
        }

        override fun onLost(network: Network) {
            dispatchConnectivityChangedEvent()
        }

        override fun onUnavailable() {
            dispatchConnectivityChangedEvent()
        }
    }
}