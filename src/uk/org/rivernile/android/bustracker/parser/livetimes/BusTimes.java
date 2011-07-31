/*
 * Copyright (C) 2011 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.parser.livetimes;

import android.os.AsyncTask;
import java.util.HashMap;

/**
 * This class is a singleton class and you must obtain an instance from
 * getInstance() before using it. Its purpose is to handle bus time requests.
 * 
 * @author Niall Scott
 */
public class BusTimes {
    
    public final static int ERROR_SERVER = 0;
    public final static int ERROR_NOCONNECTION = 1;
    public final static int ERROR_CANNOTRESOLVE = 2;
    public final static int ERROR_NOCODE = 3;
    public final static int ERROR_PARSEERR = 4;
    public final static int ERROR_NODATA = 5;
    
    private static BusTimes instance;
    
    private long lastDataRefresh = 0;
    private BusTimesEvent handler;
    private HashMap<String, BusStop> busStops;
    private BusParser parser;
    private GetBusTimesTask task;
    
    /**
     * Create a new bus times instance. This constructor MUST receive valid
     * Handler and BusParser objects to be able to operate.
     * 
     * @param handler The object to send events back to.
     * @param parser The parser implementation. That implementation is
     * responsible for any resources it may acquire.
     */
    private BusTimes(final BusTimesEvent handler, final BusParser parser) {
        if(handler == null)
            throw new IllegalArgumentException("The handler must not be null.");
        
        if(parser == null)
            throw new IllegalArgumentException("The parser must not be null.");
        
        this.handler = handler;
        this.parser = parser;
    }
    
    /**
     * Get a bus times instance. This constructor MUST receive valid
     * BusTimesEvent and BusParser objects to be able to operate.
     * 
     * @param handler The object to send events back to.
     * @param parser The parser implementation. That implementation is
     * responsible for any resources it may acquire.
     */
    public static BusTimes getInstance(final BusTimesEvent handler,
            final BusParser parser) {
        if(instance == null) {
            instance = new BusTimes(handler, parser);
        } else {
            instance.setHandler(handler);
            instance.parser = parser;
        }
        return instance;
    }
    
    /**
     * Set the Handler object to send events back to.
     * 
     * @param handler The object to send events back to.
     */
    public void setHandler(final BusTimesEvent handler) {
        this.handler = handler;
    }
    
    /**
     * Get the UNIX timestamp (in milliseconds) of when the data was last
     * refreshed.
     * 
     * @return The UNIX timestamp of the last data refresh. The timestamp is 0
     * if this was never.
     */
    public long getLastDataRefresh() {
        return lastDataRefresh;
    }
    
    /**
     * If we have sent a request off to the parser implementation and we are
     * waiting on a response back, this method will return true.
     * 
     * @return True when the parser is busy, false when not.
     */
    public boolean isExecuting() {
        return task != null ?
                task.getStatus() == AsyncTask.Status.RUNNING : false;
    }
    
    /**
     * Get the data from the previous refresh of bus time data.
     * @return Get the data from the previous refresh of bus time data.
     */
    public HashMap<String, BusStop> getBusStops() {
        return busStops;
    }
    
    /**
     * Do request.
     * @param stopCodes The list of stop codes for which we want to get bus
     * times for.
     * @return true if the task was kicked off, false if not. The task will not
     * be kicked off if another task is already taking place.
     */
    public boolean doRequest(final String[] stopCodes) {
        if(stopCodes == null || stopCodes.length == 0)
            throw new IllegalArgumentException("The stop codes array must " +
                    "not be null or empty.");
        
        if(task == null) {
            task = new GetBusTimesTask();
            task.execute(stopCodes);
            return true;
        } else {
            return false;
        }
    }
    
    private class GetBusTimesTask
            extends AsyncTask<String, Void, HashMap<String, BusStop>> {
        
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
        protected HashMap<String, BusStop> doInBackground(
                final String... stopCodes) {
            HashMap<String, BusStop> res = null;
            
            try {
                res = parser.getBusStopData(stopCodes);
                lastDataRefresh = System.currentTimeMillis();
            } catch(BusParserException e) {
                error = e.getCode();
            } finally {
                return res;
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected void onPostExecute(final HashMap<String, BusStop> result) {
            busStops = result;
            if(handler != null) {
                if(error < 0) {
                    handler.onBusTimesReady(result);
                } else {
                    handler.onBusTimesError(error);
                }
            }
            task = null;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        protected void onCancelled() {
            if(handler != null) handler.onCancel();
            task = null;
        }
    }
}