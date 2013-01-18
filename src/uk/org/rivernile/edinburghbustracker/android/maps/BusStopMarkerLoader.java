/*
 * Copyright (C) 2012 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.maps;

import android.content.Context;
import android.database.Cursor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.util.HashMap;
import uk.org.rivernile.android.utils.SimpleResultLoader;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .ServicesChooserDialogFragment;

/**
 * This Loader retrieves the bus stops for a given area from the bus stop
 * database and outputs the bus stops as a HashMap of MarkerOptions objects.
 * 
 * Optionally, a service filter can be specified where only services that are
 * contained in the filter are returned.
 * 
 * @author Niall Scott
 */
public class BusStopMarkerLoader
        extends SimpleResultLoader<HashMap<String, MarkerOptions>> {
    
    private static final int ZOOM_LEVEL_STANDARD = 14;
    private static final int ZOOM_LEVEL_FILTERED = 9;
    
    private final BusStopDatabase bsd;
    
    private final double minX;
    private final double minY;
    private final double maxX;
    private final double maxY;
    private final float zoom;
    private String[] filteredServices;
    
    /**
     * Create a new BusStopMarkerLoader.
     * 
     * @param context A Context instance.
     * @param minX The minimum X coordinate.
     * @param minY The minimum Y coordinate.
     * @param maxX The maximum X coordinate.
     * @param maxY The maximum Y coordinate.
     * @param zoom The zoom level from Google Maps.
     */
    public BusStopMarkerLoader(final Context context, final double minX,
            final double minY, final double maxX, final double maxY,
            final float zoom) {
        super(context);
        
        bsd = BusStopDatabase.getInstance(context.getApplicationContext());
        
        this.minX = minX;
        this.minY = minY;
        this.maxX = maxX;
        this.maxY = maxY;
        this.zoom = zoom;
    }
    
    /**
     * Create a new BusStopMarkerLoader.
     * 
     * @param context A Context instance.
     * @param minX The minimum X coordinate.
     * @param minY The minimum Y coordinate.
     * @param maxX The maximum X coordinate.
     * @param maxY The maximum Y coordinate.
     * @param zoom The zoom level from Google Maps.
     * @param filteredServices A String array of the only services to show.
     */
    public BusStopMarkerLoader(final Context context, final double minX,
            final double minY, final double maxX, final double maxY,
            final float zoom, final String[] filteredServices) {
        this(context, minX, minY, maxX, maxY, zoom);
        
        this.filteredServices = filteredServices;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashMap<String, MarkerOptions> loadInBackground() {
        final HashMap<String, MarkerOptions> result =
                new HashMap<String, MarkerOptions>();
        
        // Has a filter been applied?
        final boolean isFiltered = filteredServices != null &&
                filteredServices.length > 0;
        
        // What should the mininum zoom level be?
        final int minZoom = isFiltered ? ZOOM_LEVEL_FILTERED :
                ZOOM_LEVEL_STANDARD;
        
        // Does the zoom level satisfy the mininum zoom requirement?
        if(zoom < minZoom) return result;
        
        // When dealing with the Cursor externally to BusStopDatabase, then the
        // BusStopDatabase instance needs to be synchronized. This is so that
        // the database cannot be updated while it is being used.
        synchronized(bsd) {
            Cursor c;
            
            // What query to execute depends on whether filtering has been
            // enabled or not.
            if(isFiltered) {
                c = bsd.getFilteredStopsByCoords(minX, minY, maxX, maxY,
                        ServicesChooserDialogFragment
                            .getChosenServicesForSql(filteredServices));
            } else {
                c = bsd.getBusStopsByCoords(minX, minY, maxX, maxY);
            }
            
            if(c != null) {
                MarkerOptions mo;
                int orientation;
                String locality, stopCode;
                final StringBuilder sb = new StringBuilder();
                
                // Loop through all rows in the Cursor.
                while(c.moveToNext()) {
                    // Create a new MarkerOptions...
                    mo = new MarkerOptions();
                    // ...and set its options.
                    mo.draggable(false);
                    mo.anchor(0.5f, 1.f);
                    
                    // Get the stopCode.
                    stopCode = c.getString(0);
                    
                    // Set the latitude and longitude.
                    mo.position(new LatLng(c.getDouble(2), c.getDouble(3)));
                    
                    locality = c.getString(5);
                    sb.append(c.getString(1));
                    // Check to see if a locality is available.
                    if(locality != null) {
                        sb.append(',').append(' ').append(locality);
                    }
                    
                    sb.append(' ').append('(').append(stopCode).append(')');
                    mo.title(sb.toString());
                    sb.setLength(0);
                    
                    orientation = c.getInt(4);
                    
                    // The icon to use depends on the orientation.
                    switch(orientation) {
                        case 0:
                            mo.icon(BitmapDescriptorFactory.fromResource(
                                    R.drawable.mapmarker_n));
                            break;
                        case 1:
                            mo.icon(BitmapDescriptorFactory.fromResource(
                                    R.drawable.mapmarker_ne));
                            break;
                        case 2:
                            mo.icon(BitmapDescriptorFactory.fromResource(
                                    R.drawable.mapmarker_e));
                            break;
                        case 3:
                            mo.icon(BitmapDescriptorFactory.fromResource(
                                    R.drawable.mapmarker_se));
                            break;
                        case 4:
                            mo.icon(BitmapDescriptorFactory.fromResource(
                                    R.drawable.mapmarker_s));
                            break;
                        case 5:
                            mo.icon(BitmapDescriptorFactory.fromResource(
                                    R.drawable.mapmarker_sw));
                            break;
                        case 6:
                            mo.icon(BitmapDescriptorFactory.fromResource(
                                    R.drawable.mapmarker_w));
                            break;
                        case 7:
                            mo.icon(BitmapDescriptorFactory.fromResource(
                                    R.drawable.mapmarker_nw));
                            break;
                        default:
                            mo.icon(BitmapDescriptorFactory.fromResource(
                                    R.drawable.mapmarker));
                            break;
                    }
                    
                    // Add the marker to the result HashMap.
                    result.put(stopCode, mo);
                }
                
                // Remember to close the Cursor object.
                c.close();
            }
        }
        
        return result;
    }
}