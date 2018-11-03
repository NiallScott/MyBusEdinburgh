/*
 * Copyright (C) 2013 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;

import android.net.Uri;
import androidx.annotation.NonNull;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import uk.org.rivernile.edinburghbustracker.android.ApiKey;

/**
 * Unit tests for {@link EdinburghUrlBuilder}.
 * 
 * @author Niall Scott
 */
public class EdinburghUrlBuilderTests {
    
    private EdinburghUrlBuilder builder;

    @Before
    public void setUp() {
        builder = new EdinburghUrlBuilder();
    }

    @After
    public void tearDown() {
        builder = null;
    }
    
    /**
     * Test that the URL for getting the topology from the bus tracker server is correctly
     * constructed.
     */
    @Test
    public void testGetTopologyUrl() {
        final Uri uri = builder.getTopologyUrl();
        
        checkBusTrackerUri(uri);
        assertEquals("getTopoId", uri.getQueryParameter("function"));
    }
    
    /**
     * Test that the URL for checking the database version from the database server is correctly
     * constructed.
     */
    @Test
    public void testGetDbVersionCheckUrl() {
        final Uri uri = builder.getDbVersionCheckUrl("test");

        checkDatabaseServerUri(uri);
        assertEquals("/api/DatabaseVersion", uri.getPath());
        assertEquals("test", uri.getQueryParameter("schemaType"));
    }
    
    /**
     * Test that {@link EdinburghUrlBuilder#getBusTimesUrl(String[], int)} correctly throws an
     * {@link IllegalArgumentException} when {@code stopCodes} is set to empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetBusTimesUrlEmptyStopCodes() {
        builder.getBusTimesUrl(new String[] { }, 1);
    }
    
    /**
     * Test that the URL for getting bus times from the bus tracker server is correctly
     * constructed when passing in a single stop code.
     */
    @Test
    public void testGetBusTimesUrlWithOneStopCode() {
        final Uri uri = builder.getBusTimesUrl(new String[] { "123" }, 4);
        
        checkBusTrackerUri(uri);
        assertEquals("getBusTimes", uri.getQueryParameter("function"));
        assertEquals("4", uri.getQueryParameter("nb"));
        assertEquals("123", uri.getQueryParameter("stopId"));
        assertNull(uri.getQueryParameter("stopId1"));
    }
    
    /**
     * Test that the URL for getting bus times from the bus tracker server is correctly
     * constructed when passing in two stop codes.
     */
    @Test
    public void testGetBusTimesUrlWithTwoStopCodes() {
        final Uri uri = builder.getBusTimesUrl(new String[] { "123", "456" }, 6);
        
        checkBusTrackerUri(uri);
        assertEquals("getBusTimes", uri.getQueryParameter("function"));
        assertEquals("6", uri.getQueryParameter("nb"));
        assertEquals("123", uri.getQueryParameter("stopId1"));
        assertEquals("456", uri.getQueryParameter("stopId2"));
        assertNull(uri.getQueryParameter("stopId"));
        assertNull(uri.getQueryParameter("stopId3"));
    }
    
    /**
     * Test that the URL for getting bus times from the bus tracker server is correctly
     * constructed when passing in seven stop codes. It should ignore all stop codes after the
     * sixth element.
     */
    @Test
    public void testGetBusTimesUrlWithSevenStopCodes() {
        final Uri uri = builder.getBusTimesUrl(
                new String[] { "12", "23", "34", "45", "56", "67", "78" }, 2);
        
        checkBusTrackerUri(uri);
        assertEquals("getBusTimes", uri.getQueryParameter("function"));
        assertEquals("2", uri.getQueryParameter("nb"));
        assertEquals("12", uri.getQueryParameter("stopId1"));
        assertEquals("23", uri.getQueryParameter("stopId2"));
        assertEquals("34", uri.getQueryParameter("stopId3"));
        assertEquals("45", uri.getQueryParameter("stopId4"));
        assertEquals("56", uri.getQueryParameter("stopId5"));
        assertEquals("67", uri.getQueryParameter("stopId6"));
        assertNull(uri.getQueryParameter("stopId"));
        assertNull(uri.getQueryParameter("stopId7"));
    }
    
    /**
     * Test that {@link EdinburghUrlBuilder#getJourneyTimesUrl(String, String)} correctly constructs
     * a URL from the given parameters.
     */
    @Test
    public void testGetJourneyTimesUrl() {
        final Uri uri = builder.getJourneyTimesUrl("123456", "7890");
        
        checkBusTrackerUri(uri);
        assertEquals("getJourneyTimes", uri.getQueryParameter("function"));
        assertEquals("123456", uri.getQueryParameter("stopId"));
        assertEquals("7890", uri.getQueryParameter("journeyId"));
    }
    
    /**
     * Test that the URL for getting Twitter updates from the database server is correctly
     * constructed.
     */
    @Test
    public void testGetTwitterUpdatesUrl() {
        final Uri uri = builder.getTwitterUpdatesUrl();

        checkDatabaseServerUri(uri);
        assertEquals("/api/TwitterStatuses", uri.getPath());
        assertEquals("MBE", uri.getQueryParameter("appName"));
    }

    /**
     * Common checks for tests on bus tracker URLs.
     *
     * @param uri The {@link Uri} to check.
     */
    private static void checkBusTrackerUri(@NonNull final Uri uri) {
        assertEquals(EdinburghUrlBuilder.SCHEME_HTTP, uri.getScheme());
        assertEquals(EdinburghUrlBuilder.BUSTRACKER_HOST, uri.getHost());
        assertEquals("/ws.php", uri.getPath());
        assertEquals("json", uri.getQueryParameter("module"));
        assertEquals(ApiKey.getHashedKey(), uri.getQueryParameter("key"));
        assertNotNull(uri.getQueryParameter("random"));
    }

    /**
     * Common checks for tests on database server URLs.
     *
     * @param uri The {@link Uri} to check.
     */
    private static void checkDatabaseServerUri(@NonNull final Uri uri) {
        assertEquals(EdinburghUrlBuilder.SCHEME_HTTP, uri.getScheme());
        assertEquals(EdinburghUrlBuilder.DB_SERVER_HOST, uri.getHost());
        assertEquals(ApiKey.getHashedKey(), uri.getQueryParameter("key"));
        assertNotNull(uri.getQueryParameter("random"));
    }
}