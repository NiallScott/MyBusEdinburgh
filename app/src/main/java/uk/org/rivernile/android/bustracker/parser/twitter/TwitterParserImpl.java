/*
 * Copyright (C) 2014 - 2020 Niall 'Rivernile' Scott
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
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import android.text.TextUtils;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import javax.inject.Inject;
import javax.inject.Singleton;

import uk.org.rivernile.android.fetchutils.fetchers.Fetcher;
import uk.org.rivernile.android.fetchutils.fetchers.readers.JSONFetcherStreamReader;
import uk.org.rivernile.android.utils.JSONUtils;

/**
 * This is the concrete implementation of {@link TwitterParser}. It fetches and parses a JSON
 * list of tweet objects and turns this in to a {@link List} of {@link Tweet}s.
 * 
 * @author Niall Scott
 */
@Singleton
public class TwitterParserImpl implements TwitterParser {
    
    @SuppressLint({"SimpleDateFormat"})
    private static final SimpleDateFormat DATE_FORMAT =
            new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy", Locale.ENGLISH);
    
    private static final String TWITTER_BASE_URL = "https://twitter.com/";

    @Inject
    TwitterParserImpl() {
        // Constructor defined to allow direct injection.
    }

    @NonNull
    @Override
    public List<Tweet> getTweets(@NonNull final Fetcher fetcher) throws TwitterException {
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        
        try {
            fetcher.executeFetcher(reader);
            
            return parseJsonArray(reader.getJSONArray());
        } catch (IOException | JSONException e) {
            throw new TwitterException(e);
        }
    }
    
    /**
     * Parse a {@link JSONArray} of tweet objects to return a {@link List} of {@link Tweet}s.
     * 
     * @param jTweets The {@link JSONArray} of tweet objects.
     * @return A {@link List} of {@link Tweet} objects. The {@link List} is unmodifiable. The
     * {@link List} may be empty if there were no tweets or there was problems parsing the tweets.
     */
    @NonNull
    private static List<Tweet> parseJsonArray(@NonNull final JSONArray jTweets) {
        final int size = jTweets.length();

        if (size > 0) {
            final ArrayList<Tweet> tweets = new ArrayList<>(size);

            for (int i = 0; i < size; i++) {
                try {
                    final Tweet tweet = parseTweet(jTweets.getJSONObject(i));

                    if (tweet != null) {
                        tweets.add(tweet);
                    }
                } catch (JSONException e) {
                    // Nothing to do here. Ignore if there was an error.
                }
            }

            Collections.sort(tweets);

            return Collections.unmodifiableList(tweets);
        } else {
            return Collections.emptyList();
        }
    }
    
    /**
     * Parse a single {@link Tweet} from the {@link JSONArray} of tweets.
     * 
     * @param jTweet The JSON representation of the {@link Tweet} object. If this is
     * {@code null}, {@code null} will be returned.
     * @return A {@link Tweet} object representing the data in {@code jTweet}. {@code null} may be
     * returned in error circumstances.
     */
    @Nullable
    static Tweet parseTweet(@NonNull final JSONObject jTweet) {
        try {
            final Tweet.Builder builder = new Tweet.Builder();

            try {
                builder.setTime(DATE_FORMAT.parse(jTweet.getString("created_at")));
            } catch (ParseException e) {
                return null;
            }

            final String body = JSONUtils.getString(jTweet, "text");
            final JSONObject jEntities = jTweet.optJSONObject("entities");
            final JSONArray jUrls = jEntities != null ? jEntities.optJSONArray("urls") : null;

            builder.setBody(replaceUrls(body, jUrls));

            final JSONObject jUser = jTweet.getJSONObject("user");
            builder.setDisplayName(JSONUtils.getString(jUser, "name"))
                    .setProfileImageUrl(JSONUtils.optString(jUser, "profile_image_url_https",
                            null));

            final String screenName = JSONUtils.optString(jUser, "screen_name", null);

            if (!TextUtils.isEmpty(screenName)) {
                builder.setProfileUrl(TWITTER_BASE_URL + screenName);
            }

            return builder.build();
        } catch (JSONException | IllegalArgumentException e) {
            return null;
        }
    }
    
    /**
     * Replace URLs in the tweet body from their shortened form to the full URL as given by the
     * Twitter API.
     * 
     * @param body The body of the tweet.
     * @param jUrls A {@link JSONArray} containing the URL objects.
     * @return A new version of the body with the URLs replaced. May be the same as the body that
     * got passed in if there were no URLs to replace, the body was empty or {@code null}, or if
     * the {@code jUrls} array is {@code null}.
     */
    @Nullable
    static String replaceUrls(@Nullable String body, final JSONArray jUrls) {
        if (TextUtils.isEmpty(body) || jUrls == null) {
            return body;
        }
        
        final int len = jUrls.length();
        
        for (int i = 0; i < len; i++) {
            try {
                final JSONObject jUrl = jUrls.getJSONObject(i);
                final String url = JSONUtils.getString(jUrl, "url");

                if (TextUtils.isEmpty(url)) {
                    continue;
                }
                
                final String expandedUrl = JSONUtils.getString(jUrl, "expanded_url");

                if (TextUtils.isEmpty(expandedUrl)) {
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