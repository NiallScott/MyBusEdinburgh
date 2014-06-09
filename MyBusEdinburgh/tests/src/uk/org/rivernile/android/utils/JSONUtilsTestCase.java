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

import junit.framework.TestCase;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Tests for {@link JSONUtils}.
 * 
 * @author Niall Scott
 */
public class JSONUtilsTestCase extends TestCase {
    
    /**
     * Test that
     * {@link JSONUtils#getString(org.json.JSONObject, java.lang.String)}
     * throws a {@link JSONException} when the {@link JSONObject} is set as
     * <code>null</code>.
     */
    public void testGetStringForObjectWithNullObject() {
        try {
            JSONUtils.getString(null, "test");
        } catch (JSONException e) {
            return;
        }
        
        fail("The JSONObject is set as null, so a JSONException should be "
                + "thrown.");
    }
    
    /**
     * Test that
     * {@link JSONUtils#getString(org.json.JSONObject, java.lang.String)}
     * throws a {@link JSONException} when the name is set as <code>null</code>.
     */
    public void testGetStringForObjectWithNullName() {
        try {
            JSONUtils.getString(new JSONObject(), null);
        } catch (JSONException e) {
            return;
        }
        
        fail("The name is set as null, so a JSONException should be thrown.");
    }
    
    /**
     * Test that
     * {@link JSONUtils#getString(org.json.JSONObject, java.lang.String)}
     * throws a {@link JSONException} when the name is set as empty String.
     */
    public void testGetStringForObjectWithEmptyName() {
        try {
            JSONUtils.getString(new JSONObject(), "");
        } catch (JSONException e) {
            return;
        }
        
        fail("The name is set as empty String, so a JSONException should be "
                + "thrown.");
    }
    
    /**
     * Test that
     * {@link JSONUtils#getString(org.json.JSONObject, java.lang.String)}
     * throws a {@link JSONException} when there is no mapping for the name.
     */
    public void testGetStringForObjectWithNoMapping() {
        try {
            JSONUtils.getString(new JSONObject(), "example");
        } catch (JSONException e) {
            return;
        }
        
        fail("The name is set as an unmapped String, so a JSONException should "
                + "be thrown.");
    }
    
