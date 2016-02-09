/*
 * Copyright (C) 2014 - 2016 Niall 'Rivernile' Scott
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
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.json.JSONException;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;
import java.util.List;

import uk.org.rivernile.android.bustracker.parser.livetimes.AuthenticationException;
import uk.org.rivernile.android.bustracker.parser.livetimes.Journey;
import uk.org.rivernile.android.bustracker.parser.livetimes.JourneyDeparture;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.fetchutils.fetchers.AssetFileFetcher;
import uk.org.rivernile.android.fetchutils.fetchers.readers.JSONFetcherStreamReader;

/**
 * Tests for {@link JourneyTimesParser}.
 *
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class JourneyTimesParserTests {

    /**
     * Test that {@link JourneyTimesParser#parseJourneyTimes(org.json.JSONObject)} correctly throws
     * an {@link AuthenticationException} when the data is set as an error for an invalid API key.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test(expected = AuthenticationException.class)
    public void testParseJourneyTimesWithError() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_invalid_app_key.json");
        JourneyTimesParser.parseJourneyTimes(reader.getJSONObject());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyTimes(org.json.JSONObject)} correctly throws
     * a {@link LiveTimesException} containing a {@link JSONException} when the journey times are
     * missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyTimesWithMissingJourneyTimes() throws Exception {
        try {
            final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                    "endpoints/generic/empty_object.json");
            JourneyTimesParser.parseJourneyTimes(reader.getJSONObject());
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }

        fail("The data is set as an empty JSON object, so a LiveTimesException should be thrown " +
                "with a JSONException set as its cause.");
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyTimes(org.json.JSONObject)} correctly throws
     * a {@link LiveTimesException} containing a {@link JSONException} when the journey times is
     * set to {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyTimesWithNullJourneyTimes() throws Exception {
        try {
            final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                    "endpoints/bustracker/getJourneyTimes/journeytimes_null_journey_times.json");
            JourneyTimesParser.parseJourneyTimes(reader.getJSONObject());
        } catch (LiveTimesException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }

        fail("The journey times is set as null, so a LiveTimesException containing a " +
                "JSONException as the cause should be thrown.");
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyTimes(org.json.JSONObject)} correctly throws
     * a {@link LiveTimesException} when the journey times is set as an empty array.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test(expected = LiveTimesException.class)
    public void testParseJourneyTimesWithEmptyJourneyTimes() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                        + "journeytimes_empty_journey_times.json");
        JourneyTimesParser.parseJourneyTimes(reader.getJSONObject());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyTimes(org.json.JSONObject)} correctly throws
     * a {@link LiveTimesException} when the journey could not be parsed.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test(expected = LiveTimesException.class)
    public void testParseJourneyTimesWithInvalidJourney() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/"
                        + "journeytimes_invalid_journey.json");
        JourneyTimesParser.parseJourneyTimes(reader.getJSONObject());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyTimes(org.json.JSONObject)} returns a valid
     * object when the journey response is valid.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyTimesWithValidJourney() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journeytimes_valid.json");
        final Journey journey = JourneyTimesParser.parseJourneyTimes(reader.getJSONObject());

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

    /**
     * Test that {@link JourneyTimesParser#parseJourneyTimes(org.json.JSONObject)} only selects the
     * first journey in the array, as the API should only return a single journey. There is
     * currently no multi-journey support.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyTimesWithMultipleJournies() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journeytimes_valid_multiple.json");
        final Journey journey = JourneyTimesParser.parseJourneyTimes(reader.getJSONObject());

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

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns {@code null}
     * when the {@link org.json.JSONObject} sent in is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithEmptyJsonObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/generic/empty_object.json");
        assertNull(JourneyTimesParser.parseJourney(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns {@code null}
     * when the journeyId field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithMissingJourneyId() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_missing_journey_id.json");
        assertNull(JourneyTimesParser.parseJourney(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns {@code null}
     * when the journeyId field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithNullJourneyId() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_null_journey_id.json");
        assertNull(JourneyTimesParser.parseJourney(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns {@code null}
     * when the journeyId field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithEmptyJourneyId() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_empty_journey_id.json");
        assertNull(JourneyTimesParser.parseJourney(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns {@code null}
     * when the service name field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseJourneyWithMissingServiceName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_missing_service_name.json");
        assertNull(JourneyTimesParser.parseJourney(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns {@code null}
     * when the service name field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithNullServiceName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_null_service_name.json");
        assertNull(JourneyTimesParser.parseJourney(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns {@code null}
     * when the service name field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithEmptyServiceName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_empty_service_name.json");
        assertNull(JourneyTimesParser.parseJourney(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns {@code null}
     * when the terminus field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithMissingTerminus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_missing_terminus.json");
        assertNull(JourneyTimesParser.parseJourney(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns {@code null}
     * when the terminus field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithNullTerminus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_null_terminus.json");
        assertNull(JourneyTimesParser.parseJourney(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns {@code null}
     * when the terminus field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithEmptyTerminus() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_empty_terminus.json");
        assertNull(JourneyTimesParser.parseJourney(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the operator field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithMissingOperator() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_missing_operator.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the operator field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithNullOperator() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_null_operator.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the operator field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseJourneyWithEmptyOperator() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_empty_operator.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the route field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithMissingRoute() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_missing_route.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the route field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithNullRoute() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_null_route.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the route field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithEmptyRoute() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_empty_route.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the destination field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseJourneyWithMissingDestination() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_missing_destination.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the destination field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithNullDestination() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_null_destination.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the destination field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithEmptyDestination() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_empty_destination.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the global disruption field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseJourneyWithMissingGlobalDisruption() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_missing_global_disruption.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the global disruption field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseJourneyWithNullGlobalDisruption() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_null_global_disruption.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the service disruption field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseJourneyWithMissingServiceDisruption() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_missing_service_disruption.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the service disruption field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithNullServiceDisruption() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_null_service_disruption.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the service diversion field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseJourneyWithMissingServiceDiversion() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_missing_service_diversion.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * when the service diversion field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseJourneyWithNullServiceDiversion() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_null_service_diversion.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns {@code null}
     * when the departures array is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithMissingDepartures() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_missing_departures.json");
        assertNull(JourneyTimesParser.parseJourney(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns {@code null}
     * when the departures array is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithNullDepartures() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_null_departures.json");
        assertNull(JourneyTimesParser.parseJourney(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns {@code null}
     * when the departures array is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithEmptyDepartures() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_empty_departures.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertTrue(departures.isEmpty());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * but with 0 departures when there are no valid departure objects in the list.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithAllDeparturesInvalid() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_all_invalid_departures.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertTrue(departures.isEmpty());
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * with a list of valid departures, with the invalid departures discarded.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithSomeDeparturesInvalid() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_some_invalid_departures.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
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
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * and all the departures are in the correct order when the departures list is unsorted.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyWithUnsortedDepartures() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_unsorted_services.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);

        final int size = departures.size();
        assertEquals(9, size);

        final String[] expected = new String[] {
                "36237983", "36232545", "36232547", "36236754", "36253269", "36243498", "36248598",
                "36245456", "36236542"
        };

        for (int i = 0; i < size; i++) {
            assertEquals(expected[i], departures.get(i).getStopCode());
        }
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourney(org.json.JSONObject)} returns a valid object
     * with the correct number of departures.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testParseJourneyValid() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/journey_valid.json");
        final Journey journey = JourneyTimesParser.parseJourney(reader.getJSONObject());

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

        final List<JourneyDeparture> departures = journey.getDepartures();
        assertNotNull(departures);
        assertEquals(34, departures.size());

        departures.remove(0);
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns
     * {@code null} when the {@link org.json.JSONObject} sent in is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithEmptyJsonObject()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/generic/empty_object.json");
        assertNull(JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns
     * {@code null} when the {@code stopCode} field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithMissingStopCode() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_missing_stopcode.json");
        assertNull(JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns
     * {@code null} when the {@code stopCode} field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithNullStopCode() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_null_stopcode.json");
        assertNull(JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns
     * {@code null} when the {@code stopCode} field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithEmptyStopCode() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_empty_stopcode.json");
        assertNull(JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns a
     * valid object when the stop name field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithMissingStopName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_missing_stop_name.json");
        final EdinburghJourneyDeparture departure =
                JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject());

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
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns a
     * valid object when the stop name field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithNullStopName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_null_stop_name.json");
        final EdinburghJourneyDeparture departure =
                JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject());

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
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns a
     * valid object when the stop name field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithEmptyStopName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_empty_stop_name.json");
        final EdinburghJourneyDeparture departure =
                JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject());

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
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns
     * {@code null} when the minutes field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithMissingMinutes() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_missing_minutes.json");
        assertNull(JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns
     * {@code null} when the minutes field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithNullMinutes() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_null_minutes.json");
        assertNull(JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns a
     * valid object when the disrupted field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithMissingDisrupted() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_missing_disrupted.json");
        final EdinburghJourneyDeparture departure =
                JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject());

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
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns a
     * valid object when the disrupted field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithNullDisrupted() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_null_disrupted.json");
        final EdinburghJourneyDeparture departure =
                JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject());

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
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns
     * {@code null} when the reliability field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithMissingReliability() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_missing_reliability.json");
        assertNull(JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns
     * {@code null} when the reliability field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithNullReliability() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_null_reliability.json");
        assertNull(JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns
     * {@code null} when the reliability field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithEmptyReliability() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_empty_reliability.json");
        assertNull(JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns
     * {@code null} when the type field is missing.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithMissingType() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_missing_type.json");
        assertNull(JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns
     * {@code null} when the type field is {@code null}.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithNullType() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_null_type.json");
        assertNull(JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns
     * {@code null} when the type field is empty.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureWithEmptyType() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_empty_type.json");
        assertNull(JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject()));
    }

    /**
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns a
     * valid object when the data is valid, for a departure in the future.
     *
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the test fail.
     */
    @Test
    public void testParseJourneyDepartureForFutureDeparture() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_valid_future_departure.json");
        final EdinburghJourneyDeparture departure =
                JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject());

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
     * Test that {@link JourneyTimesParser#parseJourneyDeparture(org.json.JSONObject)} returns a
     * valid object when the data is valid, for a departure in the past.
     *
     * @throws Exception There are no other exceptions expected from this test, so if there are,
     * let the test fail.
     */
    @Test
    public void testParseJourneyDepartureForPastDeparture() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/getJourneyTimes/departure_valid_past_departure.json");
        final EdinburghJourneyDeparture departure =
                JourneyTimesParser.parseJourneyDeparture(reader.getJSONObject());

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
