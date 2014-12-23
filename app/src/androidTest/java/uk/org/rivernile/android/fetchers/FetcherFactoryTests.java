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

import android.net.Uri;
import android.test.InstrumentationTestCase;

/**
 * Tests for {@link FetcherFactory}.
 * 
 * @author Niall Scott
 */
public class FetcherFactoryTests extends InstrumentationTestCase {
    
    /**
     * Test that
     * {@link FetcherFactory#getFetcher(android.content.Context, android.net.Uri, boolean)}
     * returns {@code null} when the {@link Uri} is set to {@code null}.
     */
    public void testGetFetcherWithNullUri() {
        assertNull(FetcherFactory.getFetcher(getInstrumentation().getContext(),
                null, true));
    }
    
    /**
     * Test that
     * {@link FetcherFactory#getFetcher(android.content.Context, android.net.Uri, boolean)}
     * returns an instance of {@link HttpFetcher} when the {@link Uri} scheme
     * is "http".
     */
    public void testGetFetcherWithHttp() {
        final Uri uri = Uri.parse("http://www.google.com/");
        final Fetcher fetcher = FetcherFactory.getFetcher(getInstrumentation()
                .getContext(), uri, true);
        assertSame(HttpFetcher.class, fetcher.getClass());
    }
    
    /**
     * Test that
     * {@link FetcherFactory#getFetcher(android.content.Context, android.net.Uri, boolean)}
     * returns an instance of {@link HttpFetcher} when the {@link Uri} scheme
     * is "https".
     */
    public void testGetFetcherWithHttps() {
        final Uri uri = Uri.parse("https://www.google.com/");
        final Fetcher fetcher = FetcherFactory.getFetcher(getInstrumentation()
                .getContext(), uri, true);
        assertSame(HttpFetcher.class, fetcher.getClass());
    }
    
    /**
     * Test that
     * {@link FetcherFactory#getFetcher(android.content.Context, android.net.Uri, boolean)}
     * returns {@code null} when a path isn't given for the "android.asset"
     * scheme.
     */
    public void testGetFetcherWithAndroidAssetAndNoPath() {
        final Uri uri = Uri.parse("android.asset://");
        final Fetcher fetcher = FetcherFactory.getFetcher(getInstrumentation()
                .getContext(), uri, true);
        assertNull(fetcher);
    }
    
    /**
     * Test that
     * {@link FetcherFactory#getFetcher(android.content.Context, android.net.Uri, boolean)}
     * returns {@code null} when the {@link Context} is set as {@code null} and
     * the scheme is "android.asset".
     */
    public void testGetFetcherWithAndroidAssetAndNullContext() {
        final Uri uri = Uri.parse("android.asset://test/image.jpg");
        final Fetcher fetcher = FetcherFactory.getFetcher(null, uri, true);
        assertNull(fetcher);
    }
    
    /**
     * Test that
     * {@link FetcherFactory#getFetcher(android.content.Context, android.net.Uri, boolean)}
     * returns an instance of {@link AssetFileFetcher} when the {@link Uri}
     * scheme is "android.asset".
     */
    public void testGetFetcherWithAndroidAsset() {
        final Uri uri = Uri.parse("android.asset://test/image.jpg");
        final Fetcher fetcher = FetcherFactory.getFetcher(getInstrumentation()
                .getContext(), uri, true);
        assertSame(AssetFileFetcher.class, fetcher.getClass());
    }
    
    /**
     * Test that
     * {@link FetcherFactory#getFetcher(android.content.Context, android.net.Uri, boolean)}
     * returns {@code null} when a path isn't given for the "file" scheme.
     */
    public void testGetFetcherWithFileAndNoPath() {
        final Uri uri = Uri.parse("file://");
        final Fetcher fetcher = FetcherFactory.getFetcher(getInstrumentation()
                .getContext(), uri, true);
        assertNull(fetcher);
    }
    
    /**
     * Test that
     * {@link FetcherFactory#getFetcher(android.content.Context, android.net.Uri, boolean)}
     * returns an instance of {@link FileFetcher} when the {@link Uri} scheme is
     * "file".
     */
    public void testGetFetcherWithFile() {
        final Uri uri = Uri.parse("file:///usr/tmp/something.txt");
        final Fetcher fetcher = FetcherFactory.getFetcher(getInstrumentation()
                .getContext(), uri, true);
        assertSame(FileFetcher.class, fetcher.getClass());
    }
    
    /**
     * Test that
     * {@link FetcherFactory#getFetcher(android.content.Context, android.net.Uri, boolean)}
     * returns {@code null} when the {@link Uri} scheme is not handled.
     */
    public void testGetFetcherWithInvalidScheme() {
        final Uri uri = Uri.parse("invalid://test/image.jpg");
        final Fetcher fetcher = FetcherFactory.getFetcher(
                getInstrumentation().getContext(), uri, true);
        assertNull(fetcher);
    }
    
    /**
     * Test that
     * {@link FetcherFactory#getFetcher(android.content.Context, android.net.Uri, boolean)}
     * returns {@code null} when the {@link Uri} scheme is not handled and the
     * {@link Uri} is not hierarchical.
     */
    public void testGetFetcherWithInvalidScheme2() {
        final Uri uri = Uri.parse("mailto:someone@mail.com");
        final Fetcher fetcher = FetcherFactory.getFetcher(
                getInstrumentation().getContext(), uri, true);
        assertNull(fetcher);
    }
}