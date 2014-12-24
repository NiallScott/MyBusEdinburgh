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

import java.util.Date;
import java.util.GregorianCalendar;
import junit.framework.TestCase;

/**
 * Tests for Tweet.
 * 
 * @author Niall Scott
 */
public class TweetTests extends TestCase {
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the body is set to null.
     */
    public void testConstructorWithNullBody() {
        try {
            new Tweet(null, "b", new Date(), "d", "e");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The body is set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the body is set to empty.
     */
    public void testConstructorWithEmptyBody() {
        try {
            new Tweet("", "b", new Date(), "d", "e");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The body is set to empty, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the displayName is set to null.
     */
    public void testConstructorWithNullDisplayName() {
        try {
            new Tweet("a", null, new Date(), "d", "e");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The displayName is set to null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the displayName is set to empty.
     */
    public void testConstructorWithEmptyDisplayName() {
        try {
            new Tweet("a", "", new Date(), "d", "e");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The displayName is set to empty, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the time is set to null.
     */
    public void testConstructorWithNullTime() {
        try {
            new Tweet("a", "b", null, "d", "e");
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The time is set to null, so an IllegalArgumentException should "
                + "be thrown.");
    }
    
    /**
     * Test that the constructor does not throw an exception when the
     * profileImageUrl is set to null.
     */
    public void testConstructorWithNullProfileImageUrl() {
        try {
            new Tweet("a", "b", new Date(), null, "e");
        } catch (IllegalArgumentException e) {
            fail("The profileImageUrl is allowed to be null.");
        }
    }
    
    /**
     * Test that the constructor does not throw an exception when the
     * profileImageUrl is set to null.
     */
    public void testConstructorWithEmptyProfileImageUrl() {
        try {
            new Tweet("a", "b", new Date(), "", "e");
        } catch (IllegalArgumentException e) {
            fail("The profileImageUrl is allowed to be empty.");
        }
    }
    
    /**
     * Test that the constructor does not throw an exception when the profileUrl
     * is set to null.
     */
    public void testConstructorWithNullProfileUrl() {
        try {
            new Tweet("a", "b", new Date(), "d", null);
        } catch (IllegalArgumentException e) {
            fail("The profileUrl is allowed to be null.");
        }
    }
    
    /**
     * Test that the constructor does not throw an exception when the profileUrl
     * is set to null.
     */
    public void testConstructorWithEmptyProfileUrl() {
        try {
            new Tweet("a", "b", new Date(), "d", "");
        } catch (IllegalArgumentException e) {
            fail("The profileUrl is allowed to be empty.");
        }
    }
    
    /**
     * Test that after giving valid values to the constructor, no exceptions are
     * thrown and the data held in the object is as expected.
     */
    public void testWithValidValues() {
        final Date time = new Date();
        final Tweet tweet = new Tweet("This is an example tweet.", "A user",
                time, "http://example.com/image.png",
                "http://example.com/profile");
        
        assertEquals("This is an example tweet.", tweet.getBody());
        assertEquals("A user", tweet.getDisplayName());
        assertEquals(time, tweet.getTime());
        assertEquals("http://example.com/image.png",
                tweet.getProfileImageUrl());
        assertEquals("http://example.com/profile", tweet.getProfileUrl());
    }
    
    /**
     * Test that the Comparator in the Tweet correctly returns a negative value
     * when the Tweet object sent in to compare with is null.
     */
    public void testComparatorWithNullCompareTo() {
        final Tweet tweet = new Tweet("a", "b", new Date(), "d", "e");
        assertTrue(tweet.compareTo(null) < 0);
    }
    
    /**
     * Test that the Comparator in the Tweet correctly returns 0 when the time
     * that both Tweets were tweeted as is identical.
     */
    public void testComparatorWithEqualTimes() {
        final Date time = new Date();
        final Tweet tweet1 = new Tweet("a", "b", time, "d", "e");
        final Tweet tweet2 = new Tweet("z", "y", time, "w", "v");
        
        assertTrue(tweet1.compareTo(tweet2) == 0);
    }
    
    /**
     * Test that the Comparator in the Tweet correctly orders two Tweets when
     * they have different timestamps.
     */
    public void testComparatorWithDifferentTimes() {
        GregorianCalendar cal = new GregorianCalendar(2014, 1, 26, 14, 30, 21);
        final Date time1 = cal.getTime();
        
        cal = new GregorianCalendar(2014, 1, 25, 9, 4, 53);
        final Date time2 = cal.getTime();
        
        final Tweet tweet1 = new Tweet("a", "b", time1, "d", "e");
        final Tweet tweet2 = new Tweet("z", "y", time2, "w", "v");
        
        assertTrue(tweet1.compareTo(tweet2) < 0);
        assertTrue(tweet2.compareTo(tweet1) > 0);
    }
}