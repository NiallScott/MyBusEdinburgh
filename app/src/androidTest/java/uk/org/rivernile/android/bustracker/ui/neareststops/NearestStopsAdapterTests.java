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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.ArrayList;
import java.util.List;

import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * Tests for {@link NearestStopsAdapter}.
 *
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class NearestStopsAdapterTests {

    private NearestStopsAdapter adapter;

    @Before
    public void setUp() {
        final Context context = InstrumentationRegistry.getTargetContext();
        context.setTheme(R.style.MyBusEdinburgh);
        adapter = new NearestStopsAdapter(context);
    }

    @After
    public void tearDown() {
        adapter = null;
    }

    /**
     * Test the default state of {@link NearestStopsAdapter}.
     */
    @Test
    public void testDefault() {
        assertTrue(adapter.hasStableIds());
        assertEquals(0, adapter.getItemCount());
        assertEquals(0, adapter.getItemId(0));
        assertNull(adapter.getItem(0));
    }

    /**
     * Test the adapter coping with an empty {@link ArrayList} of {@link SearchResult}s.
     */
    @Test
    public void testNoItems() {
        final DataObserver observer = new DataObserver();
        adapter.registerAdapterDataObserver(observer);
        adapter.setSearchResults(new ArrayList<SearchResult>(1));

        assertTrue(observer.onChangeCalled);
        assertEquals(0, adapter.getItemCount());
        assertNull(adapter.getItem(0));
    }

    /**
     * Test the default state of {@link View}s with no data to populate them with.
     */
    @Test
    public void testViewWithNoItems() {
        final NearestStopsAdapter.ViewHolder viewHolder = adapter.createViewHolder(null, 0);
        adapter.bindViewHolder(viewHolder, 0);
        final View itemView = viewHolder.itemView;
        final ImageView imgDirection = (ImageView) itemView.findViewById(R.id.imgDirection);
        final TextView text1 = (TextView) itemView.findViewById(android.R.id.text1);
        final TextView text2 = (TextView) itemView.findViewById(android.R.id.text2);
        final TextView txtDistance = (TextView) itemView.findViewById(R.id.txtDistance);

        assertNull(imgDirection.getDrawable());
        assertNull(imgDirection.getContentDescription());
        assertTrue(TextUtils.isEmpty(text1.getText().toString()));
        assertTrue(TextUtils.isEmpty(text2.getText().toString()));
        assertTrue(TextUtils.isEmpty(txtDistance.getText().toString()));
    }

    /**
     * Test populating the {@link NearestStopsAdapter} with a single item.
     */
    @Test
    public void testSingleItem() {
        final DataObserver observer = new DataObserver();
        adapter.registerAdapterDataObserver(observer);

        final SearchResult result = new SearchResult("123456", "Stop name", "1, 2, 3", 1.2f, 4,
                "Area");
        final ArrayList<SearchResult> items = new ArrayList<>(1);
        items.add(result);
        adapter.setSearchResults(items);

        assertTrue(observer.onChangeCalled);
        assertEquals(1, adapter.getItemCount());
        assertEquals(result, adapter.getItem(0));
        assertEquals(result.hashCode(), adapter.getItemId(0));
        assertNull(adapter.getItem(1));
        assertEquals(0, adapter.getItemId(1));
        assertNull(adapter.getItem(-1));

        final NearestStopsAdapter.ViewHolder vh = adapter.createViewHolder(null, 0);
        final View itemView = vh.itemView;
        final ImageView imgDirection = (ImageView) itemView.findViewById(R.id.imgDirection);
        final TextView text1 = (TextView) itemView.findViewById(android.R.id.text1);
        final TextView text2 = (TextView) itemView.findViewById(android.R.id.text2);
        final TextView txtDistance = (TextView) itemView.findViewById(R.id.txtDistance);

        assertNull(imgDirection.getDrawable());
        assertNull(imgDirection.getContentDescription());
        assertTrue(TextUtils.isEmpty(text1.getText().toString()));
        assertTrue(TextUtils.isEmpty(text2.getText().toString()));
        assertTrue(TextUtils.isEmpty(txtDistance.getText().toString()));

        adapter.bindViewHolder(vh, 0);

        assertNotNull(imgDirection.getDrawable());
        assertEquals("Faces south", imgDirection.getContentDescription().toString());
        assertEquals("Stop name, Area (123456)", text1.getText().toString());
        assertEquals("1, 2, 3", text2.getText().toString());
        assertEquals("1 m", txtDistance.getText().toString());
    }

    /**
     * Test populating the {@link NearestStopsAdapter} with multiple items.
     */
    @Test
    public void testMultipleItems() {
        final DataObserver observer = new DataObserver();
        adapter.registerAdapterDataObserver(observer);

        final SearchResult result1 = new SearchResult("123456", "Stop name", "1, 2, 3", 1.2f, 4,
                "Area");
        final SearchResult result2 = new SearchResult("987654", "Number 2", "10, 34, 100", 5.45f,
                2, null);
        final SearchResult result3 = new SearchResult("246801", "Test stop", "TRAM",
                120.9f, -1, "West");
        final ArrayList<SearchResult> items = new ArrayList<>(3);
        items.add(result1);
        items.add(result2);
        items.add(result3);
        adapter.setSearchResults(items);

        assertTrue(observer.onChangeCalled);
        assertEquals(3, adapter.getItemCount());
        assertEquals(result1, adapter.getItem(0));
        assertEquals(result2, adapter.getItem(1));
        assertEquals(result3, adapter.getItem(2));
        assertNull(adapter.getItem(3));
        assertEquals(result1.hashCode(), adapter.getItemId(0));
        assertEquals(result2.hashCode(), adapter.getItemId(1));
        assertEquals(result3.hashCode(), adapter.getItemId(2));
        assertEquals(0, adapter.getItemId(3));
        assertNull(adapter.getItem(-1));

        final NearestStopsAdapter.ViewHolder vh1 = adapter.createViewHolder(null, 0);
        final View itemView1 = vh1.itemView;
        final ImageView imgDirection1 = (ImageView) itemView1.findViewById(R.id.imgDirection);
        final TextView text1_1 = (TextView) itemView1.findViewById(android.R.id.text1);
        final TextView text2_1 = (TextView) itemView1.findViewById(android.R.id.text2);
        final TextView txtDistance1 = (TextView) itemView1.findViewById(R.id.txtDistance);

        assertNull(imgDirection1.getDrawable());
        assertNull(imgDirection1.getContentDescription());
        assertTrue(TextUtils.isEmpty(text1_1.getText().toString()));
        assertTrue(TextUtils.isEmpty(text2_1.getText().toString()));
        assertTrue(TextUtils.isEmpty(txtDistance1.getText().toString()));

        final NearestStopsAdapter.ViewHolder vh2 = adapter.createViewHolder(null, 0);
        final View itemView2 = vh2.itemView;
        final ImageView imgDirection2 = (ImageView) itemView2.findViewById(R.id.imgDirection);
        final TextView text1_2 = (TextView) itemView2.findViewById(android.R.id.text1);
        final TextView text2_2 = (TextView) itemView2.findViewById(android.R.id.text2);
        final TextView txtDistance2 = (TextView) itemView2.findViewById(R.id.txtDistance);

        assertNull(imgDirection2.getDrawable());
        assertNull(imgDirection2.getContentDescription());
        assertTrue(TextUtils.isEmpty(text1_2.getText().toString()));
        assertTrue(TextUtils.isEmpty(text2_2.getText().toString()));
        assertTrue(TextUtils.isEmpty(txtDistance2.getText().toString()));

        adapter.bindViewHolder(vh1, 0);

        assertNotNull(imgDirection1.getDrawable());
        assertEquals("Faces south", imgDirection1.getContentDescription().toString());
        assertEquals("Stop name, Area (123456)", text1_1.getText().toString());
        assertEquals("1, 2, 3", text2_1.getText().toString());
        assertEquals("1 m", txtDistance1.getText().toString());

        adapter.bindViewHolder(vh2, 1);

        assertNotNull(imgDirection2.getDrawable());
        assertEquals("Faces east", imgDirection2.getContentDescription().toString());
        assertEquals("Number 2 (987654)", text1_2.getText().toString());
        assertEquals("10, 34, 100", text2_2.getText().toString());
        assertEquals("5 m", txtDistance2.getText().toString());

        adapter.bindViewHolder(vh1, 2);

        assertNotNull(imgDirection1.getDrawable());
        assertEquals("Faces unknown direction", imgDirection1.getContentDescription().toString());
        assertEquals("Test stop, West (246801)", text1_1.getText().toString());
        assertEquals("TRAM", text2_1.getText().toString());
        assertEquals("120 m", txtDistance1.getText().toString());
    }

    /**
     * Test that {@link NearestStopsAdapter} does not call
     * {@link RecyclerView.Adapter#notifyDataSetChanged()} when the same object is set in
     * {@link NearestStopsAdapter#setSearchResults(List)}.
     */
    @Test
    public void testSetSearchResultDoesntRefreshDataOnSameObject() {
        final DataObserver observer = new DataObserver();
        adapter.registerAdapterDataObserver(observer);
        final ArrayList<SearchResult> results = new ArrayList<>(1);
        adapter.setSearchResults(results);
        assertTrue(observer.onChangeCalled);

        observer.onChangeCalled = false;
        adapter.setSearchResults(results);
        assertFalse(observer.onChangeCalled);
    }

    /**
     * Test that {@link NearestStopsAdapter#getDirectionDrawableResourceId(int)} returns the
     * correct direction {@link android.graphics.drawable.Drawable} resource IDs for the given
     * orientation values.
     */
    @Test
    public void testGetDirectionDrawableResourceId() {
        assertEquals(R.drawable.mapmarker, NearestStopsAdapter.getDirectionDrawableResourceId(-1));
        assertEquals(R.drawable.mapmarker_n,
                NearestStopsAdapter.getDirectionDrawableResourceId(0));
        assertEquals(R.drawable.mapmarker_ne,
                NearestStopsAdapter.getDirectionDrawableResourceId(1));
        assertEquals(R.drawable.mapmarker_e,
                NearestStopsAdapter.getDirectionDrawableResourceId(2));
        assertEquals(R.drawable.mapmarker_se,
                NearestStopsAdapter.getDirectionDrawableResourceId(3));
        assertEquals(R.drawable.mapmarker_s,
                NearestStopsAdapter.getDirectionDrawableResourceId(4));
        assertEquals(R.drawable.mapmarker_sw,
                NearestStopsAdapter.getDirectionDrawableResourceId(5));
        assertEquals(R.drawable.mapmarker_w,
                NearestStopsAdapter.getDirectionDrawableResourceId(6));
        assertEquals(R.drawable.mapmarker_nw,
                NearestStopsAdapter.getDirectionDrawableResourceId(7));
        assertEquals(R.drawable.mapmarker, NearestStopsAdapter.getDirectionDrawableResourceId(8));
    }

    /**
     * This is used to record when callback methods are called to ensure that the adapter calls its
     * data change notifier.
     */
    private static class DataObserver extends RecyclerView.AdapterDataObserver {

        private boolean onChangeCalled;
        private boolean onItemRangeChangeCalled;
        private boolean onItemRangeInsertedCalled;
        private boolean onItemRangeRemovedCalled;
        private boolean onItemRangeMovedCalled;

        @Override
        public void onChanged() {
            onChangeCalled = true;
        }

        @Override
        public void onItemRangeChanged(final int positionStart, final int itemCount) {
            onItemRangeChangeCalled = true;
        }

        @Override
        public void onItemRangeInserted(final int positionStart, final int itemCount) {
            onItemRangeInsertedCalled = true;
        }

        @Override
        public void onItemRangeRemoved(final int positionStart, final int itemCount) {
            onItemRangeRemovedCalled = true;
        }

        @Override
        public void onItemRangeMoved(final int fromPosition, final int toPosition,
                final int itemCount) {
            onItemRangeMovedCalled = true;
        }
    }
}
