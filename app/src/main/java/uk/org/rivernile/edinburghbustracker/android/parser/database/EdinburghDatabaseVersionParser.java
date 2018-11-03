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

package uk.org.rivernile.edinburghbustracker.android.parser.database;

import androidx.annotation.NonNull;

import java.io.IOException;
import org.json.JSONException;
import org.json.JSONObject;
import uk.org.rivernile.android.bustracker.parser.database.DatabaseEndpointException;
import uk.org.rivernile.android.bustracker.parser.database.DatabaseVersion;
import uk.org.rivernile.android.bustracker.parser.database.DatabaseVersionParser;
import uk.org.rivernile.android.fetchutils.fetchers.Fetcher;
import uk.org.rivernile.android.fetchutils.fetchers.readers.JSONFetcherStreamReader;
import uk.org.rivernile.android.utils.JSONUtils;

/**
 * This parser gets version data from the bus stop database server.
 * 
 * @author Niall Scott
 */
public class EdinburghDatabaseVersionParser implements DatabaseVersionParser {

    @NonNull
    @Override
    public DatabaseVersion getDatabaseVersion(@NonNull final Fetcher fetcher)
            throws DatabaseEndpointException {
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        
        try {
            fetcher.executeFetcher(reader);
            
            final JSONObject jo = reader.getJSONObject();
            return new DatabaseVersion.Builder()
                    .setSchemaName(JSONUtils.getString(jo, "db_schema_version"))
                    .setTopologyId(JSONUtils.getString(jo, "topo_id"))
                    .setUrl(JSONUtils.getString(jo, "db_url"))
                    .setChecksum(JSONUtils.getString(jo, "checksum"))
                    .build();
        } catch (IOException e) {
            throw new DatabaseEndpointException(e);
        } catch (JSONException e) {
            throw new DatabaseEndpointException(e);
        } catch (IllegalArgumentException e) {
            throw new DatabaseEndpointException("Unable to parse DatabaseVersion.");
        }
    }
}