/*
 * Copyright (C) 2014 Niall 'Rivernile' Scott
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

import android.test.InstrumentationTestCase;
import java.io.IOException;
import java.util.List;
import org.json.JSONException;
import uk.org.rivernile.android.bustracker.parser.livetimes
        .AuthenticationException;
import uk.org.rivernile.android.bustracker.parser.livetimes
        .LiveTimesException;
import uk.org.rivernile.android.bustracker.parser.livetimes
        .MaintenanceException;
import uk.org.rivernile.android.bustracker.parser.livetimes
        .ServerErrorException;
import uk.org.rivernile.android.bustracker.parser.livetimes
        .SystemOverloadedException;
import uk.org.rivernile.android.fetchers.AssetFileFetcher;
import uk.org.rivernile.android.fetchers.readers.JSONFetcherStreamReader;

/**
 * Tests for {@link EdinburghParser}.
 * 
 * @author Niall Scott
 */
public class EdinburghParserTests extends InstrumentationTestCase {
    
    private EdinburghParser parser;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        parser = new EdinburghParser();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        parser = null;
    }
    
    /**
     * Test that{@link EdinburghParser#getBusTimes(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly throws an IllegalArgumentException when the fetcher is set as
     * null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetBusTimesWithNullFetcher() throws Exception {
        try {
            parser.getBusTimes(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The fetcher is set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that {@link EdinburghParser#getBusTimes(uk.org.rivernile.android.fetchers.Fetcher))}
     * correctly throws a {@link LiveTimesException} containing an
     * {@link IOException} when the fetcher is set to fetch a resource that does
     * not exist.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetBusTimesWithInvalidSource() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/generic/does_not_exist.json");
        
        try {
            parser.getBusTimes(fetcher);
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof IOException) {
                return;
            }
        }
        
        fail("The fetcher is set to an incorrect resource, so an IOException "
                + "should be set as the cause in the LiveTimesException.");
    }
    
    /**
     * Test that {@link EdinburghParser#getBusTimes(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly throws a {@link LiveTimesException} containing a
     * {@link JSONException} when the fetcher is set to fetch a resource that
     * contains invalid data.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetBusTimesWithInvalidJson() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/generic/invalid.json");
        
        try {
            parser.getBusTimes(fetcher);
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }
        
        fail("The fetcher is set to return invalid JSON, so a JSONException "
                + "should be set as the cause in the LiveTimesException.");
    }
    
    /**
     * Test that {@link EdinburghParser#getBusTimes(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly throws a {@link LiveTimesException} containing a
     * {@link JSONException} when the fetcher returns a JSON array rather than
     * a JSON object.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetBusTimesWithJsonArray() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/generic/empty_array.json");
        
        try {
            parser.getBusTimes(fetcher);
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }
        
        fail("The fetcher is set to get a JSON array, but a JSON objecy is "
                + "expected. A JSONException should be set as the cause in the "
                + "LiveTimesException.");
    }
    
    /**
     * Test that {@link EdinburghParser#getBusTimes(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly throws a {@link ServerErrorException} when the fetcher returns
     * an error response.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetBusTimesWithError() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/bustracker/generic/error_processing_error.json");
        
        try {
            parser.getBusTimes(fetcher);
        } catch (ServerErrorException e) {
            return;
        }
        
        fail("The fetcher is set to retrieve an error response that contains a "
                + "server error. A ServerErrorException should be thrown.");
    }
    
    /**
     * Test that {@link EdinburghParser#getBusTimes(uk.org.rivernile.android.fetchers.Fetcher)}
     * returns a valid object when it is supplied valid data.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetBusTimesValid() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/bustracker/getBusTimes/bustimes_valid.json");
        final EdinburghLiveBusTimes busTimes = (EdinburghLiveBusTimes)
                parser.getBusTimes(fetcher);
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        assertFalse(busTimes.isGlobalDisruption());
        
        final EdinburghLiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertFalse(busStop.isDisrupted());
        assertEquals("36237983", busStop.getStopCode());
        assertEquals("Royal Scot Acad", busStop.getStopName());
        
        final String[] expectedServices = new String[] {
            "1", "4", "22", "30", "34", "44", "44A", "N22", "N34", "N44"
        };
        
        final List<EdinburghLiveBusService> busServices = busStop.getServices();
        final int size = busServices.size();
        assertEquals(expectedServices.length, size);
        
        for (int i = 0; i < size; i++) {
            assertEquals(expectedServices[i],
                    busServices.get(i).getServiceName());
        }
    }
    
    /**
     * Test that{@link EdinburghParser#getJourneyTimes(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly throws an IllegalArgumentException when the fetcher is set as
     * null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetJourneyTimesWithNullFetcher() throws Exception {
        try {
            parser.getJourneyTimes(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The fetcher is set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that {@link EdinburghParser#getJourneyTimes(uk.org.rivernile.android.fetchers.Fetcher))}
     * correctly throws a {@link LiveTimesException} containing an
     * {@link IOException} when the fetcher is set to fetch a resource that does
     * not exist.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetJourneyTimesWithInvalidSource() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/generic/does_not_exist.json");
        
        try {
            parser.getJourneyTimes(fetcher);
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof IOException) {
                return;
            }
        }
        
        fail("The fetcher is set to an incorrect resource, so an IOException "
                + "should be set as the cause in the LiveTimesException.");
    }
    
    /**
     * Test that {@link EdinburghParser#getJourneyTimes(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly throws a {@link LiveTimesException} containing a
     * {@link JSONException} when the fetcher is set to fetch a resource that
     * contains invalid data.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetJourneyTimesWithInvalidJson() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/generic/invalid.json");
        
        try {
            parser.getJourneyTimes(fetcher);
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }
        
        fail("The fetcher is set to return invalid JSON, so a JSONException "
                + "should be set as the cause in the LiveTimesException.");
    }
    
    /**
     * Test that {@link EdinburghParser#getJourneyTimes(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly throws a {@link LiveTimesException} containing a
     * {@link JSONException} when the fetcher returns a JSON array rather than
     * a JSON object.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetJourneyTimesWithJsonArray() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/generic/empty_array.json");
        
        try {
            parser.getJourneyTimes(fetcher);
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }
        
        fail("The fetcher is set to get a JSON array, but a JSON objecy is "
                + "expected. A JSONException should be set as the cause in the "
                + "LiveTimesException.");
    }
    
    /**
     * Test that {@link EdinburghParser#getJourneyTimes(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly throws a {@link ServerErrorException} when the fetcher returns
     * an error response.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetJourneyTimesWithError() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/bustracker/generic/error_processing_error.json");
        
        try {
            parser.getJourneyTimes(fetcher);
        } catch (ServerErrorException e) {
            return;
        }
        
        fail("The fetcher is set to retrieve an error response that contains a "
                + "server error. A ServerErrorException should be thrown.");
    }
    
    /**
     * Test that {@link EdinburghParser#getJourneyTimes(uk.org.rivernile.android.fetchers.Fetcher)}
     * returns a valid object when it is supplied valid data.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetJourneyTimesValid() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/bustracker/getJourneyTimes/journeytimes_valid.json");
        final EdinburghJourney journey = (EdinburghJourney) parser
                .getJourneyTimes(fetcher);
        
        assertEquals("3322", journey.getJourneyId());
        assertEquals("LB", journey.getOperator());
        assertEquals("22", journey.getServiceName());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * correctly throws a {@link LiveTimesException} when the JSON object is set
     * as null.
     */
    public void testParseBusTimesWithNullJsonObject() {
        try {
            EdinburghParser.parseBusTimes(null);
        } catch (LiveTimesException e) {
            return;
        }
        
        fail("The JSON object is set as null, so LiveTimesException should be "
                + "thrown.");
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * correctly throws a {@link LiveTimesException} containing an
     * {@link AuthenticationException} when the JSON object is set as an API key
     * error.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithError() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_invalid_app_key.json");
        
        try {
            EdinburghParser.parseBusTimes(reader.getJSONObject());
        } catch (AuthenticationException e) {
            return;
        }
        
        fail("An API key error was passed in, so a LiveTimesException "
                + "containing an AuthenticationException should be thrown");
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a non-null {@link EdinburghLiveBusTimes} when there are no bus
     * stops in the response.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithEmptyBusTimes() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_empty.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertTrue(busTimes.isEmpty());
        assertFalse(busTimes.isGlobalDisruption());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * throws a {@link LiveTimesException} containing a {@link JSONException}
     * when the busTimes array in JSON is set as null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithNullBusTimes() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_null.json");
        
        try {
            EdinburghParser.parseBusTimes(reader.getJSONObject());
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }
        
        fail("The busTimes JSON array is set as null, so a LiveTimesException "
                + "containing a JSONException should be thrown.");
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object that contains no bus stops when the data contains
     * a single bus stop missing a stop code.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithMissingStopCodeSingle() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_missing_stopcode_single.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertTrue(busTimes.isEmpty());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object that contains no bus stops when the data contains
     * a single bus stop with a null stop code.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithNullStopCodeSingle() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_null_stopcode_single.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertTrue(busTimes.isEmpty());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object that contains no bus stops when the data contains
     * a single bus stop with an empty stop code.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithEmptyStopCodeSingle() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_empty_stopcode_single.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertTrue(busTimes.isEmpty());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object that misses out bus stops which are missing a stop
     * code.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithMissingStopCodeMultiple()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_missing_stopcode_multiple.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        
        final EdinburghLiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        
        final List<EdinburghLiveBusService> busServices = busStop.getServices();
        assertEquals(2, busServices.size());
        
        EdinburghLiveBusService service = busServices.get(0);
        assertEquals("4", service.getServiceName());
        
        service = busServices.get(1);
        assertEquals("30", service.getServiceName());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object that misses out bus stops which have a null stop
     * code.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithNullStopCodeMultiple() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_null_stopcode_multiple.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        
        final EdinburghLiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        
        final List<EdinburghLiveBusService> busServices = busStop.getServices();
        assertEquals(2, busServices.size());
        
        EdinburghLiveBusService service = busServices.get(0);
        assertEquals("4", service.getServiceName());
        
        service = busServices.get(1);
        assertEquals("30", service.getServiceName());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object that misses out bus stops which have an empty stop
     * code.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithEmptyStopCodeMultiple() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_empty_stopcode_multiple.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        
        final EdinburghLiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        
        final List<EdinburghLiveBusService> busServices = busStop.getServices();
        assertEquals(2, busServices.size());
        
        EdinburghLiveBusService service = busServices.get(0);
        assertEquals("4", service.getServiceName());
        
        service = busServices.get(1);
        assertEquals("30", service.getServiceName());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object with no bus stops when the response only contains
     * a single, invalid service.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithInvalidServiceSingle() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_invalid_service_single.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertTrue(busTimes.isEmpty());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object that misses out bus services which are invalid.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithInvalidServiceMultiple() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_invalid_service_multiple.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        
        final EdinburghLiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        
        final List<EdinburghLiveBusService> busServices = busStop.getServices();
        assertEquals(2, busServices.size());
        
        EdinburghLiveBusService service = busServices.get(0);
        assertEquals("1", service.getServiceName());
        
        service = busServices.get(1);
        assertEquals("22", service.getServiceName());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object that has a null stop name when the response does
     * not contain a stop name.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithMissingStopName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_missing_stop_name.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        
        final EdinburghLiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertNull(busStop.getStopName());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object that has a null stop name when the response
     * contains a null stop name.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithNullStopName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_null_stop_name.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        
        final EdinburghLiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertNull(busStop.getStopName());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object that has an empty stop name when the response
     * contains an emtpy stop name.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithEmptyStopName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_empty_stop_name.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        
        final EdinburghLiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertEquals("", busStop.getStopName());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object when the bus stop disrupted flag is missing and it
     * sets this flag to false in this case.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithMissingStopDisrupted() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_empty_stop_name.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        
        final EdinburghLiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertFalse(busStop.isDisrupted());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object when the bus stop disrupted flag is null and it
     * sets this flag to false in this case.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithNullStopDisrupted() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_null_stop_name.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        
        final EdinburghLiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertFalse(busStop.isDisrupted());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object when the global disrupted flag is missing and it
     * sets this flag to false in this case.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithMissingGlobalDisrupted() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_missing_global_disrupted.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        assertFalse(busTimes.isGlobalDisruption());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object when the global disrupted flag is null and it sets
     * this flag to false in this case.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesWithNullGlobalDisrupted() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_null_global_disrupted.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        assertFalse(busTimes.isGlobalDisruption());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object with the correct services in the correct order
     * when the response is valid.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesValidWithSingleStop() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bustimes_valid.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        assertFalse(busTimes.isGlobalDisruption());
        
        final EdinburghLiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertFalse(busStop.isDisrupted());
        assertEquals("36237983", busStop.getStopCode());
        assertEquals("Royal Scot Acad", busStop.getStopName());
        
        final String[] expectedServices = new String[] {
            "1", "4", "22", "30", "34", "44", "44A", "N22", "N34", "N44"
        };
        
        final List<EdinburghLiveBusService> busServices = busStop.getServices();
        final int size = busServices.size();
        assertEquals(expectedServices.length, size);
        
        for (int i = 0; i < size; i++) {
            assertEquals(expectedServices[i],
                    busServices.get(i).getServiceName());
        }
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object with the correct services in the correct order
     * when the response is valid but with the services in the wrong order.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesValidWithSingleStopUnsortedServices()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_valid_unsorted_services.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(1, busTimes.size());
        assertFalse(busTimes.isGlobalDisruption());
        
        final EdinburghLiveBusStop busStop = busTimes.getBusStop("36237983");
        assertNotNull(busStop);
        assertFalse(busStop.isDisrupted());
        assertEquals("36237983", busStop.getStopCode());
        assertEquals("Royal Scot Acad", busStop.getStopName());
        
        final String[] expectedServices = new String[] {
            "1", "4", "22", "30", "34", "44", "44A", "N22", "N34", "N44"
        };
        
        final List<EdinburghLiveBusService> busServices = busStop.getServices();
        final int size = busServices.size();
        assertEquals(expectedServices.length, size);
        
        for (int i = 0; i < size; i++) {
            assertEquals(expectedServices[i],
                    busServices.get(i).getServiceName());
        }
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object with the correct services in the correct order
     * when the response is valid and contains services from multiple bus stops.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesValidWithMultipleStops() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_valid_multiple_stops.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(3, busTimes.size());
        assertFalse(busTimes.isGlobalDisruption());
        
        // Tests for 36237983
        final EdinburghLiveBusStop busStop1 = busTimes.getBusStop("36237983");
        assertNotNull(busStop1);
        assertFalse(busStop1.isDisrupted());
        assertEquals("36237983", busStop1.getStopCode());
        assertEquals("Royal Scot Acad", busStop1.getStopName());
        
        final String[] expectedServices1 = new String[] {
            "1", "4", "22", "30", "34", "44A", "N22", "N34", "N44"
        };
        
        final List<EdinburghLiveBusService> busServices1 =
                busStop1.getServices();
        final int size1 = busServices1.size();
        assertEquals(expectedServices1.length, size1);
        
        for (int i = 0; i < size1; i++) {
            assertEquals(expectedServices1[i],
                    busServices1.get(i).getServiceName());
        }
        
        // Tests for 36236464
        final EdinburghLiveBusStop busStop2 = busTimes.getBusStop("36236464");
        assertNotNull(busStop2);
        assertFalse(busStop2.isDisrupted());
        assertEquals("36236464", busStop2.getStopCode());
        assertEquals("Lauriston Terrac", busStop2.getStopName());
        
        final String[] expectedServices2 = new String[] {
            "23", "27", "35", "47"
        };
        
        final List<EdinburghLiveBusService> busServices2 =
                busStop2.getServices();
        final int size2 = busServices2.size();
        assertEquals(expectedServices2.length, size2);
        
        for (int i = 0; i < size2; i++) {
            assertEquals(expectedServices2[i],
                    busServices2.get(i).getServiceName());
        }
        
        // Tests for 36243526
        final EdinburghLiveBusStop busStop3 = busTimes.getBusStop("36243526");
        assertNotNull(busStop3);
        assertFalse(busStop3.isDisrupted());
        assertEquals("36243526", busStop3.getStopCode());
        assertEquals("Fountainpark", busStop3.getStopName());
        
        final String[] expectedServices3 = new String[] {
            "22", "30", "N22"
        };
        
        final List<EdinburghLiveBusService> busServices3 =
                busStop3.getServices();
        final int size3 = busServices3.size();
        assertEquals(expectedServices3.length, size3);
        
        for (int i = 0; i < size3; i++) {
            assertEquals(expectedServices3[i],
                    busServices3.get(i).getServiceName());
        }
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimes(org.json.JSONObject)}
     * returns a valid object with the correct services in the correct order
     * when the response is valid and contains services from multiple bus stops
     * but with the order mixed up.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesValidWithMultipleStopsUnsortedServices()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bustimes_valid_multiple_stops_unsorted_services.json");
        final EdinburghLiveBusTimes busTimes = EdinburghParser
                .parseBusTimes(reader.getJSONObject());
        
        assertNotNull(busTimes);
        assertEquals(3, busTimes.size());
        assertFalse(busTimes.isGlobalDisruption());
        
        // Tests for 36237983
        final EdinburghLiveBusStop busStop1 = busTimes.getBusStop("36237983");
        assertNotNull(busStop1);
        assertFalse(busStop1.isDisrupted());
        assertEquals("36237983", busStop1.getStopCode());
        assertEquals("Royal Scot Acad", busStop1.getStopName());
        
        final String[] expectedServices1 = new String[] {
            "1", "4", "22", "30", "34", "44A", "N22", "N34", "N44"
        };
        
        final List<EdinburghLiveBusService> busServices1 =
                busStop1.getServices();
        final int size1 = busServices1.size();
        assertEquals(expectedServices1.length, size1);
        
        for (int i = 0; i < size1; i++) {
            assertEquals(expectedServices1[i],
                    busServices1.get(i).getServiceName());
        }
        
        // Tests for 36236464
        final EdinburghLiveBusStop busStop2 = busTimes.getBusStop("36236464");
        assertNotNull(busStop2);
        assertFalse(busStop2.isDisrupted());
        assertEquals("36236464", busStop2.getStopCode());
        assertEquals("Lauriston Terrac", busStop2.getStopName());
        
        final String[] expectedServices2 = new String[] {
            "23", "27", "35", "47"
        };
        
        final List<EdinburghLiveBusService> busServices2 =
                busStop2.getServices();
        final int size2 = busServices2.size();
        assertEquals(expectedServices2.length, size2);
        
        for (int i = 0; i < size2; i++) {
            assertEquals(expectedServices2[i],
                    busServices2.get(i).getServiceName());
        }
        
        // Tests for 36243526
        final EdinburghLiveBusStop busStop3 = busTimes.getBusStop("36243526");
        assertNotNull(busStop3);
        assertFalse(busStop3.isDisrupted());
        assertEquals("36243526", busStop3.getStopCode());
        assertEquals("Fountainpark", busStop3.getStopName());
        
        final String[] expectedServices3 = new String[] {
            "22", "30", "N22"
        };
        
        final List<EdinburghLiveBusService> busServices3 =
                busStop3.getServices();
        final int size3 = busServices3.size();
        assertEquals(expectedServices3.length, size3);
        
        for (int i = 0; i < size3; i++) {
            assertEquals(expectedServices3[i],
                    busServices3.get(i).getServiceName());
        }
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns null when the {@link org.json.JSONObject} sent in is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithNullJsonObject()
            throws Exception {
        assertNull(EdinburghParser.parseBusTimesBusService(null));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns null when the {@link org.json.JSONObject} sent in is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithEmptyJsonObject()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/generic/empty_object.json");
        assertNull(EdinburghParser.parseBusTimesBusService(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns null when the service name is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithMissingServiceName()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_missing_service_name.json");
        assertNull(EdinburghParser.parseBusTimesBusService(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns null when the service name is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithEmptyServiceName()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_empty_service_name.json");
        assertNull(EdinburghParser.parseBusTimesBusService(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns null when the service name is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithNullServiceName()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_null_service_name.json");
        assertNull(EdinburghParser.parseBusTimesBusService(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid object and the service name is "TRAM" when the service
     * name is set to "50". This is because the tram is service 50 in the bus
     * tracker system.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithTramNameConversion50()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_tram_50.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertEquals("TRAM", service.getServiceName());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid object and the service name is "TRAM" when the service
     * name is set to "T50". This is because the tram is service 50 in the bus
     * tracker system, although it has been seen set to T50 too.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithTramNameConversionT50()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_tram_T50.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertEquals("TRAM", service.getServiceName());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid object when the operator field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithMissingOperator()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_missing_operator.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertNull(service.getOperator());
        assertEquals("1", service.getServiceName());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid object when the operator field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithEmptyOperator()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_empty_operator.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertEquals("", service.getOperator());
        assertEquals("1", service.getServiceName());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid object when the operator field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithNullOperator()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_null_operator.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertNull(service.getOperator());
        assertEquals("1", service.getServiceName());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid object when the route field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithMissingRoute()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_missing_route.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertNull(service.getRoute());
        assertEquals("1", service.getServiceName());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid object when the route field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithEmptyRoute()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_empty_route.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertEquals("", service.getRoute());
        assertEquals("1", service.getServiceName());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid object when the route field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithNullRoute()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_null_route.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertNull(service.getRoute());
        assertEquals("1", service.getServiceName());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid object when the disruption field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithMissingDisruption()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_missing_disruption.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertFalse(service.isDisrupted());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid object when the disruption field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithNullDisruption()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_null_disruption.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertFalse(service.isDisrupted());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid object when the diversion field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithMissingDiversion()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_missing_diversion.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertFalse(service.isDiverted());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid object when the diversion field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithNullDiversion()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_null_diversion.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertFalse(service.isDiverted());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns null when the bus array is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithMissingBusArray()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_missing_bus_array.json");
        assertNull(EdinburghParser.parseBusTimesBusService(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns null when the bus array is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithNullBusArray()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_null_bus_array.json");
        assertNull(EdinburghParser.parseBusTimesBusService(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns null when the bus array is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithEmptyBusArray()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_empty_bus_array.json");
        assertNull(EdinburghParser.parseBusTimesBusService(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns null when the bus array contains a single invalid bus.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithInvalidBusSingle()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_invalid_bus_single.json");
        assertNull(EdinburghParser.parseBusTimesBusService(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid object when a single bus could not be parsed in the bus
     * array, but there were other buses that could be parsed in the array.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithInvalidBusMultiple()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_invalid_bus_multiple.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertEquals("1", service.getServiceName());
        assertEquals("LB", service.getOperator());
        
        final List<EdinburghLiveBus> buses = service.getLiveBuses();
        assertEquals(1, buses.size());
        
        final EdinburghLiveBus bus = buses.get(0);
        assertEquals(47, bus.getDepartureMinutes());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid service when there is a single bus and the fields are as
     * expected.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithSingleBus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_single_bus.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertEquals("1", service.getServiceName());
        assertEquals("LB", service.getOperator());
        
        final List<EdinburghLiveBus> buses = service.getLiveBuses();
        assertEquals(1, buses.size());
        
        final EdinburghLiveBus bus = buses.get(0);
        assertEquals("Clermiston", bus.getDestination());
        assertEquals(17, bus.getDepartureMinutes());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid service with the buses in the correct order when there
     * are multiple buses in an incorrect order.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceWithMultipleBusUnsorted()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "service_multiple_bus_unsorted.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertEquals("1", service.getServiceName());
        assertEquals("LB", service.getOperator());
        
        final List<EdinburghLiveBus> buses = service.getLiveBuses();
        assertEquals(4, buses.size());
        
        EdinburghLiveBus bus = buses.get(0);
        assertEquals(17, bus.getDepartureMinutes());
        
        bus = buses.get(1);
        assertEquals(47, bus.getDepartureMinutes());
        
        bus = buses.get(2);
        assertEquals(77, bus.getDepartureMinutes());
        
        bus = buses.get(3);
        assertEquals(107, bus.getDepartureMinutes());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseBusTimesBusService(org.json.JSONObject)}
     * returns a valid service when the service object is fully compliant.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusServiceValid() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/service_valid.json");
        final EdinburghLiveBusService service = EdinburghParser
                .parseBusTimesBusService(reader.getJSONObject());
        
        assertNotNull(service);
        assertEquals("1", service.getServiceName());
        assertEquals("LB", service.getOperator());
        assertEquals("Easter Road -- Clermiston", service.getRoute());
        assertFalse(service.isDisrupted());
        assertFalse(service.isDiverted());
        
        final List<EdinburghLiveBus> buses = service.getLiveBuses();
        assertEquals(2, buses.size());
        
        EdinburghLiveBus bus = buses.get(0);
        assertEquals(17, bus.getDepartureMinutes());
        
        bus = buses.get(1);
        assertEquals(47, bus.getDepartureMinutes());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns null when the {@link org.json.JSONObject} sent in is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithNullJsonObject() throws Exception {
        assertNull(EdinburghParser.parseBusTimesBus(null));
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns null when the {@link org.json.JSONObject} sent in is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseBusTimesBusWithEmptyJsonObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/generic/empty_object.json");
        assertNull(EdinburghParser.parseBusTimesBus(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns null when the destination is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithMissingDestination() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bus_missing_destination.json");
        assertNull(EdinburghParser.parseBusTimesBus(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns null when the destination is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithNullDestination() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_null_destination.json");
        assertNull(EdinburghParser.parseBusTimesBus(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns null when the destination is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithEmptyDestination() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_empty_destination.json");
        assertNull(EdinburghParser.parseBusTimesBus(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns null when the minutes field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithMissingMinutes() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_missing_minutes.json");
        assertNull(EdinburghParser.parseBusTimesBus(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns null when the minutes field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithNullMinutes() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_null_minutes.json");
        assertNull(EdinburghParser.parseBusTimesBus(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns null when the reliability field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithMissingReliability() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/"
                + "bus_missing_reliability.json");
        assertNull(EdinburghParser.parseBusTimesBus(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns null when the reliability field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithNullReliability() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_null_reliability.json");
        assertNull(EdinburghParser.parseBusTimesBus(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns null when the reliability field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithEmptyReliability() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_empty_reliability.json");
        assertNull(EdinburghParser.parseBusTimesBus(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns null when the type field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithMissingType() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_missing_type.json");
        assertNull(EdinburghParser.parseBusTimesBus(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns null when the type field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithNullType() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_null_type.json");
        assertNull(EdinburghParser.parseBusTimesBus(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns null when the type field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithEmptyType() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_empty_type.json");
        assertNull(EdinburghParser.parseBusTimesBus(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns a valid object when the terminus field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithMissingTerminus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_missing_terminus.json");
        final EdinburghLiveBus bus = EdinburghParser
                .parseBusTimesBus(reader.getJSONObject());
        
        assertNotNull(bus);
        assertNull(bus.getTerminus());
        assertEquals("Clermiston", bus.getDestination());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns a valid object when the terminus field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithNullTerminus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_null_terminus.json");
        final EdinburghLiveBus bus = EdinburghParser
                .parseBusTimesBus(reader.getJSONObject());
        
        assertNotNull(bus);
        assertNull(bus.getTerminus());
        assertEquals("Clermiston", bus.getDestination());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns a valid object when the terminus field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithEmptyTerminus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_empty_terminus.json");
        final EdinburghLiveBus bus = EdinburghParser
                .parseBusTimesBus(reader.getJSONObject());
        
        assertNotNull(bus);
        assertEquals("", bus.getTerminus());
        assertEquals("Clermiston", bus.getDestination());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns a valid object when the journeyId field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithMissingJourneyId() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_missing_journey_id.json");
        final EdinburghLiveBus bus = EdinburghParser
                .parseBusTimesBus(reader.getJSONObject());
        
        assertNotNull(bus);
        assertNull(bus.getJourneyId());
        assertEquals("Clermiston", bus.getDestination());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns a valid object when the journeyId field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithNullJourneyId() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_null_journey_id.json");
        final EdinburghLiveBus bus = EdinburghParser
                .parseBusTimesBus(reader.getJSONObject());
        
        assertNotNull(bus);
        assertNull(bus.getJourneyId());
        assertEquals("Clermiston", bus.getDestination());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns a valid object when the journeyId field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusWithEmptyJourneyId() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_empty_journey_id.json");
        final EdinburghLiveBus bus = EdinburghParser
                .parseBusTimesBus(reader.getJSONObject());
        
        assertNotNull(bus);
        assertEquals("", bus.getJourneyId());
        assertEquals("Clermiston", bus.getDestination());
    }
    
    /**
     * Test that {@link EdinburghParser#parseBusTimesBus(org.json.JSONObject)}
     * returns a valid object with its fields correctly populated when the bus
     * object in JSON is valid.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseBusTimesBusValid() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getBusTimes/bus_valid.json");
        final EdinburghLiveBus bus = EdinburghParser
                .parseBusTimesBus(reader.getJSONObject());
        
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
     * Test that {@link EdinburghParser#parseJourneyTimes(org.json.JSONObject)}
     * correctly throws a {@link LiveTimesException} when the
     * {@link org.json.JSONObject} is set as null.
     */
    public void testParseJourneyTimesWithNullJsonObject() {
        try {
            EdinburghParser.parseJourneyTimes(null);
        } catch (LiveTimesException e) {
            return;
        }
        
        fail("The JSONObject is set as null, so a LiveTimesException should be "
                + "thrown.");
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourneyTimes(org.json.JSONObject)}
     * correctly throws an {@link AuthenticationException} when the data is set
     * as an error for an invalid API key.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyTimesWithError() throws Exception {
        try {
            final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_invalid_app_key.json");
            EdinburghParser.parseJourneyTimes(reader.getJSONObject());
        } catch (AuthenticationException e) {
            return;
        }
        
        fail("The data is set as an error response for an invalid API key. An "
                + "AuthenticationException should be thrown.");
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourneyTimes(org.json.JSONObject)}
     * correctly throws a {@link LiveTimesException} containing a
     * {@link JSONException} when the journey times are missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyTimesWithMissingJourneyTimes()
            throws Exception {
        try {
            final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/generic/empty_object.json");
            EdinburghParser.parseJourneyTimes(reader.getJSONObject());
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }
        
        fail("The data is set as an empty JSON object, so a LiveTimesException "
                + "should be thrown with a JSONException set as its cause.");
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourneyTimes(org.json.JSONObject)}
     * correctly throws a {@link LiveTimesException} containing a
     * {@link JSONException} when the journey times is set to null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyTimesWithNullJourneyTimes() throws Exception {
        try {
            final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                    + "journeytimes_null_journey_times.json");
            EdinburghParser.parseJourneyTimes(reader.getJSONObject());
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }
        
        fail("The journey times is set as null, so a LiveTimesException "
                + "containing a JSONException as the cause should be thrown.");
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourneyTimes(org.json.JSONObject)}
     * correctly throws a {@link LiveTimesException} when the journey times is
     * set as an empty array.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyTimesWithEmptyJourneyTimes() throws Exception {
        try {
            final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                    + "journeytimes_empty_journey_times.json");
            EdinburghParser.parseJourneyTimes(reader.getJSONObject());
        } catch (LiveTimesException e) {
            return;
        }
        
        fail("The journey times is set as an empty array, so a "
                + "LiveTimesException should be thrown.");
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourneyTimes(org.json.JSONObject)}
     * correctly throws a {@link LiveTimesException} when the journey could not
     * be parsed.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyTimesWithInvalidJourney() throws Exception {
        try {
            final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                    + "journeytimes_invalid_journey.json");
            EdinburghParser.parseJourneyTimes(reader.getJSONObject());
        } catch (LiveTimesException e) {
            return;
        }
        
        fail("The journey times has an invalid journey, so LiveTimesException "
                + "should be thrown.");
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourneyTimes(org.json.JSONObject)}
     * returns a valid object when the journey response is valid.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyTimesWithValidJourney() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                    + "journeytimes_valid.json");
        final EdinburghJourney journey = EdinburghParser
                .parseJourneyTimes(reader.getJSONObject());
        
        assertEquals("3322", journey.getJourneyId());
        assertEquals("LB", journey.getOperator());
        assertEquals("22", journey.getServiceName());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourneyTimes(org.json.JSONObject)}
     * only selects the first journey in the array, as the API should only
     * return a single journey. There is currently no multi-journey support.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyTimesWithMultipleJournies() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                    + "journeytimes_valid_multiple.json");
        final EdinburghJourney journey = EdinburghParser
                .parseJourneyTimes(reader.getJSONObject());
        
        assertEquals("3322", journey.getJourneyId());
        assertEquals("LB", journey.getOperator());
        assertEquals("22", journey.getServiceName());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the {@link org.json.JSONObject} sent in is null.
     */
    public void testParseJourneyWithNullJsonObject() {
        assertNull(EdinburghParser.parseJourney(null));
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the {@link org.json.JSONObject} sent in is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyWithEmptyJsonObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/generic/empty_object.json");
        assertNull(EdinburghParser.parseJourney(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the journeyId field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyWithMissingJourneyId() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_missing_journey_id.json");
        assertNull(EdinburghParser.parseJourney(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the journeyId field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyWithNullJourneyId() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_null_journey_id.json");
        assertNull(EdinburghParser.parseJourney(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the journeyId field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyWithEmptyJourneyId() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_empty_journey_id.json");
        assertNull(EdinburghParser.parseJourney(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the service name field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyWithMissingServiceName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_missing_service_name.json");
        assertNull(EdinburghParser.parseJourney(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the service name field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyWithNullServiceName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_null_service_name.json");
        assertNull(EdinburghParser.parseJourney(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the service name field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyWithEmptyServiceName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_empty_service_name.json");
        assertNull(EdinburghParser.parseJourney(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the terminus field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyWithMissingTerminus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_missing_terminus.json");
        assertNull(EdinburghParser.parseJourney(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the terminus field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithNullTerminus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_null_terminus.json");
        assertNull(EdinburghParser.parseJourney(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the terminus field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithEmptyTerminus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_empty_terminus.json");
        assertNull(EdinburghParser.parseJourney(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the operator field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithMissingOperator() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_missing_operator.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertNull(journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the operator field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithNullOperator() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_null_operator.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertNull(journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the operator field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithEmptyOperator() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_empty_operator.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the route field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithMissingRoute() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_missing_route.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertNull(journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the route field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithNullRoute() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_null_route.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertNull(journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the route field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithEmptyRoute() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_empty_route.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the destination field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithMissingDestination() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_missing_destination.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertNull(journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the destination field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithNullDestination() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_null_destination.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertNull(journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the destination field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithEmptyDestination() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_empty_destination.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the global disruption field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithMissingGlobalDisruption() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_missing_global_disruption.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the global disruption field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithNullGlobalDisruption() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_null_global_disruption.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the service disruption field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithMissingServiceDisruption()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_missing_service_disruption.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the service disruption field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithNullServiceDisruption() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_null_service_disruption.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the service diversion field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithMissingServiceDiversion() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_missing_service_diversion.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object when the service diversion field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithNullServiceDiversion() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_null_service_diversion.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the departures array is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithMissingDepartures() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_missing_departures.json");
        assertNull(EdinburghParser.parseJourney(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the departures array is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithNullDepartures() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_null_departures.json");
        assertNull(EdinburghParser.parseJourney(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns null when the departures array is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithEmptyDepartures() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_empty_departures.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertTrue(departures.isEmpty());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object but with 0 departures when there are no valid
     * departure objects in the list.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithAllDeparturesInvalid() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_all_invalid_departures.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertTrue(departures.isEmpty());
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object with a list of valid departures, with the invalid
     * departures discarded.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithSomeDeparturesInvalid() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_some_invalid_departures.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        
        final int size = departures.size();
        assertEquals(3, size);
        
        final String[] expected = new String[] {
            "36237983", "36232545", "36236754"
        };
        
        for (int i = 0; i < size; i++) {
            assertEquals(expected[i], departures.get(i).getStopCode());
        }
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object and all the departures are in the correct order
     * when the departures list is unsorted.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyWithUnsortedDepartures() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_unsorted_services.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        
        final int size = departures.size();
        assertEquals(9, size);
        
        final String[] expected = new String[] {
            "36237983", "36232545", "36232547", "36236754", "36253269",
            "36243498", "36248598", "36245456", "36236542"
        };
        
        for (int i = 0; i < size; i++) {
            assertEquals(expected[i], departures.get(i).getStopCode());
        }
    }
    
    /**
     * Test that {@link EdinburghParser#parseJourney(org.json.JSONObject)}
     * returns a valid object with the correct number of departures.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyValid() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "journey_valid.json");
        final EdinburghJourney journey = EdinburghParser.parseJourney(
                reader.getJSONObject());
        
        assertNotNull(journey);
        assertEquals("3084", journey.getJourneyId());
        assertEquals("22", journey.getServiceName());
        assertEquals("LB", journey.getOperator());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<EdinburghJourneyDeparture> departures = journey
                .getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
        
        try {
            departures.remove(0);
        } catch (UnsupportedOperationException e) {
            return;
        }
        
        fail("The departures list should not be modifiable.");
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns null when the {@link org.json.JSONObject} sent in is null.
     */
    public void testParseJourneyDepartureWithNullJsonObject() {
        assertNull(EdinburghParser.parseJourneyDeparture(null));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns null when the {@link org.json.JSONObject} sent in is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyDepartureWithEmptyJsonObject()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/generic/empty_object.json");
        assertNull(EdinburghParser.parseJourneyDeparture(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns null when the stopCode field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyDepartureWithMissingStopCode()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_missing_stopcode.json");
        assertNull(EdinburghParser.parseJourneyDeparture(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns null when the stopCode field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyDepartureWithNullStopCode() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_null_stopcode.json");
        assertNull(EdinburghParser.parseJourneyDeparture(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns null when the stopCode field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them. 
     */
    public void testParseJourneyDepartureWithEmptyStopCode() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_empty_stopcode.json");
        assertNull(EdinburghParser.parseJourneyDeparture(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns a valid object when the stop name field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureWithMissingStopName()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_missing_stop_name.json");
        final EdinburghJourneyDeparture departure =
                EdinburghParser.parseJourneyDeparture(reader.getJSONObject());
        
        assertNotNull(departure);
        assertNull(departure.getStopName());
        assertEquals("36236754", departure.getStopCode());
        assertEquals(4, departure.getDepartureMinutes());
        assertFalse(departure.isEstimatedTime());
        assertFalse(departure.isDelayed());
        assertFalse(departure.isDiverted());
        assertFalse(departure.isTerminus());
        assertFalse(departure.isPartRoute());
        assertFalse(departure.isBusStopDisrupted());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns a valid object when the stop name field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureWithNullStopName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_null_stop_name.json");
        final EdinburghJourneyDeparture departure =
                EdinburghParser.parseJourneyDeparture(reader.getJSONObject());
        
        assertNotNull(departure);
        assertNull(departure.getStopName());
        assertEquals("36236754", departure.getStopCode());
        assertEquals(4, departure.getDepartureMinutes());
        assertFalse(departure.isEstimatedTime());
        assertFalse(departure.isDelayed());
        assertFalse(departure.isDiverted());
        assertFalse(departure.isTerminus());
        assertFalse(departure.isPartRoute());
        assertFalse(departure.isBusStopDisrupted());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns a valid object when the stop name field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureWithEmptyStopName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_empty_stop_name.json");
        final EdinburghJourneyDeparture departure =
                EdinburghParser.parseJourneyDeparture(reader.getJSONObject());
        
        assertNotNull(departure);
        assertEquals("", departure.getStopName());
        assertEquals("36236754", departure.getStopCode());
        assertEquals(4, departure.getDepartureMinutes());
        assertFalse(departure.isEstimatedTime());
        assertFalse(departure.isDelayed());
        assertFalse(departure.isDiverted());
        assertFalse(departure.isTerminus());
        assertFalse(departure.isPartRoute());
        assertFalse(departure.isBusStopDisrupted());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns null when the minutes field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureWithMissingMinutes() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_missing_minutes.json");
        assertNull(EdinburghParser.parseJourneyDeparture(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns null when the minutes field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureWithNullMinutes() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_null_minutes.json");
        assertNull(EdinburghParser.parseJourneyDeparture(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns a valid object when the disrupted field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureWithMissingDisrupted()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_missing_disrupted.json");
        final EdinburghJourneyDeparture departure =
                EdinburghParser.parseJourneyDeparture(reader.getJSONObject());
        
        assertNotNull(departure);
        assertEquals("The Exchange", departure.getStopName());
        assertEquals("36236754", departure.getStopCode());
        assertEquals(4, departure.getDepartureMinutes());
        assertFalse(departure.isEstimatedTime());
        assertFalse(departure.isDelayed());
        assertFalse(departure.isDiverted());
        assertFalse(departure.isTerminus());
        assertFalse(departure.isPartRoute());
        assertFalse(departure.isBusStopDisrupted());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns a valid object when the disrupted field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureWithNullDisrupted() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_null_disrupted.json");
        final EdinburghJourneyDeparture departure =
                EdinburghParser.parseJourneyDeparture(reader.getJSONObject());
        
        assertNotNull(departure);
        assertEquals("The Exchange", departure.getStopName());
        assertEquals("36236754", departure.getStopCode());
        assertEquals(4, departure.getDepartureMinutes());
        assertFalse(departure.isEstimatedTime());
        assertFalse(departure.isDelayed());
        assertFalse(departure.isDiverted());
        assertFalse(departure.isTerminus());
        assertFalse(departure.isPartRoute());
        assertFalse(departure.isBusStopDisrupted());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns null when the reliability field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureWithMissingReliability()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_missing_reliability.json");
        assertNull(EdinburghParser.parseJourneyDeparture(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns null when the reliability field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureWithNullReliability()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_null_reliability.json");
        assertNull(EdinburghParser.parseJourneyDeparture(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns null when the reliability field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureWithEmptyReliability()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_empty_reliability.json");
        assertNull(EdinburghParser.parseJourneyDeparture(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns null when the type field is missing.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureWithMissingType() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_missing_type.json");
        assertNull(EdinburghParser.parseJourneyDeparture(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns null when the type field is null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureWithNullType() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_null_type.json");
        assertNull(EdinburghParser.parseJourneyDeparture(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns null when the type field is empty.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureWithEmptyType() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_empty_type.json");
        assertNull(EdinburghParser.parseJourneyDeparture(
                reader.getJSONObject()));
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns a valid object when the data is valid, for a departure in the
     * future.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureForFutureDeparture() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_valid_future_departure.json");
        final EdinburghJourneyDeparture departure =
                EdinburghParser.parseJourneyDeparture(reader.getJSONObject());
        
        assertNotNull(departure);
        assertEquals("The Exchange", departure.getStopName());
        assertEquals("36236754", departure.getStopCode());
        assertEquals(4, departure.getDepartureMinutes());
        assertFalse(departure.isEstimatedTime());
        assertFalse(departure.isDelayed());
        assertFalse(departure.isDiverted());
        assertFalse(departure.isTerminus());
        assertFalse(departure.isPartRoute());
        assertFalse(departure.isBusStopDisrupted());
    }
    
    /**
     * Test that
     * {@link EdinburghParser#parseJourneyDeparture(org.json.JSONObject)}
     * returns a valid object when the data is valid, for a departure in the
     * past.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testParseJourneyDepartureForPastDeparture() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                + "departure_valid_past_departure.json");
        final EdinburghJourneyDeparture departure =
                EdinburghParser.parseJourneyDeparture(reader.getJSONObject());
        
        assertNotNull(departure);
        assertEquals("The Exchange", departure.getStopName());
        assertEquals("36236754", departure.getStopCode());
        assertTrue(departure.getDepartureMinutes() < 0);
        assertFalse(departure.isEstimatedTime());
        assertFalse(departure.isDelayed());
        assertFalse(departure.isDiverted());
        assertFalse(departure.isTerminus());
        assertFalse(departure.isPartRoute());
        assertFalse(departure.isBusStopDisrupted());
    }
    
    /**
     * Test that {@link EdinburghParser#parseError(org.json.JSONObject)} returns
     * a {@link LiveTimesException} when the JSON object is set to null.
     */
    public void testParseErrorWithNullJsonObject() {
        final LiveTimesException exception = EdinburghParser.parseError(null);
        assertNotNull(exception);
    }
    
    /**
     * Test that {@link EdinburghParser#parseError(org.json.JSONObject)} returns
     * a {@link LiveTimesException} when the JSON object is set as an empty
     * object.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseErrorWithEmptyJsonObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/generic/empty_object.json");
        final LiveTimesException exception = EdinburghParser
                .parseError(reader.getJSONObject());
        assertNotNull(exception);
    }
    
    /**
     * Test that {@link EdinburghParser#parseError(org.json.JSONObject)} returns
     * a {@link LiveTimesException} when the JSON object is set as an empty
     * object.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseErrorWithUnknownError() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_not_known.json");
        final LiveTimesException exception = EdinburghParser
                .parseError(reader.getJSONObject());
        assertNotNull(exception);
    }
    
    /**
     * Test that {@link EdinburghParser#parseError(org.json.JSONObject)} returns
     * a {@link AuthenticationException} when the error denotes an invalid API
     * key.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseErrorAppKey() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_invalid_app_key.json");
        final LiveTimesException exception = EdinburghParser
                .parseError(reader.getJSONObject());
        assertNotNull(exception);
        assertTrue(exception instanceof AuthenticationException);
    }
    
    /**
     * Test that {@link EdinburghParser#parseError(org.json.JSONObject)} returns
     * a {@link ServerErrorException} when the error denotes an invalid
     * parameter.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseErrorInvalidParameter() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_invalid_parameter.json");
        final LiveTimesException exception = EdinburghParser
                .parseError(reader.getJSONObject());
        assertNotNull(exception);
        assertTrue(exception instanceof ServerErrorException);
    }
    
    /**
     * Test that {@link EdinburghParser#parseError(org.json.JSONObject)} returns
     * a {@link ServerErrorException} when the error denotes a processing error.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseErrorProcessingError() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_processing_error.json");
        final LiveTimesException exception = EdinburghParser
                .parseError(reader.getJSONObject());
        assertNotNull(exception);
        assertTrue(exception instanceof ServerErrorException);
    }
    
    /**
     * Test that {@link EdinburghParser#parseError(org.json.JSONObject)} returns
     * a {@link MaintenanceException} when the error denotes that the system is
     * down for maintenance.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseErrorSystemMaintenance() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_system_maintenance.json");
        final LiveTimesException exception = EdinburghParser
                .parseError(reader.getJSONObject());
        assertNotNull(exception);
        assertTrue(exception instanceof MaintenanceException);
    }
    
    /**
     * Test that {@link EdinburghParser#parseError(org.json.JSONObject)} returns
     * a {@link SystemOverloadedException} when the error denotes the system is
     * overloaded.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseErrorSystemOverloaded() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_system_overloaded.json");
        final LiveTimesException exception = EdinburghParser
                .parseError(reader.getJSONObject());
        assertNotNull(exception);
        assertTrue(exception instanceof SystemOverloadedException);
    }
    
    /**
     * Get the data from assets.
     * 
     * @param filePath The file path in the assets to get data from.
     * @return The data.
     * @throws IOException When there was a problem reading the data.
     */
    private JSONFetcherStreamReader getReaderAfterFetchingData(
            final String filePath) throws IOException{
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        filePath);
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        fetcher.executeFetcher(reader);
        
        return reader;
    }
}