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

package uk.org.rivernile.android.bustracker.parser.database;

import junit.framework.TestCase;

/**
 * Tests for DatabaseVersion.
 * 
 * @author Niall Scott
 */
public class DatabaseVersionTests extends TestCase {
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the schemaName is set to null.
     */
    public void testConstructorWithNullSchemaName() {
        try {
            new DatabaseVersion(null, "b", "c", "d");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The schemaName was set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the schemaName is set to empty.
     */
    public void testConstructorWithEmptySchemaName() {
        try {
            new DatabaseVersion("", "b", "c", "d");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The schemaName was set to empty, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the topologyId is set to null.
     */
    public void testConstructorWithNullTopologyId() {
        try {
            new DatabaseVersion("a", null, "c", "d");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The topologyId was set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the topologyId is set to empty.
     */
    public void testConstructorWithEmptyTopologyId() {
        try {
            new DatabaseVersion("a", "", "c", "d");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The topologyId was set to empty, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the url is set to null.
     */
    public void testConstructorWithNullUrl() {
        try {
            new DatabaseVersion("a", "b", null, "d");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The url was set to null, so an IllegalArgumentException should "
                + "be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the url is set to empty.
     */
    public void testConstructorWithEmptyUrl() {
        try {
            new DatabaseVersion("a", "b", "", "d");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The url was set to empty, so an IllegalArgumentException should "
                + "be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the checksum is set to null.
     */
    public void testConstructorWithNullChecksum() {
        try {
            new DatabaseVersion("a", "b", "c", null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The checksum was set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the checksum is set to empty.
     */
    public void testConstructorWithEmptyChecksum() {
        try {
            new DatabaseVersion("a", "b", "c", "");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The checksum was set to empty, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that with valid data sent to the constructor, the getters return
     * the correct data.
     */
    public void testWithValidData() {
        final DatabaseVersion version = new DatabaseVersion("MBE_10",
                "aeb023caaab29d2f73868bd34028e003",
                "http://example.com/path/database.db",
                "6758df59b449d00731a329edd4020a61");
        
        assertEquals("MBE_10", version.getSchemaName());
        assertEquals("aeb023caaab29d2f73868bd34028e003",
                version.getTopologyId());
        assertEquals("http://example.com/path/database.db", version.getUrl());
        assertEquals("6758df59b449d00731a329edd4020a61", version.getChecksum());
    }
}