/*
 * Copyright (C) 2010 - 2012 Niall 'Rivernile' Scott
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

import static uk.org.rivernile.edinburghbustracker.android.fetchers
        .FetchNewsUpdatesTask.ERROR_NODATA;
import static uk.org.rivernile.edinburghbustracker.android.fetchers
        .FetchNewsUpdatesTask.ERROR_PARSEERR;
import static uk.org.rivernile.edinburghbustracker.android.fetchers
        .FetchNewsUpdatesTask.ERROR_IOERR;
import static uk.org.rivernile.edinburghbustracker.android.fetchers
        .FetchNewsUpdatesTask.ERROR_URLERR;

import uk.org.rivernile.edinburghbustracker.android.fetchers
        .FetchNewsUpdatesTask;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ListActivity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SimpleAdapter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import uk.org.rivernile.edinburghbustracker.android.fetchers.NewsEvent;
import uk.org.rivernile.edinburghbustracker.android.fetchers.TwitterNewsItem;

public class NewsUpdatesActivity extends ListActivity implements NewsEvent {

    /* Constants for dialogs */
    private static final int DIALOG_PROGRESS = 0;

    private boolean progressDialogShown = false;
    private FetchNewsUpdatesTask fetchTask;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.newsupdates);
        setTitle(R.string.newsupdates_title);

        fetchTask = FetchNewsUpdatesTask.getInstance(this);
    }

    @Override
    public void onResume() {
        super.onResume();

        final ArrayList<TwitterNewsItem> temp = fetchTask.getNewsItems();
        if(temp != null && temp.size() > 0) {
            populateList(temp);
        } else if(!fetchTask.isExecuting()) {
            fetchTask.doTask();
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        fetchTask.setHandler(null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onCreateOptionsMenu(final Menu menu) {
        getMenuInflater().inflate(R.menu.newsupdates_option_menu, menu);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case R.id.newsupdates_option_menu_refresh:
                fetchTask.doTask();
                break;
            default:
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected Dialog onCreateDialog(final int id) {
        switch(id) {
            case DIALOG_PROGRESS:
                final ProgressDialog prog = new ProgressDialog(this);
                prog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                prog.setCancelable(true);
                prog.setMessage(getString(
                        R.string.displaystopdata_gettingdata));
                prog.setOnCancelListener(new DialogInterface
                        .OnCancelListener() {
                    public void onCancel(DialogInterface di) {
                        finish();
                    }
                });
                progressDialogShown = true;
                return prog;
            default:
                return null;
        }
    }
    
    @Override
    public void onPreExecute() {
        showDialog(DIALOG_PROGRESS);
    }
    
    @Override
    public void onNewsAvailable(final ArrayList<TwitterNewsItem> result) {
        populateList(result);
    }
    
    @Override
    public void onFetchError(final int error) {
        handleError(error);
    }

    private void handleError(final int errorCode) {
        if(progressDialogShown) dismissDialog(DIALOG_PROGRESS);
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        switch(errorCode) {
            case ERROR_NODATA:
                builder.setMessage(R.string.newsupdates_err_nodata);
                break;
            case ERROR_PARSEERR:
                builder.setMessage(R.string.newsupdates_err_parseerr);
                break;
            case ERROR_IOERR:
                builder.setMessage(R.string.newsupdates_err_ioerr);
                break;
            case ERROR_URLERR:
                builder.setMessage(R.string.newsupdates_err_urlerr);
                break;
            default:
                break;
        }
        builder.setCancelable(false).setTitle(R.string.error)
                .setPositiveButton(R.string.retry,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface di, final int i) {
                fetchTask.doTask();
            }
        }).setNegativeButton(R.string.cancel,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface di, final int i) {
                di.dismiss();
            }
        });
        builder.create().show();
    }
    
    private void populateList(final ArrayList<TwitterNewsItem> items) {
        if(items == null || items.isEmpty()) {
            handleError(ERROR_NODATA);
            return;
        }
        
        final ArrayList<HashMap<String, String>> list =
                new ArrayList<HashMap<String, String>>();
        HashMap<String, String> map;
        for(TwitterNewsItem item : items) {
            map = new HashMap<String, String>();
            map.put("TEXT", item.getBody());
            map.put("INFO", item.getPoster() + " - " + item.getDate());
            list.add(map);
        }
        
        NewsItemsAdapter adapter = new NewsItemsAdapter(this, list,
                R.layout.newsupdateslist, new String[] { "TEXT",
                "INFO" }, new int[] { R.id.twitText, R.id.twitInfo });
        setListAdapter(adapter);
        
        if(progressDialogShown) dismissDialog(DIALOG_PROGRESS);
    }
    
    private class NewsItemsAdapter extends SimpleAdapter {
        
        public NewsItemsAdapter(final Context context,
                final List<? extends Map<String, ?>> data, final int resource,
                final String[] from, final int[] to) {
            super(context, data, resource, from, to);
        }
        
        @Override
        public boolean isEnabled(final int index) {
            return false;
        }
    }
}