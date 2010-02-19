/*
 * Copyright (C) 2009 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android;

import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import java.util.ArrayList;

public class BusStopMapOverlay extends ItemizedOverlay<BusStopOverlayItem> {

    private ArrayList<BusStopOverlayItem> items =
            new ArrayList<BusStopOverlayItem>();
    private MapActivity context;
    private MapView mapView;
    private int lastZoom;
    private GeoPoint lastCenter;
    private BusStopOverlayItem selectedItem;

    public BusStopMapOverlay(final Drawable defaultMarker,
            final MapActivity context, final MapView mapView)
    {
        super(boundCenterBottom(defaultMarker));
        this.context = context;
        this.mapView = mapView;
        lastZoom = mapView.getZoomLevel();
        lastCenter = mapView.getMapCenter();
        populate();
    }

    @Override
    protected BusStopOverlayItem createItem(final int i) {
        return items.get(i);
    }

    @Override
    public int size() {
        return items.size();
    }

    @Override
    protected boolean onTap(final int index) {
        selectedItem = items.get(index);
        context.showDialog(0);
        return true;
    }

    public void doPopulateBusStops() {
        setLastFocusedIndex(-1);
        items.clear();
        populate();
        if(mapView.getZoomLevel() >= 16) {
            int minX, minY, maxX, maxY, latSpan, longSpan;
            GeoPoint g = mapView.getMapCenter();
            latSpan = (mapView.getLatitudeSpan() / 2) + 1;
            longSpan = (mapView.getLongitudeSpan() / 2) + 1;
            minX = g.getLatitudeE6() + latSpan;
            minY = g.getLongitudeE6() - longSpan;
            maxX = g.getLatitudeE6() - latSpan;
            maxY = g.getLongitudeE6() + longSpan;
            BusStopDatabase bsd = BusStopDatabase.getInstance(context);
            Cursor c = bsd.getBusStopsByCoords(minX, minY, maxX, maxY);
            if(c.getCount() > 0) {
                String stopCode, stopName;
                int x, y;
                BusStopOverlayItem oi;
                while(!c.isLast()) {
                    c.moveToNext();
                    stopCode = c.getString(0);
                    stopName = c.getString(1);
                    x = c.getInt(2);
                    y = c.getInt(3);
                    oi = new BusStopOverlayItem(new GeoPoint(x, y), stopCode,
                        stopName);
                    items.add(oi);
                }
                populate();
            }
            c.close();
        }
    }

    @Override
    public void draw(final Canvas canvas, final MapView mapView,
            final boolean shadow)
    {
        super.draw(canvas, mapView, shadow);
        int zoom = mapView.getZoomLevel();
        GeoPoint g = mapView.getMapCenter();

        if(zoom != lastZoom || !g.equals(lastCenter)) {
            lastZoom = zoom;
            lastCenter = g;
            doPopulateBusStops();
        }
    }

    public BusStopOverlayItem getSeletedItem() {
        return selectedItem;
    }
}