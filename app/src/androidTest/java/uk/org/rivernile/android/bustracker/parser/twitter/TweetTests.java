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

package uk.org.rivernile.android.bustracker.parser.twitter;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

import androidx.test.runner.AndroidJUnit4;

import java.util.Date;
import java.util.GregorianCalendar;

import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link Tweet}.
 * 
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class TweetTests {

    /**
     * Test that {@link Tweet.Builder#build()} throws an {@link IllegalArgumentException} when the
     * tweet body is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithNullBody() {
        new Tweet.Builder()
                .setBody(null)
                .setDisplayName("b")
                .setTime(new Date())
                .setProfileImageUrl("c")
                .setProfileUrl("d")
                .build();
    }

    /**
     * Test that {@link Tweet.Builder#build()} throws an {@link IllegalArgumentException} when the
     * tweet body is set to empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithEmptyBody() {
        new Tweet.Builder()
                .setBody("")
                .setDisplayName("b")
                .setTime(new Date())
                .setProfileImageUrl("c")
                .setProfileUrl("d")
                .build();
    }

    /**
     * Test that {@link Tweet.Builder#build()} throws an {@link IllegalArgumentException} when the
     * display name is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithNullDisplayName() {
        new Tweet.Builder()
                .setBody("a")
                .setDisplayName(null)
                .setTime(new Date())
                .setProfileImageUrl("c")
                .setProfileUrl("d")
                .build();
    }

    /**
     * Test that {@link Tweet.Builder#build()} throws an {@link IllegalArgumentException} when the
     * display name is set to empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithEmptyDisplayName() {
        new Tweet.Builder()
                .setBody("a")
                .setDisplayName("")
                .setTime(new Date())
                .setProfileImageUrl("c")
                .setProfileUrl("d")
                .build();
    }

    /**
     * Test that {@link Tweet.Builder#build()} throws an {@link IllegalArgumentException} when the
     * time is set to {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testBuildWithNullTime() {
        new Tweet.Builder()
                .setBody("a")
                .setDisplayName("b")
                .setTime(null)
                .setProfileImageUrl("c")
                .setProfileUrl("d")
                .build();
    }

    /**
     * Test that no exception is thrown when {@link Tweet.Builder#build()} is called with the
     * profile image URL set to {@code null}.
     */
    @Test
    public void testBuildWithNullProfileImageUrl() {
        new Tweet.Builder()
                .setBody("a")
                .setDisplayName("b")
                .setTime(new Date())
                .setProfileImageUrl(null)
                .setProfileUrl("d")
                .build();
    }

    /**
     * Test that no exception is thrown when {@link Tweet.Builder#build()} is called with the
     * profile image URL set to empty.
     */
    @Test
    public void testBuildWithEmptyProfileImageUrl() {
        new Tweet.Builder()
                .setBody("a")
                .setDisplayName("b")
                .setTime(new Date())
                .setProfileImageUrl("")
                .setProfileUrl("d")
                .build();
    }

    /**
     * Test that no exception is thrown when {@link Tweet.Builder#build()} is called with the
     * profile URL set to {@code null}.
     */
    @Test
    public void testBuildWithNullProfileUrl() {
        new Tweet.Builder()
                .setBody("a")
                .setDisplayName("b")
                .setTime(new Date())
                .setProfileImageUrl("c")
                .setProfileUrl(null)
                .build();
    }

    /**
     * Test that no exception is thrown when {@link Tweet.Builder#build()} is called with the
     * profile URL set to empty.
     */
    @Test
    public void testBuildWithEmptyProfileUrl() {
        new Tweet.Builder()
                .setBody("a")
                .setDisplayName("b")
                .setTime(new Date())
                .setProfileImageUrl("c")
                .setProfileUrl("")
                .build();
    }
    
    /**
     * Test that after giving valid values to the {@link Tweet.Builder}, no exceptions are thrown
     * and the getters return the expected data.
     */
    @Test
    public void testWithValidValues() {
        final Date time = new Date();
        final Tweet tweet = new Tweet.Builder()
                .setBody("This is an example tweet.")
                .setDisplayName("A user")
                .setTime(time)
                .setProfileImageUrl("http://example.com/image.png")
                .setProfileUrl("http://example.com/profile")
                .build();

        assertEquals("This is an example tweet.", tweet.getBody());
        assertEquals("A user", tweet.getDisplayName());
        assertEquals(time, tweet.getTime());
        assertEquals("http://example.com/image.png",
                tweet.getProfileImageUrl());
        assertEquals("http://example.com/profile", tweet.getProfileUrl());
    }
    
    /**
     * Test that the {@link java.util.Comparator} in the {@link Tweet} correctly returns {@code 0}
     * when the time that both {@link Tweet}s were tweeted at is identical.
     */
    @Test
    public void testComparatorWithEqualTimes() {
        final Date time = new Date();
        final Tweet tweet1 = new Tweet.Builder()
                .setBody("a")
                .setDisplayName("b")
                .setTime(time)
                .setProfileImageUrl("d")
                .setProfileUrl("e")
                .build();
        final Tweet tweet2 = new Tweet.Builder()
                .setBody("z")
                .setDisplayName("y")
                .setTime(time)
                .setProfileImageUrl("w")
                .setProfileUrl("v")
                .build();

        assertEquals(0, tweet1.compareTo(tweet2));
    }
    
    /**
     * Test that the {@link java.util.Comparator} in the {@link Tweet} correctly orders two
     * {@link Tweet}s when they have different timestamps.
     */
    @Test
    public void testComparatorWithDifferentTimes() {
        GregorianCalendar cal = new GregorianCalendar(2014, 1, 26, 14, 30, 21);
        final Date time1 = cal.getTime();
        
        cal = new GregorianCalendar(2014, 1, 25, 9, 4, 53);
        final Date time2 = cal.getTime();

        final Tweet tweet1 = new Tweet.Builder()
                .setBody("a")
                .setDisplayName("b")
                .setTime(time1)
                .setProfileImageUrl("d")
                .setProfileUrl("e")
                .build();
        final Tweet tweet2 = new Tweet.Builder()
                .setBody("z")
                .setDisplayName("y")
                .setTime(time2)
                .setProfileImageUrl("w")
                .setProfileUrl("v")
                .build();
        
        assertTrue(tweet1.compareTo(tweet2) < 0);
        assertTrue(tweet2.compareTo(tweet1) > 0);
    }

    /**
     * Test that {@link Tweet#equals(Object)} and {@link Tweet#hashCode()} behave correctly.
     */
    @Test
    public void testEqualsAndHashCode() {
        final Date date1 = new GregorianCalendar(2014, 1, 26, 14, 30, 21).getTime();
        final Date date2 = new GregorianCalendar(2014, 1, 26, 15, 30, 21).getTime();
        final Tweet tweet1 = new Tweet.Builder()
                .setBody("Body")
                .setDisplayName("User")
                .setTime(date1)
                .setProfileImageUrl("a")
                .setProfileUrl("b")
                .build();
        final Tweet tweet2 = new Tweet.Builder()
                .setBody("Body")
                .setDisplayName("User")
                .setTime(date1)
                .setProfileImageUrl("a")
                .setProfileUrl("b")
                .build();
        final Tweet tweet3 = new Tweet.Builder()
                .setBody("Body 2")
                .setDisplayName("User")
                .setTime(date1)
                .setProfileImageUrl("a")
                .setProfileUrl("b")
                .build();
        final Tweet tweet4 = new Tweet.Builder()
                .setBody("Body")
                .setDisplayName("User 2")
                .setTime(date1)
                .setProfileImageUrl("a")
                .setProfileUrl("b")
                .build();
        final Tweet tweet5 = new Tweet.Builder()
                .setBody("Body")
                .setDisplayName("User")
                .setTime(date2)
                .setProfileImageUrl("a")
                .setProfileUrl("b")
                .build();

        assertTrue(tweet1.equals(tweet1));
        assertTrue(tweet1.equals(tweet2));
        assertFalse(tweet1.equals(tweet3));
        assertFalse(tweet1.equals(tweet4));
        assertFalse(tweet1.equals(tweet5));
        assertFalse(tweet1.equals(new Object()));
        assertFalse(tweet1.equals(null));

        assertEquals(tweet1.hashCode(), tweet1.hashCode());
        assertEquals(tweet1.hashCode(), tweet2.hashCode());
        assertNotEquals(tweet1.hashCode(), tweet3.hashCode());
        assertNotEquals(tweet1.hashCode(), tweet4.hashCode());
        assertNotEquals(tweet1.hashCode(), tweet5.hashCode());
    }
}