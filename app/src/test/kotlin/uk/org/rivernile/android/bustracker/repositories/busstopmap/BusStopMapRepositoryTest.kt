/*
 * Copyright (C) 2018 - 2019 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.repositories.busstopmap

import com.nhaarman.mockitokotlin2.verify
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner

/**
 * Tests for [BusStopMapRepository].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class BusStopMapRepositoryTest {

    @Mock
    lateinit var liveDataFactory: BusStopMapLiveDataFactory

    private lateinit var repository: BusStopMapRepository

    @Before
    fun setUp() {
        repository = BusStopMapRepository(liveDataFactory)
    }

    @Test
    fun getServiceNamesCreatesServiceNamesLiveData() {
        repository.getServiceNames()

        verify(liveDataFactory)
                .createServiceNamesLiveData()
    }

    @Test
    fun getBusStopsCreatesBusStopsLiveData() {
        repository.getBusStops(arrayOf("1", "2", "3"))

        verify(liveDataFactory)
                .createBusStopsLiveData(arrayOf("1", "2", "3"))
    }

    @Test
    fun getBusStopCreateaBusStopLiveData() {
        repository.getBusStop("123456")

        verify(liveDataFactory)
                .createBusStopLiveData("123456")
    }

    @Test
    fun getRouteLinesCreatesRouteLinesLiveData() {
        repository.getRouteLines(arrayOf("1", "2", "3"))

        verify(liveDataFactory)
                .createRouteLineLiveData(arrayOf("1", "2", "3"))
    }
}