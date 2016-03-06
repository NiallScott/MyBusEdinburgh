/*
 * Copyright (C) 2011 - 2016 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.fragments.general;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v4.widget.CursorAdapter;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import uk.org.rivernile.android.bustracker.database.settings.SettingsContract;
import uk.org.rivernile.android.bustracker.ui.callbacks
        .OnShowConfirmDeleteProximityAlertListener;
import uk.org.rivernile.android.bustracker.ui.callbacks
        .OnShowConfirmDeleteTimeAlertListener;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This Fragment allows the users to view what proximity and time alerts they
 * have set and allow them to delete single alerts or all alerts.
 * 
 * Instances of this Fragment are retained between rotation changes.
 * 
 * @author Niall Scott
 */
public class AlertManagerFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor> {
    
    private Callbacks callbacks;
    private AlertCursorAdapter ad;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(final Activity activity) {
        super.onAttach(activity);
        
        try {
            callbacks = (Callbacks) activity;
        } catch (ClassCastException e) {
            throw new IllegalStateException(activity.getClass().getName() +
                    " does not implement " + Callbacks.class.getName());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Create the adapter.
        ad = new AlertCursorAdapter(getActivity(), null);
        setListAdapter(ad);
        
        // Tell the underlying Activity that this Fragment hosts an options
        // menu.
        setHasOptionsMenu(true);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        return inflater.inflate(R.layout.alertmanager, container, false);
    }

    @Override
    public void onActivityCreated(final Bundle bundle) {
        super.onActivityCreated(bundle);
        
        getActivity().setTitle(R.string.alertmanager_title);
        getLoaderManager().initLoader(0, null, this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateOptionsMenu(final Menu menu,
            final MenuInflater inflater) {
        // Inflate the menu.
        inflater.inflate(R.menu.alertmanager_option_menu, menu);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onPrepareOptionsMenu(final Menu menu) {
        super.onPrepareOptionsMenu(menu);
        
        final MenuItem deleteAllItem = menu.findItem(
                R.id.alertmanager_option_menu_delete_all);
        
        // Only show the 'Delete all alerts' item when there's alerts to delete.
        if(ad.getCount() > 0) {
            deleteAllItem.setVisible(true);
        } else {
            deleteAllItem.setVisible(false);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.alertmanager_option_menu_delete_all:
                // Show the 'Delete all alerts' confirmation DialogFragment.
                callbacks.onShowConfirmDeleteAllAlerts();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        // Return the only Loader for this Fragment.
        return new AlertCursorLoader(getActivity());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor c) {
        // Swap in the new Cursor. The superclass deals with closing the old
        // Cursor object.
        ad.swapCursor(c);
        // There may be change in status so refresh the options menu.
        getActivity().supportInvalidateOptionsMenu();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        // If the Loader has been reset, empty the ListAdapter.
        ad.swapCursor(null);
    }
    
    /**
     * This class allows the alert list to be loaded in the background.
     */
    private static class AlertCursorLoader extends CursorLoader {
        
        /**
         * Create a new AlertCursorLoader.
         * 
         * @param context A Context object.
         */
        public AlertCursorLoader(final Context context) {
            super(context, SettingsContract.Alerts.CONTENT_URI, null, null, null, null);
        }
    }

    /**
     * This CursorAdapter shows a list of alerts that have been set by the user.
     */
    private class AlertCursorAdapter extends CursorAdapter {
        
        private BusStopDatabase bsd;
        private int typeColumnIndex;
        private int busStopCodeIndex;
        private int distanceFromColumn;
        private int servicesColumn;
        private int timeTriggerColumn;
        
        /**
         * Create a new AlertCursorAdapter.
         * 
         * @param context A Context object.
         * @param c The Cursor to use.
         */
        public AlertCursorAdapter(final Context context, final Cursor c) {
            super(context, c, 0);
            
            // Get a reference to the BusStopDatabase.
            bsd = BusStopDatabase.getInstance(context.getApplicationContext());
        }
        
        /**
         * This getView() overrides the superclass implementation as we do not
         * want to use convertView here. There can only be 1 item of its type
         * shown in the list and therefore will never have a proper item to
         * convert from. This means that the View will need to be inflated from
         * XML each time it needs to be used.
         * 
         * @param position The position in the list, and therefore the Cursor,
         * to be used.
         * @param convertView A View to be converted from. This is not used in
         * this implementation.
         * @param parent The parent View.
         */
        @Override
        public View getView(final int position, final View convertView, final ViewGroup parent) {
            // Move the Cursor in to position. Throw an Exception if this is
            // not possible.
            if(!mCursor.moveToPosition(position)) {
                throw new IllegalStateException("Couldn't move cursor to position " + position);
            }
            
            // Create a new View.
            final View v = newView(mContext, mCursor, parent);
            // Populate the View
            bindView(v, mContext, mCursor);
            
            return v;
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public View newView(final Context context, final Cursor c, final ViewGroup container) {
            final LayoutInflater inflater = LayoutInflater.from(context);
            
            // The inflated layout depends on what time of alert it is showing.
            switch(c.getInt(typeColumnIndex)) {
                case SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY:
                    return inflater.inflate(R.layout.alertmanager_list_proximity, container, false);
                case SettingsContract.Alerts.ALERTS_TYPE_TIME:
                    return inflater.inflate(R.layout.alertmanager_list_time, container, false);
                default:
                    return null;
            }
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public void bindView(final View view, final Context context, final Cursor c) {
            // Get the stopCode and locality information.
            final String stopCode = c.getString(busStopCodeIndex);
            final String locality = bsd.getLocalityForStopCode(stopCode);
            final String busStop;
            
            // Append the locality if it is available.
            if(locality != null) {
                busStop = context.getString(R.string.busstop_locality_coloured,
                        bsd.getNameForBusStop(stopCode), locality, stopCode);
            } else {
                busStop = context.getString(R.string.busstop_coloured,
                        bsd.getNameForBusStop(stopCode), stopCode);
            }
            
            Button btn;
            TextView txt;
            
            // How the View is populated depends on the alert type.
            switch(c.getInt(typeColumnIndex)) {
                case SettingsContract.Alerts.ALERTS_TYPE_PROXIMITY:
                    btn = (Button)view.findViewById(R.id.btnRemoveProxAlert);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            callbacks.onShowConfirmDeleteProximityAlert();
                        }
                    });
                    
                    // Set information text.
                    txt = (TextView)view.findViewById(R.id.txtAlertManProx);
                    txt.setText(Html.fromHtml(context
                            .getString(R.string.alertmanager_prox_text,
                                c.getInt(distanceFromColumn), busStop)));
                    break;
                case SettingsContract.Alerts.ALERTS_TYPE_TIME:
                    btn = (Button)view.findViewById(R.id.btnRemoveTimeAlert);
                    btn.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(final View v) {
                            callbacks.onShowConfirmDeleteTimeAlert();
                        }
                    });
                    
                    txt = (TextView)view.findViewById(R.id.txtAlertManTime);
                    final int timeTrigger = c.getInt(timeTriggerColumn);
                    final String[] services = c.getString(servicesColumn).split(",");
                    final StringBuilder sb = new StringBuilder();
                    
                    for(String service : services) {
                        if(sb.length() > 0) sb.append(", ");
                        
                        sb.append(service);
                    }
                    
                    // Get the correct text to display, depending on plurality.
                    txt.setText(Html.fromHtml(
                            context.getResources().getQuantityString(
                                R.plurals.alertmanager_time_text,
                                timeTrigger == 0 ? 1 : timeTrigger, busStop,
                                sb.toString(), timeTrigger)));
                    break;
                default:
                    break;
            }
        }

        @Override
        public Cursor swapCursor(final Cursor newCursor) {
            if (newCursor != null) {
                typeColumnIndex = newCursor.getColumnIndex(SettingsContract.Alerts.TYPE);
                busStopCodeIndex = newCursor.getColumnIndex(SettingsContract.Alerts.STOP_CODE);
                distanceFromColumn = newCursor.getColumnIndex(SettingsContract.Alerts
                        .DISTANCE_FROM);
                servicesColumn = newCursor.getColumnIndex(SettingsContract.Alerts.SERVICE_NAMES);
                timeTriggerColumn = newCursor.getColumnIndex(SettingsContract.Alerts.TIME_TRIGGER);
            }

            return super.swapCursor(newCursor);
        }
    }
    
    /**
     * Any Activities which host this Fragment must implement this interface to
     * handle navigation events.
     */
    public interface Callbacks
            extends OnShowConfirmDeleteProximityAlertListener,
            OnShowConfirmDeleteTimeAlertListener {
        
        /**
         * This is called when it should be confirmed with the user that they
         * want to delete all alerts.
         */
        void onShowConfirmDeleteAllAlerts();
    }
}