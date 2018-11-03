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

package uk.org.rivernile.android.bustracker.database.busstop;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.test.mock.MockContentProvider;
import android.test.mock.MockContentResolver;
import android.test.mock.MockContext;

import org.junit.Before;
import org.junit.Test;

import java.util.Map;

/**
 * Tests for {@link BusStopDatabase}.
 *
 * @author Niall Scott
 */
public class BusStopDatabaseIntegrationTests {

    private Context mockContext;
    private MockContentResolver mockContentResolver;

    @Before
    public void setUp() {
        mockContentResolver = new MockContentResolver();
        mockContext = new MockContext() {
            @Override
            public ContentResolver getContentResolver() {
                return mockContentResolver;
            }
        };
    }

    /**
     * Test the query arguments for {@link BusStopDatabase#getTopologyId(Context)}.
     */
    @Test
    public void testGetTopologyIdQuery() {
        mockContentResolver.addProvider(BusStopContract.AUTHORITY, new MockContentProvider() {
            @Override
            public Cursor query(final Uri uri, final String[] projection, final String selection,
                    final String[] selectionArgs, final String sortOrder) {
                assertEquals(BusStopContract.DatabaseInformation.CONTENT_URI, uri);
                assertNotNull(projection);
                assertEquals(1, projection.length);
                assertEquals(BusStopContract.DatabaseInformation.CURRENT_TOPOLOGY_ID,
                        projection[0]);
                assertNull(selection);
                assertNull(selectionArgs);
                assertNull(sortOrder);

                return null;
            }
        });

        BusStopDatabase.getTopologyId(mockContext);
    }

    /**
     * Test that a {@code null} topology ID is returned when a {@code null} {@link Cursor} is
     * returned.
     */
    @Test
    public void testGetTopologyIdWithNullCursor() {
        mockContentResolver.addProvider(BusStopContract.AUTHORITY, new MockContentProvider() {
            @Override
            public Cursor query(final Uri uri, final String[] projection, final String selection,
                    final String[] selectionArgs, final String sortOrder) {
                return null;
            }
        });

        assertNull(BusStopDatabase.getTopologyId(mockContext));
    }

    /**
     * Test that a {@code null} topology ID is returned when an empty {@link Cursor} is returned.
     */
    @Test
    public void testGetTopologyIdWithEmptyCursor() {
        mockContentResolver.addProvider(BusStopContract.AUTHORITY, new MockContentProvider() {
            @Override
            public Cursor query(final Uri uri, final String[] projection, final String selection,
                    final String[] selectionArgs,
                    final String sortOrder) {
                return new MatrixCursor(new String[] {
                        BusStopContract.DatabaseInformation.CURRENT_TOPOLOGY_ID
                });
            }
        });

        assertNull(BusStopDatabase.getTopologyId(mockContext));
    }

    /**
     * Test that {@link BusStopDatabase#getTopologyId(Context)} returns the correct item from the
     * {@link Cursor}.
     */
    @Test
    public void testGetTopologyIdWithPopulatedCursor() {
        mockContentResolver.addProvider(BusStopContract.AUTHORITY, new MockContentProvider() {
            @Override
            public Cursor query(final Uri uri, final String[] projection, final String selection,
                    final String[] selectionArgs, final String sortOrder) {
                final MatrixCursor cursor = new MatrixCursor(new String[] {
                        BusStopContract.DatabaseInformation.CURRENT_TOPOLOGY_ID
                });

                cursor.addRow(new String[] { "testTopoId" });

                return cursor;
            }
        });

        assertEquals("testTopoId", BusStopDatabase.getTopologyId(mockContext));
    }

    /**
     * Test that the query arguments are correct for
     * {@link BusStopDatabase#searchBusStops(Context, String)}.
     */
    @Test
    public void testSearchDatabaseQuery() {
        mockContentResolver.addProvider(BusStopContract.AUTHORITY, new MockContentProvider() {
            @Override
            public Cursor query(final Uri uri, final String[] projection, final String selection,
                    final String[] selectionArgs, final String sortOrder) {
                assertEquals(BusStopContract.BusStops.CONTENT_URI, uri);
                assertArrayEquals(new String[] {
                        BusStopContract.BusStops.STOP_CODE,
                        BusStopContract.BusStops.STOP_NAME,
                        BusStopContract.BusStops.LATITUDE,
                        BusStopContract.BusStops.LONGITUDE,
                        BusStopContract.BusStops.ORIENTATION,
                        BusStopContract.BusStops.LOCALITY,
                        BusStopContract.BusStops.SERVICE_LISTING
                }, projection);
                assertEquals("stopCode LIKE ? OR stopName LIKE ? OR locality LIKE ?", selection);
                assertArrayEquals(new String[] {
                        "%example%",
                        "%example%",
                        "%example%"
                }, selectionArgs);
                assertNull(sortOrder);

                return null;
            }
        });

        BusStopDatabase.searchBusStops(mockContext, "example");
    }

