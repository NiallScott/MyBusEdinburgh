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

package uk.org.rivernile.android.fetchers;

import android.content.Context;
import android.net.Uri;

/**
 * This class contains a static method that allows an appropriate
 * {@link Fetcher} to be created depending on the {@link Uri}.
 * 
 * @author Niall Scott
 * @see #getFetcher(android.content.Context, android.net.Uri, boolean)
 */
public final class FetcherFactory {
    
    private static final String SCHEME_HTTP = "http";
    private static final String SCHEME_HTTPS = "https";
    private static final String SCHEME_ASSET = "android.asset";
    private static final String SCHEME_FILE = "file";
    
    /**
     * This private constructor exists to prevent instantiation of this class.
     */
    private FetcherFactory() {
        // Intentionally left blank.
    }
    
    /**
     * Get the most appropriate {@link Fetcher} for the given {@link Uri}. This
     * is based on the scheme in the {@link Uri} object.
     * 
     * The following are supported;
     * 
     * <ul>
     * <li>{@code http://<host>[:port]/[path]}</li>
     * <li>{@code https://<host>[:port]/[path]}</li>
     * <li>{@code android.asset://<path>}</li>
     * <li>{@code file://<path>}</li>
     * </ul>
     * 
     * @param context A {@link Context} instance. If this is {@code null}, an
     * {@link IllegalArgumentException} may be thrown.
     * @param uri The {@link Uri} of the data to be fetched. If this is set as
     * {@code null}, then this method will return {@code null}. If the
     * {@link Uri} is incomplete, such as if it contains a {@code file://}
     * scheme without a path, {@code null} will be returned.
     * @param allowRedirects Some {@link Fetcher}s allow the path to be
     * redirected. Set this to {@code false} to prevent this from happening.
     * @return An appropriate {@link Fetcher} for the given {@link Uri}, or
     * {@code null} if there is no suitable {@link Fetcher}s or {@code uri} is
     * set as {@code null}.
     */
    public static Fetcher getFetcher(final Context context, final Uri uri,
            final boolean allowRedirects) {
        if (uri == null) {
            return null;
        }
        
        final String scheme = uri.getScheme();
        try {
            if (SCHEME_HTTP.equalsIgnoreCase(scheme) ||
                    SCHEME_HTTPS.equalsIgnoreCase(scheme)) {
                return new HttpFetcher(uri.toString(), allowRedirects);
            } else if (SCHEME_ASSET.equalsIgnoreCase(scheme)) {
                return new AssetFileFetcher(context, uri.getPath());
            } else if (SCHEME_FILE.equalsIgnoreCase(scheme)) {
                return new FileFetcher(uri.getPath());
            }
        } catch (IllegalArgumentException e) {
            return null;
        }
        
        return null;
    }
}