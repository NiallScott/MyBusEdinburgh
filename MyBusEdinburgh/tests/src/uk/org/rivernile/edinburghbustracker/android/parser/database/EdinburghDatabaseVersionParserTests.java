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

package uk.org.rivernile.edinburghbustracker.android.parser.database;

import android.test.InstrumentationTestCase;
import uk.org.rivernile.android.bustracker.parser.database
        .DatabaseEndpointException;
import uk.org.rivernile.android.bustracker.parser.database.DatabaseVersion;
import uk.org.rivernile.android.fetchers.AssetFileFetcher;

/**
 * Tests for EdinburghDatabaseVersionParser.
 * 
 * @author Niall Scott
 */
public class EdinburghDatabaseVersionParserTests
        extends InstrumentationTestCase {
    
    private EdinburghDatabaseVersionParser parser;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        parser = new EdinburghDatabaseVersionParser();
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
     * Test that {@link EdinburghDatabaseVersionParser#getDatabaseVersion(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly throws IllegalArgumentException when the Fetcher is null.
     * 
     * @throws DatabaseEndpointException This shouldn't be thrown by this test,
     * but if it is, let the TestCase handle the exception to fail the test.
     */
    public void testGetDatabaseVersionWithNullFetcher()
            throws DatabaseEndpointException {
        try {
            parser.getDatabaseVersion(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The fetcher is set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the resource
     * does not exist.
     */
    public void testJsonFileDoesNotExist() {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "endpoints/generic/not_a_file.json");
        
        try {
            parser.getDatabaseVersion(fetcher);
        } catch (DatabaseEndpointException e) {
            return;
        }
        
        fail("The JSON file does not exist, so a DatabaseEndpointException "
                + "should be thrown.");
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the JSON file
     * contains ill-formatted JSON.
     */
    public void testInvalidJson() {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "endpoints/generic/invalid.json");
        
        try {
            parser.getDatabaseVersion(fetcher);
        } catch (DatabaseEndpointException e) {
            return;
        }
        
        fail("The JSON response was invalid, so a DatabaseEndpointException "
                + "should be thrown.");
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the JSON
     * object is empty.
     */
    public void testEmptyJsonObject() {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "endpoints/generic/empty_object.json");
        
        try {
            parser.getDatabaseVersion(fetcher);
        } catch (DatabaseEndpointException e) {
            return;
        }
        
        fail("The JSON response was empty, so a DatabaseEndpointException "
                + "should be thrown.");
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the JSON does
     * not include the schema name.
     */
    public void testSchemaNameMissing() {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "endpoints/databaseVersion/missing_schema_name.json");
        
        try {
            parser.getDatabaseVersion(fetcher);
        } catch (DatabaseEndpointException e) {
            return;
        }
        
        fail("The JSON response was missing a schema name, so a "
                + "DatabaseEndpointException should be thrown.");
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the JSON does
     * not include the topology ID.
     */
    public void testTopologyIdMissing() {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "endpoints/databaseVersion/missing_topology_id.json");
        
        try {
            parser.getDatabaseVersion(fetcher);
        } catch (DatabaseEndpointException e) {
            return;
        }
        
        fail("The JSON response was missing a topology ID, so a "
                + "DatabaseEndpointException should be thrown.");
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the JSON does
     * not include the database URL.
     */
    public void testDbUrlMissing() {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "endpoints/databaseVersion/missing_db_url.json");
        
        try {
            parser.getDatabaseVersion(fetcher);
        } catch (DatabaseEndpointException e) {
            return;
        }
        
        fail("The JSON response was missing a database URL, so a "
                + "DatabaseEndpointException should be thrown.");
    }
    
    /**
     * Test that {@link DatabaseEndpointException} is thrown if the JSON does
     * not include the checksum.
     */
    public void testChecksumMissing() {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "endpoints/databaseVersion/missing_checksum.json");
        
        try {
            parser.getDatabaseVersion(fetcher);
        } catch (DatabaseEndpointException e) {
            return;
        }
        
        fail("The JSON response was missing a checksum, so a "
                + "DatabaseEndpointException should be thrown.");
    }
    
    /**
     * Test that a valid response is correctly parsed and yields correct data.
     * 
     * @throws DatabaseEndpointException This is not expected to be thrown in
     * this test, so if it is, let the TestCase cause a test failure.
     */
    public void testValidResponse() throws DatabaseEndpointException {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        "endpoints/databaseVersion/valid.json");
        final DatabaseVersion version = parser.getDatabaseVersion(fetcher);
        
        assertEquals("MBE_10", version.getSchemaName());
        assertEquals("aeb023caaab29d2f73868bd34028e003",
                version.getTopologyId());
        assertEquals("http://example.com/db/database.db", version.getUrl());
        assertEquals("6758df59b449d00731a329edd4020a61", version.getChecksum());
    }
}