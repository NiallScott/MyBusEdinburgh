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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Enable testing on a [Flow] object.
 *
 * @param T The type of data the [Flow] emits.
 * @param scope The [CoroutineScope] to execute the [Flow] under.
 * @return A [FlowTestObserver] for the given [Flow].
 */
fun <T> Flow<T>.test(scope: CoroutineScope): FlowTestObserver<T> =
        FlowTestObserver(scope, this)

/**
 * This is used to aid in the testing of [Flow] objects.
 *
 * Obtained from https://proandroiddev.com/from-rxjava-to-kotlin-flow-testing-42f1641d8433
 *
 * @param T The type of data the [Flow] emits.
 * @param scope The [CoroutineScope] to execute the [Flow] under.
 * @param flow The [Flow] that is being tested.
 */
class FlowTestObserver<T>(
        scope: CoroutineScope,
        flow: Flow<T>) {

    private val values = mutableListOf<T>()

    private val job = scope.launch {
        flow.collect {
            values.add(it)
        }
    }

    /**
     * Assert that a [Flow] has never emitted any values.
     */
    fun assertNoValues(): FlowTestObserver<T> {
        assertTrue(values.isEmpty())

        return this
    }

    /**
     * Assert that a [Flow] has emitted these values.
     */
    fun assertValues(vararg values: T): FlowTestObserver<T> {
        assertEquals(values.toList(), this.values)

        return this
    }

    /**
     * Finish the flow.
     */
    fun finish() {
        job.cancel()
    }
}