/*
 * Copyright (C) 2014 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBus;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusService;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusStop;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This Adapter is for use in an {@link android.widget.ExpandableListView} to
 * display bus times in an expandable/collapsible fashion.
 * 
 * @author Niall Scott
 */
public class BusTimesExpandableListAdapter extends BaseExpandableListAdapter {
    
    /**
     * This enum describes the ways in which the user may order the bus
     * services.
     */
    public static enum Order {
        /** Order services by name. This is the default ordering. */
        SERVICE_NAME,
        /** Order services by arrival time. */
        ARRIVAL_TIME
    }
    
    private final Context context;
    private final LayoutInflater inflater;
    private final BusStopDatabase bsd;
    private final int defaultColour;
    
    private DateFormat busTimeFormatter;
    private LiveBusStop busStop;
    private List<LiveBusService> busServices;
    private HashMap<String, String> colours;
    private boolean showNightServices = true;
    private Order order = Order.SERVICE_NAME;
    
    /**
     * Create a new BusTimesExpandableListAdapter.
     * 
     * @param context A Context instance.
     */
    public BusTimesExpandableListAdapter(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("The context must not be null.");
        }
        
        this.context = context;
        inflater = LayoutInflater.from(context);
        defaultColour = context.getResources().getColor(R.color
                    .defaultBusColour);
        
