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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.fail;

import androidx.test.InstrumentationRegistry;
import java.io.IOException;
import java.util.List;
import org.json.JSONException;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.org.rivernile.android.bustracker.parser.livetimes.Journey;
import uk.org.rivernile.android.bustracker.parser.livetimes.JourneyDeparture;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusStop;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusTimes;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.bustracker.parser.livetimes.ServerErrorException;
import uk.org.rivernile.android.fetchutils.fetchers.AssetFileFetcher;
import uk.org.rivernile.android.fetchutils.fetchers.Fetcher;

/**
 * Tests for {@link EdinburghParser}.
 * 
 * @author Niall Scott
 */
public class EdinburghParserTests {
    
    private EdinburghParser parser;

    @Before
    public void setUp() {
        parser = new EdinburghParser();
    }

    @After
    public void tearDown() {
        parser = null;
    }
    
    /**
     * Test that {@link EdinburghParser#getBusTimes(Fetcher)} correctly throws a
     * {@link LiveTimesException} containing an {@link IOException} when the fetcher is set to
     * fetch a resource that does not exist.
     * 
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testGetBusTimesWithInvalidSource() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/generic/does_not_exist.json");
        
        try {
            parser.getBusTimes(fetcher);
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof IOException) {
                return;
            }
        }
        
        fail("The fetcher is set to an incorrect resource, so an IOException should be set as " +
                "the cause in the LiveTimesException.");
    }
    
    /**
     * Test that {@link EdinburghParser#getBusTimes(Fetcher)} correctly throws a
     * {@link LiveTimesException} containing a {@link JSONException} when the fetcher is set to
     * fetch a resource that contains invalid data.
     * 
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testGetBusTimesWithInvalidJson() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/generic/invalid.json");
        
        try {
            parser.getBusTimes(fetcher);
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }
        
        fail("The fetcher is set to return invalid JSON, so a JSONException should be set as the " +
                "cause in the LiveTimesException.");
    }
    
    /**
     * Test that {@link EdinburghParser#getBusTimes(Fetcher)} correctly throws a
     * {@link LiveTimesException} containing a {@link JSONException} when the fetcher returns a
     * JSON array rather than a JSON object.
     * 
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testGetBusTimesWithJsonArray() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/generic/empty_array.json");
        
        try {
            parser.getBusTimes(fetcher);
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }
        
        fail("The fetcher is set to get a JSON array, but a JSON objecy is expected. A " +
                "JSONException should be set as the cause in the LiveTimesException.");
    }
    
    /**
     * Test that {@link EdinburghParser#getBusTimes(Fetcher)} correctly throws a
     * {@link ServerErrorException} when the fetcher returns an error response.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    @Test(expected = ServerErrorException.class)
    public void testGetBusTimesWithError() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/bustracker/generic/error_processing_error.json");
        parser.getBusTimes(fetcher);
    }
    
    /**
     * Test that {@link EdinburghParser#getBusTimes(Fetcher)} returns a valid object when it is
     * supplied valid data.
     * 
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testGetBusTimesValid() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/bustracker/getBusTimes/bustimes_valid.json");
        final LiveBusTimes busTimes = parser.getBusTimes(fetcher);
        
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
     * Test that {@link EdinburghParser#getJourneyTimes(Fetcher)} correctly throws a
     * {@link LiveTimesException} containing an {@link IOException} when the fetcher is set to
     * fetch a resource that does not exist.
     * 
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testGetJourneyTimesWithInvalidSource() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/generic/does_not_exist.json");
        
        try {
            parser.getJourneyTimes(fetcher);
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof IOException) {
                return;
            }
        }
        
        fail("The fetcher is set to an incorrect resource, so an IOException should be set as " +
                "the cause in the LiveTimesException.");
    }
    
    /**
     * Test that {@link EdinburghParser#getJourneyTimes(Fetcher)} correctly throws a
     * {@link LiveTimesException} containing a {@link JSONException} when the fetcher is set to
     * fetch a resource that contains invalid data.
     * 
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testGetJourneyTimesWithInvalidJson() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/generic/invalid.json");
        
        try {
            parser.getJourneyTimes(fetcher);
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }
        
        fail("The fetcher is set to return invalid JSON, so a JSONException should be set as the " +
                "cause in the LiveTimesException.");
    }
    
    /**
     * Test that {@link EdinburghParser#getJourneyTimes(Fetcher)} correctly throws a
     * {@link LiveTimesException} containing a {@link JSONException} when the fetcher returns a
     * JSON array rather than a JSON object.
     * 
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testGetJourneyTimesWithJsonArray() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/generic/empty_array.json");
        
        try {
            parser.getJourneyTimes(fetcher);
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }
        
        fail("The fetcher is set to get a JSON array, but a JSON objecy is expected. A " +
                "JSONException should be set as the cause in the LiveTimesException.");
    }
    
    /**
     * Test that {@link EdinburghParser#getJourneyTimes(Fetcher)} correctly throws a
     * {@link ServerErrorException} when the fetcher returns an error response.
     * 
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let test fail.
     */
    @Test(expected = ServerErrorException.class)
    public void testGetJourneyTimesWithError() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/bustracker/generic/error_processing_error.json");
        parser.getJourneyTimes(fetcher);
    }
    
    /**
     * Test that {@link EdinburghParser#getJourneyTimes(Fetcher)} returns a valid object when it
     * is supplied valid data.
     * 
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testGetJourneyTimesValid() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/bustracker/getJourneyTimes/journeytimes_valid.json");
        final Journey journey = parser.getJourneyTimes(fetcher);
        
        assertEquals("3322", journey.getJourneyId());
        assertEquals("LB", journey.getOperator());
        assertEquals("22", journey.getServiceName());
        assertEquals("Gyle -- Ocean Terminal", journey.getRoute());
        assertEquals("Gyle Centre", journey.getDestination());
        assertEquals("36242462", journey.getTerminus());
        assertFalse(journey.hasGlobalDisruption());
        assertFalse(journey.hasServiceDisruption());
        assertFalse(journey.hasServiceDiversion());
        
        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }
}