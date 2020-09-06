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

package uk.org.rivernile.android.bustracker.livedata

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import uk.org.rivernile.android.bustracker.testutils.LiveDataTestObserver

/**
 * Tests for [DistinctLiveData].
 *
 * @author Niall Scott
 */
class DistinctLiveDataTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val observer = LiveDataTestObserver<String>()

    private lateinit var liveData: DistinctLiveData<String>

    @Before
    fun setUp() {
        liveData = DistinctLiveData()
    }

    @Test
    fun distinctLiveDataOnlyEmitsChangedItems() {
        liveData.observeForever(observer)
        liveData.setValue("one")
        liveData.setValue("two")
        liveData.setValue("two")
        liveData.setValue("three")
        liveData.setValue("four")
        liveData.setValue("four")
        liveData.setValue("four")
        liveData.setValue("five")

        observer.assertValues(
                "one",
                "two",
                "three",
                "four",
                "five")
    }
}