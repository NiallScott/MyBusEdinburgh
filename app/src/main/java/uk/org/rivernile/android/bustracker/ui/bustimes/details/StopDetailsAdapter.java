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
import android.content.res.ColorStateList;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.ref.WeakReference;
import java.util.List;

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
    private WeakReference<OnItemClickListener> clickListenerRef;
    private BusStopLocation location;
    private List<Service> services;

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
                return new ServiceViewHolder(
                        inflater.inflate(R.layout.stopdetails_service_item, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        if (holder instanceof LocationViewHolder) {
            ((LocationViewHolder) holder).populate(location);
        } else if (holder instanceof ServiceViewHolder) {
            ((ServiceViewHolder) holder).populate(getServiceForAdapterPosition(position));
        }
    }

    @Override
    public int getItemCount() {
        int size = services != null ? services.size() : 0;

        if (location != null) {
            size++;
        }

        return size;
    }

    @Override
    public int getItemViewType(final int position) {
        return position == 0 && location != null ? VIEW_TYPE_LOCATION : VIEW_TYPE_SERVICE;
    }

    /**
     * Set the listener to be called when the user has clicked on an item.
     *
     * @param listener The listener that is called when the user has clicked on an item.
     */
    void setOnItemClickedListener(@Nullable final OnItemClickListener listener) {
        clickListenerRef = listener != null ? new WeakReference<>(listener) : null;
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
     * Set the {@link List} of {@link Service}s to use in this adapter.
     *
     * @param services The {@link List} of {@link Service}s to use in this adapter.
     */
    void setServices(@Nullable final List<Service> services) {
        this.services = services;
        notifyDataSetChanged();
    }

    /**
     * Get the {@link Service} for the given adapter position, or {@code null} if there is no
     * {@link Service} at this position.
     *
     * @param position The position to get the {@link Service} for.
     * @return The {@link Service} for the given adapter position, or {@code null} if there is no
     * {@link Service} at this position.
     */
    @Nullable
    private Service getServiceForAdapterPosition(int position) {
        if (location != null) {
            position--;
        }

        return services != null && position >= 0 && position < services.size()
                ? services.get(position) : null;
    }

    /**
     * This {@link RecyclerView.ViewHolder} populates the location {@link View}.
     */
    private class LocationViewHolder extends RecyclerView.ViewHolder implements OnMapReadyCallback,
            GoogleMap.OnMapClickListener {

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
            map.setOnMapClickListener(this);
            populateMap();
        }

        @Override
        public void onMapClick(final LatLng latLng) {
            final int position = getAdapterPosition();

            if (position == RecyclerView.NO_POSITION) {
                return;
            }

            final OnItemClickListener clickListener =
                    clickListenerRef != null ? clickListenerRef.get() : null;

            if (clickListener != null) {
                clickListener.onMapClicked();
            }
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

    /**
     * This {@link RecyclerView.ViewHolder} populates {@link View}s relating to {@link Service}s.
     */
    private class ServiceViewHolder extends RecyclerView.ViewHolder
            implements View.OnClickListener {

        private final AppCompatTextView txtServiceName;
        private final TextView txtDescription;

        /**
         * Create a new {@code ServiceViewHolder}.
         *
         * @param itemView The {@link View} for this item.
         */
        private ServiceViewHolder(@NonNull final View itemView) {
            super(itemView);

            txtServiceName = (AppCompatTextView) itemView.findViewById(R.id.txtServiceName);
            txtDescription = (TextView) itemView.findViewById(R.id.txtDescription);

            itemView.setOnClickListener(this);
        }

        @Override
        public void onClick(final View view) {
            final int position = getAdapterPosition();

            if (position == RecyclerView.NO_POSITION) {
                return;
            }

            final Service service = getServiceForAdapterPosition(position);

            if (service == null) {
                return;
            }

            final OnItemClickListener clickListener =
                    clickListenerRef != null ? clickListenerRef.get() : null;

            if (clickListener != null) {
                clickListener.onServiceClicked(service.getServiceName());
            }
        }

        /**
         * Populate the {@link View}s in this {@link RecyclerView.ViewHolder}.
         *
         * @param service The {@link Service} to populate in this {@link RecyclerView.ViewHolder}.
         */
        private void populate(@Nullable final Service service) {
            if (service != null) {
                txtServiceName.setSupportBackgroundTintList(
                        ColorStateList.valueOf(service.getColour()));
                txtServiceName.setText(service.getServiceName());
                txtDescription.setText(service.getDescription());
            } else {
                txtServiceName.setText(null);
                txtDescription.setText(null);
            }
        }
    }

    /**
     * Implement this interface to receive callbacks when items inside this
     * {@link RecyclerView.ViewHolder} have been clicked.
     */
    interface OnItemClickListener {

        /**
         * This is called when the map has been clicked.
         */
        void onMapClicked();

        /**
         * This is called when a {@link Service} has been clicked.
         *
         * @param serviceName The name of the {@link Service} that has been clicked.
         */
        void onServiceClicked(@NonNull String serviceName);
    }
}