        final BusApplication app = (BusApplication) context
                .getApplicationContext();
        bsd = app.getBusStopDatabase();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getGroupCount() {
        return busServices != null ? busServices.size() : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getChildrenCount(final int groupPosition) {
        final LiveBusService busService = getGroup(groupPosition);
        
        // The size is minus 1 because the first bus is shown in the group view.
        final int count = busService != null ?
                (busService.getLiveBuses().size() - 1) : 0;
        
        return count > 0 ? count : 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LiveBusService getGroup(final int groupPosition) {
        return busServices != null && groupPosition >= 0 &&
                groupPosition < busServices.size() ?
                busServices.get(groupPosition) : null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public LiveBus getChild(final int groupPosition, int childPosition) {
        // childPosition is incremented as the first item is displayed in the
        // group view.
        childPosition++;
        final LiveBusService busService = getGroup(groupPosition);
        
        if (busService != null) {
            final List<LiveBus> buses = busService.getLiveBuses();
            return childPosition > 0 && childPosition < buses.size() ?
                    buses.get(childPosition) : null;
        } else {
            return null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getGroupId(final int groupPosition) {
        final LiveBusService busService = getGroup(groupPosition);
        return busService != null ? busService.getServiceName().hashCode() : 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getChildId(final int groupPosition, final int childPosition) {
        final LiveBus bus = getChild(groupPosition, childPosition);
        final String journeyId = bus != null ? bus.getJourneyId() : null;
        
        return journeyId != null ? journeyId.hashCode() : 0L;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean hasStableIds() {
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded,
            final View convertView, final ViewGroup parent) {
        final LiveBusService busService = getGroup(groupPosition);
        if (busService == null) {
            return null;
        }
        
        final List<LiveBus> buses = busService.getLiveBuses();
        if (buses.isEmpty()) {
            return null;
        }
        
        final LiveBus bus = buses.get(0);
        final View v;
        final GroupViewHolder holder;
        
        if (convertView == null) {
            v = inflater.inflate(R.layout.bustimes_list_group, parent, false);
            
            holder = new GroupViewHolder();
            holder.txtBusService = (TextView) v
                    .findViewById(R.id.txtBusService);
            holder.txtBusDestination = (TextView) v
                    .findViewById(R.id.txtBusDestination);
            holder.txtBusTime = (TextView) v.findViewById(R.id.txtBusTime);
            
            v.setTag(holder);
        } else {
            v = convertView;
            holder = (GroupViewHolder) v.getTag();
        }
        
        populateServiceName(holder.txtBusService, busService);
        populateBusRow(v, bus);
        
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View getChildView(final int groupPosition, final int childPosition,
            final boolean isLastChild, final View convertView,
            final ViewGroup parent) {
        final LiveBus bus = getChild(groupPosition, childPosition);
        if (bus == null) {
            return null;
        }
        
        final View v;
        
        if (convertView == null) {
            v = inflater.inflate(R.layout.bustimes_list_child, parent, false);
            
            final ChildViewHolder holder = new ChildViewHolder();
            holder.txtBusDestination = (TextView) v
                    .findViewById(R.id.txtBusDestination);
            holder.txtBusTime = (TextView) v.findViewById(R.id.txtBusTime);
            
            v.setTag(holder);
        } else {
            v = convertView;
        }
        
        populateBusRow(v, bus);
        
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isChildSelectable(final int groupPosition,
            final int childPosition) {
        return false;
    }
    
    /**
     * Get the Context used by this ExpandableListAdapter.
     * 
     * @return The Context used by this ExpandableListAdapter.
     */
    public final Context getContext() {
        return context;
    }
    
    /**
     * Get whether night services are shown or not.
     * 
     * @return true if night services are shown, false if not.
     * @see #setShowNightServices(boolean)
     */
    public boolean getShowNightServices() {
        return showNightServices;
    }
    
    /**
     * Get the default colour to use for the service {@link TextView}
     * background.
     * 
     * @return The default colour to use for the service {@link TextView}
     * background.
     */
    public int getDefaultServiceBackgroundColour() {
        return defaultColour;
    }
    
    /**
     * Get the desired ordering of bus services.
     * 
     * @return The ordering of the bus services.
     * @see #setOrder(uk.org.rivernile.android.bustracker.ui.bustimes.BusTimesExpandableListAdapter.Order)}
     */
    public Order getOrdering() {
        return order;
    }
    
    /**
     * Set the bus stop that holds the {@link List} of {@link LiveBusService}s
     * to use in this Adapter.
     * 
     * @param busStop The {@link LiveBusStop} to use in this Adapter.
     */
    public void setBusStop(final LiveBusStop busStop) {
        if (this.busStop != busStop) {
            this.busStop = busStop;
            populateBusServices();
        }
    }
    
    /**
     * Set whether night services are shown or not.
     * 
     * @param showNightServices true if night services are to be shown, false if
     * not.
     */
    public void setShowNightServices(final boolean showNightServices) {
        if (this.showNightServices != showNightServices) {
            this.showNightServices = showNightServices;
            populateBusServices();
        }
    }
    
    /**
     * Set the order that the bus services should be displayed in.
     * 
     * @param order The order of the bus services.
     */
    public void setOrder(final Order order) {
        if (order != null && this.order != order) {
            this.order = order;
            populateBusServices();
        }
    }
    
    /**
     * Populate this Adapter's internal list of bus services. How this list is
     * populated depends if showing night services is enabled, and the preferred
     * ordering of services.
     */
    private void populateBusServices() {
        if (busStop == null) {
            busServices = null;
        } else {
            final List<LiveBusService> services = busStop.getServices();
            final ArrayList<LiveBusService> tempServices =
                    new ArrayList<LiveBusService>();
            final int len = services.size();
            LiveBusService service;
            
            for (int i = 0; i < len; i++) {
                service = services.get(i);
                
                if (!showNightServices &&
                        isNightService(service.getServiceName())) {
                    continue;
                }
                
                tempServices.add(service);
            }
            
            if (order == Order.ARRIVAL_TIME) {
                Collections.sort(tempServices, TIME_COMPARATOR);
            }
            
            busServices = tempServices;
        }
        
        populateServiceColours();
        notifyDataSetChanged();
    }
    
    /**
     * Populate the cache of bus service colours by querying the bus stop
     * database for the colours of all services we have.
     */
    private void populateServiceColours() {
        if (busServices == null || busServices.isEmpty()) {
            colours = null;
            return;
        }
        
        final int count = busServices.size();
        final String[] services = new String[count];
        
        for (int i = 0; i < count; i++) {
            services[i] = busServices.get(i).getServiceName();
        }
        
        colours = bsd.getServiceColours(services);
    }
    
    /**
     * Populate the service name {@link TextView} with the service name and
     * apply any required styling.
     * 
     * @param txtBusService The TextView that is populated with the service
     * name.
     * @param busService The bus service to use.
     */
    @SuppressLint({"NewAPI"})
    protected void populateServiceName(final TextView txtBusService,
            final LiveBusService busService) {
        final String serviceName = busService.getServiceName();
        txtBusService.setText(serviceName);
        
        // Get the Drawable which makes up the retangle in the background
        // with the rounded corners. Make it mutable so it doesn't affect
        // other instances of the same Drawable.
        final GradientDrawable background;
        try {
            background = (GradientDrawable) context.getResources()
                    .getDrawable(R.drawable.bus_service_rounded_background)
                    .mutate();
        } catch(ClassCastException e) {
            txtBusService.setTextColor(Color.BLACK);
            
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                txtBusService.setBackground(null);
            } else {
                txtBusService.setBackgroundDrawable(null);
            }
            
            return;
        }
        
        txtBusService.setTextColor(Color.WHITE);
        
        if (colours != null && colours.containsKey(serviceName)) {
            try {
                background.setColor(Color.parseColor(colours.get(serviceName)));
            } catch (IllegalArgumentException e) {
                background.setColor(getDefaultServiceBackgroundColour());
            }
        } else {
            background.setColor(getDefaultServiceBackgroundColour());
        }
        
        // Set the background and return the View for the group.
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            txtBusService.setBackground(background);
        } else {
            txtBusService.setBackgroundDrawable(background);
        }
    }
    
    /**
     * Populate the destination and time {@link TextView}s with their required
     * information.
     * 
     * @param row The row container {@link View}.
     * @param bus The bus which contains the data to populate this {@link View}
     * with.
     */
    private void populateBusRow(final View row, final LiveBus bus) {
        final ChildViewHolder holder = (ChildViewHolder) row.getTag();
        
        if (holder.txtBusDestination != null) {
            final String destination = bus.getDestination();
            
            if (bus.isDiverted()) {
                if (TextUtils.isEmpty(destination)) {
                    holder.txtBusDestination.setText(R.string
                            .displaystopdata_diverted);
                } else {
                    holder.txtBusDestination.setText(context.getString(R.string
                            .displaystopdata_diverted_with_destination,
                                    destination));
                }
                
                if (holder.txtBusTime != null) {
                    holder.txtBusTime.setVisibility(View.GONE);
                }
                
                return;
            } else {
                holder.txtBusDestination.setText(destination);
            }
        }
        
        if (holder.txtBusTime != null) {
            String timeToDisplay;
            final int minutes = bus.getDepartureMinutes();
            
            if (minutes > 59) {
                timeToDisplay = getBusTimeFormatter()
                        .format(bus.getDepartureTime());
            } else if (minutes < 2) {
                timeToDisplay = context.getString(R.string.displaystopdata_due);
            } else {
                timeToDisplay = String.valueOf(minutes);
            }
            
            if (bus.isEstimatedTime()) {
                timeToDisplay = context.getString(
                        R.string.displaystopdata_estimated_time, timeToDisplay);
            }
            
            holder.txtBusTime.setText(timeToDisplay);
            holder.txtBusTime.setVisibility(View.VISIBLE);
        }
    }
    
    /**
     * Get a {@link DateFormat} that will format bus times correctly. This
     * method may be overridden to provide a different time formatter.
     * 
     * @return The formatter to use for bus times.
     */
    protected DateFormat getBusTimeFormatter() {
        if (busTimeFormatter == null) {
            busTimeFormatter = new SimpleDateFormat("HH:mm");
        }
        
        return busTimeFormatter;
    }
    
    /**
     * Is the given service a night service?
     * 
     * @param serviceName The name of the service to test if it's a night
     * service.
     * @return true if the service is a night service, false if not.
     */
    protected boolean isNightService(final String serviceName) {
        return false;
    }
    
    /**
     * The {@link Comparator} for sorting bus services by time.
     */
    static final Comparator<LiveBusService> TIME_COMPARATOR =
            new Comparator<LiveBusService>() {
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
    
    /**
     * A ChildViewHolder is used to hold references to the Views contained
     * within the child elements as {@link View#findViewById(int)} is
     * inefficient when it may be constantly called as a list is scrolled.
     */
    protected static class ChildViewHolder {
        TextView txtBusDestination;
        TextView txtBusTime;
    }
    
    /**
     * A GroupViewHolder is used to hold references to the Views contained
     * within the group elements as {@link View#findViewById(int)} is
     * inefficient when it may be constantly called as a list is scrolled.
     */
    protected static class GroupViewHolder extends ChildViewHolder {
        TextView txtBusService;
        
        /**
         * Get the {@link TextView} that displays the service name. This method
         * exists so that subclasses have read-only access to the txtBusService
         * field held in this class.
         * 
         * @return The {@link TextView} that shows the service name.
         */
        public TextView getBusServiceTextView() {
            return txtBusService;
        }
    }
}