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

package uk.org.rivernile.android.bustracker.parser.twitter;

import android.annotation.SuppressLint;
import android.support.annotation.NonNull;
import android.text.TextUtils;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import uk.org.rivernile.android.fetchutils.fetchers.Fetcher;
import uk.org.rivernile.android.fetchutils.fetchers.readers.JSONFetcherStreamReader;

/**
 * This is the concrete implementation of {@link TwitterParser}. It fetches and parses a JSON
 * list of tweet objects and turns this in to a {@link List} of {@link Tweet}s.
 * 
 * @author Niall Scott
 */
public class TwitterParserImpl implements TwitterParser {
    
    @SuppressLint({"SimpleDateFormat"})
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
    
    private static final String TWITTER_BASE_URL = "https://twitter.com/";

    @NonNull
    @Override
    public List<Tweet> getTweets(@NonNull final Fetcher fetcher) throws TwitterException {
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        
        try {
            fetcher.executeFetcher(reader);
            
            return parseJsonArray(reader.getJSONArray());
        } catch (IOException e) {
            throw new TwitterException(e);
        } catch (JSONException e) {
            throw new TwitterException(e);
        }
    }
    
    /**
     * Parse a JSONArray of tweet objects to return a List of Tweets.
     * 
     * @param jTweets The JSONArray of tweet objects.
     * @return A List of Tweet objects. May be empty if there was no tweets, all
     * tweets contained errors or the JSON array was ill-formatted. This List is
     * unmodifiable - UnsupportedOperationException will be thrown if the state
     * is attempted to be changed.
     * @throws JSONException When there was a problem parsing the JSON response.
     */
    private static List<Tweet> parseJsonArray(final JSONArray jTweets)
            throws JSONException {
        final List<Tweet> tweets = new ArrayList<Tweet>();
        
        if (jTweets != null) {
            final int len = jTweets.length();
            Tweet tweet;
            for (int i = 0; i < len; i++) {
                try {
                    tweet = parseTweet(jTweets.getJSONObject(i));
                    if (tweet != null) {
                        tweets.add(tweet);
                    }
                } catch (JSONException e) {
                    // Nothing to do here. Ignore the tweet if it error'd.
                }
            }
            
            // The response from the server should already be in time-order, but
            // carry out this step to ensure this.
            Collections.sort(tweets);
        }
        
        // Return an unmodifiable List as there is no reason for this List to
        // manipulated elsewhere.
        return Collections.unmodifiableList(tweets);
    }
    
    /**
     * Parse a single Tweet from the JSON array of Tweets.
     * 
     * @param jTweet The JSON representation of the Tweet object. If this is
     * null, null will be returned.
     * @return A Tweet object representing the data in jTweet. Null may be
     * returned in error circumstances.
     */
    protected static Tweet parseTweet(final JSONObject jTweet) {
        if (jTweet == null) {
            return null;
        }
        
        try {
            // Date and time.
            final Date time;
            try {
                time = DATE_FORMAT.parse(jTweet.getString("created_at"));
            } catch (ParseException e) {
                return null;
            }
            
            // Tweet body.
            final String body = jTweet.getString("text");
            if (JSONObject.NULL.toString().equals(body)) {
                return null;
            }

            // User.
            final JSONObject jUser = jTweet.getJSONObject("user");
            // Display name.
            final String displayName = jUser.getString("name");
            if (JSONObject.NULL.toString().equals(displayName)) {
                return null;
            }
            
            // Entities and URLs.
            final JSONObject jEntities = jTweet.optJSONObject("entities");
            final JSONArray jUrls = jEntities != null ?
                    jEntities.optJSONArray("urls") : null;

            // Screen name.
            String screenName = jUser.optString("screen_name", null);
            if (JSONObject.NULL.toString().equals(screenName)) {
                screenName = null;
            }
            
            // Profile URL.
            final String profileUrl = screenName != null ?
                    TWITTER_BASE_URL + screenName : null;
            
            // Profile image URL.
            String profileImageUrl = jUser.optString("profile_image_url", null);
            if (JSONObject.NULL.toString().equals(profileImageUrl)) {
                profileImageUrl = null;
            }

            return new Tweet(replaceUrls(body, jUrls), displayName, time,
                    profileImageUrl, profileUrl);
        } catch (JSONException e) {
            return null;
        }
    }
    
    /**
     * Replace URLs in the tweet body from their shortened form to the full URL
     * as given by the Twitter API.
     * 
     * @param body The body of the tweet.
     * @param jUrls A JSONArray containing the URL objects.
     * @return A new version of the body with the URLs replaced. May be the same
     * as the body that got passed in if there were no URLs to replace, the body
     * was empty or null, or if the jUrls array is null.
     */
    protected static String replaceUrls(String body, final JSONArray jUrls) {
        if (TextUtils.isEmpty(body) || jUrls == null) {
            return body;
        }
        
        JSONObject jUrl;
        final int len = jUrls.length();
        String url, expandedUrl;
        
        for (int i = 0; i < len; i++) {
            try {
                jUrl = jUrls.getJSONObject(i);
                
                url = jUrl.getString("url");
                if (JSONObject.NULL.toString().equals(url)) {
                    continue;
                }
                
                expandedUrl = jUrl.getString("expanded_url");
                if (JSONObject.NULL.toString().equals(expandedUrl)) {
                    continue;
                }
                
                body = body.replaceAll("\\b" + url + "\\b", expandedUrl);
            } catch (JSONException e) {
                // Nothing to do here.
            }
        }
        
        return body;
    }
}