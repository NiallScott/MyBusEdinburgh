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

import android.test.InstrumentationTestCase;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import uk.org.rivernile.android.fetchers.AssetFileFetcher;
import uk.org.rivernile.android.fetchers.readers.JSONFetcherStreamReader;

/**
 * Tests for TwitterParserImpl.
 * 
 * @author Niall Scott
 */
public class TwitterParserImplTests extends InstrumentationTestCase {
    
    private TwitterParserImpl parser;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        
        parser = new TwitterParserImpl();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        
        parser = null;
    }
    
    /**
     * Test that {@link TwitterParserImpl#getTweets(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly throws an IllegalArgumentExcaption when the fetcher is set as
     * null.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetTweetsWithNullFetcher() throws Exception {
        try {
            parser.getTweets(null);
        } catch (IllegalArgumentException e) {
            return;
        }
        
        fail("The fetcher is set as null, so an IllegalArgumentException "
                + "should be thrown.");
    }
    
    /**
     * Test that {@link TwitterParserImpl#getTweets(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly throws a TwitterException containing an IOException when the
     * fetcher is set to fetch a resource that does not exist.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetTweetsWithInvalidSource() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/generic/does_not_exist.json");
        
        try {
            parser.getTweets(fetcher);
        } catch (TwitterException e) {
            if (e.getCause() instanceof IOException) {
                return;
            }
        }
        
        fail("The fetcher is set to an incorrect resource, so an IOException "
                + "should be set as the cause in the TwitterException.");
    }
    
    /**
     * Test that {@link TwitterParserImpl#getTweets(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly throws a TwitterException containing a JSONException when the
     * fetcher is set to fetch a resource that does not exist.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetTweetsWithInvalidJson() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/generic/invalid.json");
        
        try {
            parser.getTweets(fetcher);
        } catch (TwitterException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }
        
        fail("The fetcher is set to return invalid JSON, so a JSONException "
                + "should be set as the cause in the TwitterException.");
    }
    
    /**
     * Test that {@link TwitterParserImpl#getTweets(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly throws a TwitterException containing a JSONException when the
     * fetcher returns a JSON object rather than a JSON array.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetTweetsWithJsonObject() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/generic/empty_object.json");
        
        try {
            parser.getTweets(fetcher);
        } catch (TwitterException e) {
            if (e.getCause() instanceof JSONException) {
                return;
            }
        }
        
        fail("The fetcher is set to get a JSON object, but a JSON array is "
                + "expected. A JSONException should be set as the cause in the "
                + "TwitterException.");
    }
    
    /**
     * Test that {@link TwitterParserImpl#getTweets(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly returns an empty list if an empty JSON array is fetched.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetTweetsWithEmptyJsonArray() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/generic/empty_array.json");
        final List<Tweet> tweets = parser.getTweets(fetcher);
        
        assertNotNull(tweets);
        assertTrue(tweets.isEmpty());
    }
    
    /**
     * Test that {@link TwitterParserImpl#getTweets(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly returns an empty list when the JSON array only contains a
     * single invalid tweet.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetTweetsWithSingleInvalidTweet() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/twitter/get_tweets_single_invalid.json");
        final List<Tweet> tweets = parser.getTweets(fetcher);
        
        assertNotNull(tweets);
        assertTrue(tweets.isEmpty());
    }
    
    /**
     * Test that {@link TwitterParserImpl#getTweets(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly returns an empty list when the JSON array only contains
     * multiple invalid tweets.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetTweetsWithMultipleInvalidTweets() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/twitter/get_tweets_multiple_invalid.json");
        final List<Tweet> tweets = parser.getTweets(fetcher);
        
        assertNotNull(tweets);
        assertTrue(tweets.isEmpty());
    }
    
    /**
     * Test that {@link TwitterParserImpl#getTweets(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly returns a single tweet with a JSON array of 2 elements, one of
     * which is invalid.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetTweetsMultipleWithInvalidTweet() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/twitter/get_tweets_multiple_with_single_invalid"
                        + ".json");
        final List<Tweet> tweets = parser.getTweets(fetcher);
        
        assertNotNull(tweets);
        assertEquals(1, tweets.size());
        
        final Date time = new GregorianCalendar(2014, Calendar.JANUARY, 22,
                20, 25, 18).getTime();
        final Tweet tweet = tweets.get(0);
        
        assertEquals("This is an example tweet.", tweet.getBody());
        assertEquals(time, tweet.getTime());
    }
    
    /**
     * Test that {@link TwitterParserImpl#getTweets(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly returns 3 tweets which in turn have been parsed correctly. The
     * test data is deliberately in the wrong order. It will be verified as
     * having been correctly ordered while being parsed in this test.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetTweetsMultiple() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/twitter/get_tweets_multiple.json");
        final List<Tweet> tweets = parser.getTweets(fetcher);
        
        assertNotNull(tweets);
        assertEquals(3, tweets.size());
        
        final GregorianCalendar cal1 =
                new GregorianCalendar(2014, Calendar.JANUARY, 22, 20, 25, 18);
        final Tweet tweet1 = tweets.get(0);
        assertEquals("http://www.example.com This is an example tweet. "
                + "http://www.example2.com", tweet1.getBody());
        assertEquals("Twitter User", tweet1.getDisplayName());
        assertEquals(cal1.getTime(), tweet1.getTime());
        assertEquals("http://example.com/profile1.jpeg",
                tweet1.getProfileImageUrl());
        assertEquals("https://twitter.com/twitter_user",
                tweet1.getProfileUrl());
        
        final GregorianCalendar cal2 =
                new GregorianCalendar(2013, Calendar.DECEMBER, 10, 11, 1, 2);
        final Tweet tweet2 = tweets.get(1);
        assertEquals("Test tweet.", tweet2.getBody());
        assertEquals("Another Twitter User", tweet2.getDisplayName());
        assertEquals(cal2.getTime(), tweet2.getTime());
        assertEquals("http://example.com/profile2.jpeg",
                tweet2.getProfileImageUrl());
        assertEquals("https://twitter.com/tweeter", tweet2.getProfileUrl());
        
        final GregorianCalendar cal3 =
                new GregorianCalendar(2013, Calendar.NOVEMBER, 9, 3, 51, 42);
        final Tweet tweet3 = tweets.get(2);
        assertEquals("A tweet with a single URL. http://www.example3.com",
                tweet3.getBody());
        assertEquals("A tweeting person", tweet3.getDisplayName());
        assertEquals(cal3.getTime(), tweet3.getTime());
        assertEquals("http://example.com/profile3.jpeg",
                tweet3.getProfileImageUrl());
        assertEquals("https://twitter.com/my_twitter_handle",
                tweet3.getProfileUrl());
    }
    
    /**
     * Test that {@link TwitterParserImpl#getTweets(uk.org.rivernile.android.fetchers.Fetcher)}
     * correctly returns a List that is unmodifiable. If any methods are called
     * on the List to change its state, an UnsupportedOperationException should
     * be thrown.
     * 
     * @throws Exception There are no other exceptions expected from this test,
     * so if there are, let the TestCase fail the test when it intercepts them.
     */
    public void testGetTweetsListIsUnmodifiable() throws Exception {
        final AssetFileFetcher fetcher = new AssetFileFetcher(
                getInstrumentation().getContext(),
                "endpoints/generic/empty_array.json");
        final List<Tweet> tweets = parser.getTweets(fetcher);
        
        assertNotNull(tweets);
        
        try {
            tweets.add(new Tweet("a", "b", new Date(), "d", "e"));
        } catch (UnsupportedOperationException e) {
            return;
        }
        
        fail("The List that is returned from getTweets() should be an "
                + "unmodifiable List. That is, any calls to methods which "
                + "changes the contents of the List should thrown an "
                + "UnsupportedOperationException.");
    }
    
    /**
     * Test that {@link TwitterParserImpl#replaceUrls(java.lang.String, org.json.JSONArray)}
     * correctly returns null when the body is set as null.
     */
    public void testReplaceUrlsWithNullBody() {
        assertNull(TwitterParserImpl.replaceUrls(null, new JSONArray()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#replaceUrls(java.lang.String, org.json.JSONArray)}
     * correctly returns empty String when the body is set as empty String.
     */
    public void testReplaceUrlsWithEmptyBody() {
        assertEquals("", TwitterParserImpl.replaceUrls("", new JSONArray()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#replaceUrls(java.lang.String, org.json.JSONArray)}
     * correctly returns the input body as is when the JSON array is set to
     * null.
     */
    public void testReplaceUrlsWithNullJsonArray() {
        assertEquals("Example", TwitterParserImpl.replaceUrls("Example", null));
    }
    
    /**
     * Test that {@link TwitterParserImpl#replaceUrls(java.lang.String, org.json.JSONArray)}
     * correctly returns the input body as is when the JSON array is set as
     * empty.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testReplaceUrlsWithEmptyJsonArray() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/generic/empty_array.json");
        
        assertEquals("Example", TwitterParserImpl
                .replaceUrls("Example", reader.getJSONArray()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#replaceUrls(java.lang.String, org.json.JSONArray)}
     * correctly returns the input body as is when the URL field is missing from
     * the JSON object in the array.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testReplaceUrlsWithMissingUrl() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/replace_urls_missing_url.json");
        
        assertEquals("http://t.co/example Example", TwitterParserImpl
                .replaceUrls("http://t.co/example Example",
                        reader.getJSONArray()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#replaceUrls(java.lang.String, org.json.JSONArray)}
     * correctly replaces URLs with valid JSON when it can, and ignores those
     * without valid JSON.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testReplaceUrlsWithMissingUrlMultiple() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/replace_urls_missing_url_multiple.json");
        
        assertEquals("http://www.example.com http://t.co/example2 Example",
                TwitterParserImpl.replaceUrls("http://t.co/example "
                        + "http://t.co/example2 Example",
                                reader.getJSONArray()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#replaceUrls(java.lang.String, org.json.JSONArray)}
     * correctly returns the input body as is when the expanded URL field is
     * missing from the JSON object in the array.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testReplaceUrlsWithMissingExpandedUrl() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/replace_urls_missing_expanded_url.json");
        
        assertEquals("http://t.co/example Example", TwitterParserImpl
                .replaceUrls("http://t.co/example Example",
                        reader.getJSONArray()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#replaceUrls(java.lang.String, org.json.JSONArray)}
     * correctly replaces URLs with valid JSON when it can, and ignores those
     * without valid JSON.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testReplaceUrlsWithMissingExpandedUrlMultiple()
            throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/replace_urls_missing_expanded_url_multiple"
                + ".json");
        
        assertEquals("http://www.example.com http://t.co/example2 Example",
                TwitterParserImpl.replaceUrls("http://t.co/example "
                        + "http://t.co/example2 Example",
                                reader.getJSONArray()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns null when the JSON object is set as null.
     * 
     * @throws Exception No exceptions are expected from this test, so fail the
     * test if there are any.
     */
    public void testParseTweetWithNullJsonObject() throws Exception {
        assertNull(TwitterParserImpl.parseTweet(null));
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns null when the JSON object is empty.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithEmptyJsonObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/generic/empty_object.json");
        
        assertNull(TwitterParserImpl.parseTweet(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns null when the date field is missing.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithMissingDate() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_missing_date.json");
        
        assertNull(TwitterParserImpl.parseTweet(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns null when the date field is null.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithNullDate() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_null_date.json");
        
        assertNull(TwitterParserImpl.parseTweet(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns null when the date field is invalid.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithInvalidDate() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_invalid_date.json");
        
        assertNull(TwitterParserImpl.parseTweet(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns null when the user object is missing.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithMissingUserObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_missing_user.json");
        
        assertNull(TwitterParserImpl.parseTweet(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns null when the user object is null.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithNullUserObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_null_user.json");
        
        assertNull(TwitterParserImpl.parseTweet(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns null when the body is null.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithMissingBody() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_missing_body.json");
        
        assertNull(TwitterParserImpl.parseTweet(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns null when the body is null.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithNullBody() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_null_body.json");
        
        assertNull(TwitterParserImpl.parseTweet(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns null when the display name is missing.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithMissingDisplayName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_missing_display_name.json");
        
        assertNull(TwitterParserImpl.parseTweet(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns null when the display name is null.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithNullDisplayName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_null_display_name.json");
        
        assertNull(TwitterParserImpl.parseTweet(reader.getJSONObject()));
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns a valid Tweet object, even when the profile image URL
     * is missing.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithMissingProfileImageUrl() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_missing_profile_image_url.json");
        final Tweet tweet = TwitterParserImpl
                .parseTweet(reader.getJSONObject());
        
        assertNotNull(tweet);
        assertNull(tweet.getProfileImageUrl());
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns a valid Tweet object, even when the profile image URL
     * is null.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithNullProfileImageUrl() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_null_profile_image_url.json");
        final Tweet tweet = TwitterParserImpl
                .parseTweet(reader.getJSONObject());
        
        assertNotNull(tweet);
        assertNull(tweet.getProfileImageUrl());
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns a valid Tweet object, even when the screen name is
     * missing.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithMissingScreenName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_missing_screen_name.json");
        final Tweet tweet = TwitterParserImpl
                .parseTweet(reader.getJSONObject());
        
        assertNotNull(tweet);
        assertNull(tweet.getProfileUrl());
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns a valid Tweet object, even when the screen name is
     * is null.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithNullScreenName() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_null_screen_name.json");
        final Tweet tweet = TwitterParserImpl
                .parseTweet(reader.getJSONObject());
        
        assertNotNull(tweet);
        assertNull(tweet.getProfileUrl());
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns a valid Tweet object, even when the entities object is
     * missing.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithMissingEntitiesObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_missing_entities_object.json");
        final Tweet tweet = TwitterParserImpl
                .parseTweet(reader.getJSONObject());
        
        assertNotNull(tweet);
        assertEquals("http://t.co/example1 This is an example tweet.",
                tweet.getBody());
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns a valid Tweet object, even when the entities object is
     * null.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithNullEntitiesObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_null_entities_object.json");
        final Tweet tweet = TwitterParserImpl
                .parseTweet(reader.getJSONObject());
        
        assertNotNull(tweet);
        assertEquals("http://t.co/example1 This is an example tweet.",
                tweet.getBody());
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns a valid Tweet object, even when the urls object is
     * missing.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithMissingUrlsObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_missing_urls_object.json");
        final Tweet tweet = TwitterParserImpl
                .parseTweet(reader.getJSONObject());
        
        assertNotNull(tweet);
        assertEquals("http://t.co/example1 This is an example tweet.",
                tweet.getBody());
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns a valid Tweet object, even when the urls object is
     * null.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetWithNullUrlsObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_null_urls_object.json");
        final Tweet tweet = TwitterParserImpl
                .parseTweet(reader.getJSONObject());
        
        assertNotNull(tweet);
        assertEquals("http://t.co/example1 This is an example tweet.",
                tweet.getBody());
    }
    
    /**
     * Test that {@link TwitterParserImpl#parseTweet(org.json.JSONObject)}
     * correctly returns a valid Tweet object and the data contained within is
     * all as expected when the JSON is all as expected.
     * 
     * @throws Exception When there was a problem reading the JSON file from
     * assets. This is not expected.
     */
    public void testParseTweetFromCompliantResponse() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/twitter/parse_tweet_compliant.json");
        final Tweet tweet = TwitterParserImpl
                .parseTweet(reader.getJSONObject());
        final Date time = new GregorianCalendar(2014, Calendar.JANUARY, 22,
                20, 25, 18).getTime();
        
        assertNotNull(tweet);
        assertEquals("http://www.example.com This is an example tweet. "
                + "http://www.example2.com", tweet.getBody());
        assertEquals("Twitter User", tweet.getDisplayName());
        assertEquals(time, tweet.getTime());
        assertEquals("http://example.com/profile.jpeg",
                tweet.getProfileImageUrl());
        assertEquals("https://twitter.com/twitter_user", tweet.getProfileUrl());
    }
    
    /**
     * Get the data from assets.
     * 
     * @param filePath The file path in the assets to get data from.
     * @return The data.
     * @throws IOException When there was a problem reading the data.
     */
    private JSONFetcherStreamReader getReaderAfterFetchingData(
            final String filePath) throws IOException{
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(getInstrumentation().getContext(),
                        filePath);
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        fetcher.executeFetcher(reader);
        
        return reader;
    }
}