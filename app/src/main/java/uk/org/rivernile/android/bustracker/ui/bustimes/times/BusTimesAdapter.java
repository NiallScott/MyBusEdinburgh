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

package uk.org.rivernile.android.bustracker.ui.bustimes.times;

import android.content.Context;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBus;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusService;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link RecyclerView.Adapter} shows a listing of times for the stop.
 *
 * @author Niall Scott
 */
class BusTimesAdapter extends RecyclerView.Adapter<BusTimesAdapter.ViewHolder> {

    /**
     * Mark an {@code int} field as having to have the value of {@link #SORT_SERVICE_NAME} or
     * {@link #SORT_ARRIVAL_TIME}.
     */
    @Retention(RetentionPolicy.SOURCE)
    @IntDef({ SORT_SERVICE_NAME, SORT_ARRIVAL_TIME })
    @interface SortMode { }

    /**
     * Sort the bus services by service name.
     */
    static final int SORT_SERVICE_NAME = 1;
    /**
     * Sort the bus services by arrival time.
     */
    static final int SORT_ARRIVAL_TIME = 2;

    private final Context context;
    private final LayoutInflater inflater;
    private List<LiveBusService> services;
    private Map<String, String> serviceColours;
    @SortMode
    private int sortMode = SORT_SERVICE_NAME;

    /**
     * Create a new {@code BusTimesAdapter}.
     *
     * @param context The {@link android.app.Activity} {@link Context}.
     */
    BusTimesAdapter(@NonNull final Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.bustimes_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.populate(getService(position));
    }

    @Override
    public int getItemCount() {
        return services != null ? services.size() : 0;
    }

    /**
     * Set the {@link List} of {@link LiveBusService}s for this adapter.
     *
     * @param services The {@link List} of {@link LiveBusService}s for this adapter.
     */
    void setServices(@Nullable final List<LiveBusService> services) {
        if (this.services != services) {
            this.services = services;
            notifyDataSetChanged();
        }
    }

    /**
     * Set the mapping of service name -> hex colour.
     *
     * @param serviceColours The mapping of service name -> hex colour.
     */
    void setServiceColours(@Nullable final Map<String, String> serviceColours) {
        if (this.serviceColours != serviceColours) {
            this.serviceColours = serviceColours;
            notifyDataSetChanged();
        }
    }

    /**
     * Set the sort mode of this adapter.
     *
     * @param sortMode The sort mode of this adapter.
     */
    void setSortMode(@SortMode final int sortMode) {
        if (this.sortMode != sortMode) {
            this.sortMode = sortMode;
            populateBusServices();
        }
    }

    /**
     * Get the {@link LiveBusService} at the given position.
     *
     * @param position The position to get the service for.
     * @return The {@link LiveBusService} at the given position or {@code null} if that position
     * cannot be reached.
     */
    @Nullable
    private LiveBusService getService(final int position) {
        return services != null && position >= 0 && position < services.size()
                ? services.get(position) : null;
    }

    private void populateBusServices() {

    }

    /**
     * The {@link RecyclerView.ViewHolder} for showing stop times.
     */
    class ViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtServiceName;
        private final TextView txtDestination;
        private final TextView txtTime;

        /**
         * Create a new {@code ViewHolder}.
         *
         * @param itemView The inflated {@link View} for this {@code ViewHolder}.
         */
        public ViewHolder(@NonNull final View itemView) {
            super(itemView);

            txtServiceName = (TextView) itemView.findViewById(R.id.txtServiceName);
            txtDestination = (TextView) itemView.findViewById(R.id.txtDestination);
            txtTime = (TextView) itemView.findViewById(R.id.txtTime);
        }

        /**
         * Populate the contents of this {@code ViewHolder}.
         *
         * @param service The {@link LiveBusService} to populate this {@code ViewHolder} with.
         */
        private void populate(@Nullable final LiveBusService service) {
            if (service != null) {
                final LiveBus bus = service.getLiveBuses().get(0);
                txtServiceName.setText(service.getServiceName());
                txtDestination.setText(bus.getDestination());
                txtTime.setText(String.valueOf(bus.getDepartureMinutes()));
            } else {
                txtServiceName.setText(null);
                txtDestination.setText(null);
                txtTime.setText(null);
            }
        }
    }

    private final Comparator<LiveBusService> timeComparator = new Comparator<LiveBusService>() {
            @Override
            public int compare(final LiveBusService lhs, final LiveBusService rhs) {
                if (lhs == rhs) {
                    return 0;
                } else if (lhs == null) {
                    return 1;
                } else if (rhs == null) {
                    return -1;
                }

                final List<LiveBus> lhsBuses = lhs.getLiveBuses();
                final List<LiveBus> rhsBuses = rhs.getLiveBuses();

                if (lhsBuses.isEmpty() && rhsBuses.isEmpty()) {
                    return 0;
                } else if (lhsBuses.isEmpty()) {
                    return 1;
                } else if (rhsBuses.isEmpty()) {
                    return -1;
                }

                final LiveBus lhsBus = lhsBuses.get(0);
                final LiveBus rhsBus = rhsBuses.get(0);

                return lhsBus.getDepartureMinutes() - rhsBus.getDepartureMinutes();
            }
        };
}
