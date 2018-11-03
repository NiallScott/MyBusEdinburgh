/*
 * Copyright (C) 2014 - 2018 Niall 'Rivernile' Scott
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
 */

package uk.org.rivernile.edinburghbustracker.android.parser.livetimes;

import androidx.annotation.NonNull;
import androidx.test.InstrumentationRegistry;

import org.json.JSONException;
import org.junit.Test;

import java.io.IOException;
import java.util.List;

import uk.org.rivernile.android.bustracker.parser.livetimes.AuthenticationException;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBus;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusStop;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimes;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.fetchutils.fetchers.AssetFileFetcher;
import uk.org.rivernile.android.fetchutils.fetchers.readers.JSONFetcherStreamReader;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Tests for {@link BusTimesParser}.
 *
 * @author Niall Scott
 */
public class BusTimesParserTests {

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} correctly throws a
     * {@link LiveTimesException} containing an {@link AuthenticationException} when the JSON
     * object is set as an API key error.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test(expected = AuthenticationException.class)
    public void testParseBusTimesWithError() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_invalid_app_key.json");
        BusTimesParser.parseBusTimes(reader.getJSONObject());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a
     * non-{@code null} {@link LiveBusTimes} when there are no bus stops in the response.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseBusTimesWithEmptyBusTimes() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_empty.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertTrue(busTimes.isEmpty());
        assertFalse(busTimes.hasGlobalDisruption());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} throws a
     * {@link LiveTimesException} containing a {@link JSONException} when the {@code busTimes} array
     * in JSON is set as {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithNullBusTimes() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_null.json");

        try {
            BusTimesParser.parseBusTimes(reader.getJSONObject());
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }

        fail("The busTimes JSON array is set as null, so a LiveTimesException containing a " +
                "JSONException should be thrown.");
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object that contains no bus stops when the data contains a single bus stop missing a stop
     * code.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithMissingStopCodeSingle() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_missing_stopcode_single.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertTrue(busTimes.isEmpty());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object that contains no bus stops when the data contains a single bus stop with a
     * {@code null} stop code.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithNullStopCodeSingle() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_null_stopcode_single.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertTrue(busTimes.isEmpty());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object that contains no bus stops when the data contains a single bus stop with an empty
     * stop code.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithEmptyStopCodeSingle() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_empty_stopcode_single.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertTrue(busTimes.isEmpty());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object that misses out bus stops which are missing a stop code.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithMissingStopCodeMultiple()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_missing_stopcode_multiple.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());

        final LiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);

        final List<LiveBusService> busServices = busStop.getServices();
        assertEquals(2, busServices.size());

        LiveBusService service = busServices.get(0);
        assertEquals("4", service.getServiceName());

        service = busServices.get(1);
        assertEquals("30", service.getServiceName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object that misses out bus stops which have a {@code null} stop code.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithNullStopCodeMultiple() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_null_stopcode_multiple.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());

        final LiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);

        final List<LiveBusService> busServices = busStop.getServices();
        assertEquals(2, busServices.size());

        LiveBusService service = busServices.get(0);
        assertEquals("4", service.getServiceName());

        service = busServices.get(1);
        assertEquals("30", service.getServiceName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object that misses out bus stops which have an empty stop code.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithEmptyStopCodeMultiple() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_empty_stopcode_multiple.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());

        final LiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);

        final List<LiveBusService> busServices = busStop.getServices();
        assertEquals(2, busServices.size());

        LiveBusService service = busServices.get(0);
        assertEquals("4", service.getServiceName());

        service = busServices.get(1);
        assertEquals("30", service.getServiceName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object with no bus stops when the response only contains a single, invalid service.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithInvalidServiceSingle() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_invalid_service_single.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertTrue(busTimes.isEmpty());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object that misses out bus services which are invalid.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithInvalidServiceMultiple() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_invalid_service_multiple.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());

        final LiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);

        final List<LiveBusService> busServices = busStop.getServices();
        assertEquals(2, busServices.size());

        LiveBusService service = busServices.get(0);
        assertEquals("1", service.getServiceName());

        service = busServices.get(1);
        assertEquals("22", service.getServiceName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object that has a {@code null} stop name when the response does not contain a stop name.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithMissingStopName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_missing_stop_name.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());

        final LiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertNull(busStop.getStopName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object that has a {@code null} stop name when the response contains a {@code null} stop name.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithNullStopName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_null_stop_name.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());

        final LiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertNull(busStop.getStopName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object that has an empty stop name when the response contains an emtpy stop name.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithEmptyStopName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_empty_stop_name.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());

        final LiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertEquals("", busStop.getStopName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object when the bus stop disrupted flag is missing and it sets this flag to false in this
     * case.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithMissingStopDisrupted() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_empty_stop_name.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());

        final LiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertFalse(busStop.isDisrupted());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object when the bus stop disrupted flag is {@code null} and it sets this flag to false in
     * this case.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithNullStopDisrupted() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_null_stop_name.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());

        final LiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertFalse(busStop.isDisrupted());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object when the global disrupted flag is missing and it sets this flag to false in this case.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesWithMissingGlobalDisrupted() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_missing_global_disrupted.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        assertFalse(busTimes.hasGlobalDisruption());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object when the global disrupted flag is {@code null} and it sets this flag to {@code false}
     * in this case.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseBusTimesWithNullGlobalDisrupted() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_null_global_disrupted.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        assertFalse(busTimes.hasGlobalDisruption());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object with the correct services in the correct order when the response is valid.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseBusTimesValidWithSingleStop() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_valid.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        assertFalse(busTimes.hasGlobalDisruption());

        final LiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertFalse(busStop.isDisrupted());
        assertEquals("36237983", busStop.getStopCode());
        assertEquals("Royal Scot Acad", busStop.getStopName());

        final String[] expectedServices = new String[] {
                "1", "4", "22", "30", "34", "44", "44A", "N22", "N34", "N44"
        };

        final List<LiveBusService> busServices = busStop.getServices();
        final int size = busServices.size();
        assertEquals(expectedServices.length, size);

        for (int i = 0; i < size; i++) {
            assertEquals(expectedServices[i], busServices.get(i).getServiceName());
        }
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object with the correct services in the correct order when the response is valid but with
     * the services in the wrong order.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesValidWithSingleStopUnsortedServices() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_valid_unsorted_services.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        assertFalse(busTimes.hasGlobalDisruption());

        final LiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertFalse(busStop.isDisrupted());
        assertEquals("36237983", busStop.getStopCode());
        assertEquals("Royal Scot Acad", busStop.getStopName());

        final String[] expectedServices = new String[] {
                "1", "4", "22", "30", "34", "44", "44A", "N22", "N34", "N44"
        };

        final List<LiveBusService> busServices = busStop.getServices();
        final int size = busServices.size();
        assertEquals(expectedServices.length, size);

        for (int i = 0; i < size; i++) {
            assertEquals(expectedServices[i], busServices.get(i).getServiceName());
        }
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object with the correct services in the correct order when the response is valid and
     * contains services from multiple bus stops.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesValidWithMultipleStops() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_valid_multiple_stops.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(3, busTimes.size());
        assertFalse(busTimes.hasGlobalDisruption());

        // Tests for 36237983
        final LiveBusStop busStop1 = busTimes.getBusStop("36237983");
        assertNotNull(busStop1);
        assertFalse(busStop1.isDisrupted());
        assertEquals("36237983", busStop1.getStopCode());
        assertEquals("Royal Scot Acad", busStop1.getStopName());

        final String[] expectedServices1 = new String[] {
                "1", "4", "22", "30", "34", "44A", "N22", "N34", "N44"
        };

        final List<LiveBusService> busServices1 = busStop1.getServices();
        final int size1 = busServices1.size();
        assertEquals(expectedServices1.length, size1);

        for (int i = 0; i < size1; i++) {
            assertEquals(expectedServices1[i], busServices1.get(i).getServiceName());
        }

        // Tests for 36236464
        final LiveBusStop busStop2 = busTimes.getBusStop("36236464");
        assertNotNull(busStop2);
        assertFalse(busStop2.isDisrupted());
        assertEquals("36236464", busStop2.getStopCode());
        assertEquals("Lauriston Terrac", busStop2.getStopName());

        final String[] expectedServices2 = new String[] {
                "23", "27", "35", "47"
        };

        final List<LiveBusService> busServices2 = busStop2.getServices();
        final int size2 = busServices2.size();
        assertEquals(expectedServices2.length, size2);

        for (int i = 0; i < size2; i++) {
            assertEquals(expectedServices2[i], busServices2.get(i).getServiceName());
        }

        // Tests for 36243526
        final LiveBusStop busStop3 = busTimes.getBusStop("36243526");
        assertNotNull(busStop3);
        assertFalse(busStop3.isDisrupted());
        assertEquals("36243526", busStop3.getStopCode());
        assertEquals("Fountainpark", busStop3.getStopName());

        final String[] expectedServices3 = new String[] {
                "22", "30", "N22"
        };

        final List<LiveBusService> busServices3 = busStop3.getServices();
        final int size3 = busServices3.size();
        assertEquals(expectedServices3.length, size3);

        for (int i = 0; i < size3; i++) {
            assertEquals(expectedServices3[i], busServices3.get(i).getServiceName());
        }
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimes(org.json.JSONObject)} returns a valid
     * object with the correct services in the correct order when the response is valid and
     * contains services from multiple bus stops but with the order mixed up.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesValidWithMultipleStopsUnsortedServices() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/" +
                        "bustimes_valid_multiple_stops_unsorted_services.json");
        final LiveBusTimes busTimes = BusTimesParser.parseBusTimes(reader.getJSONObject());

        assertNotNull(busTimes);
        assertEquals(3, busTimes.size());
        assertFalse(busTimes.hasGlobalDisruption());

        // Tests for 36237983
        final LiveBusStop busStop1 = busTimes.getBusStop("36237983");
        assertNotNull(busStop1);
        assertFalse(busStop1.isDisrupted());
        assertEquals("36237983", busStop1.getStopCode());
        assertEquals("Royal Scot Acad", busStop1.getStopName());

        final String[] expectedServices1 = new String[] {
                "1", "4", "22", "30", "34", "44A", "N22", "N34", "N44"
        };

        final List<LiveBusService> busServices1 = busStop1.getServices();
        final int size1 = busServices1.size();
        assertEquals(expectedServices1.length, size1);

        for (int i = 0; i < size1; i++) {
            assertEquals(expectedServices1[i], busServices1.get(i).getServiceName());
        }

        // Tests for 36236464
        final LiveBusStop busStop2 = busTimes.getBusStop("36236464");
        assertNotNull(busStop2);
        assertFalse(busStop2.isDisrupted());
        assertEquals("36236464", busStop2.getStopCode());
        assertEquals("Lauriston Terrac", busStop2.getStopName());

        final String[] expectedServices2 = new String[] {
                "23", "27", "35", "47"
        };

        final List<LiveBusService> busServices2 = busStop2.getServices();
        final int size2 = busServices2.size();
        assertEquals(expectedServices2.length, size2);

        for (int i = 0; i < size2; i++) {
            assertEquals(expectedServices2[i], busServices2.get(i).getServiceName());
        }

        // Tests for 36243526
        final LiveBusStop busStop3 = busTimes.getBusStop("36243526");
        assertNotNull(busStop3);
        assertFalse(busStop3.isDisrupted());
        assertEquals("36243526", busStop3.getStopCode());
        assertEquals("Fountainpark", busStop3.getStopName());

        final String[] expectedServices3 = new String[] {
                "22", "30", "N22"
        };

        final List<LiveBusService> busServices3 = busStop3.getServices();
        final int size3 = busServices3.size();
        assertEquals(expectedServices3.length, size3);

        for (int i = 0; i < size3; i++) {
            assertEquals(expectedServices3[i], busServices3.get(i).getServiceName());
        }
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns
     * {@code null} when the {@link org.json.JSONObject} sent in is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithEmptyJsonObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/generic/empty_object.json");
        assertNull(BusTimesParser.parseBusTimesBusService(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns
     * {@code null} when the service name is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithMissingServiceName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_missing_service_name.json");
        assertNull(BusTimesParser.parseBusTimesBusService(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns
     * {@code null} when the service name is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithEmptyServiceName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_empty_service_name.json");
        assertNull(BusTimesParser.parseBusTimesBusService(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns
     * {@code null} when the service name is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithNullServiceName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_null_service_name.json");
        assertNull(BusTimesParser.parseBusTimesBusService(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid object and the service name is "TRAM" when the service name is set to "50". This is
     * because the tram is service 50 in the bus tracker system.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithTramNameConversion50() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_tram_50.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertEquals("TRAM", service.getServiceName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid object and the service name is "TRAM" when the service name is set to "T50". This is
     * because the tram is service 50 in the bus tracker system, although it has been seen set to
     * T50 too.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithTramNameConversionT50() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_tram_T50.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertEquals("TRAM", service.getServiceName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid object when the operator field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithMissingOperator() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_missing_operator.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertNull(service.getOperator());
        assertEquals("1", service.getServiceName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid object when the operator field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithEmptyOperator() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_empty_operator.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertEquals("", service.getOperator());
        assertEquals("1", service.getServiceName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid object when the operator field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithNullOperator() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_null_operator.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertNull(service.getOperator());
        assertEquals("1", service.getServiceName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid object when the route field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithMissingRoute() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_missing_route.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertNull(service.getRoute());
        assertEquals("1", service.getServiceName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid object when the route field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithEmptyRoute() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_empty_route.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertEquals("", service.getRoute());
        assertEquals("1", service.getServiceName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid object when the route field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithNullRoute() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_null_route.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertNull(service.getRoute());
        assertEquals("1", service.getServiceName());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid object when the disruption field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithMissingDisruption() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_missing_disruption.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertFalse(service.isDisrupted());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid object when the disruption field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithNullDisruption() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_null_disruption.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertFalse(service.isDisrupted());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid object when the diversion field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithMissingDiversion() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_missing_diversion.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertFalse(service.isDiverted());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid object when the diversion field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithNullDiversion() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_null_diversion.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertFalse(service.isDiverted());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns
     * {@code null} when the bus array is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithMissingBusArray() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_missing_bus_array.json");
        assertNull(BusTimesParser.parseBusTimesBusService(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns {@code null} when the bus array is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithNullBusArray() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_null_bus_array.json");
        assertNull(BusTimesParser.parseBusTimesBusService(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns
     * {@code null} when the bus array is empty.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithEmptyBusArray() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_empty_bus_array.json");
        assertNull(BusTimesParser.parseBusTimesBusService(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns
     * {@code null} when the bus array contains a single invalid bus.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithInvalidBusSingle() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_invalid_bus_single.json");
        assertNull(BusTimesParser.parseBusTimesBusService(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid object when a single bus could not be parsed in the bus array, but there were other
     * buses that could be parsed in the array.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithInvalidBusMultiple() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_invalid_bus_multiple.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertEquals("1", service.getServiceName());
        assertEquals("LB", service.getOperator());

        final List<LiveBus> buses = service.getLiveBuses();
        assertEquals(1, buses.size());

        final LiveBus bus = buses.get(0);
        assertEquals(47, bus.getDepartureMinutes());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid service when there is a single bus and the fields are as expected.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithSingleBus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_single_bus.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertEquals("1", service.getServiceName());
        assertEquals("LB", service.getOperator());

        final List<LiveBus> buses = service.getLiveBuses();
        assertEquals(1, buses.size());

        final LiveBus bus = buses.get(0);
        assertEquals("Clermiston", bus.getDestination());
        assertEquals(17, bus.getDepartureMinutes());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid service with the buses in the correct order when there are multiple buses in an
     * incorrect order.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceWithMultipleBusUnsorted() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_multiple_bus_unsorted.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertEquals("1", service.getServiceName());
        assertEquals("LB", service.getOperator());

        final List<LiveBus> buses = service.getLiveBuses();
        assertEquals(4, buses.size());

        LiveBus bus = buses.get(0);
        assertEquals(17, bus.getDepartureMinutes());

        bus = buses.get(1);
        assertEquals(47, bus.getDepartureMinutes());

        bus = buses.get(2);
        assertEquals(77, bus.getDepartureMinutes());

        bus = buses.get(3);
        assertEquals(107, bus.getDepartureMinutes());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBusService(org.json.JSONObject)} returns a
     * valid service when the service object is fully compliant.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusServiceValid() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_valid.json");
        final LiveBusService service = BusTimesParser
                .parseBusTimesBusService(reader.getJSONObject());

        assertNotNull(service);
        assertEquals("1", service.getServiceName());
        assertEquals("LB", service.getOperator());
        assertEquals("Easter Road -- Clermiston", service.getRoute());
        assertFalse(service.isDisrupted());
        assertFalse(service.isDiverted());

        final List<LiveBus> buses = service.getLiveBuses();
        assertEquals(2, buses.size());

        LiveBus bus = buses.get(0);
        assertEquals(17, bus.getDepartureMinutes());

        bus = buses.get(1);
        assertEquals(47, bus.getDepartureMinutes());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns
     * {@code null} when the {@link org.json.JSONObject} sent in is empty.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithEmptyJsonObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/generic/empty_object.json");
        assertNull(BusTimesParser.parseBusTimesBus(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns
     * {@code null} when the destination is missing.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithMissingDestination() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_missing_destination.json");
        assertNull(BusTimesParser.parseBusTimesBus(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns
     * {@code null} when the destination is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithNullDestination() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_null_destination.json");
        assertNull(BusTimesParser.parseBusTimesBus(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns
     * {@code null} when the destination is empty.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithEmptyDestination() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_empty_destination.json");
        assertNull(BusTimesParser.parseBusTimesBus(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns
     * {@code null} when the minutes field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithMissingMinutes() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_missing_minutes.json");
        assertNull(BusTimesParser.parseBusTimesBus(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns
     * {@code null} when the minutes field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithNullMinutes() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_null_minutes.json");
        assertNull(BusTimesParser.parseBusTimesBus(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns
     * {@code null} when the reliability field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithMissingReliability() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_missing_reliability.json");
        assertNull(BusTimesParser.parseBusTimesBus(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns
     * {@code null} when the reliability field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithNullReliability() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_null_reliability.json");
        assertNull(BusTimesParser.parseBusTimesBus(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns
     * {@code null} when the reliability field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithEmptyReliability() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_empty_reliability.json");
        assertNull(BusTimesParser.parseBusTimesBus(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns
     * {@code null} when the type field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithMissingType() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_missing_type.json");
        assertNull(BusTimesParser.parseBusTimesBus(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns
     * {@code null} when the type field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithNullType() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_null_type.json");
        assertNull(BusTimesParser.parseBusTimesBus(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns
     * {@code null} when the type field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithEmptyType() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_empty_type.json");
        assertNull(BusTimesParser.parseBusTimesBus(reader.getJSONObject()));
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns a valid
     * object when the terminus field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithMissingTerminus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_missing_terminus.json");
        final EdinburghLiveBus bus = BusTimesParser.parseBusTimesBus(reader.getJSONObject());

        assertNotNull(bus);
        assertNull(bus.getTerminus());
        assertEquals("Clermiston", bus.getDestination());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns a valid
     * object when the terminus field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithNullTerminus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_null_terminus.json");
        final EdinburghLiveBus bus = BusTimesParser.parseBusTimesBus(reader.getJSONObject());

        assertNotNull(bus);
        assertNull(bus.getTerminus());
        assertEquals("Clermiston", bus.getDestination());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns a valid
     * object when the terminus field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithEmptyTerminus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_empty_terminus.json");
        final EdinburghLiveBus bus = BusTimesParser.parseBusTimesBus(reader.getJSONObject());

        assertNotNull(bus);
        assertEquals("", bus.getTerminus());
        assertEquals("Clermiston", bus.getDestination());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns a valid
     * object when the journeyId field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithMissingJourneyId() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_missing_journey_id.json");
        final EdinburghLiveBus bus = BusTimesParser.parseBusTimesBus(reader.getJSONObject());

        assertNotNull(bus);
        assertNull(bus.getJourneyId());
        assertEquals("Clermiston", bus.getDestination());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns a valid
     * object when the journeyId field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithNullJourneyId() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_null_journey_id.json");
        final EdinburghLiveBus bus = BusTimesParser.parseBusTimesBus(reader.getJSONObject());

        assertNotNull(bus);
        assertNull(bus.getJourneyId());
        assertEquals("Clermiston", bus.getDestination());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns a valid
     * object when the journeyId field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusWithEmptyJourneyId() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_empty_journey_id.json");
        final EdinburghLiveBus bus = BusTimesParser.parseBusTimesBus(reader.getJSONObject());

        assertNotNull(bus);
        assertEquals("", bus.getJourneyId());
        assertEquals("Clermiston", bus.getDestination());
    }

    /**
     * Test that {@link BusTimesParser#parseBusTimesBus(org.json.JSONObject)} returns a valid
     * object with its fields correctly populated when the bus object in JSON is valid.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseBusTimesBusValid() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_valid.json");
        final LiveBus bus = BusTimesParser.parseBusTimesBus(reader.getJSONObject());

        assertNotNull(bus);
        assertEquals("Clermiston", bus.getDestination());
        assertEquals(47, bus.getDepartureMinutes());
        assertEquals("36237636", bus.getTerminus());
        assertEquals("1461", bus.getJourneyId());
        assertTrue(bus.isEstimatedTime());
        assertFalse(bus.isDelayed());
        assertFalse(bus.isDiverted());
        assertTrue(bus.isPartRoute());
        assertFalse(bus.isTerminus());
    }

    /**
     * Get the data from assets.
     *
     * @param filePath The file path in the assets to get data from.
     * @return The data.
     * @throws IOException When there was a problem reading the data.
     */
    @NonNull
    private JSONFetcherStreamReader getReaderAfterFetchingData(@NonNull final String filePath)
            throws IOException {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(InstrumentationRegistry.getContext(), filePath);
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        fetcher.executeFetcher(reader);

        return reader;
    }
}
