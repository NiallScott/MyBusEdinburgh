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
import android.location.Address;
import android.location.Geocoder;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import uk.org.rivernile.android.utils.SimpleResultLoader;

/**
 * The GeoSearchLoader takes a query parameter and uses the system Geocoding
 * APIs to return locations within the boundaries which match the query.
 * 
 * @author Niall Scott
 */
public class GeoSearchLoader
        extends SimpleResultLoader<HashSet<MarkerOptions>> {
    
    private static final double BOUNDS_NORTH = 56.000000;
    private static final double BOUNDS_SOUTH = 55.819447;
    private static final double BOUNDS_WEST = -3.403363;
    private static final double BOUNDS_EAST = -2.864143;
    private static final int MAX_GEO_RESULTS = 10;
    
    private final String query;
    
    /**
     * Create a new GeoSearchLoader, specifying the query to use on the
     * Geocoding service.
     * 
     * @param context A Context instance.
     * @param query The query to look up.
     */
    public GeoSearchLoader(final Context context, final String query) {
        super(context);
        
        if(query == null) {
            throw new IllegalArgumentException("query must not be null.");
        }
        
        this.query = query;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public HashSet<MarkerOptions> loadInBackground() {
        final HashSet<MarkerOptions> result = new HashSet<MarkerOptions>();
        final Geocoder geo = new Geocoder(getContext());
        final List<Address> addressList;
        
        try {
            addressList = geo.getFromLocationName(query, MAX_GEO_RESULTS,
                    BOUNDS_SOUTH, BOUNDS_WEST, BOUNDS_NORTH, BOUNDS_EAST);
        } catch(IOException e) {
            return result;
        }
        
        if(addressList != null) {
            MarkerOptions mo;
            final StringBuilder sb = new StringBuilder();
            String locality, postalCode;
            
            for(Address a : addressList) {
                mo = new MarkerOptions();
                mo.draggable(false);
                mo.anchor(0.5f, 1.f);
                mo.position(new LatLng(a.getLatitude(), a.getLongitude()));
                mo.icon(BitmapDescriptorFactory.defaultMarker());
                mo.title(a.getFeatureName());
                
                locality = a.getLocality();
                postalCode = a.getPostalCode();
                
                if(locality != null && postalCode != null) {
                    sb.append(locality).append(',').append(' ')
                            .append(postalCode);
                    mo.snippet(sb.toString());
                } else if(locality != null) {
                    mo.snippet(locality);
                } else if(postalCode != null) {
                    mo.snippet(postalCode);
                }
                
                result.add(mo);
                sb.setLength(0);
            }
        }
        
        return result;
    }
}