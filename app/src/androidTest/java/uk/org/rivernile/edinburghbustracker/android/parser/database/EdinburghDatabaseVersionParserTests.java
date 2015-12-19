/*
 * Copyright (C) 2014 - 2015 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.parser.database;

import static org.junit.Assert.assertEquals;

import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import uk.org.rivernile.android.bustracker.parser.database.DatabaseEndpointException;
import uk.org.rivernile.android.bustracker.parser.database.DatabaseVersion;
import uk.org.rivernile.android.fetchutils.fetchers.AssetFileFetcher;

/**
 * Tests for {@link EdinburghDatabaseVersionParser}.
 * 
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class EdinburghDatabaseVersionParserTests {
    
    private EdinburghDatabaseVersionParser parser;

    @Before
    public void setUp() {
        parser = new EdinburghDatabaseVersionParser();
    }

    @After
    public void tearDown() {
        parser = null;
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the resource does not exist.
     */
    @Test(expected = DatabaseEndpointException.class)
    public void testJsonFileDoesNotExist() throws DatabaseEndpointException {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/generic/not_a_file.json");
        parser.getDatabaseVersion(fetcher);
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the JSON file contains
     * ill-formatted JSON.
     */
    @Test(expected = DatabaseEndpointException.class)
    public void testInvalidJson() throws DatabaseEndpointException {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/generic/invalid.json");
        parser.getDatabaseVersion(fetcher);
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the JSON object is empty.
     */
    @Test(expected = DatabaseEndpointException.class)
    public void testEmptyJsonObject() throws DatabaseEndpointException {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/generic/empty_object.json");
        parser.getDatabaseVersion(fetcher);
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the JSON does not include the
     * schema name.
     */
    @Test(expected = DatabaseEndpointException.class)
    public void testSchemaNameMissing() throws DatabaseEndpointException {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/databaseVersion/missing_schema_name.json");
        parser.getDatabaseVersion(fetcher);
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the JSON does not include the
     * topology ID.
     */
    @Test(expected = DatabaseEndpointException.class)
    public void testTopologyIdMissing() throws DatabaseEndpointException {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/databaseVersion/missing_topology_id.json");
        parser.getDatabaseVersion(fetcher);
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the JSON does not include the
     * database URL.
     */
    @Test(expected = DatabaseEndpointException.class)
    public void testDbUrlMissing() throws DatabaseEndpointException {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(InstrumentationRegistry.getContext(),
                        "endpoints/databaseVersion/missing_db_url.json");
        parser.getDatabaseVersion(fetcher);
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the JSON does not include the
     * checksum.
     */
    @Test(expected = DatabaseEndpointException.class)
    public void testChecksumMissing() throws DatabaseEndpointException {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/databaseVersion/missing_checksum.json");
        parser.getDatabaseVersion(fetcher);
    }
    
    /**
     * Test that a valid response is correctly parsed and yields correct data.
     * 
     * @throws DatabaseEndpointException This is not expected to be thrown in this test, so if it
     * is, let the test fail.
     */
    @Test
    public void testValidResponse() throws DatabaseEndpointException {
        final AssetFileFetcher fetcher = new AssetFileFetcher(InstrumentationRegistry.getContext(),
                "endpoints/databaseVersion/valid.json");
        final DatabaseVersion version = parser.getDatabaseVersion(fetcher);
        
        assertEquals("MBE_10", version.getSchemaName());
        assertEquals("aeb023caaab29d2f73868bd34028e003", version.getTopologyId());
        assertEquals("http://example.com/db/database.db", version.getUrl());
        assertEquals("6758df59b449d00731a329edd4020a61", version.getChecksum());
    }
}