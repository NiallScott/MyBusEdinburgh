/*
 * Copyright (C) 2014 - 2018 Niall 'Rivernile' Scott
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Static utility methods for performing operations on JSON.
 * 
 * @author Niall Scott
 */
public final class JSONUtils {
    
    /**
     * This constructor has been made private to prevent instantiation of this class.
     */
    private JSONUtils() {
        // No implementation.
    }
    
    /**
     * Get a {@link String} from a {@link JSONObject}. If the {@link String} given by name does not
     * exist, a {@link JSONException} will be thrown.
     * 
     * @param obj The {@link JSONObject} to get the {@link String} from.
     * @param name The name of the JSON {@link String} to get.
     * @return The value of the JSON {@link String} given by name. If the value is JSON
     * {@code null}, then this will be converted in to Java's {@code null}.
     * @throws JSONException See {@link JSONObject#getString(String)}.
     * @see JSONObject#getString(String)
     */
    @Nullable
    public static String getString(@NonNull final JSONObject obj, @Nullable final String name)
            throws JSONException {
        // The value is fetched first to cause the default behaviour to be executed, then we take
        // over with our extended behaviour.
        final String value = obj.getString(name);
        return !obj.isNull(name) ? value : null;
    }
    
    /**
     * Get a {@link String} from a {@link JSONArray}. If the index does not exist, a
     * {@link JSONException} will be thrown.
     * 
     * @param arr The {@link JSONArray} to get the {@link String} from.
     * @param index The index in the array to get the {@link String} from.
     * @return The value of the JSON {@link String} at the given index. If the value is JSON
     * {@code null}, then this will be converted in to Java's {@code null}.
     * @throws JSONException See {@link JSONArray#getString(int)}.
     * @see JSONArray#getString(int)
     */
    @Nullable
    public static String getString(@NonNull final JSONArray arr, final int index)
            throws JSONException {
        // The value is fetched first to cause the default behaviour to be executed, then we take
        // over with our extended behaviour.
        final String value = arr.getString(index);
        return !arr.isNull(index) ? value : null;
    }
    
    /**
     * Get an optional {@link String} from a {@link JSONObject}. This method exists because when
     * the value of the mapping is JSON's {@code null}, then a {@link String} is returned rather
     * than Java's {@code null}. This method checks for a {@code null} value with the JSON API
     * and returns the correct value in this case.
     *
     * @param obj The {@link JSONObject} to retrieve the {@link String} value from.
     * @param name The name of the key which maps to the {@link String} value.
     * @param fallback What to return when the mapping does not exist or the value is JSON
     * {@code null}.
     * @return The value of the mapping given by the name, or {@code fallback} if this does not
     * exist.
     * @see JSONObject#optString(String, String)
     */
    @Nullable
    public static String optString(@NonNull final JSONObject obj, @Nullable final String name,
            @Nullable final String fallback) {
        return obj.has(name) && obj.isNull(name) ? null : obj.optString(name, fallback);
    }
    
    /**
     * Get an optional {@link String} from a {@link JSONArray}. This method exists because when
     * the value of the mapping is JSON's {@code null}, then a {@link String} is returned rather
     * than Java's {@code null}. This method checks for a {@code null} value with the JSON API
     * and returns the correct value in this case.
     * 
     * @param arr The {@link JSONArray} to retrieve the {@link String} value from.
     * @param index The index in the {@link JSONArray} that the {@link String} exists.
     * @param fallback What to return when the element does not exist or the value is JSON
     * {@code null}.
     * @return The value of the mapping given by the index, or {@code fallback} if this does not
     * exist.
     * @see JSONArray#optString(int, String)
     */
    @Nullable
    public static String optString(@NonNull final JSONArray arr, final int index,
            @Nullable final String fallback) {
        return index >= 0 && index < arr.length() && arr.isNull(index) ?
                null : arr.optString(index, fallback);
    }
}