/*
 * Copyright (C) 2010 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * This class fetches the latest updates from the Twitter list of the URL
 * specified in REQUEST_URL. It then feeds this information back to the handler
 * defined in the 'handler' field.
 *
 * @author Niall Scott
 */
public class FetchNewsUpdatesTask implements Runnable {

    private final static String REQUEST_URL = "http://api.twitter.com/1/" +
            "NiallScott/lists/bus-tracker-updates/statuses.json";

    private static FetchNewsUpdatesTask instance = null;
    private Handler handler;
    private boolean executing = false;
    private String jsonString;
    private long lastRequestTime = 0;

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
     * @param handler The handler object to fire data back to.
     * @return An instance of this class.
     */
    public static FetchNewsUpdatesTask getInstance(final Handler handler) {
        if(instance == null) instance = new FetchNewsUpdatesTask();
        instance.setHandler(handler);
        return instance;
    }

    /**
     * Set the handler to fire the data back to. This method is thread safe.
     * This method can even be called when the main task of this class is
     * executing. The handler may need to be changed when the device screen
     * orientation is changed.
     *
     * @param handler The handler object to fire data back to.
     */
    public void setHandler(final Handler handler) {
        synchronized(this) {
            this.handler = handler;
        }
    }

    /**
     * Get the Handler object where data is fired back to.
     *
     * @return The Handler object.
     */
    public Handler getHandler() {
        synchronized(this) {
            return handler;
        }
    }

    /**
     * Get the JSON String this object holds. Be prepared to handle a null or
     * 0 length String.
     *
     * @return The JSON String for this object.
     */
    public String getJSONString() {
        return jsonString;
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
        return executing;
    }

    /**
     * Contact the server and get the JSON string. This data will be fired back
     * to the Handler object specified by getInstance() or setHandler().
     */
    public void doTask() {
        if(!executing) {
            executing = true;
            new Thread(this).start();
        }
    }

    /**
     * The thread in this class.
     */
    @Override
    public void run() {
        Message msg;
        Bundle b = new Bundle();
        try {
            URL u = new URL(REQUEST_URL);
            URLConnection con = u.openConnection();
            BufferedReader in = new BufferedReader(new InputStreamReader(
                    con.getInputStream()));
            jsonString = "";
            String lineIn;
            while((lineIn = in.readLine()) != null) {
                jsonString = jsonString + lineIn;
            }
            in.close();

            lastRequestTime = System.currentTimeMillis();

            if(getHandler() == null) return;
            b.putString("jsonString", jsonString);
            synchronized(this) {
                msg = handler.obtainMessage();
                msg.setData(b);
                handler.sendMessage(msg);
            }
        } catch(MalformedURLException e) {
            if(getHandler() == null) return;
            b.putInt("errorCode", NewsUpdatesActivity.ERROR_URLERR);
            synchronized(this) {
                msg = handler.obtainMessage();
                msg.setData(b);
                handler.sendMessage(msg);
            }
        } catch(IOException e) {
            if(getHandler() == null) return;
            b.putInt("errorCode", NewsUpdatesActivity.ERROR_IOERR);
            synchronized(this) {
                msg = handler.obtainMessage();
                msg.setData(b);
                handler.sendMessage(msg);
            }
        } finally {
            executing = false;
        }
    }
}