    /**
     * Test that
     * {@link JSONUtils#getString(org.json.JSONObject, java.lang.String)}
     * returns <code>null</code> when the mapping is set as
     * {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this
     * test, so if there are, let the TestCase fail the test when it intercepts
     * them.
     */
    public void testGetStringForObjectWithNullMapping() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("example", JSONObject.NULL);
        assertNull(JSONUtils.getString(jo, "example"));
    }
    
    /**
     * Test that
     * {@link JSONUtils#getString(org.json.JSONObject, java.lang.String)}
     * returns non-<code>null</code> when the mapping is set as something other
     * than {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this
     * test, so if there are, let the TestCase fail the test when it intercepts
     * them.
     */
    public void testGetStringForObjectWithNonNullMapping()
            throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("example", "value");
        assertEquals("value", JSONUtils.getString(jo, "example"));
    }
    
    /**
     * Test that
     * {@link JSONUtils#getString(org.json.JSONArray, int)}
     * throws a {@link JSONException} when the {@link JSONArray} is set as
     * <code>null</code>.
     */
    public void testGetStringForArrayWithNullObject() {
        try {
            JSONUtils.getString(null, 0);
        } catch (JSONException e) {
            return;
        }
        
        fail("The JSONArray is set as null, so a JSONException should be "
                + "thrown.");
    }
    
    /**
     * Test that
     * {@link JSONUtils#getString(org.json.JSONArray, int)}
     * throws a {@link JSONException} when there is no mapping for the index.
     */
    public void testGetStringForArrayWithNoMapping() {
        try {
            JSONUtils.getString(new JSONArray(), 1);
        } catch (JSONException e) {
            return;
        }
        
        fail("The index is set as an unmapped value, so a JSONException should "
                + "be thrown.");
    }
    
    /**
     * Test that
     * {@link JSONUtils#getString(org.json.JSONArray, int)}
     * returns <code>null</code> when the mapping is set as
     * {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this
     * test, so if there are, let the TestCase fail the test when it intercepts
     * them.
     */
    public void testGetStringForArrayWithNullMapping() throws JSONException {
        final JSONArray ja = new JSONArray();
        ja.put(JSONObject.NULL);
        assertNull(JSONUtils.getString(ja, 0));
    }
    
    /**
     * Test that
     * {@link JSONUtils#getString(org.json.JSONArray, int)}
     * returns non-<code>null</code> when the mapping is set as something other
     * than {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this
     * test, so if there are, let the TestCase fail the test when it intercepts
     * them.
     */
    public void testGetStringForArrayWithNonNullMapping()
            throws JSONException {
        final JSONArray ja = new JSONArray();
        ja.put("value");
        assertEquals("value", JSONUtils.getString(ja, 0));
    }
    
    /**
     * Test that {@link JSONUtils#optString(org.json.JSONObject, java.lang.String, java.lang.String)}
     * returns the default value when the {@link JSONObject} passed in is null.
     */
    public void testOptStringForObjectWithNullObject() {
        assertEquals("test", JSONUtils.optString(null, "key", "test"));
    }
    
    /**
     * Test that {@link JSONUtils#optString(org.json.JSONObject, java.lang.String, java.lang.String)}
     * returns null when the {@link JSONObject} passed in is null and the
     * default value is set as null.
     */
    public void testOptStringForObjectWithNullObjectAndNullDefault() {
        assertNull(JSONUtils.optString(null, "key", null));
    }
    
    /**
     * Test that {@link JSONUtils#optString(org.json.JSONObject, java.lang.String, java.lang.String)}
     * returns the default value when the name is set as null.
     */
    public void testOptStringForObjectWithNullName() {
       assertEquals("test",
               JSONUtils.optString(new JSONObject(), null, "test"));
    }
    
    /**
     * Test that {@link JSONUtils#optString(org.json.JSONObject, java.lang.String, java.lang.String)}
     * returns the default value when the name is set as empty String.
     */
    public void testOptStringForObjectWithEmptyName() {
        assertEquals("test",
               JSONUtils.optString(new JSONObject(), "", "test"));
    }
    
    /**
     * Test that {@link JSONUtils#optString(org.json.JSONObject, java.lang.String, java.lang.String)}
     * returns the default value when the name is set as an unmapped key.
     */
    public void testOptStringForObjectWithNoMapping() {
        assertEquals("test",
               JSONUtils.optString(new JSONObject(), "example", "test"));
    }
    
    /**
     * Test that {@link JSONUtils#optString(org.json.JSONObject, java.lang.String, java.lang.String)}
     * returns <code>null</code> when the mapping is set as
     * {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this
     * test, so if there are, let the TestCase fail the test when it intercepts
     * them.
     */
    public void testOptStringForObjectWithNullMapping() throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("example", JSONObject.NULL);
        assertNull(JSONUtils.optString(jo, "example", "test"));
    }
    
    /**
     * Test that {@link JSONUtils#optString(org.json.JSONObject, java.lang.String, java.lang.String)}
     * returns non-<code>null</code> when the mapping is set as something other
     * than {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this
     * test, so if there are, let the TestCase fail the test when it intercepts
     * them.
     */
    public void testOptStringForObjectWithNonNullMapping()
            throws JSONException {
        final JSONObject jo = new JSONObject();
        jo.put("example", "value");
        assertEquals("value", JSONUtils.optString(jo, "example", "test"));
    }
    
    /**
     * Test that
     * {@link JSONUtils#optString(org.json.JSONArray, int, java.lang.String)}
     * returns the default value when the {@link JSONArray} is set as
     * <code>null</code>.
     */
    public void testOptStringForArrayWithNullObject() {
        assertEquals("test", JSONUtils.optString(null, 0, "test"));
    }
    
    /**
     * Test that
     * {@link JSONUtils#optString(org.json.JSONArray, int, java.lang.String)}
     * returns the default value when there is no mapping for the index.
     */
    public void testOptStringForArrayWithNoMapping() {
        assertEquals("test", JSONUtils.optString(new JSONArray(), 1, "test"));
    }
    
    /**
     * Test that
     * {@link JSONUtils#optString(org.json.JSONArray, int, java.lang.String)}
     * returns <code>null</code> when the mapping is set as
     * {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this
     * test, so if there are, let the TestCase fail the test when it intercepts
     * them.
     */
    public void testOptStringForArrayWithNullMapping() throws JSONException {
        final JSONArray ja = new JSONArray();
        ja.put(JSONObject.NULL);
        assertNull(JSONUtils.optString(ja, 0, "test"));
    }
    
    /**
     * Test that
     * {@link JSONUtils#optString(org.json.JSONArray, int, java.lang.String)}
     * returns non-<code>null</code> when the mapping is set as something other
     * than {@link JSONObject#NULL}.
     * 
     * @throws JSONException There are no other exceptions expected from this
     * test, so if there are, let the TestCase fail the test when it intercepts
     * them.
     */
    public void testOptStringForArrayWithNonNullMapping()
            throws JSONException {
        final JSONArray ja = new JSONArray();
        ja.put("value");
        assertEquals("value", JSONUtils.optString(ja, 0, "test"));
    }
}