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

import java.util.List;
import uk.org.rivernile.android.bustracker.parser.twitter.Tweet;
import uk.org.rivernile.android.bustracker.parser.twitter.TwitterException;
import uk.org.rivernile.android.bustracker.parser.twitter.TwitterParser;

/**
 * A Twitter endpoint is an abstraction layer to enable slotting in new
 * versions easily and enables easy unit testing. Subclasses define the way that
 * data is fetched from the data source.
 * 
 * @author Niall Scott
 */
public abstract class TwitterEndpoint {
    
    private final TwitterParser parser;
    
    /**
     * Create a new TwitterEndpoint.
     * 
     * @param parser The parser to use to parse tweets coming from the endpoint.
     */
    public TwitterEndpoint(final TwitterParser parser) {
        if (parser == null) {
            throw new IllegalArgumentException("The parser must not be null.");
        }
        
        this.parser = parser;
    }
    
    /**
     * Get the parser instance.
     * 
     * @return The parser instance.
     */
    protected final TwitterParser getParser() {
        return parser;
    }
    
    /**
     * Get a List of available Tweets. If there was an error while fetching the
     * tweets, a TwitterException will be thrown. The returned List should not
     * be null. The List may be empty if there was no available tweets, or there
     * was a problem parsing tweets.
     * 
     * @return A List of Tweet objects. Will never be null. May be empty.
     * @throws TwitterException If there was a problem fetching the data.
     */
    public abstract List<Tweet> getTweets() throws TwitterException;
}