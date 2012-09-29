/*
 * Copyright (C) 2012 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.twitter;

import android.content.Context;
import android.text.Html;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import uk.org.rivernile.android.utils.SimpleResultLoader;

/**
 * This Loader handles fetching data from Twitter to display as news items
 * inside the application. This Loader does not expect any arguments.
 * 
 * @author Niall Scott
 * @see SimpleResultLoader
 */
public class TwitterUpdatesLoader
        extends SimpleResultLoader<TwitterLoaderResult> {
    
    /** Error code for when no data is available. */
    public static final byte ERROR_NODATA = 0;
    /** Error code for when there was a problem parsing the incoming data. */
    public static final byte ERROR_PARSEERR = 1;
    /** Error code for an IO error, such as a socket error or reading error. */
    public static final byte ERROR_IOERR = 2;
    /** There was a problem parsing the URL. Should never happen. */
    public static final byte ERROR_URLERR = 3;
    
    private final static String REQUEST_URL = "http://api.twitter.com/1/" +
            "lists/statuses.json?slug=bus-tracker-updates&" +
            "owner_screen_name=NiallScott&per_page=30";
    
    /**
     * Create a new TwitterUpdatesLoader.
     * 
     * @param context A Context object.
     */
    public TwitterUpdatesLoader(final Context context) {
        super(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public TwitterLoaderResult loadInBackground() {
        final ArrayList<TwitterNewsItem> items =
                new ArrayList<TwitterNewsItem>();
        byte error = -1;

        try {
            // Create URL object.
            final URL u = new URL(REQUEST_URL);
            // Open the connection to the HTTP server.
            final URLConnection con = u.openConnection();
            // Buffer the input.
            final BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            // All text will be put in to a StringBuilder.
            final StringBuilder sb = new StringBuilder();
            String lineIn;
            
            // Keep accepting input until there is no more.
            while((lineIn = in.readLine()) != null) {
                sb.append(lineIn);
            }
            
            // Remember to release resources.
            in.close();

            // Parse the JSON.
            error = parseJSON(items, sb.toString());
        } catch(MalformedURLException e) {
            error = ERROR_URLERR;
        } catch(IOException e) {
            error = ERROR_IOERR;
        } catch(JSONException e) {
            error = ERROR_PARSEERR;
        }
        
        // Error codes are >= 0, so if the error code matches this, create an
        // error object, otherwise create a result object.
        if(error < 0) {
            return new TwitterLoaderResult(items);
        } else {
            return new TwitterLoaderResult(error);
        }
    }
    
    /**
     * Parse the JSON which was received from Twitter.
     * 
     * @param items The ArrayList to put all TwitterNewsItems in to.
     * @param jsonString The source JSON String.
     * @throws JSONException If a parsing error occurs.
     */
    private byte parseJSON(final ArrayList<TwitterNewsItem> items,
            final String jsonString) throws JSONException {
        // Make sure that the JSON String exists.
        if(jsonString == null || jsonString.length() == 0) {
            return ERROR_NODATA;
        }

        // Twitter starts off as a JSON Array.
        final JSONArray ja = new JSONArray(jsonString);
        // Get the number of items.
        final int len = ja.length();
        JSONObject joTweet, joUser;

        for(int i = 0; i < len; i++) {
            // Get the tweet object.
            joTweet = ja.getJSONObject(i);
            // Get the user object.
            joUser = joTweet.getJSONObject("user");

            // Create the TwitterNewsItem and add to the ArrayList.
            items.add(new TwitterNewsItem(
                    Html.fromHtml(joTweet.getString("text")).toString(),
                    joUser.getString("name"),
                    joTweet.getString("created_at")));
        }
        
        return -1;
    }
}