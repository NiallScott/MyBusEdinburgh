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

package uk.org.rivernile.android.bustracker.parser.twitter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import junit.framework.TestCase;

/**
 * Tests for TwitterLoaderResult.
 * 
 * @author Niall Scott
 */
public class TwitterLoaderResultTests extends TestCase {
    
    /**
     * Test that the constructor used for accepting a List of {@link Tweet}s
     * correctly throws an IllegalArgumentException when that List is set to
     * null.
     */
    public void testNullListConstructor() {
        try {
            new TwitterLoaderResult((List<Tweet>) null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The tweets List is set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor used for accepting a {@link TwitterException}
     * correctly throws an IllegalArgumentException when the exception is set to
     * null.
     */
    public void testNullExceptionConstructor() {
        try {
            new TwitterLoaderResult((TwitterException) null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The exception is set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor used for accepting a List of {@link Tweet}s
     * correctly takes the List, makes the List available via the
     * {@link TwitterLoaderResult#getTweets()} method and that
     * {@link TwitterLoaderResult#hasException()} returns false.
     */
    public void testConstructorWithList() {
        final List<Tweet> tweets = new ArrayList<Tweet>();
        final Tweet tweet = new Tweet("a", "b", new Date(), "d", "e");
        tweets.add(tweet);
        
        final TwitterLoaderResult result = new TwitterLoaderResult(tweets);
        assertFalse(result.hasException());
        
        final List<Tweet> resultTweets = result.getTweets();
        assertNotNull(resultTweets);
        assertEquals(1, resultTweets.size());
        final Tweet resultTweet = resultTweets.get(0);
        
        assertEquals("a", resultTweet.getBody());
        assertEquals("b", resultTweet.getDisplayName());
    }
    
    /**
     * Test that the constructor used for accepting a {@link TwitterException}
     * correctly takes the TwitterException, makes it available via the
     * {@link TwitterLoaderResult#getException()} method and that
     * {@link TwitterLoaderResult#hasException()} returns true.
     */
    public void testConstructorWithException() {
        final TwitterException exception = new TwitterException();
        final TwitterLoaderResult result = new TwitterLoaderResult(exception);
        
        assertTrue(result.hasException());
        assertEquals(exception, result.getException());
    }
}