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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Static utility methods for performing operations on JSON.
 * 
 * @author Niall Scott
 */
public class JSONUtils {
    
    /**
     * This constructor has been made private to prevent instantiation of this
     * class.
     */
    private JSONUtils() {
        // No implementation.
    }
    
    /**
     * Get a String from a JSON object. If the String given by name does not
     * exist, a {@link JSONException} will be thrown.
     * 
     * @param obj The JSON object to get the String from. If this is null, a
     * {@link JSONException} will be thrown.
     * @param name The name of the JSON String to get.
     * @return The value of the JSON String given by name. If the value is JSON
     * null, then this will be converted in to Java's null.
     * @throws JSONException When obj is null, or the conditions given at
     * {@link JSONObject#getString(java.lang.String)}.
     * @see JSONObject#getString(java.lang.String)
     */
    public static String getString(final JSONObject obj, final String name)
            throws JSONException {
        if (obj == null) {
            throw new JSONException("The JSON object is null.");
        }
        
        // The value is fetched first to cause the default behaviour to be
        // executed, then we take over with our extended behaviour.
        final String value = obj.getString(name);
        return !obj.isNull(name) ? value : null;
    }
    
    /**
     * Get a String from a JSON array. If the index does not exist, a
     * {@link JSONException} will be thrown.
     * 
     * @param arr The JSON array to get the String from. If this is null, a
     * {@link JSONException} will be thrown.
     * @param index The index in the array to get the String from.
     * @return The value of the JSON String at the given index. If the value is
     * JSON null, then this will be converted in to Java's null.
     * @throws JSONException When arr is null, or the conditions given at
     * {@link JSONArray#getString(int)}.
     * @see JSONArray#getString(int)
     */
    public static String getString(final JSONArray arr, final int index)
            throws JSONException {
        if (arr == null) {
            throw new JSONException("The JSON array is null.");
        }
        
        // The value is fetched first to cause the default behaviour to be
        // executed, then we take over with our extended behaviour.
        final String value = arr.getString(index);
        return !arr.isNull(index) ? value : null;
    }
    
    /**
     * Get an optional String from a JSON object. This method exists because
     * when the value of the mapping is JSON's null, then a String is returned
     * rather than Java's null. This method checks for a null value with the
     * JSON API and returns the correct value in this case.
     * 
     * @param obj The JSON object to retrieve the String value from.
     * @param name The name of the key which maps to the String value.
     * @param fallback What to return when obj is null, the mapping does not
     * exist or the value is JSON null.
     * @return The value of the mapping given by the name, or fallback if this
     * does not exist.
     * @see JSONObject#optString(java.lang.String, java.lang.String)
     */
    public static String optString(final JSONObject obj, final String name,
            final String fallback) {
        return obj != null ? (obj.has(name) && obj.isNull(name) ?
                null : obj.optString(name, fallback)) : fallback;
    }
    
    /**
     * Get an optional String from a JSON array. This method exists because
     * when the value of the mapping is JSON's null, then a String is returned
     * rather than Java's null. This method checks for a null value with the
     * JSON API and returns the correct value in this case.
     * 
     * @param arr The JSON array to retrieve the String value from.
     * @param index The index in the JSON array that the String exists.
     * @param fallback What to return when arr is null, the element does not
     * exist or the value is JSON null.
     * @return The value of the mapping given by the index, or fallback if this
     * does not exist.
     * @see JSONArray#optString(int, java.lang.String)
     */
    public static String optString(final JSONArray arr, final int index,
            final String fallback) {
        return arr != null ?
                (index >= 0 && index < arr.length() && arr.isNull(index) ?
                null : arr.optString(index, fallback)) : fallback;
    }
}