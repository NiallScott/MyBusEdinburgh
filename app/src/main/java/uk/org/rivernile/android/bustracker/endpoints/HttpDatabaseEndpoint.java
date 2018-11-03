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

import android.content.Context;
import androidx.annotation.NonNull;

import uk.org.rivernile.android.bustracker.parser.database.DatabaseEndpointException;
import uk.org.rivernile.android.bustracker.parser.database.DatabaseVersion;
import uk.org.rivernile.android.bustracker.parser.database.DatabaseVersionParser;
import uk.org.rivernile.android.fetchutils.fetchers.HttpFetcher;

/**
 * This defines the HTTP endpoint for getting database version information.
 * 
 * @author Niall Scott
 */
public class HttpDatabaseEndpoint extends DatabaseEndpoint {

    private final Context context;
    private final UrlBuilder urlBuilder;
    
    /**
     * Create a new {@code HttpDatabaseEndpoint}.
     * 
     * @param parser The parser to use to parse the data.
     * @param urlBuilder The {@link UrlBuilder} to use.
     */
    public HttpDatabaseEndpoint(@NonNull final Context context,
            @NonNull final DatabaseVersionParser parser, @NonNull final UrlBuilder urlBuilder) {
        super(parser);

        this.context = context;
        this.urlBuilder = urlBuilder;
    }

    @NonNull
    @Override
    public DatabaseVersion getDatabaseVersion(@NonNull final String schemaType)
            throws DatabaseEndpointException {
        final HttpFetcher fetcher = new HttpFetcher.Builder(context)
                .setUrl(urlBuilder.getDbVersionCheckUrl(schemaType).toString())
                .setAllowHostRedirects(false)
                .build();

        return getParser().getDatabaseVersion(fetcher);
    }
}