/*
 * Copyright (C) 2016 - 2017 Niall 'Rivernile' Scott
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

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPropertyAnimatorCompat;
import android.support.v7.util.DiffUtil;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBus;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusService;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This adapter populates bus times in an expandable fashion.
 *
 * @author Niall Scott
 */
class BusTimesAdapter extends RecyclerView.Adapter {

    @SuppressLint("SimpleDateFormat")
    private static final SimpleDateFormat BUS_TIME_FORMAT = new SimpleDateFormat("HH:mm");

    private static final String STATE_KEY_EXPANDED_ITEMS = "expandedItems";

    private static final int TYPE_PARENT = 1;
    private static final int TYPE_CHILD = 2;

    private final Context context;
    private final LayoutInflater inflater;
    private final HashSet<String> expandedItems = new HashSet<>();
    private List<LiveBusService> services;
    private List<BusTimesItem> items;
    private Map<String, Integer> serviceColours;
    private boolean sortByTime;

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
    public RecyclerView.ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        switch (viewType) {
            case TYPE_PARENT:
                return new ParentViewHolder(
                        inflater.inflate(R.layout.bustimes_parent_item, parent, false));
            case TYPE_CHILD:
                return new ChildViewHolder(
                        inflater.inflate(R.layout.bustimes_child_item, parent, false));
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(final RecyclerView.ViewHolder holder, final int position) {
        final BusTimesItem item = getItem(position);

        if (holder instanceof ParentViewHolder) {
            ((ParentViewHolder) holder).populate(item);
        } else if (holder instanceof ChildViewHolder) {
            ((ChildViewHolder) holder).populate(item);
        }
    }

    @Override
    public int getItemCount() {
        return items != null ? items.size() : 0;
    }

    @Override
    public int getItemViewType(final int position) {
        final BusTimesItem item = getItem(position);

        if (item != null) {
            return item.isParent() ? TYPE_PARENT : TYPE_CHILD;
        } else {
            return 0;
        }
    }

    @Override
    public long getItemId(final int position) {
        final BusTimesItem item = getItem(position);
        return item != null ? item.hashCode() : 0;
    }

    /**
     * Restore any previously saved state back in to this adapter.
     *
     * @param savedInstanceState The source of the state.
     */
    void onRestoreInstanceState(@NonNull final Bundle savedInstanceState) {
        expandedItems.clear();
        final ArrayList<String> expandedAsList =
                savedInstanceState.getStringArrayList(STATE_KEY_EXPANDED_ITEMS);

        if (expandedAsList != null) {
            expandedItems.addAll(expandedAsList);
        }

        repopulateServices();
    }

    /**
     * Give this adapter an opportunity to save state related to it for a future instance.
     *
     * @param outState Where to save any state.
     */
    void onSaveInstanceState(@NonNull final Bundle outState) {
        outState.putStringArrayList(STATE_KEY_EXPANDED_ITEMS, new ArrayList<>(expandedItems));
    }

    /**
     * Set the {@link List} of {@link LiveBusService}s to be displayed by this adapter.
     *
     * @param services The {@link List} of {@link LiveBusService}s to be displayed by this adapter.
     */
    void setServices(@Nullable final List<LiveBusService> services) {
        this.services = services;
        resortItems();
        repopulateServices();
    }

    /**
     * Set the mapping of service name -> colour integer.
     *
     * @param serviceColours The mapping of service name -> colour integer.
     */
    void setServiceColours(@Nullable final Map<String, Integer> serviceColours) {
        if (this.serviceColours != serviceColours) {
            this.serviceColours = serviceColours;

            if (items != null) {
                final int itemsSize = items.size();

                for (int i = 0; i < itemsSize; i++) {
                    if (items.get(i).isParent()) {
                        notifyItemChanged(i);
                    }
                }
            }
        }
    }

    /**
     * Should the services be sorted by time?
     *
     * @param sortByTime {@code true} if the services should be sorted by time, {@code false} if
     * they should be sorted by service name in alphanumeric order.
     */
    void setSortByTime(final boolean sortByTime) {
        this.sortByTime = sortByTime;
        resortItems();
        repopulateServices();
    }

    /**
     * Get the {@link BusTimesItem} at the given position.
     *
     * @param position The position of the item to get.
     * @return The {@link BusTimesItem} at the given position, or {@code null} if the position is
     * {@code < 0} or {@code >= items.size()}.
     */
    @Nullable
    private BusTimesItem getItem(final int position) {
        return position >= 0 && position < items.size() ? items.get(position) : null;
    }

    /**
     * Sort the original {@link List} of {@link LiveBusService}s depending on the sort mode.
     */
    private void resortItems() {
        if (services != null) {
            if (sortByTime) {
                Collections.sort(services, timeComparator);
            } else {
                Collections.sort(services);
            }
        }
    }

    /**
     * Re-populate the services in to the adapter.
     */
    private void repopulateServices() {
        final List<BusTimesItem> oldItems = items;
        items = flattenServicesToItems(services);

        DiffUtil.calculateDiff(new DiffUtil.Callback() {
            @Override
            public int getOldListSize() {
                return oldItems != null ? oldItems.size() : 0;
            }

            @Override
            public int getNewListSize() {
                return items != null ? items.size() : 0;
            }

            @Override
            public boolean areItemsTheSame(final int oldItemPosition, final int newItemPosition) {
                return oldItems.get(oldItemPosition).equals(items.get(newItemPosition));
            }

            @Override
            public boolean areContentsTheSame(final int oldItemPosition,
                    final int newItemPosition) {
                return oldItems.get(oldItemPosition).getLiveBus().equals(
                        items.get(newItemPosition).getLiveBus());
            }
        }).dispatchUpdatesTo(this);
    }

    /**
     * The {@link List} of {@link LiveBusService}s is an object graph. Flatten this in to something
     * that's more consumable by this adapter to populate a {@link List} of items rather than
     * dealing with nested {@link List}s.
     *
     * @param services The provided {@link List} of {@link LiveBusService}s.
     * @return A {@link List} of {@link BusTimesItem}s which represents a flattened version of the
     * input. If the input is {@code null}, then return {@code null}.
     */
    @Nullable
    private List<BusTimesItem> flattenServicesToItems(
            @Nullable final List<LiveBusService> services) {
        if (services == null) {
            return null;
        }

        final int servicesSize = services.size();
        final ArrayList<BusTimesItem> result = new ArrayList<>();

        for (int i = 0; i < servicesSize; i++) {
            final LiveBusService liveBusService = services.get(i);
            final List<LiveBus> liveBuses = liveBusService.getLiveBuses();
            final int busesSize = expandedItems.contains(liveBusService.getServiceName()) ?
                    liveBuses.size() : 1;

            for (int j = 0; j < busesSize; j++) {
                result.add(new BusTimesItem(liveBusService, liveBuses.get(j), j));
            }
        }

        return result;
    }

    /**
     * This {@link RecyclerView.ViewHolder} populates the view for a child item. Parent items also
     * contain the same view as child items, so this is re-used for parent items.
     */
    private class ChildViewHolder extends RecyclerView.ViewHolder {

        private final TextView txtDestination;
        private final TextView txtTime;

        /**
         * Create a new {@code ChildViewHolder}.
         *
         * @param itemView The root {@link View} of this {@link RecyclerView.ViewHolder}.
         */
        ChildViewHolder(final View itemView) {
            super(itemView);

            txtDestination = (TextView) itemView.findViewById(R.id.txtDestination);
            txtTime = (TextView) itemView.findViewById(R.id.txtTime);
        }

        /**
         * Populate this {@link RecyclerView.ViewHolder} with the given {@link BusTimesItem}.
         *
         * @param item The item to populate.
         */
        void populate(@Nullable final BusTimesItem item) {
            if (populateDestination(item)) {
                populateTime(item);
            }
        }

        /**
         * Populate the destination text.
         *
         * @param item The item for this row.
         * @return {@code true} if the time should be populated too, {@code false} if not.
         */
        private boolean populateDestination(@Nullable final BusTimesItem item) {
            if (item != null) {
                final LiveBus bus = item.getLiveBus();
                final String destination = bus.getDestination();

                if (bus.isDiverted()) {
                    if (TextUtils.isEmpty(destination)) {
                        txtDestination.setText(R.string.bustimes_diverted);
                    } else {
                        txtDestination.setText(context.getString(
                                R.string.bustimes_diverted_with_destination, destination));
                    }

                    txtTime.setVisibility(View.GONE);

                    return false;
                } else {
                    txtDestination.setText(destination);
                }
            } else {
                txtDestination.setText(null);
            }

            txtTime.setVisibility(View.VISIBLE);

            return true;
        }

        /**
         * Populate the time text.
         *
         * @param item The item for this row.
         */
        private void populateTime(@Nullable final BusTimesItem item) {
            if (item != null) {
                final LiveBus bus = item.getLiveBus();
                String timeToDisplay;
                final int minutes = bus.getDepartureMinutes();

                if (minutes > 59) {
                    timeToDisplay = BUS_TIME_FORMAT.format(bus.getDepartureTime());
                } else if (minutes < 2) {
                    timeToDisplay = context.getString(R.string.bustimes_due);
                } else {
                    timeToDisplay = String.valueOf(minutes);
                }

                if (bus.isEstimatedTime()) {
                    timeToDisplay = context.getString(
                            R.string.bustimes_estimated_time, timeToDisplay);
                }

                txtTime.setText(timeToDisplay);
            } else {
                txtTime.setText(null);
            }
        }
    }

    /**
     * This {@link RecyclerView.ViewHolder} populates the view for a parent item.
     */
    private class ParentViewHolder extends ChildViewHolder implements View.OnClickListener {

        private static final float COLLAPSED_DEGREES = 0f;
        private static final float EXPANDED_DEGREES = 180f;

        private final AppCompatTextView txtServiceName;
        private final ImageView imgArrow;

        private ViewPropertyAnimatorCompat expandAnimation;
        private ViewPropertyAnimatorCompat collapseAnimation;

        /**
         * Create a new {@code ParentViewHolder}.
         *
         * @param itemView The root {@link View} of this {@link RecyclerView.ViewHolder}.
         */
        ParentViewHolder(@NonNull final View itemView) {
            super(itemView);

            txtServiceName = (AppCompatTextView) itemView.findViewById(R.id.txtServiceName);
            imgArrow = (ImageView) itemView.findViewById(R.id.imgArrow);

            itemView.setOnClickListener(this);
        }

        @Override
        void populate(@Nullable final BusTimesItem item) {
            if (item != null) {
                final String serviceName = item.getLiveBusService().getServiceName();
                final Integer boxedColour = serviceColours != null
                        ? serviceColours.get(serviceName) : null;
                @ColorInt final int colour = boxedColour != null
                        ? boxedColour : ContextCompat.getColor(context, R.color.colorAccent);
                txtServiceName.setSupportBackgroundTintList(ColorStateList.valueOf(colour));
                txtServiceName.setText(serviceName);

                cancelExpandAnimation();
                cancelCollapseAnimation();

                imgArrow.setRotation(expandedItems.contains(serviceName)
                        ? EXPANDED_DEGREES : COLLAPSED_DEGREES);

                super.populate(item);
            } else {
                txtServiceName.setText(null);
                super.populate(null);
            }
        }

        @Override
        public void onClick(final View v) {
            final BusTimesItem item = getItem(getAdapterPosition());

            if (item != null) {
                final String serviceName = item.getLiveBusService().getServiceName();

                if (!expandedItems.add(serviceName)) {
                    expandedItems.remove(serviceName);
                    performCollapseAnimation();
                } else {
                    performExpandAnimation();
                }

                repopulateServices();
            }
        }

        /**
         * Perform the expand animation.
         */
        private void performExpandAnimation() {
            expandAnimation = ViewCompat.animate(imgArrow)
                    .setInterpolator(new LinearInterpolator())
                    .setDuration(context.getResources().getInteger(
                            android.R.integer.config_shortAnimTime))
                    .rotation(EXPANDED_DEGREES)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            expandAnimation = null;
                        }
                    });
        }

        /**
         * Perform the collapse animation.
         */
        private void performCollapseAnimation() {
            collapseAnimation = ViewCompat.animate(imgArrow)
                    .setInterpolator(new LinearInterpolator())
                    .setDuration(context.getResources().getInteger(
                            android.R.integer.config_shortAnimTime))
                    .rotation(COLLAPSED_DEGREES)
                    .withEndAction(new Runnable() {
                        @Override
                        public void run() {
                            collapseAnimation = null;
                        }
                    });
        }

        /**
         * Cancel the expand animation.
         */
        private void cancelExpandAnimation() {
            if (expandAnimation != null) {
                expandAnimation.cancel();
                expandAnimation = null;
            }
        }

        /**
         * Cancel the collapse animation.
         */
        private void cancelCollapseAnimation() {
            if (collapseAnimation != null) {
                collapseAnimation.cancel();
                collapseAnimation = null;
            }
        }
    }

    /**
     * The {@link Comparator} for sorting bus services by time.
     */
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
