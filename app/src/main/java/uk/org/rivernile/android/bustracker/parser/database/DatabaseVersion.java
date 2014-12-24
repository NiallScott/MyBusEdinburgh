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

import android.text.TextUtils;

/**
 * This model object describes the response from the DatabaseVersion from the
 * database server API.
 * 
 * @author Niall Scott
 */
public class DatabaseVersion {
    
    private final String schemaName;
    private final String topologyId;
    private final String url;
    private final String checksum;
    
    /**
     * Create a new DatabaseVersion. All parameters must not be null or empty.
     * 
     * @param schemaName The schema name.
     * @param topologyId The topology ID.
     * @param url The URL to download the database file.
     * @param checksum The checksum of the database file.
     */
    public DatabaseVersion(final String schemaName, final String topologyId,
            final String url, final String checksum) {
        if (TextUtils.isEmpty(schemaName)) {
            throw new IllegalArgumentException("The schemaName must not be "
                    + "null.");
        }
        
        if (TextUtils.isEmpty(topologyId)) {
            throw new IllegalArgumentException("The topologyId must not be "
                    + "null.");
        }
        
        if (TextUtils.isEmpty(url)) {
            throw new IllegalArgumentException("The url must not be null.");
        }
        
        if (TextUtils.isEmpty(checksum)) {
            throw new IllegalArgumentException("The checksum must not be "
                    + "null.");
        }
        
        this.schemaName = schemaName;
        this.topologyId = topologyId;
        this.url = url;
        this.checksum = checksum;
    }

    /**
     * Get the schema name.
     * 
     * @return The schema name.
     */
    public String getSchemaName() {
        return schemaName;
    }

    /**
     * Get the topology ID.
     * 
     * @return The topology ID.
     */
    public String getTopologyId() {
        return topologyId;
    }

    /**
     * Get the URL to download the database file.
     * 
     * @return The URL to download the database file.
     */
    public String getUrl() {
        return url;
    }

    /**
     * Get the checksum of the database file.
     * 
     * @return The checksum of the database file.
     */
    public String getChecksum() {
        return checksum;
    }
}