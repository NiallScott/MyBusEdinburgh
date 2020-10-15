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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.never
import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.testutils.LiveDataTestObserver

/**
 * Tests for [RefreshLiveData].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class RefreshLiveDataTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var refreshController: RefreshController

    private val observer = LiveDataTestObserver<Nothing>()

    private lateinit var liveData: RefreshLiveData

    @Before
    fun setUp() {
        liveData = RefreshLiveData(refreshController)
    }

    @Test
    fun observingLiveDataCausesRefreshControllerToHaveActiveStatusSet() {
        liveData.observeForever(observer)

        verify(refreshController)
                .setActiveState(true)
        verify(refreshController, never())
                .setActiveState(false)
    }

    @Test
    fun removingObserverFromLiveDataCausesRefreshControllerToHaveActiveStatusSetToInactive() {
        liveData.observeForever(observer)
        liveData.removeObserver(observer)

        verify(refreshController)
                .setActiveState(true)
        verify(refreshController)
                .setActiveState(false)
    }
}