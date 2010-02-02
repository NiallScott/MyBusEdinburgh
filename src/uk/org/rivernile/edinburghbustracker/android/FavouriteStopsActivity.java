/*
 * Copyright (C) 2009 Niall 'Rivernile' Scott
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
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;

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

    private ListAdapter ca;
    private Cursor c;
    private String selectedStopCode;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.favouritestops);
        setTitle(R.string.favouritestops_title);

        c = SettingsDatabase.getAllFavouriteStops(this);
        startManagingCursor(c);
        ca = new SimpleCursorAdapter(this, android.R.layout.simple_list_item_1,
                c, new String[] { SettingsDatabase.FAVOURITE_STOPS_STOPNAME },
                new int[] { android.R.id.text1 });
        setListAdapter(ca);
        registerForContextMenu(getListView());
    }

    @Override
    public void onDestroy() {
        c.close();
        super.onDestroy();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onListItemClick(final ListView l, final View v,
            final int position, final long id)
    {
        Intent intent = new Intent(this, DisplayStopDataActivity.class);
        intent.setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA);
        intent.putExtra("stopCode", String.valueOf(id));
        startActivity(intent);
    }

    @Override
    public void onCreateContextMenu(final ContextMenu menu, final View v,
            final ContextMenuInfo menuInfo)
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        AdapterContextMenuInfo info = (AdapterContextMenuInfo)menuInfo;
        menu.setHeaderTitle(String.valueOf(info.id) + " " + SettingsDatabase
                .getNameForStop(this, String.valueOf(info.id)));
        menu.add(0, CONTEXT_MENU_VIEW, 1, R.string.favouritestops_menu_view);
        menu.add(0, CONTEXT_MENU_MODIFY, 1, R.string.favouritestops_menu_edit);
        menu.add(0, CONTEXT_MENU_DELETE, 2, R.string
                .favouritestops_menu_delete);
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
                selectedStopCode = String.valueOf(info.id);
                showDialog(0);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setCancelable(false)
                .setTitle(R.string.favouritestops_dialog_confirm_title)
                .setPositiveButton(R.string.okay,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                            final int id)
                    {
                        SettingsDatabase.deleteFavouriteStop(
                                FavouriteStopsActivity.this, selectedStopCode);
                        FavouriteStopsActivity.this.c.requery();
                    }
                }).setNegativeButton(R.string.cancel,
                        new DialogInterface.OnClickListener() {
                     public void onClick(final DialogInterface dialog,
                             final int id)
                     {
                        dismissDialog(0);
                     }
        });
        return builder.create();
    }
}