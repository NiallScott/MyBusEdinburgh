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

package uk.org.rivernile.android.bustracker.ui.bustimes.details;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import uk.org.rivernile.android.utils.MapsUtils;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This adapter displays stop location information plus a listing of services which service this
 * stop.
 *
 * @author Niall Scott
 */
class StopDetailsAdapter extends RecyclerView.Adapter {

    private static final int VIEW_TYPE_LOCATION = 1;
    private static final int VIEW_TYPE_SERVICE = 2;

    private final Context context;
    private final LayoutInflater inflater;
    private BusStopLocation location;

    /**
     * Create a new {@code StopDetailsAdapter}.
     *
     * @param context The {@link android.app.Activity} instance.
     */
    StopDetailsAdapter(@NonNull final Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case VIEW_TYPE_LOCATION:
                return new LocationViewHolder(
                        inflater.inflate(R.layout.stopdetails_location_item, parent, false));
            case VIEW_TYPE_SERVICE:
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof LocationViewHolder) {
            ((LocationViewHolder) holder).populate(location);
        }
    }

    @Override
    public int getItemCount() {
        return location != null ? 1 : 0;
    }

    @Override
    public int getItemViewType(final int position) {
        return position == 0 && location != null ? VIEW_TYPE_LOCATION : VIEW_TYPE_SERVICE;
    }

    /**
     * Set the stop location for displaying stop location information.
     *
     * @param location The stop location.
     */
    void setBusStopLocation(@Nullable final BusStopLocation location) {
        if (this.location != location) {
            this.location = location;
            notifyDataSetChanged();
        }
    }

    /**
     * This view holder populates the location view.
     */
    private class LocationViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback {

        private final MapView mapView;
        private GoogleMap map;
        private LatLng latLon;
        private int stopOrientation;
        private Marker marker;

        /**
         * Create a new {@code LocationViewHolder}.
         *
         * @param itemView The {@link View} for this view holder.
         */
        private LocationViewHolder(@NonNull final View itemView) {
            super(itemView);

            mapView = (MapView) itemView.findViewById(R.id.mapView);

            if (MapsUtils.isGoogleMapsAvailable(context)) {
                mapView.onCreate(null);
                mapView.getMapAsync(this);
            } else {
                mapView.setVisibility(View.GONE);
            }
        }

        @Override
        public void onMapReady(final GoogleMap googleMap) {
            map = googleMap;
            map.getUiSettings().setMapToolbarEnabled(false);
            populateMap();
        }

        /**
         * Populate the data in this view holder.
         *
         * @param location The location data to populate in this view holder.
         */
        private void populate(@Nullable final BusStopLocation location) {
            if (location != null) {
                latLon = new LatLng(location.getLatitude(), location.getLongitude());
                stopOrientation = location.getOrientation();
            } else {
                latLon = null;
                stopOrientation = -1;
            }

            populateMap();
        }

        /**
         * Populate the map.
         */
        private void populateMap() {
            if (marker != null) {
                marker.remove();
                marker = null;
            }

            if (map != null && latLon != null) {
                map.moveCamera(CameraUpdateFactory.newLatLng(latLon));
                final MarkerOptions mo = new MarkerOptions().position(latLon);
                MapsUtils.applyStopDirectionToMarker(mo, stopOrientation);
                marker = map.addMarker(mo);
                mapView.setVisibility(View.VISIBLE);
            } else {
                mapView.setVisibility(View.GONE);
            }
        }
    }
}
