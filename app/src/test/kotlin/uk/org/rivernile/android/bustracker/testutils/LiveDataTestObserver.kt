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

package uk.org.rivernile.android.bustracker.testutils

import androidx.lifecycle.LiveData
import androidx.lifecycle.Observer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

/**
 * Obtain a [LiveDataTestObserver] for a [LiveData] instance.
 *
 * @param T The type of data emitted by the [LiveData] instance.
 * @return A [LiveDataTestObserver] for this [LiveData] instance.
 */
fun <T> LiveData<T>.test() = LiveDataTestObserver<T>().also(this::observeForever)

/**
 * This class is used to collect the emissions of a [LiveData] object and store them for later
 * value assertion during unit testing.
 *
 * @param T The type of data the [Observer] will receive.
 * @author Niall Scott
 */
class LiveDataTestObserver<T> : Observer<T> {

    /**
     * This is the [List] of observed values.
     */
    val observedValues: List<T> get() = values

    private val values = mutableListOf<T>()

    override fun onChanged(t: T) {
        values.add(t)
    }

    /**
     * Assert that a [LiveData] has emitted these values.
     *
     * @param values The values to compare with the collected values. This must match the collected
     * values exactly, otherwise the assertion will fail.
     */
    fun assertValues(vararg values: T) {
        assertEquals(values.toList(), this.values)
    }

    /**
     * Assert this [Observer] has not collected any values.
     */
    fun assertEmpty() {
        assertTrue("values = $values", values.isEmpty())
    }

    /**
     * Assert this [Observer] has collected [expectedSize] number of items.
     *
     * @param expectedSize The number of items to assert the collection size against.
     */
    fun assertSize(expectedSize: Int) {
        assertEquals(expectedSize, values.size)
    }
}