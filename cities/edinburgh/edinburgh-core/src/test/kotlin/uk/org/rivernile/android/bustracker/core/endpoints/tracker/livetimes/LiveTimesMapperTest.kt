/*
 * Copyright (C) 2019 - 2024 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.endpoints.tracker.livetimes

import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.mock
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.endpoints.tracker.ErrorMapper
import uk.org.rivernile.android.bustracker.core.time.TimeUtils
import uk.org.rivernile.edinburghbustrackerapi.bustimes.BusTime
import uk.org.rivernile.edinburghbustrackerapi.bustimes.BusTimes
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Tests for [LiveTimesMapper].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
internal class LiveTimesMapperTest {

    companion object {

        private const val TEST_TIMESTAMP = 123L
        private const val TEST_STOP_CODE = "123456"
        private const val TEST_STOP_NAME = "Stop name"
        private const val TEST_STOP_CODE_2 = "987654"
        private const val TEST_STOP_NAME_2 = "Stop name 2"
        private const val TEST_STOP_CODE_3 = "135791"
    }

    @Mock
    lateinit var errorMapper: ErrorMapper
    @Mock
    lateinit var serviceMapper: ServiceMapper
    @Mock
    lateinit var timeUtils: TimeUtils

    @Mock
    lateinit var busTimes: BusTimes
    @Mock
    lateinit var busTime: BusTime
    @Mock
    lateinit var service: Service

    private lateinit var liveTimesMapper: LiveTimesMapper

    @BeforeTest
    fun setUp() {
        liveTimesMapper = LiveTimesMapper(
            errorMapper,
            serviceMapper,
            timeUtils
        )
    }

    @Test
    fun mapToLiveTimesThrowsExceptionWhenErrorIsFoundInData() {
        val expected = LiveTimesResponse.Error.ServerError.Other()
        whenever(errorMapper.extractError(busTimes))
            .thenReturn(expected)

        val result = liveTimesMapper.mapToLiveTimes(busTimes)

        assertEquals(expected, result)
    }

    @Test
    fun mapToLiveTimesReturnsEmptyTimesWhenBusTimesIsNull() {
        givenTimeUtilsReturnsTestTimestamp()
        whenever(busTimes.busTimes)
            .thenReturn(null)
        val expected = LiveTimesResponse.Success(LiveTimes(emptyMap(), TEST_TIMESTAMP, false))

        val result = liveTimesMapper.mapToLiveTimes(busTimes)

        assertEquals(expected, result)
    }

    @Test
    fun mapToLiveTimesReturnsEmptyTimesWhenBusTimesIsEmpty() {
        givenTimeUtilsReturnsTestTimestamp()
        whenever(busTimes.busTimes)
            .thenReturn(emptyList())
        val expected = LiveTimesResponse.Success(LiveTimes(emptyMap(), TEST_TIMESTAMP, false))

        val result = liveTimesMapper.mapToLiveTimes(busTimes)

        assertEquals(expected, result)
    }

    @Test
    fun mapToLiveTimesReturnsEmptyTimesWhenSingleBusTimesHasNullStopCode() {
        givenTimeUtilsReturnsTestTimestamp()
        whenever(busTimes.busTimes)
            .thenReturn(listOf(busTime))
        whenever(busTime.stopId)
            .thenReturn(null)
        val expected = LiveTimesResponse.Success(LiveTimes(emptyMap(), TEST_TIMESTAMP, false))

        val result = liveTimesMapper.mapToLiveTimes(busTimes)

        assertEquals(expected, result)
        verify(serviceMapper, never())
            .mapToService(busTime)
    }

    @Test
    fun mapToLiveTimesReturnsEmptyTimesWhenSingleBusTimesHasEmptyStopCode() {
        givenTimeUtilsReturnsTestTimestamp()
        whenever(busTimes.busTimes)
            .thenReturn(listOf(busTime))
        whenever(busTime.stopId)
            .thenReturn("")
        val expected = LiveTimesResponse.Success(LiveTimes(emptyMap(), TEST_TIMESTAMP, false))

        val result = liveTimesMapper.mapToLiveTimes(busTimes)

        assertEquals(expected, result)
        verify(serviceMapper, never())
            .mapToService(busTime)
    }

    @Test
    fun mapToLiveTimesReturnsEmptyTimesWhenServiceMapperReturnsNullForSingleBusTime() {
        givenTimeUtilsReturnsTestTimestamp()
        whenever(busTimes.busTimes)
            .thenReturn(listOf(busTime))
        whenever(busTime.stopId)
            .thenReturn(TEST_STOP_CODE)
        whenever(serviceMapper.mapToService(busTime))
            .thenReturn(null)
        val expected = LiveTimesResponse.Success(LiveTimes(emptyMap(), TEST_TIMESTAMP, false))

        val result = liveTimesMapper.mapToLiveTimes(busTimes)

        assertEquals(expected, result)
    }

    @Test
    fun mapToLiveTimesReturnsSingleStopWithSingleServiceWhenSingleBusTimeProvided() {
        givenTimeUtilsReturnsTestTimestamp()
        givenBusTimeIsSetUpWithValues(busTime, TEST_STOP_CODE, TEST_STOP_NAME)
        whenever(busTimes.busTimes)
            .thenReturn(listOf(busTime))
        whenever(serviceMapper.mapToService(busTime))
            .thenReturn(service)
        val expectedStop = Stop(TEST_STOP_CODE, TEST_STOP_NAME, listOf(service), true)
        val expectedLiveTimes = LiveTimesResponse.Success(
            LiveTimes(
                mapOf(TEST_STOP_CODE to expectedStop),
                TEST_TIMESTAMP,
                true
            )
        )

        val result = liveTimesMapper.mapToLiveTimes(busTimes)

        assertEquals(expectedLiveTimes, result)
    }

    @Test
    fun mapToLiveTimesReturnsSingleStopWithMultipleServicesWhenMultipleBusTimesProvided() {
        givenTimeUtilsReturnsTestTimestamp()
        val busTime1 = mock<BusTime>()
        val busTime2 = mock<BusTime>()
        val busTime3 = mock<BusTime>()
        val service1 = mock<Service>()
        val service2 = mock<Service>()
        val service3 = mock<Service>()
        whenever(busTimes.busTimes)
            .thenReturn(listOf(busTime1, busTime2, busTime3))
        givenBusTimeIsSetUpWithValues(busTime1, TEST_STOP_CODE, TEST_STOP_NAME)
        givenBusTimeIsSetUpWithValues(busTime2, TEST_STOP_CODE, TEST_STOP_NAME)
        givenBusTimeIsSetUpWithValues(busTime3, TEST_STOP_CODE, TEST_STOP_NAME)
        givenServiceMapperReturnsMappings(
            mapOf(
                busTime1 to service1,
                busTime2 to service2,
                busTime3 to service3
            )
        )
        val expectedStop = Stop(
            TEST_STOP_CODE,
            TEST_STOP_NAME,
            listOf(service1, service2, service3),
            true
        )
        val expectedLiveTimes = LiveTimesResponse.Success(
            LiveTimes(
                mapOf(TEST_STOP_CODE to expectedStop),
                TEST_TIMESTAMP,
                true
            )
        )

        val result = liveTimesMapper.mapToLiveTimes(busTimes)

        assertEquals(expectedLiveTimes, result)
    }

    @Test
    fun mapToLiveTimesReturnsMultipleStopsWithSingleServiceWhenMultipleBusTimesProvided() {
        givenTimeUtilsReturnsTestTimestamp()
        val busTime1 = mock<BusTime>()
        val busTime2 = mock<BusTime>()
        val busTime3 = mock<BusTime>()
        val service1 = mock<Service>()
        val service2 = mock<Service>()
        val service3 = mock<Service>()
        whenever(busTimes.busTimes)
            .thenReturn(listOf(busTime1, busTime2, busTime3))
        givenBusTimeIsSetUpWithValues(busTime1, TEST_STOP_CODE, TEST_STOP_NAME)
        givenBusTimeIsSetUpWithValues(busTime2, TEST_STOP_CODE_2, TEST_STOP_NAME_2)
        givenBusTimeIsSetUpWithValues(busTime3, TEST_STOP_CODE_3, null)
        givenServiceMapperReturnsMappings(mapOf(
            busTime1 to service1,
            busTime2 to service2,
            busTime3 to service3))
        val expectedStop1 = Stop(
            TEST_STOP_CODE,
            TEST_STOP_NAME,
            listOf(service1),
            true
        )
        val expectedStop2 = Stop(
            TEST_STOP_CODE_2,
            TEST_STOP_NAME_2,
            listOf(service2),
            true
        )
        val expectedStop3 = Stop(
            TEST_STOP_CODE_3,
            null,
            listOf(service3),
            true
        )
        val expectedLiveTimes = LiveTimesResponse.Success(
            LiveTimes(
                mapOf(
                    TEST_STOP_CODE to expectedStop1,
                    TEST_STOP_CODE_2 to expectedStop2,
                    TEST_STOP_CODE_3 to expectedStop3
                ),
                TEST_TIMESTAMP,
                true
            )
        )

        val result = liveTimesMapper.mapToLiveTimes(busTimes)

        assertEquals(expectedLiveTimes, result)
    }

    @Test
    fun mapToLiveTimesReturnsMultipleStopsWithMultipleServicesWhenMultipleBusTimesProvided() {
        givenTimeUtilsReturnsTestTimestamp()
        val busTime1 = mock<BusTime>()
        val busTime2 = mock<BusTime>()
        val busTime3 = mock<BusTime>()
        val busTime4 = mock<BusTime>()
        val busTime5 = mock<BusTime>()
        val service1 = mock<Service>()
        val service3 = mock<Service>()
        val service4 = mock<Service>()
        val service5 = mock<Service>()
        whenever(busTimes.busTimes)
            .thenReturn(listOf(busTime1, busTime2, busTime3, busTime4, busTime5))
        givenBusTimeIsSetUpWithValues(busTime1, TEST_STOP_CODE, TEST_STOP_NAME)
        givenBusTimeIsSetUpWithValues(busTime2, TEST_STOP_CODE_2, TEST_STOP_NAME_2)
        givenBusTimeIsSetUpWithValues(busTime3, TEST_STOP_CODE_3, null)
        givenBusTimeIsSetUpWithValues(busTime4, TEST_STOP_CODE, TEST_STOP_NAME)
        givenBusTimeIsSetUpWithValues(busTime5, TEST_STOP_CODE, TEST_STOP_NAME)
        givenServiceMapperReturnsMappings(
            mapOf(
                busTime1 to service1,
                busTime2 to null,
                busTime3 to service3,
                busTime4 to service4,
                busTime5 to service5
            )
        )
        val expectedStop1 = Stop(
            TEST_STOP_CODE,
            TEST_STOP_NAME,
            listOf(service1, service4, service5),
            true
        )
        val expectedStop2 = Stop(
            TEST_STOP_CODE_3,
            null,
            listOf(service3),
            true
        )
        val expectedLiveTimes = LiveTimesResponse.Success(
            LiveTimes(
                mapOf(
                    TEST_STOP_CODE to expectedStop1,
                    TEST_STOP_CODE_3 to expectedStop2
                ),
                TEST_TIMESTAMP,
                true
            )
        )

        val result = liveTimesMapper.mapToLiveTimes(busTimes)

        assertEquals(expectedLiveTimes, result)
    }

    @Test
    fun emptyLiveTimesProducesLiveTimesInstanceWithNoStops() {
        givenTimeUtilsReturnsTestTimestamp()
        val expected = LiveTimesResponse.Success(LiveTimes(emptyMap(), TEST_TIMESTAMP, false))

        val result = liveTimesMapper.emptyLiveTimes()

        assertEquals(expected, result)
    }

    private fun givenTimeUtilsReturnsTestTimestamp() {
        whenever(timeUtils.currentTimeMills)
            .thenReturn(TEST_TIMESTAMP)
    }

    private fun givenBusTimeIsSetUpWithValues(
        busTime: BusTime,
        stopCode: String?,
        stopName: String?) {
        whenever(busTime.stopId)
            .thenReturn(stopCode)
        whenever(busTime.stopName)
            .thenReturn(stopName)
        whenever(busTime.busStopDisruption)
            .thenReturn(true)
        whenever(busTime.globalDisruption)
            .thenReturn(true)
    }

    private fun givenServiceMapperReturnsMappings(mappings: Map<BusTime, Service?>) {
        for ((k, v) in mappings) {
            whenever(serviceMapper.mapToService(k))
                .thenReturn(v)
        }
    }
}