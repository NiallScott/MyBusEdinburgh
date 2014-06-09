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

package uk.org.rivernile.android.utils;

/**
 * A {@link android.content.Loader} can only return a single object of a single
 * type. This class allows a Loader to return specific data on success, or an
 * {@link java.lang.Exception} when an error has occurred.
 * 
 * @author Niall Scott
 * @param <D> The type of data to return on success.
 * @param <E> The type of Exception to return on failure.
 */
public class LoaderResult<D, E extends Exception> {
    
    private final D result;
    private final E exception;
    private final long loadTime;
    
    /**
     * Create a new LoaderResult that will contain the data of when the data was
     * loaded successfully.
     * 
     * @param result The result of a successful load.
     * @param loadTime The time, relative to
     * {@link android.os.SystemClock#elapsedRealtime()}, that the data was
     * loaded at.
     */
    public LoaderResult(final D result, final long loadTime) {
        this.result = result;
        exception = null;
        this.loadTime = loadTime;
    }
    
    /**
     * Create a new LoaderResult for when the load was an error and an
     * {@link java.lang.Exception} was thrown.
     * 
     * @param exception The resulting {@link java.lang.Exception} of a failed
     * load.
     * @param loadTime The time, relative to
     * {@link android.os.SystemClock#elapsedRealtime()}, that the data was
     * loaded at.
     */
    public LoaderResult(final E exception, final long loadTime) {
        if (exception == null) {
            throw new IllegalArgumentException("The exception must not be "
                    + "null.");
        }
        
        this.exception = exception;
        result = null;
        this.loadTime = loadTime;
    }
    
    /**
     * Get the result of a successful load. Before calling this, it is advisable
     * to call {@link #hasException()} to check if the load was an error or not.
     * If the load was an error, this method will return null. Also, it is
     * possible the result of a load yielded a null result.
     * 
     * @return The result, or null if the result was null or the load yielded
     * an error.
     */
    public D getResult() {
        return result;
    }
    
    /**
     * Get the Exception which describes why the load failed. Before calling
     * this, it is advisable to call {@link #hasException()} to check if the
     * load was an error or not. If it was an error, this method will return a
     * non-null result. If it was not an error, this method will return null.
     * 
     * @return The Exception which describes why the load failed, or null if the
     * load was not an error.
     */
    public E getException() {
        return exception;
    }
    
    /**
     * Get the time, relative to
     * {@link android.os.SystemClock#elapsedRealtime()}, that the data was
     * loaded at.
     * 
     * @return The time, relative to
     * {@link android.os.SystemClock#elapsedRealtime()}, that the data was
     * loaded at.
     */
    public long getLoadTime() {
        return loadTime;
    }
    
    /**
     * Get whether the load result was an error or not.
     * 
     * @return true if the load was an error (i.e. it has an Exception), false
     * if the load was successful.
     */
    public boolean hasException() {
        return exception != null;
    }
}