/*
 * Copyright (C) 2014 - 2015 Niall 'Rivernile' Scott
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

import static org.junit.Assert.assertSame;

import android.support.annotation.NonNull;
import android.support.test.runner.AndroidJUnit4;

import java.util.List;

import org.junit.Test;
import org.junit.runner.RunWith;

import uk.org.rivernile.android.bustracker.parser.twitter.Tweet;
import uk.org.rivernile.android.bustracker.parser.twitter.TwitterException;
import uk.org.rivernile.android.bustracker.parser.twitter.TwitterParser;
import uk.org.rivernile.android.bustracker.parser.twitter.TwitterParserImpl;

/**
 * Tests for {@link TwitterEndpoint}.
 * 
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class TwitterEndpointTests {
    
    /**
     * Test that {@link TwitterEndpoint#getParser()} returns the same {@link TwitterParser}
     * object given to it in the constructor.
     */
    @Test
    public void testNotNullConstructor() {
        final TwitterParserImpl parser = new TwitterParserImpl();
        final MockTwitterEndpoint endpoint = new MockTwitterEndpoint(parser);
        
        assertSame(parser, endpoint.getParser());
    }
    
    /**
     * Because this is testing an abstract class, it's necessary to mock it out in to a concrete
     * class.
     */
    private static class MockTwitterEndpoint extends TwitterEndpoint {
        
        /**
         * Create a new {@code MockTwitterEndpoint}.
         * 
         * @param parser The parser to use.
         */
        public MockTwitterEndpoint(@NonNull final TwitterParser parser) {
            super(parser);
        }

        @NonNull
        @Override
        public List<Tweet> getTweets() throws TwitterException {
            throw new UnsupportedOperationException();
        }
    }
}