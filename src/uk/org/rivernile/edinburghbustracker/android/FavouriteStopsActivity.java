/*
 * Copyright (C) 2009 - 2011 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.os.Parcelable;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import java.util.HashMap;
import uk.org.rivernile.edinburghbustracker.android.alerts.AlertManager;

/**
 * The FavouriteStopsActivity displays the user a list of their saved favourite
 * bus stops and allows them to perform actions on them.
 *
 * @author Niall Scott
 */
public class FavouriteStopsActivity extends ListActivity {

    private final static int CONTEXT_MENU_VIEW = ContextMenu.FIRST;
    private final static int CONTEXT_MENU_MODIFY = ContextMenu.FIRST +1;
    private final static int CONTEXT_MENU_DELETE = ContextMenu.FIRST + 2;
    private final static int CONTEXT_MENU_PROX = ContextMenu.FIRST + 3;
    private final static int CONTEXT_MENU_TIME = ContextMenu.FIRST + 4;
    private final static int CONTEXT_MENU_SHOWONMAP = ContextMenu.FIRST + 5;
    
    private final static int DIALOG_FAV_DEL = 0;
    private final static int DIALOG_PROX_DEL = 1;
    private final static int DIALOG_TIME_DEL = 2;

    private ListAdapter ca;
    private Cursor c;
    private String selectedStopCode;
    private SettingsDatabase sd;
    private AlertManager alertMan;
    private boolean isCreateShortcut = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favouritestops);
        
        if(savedInstanceState != null && savedInstanceState
                .containsKey("selectedStopCode")) {
            selectedStopCode = savedInstanceState.getString("selectedStopCode");
        }

        sd = SettingsDatabase.getInstance(this);
        alertMan = AlertManager.getInstance(getApplicationContext());
        c = sd.getAllFavouriteStops();
        startManagingCursor(c);
        ca = new FavouritesCursorAdapter(this,
                android.R.layout.simple_list_item_2, c,
                new String[] { SettingsDatabase.FAVOURITE_STOPS_STOPNAME },
                new int[] { android.R.id.text1 });
        setListAdapter(ca);
        
        isCreateShortcut = Intent.ACTION_CREATE_SHORTCUT.equals(
                getIntent().getAction());
        
        if(isCreateShortcut) {
            setTitle(R.string.favouriteshortcut_title);
        } else {
            setTitle(R.string.favouritestops_title);
            registerForContextMenu(getListView());
        }
    }
    
    @Override
    public void onSaveInstanceState(final Bundle outState) {
        super.onSaveInstanceState(outState);
        
        if(selectedStopCode != null) {
            outState.putString("selectedStopCode", selectedStopCode);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onListItemClick(final ListView l, final View v,
            final int position, final long id)
    {
        String stopCode = String.valueOf(id);
        Intent intent;
        
        if(isCreateShortcut) {
            intent = new Intent(Intent.ACTION_MAIN);
            intent.setClass(this, DisplayStopDataActivity.class);
            intent.putExtra("stopCode", stopCode);

            Intent result = new Intent();
            result.putExtra(Intent.EXTRA_SHORTCUT_INTENT, intent);
            result.putExtra(Intent.EXTRA_SHORTCUT_NAME,
                    sd.getNameForStop(stopCode));
            Parcelable icon = Intent.ShortcutIconResource.fromContext(this,
                    R.drawable.appicon);
            result.putExtra(Intent.EXTRA_SHORTCUT_ICON_RESOURCE, icon);

            setResult(RESULT_OK, result);
            finish();
        } else {
            intent = new Intent(this, DisplayStopDataActivity.class);
            intent.setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA);
            intent.putExtra("stopCode", String.valueOf(id));
            startActivity(intent);
        }
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        selectedStopCode = String.valueOf(info.id);
        
        menu.setHeaderTitle(sd.getNameForStop(String.valueOf(info.id)) + " (" +
                String.valueOf(info.id) + ")");
        menu.add(0, CONTEXT_MENU_VIEW, 1, R.string.favouritestops_menu_view);
        menu.add(0, CONTEXT_MENU_MODIFY, 2, R.string.favouritestops_menu_edit);
        menu.add(0, CONTEXT_MENU_DELETE, 3, R.string
                .favouritestops_menu_delete);
        
        if(sd.isActiveProximityAlert(selectedStopCode)) {
            menu.add(0, CONTEXT_MENU_PROX, 4, R.string.alert_prox_rem);
        } else {
            menu.add(0, CONTEXT_MENU_PROX, 4, R.string.alert_prox_add);
        }
        
        if(sd.isActiveTimeAlert(selectedStopCode)) {
            menu.add(0, CONTEXT_MENU_TIME, 5, R.string.alert_time_rem);
        } else {
            menu.add(0, CONTEXT_MENU_TIME, 5, R.string.alert_time_add);
        }
        
        menu.add(0, CONTEXT_MENU_SHOWONMAP, 6, R.string
                .favouritestops_menu_showonmap);
    }

    @Override
    public boolean onContextItemSelected(final MenuItem item) {
        AdapterContextMenuInfo info =
                (AdapterContextMenuInfo)item.getMenuInfo();
        Intent intent;
        switch (item.getItemId()) {
            case CONTEXT_MENU_VIEW:
                intent = new Intent(this, DisplayStopDataActivity.class);
                intent.setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA);
                intent.putExtra("stopCode", String.valueOf(info.id));
                startActivity(intent);
                return true;
            case CONTEXT_MENU_MODIFY:
                intent = new Intent(this,
                        AddEditFavouriteStopActivity.class);
                intent.setAction(AddEditFavouriteStopActivity
                        .ACTION_ADD_EDIT_FAVOURITE_STOP);
                intent.putExtra("stopCode", String.valueOf(info.id));
                startActivity(intent);
                return true;
            case CONTEXT_MENU_DELETE:
                showDialog(DIALOG_FAV_DEL);
                return true;
            case CONTEXT_MENU_SHOWONMAP:
                intent = new Intent(this, BusStopMapActivity.class);
                intent.putExtra("stopCode", String.valueOf(info.id));
                intent.putExtra("zoom", 19);
                startActivity(intent);
                return true;
            case CONTEXT_MENU_PROX:
                if(sd.isActiveProximityAlert(String.valueOf(info.id))) {
                    showDialog(DIALOG_PROX_DEL);
                } else {
                    intent = new Intent(this, AddProximityAlertActivity.class);
                    intent.putExtra("stopCode", String.valueOf(info.id));
                    startActivity(intent);
                }
                return true;
            case CONTEXT_MENU_TIME:
                if(sd.isActiveTimeAlert(String.valueOf(info.id))) {
                    showDialog(DIALOG_TIME_DEL);
                } else {
                    intent = new Intent(this, AddTimeAlertActivity.class);
                    intent.putExtra("stopCode", String.valueOf(info.id));
                    startActivity(intent);
                }
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch(id) {
            case DIALOG_FAV_DEL:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setCancelable(true)
                        .setTitle(R.string.favouritestops_dialog_confirm_title)
                        .setPositiveButton(R.string.okay,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int id)
                            {
                                sd.deleteFavouriteStop(selectedStopCode);
                                FavouriteStopsActivity.this.c.requery();
                            }
                        }).setNegativeButton(R.string.cancel,
                                new DialogInterface.OnClickListener() {
                             public void onClick(final DialogInterface dialog,
                                     final int id)
                             {
                                dialog.dismiss();
                             }
                });
                return builder.create();
            case DIALOG_PROX_DEL:
                return alertMan.getConfirmDeleteProxAlertDialog(this);
            case DIALOG_TIME_DEL:
                return alertMan.getConfirmDeleteTimeAlertDialog(this);
            default:
                return null;
        }
    }
    
    public static class FavouritesCursorAdapter extends SimpleCursorAdapter {
        
        private BusStopDatabase bsd;
        private final HashMap<String, String> serviceListings =
                new HashMap<String, String>();
        
        public FavouritesCursorAdapter(final Context context, final int layout,
                final Cursor c, final String[] from, final int[] to) {
            super(context, layout, c, from, to);
            
            bsd = BusStopDatabase.getInstance(context);
            
            String stopCode;
            while(c.moveToNext()) {
                stopCode = c.getString(0);
                serviceListings.put(stopCode,
                        bsd.getBusServicesForStopAsString(stopCode));
            }
        }
        
        @Override
        public View getView(final int position, final View convertView,
                final ViewGroup parent) {
            View v = super.getView(position, convertView, parent);
            
            String stopCode = getCursor().getString(0);
            TextView services = (TextView)v.findViewById(android.R.id.text2);
            
            if(serviceListings.containsKey(stopCode)) {
                services.setText(serviceListings.get(stopCode));
            } else {
                String s = bsd.getBusServicesForStopAsString(stopCode);
                services.setText(s);
                serviceListings.put(stopCode, s);
            }
            
            return v;
        }
    }
}