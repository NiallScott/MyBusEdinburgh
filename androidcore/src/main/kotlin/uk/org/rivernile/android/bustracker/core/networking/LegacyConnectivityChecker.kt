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

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.ConnectivityManager

/**
 * This is the legacy implementation of the [ConnectivityChecker]. It provides the way to detect the
 * presence of connectivity up until API level 29, where a new API became available.
 *
 * @param context The application [Context].
 * @param connectivityManager The Android [ConnectivityManager].
 * @author Niall Scott
 */
internal class LegacyConnectivityChecker(
        private val context: Context,
        private val connectivityManager: ConnectivityManager) : ConnectivityChecker {

    private val listeners = mutableListOf<ConnectivityChecker.OnConnectivityChangedListener>()

    @Suppress("DEPRECATION")
    override fun addOnConnectivityChangedListener(
            listener: ConnectivityChecker.OnConnectivityChangedListener) {
        synchronized(listeners) {
            listeners += listener

            if (listeners.size == 1) {
                context.registerReceiver(connectivityReceiver,
                        IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
            }
        }
    }

    override fun removeOnConnectivityChangedListener(
            listener: ConnectivityChecker.OnConnectivityChangedListener) {
        synchronized(listeners) {
            listeners -= listener

            if (listeners.isEmpty()) {
                context.unregisterReceiver(connectivityReceiver)
            }
        }
    }

    @Suppress("DEPRECATION")
    override fun hasInternetConnectivity() =
            connectivityManager.activeNetworkInfo?.isConnected ?: false

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

    private val connectivityReceiver = object : BroadcastReceiver() {
        @Suppress("DEPRECATION")
        override fun onReceive(context: Context, intent: Intent) {
            if (ConnectivityManager.CONNECTIVITY_ACTION == intent.action) {
                dispatchConnectivityChangedEvent()
            }
        }
    }
}