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

package uk.org.rivernile.android.bustracker.ui.news;

import android.database.DataSetObserver;
import android.test.AndroidTestCase;
import android.view.View;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import uk.org.rivernile.android.bustracker.parser.twitter.Tweet;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * Tests for TweetAdapter.
 * 
 * @author Niall Scott
 */
public class TweetAdapterTests extends AndroidTestCase {
    
    private TweetAdapter adapter;
    private boolean notifyCalled;

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
        
        adapter = new TweetAdapter(getContext());
        notifyCalled = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
        
        adapter = null;
    }
    
    /**
     * Test that the constructor correctly throws an IllegalArgumentException
     * when the context is set as null.
     */
    public void testConstructorWithNullContext() {
        try {
            new TweetAdapter(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The context is set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that the adapter methods return expected values in the default,
     * freshly constructed state.
     */
    public void testDefaultState() {
        assertEquals(0, adapter.getCount());
        assertNull(adapter.getItem(0));
        assertEquals(getContext(), adapter.getContext());
        assertNull(adapter.getTweets());
    }
    
    /**
     * Test that the adapter methods return expected values when supplied with
     * canned data.
     */
    public void testWithCannedData() {
        adapter.setTweets(getCannedData());
        
        assertEquals(3, adapter.getCount());
        assertNotNull(adapter.getTweets());
        
        final Tweet tweet1 = adapter.getItem(0);
        final Tweet tweet2 = adapter.getItem(1);
        final Tweet tweet3 = adapter.getItem(2);
        
        assertNotNull(tweet1);
        assertNotNull(tweet2);
        assertNotNull(tweet3);
        assertNull(adapter.getItem(3));
        
        assertEquals("a", tweet1.getBody());
        assertEquals("f", tweet2.getBody());
        assertEquals("k", tweet3.getBody());
    }
    
    /**
     * Test that {@link TweetAdapter#isEnabled(int)} always returns false.
     */
    public void testIsEnabledAlwaysReturnsFalse() {
        adapter.setTweets(getCannedData());
        
        for (int i = 0; i < 10; i++) {
            assertFalse("Item " + i + " is not false.", adapter.isEnabled(i));
        }
    }
    
    /**
     * Test that the ID returned by {@link TweetAdapter#getItemId(int)} matches
     * the position sent in.
     */
    public void testGetItemId() {
        adapter.setTweets(getCannedData());
        
        for (int i = 0; i < 10; i++) {
            assertEquals("Item " + i + " returned an incorrect ID.", i,
                    adapter.getItemId(i));
        }
    }
    
    /**
     * Test that the state correctly changes between calls to
     * {@link TweetAdapter#setTweets(java.util.List)}. Also, test that
     * {@link TweetAdapter#notifyDataSetChanged()} is being called each time.
     */
    public void testSetTweets() {
        adapter.registerDataSetObserver(new DataSetObserver() {
            @Override
            public void onChanged() {
                notifyCalled = true;
            }
        });
        
        adapter.setTweets(getCannedData());
        assertTrue(notifyCalled);
        notifyCalled = false;
        
        adapter.setTweets(null);
        assertTrue(notifyCalled);
        notifyCalled = false;
        assertEquals(0, adapter.getCount());
        assertNull(adapter.getTweets());
        assertNull(adapter.getItem(0));
        
        final List<Tweet> tweets = getCannedData();
        tweets.add(new Tweet("p", "q", new Date(), "s", "t"));
        adapter.setTweets(tweets);
        assertTrue(notifyCalled);
        assertEquals(4, adapter.getCount());
        assertNotNull(adapter.getTweets());
        assertNotNull(adapter.getItem(3));
        assertNull(adapter.getItem(4));
    }
    
    /**
     * Test that the View returned by
     * {@link TweetAdapter#getView(int, android.view.View, android.view.ViewGroup)}
     * has the expected Views. Also check that Views are being recycled
     * properly.
     */
    public void testGetView() {
        adapter.setTweets(getCannedData());
        
        final View view1 = adapter.getView(0, null, null);
        final TextView txtBody = (TextView) view1.findViewById(R.id.txtBody);
        assertNotNull(txtBody);
        assertNotNull(view1.findViewById(R.id.txtInfo));
        assertEquals("a", txtBody.getText().toString());
        
        final View view2 = adapter.getView(1, view1, null);
        assertTrue(view1 == view2);
        assertEquals("f", txtBody.getText().toString());
    }
    
    /**
     * A utility method for getting pre-made canned data.
     * 
     * @return Get a List of canned Tweets.
     */
    private List<Tweet> getCannedData() {
        final List<Tweet> tweets = new ArrayList<Tweet>();
        tweets.add(new Tweet("a", "b", new Date(), "d", "e"));
        tweets.add(new Tweet("f", "g", new Date(), "i", "j"));
        tweets.add(new Tweet("k", "l", new Date(), "n", "o"));
        
        return tweets;
    }
}