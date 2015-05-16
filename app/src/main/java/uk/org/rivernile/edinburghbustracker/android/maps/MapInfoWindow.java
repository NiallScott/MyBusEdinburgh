/*
 * Copyright (C) 2013 - 2015 Niall 'Rivernile' Scott
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
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.model.Marker;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * The {@code MapInfoWindow} supplies a custom {@link View} for an {@code InfoWindow} if the bus
 * stop marker {@code InfoWindow} is being shown. This allows the text for services not to ellipsise
 * (i.e. be multi-line). Also, the service colouring can take place on the text.
 *
 * <p>
 *     If the Marker is any other type, then the default Google Maps {@code InfoWindow} is shown
 *     instead.
 * </p>
 * 
 * @author Niall Scott
 */
public class MapInfoWindow implements GoogleMap.InfoWindowAdapter {
    
    private static final Pattern STOP_CODE_PATTERN = Pattern.compile("(\\d{8})\\)$");
    
    private final LayoutInflater inflater;
    private View rootView;
    
    /**
     * Create a new {@code MapInfoWindow}.
     * 
     * @param context A {@link Context} instance, must be non-{@code null}.
     */
    public MapInfoWindow(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("The context must not be null.");
        }
        
        // Cache the LayoutInflater for later use.
        inflater = LayoutInflater.from(context);
    }

    @Override
    public View getInfoWindow(final Marker marker) {
        // Since we don't want to modify the window decoration, return null here so that Google Maps
        // uses its own implementation.
        return null;
    }

    @Override
    public View getInfoContents(final Marker marker) {
        final Matcher matcher = STOP_CODE_PATTERN.matcher(marker.getTitle());

        // If the Marker is a bus stop, we want to provide our own View.
        if (matcher.find()) {
            // Inflate the View from XML.
            if (rootView == null) {
                rootView = inflater.inflate(R.layout.map_info_window, null, false);
            }
            
            TextView txt = (TextView) rootView.findViewById(R.id.txtTitle);
            // Set the title TextView.
            txt.setText(marker.getTitle());
            
            txt = (TextView) rootView.findViewById(R.id.txtSnippet);
            // Set the snippet TextView to that of a coloured list String.
            txt.setText(BusStopDatabase.getColouredServiceListString(marker.getSnippet()));
            
            return rootView;
        }
        
        // If not a bus stop, return null so that Google Maps uses the default InfoWindow View.
        return null;
    }
}