    /**
     * Test that the query arguments are correct for when the services array is {@code null} or
     * empty in {@link BusStopDatabase#getServiceColours(Context, String[])}.
     */
    @Test
    public void testGetServiceColoursWithNullOrEmptyServices() {
        mockContentResolver.addProvider(BusStopContract.AUTHORITY, new MockContentProvider() {
            @Override
            public Cursor query(final Uri uri, final String[] projection, final String selection,
                    final String[] selectionArgs, final String sortOrder) {
                assertEquals(BusStopContract.Services.CONTENT_URI, uri);
                assertArrayEquals(new String[] {
                        BusStopContract.Services.NAME,
                        BusStopContract.Services.COLOUR
                }, projection);
                assertEquals("hex_colour IS NOT NULL", selection);
                assertNull(selectionArgs);
                assertNull(sortOrder);

                return null;
            }
        });

        BusStopDatabase.getServiceColours(mockContext, null);
        BusStopDatabase.getServiceColours(mockContext, new String[] { });
    }

    /**
     * Test that the query arguments are correct for when the services array is populated with
     * service names in {@link BusStopDatabase#getServiceColours(Context, String[])}.
     */
    @Test
    public void testGetServiceColoursWithNonEmptyServices() {
        final String[] services = new String[] {
                "1",
                "3",
                "44A",
                "100",
                "TRAM"
        };

        mockContentResolver.addProvider(BusStopContract.AUTHORITY, new MockContentProvider() {
            @Override
            public Cursor query(final Uri uri, final String[] projection, final String selection,
                    final String[] selectionArgs, final String sortOrder) {
                assertEquals(BusStopContract.Services.CONTENT_URI, uri);
                assertArrayEquals(new String[] {
                        BusStopContract.Services.NAME,
                        BusStopContract.Services.COLOUR
                }, projection);
                assertEquals("hex_colour IS NOT NULL AND name IN (?,?,?,?,?)", selection);
                assertArrayEquals(services, selectionArgs);
                assertNull(sortOrder);

                return null;
            }
        });

        BusStopDatabase.getServiceColours(mockContext, services);
    }

    /**
     * Test that {@link BusStopDatabase#getServiceColours(Context, String[])} returns {@code null}
     * when the {@link Cursor} is {@code null}.
     */
    @Test
    public void testGetServiceColoursWithNullCursor() {
        mockContentResolver.addProvider(BusStopContract.AUTHORITY, new MockContentProvider() {
            @Override
            public Cursor query(final Uri uri, final String[] projection, final String selection,
                    final String[] selectionArgs, final String sortOrder) {
                return null;
            }
        });

        final String[] services = new String[] {
                "1",
                "3",
                "44A",
                "100",
                "TRAM"
        };

        assertNull(BusStopDatabase.getServiceColours(mockContext, services));
    }

    /**
     * Test that {@link BusStopDatabase#getServiceColours(Context, String[])} returns a
     * non-{@code null} {@link Map} when the {@link Cursor} is non-{@code null} but empty.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetServiceColoursWithEmptyCursor() {
        mockContentResolver.addProvider(BusStopContract.AUTHORITY, new MockContentProvider() {
            @Override
            public Cursor query(final Uri uri, final String[] projection, final String selection,
                    final String[] selectionArgs, final String sortOrder) {
                return new MatrixCursor(new String[] {
                        BusStopContract.Services.NAME,
                        BusStopContract.Services.COLOUR
                });
            }
        });

        final String[] services = new String[] {
                "1",
                "3",
                "44A",
                "100",
                "TRAM"
        };

        final Map<String, String> colours =
                BusStopDatabase.getServiceColours(mockContext, services);
        assertNotNull(colours);
        assertTrue(colours.isEmpty());
        colours.put("a", "b");
    }

    /**
     * Test that {@link BusStopDatabase#getServiceColours(Context, String[])} returns a fully
     * populated {@link Map} when the {@link Cursor} returns data.
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testGetServiceColoursWithPopulatedCursor() {
        mockContentResolver.addProvider(BusStopContract.AUTHORITY, new MockContentProvider() {
            @Override
            public Cursor query(final Uri uri, final String[] projection, final String selection,
                    final String[] selectionArgs, final String sortOrder) {
                final MatrixCursor cursor = new MatrixCursor(new String[] {
                        BusStopContract.Services.NAME,
                        BusStopContract.Services.COLOUR
                });

                cursor.addRow(new String[] { "1", "#000001" });
                cursor.addRow(new String[] { "3", "#000002" });
                cursor.addRow(new String[] { "44A", "#000003" });
                cursor.addRow(new String[] { "100", "#000004" });
                cursor.addRow(new String[] { "TRAM", "#000005" });

                return cursor;
            }
        });

        final String[] services = new String[] {
                "1",
                "3",
                "44A",
                "100",
                "TRAM"
        };

        final Map<String, String> colours =
                BusStopDatabase.getServiceColours(mockContext, services);
        assertNotNull(colours);
        assertEquals(5, colours.size());
        assertEquals("#000001", colours.get("1"));
        assertEquals("#000002", colours.get("3"));
        assertEquals("#000003", colours.get("44A"));
        assertEquals("#000004", colours.get("100"));
        assertEquals("#000005", colours.get("TRAM"));
        colours.put("a", "b");
    }
}
