/*
 * Copyright (C) 2016 Niall 'Rivernile' Scott
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
 * 1. This notice may not be removed or altered from any file it appears in.
 *
 * 2. Any modifications made to this software, except those defined in
 *    clause 3 of this agreement, must be released under this license, and
 *    the source code of any modifications must be made available on a
 *    publically accessible (and locateable) website, or sent to the
 *    original author of this software.
 *
 * 3. Software modifications that do not alter the functionality of the
 *    software but are simply adaptations to a specific environment are
 *    exempt from clause 2.
 */

package uk.org.rivernile.android.bustracker.database.busstop;

import android.content.ContentValues;
import android.net.Uri;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.test.ProviderTestCase2;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for {@link BusStopProvider}.
 *
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class BusStopProviderTests extends ProviderTestCase2<BusStopProvider> {

    public BusStopProviderTests() {
        super(BusStopProvider.class, BusStopContract.AUTHORITY);
    }

    @Before
    @Override
    public void setUp() throws Exception {
        setContext(InstrumentationRegistry.getTargetContext());
        super.setUp();
    }

    @After
    @Override
    public void tearDown() throws Exception {
        super.tearDown();

        getMockContext().deleteDatabase(BusStopContract.DB_NAME);
    }

    /**
     * Test that {@link BusStopProvider#getType(Uri)} returns the correct MIME types for the
     * supplied content {@link Uri}s.
     */
    @Test
    public void testGetTypeSuccess() {
        assertEquals(BusStopContract.DatabaseInformation.CONTENT_TYPE,
                getMockContentResolver().getType(BusStopContract.DatabaseInformation.CONTENT_URI));
        assertEquals(BusStopContract.Services.CONTENT_TYPE,
                getMockContentResolver().getType(BusStopContract.Services.CONTENT_URI));
        assertEquals(BusStopContract.BusStops.CONTENT_TYPE,
                getMockContentResolver().getType(BusStopContract.BusStops.CONTENT_URI));
        assertEquals(BusStopContract.ServiceStops.CONTENT_TYPE,
                getMockContentResolver().getType(BusStopContract.ServiceStops.CONTENT_URI));
        assertEquals(BusStopContract.ServicePoints.CONTENT_TYPE,
                getMockContentResolver().getType(BusStopContract.ServicePoints.CONTENT_URI));
    }

    /**
     * Test that {@link BusStopProvider#getType(Uri)} returns {@code null} when invalid
     * {@link Uri}s are supplied.
     */
    @Test
    public void testGetTypeWithInvalidUris() {
        assertNull(getMockContentResolver().getType(Uri.parse("content://invalid.uri/thing")));
        assertNull(getMockContentResolver()
                .getType(Uri.parse("content://" + BusStopContract.AUTHORITY)));
        assertNull(getMockContentResolver()
                .getType(Uri.parse("content://" + BusStopContract.AUTHORITY + "/invalid")));
    }

    /**
     * Test that {@link BusStopProvider#query(Uri, String[], String, String[], String)} throws an
     * {@link IllegalArgumentException} when an invalid {@link Uri} has been supplied.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testQueryWithInvalidUri() {
        getMockContentResolver()
                .query(Uri.parse("content://" + BusStopContract.AUTHORITY + "/invalid"), null,
                        null, null, null);
    }

    /**
     * As the {@link android.content.ContentProvider} is read-only, test that an attempt to insert
     * in to {@link BusStopProvider} throws {@link UnsupportedOperationException}.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testInsertIsNoOp() {
        final ContentValues cv = new ContentValues();
        cv.put(BusStopContract.ServiceStops.SERVICE_NAME, "1");
        cv.put(BusStopContract.ServiceStops.STOP_CODE, "123456");
        getMockContentResolver().insert(BusStopContract.ServiceStops.CONTENT_URI, cv);
    }

    /**
     * As the {@link android.content.ContentProvider} is read-only, test that an attempt to delete
     * from {@link BusStopProvider} throws {@link UnsupportedOperationException}.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testDeleteIsNoOp() {
        getMockContentResolver().delete(BusStopContract.BusStops.CONTENT_URI, null, null);
    }

    /**
     * As the {@link android.content.ContentProvider} is read-only, test that an attempt to update
     * the {@link BusStopProvider} throws {@link UnsupportedOperationException}.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testUpdateIsNoOp() {
        final ContentValues cv = new ContentValues();
        cv.put(BusStopContract.ServiceStops.SERVICE_NAME, "1");
        cv.put(BusStopContract.ServiceStops.STOP_CODE, "123456");
        getMockContentResolver().update(BusStopContract.ServiceStops.CONTENT_URI, cv, null, null);
    }
}
