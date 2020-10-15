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

package uk.org.rivernile.android.bustracker.coroutines

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.ReceiveChannel
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Enable testing on a [ReceiveChannel] object.
 *
 * @param T The type of data the [ReceiveChannel] emits.
 * @param scope The [CoroutineScope] to execute the [ReceiveChannel] under.
 * @return A [ChannelTestObserver] for the given [ReceiveChannel].
 */
fun <T> ReceiveChannel<T>.test(scope: CoroutineScope): ChannelTestObserver<T> =
        ChannelTestObserver(scope, this)

/**
 * Enable testing on a [ReceiveChannel] object.
 *
 * @param T The type of data that the [ReceiveChannel] emits.
 * @param scope The [CoroutineScope] to execute the [ReceiveChannel] under.
 * @param channel The [ReceiveChannel] that is being tested.
 * @author Niall Scott
 */
class ChannelTestObserver<T>(
        private val scope: CoroutineScope,
        channel: ReceiveChannel<T>? = null) {

    private val values = mutableListOf<T>()

    private var job: Job? = null

    init {
        channel?.let {
            observeChannel(it)
        }
    }

    /**
     * Allows for the late observation of a [ReceiveChannel] when the [ReceiveChannel] instance was
     * not available at the time of instantiating this class.
     *
     * If a [ReceiveChannel] is already being observed when this method is called, an
     * [IllegalStateException] will be thrown.
     *
     * @param channel The [ReceiveChannel] to observe.
     */
    fun observe(channel: ReceiveChannel<T>) {
        if (job == null) {
            observeChannel(channel)
        } else {
            throw IllegalStateException("Already observing a ReceiveChannel.")
        }
    }

    /**
     * Assert that a [ReceiveChannel] has emitted these values.
     *
     * @param values The values to assert that the [ReceiveChannel] emitted.
     */
    fun assertValues(vararg values: T): ChannelTestObserver<T> {
        assertEquals(values.toList(), this.values)

        return this
    }

    /**
     * Assert that no values have been emitted by the [ReceiveChannel].
     */
    fun assertEmpty() {
        assertTrue(values.isEmpty())
    }

    /**
     * Finish observing the [ReceiveChannel].
     */
    fun finish() {
        job?.cancel()
    }

    /**
     * Observe a given [ReceiveChannel].
     *
     * @param channel The [ReceiveChannel] to observe.
     */
    private fun observeChannel(channel: ReceiveChannel<T>) {
        job = scope.launch {
            for (item in channel) {
                values.add(item)
            }
        }
    }
}