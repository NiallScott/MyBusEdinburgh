/*
 * Copyright (C) 2012 - 2014 Niall 'Rivernile' Scott
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

import java.util.List;

/**
 * This object is used to store the result of loading updates from Twitter. It
 * will either hold a List of Tweets, or a TwitterException if there was a
 * problem while loading.
 * 
 * @author Niall Scott
 */
public class TwitterLoaderResult {
    
    private final List<Tweet> tweets;
    private final TwitterException exception;
    
    /**
     * Create a new TwitterLoaderResult to hold the result of a successful load.
     * A successful load is one where a List of Tweets exists. This List can be
     * empty but not null.
     * 
     * @param tweets A non-null List of Tweets.
     */
    public TwitterLoaderResult(final List<Tweet> tweets) {
        if (tweets == null) {
            throw new IllegalArgumentException("The List of Tweets must not be "
                    + "null.");
        }
        
        this.tweets = tweets;
        exception = null;
    }
    
    /**
     * Create a new TwitterLoaderResult to hold the results of a load that
     * failed.
     * 
     * @param exception The TwitterException containing details about the
     * failure. Must not be null.
     */
    public TwitterLoaderResult(final TwitterException exception) {
        if (exception == null) {
            throw new IllegalArgumentException("The exception must not be "
                    + "null.");
        }
        
        this.exception = exception;
        tweets = null;
    }
    
    /**
     * Return the List of Tweets contained in this result. This will return null
     * if the result contains an Exception.
     * 
     * @return null if there was an Exception, otherwise a List of Tweets.
     */
    public List<Tweet> getTweets() {
        return tweets;
    }
    
    /**
     * Returns the TwitterException held in this result. This will return null
     * if there is no Exception held.
     * 
     * @return The TwitterException if there was an error, or null if there was
     * no error.
     */
    public TwitterException getException() {
        return exception;
    }
    
    /**
     * Get whether the result contains an Exception or not.
     * 
     * @return true if the result contains an Exception, false if not.
     */
    public boolean hasException() {
        return exception != null;
    }
}