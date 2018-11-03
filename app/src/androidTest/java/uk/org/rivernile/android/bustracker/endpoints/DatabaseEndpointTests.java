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

package uk.org.rivernile.android.bustracker.endpoints;

import static org.junit.Assert.assertSame;

import androidx.annotation.NonNull;

import org.junit.Test;

import uk.org.rivernile.android.bustracker.parser.database.DatabaseVersion;
import uk.org.rivernile.android.bustracker.parser.database.DatabaseVersionParser;
import uk.org.rivernile.edinburghbustracker.android.parser.database.EdinburghDatabaseVersionParser;

/**
 * Tests for {@link DatabaseEndpoint}.
 * 
 * @author Niall Scott
 */
public class DatabaseEndpointTests {
    
    /**
     * Test that {@link DatabaseEndpoint#getParser()} returns the same
     * {@link DatabaseVersionParser} object given to it in the constructor.
     */
    @Test
    public void testNotNullConstructor() {
        final EdinburghDatabaseVersionParser parser = new EdinburghDatabaseVersionParser();
        final MockDatabaseEndpoint endpoint = new MockDatabaseEndpoint(parser);
        
        assertSame(parser, endpoint.getParser());
    }
    
    /**
     * Because this is testing an abstract class, it's necessary to mock it out in to a concrete
     * class.
     */
    private static class MockDatabaseEndpoint extends DatabaseEndpoint {
        
        /**
         * Create a new {@code MockDatabaseEndpoint}.
         * 
         * @param parser The parser to use.
         */
        MockDatabaseEndpoint(@NonNull final DatabaseVersionParser parser) {
            super(parser);
        }

        @NonNull
        @Override
        public DatabaseVersion getDatabaseVersion(@NonNull final String schemaType) {
            throw new UnsupportedOperationException();
        }
    }
}