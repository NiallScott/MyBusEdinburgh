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

package uk.org.rivernile.edinburghbustracker.android.fetchers;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import uk.org.rivernile.edinburghbustracker.android.DisplayStopDataActivity;

/**
 * This class is a helper class to the DisplayStopDataActivity. Only a single
 * instance of this class can be created and this is controlled through the
 * getInstance() method. This is used so that problems do not occur regarding
 * null pointers and so in DisplayStopDataActivity.
 *
 * @author Niall Scott
 */
public class FetchLiveTimesTask implements Runnable {

    private final static String STOP_DATA_COMMAND = "getBusTimesByStopCode";

    private static FetchLiveTimesTask instance = null;
    private Handler handler;
    private String stopCode;
    private String remoteHost;
    private int remotePort;
    private boolean executing = false;

    /**
     * This constructor is intentionally left blank and private, this class
     * can only be instantiated from within.
     */
    private FetchLiveTimesTask() {
        // Nothing to do here
    }

    /**
     * Get an instance of this class. A handler object needs to be specified so
     * this class knows where to return the server data to.
     *
     * @param handler The handler object to fire data back to.
     * @return An instance of this class.
     */
    public static FetchLiveTimesTask getInstance(final Handler handler) {
        if(instance == null) instance = new FetchLiveTimesTask();
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
     *
     * @param stopCode The stop code to get.
     * @param remoteHost The remote host to connect to.
     * @param remotePort The remote port to connect to.
     */
    public void doTask(final String stopCode, final String remoteHost,
            final int remotePort) {
        this.stopCode = stopCode;
        this.remoteHost = remoteHost;
        this.remotePort = remotePort;
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
            // Set up socket stuff.
            Socket sock = new Socket();
            sock.setSoTimeout(30000);
            sock.connect(new InetSocketAddress(remoteHost, remotePort), 20000);
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(sock.getInputStream()));
            PrintWriter writer = new PrintWriter(sock.getOutputStream(), true);
            writer.println(STOP_DATA_COMMAND + ":" + stopCode);
            String jsonString = "";
            String tmp = "";
            boolean readJson = false;
            while ((tmp = reader.readLine()) != null) {
                if(tmp.startsWith("Error:")) {
                    if(getHandler() != null) {
                        b.putInt("errorCode",
                                DisplayStopDataActivity.ERROR_SERVER);
                        synchronized(this) {
                            msg = handler.obtainMessage();
                            msg.setData(b);
                            handler.sendMessage(msg);
                        }
                    }
                    writer.println("exit");
                    reader.close();
                    writer.close();
                    sock.close();
                    return;
                }
                if (tmp.equals("+")) {
                    readJson = true;
                } else if (tmp.equals("-")) {
                    break;
                } else if (readJson) {
                    jsonString = jsonString + tmp;
                }
            }
            writer.println("exit");
            reader.close();
            writer.close();
            sock.close();

            if(getHandler() == null) return;
            b.putString("jsonString", jsonString);
            synchronized(this) {
                msg = handler.obtainMessage();
                msg.setData(b);
                handler.sendMessage(msg);
            }
        } catch (UnknownHostException e) {
            if(getHandler() == null) return;
            b.putInt("errorCode", DisplayStopDataActivity.ERROR_CANNOTRESOLVE);
            synchronized(this) {
                msg = handler.obtainMessage();
                msg.setData(b);
                handler.sendMessage(msg);
            }
        } catch (IOException e) {
            if(getHandler() == null) return;
            b.putInt("errorCode", DisplayStopDataActivity.ERROR_NOCONNECTION);
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