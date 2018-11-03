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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;

/**
 * Tests for {@link JSONUtils}.
 * 
 * @author Niall Scott
 */
public class JSONUtilsTestCase {
    
    /**
     * Test that {@link JSONUtils#getString(JSONObject, String)} throws a {@link JSONException}
     * when the name is set as {@code null}.
     */
    @Test(expected = JSONException.class)
    public void testGetStringForObjectWithNullName() throws JSONException {
        JSONUtils.getString(new JSONObject(), null);
    }
    
    /**
     * Test that {@link JSONUtils#getString(JSONObject, String)} throws a {@link JSONException}
     * when the name is set as empty {@link String}.
     */
    @Test(expected = JSONException.class)
    public void testGetStringForObjectWithEmptyName() throws JSONException {
        JSONUtils.getString(new JSONObject(), "");
    }
    
    /**
     * Test that {@link JSONUtils#getString(JSONObject, String)} throws a {@link JSONException}
     * when there is no mapping for the name.
     */
    @Test(expected = JSONException.class)
    public void testGetStringForObjectWithNoMapping() throws JSONException {
        JSONUtils.getString(new JSONObject(), "example");
    }
    
    /**
     * Test that {@link JSONUtils#getString(JSONObject, String)} returns {@code null} when the
     * mapping is set as {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this test, so if there
     * are, let the test fail.
     */
    @Test
    public void testGetStringForObjectWithNullMapping() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("example", JSONObject.NULL);
        assertNull(JSONUtils.getString(jo, "example"));
    }
    
    /**
     * Test that {@link JSONUtils#getString(JSONObject, String)} returns non-{@code null} when
     * the mapping is set as something other than {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this test, so if there
     * are, let the test fail.
     */
    @Test
    public void testGetStringForObjectWithNonNullMapping() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("example", "value");
        assertEquals("value", JSONUtils.getString(jo, "example"));
    }
    
    /**
     * Test that {@link JSONUtils#getString(JSONArray, int)} throws a {@link JSONException} when
     * there is no mapping for the index.
     */
    @Test(expected = JSONException.class)
    public void testGetStringForArrayWithNoMapping() throws JSONException {
        JSONUtils.getString(new JSONArray(), 1);
    }
    
    /**
     * Test that {@link JSONUtils#getString(JSONArray, int)} returns {@code null} when the
     * mapping is set as {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this test, so if there
     * are, let the test fail.
     */
    @Test
    public void testGetStringForArrayWithNullMapping() throws JSONException {
        final JSONArray ja = new JSONArray();
        ja.put(JSONObject.NULL);
        assertNull(JSONUtils.getString(ja, 0));
    }
    
    /**
     * Test that {@link JSONUtils#getString(JSONArray, int)} returns non-{@code null} when the
     * mapping is set as something other than {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this test, so if there
     * are, let the test fail.
     */
    @Test
    public void testGetStringForArrayWithNonNullMapping() throws JSONException {
        final JSONArray ja = new JSONArray();
        ja.put("value");
        assertEquals("value", JSONUtils.getString(ja, 0));
    }
    
    /**
     * Test that {@link JSONUtils#optString(JSONObject, String, String)} returns the default
     * value when the name is set as {@code null}.
     */
    @Test
    public void testOptStringForObjectWithNullName() {
       assertEquals("test", JSONUtils.optString(new JSONObject(), null, "test"));
    }
    
    /**
     * Test that {@link JSONUtils#optString(JSONObject, String, String)} returns the default
     * value when the name is set as empty {@link String}.
     */
    @Test
    public void testOptStringForObjectWithEmptyName() {
        assertEquals("test", JSONUtils.optString(new JSONObject(), "", "test"));
    }
    
    /**
     * Test that {@link JSONUtils#optString(JSONObject, String, String)} returns the default
     * value when the name is set as an unmapped key.
     */
    @Test
    public void testOptStringForObjectWithNoMapping() {
        assertEquals("test", JSONUtils.optString(new JSONObject(), "example", "test"));
    }
    
    /**
     * Test that {@link JSONUtils#optString(JSONObject, String, String)} returns {@code null}
     * when the mapping is set as {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this test, so if there
     * are, let the test fail.
     */
    @Test
    public void testOptStringForObjectWithNullMapping() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("example", JSONObject.NULL);
        assertNull(JSONUtils.optString(jo, "example", "test"));
    }
    
    /**
     * Test that {@link JSONUtils#optString(JSONObject, String, String)} returns non-{@code null}
     * when the mapping is set as something other than {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this test, so if there
     * are, let the test fail.
     */
    @Test
    public void testOptStringForObjectWithNonNullMapping() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("example", "value");
        assertEquals("value", JSONUtils.optString(jo, "example", "test"));
    }
    
    /**
     * Test that {@link JSONUtils#optString(JSONArray, int, String)} returns the default value
     * when there is no mapping for the index.
     */
    @Test
    public void testOptStringForArrayWithNoMapping() {
        assertEquals("test", JSONUtils.optString(new JSONArray(), 1, "test"));
    }
    
    /**
     * Test that {@link JSONUtils#optString(JSONArray, int, String)} returns {@code null} when
     * the mapping is set as {@link JSONObject#NULL}.
     */
    @Test
    public void testOptStringForArrayWithNullMapping() {
        final JSONArray ja = new JSONArray();
        ja.put(JSONObject.NULL);
        assertNull(JSONUtils.optString(ja, 0, "test"));
    }
    
    /**
     * Test that {@link JSONUtils#optString(JSONArray, int, String)} returns non-{@code null}
     * when the mapping is set as something other than {@link JSONObject#NULL}.
     */
    @Test
    public void testOptStringForArrayWithNonNullMapping() {
        final JSONArray ja = new JSONArray();
        ja.put("value");
        assertEquals("value", JSONUtils.optString(ja, 0, "test"));
    }
}