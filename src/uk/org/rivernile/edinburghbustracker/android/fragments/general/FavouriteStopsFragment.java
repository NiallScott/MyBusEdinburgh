/*
 * Copyright (C) 2009 - 2013 Niall 'Rivernile' Scott
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
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.v4.app.ListFragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v4.widget.SimpleCursorAdapter;
import android.text.Spanned;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.TouchDelegate;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import java.util.HashMap;
import uk.org.rivernile.android.utils.SimpleCursorLoader;
import uk.org.rivernile.edinburghbustracker.android
        .AddEditFavouriteStopActivity;
import uk.org.rivernile.edinburghbustracker.android.AddProximityAlertActivity;
import uk.org.rivernile.edinburghbustracker.android.AddTimeAlertActivity;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.BusStopMapActivity;
import uk.org.rivernile.edinburghbustracker.android.DisplayStopDataActivity;
import uk.org.rivernile.edinburghbustracker.android.R;
import uk.org.rivernile.edinburghbustracker.android.SettingsDatabase;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteFavouriteDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteProximityAlertDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .DeleteTimeAlertDialogFragment;

/**
 * This Fragment shows the user a list of their favourite bus stops. What this
 * Fragment does depends on the CREATE_SHORTCUT argument.
 * 
 * If CREATE_SHORTCUT is set to true;
 * 
 * - Do not show the context menu when the user long presses on a list item.
 * - When the user selects a list item, set the Activity result with an Intent
 * which sets the shortcut icon.
 * 
 * If CREATE_SHORTCUT is set to false;
 * 
 * - Allow the user to bring up a context menu when they long press on a list
 * item.
 * - When the user selects a list item, show the bus times for that bus stop.
 * 
 * @author Niall Scott
 */
public class FavouriteStopsFragment extends ListFragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        DeleteFavouriteDialogFragment.EventListener, OnClickListener{
    
    /** The argument to signify create shortcut mode. */
    public static final String CREATE_SHORTCUT = "CREATE_SHORTCUT";
    
    private static final String DELETE_FAV_DIALOG_TAG = "deleteFavDialog";
    private static final String DELETE_TIME_ALERT_DIALOG_TAG =
            "delTimeAlertDialog";
    private static final String DELETE_PROX_ALERT_DIALOG_TAG =
            "delProxAlertDialog";
    
    private SimpleCursorAdapter ca;
    private String selectedStopCode;
    private SettingsDatabase sd;
    private boolean isCreateShortcut = false;
    private View layoutProgress, layoutEmpty;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Cache the Activity instance.
        final Activity activity = getActivity();
        
        // Get an instance of the SettingsDatabase.
        sd = SettingsDatabase.getInstance(activity.getApplicationContext());
        // Determine the mode this Fragment should be in.
        isCreateShortcut = getArguments().getBoolean(CREATE_SHORTCUT);
        // Create the ListAdapter.
        if(isCreateShortcut) {
            ca = new FavouritesCursorAdapter(activity,
                    android.R.layout.simple_list_item_2, null,
                    new String[] { SettingsDatabase.FAVOURITE_STOPS_STOPNAME },
                    new int[] { android.R.id.text1 }, null);
        } else {
            ca = new FavouritesCursorAdapter(activity,
                    R.layout.favouritestops_list_item, null,
                    new String[] { SettingsDatabase.FAVOURITE_STOPS_STOPNAME },
                    new int[] { android.R.id.text1 }, this);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.favouritestops, container,
                false);
        
        layoutProgress = v.findViewById(R.id.layoutProgress);
        layoutEmpty = v.findViewById(R.id.layoutEmpty);
        
        return v;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        
        // Set the list adapter.
        setListAdapter(ca);
        
