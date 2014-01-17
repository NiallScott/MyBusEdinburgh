/*
 * Copyright (C) 2013 - 2014 Niall 'Rivernile' Scott
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

import android.net.Uri;
import android.text.TextUtils;
import java.util.Random;
import uk.org.rivernile.android.bustracker.endpoints.UrlBuilder;
import uk.org.rivernile.edinburghbustracker.android.ApiKey;

/**
 * This class contains a collection of methods used to generate URLs for
 * contacting servers for resources.
 * 
 * @author Niall Scott
 */
public class EdinburghUrlBuilder implements UrlBuilder {
    
    private static final Random RANDOM = new Random(System.currentTimeMillis());
    
    protected static final String SCHEME_HTTP = "http";
    protected static final String BUSTRACKER_HOST = "www.mybustracker.co.uk";
    protected static final String DB_SERVER_HOST = "edinb.us";
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Uri getTopologyUrl() {
        return getBustrackerBuilder()
                .appendQueryParameter("function", "getTopoId")
                .build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Uri getDbVersionCheckUrl(final String schemaType) {
        if (TextUtils.isEmpty(schemaType)) {
            throw new IllegalArgumentException("schemaType must not be null or "
                    + "empty.");
        }
        
        return getDbServerBuilder()
                .appendPath("DatabaseVersion")
                .appendQueryParameter("schemaType", schemaType)
                .build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Uri getBusTimesUrl(final String[] stopCodes,
            final int numDepartures) {
        if (stopCodes == null || stopCodes.length == 0) {
            throw new IllegalArgumentException("The stopCodes array must not "
                    + "be null or empty.");
        }
        
        final Uri.Builder builder = getBustrackerBuilder();
        builder.appendQueryParameter("function", "getBusTimes")
                .appendQueryParameter("nb", String.valueOf(numDepartures));
        
        final int len = stopCodes.length;
        if (len == 1) {
            builder.appendQueryParameter("stopId", stopCodes[0]);
        } else {
            for (int i = 0; i < len; i++) {
                if (i >= 6) {
                    break;
                }
                
                builder.appendQueryParameter("stopId" + (i + 1), stopCodes[i]);
            }
        }
        
        return builder.build();
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Uri getTwitterUpdatesUrl() {
        return getDbServerBuilder()
                .appendPath("TwitterStatuses")
                .appendQueryParameter("appName", "MBE")
                .build();
    }
    
    /**
     * Get a Uri.Builder which represents a URL on the bus tracker API server.
     * The specific request builds upon this builder.
     * 
     * @return A Uri.Builder configured for a URL on the bus tracker API server.
     */
    private static Uri.Builder getBustrackerBuilder() {
        return new Uri.Builder().scheme(SCHEME_HTTP)
                .authority(BUSTRACKER_HOST).path("ws.php")
                .appendQueryParameter("module", "json")
                .appendQueryParameter("key", ApiKey.getHashedKey())
                .appendQueryParameter("random",
                        String.valueOf(RANDOM.nextInt()));
    }
    
    /**
     * Get a Uri.Builder which represents a URL on the database server. The
     * specific request builds upon this builder.
     * 
     * @return A Uri.Builder configured for a URL on the database server.
     */
    private static Uri.Builder getDbServerBuilder() {
        return new Uri.Builder().scheme(SCHEME_HTTP)
                .authority(DB_SERVER_HOST)
                .appendPath("api")
                .appendQueryParameter("key", ApiKey.getHashedKey())
                .appendQueryParameter("random",
                        String.valueOf(RANDOM.nextInt()));
    }
}