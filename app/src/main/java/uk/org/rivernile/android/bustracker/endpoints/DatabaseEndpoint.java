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

package uk.org.rivernile.android.bustracker.endpoints;

import uk.org.rivernile.android.bustracker.parser.database
        .DatabaseEndpointException;
import uk.org.rivernile.android.bustracker.parser.database.DatabaseVersion;
import uk.org.rivernile.android.bustracker.parser.database
        .DatabaseVersionParser;

/**
 * A database endpoint is an abstraction layer to enable slotting in new
 * versions easily and enables unit testing. Subclasses define the way that data
 * is fetched from the data source.
 * 
 * @author Niall Scott
 */
public abstract class DatabaseEndpoint {
    
    private final DatabaseVersionParser parser;
    
    /**
     * Create a new DatabaseEndpoint.
     * 
     * @param parser The parser to use to parse the incoming data. Must not be
     * null.
     */
    public DatabaseEndpoint(final DatabaseVersionParser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("The parser should not be "
                    + "null.");
        }
        
        this.parser = parser;
    }
    
    /**
     * Get the parser instance.
     * 
     * @return The parser instance.
     */
    protected final DatabaseVersionParser getParser() {
        return parser;
    }
    
    /**
     * Get the DatabaseVersion from the endpoint.
     * 
     * @param schemaType The name of the schema to check the server for.
     * @return The DatabaseVersion if not exceptions occur.
     * @throws DatabaseEndpointException If a problem occurs during fetching or
     * parsing of the data.
     */
    public abstract DatabaseVersion getDatabaseVersion(String schemaType)
            throws DatabaseEndpointException;
}