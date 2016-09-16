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

package uk.org.rivernile.android.bustracker.ui.alerts;

import android.content.Context;
import android.database.Cursor;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.lang.ref.WeakReference;
import java.util.Map;

import uk.org.rivernile.android.bustracker.database.settings.SettingsContract;
import uk.org.rivernile.android.bustracker.ui.RecyclerCursorAdapter;
import uk.org.rivernile.android.utils.MapsUtils;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This adapter displays the user's active alerts, allowing them to view details on the alert or to
 * remove it.
 *
 * @author Niall Scott
 */
class AlertsAdapter extends RecyclerCursorAdapter<AlertsAdapter.BaseViewHolder> {

    private static final int VIEW_TYPE_PROXIMITY = 1;
    private static final int VIEW_TYPE_TIME = 2;

    private WeakReference<OnItemClickListener> clickListenerRef;
    private Map<String, BusStop> busStops;

    private int typeColumnIndex;
    private int busStopCodeIndex;
    private int distanceFromColumn;
    private int servicesColumn;
    private int timeTriggerColumn;

    /**
     * Create a new {@code AlertsAdapter}.
     *
     * @param context A {@link Context} instance.
     */
    AlertsAdapter(@NonNull final Context context) {
        super(context);
    }

    @Override
    public BaseViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case VIEW_TYPE_PROXIMITY:
                return new ProximityViewHolder(getLayoutInflater()
                        .inflate(R.layout.alertmanager_proximity_item, parent, false));
            case VIEW_TYPE_TIME:
                return new TimeViewHolder(getLayoutInflater()
                        .inflate(R.layout.alertmanager_time_item, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final BaseViewHolder holder, final int position) {
        holder.populate(getItem(position));
    }

    @Override
    public int getItemViewType(final int position) {
        final Cursor cursor = getItem(position);

        if (cursor != null) {
            switch (cursor.getInt(typeColumnIndex)) {
                case SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY:
                    return VIEW_TYPE_PROXIMITY;
                case SettingsContract.Alerts.ALERTS_TYPE_TIME:
                    return VIEW_TYPE_TIME;
                default:
                    return 0;
            }
        } else {
            return 0;
        }
    }

    @Nullable
    @Override
    public Cursor swapCursor(@Nullable final Cursor cursor) {
        if (cursor != null) {
            typeColumnIndex = cursor.getColumnIndex(SettingsContract.Alerts.TYPE);
            busStopCodeIndex = cursor.getColumnIndex(SettingsContract.Alerts.STOP_CODE);
            distanceFromColumn = cursor.getColumnIndex(SettingsContract.Alerts.DISTANCE_FROM);
            servicesColumn = cursor.getColumnIndex(SettingsContract.Alerts.SERVICE_NAMES);
            timeTriggerColumn = cursor.getColumnIndex(SettingsContract.Alerts.TIME_TRIGGER);
        }

        return super.swapCursor(cursor);
    }

    /**
     * Set the listener to be invoked when click events happen on the items.
     *
     * @param listener The {@link OnItemClickListener}.
     */
    void setOnItemClickListener(@Nullable final OnItemClickListener listener) {
        clickListenerRef = listener != null ? new WeakReference<>(listener) : null;
    }

    /**
     * Set the {@link Map} of bus stops that the alerts reference, so that bus stop data is
     * populated.
     *
     * @param busStops The {@link Map} of bus stop data.
     */
    void setBusStops(@Nullable final Map<String, BusStop> busStops) {
        if (this.busStops != busStops) {
            this.busStops = busStops;
            notifyDataSetChanged();
        }
    }

    /**
     * This {@link RecyclerView.ViewHolder} provides a base implementation of the items.
     */
    abstract class BaseViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener,
            OnMapReadyCallback {

        MapView mapView;
        TextView txtDescription;
        Button btnRemove;
        private GoogleMap map;
        protected String stopName;
        private LatLng latLon;
        private int stopOrientation = -1;

        /**
         * Create a new {@code BaseViewHolder}.
         *
         * @param itemView The root {@link View} of the item.
         */
        public BaseViewHolder(@NonNull final View itemView) {
            super(itemView);

            mapView = (MapView) itemView.findViewById(R.id.mapView);
            txtDescription = (TextView) itemView.findViewById(R.id.txtDescription);
            btnRemove = (Button) itemView.findViewById(R.id.btnRemove);

            btnRemove.setOnClickListener(this);
            mapView.setClickable(false);

            if (MapsUtils.isGoogleMapsAvailable(getContext())) {
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

            if (latLon != null) {
                populateMap(googleMap, latLon, stopOrientation);
                mapView.setVisibility(View.VISIBLE);
            }
        }

        /**
         * This is called when it's time for the item to refresh its data from the {@link Cursor}.
         * Child implementations must call through to this {@code super} implementation.
         *
         * @param cursor The {@link Cursor} to take the data from, already set to the correct row.
         */
        @CallSuper
        void populate(@Nullable final Cursor cursor) {
            if (cursor != null) {
                final String stopCode = cursor.getString(busStopCodeIndex);
                final BusStop busStop = busStops != null ? busStops.get(stopCode) : null;

                if (busStop != null) {
                    final String stopName = busStop.getStopName();
                    final String locality = busStop.getLocality();

                    if (TextUtils.isEmpty(locality)) {
                        this.stopName = getContext().getString(R.string.busstop, stopName,
                                stopCode);
                    } else {
                        this.stopName = getContext().getString(R.string.busstop_locality,
                                stopName, locality, stopCode);
                    }

                    latLon = new LatLng(busStop.getLatitude(), busStop.getLongitude());
                    stopOrientation = busStop.getOrientation();
                } else {
                    stopName = null;
                    latLon = null;
                    stopOrientation = -1;
                }
            } else {
                stopName = null;
                latLon = null;
                stopOrientation = -1;
            }

            if (map != null && latLon != null) {
                populateMap(map, latLon, stopOrientation);
                mapView.setVisibility(View.VISIBLE);
            } else {
                mapView.setVisibility(View.GONE);
            }
        }

        /**
         * Populate the map with the given point.
         *
         * @param map The {@link GoogleMap} to populate.
         * @param latLon The point of interest.
         * @param stopOrientation The bus stop orientation. See
         * {@link MapsUtils#applyStopDirectionToMarker(MarkerOptions, int)} for details.
         */
        abstract void populateMap(@NonNull GoogleMap map, @NonNull LatLng latLon,
                int stopOrientation);
    }

    /**
     * This {@link RecyclerView.ViewHolder} is used to display proximity alert items.
     */
    class ProximityViewHolder extends BaseViewHolder {

        Button btnLocationSettings;
        private final int rangeRingStrokeColour;
        private final int rangeRingFillColour;
        private final float rangeRingStrokeWidth;
        private int distance;
        private Circle circle;
        private Marker marker;

        /**
         * Create a new {@code ProximityViewHolder}.
         *
         * @param itemView The root {@link View} of the item.
         */
        public ProximityViewHolder(@NonNull final View itemView) {
            super(itemView);

            rangeRingStrokeColour = ContextCompat.getColor(getContext(),
                    R.color.map_range_ring_stroke);
            rangeRingFillColour = ContextCompat.getColor(getContext(),
                    R.color.map_range_ring_fill);
            rangeRingStrokeWidth = getContext().getResources()
                    .getDimension(R.dimen.map_range_ring_stroke);
            btnLocationSettings = (Button) itemView.findViewById(R.id.btnLocationSettings);
            btnLocationSettings.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            final OnItemClickListener listener =
                    clickListenerRef != null ? clickListenerRef.get() : null;

            if (listener != null) {
                if (v == btnRemove) {
                    final Cursor cursor = getItem(getAdapterPosition());

                    if (cursor != null) {
                        listener.onRemoveProximityAlertClicked(cursor);
                    }
                } else if (v == btnLocationSettings) {
                    listener.onLocationSettingsClicked();
                }
            }
        }

        @Override
        void populate(@Nullable final Cursor cursor) {
            if (circle != null) {
                circle.remove();
                circle = null;
            }

            if (marker != null) {
                marker.remove();
                marker = null;
            }

            super.populate(cursor);

            if (cursor != null) {
                distance = cursor.getInt(distanceFromColumn);
                txtDescription.setText(getContext().getString(R.string.alertmanager_prox_subtitle,
                        distance, stopName));
            } else {
                distance = 0;
                txtDescription.setText(null);
            }
        }

        @Override
        void populateMap(@NonNull final GoogleMap map, @NonNull final LatLng latLon,
                final int stopOrientation) {
            map.moveCamera(CameraUpdateFactory.newLatLng(latLon));
            circle = map.addCircle(new CircleOptions()
                .center(latLon)
                .radius(distance)
                .strokeColor(rangeRingStrokeColour)
                .strokeWidth(rangeRingStrokeWidth)
                .fillColor(rangeRingFillColour));
            final MarkerOptions mo = new MarkerOptions().position(latLon);
            MapsUtils.applyStopDirectionToMarker(mo, stopOrientation);
            marker = map.addMarker(mo);
        }
    }

    /**
     * This {@link RecyclerView.ViewHolder} is used to display time alert items.
     */
    class TimeViewHolder extends BaseViewHolder {

        private Marker marker;

        /**
         * Create a new {@code TimeViewHolder}.
         *
         * @param itemView The root {@link View} of the item.
         */
        public TimeViewHolder(@NonNull final View itemView) {
            super(itemView);
        }

        @Override
        public void onClick(final View v) {
            final OnItemClickListener listener =
                    clickListenerRef != null ? clickListenerRef.get() : null;

            if (listener != null) {
                if (v == btnRemove) {
                    final Cursor cursor = getItem(getAdapterPosition());

                    if (cursor != null) {
                        listener.onRemoveTimeAlertClicked(cursor);
                    }
                }
            }
        }

        @Override
        void populate(@Nullable final Cursor cursor) {
            if (marker != null) {
                marker.remove();
                marker = null;
            }

            super.populate(cursor);

            if (cursor != null) {
                final String packedServices = cursor.getString(servicesColumn);
                final String[] services = packedServices != null ?
                        packedServices.split(",") : null;
                final String unpackedServices;
                final int servicesLen;

                if (services != null) {
                    final StringBuilder sb = new StringBuilder();
                    servicesLen = services.length;

                    for (String service : services) {
                        if (sb.length() > 0) {
                            sb.append(", ");
                        }

                        sb.append(service);
                    }

                    unpackedServices = sb.toString();
                } else {
                    unpackedServices = "";
                    servicesLen = 0;
                }

                final int minutes = cursor.getInt(timeTriggerColumn);
                final int pluralsRes = servicesLen > 1
                        ? R.plurals.alertmanager_time_subtitle_multiple_services
                        : R.plurals.alertmanager_time_subtitle_single_service;
                txtDescription.setText(getContext().getResources().getQuantityString(pluralsRes,
                        minutes, unpackedServices, stopName, minutes));
            } else {
                txtDescription.setText(null);
            }
        }

        @Override
        void populateMap(@NonNull final GoogleMap map, @NonNull final LatLng latLon,
                final int stopOrientation) {
            map.moveCamera(CameraUpdateFactory.newLatLng(latLon));
            final MarkerOptions mo = new MarkerOptions().position(latLon);
            MapsUtils.applyStopDirectionToMarker(mo, stopOrientation);
            marker = map.addMarker(mo);
        }
    }

    /**
     * Classes which wish to be informed when click events happen should implement this interface.
     */
    interface OnItemClickListener {

        /**
         * This is called when the location settings button has been clicked.
         */
        void onLocationSettingsClicked();

        /**
         * This is called when the remove proximity alert button has been clicked.
         *
         * @param cursor The {@link Cursor} backing this adapter, already pointing at the correct
         * row for the clicked item.
         */
        void onRemoveProximityAlertClicked(@NonNull Cursor cursor);

        /**
         * This is called when the remove time alert button has been clicked.
         *
         * @param cursor The {@link Cursor} backing this adapter, already pointing at the correct
         * row for the clicked item.
         */
        void onRemoveTimeAlertClicked(@NonNull Cursor cursor);
    }
}
