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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertSame
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Tests for [LiveTimesFlowFactory].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class LiveTimesFlowFactoryTest {

    @Mock
    private lateinit var liveTimesLoader: LiveTimesLoader
    @Mock
    private lateinit var liveTimesTransform: LiveTimesTransform

    private lateinit var factory: LiveTimesFlowFactory

    @Before
    fun setUp() {
        factory = LiveTimesFlowFactory(liveTimesLoader, liveTimesTransform)
    }

    @Test
    fun createLiveTimesFlowPassesLiveTimesLoaderToLiveTimesTransform() {
        val stopCodeFlow = emptyFlow<String?>()
        val expandedServicesFlow = emptyFlow<Set<String>>()
        val refreshTriggerFlow = emptyFlow<Unit>()
        val liveTimesFlow = emptyFlow<UiResult>()
        val expectedFlow = emptyFlow<UiTransformedResult>()
        whenever(liveTimesLoader.loadLiveTimesFlow(stopCodeFlow, refreshTriggerFlow))
                .thenReturn(liveTimesFlow)
        whenever(liveTimesTransform.getLiveTimesTransformFlow(liveTimesFlow, expandedServicesFlow))
                .thenReturn(expectedFlow)

        val result = factory.createLiveTimesFlow(stopCodeFlow, expandedServicesFlow,
                refreshTriggerFlow)

        assertSame(expectedFlow, result)
    }
}