        // What title is set depends on the mode.
        if(isCreateShortcut) {
            getActivity().setTitle(R.string.favouriteshortcut_title);
        } else {
            getActivity().setTitle(R.string.favouritestops_title);
            // Allow the context menu to be shown in normal mode.
            registerForContextMenu(getListView());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        
        // Create the Loader.
        getLoaderManager().restartLoader(0, null, this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onListItemClick(final ListView l, final View v,
            final int position, final long id) {
        // Get the stopCode and cache the Activity.
        final String stopCode = String.valueOf(id);
        final Activity activity = getActivity();
        Intent intent;
        
        // What happens when the user selects a list item depends on what mode
        // is active.
        if(isCreateShortcut) {
            // Set the Intent which is used when the shortcut is tapped.
            intent = new Intent(Intent.ACTION_MAIN);
            intent.setClass(activity, DisplayStopDataActivity.class);
            intent.setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(DisplayStopDataActivity.ARG_STOPCODE, stopCode);

            // Set the Activity result to send back to the launcher, which
            // contains a name, Intent and icon.
            final Intent result = new Intent();
            result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
            result.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                    sd.getNameForStop(stopCode));
            final Parcelable icon = Intent.ShortcutIconResource
                    .fromContext(activity, R.drawable.appicon_favourite);
            result.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

            // Set the Activity result and exit.
            activity.setResult(Activity.RESULT_OK, result);
            activity.finish();
        } else {
            // View bus stop times.
            intent = new Intent(activity, DisplayStopDataActivity.class);
            intent.setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA);
            intent.putExtra(DisplayStopDataActivity.ARG_STOPCODE,
                    String.valueOf(id));
            startActivity(intent);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        
        // Get the menu inflater.
        final MenuInflater inflater = getActivity().getMenuInflater();
        final AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        // Get the stopCode.
        selectedStopCode = String.valueOf(info.id);
        
        // Set the header title of the context menu.
        menu.setHeaderTitle(sd.getNameForStop(String.valueOf(info.id)) + " (" +
                String.valueOf(info.id) + ")");
        // Inflate the menu from XML.
        inflater.inflate(R.menu.favouritestops_context_menu, menu);
        
        // Set the title of the proximity alert item depending whether one is
        // set or not.
        MenuItem item = menu.findItem(
                R.id.favouritestops_context_menu_prox_alert);
        
        if(sd.isActiveProximityAlert(selectedStopCode)) {
            item.setTitle(R.string.favouritestops_menu_prox_rem);
        } else {
            item.setTitle(R.string.favouritestops_menu_prox_add);
        }
        
        // Set the title of the time alert item depending whether one is set or
        // not.
        item = menu.findItem(R.id.favouritestops_context_menu_time_alert);
        
        if(sd.isActiveTimeAlert(selectedStopCode)) {
            item.setTitle(R.string.favouritestops_menu_time_rem);
        } else {
            item.setTitle(R.string.favouritestops_menu_time_add);
        }
        
        // If the Google Play Services is not available, then don't show the
        // option to show the stop on the map.
        item = menu.findItem(R.id.favouritestops_context_menu_showonmap);
        
        if(GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getActivity()) !=
                    ConnectionResult.SUCCESS) {
            item.setVisible(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        // Get the info so we can get the stopCode.
        final AdapterContextMenuInfo info =
                (AdapterContextMenuInfo)item.getMenuInfo();
        final Activity activity = getActivity();
        Intent intent;
        
        switch (item.getItemId()) {
            case R.id.favouritestops_context_menu_view:
                // Show bus times.
                intent = new Intent(activity, DisplayStopDataActivity.class);
                intent.setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA);
                intent.putExtra("stopCode", String.valueOf(info.id));
                startActivity(intent);
                return true;
            case R.id.favouritestops_context_menu_modify:
                // Allow the user to edit the name of the favourite stop.
                intent = new Intent(activity,
                        AddEditFavouriteStopActivity.class);
                intent.putExtra("stopCode", String.valueOf(info.id));
                startActivity(intent);
                return true;
            case R.id.favouritestops_context_menu_delete:
                // Show the DeleteFavouriteDialogFragment.
                final DeleteFavouriteDialogFragment deleteFavFrag =
                        DeleteFavouriteDialogFragment.newInstance(
                        selectedStopCode, this);
                deleteFavFrag.show(getFragmentManager(), DELETE_FAV_DIALOG_TAG);
                return true;
            case R.id.favouritestops_context_menu_showonmap:
                // Show the selected bus stop on the map.
                intent = new Intent(activity, BusStopMapActivity.class);
                intent.putExtra(BusStopMapActivity.ARG_STOPCODE,
                        String.valueOf(info.id));
                startActivity(intent);
                return true;
            case R.id.favouritestops_context_menu_prox_alert:
                // Either show the Activity which allows the user to add a
                // proximity alert, or the DialogFragment to confirm the alert's
                // removal.
                if(sd.isActiveProximityAlert(String.valueOf(info.id))) {
                    new DeleteProximityAlertDialogFragment()
                            .show(getFragmentManager(),
                            DELETE_PROX_ALERT_DIALOG_TAG);
                } else {
                    intent = new Intent(activity, AddProximityAlertActivity
                            .class);
                    intent.putExtra("stopCode", String.valueOf(info.id));
                    startActivity(intent);
                }
                return true;
            case R.id.favouritestops_context_menu_time_alert:
                // Either show the Activity which allows the user to add a time
                // alert, or the DialogFragment to confirm the alert's removal.
                if(sd.isActiveTimeAlert(String.valueOf(info.id))) {
                    new DeleteTimeAlertDialogFragment()
                            .show(getFragmentManager(),
                            DELETE_TIME_ALERT_DIALOG_TAG);
                } else {
                    intent = new Intent(activity, AddTimeAlertActivity.class);
                    intent.putExtra("stopCode", String.valueOf(info.id));
                    startActivity(intent);
                }
            default:
                return super.onContextItemSelected(item);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Loader<Cursor> onCreateLoader(final int i, final Bundle bundle) {
        layoutProgress.setVisibility(View.VISIBLE);
        layoutEmpty.setVisibility(View.GONE);
        
        // Return the only Loader of this Fragment.
        return new FavouritesCursorLoader(getActivity());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor c) {
        // When loading is complete, swap the Cursor. The old Cursor is
        // automatically closed.
        ca.swapCursor(c);
        
        if(c == null || c.getCount() == 0) {
            layoutProgress.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        // Give the adapter a null Cursor when the Loader is reset.
        ca.swapCursor(null);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onConfirmFavouriteDeletion() {
        // When the user deletes a favourite bus stop, reload the Cursor.
        getLoaderManager().restartLoader(0, null, this);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCancelFavouriteDeletion() {
        // Do nothing.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(final View v) {
        final int position = getListView().getPositionForView(v);
        if(position != AdapterView.INVALID_POSITION) {
            final Cursor c = ca.getCursor();
            if(c != null && c.moveToPosition(position)) {
                final DeleteFavouriteDialogFragment deleteFavFrag =
                        DeleteFavouriteDialogFragment.newInstance(
                        c.getString(0), this);
                deleteFavFrag.show(getFragmentManager(), DELETE_FAV_DIALOG_TAG);
            }
        }
    }
    
    /**
     * This Loader loads the user's list of favourite bus stops.
     */
    public static class FavouritesCursorLoader extends SimpleCursorLoader {
        
        private final SettingsDatabase sd;
        
        /**
         * Create a new instance of this Loader.
         * 
         * @param context A Context object.
         */
        public FavouritesCursorLoader(final Context context) {
            super(context);
            
            sd = SettingsDatabase.getInstance(context.getApplicationContext());
        }

        /**
         * {@inheritDoc}
         */
        @Override
        public Cursor loadInBackground() {
            final Cursor c = sd.getAllFavouriteStops();
            
            // This ensure's the Cursor's window is set properly.
            if(c != null) c.getCount();
            
            return c;
        }
    }
    
    /**
     * This is a custom Cursor adapter to add a services list to the row being
     * displayed.
     */
    public class FavouritesCursorAdapter extends SimpleCursorAdapter {
        
        private BusStopDatabase bsd;
        private final HashMap<String, Spanned> serviceListings =
                new HashMap<String, Spanned>();
        private final OnClickListener starClickListener;
        private final float density;
        
        /**
         * Create a new FavouritesCursorAdapter.
         * 
         * @param context A Context instance.
         * @param layout The layout reference ID.
         * @param c The Cursor to populate the list from.
         * @param from An array of Strings to map to UI components.
         * @param to An array of resource IDs to map the from Strings to.
         * @param starClickListener The callback for when the star is clicked.
         */
        public FavouritesCursorAdapter(final Context context, final int layout,
                final Cursor c, final String[] from, final int[] to,
                final OnClickListener starClickListener) {
            super(context, layout, c, from, to);
            
            bsd = BusStopDatabase.getInstance(context);
            this.starClickListener = starClickListener;
            this.density = context.getResources().getDisplayMetrics().density;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public View newView(final Context context, final Cursor cursor,
                final ViewGroup parent) {
            final View v = super.newView(context, cursor, parent);
            final ImageButton imgbtnFavourite = (ImageButton)v
                    .findViewById(R.id.imgbtnFavourite);
            if(imgbtnFavourite != null) {
                imgbtnFavourite.setFocusable(false);
                imgbtnFavourite.setFocusableInTouchMode(false);
                imgbtnFavourite.setOnClickListener(starClickListener);
                
                v.post(new Runnable() {
                    @Override
                    public void run() {
                        final int vertical = ((v.getHeight() -
                                imgbtnFavourite.getHeight()) / 2) + 1;
                        final Rect rect = new Rect();
                        imgbtnFavourite.getHitRect(rect);
                        rect.right += v.getPaddingRight();
                        rect.top -= vertical;
                        rect.bottom += vertical;
                        rect.left -= (int)(25 * density);
                        v.setTouchDelegate(new TouchDelegate(rect,
                                imgbtnFavourite));
                    }
                });
            }
            
            return v;
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public void bindView(final View v, final Context context,
                final Cursor cursor) {
            super.bindView(v, context, cursor);
            
            final String stopCode = cursor.getString(0);
            final TextView services = (TextView)v.findViewById(
                    android.R.id.text2);
            
            // Look to see if the service list is in the cache. If not, get it
            // from the database.
            Spanned s = serviceListings.get(stopCode);
            if(s == null) {
                s = BusStopDatabase.getColouredServiceListString(
                        bsd.getBusServicesForStopAsString(stopCode));
                if(s != null) {
                    services.setText(s);
                    serviceListings.put(stopCode, s);
                }
            } else {
                services.setText(s);
            }
        }
        
        /**
         * {@inheritDoc}
         */
        @Override
        public Cursor swapCursor(final Cursor newCursor) {
            // If the Cursor is being swapped, clear the service listings cache.
            serviceListings.clear();
            
            return super.swapCursor(newCursor);
        }
    }
}