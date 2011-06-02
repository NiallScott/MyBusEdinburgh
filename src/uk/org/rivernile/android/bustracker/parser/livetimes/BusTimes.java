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

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.util.HashMap;
import uk.org.rivernile.edinburghbustracker.android.DisplayStopDataActivity;

public class BusTimes implements Runnable {
    
    public final static int ERROR_SERVER = 0;
    public final static int ERROR_NOCONNECTION = 1;
    public final static int ERROR_CANNOTRESOLVE = 2;
    public final static int ERROR_NOCODE = 3;
    public final static int ERROR_PARSEERR = 4;
    public final static int ERROR_NODATA = 5;
    
    private static BusTimes instance;
    
    private long lastDataRefresh = 0;
    private Handler handler;
    private HashMap<String, BusStop> busStops;
    private String[] stopCodes;
    private boolean executing = false;
    private Thread fetchThread;
    private BusParser parser;
    
    /**
     * Create a new bus times instance. This constructor MUST receive valid
     * Handler and BusParser objects to be able to operate.
     * 
     * @param handler The Handler object to send events back to.
     * @param parser The parser implementation. That implementation is
     * responsible for any resources it may acquire.
     */
    private BusTimes(final Handler handler, final BusParser parser) {
        if(handler == null)
            throw new IllegalArgumentException("The handler must not be null.");
        
        if(parser == null)
            throw new IllegalArgumentException("The parser must not be null.");
        
        this.handler = handler;
        this.parser = parser;
    }
    
    /**
     * Get a bus times instance. This constructor MUST receive valid Handler and
     * BusParser objects to be able to operate.
     * 
     * @param handler The Handler object to send events back to.
     * @param parser The parser implementation. That implementation is
     * responsible for any resources it may acquire.
     */
    public static BusTimes getInstance(final Handler handler,
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
     * @param handler The Handler object to send events back to.
     */
    public void setHandler(final Handler handler) {
        if((handler == null || handler != this.handler)
                && fetchThread != null) {
            fetchThread.interrupt();
            executing = false;
        }
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
        return executing;
    }
    
    /**
     * Get the data from the previous refresh of bus time data.
     * @return Get the data from the previous refresh of bus time data.
     */
    public HashMap<String, BusStop> getBusStops() {
        return busStops;
    }
    
    /**
     * Do 
     * @param stopCodes 
     */
    public void doRequest(final String[] stopCodes) {
        if(stopCodes == null || stopCodes.length == 0)
            throw new IllegalArgumentException("The stop codes array must " +
                    "not be null or empty.");
        
        this.stopCodes = stopCodes;
        
        if(!executing) {
            executing = true;
            fetchThread = new Thread(this);
            fetchThread.start();
        }
    }
    
    /**
     * This is the thread which kicks off parsing. This method should not be
     * invoked externally to this class.
     */
    @Override
    public void run() {
        try {
            busStops = parser.getBusStopData(stopCodes);
            lastDataRefresh = System.currentTimeMillis();
            
            if(handler != null && !fetchThread.isInterrupted()) {
                handler.sendEmptyMessage(DisplayStopDataActivity.EVENT_READY);
            }
        } catch(BusParserException e) {
            if(handler != null && !fetchThread.isInterrupted()) {
                Message msg = handler.obtainMessage();
                msg.what = DisplayStopDataActivity.EVENT_ERROR;
                Bundle b = new Bundle();
                b.putInt("errorCode", e.getCode());
                msg.setData(b);
                handler.sendMessage(msg);
            }
        }
        executing = false;
    }
}