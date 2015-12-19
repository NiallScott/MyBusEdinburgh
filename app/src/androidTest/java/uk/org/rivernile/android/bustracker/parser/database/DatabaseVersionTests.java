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

package uk.org.rivernile.android.bustracker.parser.database;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link DatabaseVersion}.
 * 
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class DatabaseVersionTests {

    /**
     * Test that {@link DatabaseVersion.Builder#build()} throws an {@link IllegalArgumentException}
     * when the schema name is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSchemaNameIsNull() {
        new DatabaseVersion.Builder()
                .setSchemaName(null)
                .setTopologyId("b")
                .setUrl("c")
                .setChecksum("d")
                .build();
    }

    /**
     * Test that {@link DatabaseVersion.Builder#build()} throws an {@link IllegalArgumentException}
     * when the schema name is empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testSchemaNameIsEmpty() {
        new DatabaseVersion.Builder()
                .setSchemaName("")
                .setTopologyId("b")
                .setUrl("c")
                .setChecksum("d")
                .build();
    }

    /**
     * Test that {@link DatabaseVersion.Builder#build()} throws an {@link IllegalArgumentException}
     * when the topology ID is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTopologyIdIsNull() {
        new DatabaseVersion.Builder()
                .setSchemaName("a")
                .setTopologyId(null)
                .setUrl("c")
                .setChecksum("d")
                .build();
    }

    /**
     * Test that {@link DatabaseVersion.Builder#build()} throws an {@link IllegalArgumentException}
     * when the topology ID is empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testTopologyIdIsEmpty() {
        new DatabaseVersion.Builder()
                .setSchemaName("a")
                .setTopologyId("")
                .setUrl("c")
                .setChecksum("d")
                .build();
    }

    /**
     * Test that {@link DatabaseVersion.Builder#build()} throws an {@link IllegalArgumentException}
     * when the URL is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUrlIsNull() {
        new DatabaseVersion.Builder()
                .setSchemaName("a")
                .setTopologyId("b")
                .setUrl(null)
                .setChecksum("d")
                .build();
    }

    /**
     * Test that {@link DatabaseVersion.Builder#build()} throws an {@link IllegalArgumentException}
     * when the URL is empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testUrlIsEmpty() {
        new DatabaseVersion.Builder()
                .setSchemaName("a")
                .setTopologyId("b")
                .setUrl("")
                .setChecksum("d")
                .build();
    }

    /**
     * Test that {@link DatabaseVersion.Builder#build()} throws an {@link IllegalArgumentException}
     * when the checksum is null.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testChecksumIsNull() {
        new DatabaseVersion.Builder()
                .setSchemaName("a")
                .setTopologyId("b")
                .setUrl("c")
                .setChecksum(null)
                .build();
    }

    /**
     * Test that {@link DatabaseVersion.Builder#build()} throws an {@link IllegalArgumentException}
     * when the checksum is empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testChecksumIsEmpty() {
        new DatabaseVersion.Builder()
                .setSchemaName("a")
                .setTopologyId("b")
                .setUrl("c")
                .setChecksum("")
                .build();
    }

    /**
     * Test that a {@link DatabaseVersion} object is correctly built and the getters return the
     * correct data.
     */
    @Test
    public void testValidObject() {
        final DatabaseVersion dbVersion = new DatabaseVersion.Builder()
                .setSchemaName("MBE_10")
                .setTopologyId("aeb023caaab29d2f73868bd34028e003")
                .setUrl("http://example.com/path/database.db")
                .setChecksum("6758df59b449d00731a329edd4020a61")
                .build();

        assertEquals("MBE_10", dbVersion.getSchemaName());
        assertEquals("aeb023caaab29d2f73868bd34028e003", dbVersion.getTopologyId());
        assertEquals("http://example.com/path/database.db", dbVersion.getUrl());
        assertEquals("6758df59b449d00731a329edd4020a61", dbVersion.getChecksum());
    }

    /**
     * Test that {@link DatabaseVersion#equals(Object)} behaves as expected.
     */
    @Test
    public void testEquals() {
        final DatabaseVersion[] versions = getObjectsForTestingEqualsAndHashCode();

        assertTrue(versions[0].equals(versions[0]));
        assertTrue(versions[0].equals(versions[1]));
        assertTrue(versions[0].equals(versions[4]));
        assertTrue(versions[0].equals(versions[5]));

        assertFalse(versions[0].equals(versions[2]));
        assertFalse(versions[0].equals(versions[3]));
        assertFalse(versions[0].equals(null));
        assertFalse(versions[0].equals(new Object()));
    }

    /**
     * Test that {@link DatabaseVersion#hashCode()} behaves as expected.
     */
    @Test
    public void testHashCode() {
        final DatabaseVersion[] versions = getObjectsForTestingEqualsAndHashCode();

        assertEquals(versions[0].hashCode(), versions[0].hashCode());
        assertEquals(versions[0].hashCode(), versions[1].hashCode());
        assertEquals(versions[0].hashCode(), versions[4].hashCode());
        assertEquals(versions[0].hashCode(), versions[5].hashCode());

        assertNotEquals(versions[0].hashCode(), versions[2].hashCode());
        assertNotEquals(versions[0].hashCode(), versions[3].hashCode());
    }

    /**
     * Get an array of {@link DatabaseVersion} objects as test data for testing
     * {@link DatabaseVersion#equals(Object)} and {@link DatabaseVersion#hashCode()}.
     *
     * @return An array of {@link DatabaseVersion} objects as test data for testing
     * {@link DatabaseVersion#equals(Object)} and {@link DatabaseVersion#hashCode()}.
     */
    @NonNull
    private static DatabaseVersion[] getObjectsForTestingEqualsAndHashCode() {
        final DatabaseVersion[] versions = new DatabaseVersion[6];
        versions[0] = new DatabaseVersion.Builder()
                .setSchemaName("MBE_10")
                .setTopologyId("aeb023caaab29d2f73868bd34028e003")
                .setUrl("http://example.com/path/database.db")
                .setChecksum("6758df59b449d00731a329edd4020a61")
                .build();
        versions[1] = new DatabaseVersion.Builder()
                .setSchemaName("MBE_10")
                .setTopologyId("aeb023caaab29d2f73868bd34028e003")
                .setUrl("http://example.com/path/database.db")
                .setChecksum("6758df59b449d00731a329edd4020a61")
                .build();
        versions[2] = new DatabaseVersion.Builder()
                .setSchemaName("MBE_11")
                .setTopologyId("aeb023caaab29d2f73868bd34028e003")
                .setUrl("http://example.com/path/database.db")
                .setChecksum("6758df59b449d00731a329edd4020a61")
                .build();
        versions[3] = new DatabaseVersion.Builder()
                .setSchemaName("MBE_10")
                .setTopologyId("aeb023caaab29d2f73868bd34028e004")
                .setUrl("http://example.com/path/database.db")
                .setChecksum("6758df59b449d00731a329edd4020a61")
                .build();
        versions[4] = new DatabaseVersion.Builder()
                .setSchemaName("MBE_10")
                .setTopologyId("aeb023caaab29d2f73868bd34028e003")
                .setUrl("http://example.com/path/database.db2")
                .setChecksum("6758df59b449d00731a329edd4020a61")
                .build();
        versions[5] = new DatabaseVersion.Builder()
                .setSchemaName("MBE_10")
                .setTopologyId("aeb023caaab29d2f73868bd34028e003")
                .setUrl("http://example.com/path/database.db")
                .setChecksum("6758df59b449d00731a329edd4020a62")
                .build();

        return versions;
    }
}