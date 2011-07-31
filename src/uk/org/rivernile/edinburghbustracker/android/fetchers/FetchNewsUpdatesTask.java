/*
 * Copyright (C) 2010 - 2011 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.fetchers;

import android.os.AsyncTask;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import uk.org.rivernile.edinburghbustracker.android.NewsUpdatesActivity;

/**
 * This class fetches the latest updates from the Twitter list of the URL
 * specified in REQUEST_URL. It then feeds this information back to the handler
 * defined in the 'handler' field.
 *
 * @author Niall Scott
 */
public class FetchNewsUpdatesTask {

    private final static String REQUEST_URL = "http://api.twitter.com/1/" +
            "NiallScott/lists/bus-tracker-updates/statuses.json";

    private static FetchNewsUpdatesTask instance = null;
    private NewsEvent handler;
    private String newsJsonString;
    private long lastRequestTime = 0;
    private GetNewsTask task;

    /**
     * This constructor is intentionally left blank and private, this class
     * can only be instantiated from within.
     */
    private FetchNewsUpdatesTask() {
        // Nothing to do here
    }

    /**
     * Get an instance of this class. A handler object needs to be specified so
     * this class knows where to return the server data to.
     *
     * @param handler The object to fire data back to.
     * @return An instance of this class.
     */
    public static FetchNewsUpdatesTask getInstance(final NewsEvent handler) {
        if(instance == null) instance = new FetchNewsUpdatesTask();
        instance.setHandler(handler);
        return instance;
    }

    /**
     * Set the handler to fire the data back to. This method can even be called
     * when the main task of this class is executing. The handler may need to be
     * changed when the device screen orientation is changed.
     *
     * @param handler The object to fire data back to.
     */
    public void setHandler(final NewsEvent handler) {
        this.handler = handler;
    }

    /**
     * Get the object where data is fired back to.
     *
     * @return The callback object.
     */
    public NewsEvent getHandler() {
        return handler;
    }

    /**
     * Get the JSON String this object holds. Be prepared to handle a null or
     * 0 length String.
     *
     * @return The JSON String for this object.
     */
    public String getJSONString() {
        return newsJsonString;
    }

    /**
     * Get the timestamp (in milliseconds) that the Twitter data was last
     * refreshed at.
     *
     * @return The timestamp (in milliseconds) the data was last refreshed at.
     */
    public long getLastRequestTime() {
        return lastRequestTime;
    }

    /**
     * When the thread within this class is executing, true will be returned
     * otherwise false will be returned.
     *
     * @return The execution state of the thread in this class.
     */
    public boolean isExecuting() {
        return task != null ?
                task.getStatus() == AsyncTask.Status.RUNNING : false;
    }

    /**
     * Contact the server and get the JSON string. This data will be fired back
     * to the Handler object specified by getInstance() or setHandler().
     * 
     * @return true when the task was started, false when the task couldn't be
     * started because a task is already running.
     */
    public boolean doTask() {
        if(task == null) {
            task = new GetNewsTask();
            task.execute(new Void[] { });
            return true;
        } else {
            return false;
        }
    }
    
    private class GetNewsTask extends AsyncTask<Void, Void, String> {
        
        private int error = -1;
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPreExecute() {
            if(handler != null) handler.onPreExecute();
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected String doInBackground(final Void... v) {
            try {
                URL u = new URL(REQUEST_URL);
                URLConnection con = u.openConnection();
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String jsonString = "";
                String lineIn;
                while((lineIn = in.readLine()) != null) {
                    jsonString = jsonString + lineIn;
                }
                in.close();

                lastRequestTime = System.currentTimeMillis();

                return jsonString;
            } catch(MalformedURLException e) {
                error = NewsUpdatesActivity.ERROR_URLERR;
            } catch(IOException e) {
                error = NewsUpdatesActivity.ERROR_IOERR;
            }
            
            return null;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void onPostExecute(final String result) {
            newsJsonString = result;
            if(handler != null) {
                if(error < 0) {
                    handler.onNewsAvailable(result);
                } else {
                    handler.onFetchError(error);
                }
            }
            task = null;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void onCancelled() {
            task = null;
        }
    }
}