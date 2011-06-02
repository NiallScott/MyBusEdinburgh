/*
 * Copyright (C) 2009 - 2011 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.mapoverlays;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
import java.util.ArrayList;
import uk.org.rivernile.edinburghbustracker.android
        .AddEditFavouriteStopActivity;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.DisplayStopDataActivity;
import uk.org.rivernile.edinburghbustracker.android.Filterable;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.ServiceFilter;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;

public class BusStopMapOverlay extends ItemizedOverlay<BusStopOverlayItem>
        implements OnItemClickListener, Filterable {

    private ArrayList<BusStopOverlayItem> items =
            new ArrayList<BusStopOverlayItem>();
    private MapActivity context;
    private MapView mapView;
    private int lastZoom;
    private GeoPoint lastCenter;

    private Dialog stopDialog;
    private BusStopOverlayItem currentItem;
    private boolean isFavourite;
    private ArrayAdapter<String> ad;

    private Dialog confirmDialog;
    private ServiceFilter serviceFilter;

    public BusStopMapOverlay(final Drawable defaultMarker,
            final MapActivity context, final MapView mapView)
    {
        super(boundCenterBottom(defaultMarker));
        serviceFilter = ServiceFilter.getInstance(context);
        this.context = context;
        this.mapView = mapView;
        lastZoom = mapView.getZoomLevel();
        lastCenter = mapView.getMapCenter();
        doPopulateBusStops();
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
        currentItem = items.get(index);
        showStopDialog();
        return true;
    }

    public void doPopulateBusStops() {
        setLastFocusedIndex(-1);
        items.clear();
        populate();
        boolean filtered = serviceFilter.isFiltered();
        int zoomLevel = filtered ? 9 : 15;
        if(mapView.getZoomLevel() >= zoomLevel) {
            int minX, minY, maxX, maxY, latSpan, longSpan;
            GeoPoint g = mapView.getMapCenter();
            latSpan = (mapView.getLatitudeSpan() / 2) + 1;
            longSpan = (mapView.getLongitudeSpan() / 2) + 1;
            minX = g.getLatitudeE6() + latSpan;
            minY = g.getLongitudeE6() - longSpan;
            maxX = g.getLatitudeE6() - latSpan;
            maxY = g.getLongitudeE6() + longSpan;
            BusStopDatabase bsd = BusStopDatabase.getInstance(context);
            Cursor c;
            if(filtered) {
                c = bsd.getFilteredStopsByCoords(minX, minY, maxX, maxY,
                        serviceFilter.toString());
            } else {
                c = bsd.getBusStopsByCoords(minX, minY, maxX, maxY);
            }
            if(c.getCount() > 0) {
                while(!c.isLast()) {
                    c.moveToNext();
                    items.add(new BusStopOverlayItem(new GeoPoint(c.getInt(2),
                            c.getInt(3)), c.getString(0), c.getString(1)));
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

    public String getCurrentStopCode() {
        if(stopDialog != null && stopDialog.isShowing() && currentItem != null)
        {
            return currentItem.getStopCode();
        }
        return null;
    }

    public void setCurrentStopCodeAndShowDialog(final String stopCode) {
        if(currentItem != null) return;

        BusStopDatabase bsd = BusStopDatabase.getInstance(context);
        Cursor c = bsd.getBusStopByCode(stopCode);
        if(c.getCount() != 1) return;
        c.moveToFirst();
        currentItem = new BusStopOverlayItem(new GeoPoint(c.getInt(2),
                c.getInt(3)), c.getString(0), c.getString(1));
        c.close();
        showStopDialog();
    }

    public void mapResumed() {
        if(stopDialog != null && stopDialog.isShowing()) {
            stopDialog.dismiss();
            showStopDialog();
        }
        serviceFilter.setCallback(this);
    }

    private void createStopDialog() {
        stopDialog = new Dialog(context);
        stopDialog.setContentView(R.layout.mapdialog);
        stopDialog.setCancelable(true);
        ListView list =
            (ListView)stopDialog.findViewById(R.id.mapdialog_list_options);
        ad = new ArrayAdapter<String>(stopDialog.getContext(),
        android.R.layout.simple_list_item_1);
        list.setAdapter(ad);
        list.setOnItemClickListener(this);
    }

    private void createConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true)
            .setTitle(R.string.favouritestops_dialog_confirm_title)
            .setPositiveButton(R.string.okay,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int id)
                {
                    SettingsDatabase.getInstance(context)
                        .deleteFavouriteStop(currentItem.getStopCode());
                    isFavourite = false;
                    showStopDialog();
                }
            }).setNegativeButton(R.string.cancel,
            new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialog, final int id)
                {
                    dialog.dismiss();
                    showStopDialog();
                }
            }
        );
        confirmDialog = builder.create();
    }

    private void showStopDialog() {
        if(stopDialog == null) createStopDialog();
        if(currentItem == null) return;
        isFavourite = SettingsDatabase.getInstance(context)
                .getFavouriteStopExists(currentItem.getStopCode());
        String[] services = BusStopDatabase.getInstance(context)
                    .getBusServicesForStop(currentItem.getStopCode());
        TextView tv = (TextView)stopDialog.findViewById(
                R.id.mapdialog_text_services);
        stopDialog.setTitle(currentItem.getStopCode() + " " +
                currentItem.getStopName());
        if(services == null) {
            tv.setText(R.string.map_dialog_noservices);
        } else {
            String s = context.getString(R.string.services) + ": ";
            for(int i = 0; i < services.length; i++) {
                if(i == (services.length - 1)) {
                    s = s + services[i];
                } else {
                    s = s + services[i] + ", ";
                }
            }
            tv.setText(s);
        }
        ad.clear();
        ad.add(context.getString(R.string.map_dialog_showtimes));
        if(isFavourite) {
            ad.add(context.getString(R.string.displaystopdata_menu_remfav));
        } else {
            ad.add(context.getString(R.string.displaystopdata_menu_addfav));
        }
        ad.add(context.getString(R.string.map_dialog_close));
        stopDialog.show();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onItemClick(final AdapterView<?> l, final View view,
            final int position, final long id)
    {
        switch(position) {
            case 0:
                Intent intent = new Intent(context,
                        DisplayStopDataActivity.class);
                intent.setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA);
                intent.putExtra("stopCode", currentItem.getStopCode());
                context.startActivity(intent);
                break;
            case 1:
                if(isFavourite) {
                    stopDialog.dismiss();
                    if(confirmDialog == null) createConfirmDialog();
                    confirmDialog.show();
                } else {
                    Intent intent2 = new Intent(context,
                            AddEditFavouriteStopActivity.class);
                    intent2.setAction(AddEditFavouriteStopActivity
                            .ACTION_ADD_EDIT_FAVOURITE_STOP);
                    intent2.putExtra("stopCode", currentItem.getStopCode());
                    intent2.putExtra("stopName", currentItem.getStopName());
                    context.startActivity(intent2);
                }
                break;
            case 2:
                stopDialog.dismiss();
                break;
        }
    }
    
    @Override
    public void onFilterSet() {
        doPopulateBusStops();
        mapView.invalidate();
    }
}