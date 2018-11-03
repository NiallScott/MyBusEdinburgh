/*
 * Copyright (C) 2016 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.neareststops;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.os.Parcel;

import org.junit.Test;

/**
 * Tests for {@link SearchResult}.
 *
 * @author Niall Scott
 */
public class SearchResultTests {

    /**
     * Test that {@link SearchResult#SearchResult(String, String, String, float, int, String)}
     * throws an {@link IllegalArgumentException} when the stop code is {@code null}.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithNullStopCode() {
        new SearchResult(null, "Name", "1, 2, 3", 1f, 0, "Area");
    }

    /**
     * Test that {@link SearchResult#SearchResult(String, String, String, float, int, String)}
     * throws an {@link IllegalArgumentException} when the stop code is empty.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testConstructorWithEmptyStopCode() {
        new SearchResult("", "Name", "1, 2, 3", 1f, 0, "Area");
    }

    /**
     * Test that a valid object can be constructed with optional values, such as passing in
     * {@code null}.
     */
    @Test
    public void testConstructorWithOptionals() {
        final SearchResult result = new SearchResult("123456", null, null, 0f, 0, null);

        assertEquals("123456", result.getStopCode());
        assertNull(result.getStopName());
        assertNull(result.getServices());
        assertEquals(0f, result.getDistance(), 0.001f);
        assertEquals(0, result.getOrientation());
        assertNull(result.getLocality());
    }

    /**
     * Test that the getters return correct data on a constructed object.
     */
    @Test
    public void testValidObject() {
        final SearchResult result = new SearchResult("123456", "Name", "1, 2, 3", 1f, 5, "Area");

        assertEquals("123456", result.getStopCode());
        assertEquals("Name", result.getStopName());
        assertEquals("1, 2, 3", result.getServices());
        assertEquals(1f, result.getDistance(), 0.001f);
        assertEquals(5, result.getOrientation());
        assertEquals("Area", result.getLocality());
    }

    /**
     * Test that with optional parameters, writing to a {@link Parcel} works correctly.
     */
    @Test
    public void testWriteToParcelWithOptionals() {
        final Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        final SearchResult result = new SearchResult("123456", null, null, 0f, 0, null);
        result.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        assertEquals("123456", parcel.readString());
        assertNull(parcel.readString());
        assertNull(parcel.readString());
        assertEquals(0f, parcel.readFloat(), 0.001f);
        assertEquals(0, parcel.readInt());
        assertNull(parcel.readString());
        parcel.recycle();
    }

    /**
     * Test that with a valid object, writing to a {@link Parcel} works correctly.
     */
    @Test
    public void testWriteToParcelWithValidObject() {
        final Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        final SearchResult result = new SearchResult("123456", "Name", "1, 2, 3", 1f, 5, "Area");
        result.writeToParcel(parcel, 0);
        parcel.setDataPosition(0);

        assertEquals("123456", parcel.readString());
        assertEquals("Name", parcel.readString());
        assertEquals("1, 2, 3", parcel.readString());
        assertEquals(1f, parcel.readFloat(), 0.001f);
        assertEquals(5, parcel.readInt());
        assertEquals("Area", parcel.readString());
    }

    /**
     * Test that a {@link Parcel} containing optional values constructs a {@link SearchResult}
     * correctly.
     */
    @Test
    public void testReadFromParcelWithOptionals() {
        final Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        parcel.writeString("123456");
        parcel.writeString(null);
        parcel.writeString(null);
        parcel.writeFloat(0f);
        parcel.writeInt(0);
        parcel.writeString(null);
        parcel.setDataPosition(0);
        final SearchResult result = SearchResult.CREATOR.createFromParcel(parcel);
        parcel.recycle();

        assertEquals("123456", result.getStopCode());
        assertNull(result.getStopName());
        assertNull(result.getServices());
        assertEquals(0f, result.getDistance(), 0.001f);
        assertEquals(0, result.getOrientation());
        assertNull(result.getLocality());
    }

    /**
     * Test that a {@link Parcel} containing valid values constructs a {@link SearchResult}
     * correctly.
     */
    @Test
    public void testReadFromParcelWithValidData() {
        final Parcel parcel = Parcel.obtain();
        parcel.setDataPosition(0);
        parcel.writeString("123456");
        parcel.writeString("Name");
        parcel.writeString("1, 2, 3");
        parcel.writeFloat(1.0f);
        parcel.writeInt(5);
        parcel.writeString("Area");
        parcel.setDataPosition(0);
        final SearchResult result = SearchResult.CREATOR.createFromParcel(parcel);
        parcel.recycle();

        assertEquals("123456", result.getStopCode());
        assertEquals("Name", result.getStopName());
        assertEquals("1, 2, 3", result.getServices());
        assertEquals(1f, result.getDistance(), 0.001f);
        assertEquals(5, result.getOrientation());
        assertEquals("Area", result.getLocality());
    }

    /**
     * Test that {@link SearchResult#compareTo(SearchResult)} returns expected values.
     */
    @Test
    public void testComparator() {
        final SearchResult result1 = new SearchResult("123456", "Name", "1, 2, 3", 2f, 2, "Area");
        final SearchResult result2 = new SearchResult("123456", "Name", "1, 2, 3", 2f, 2, "Area");
        final SearchResult result3 = new SearchResult("123456", "Name", "1, 2, 3", 2.5f, 2, "Area");

        assertEquals(0, result1.compareTo(result2));
        assertTrue(result1.compareTo(result3) < 0);
        assertTrue(result3.compareTo(result1) > 0);
    }

    /**
     * Test that {@link SearchResult#equals(Object)} and {@link SearchResult#hashCode()} return the
     * expected results.
     */
    @Test
    public void testEqualsAndHashCode() {
        final SearchResult result1 = new SearchResult("123456", "Name", "1, 2, 3", 2f, 2, "Area");
        final SearchResult result2 = new SearchResult("123456", "Name", "1, 2, 3", 2f, 2, "Area");
        final SearchResult result3 = new SearchResult("987654", "Name", "1, 2, 3", 2f, 2, "Area");

        // Equals.
        assertEquals(result1, result1);
        assertEquals(result1, result2);
        assertNotEquals(result1, result3);
        assertNotEquals(result1, null);
        assertNotEquals(result1, new Object());

        // Hash code.
        assertEquals(result1.hashCode(), result1.hashCode());
        assertEquals(result1.hashCode(), result2.hashCode());
        assertNotEquals(result1.hashCode(), result3.hashCode());
    }

    /**
     * Test that {@link SearchResult#describeContents()} returns {@code 0}.
     */
    @Test
    public void testDescribeContents() {
        final SearchResult result = new SearchResult("123456", "Name", "1, 2, 3", 2f, 2, "Area");
        assertEquals(0, result.describeContents());
    }

    /**
     * Test that the {@link android.os.Parcelable.Creator#newArray(int)} within {@link SearchResult}
     * returns an array with the correct number of elements
     */
    @Test
    public void testParableCreatorNewArray() {
        final SearchResult[] results = SearchResult.CREATOR.newArray(7);
        assertEquals(7, results.length);
    }
}
