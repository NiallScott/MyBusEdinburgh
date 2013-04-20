/*
 * Copyright (C) 2012 - 2013 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.fragments.dialogs;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.SQLException;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.util.Calendar;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This Fragment displays the application About Dialog.
 * 
 * @author Niall Scott
 */
public class AboutDialogFragment extends DialogFragment {
    
    private static final String LICENSE_DIALOG_TAG = "licenseDialog";
    private static final boolean isHoneycombOrGreater =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    private static final DateFormat dateFormat =
            DateFormat.getDateTimeInstance();
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Activity activity = getActivity();
        
        // Get the inflater then inflate the view from XML.
        final LayoutInflater inflater = LayoutInflater.from(activity);
        final View layout = inflater.inflate(R.layout.about, null);

        final TextView temp = (TextView)layout.findViewById(R.id.aboutVersion);
        
        // Set the version text.
        try {
            temp.setText(getString(R.string.aboutdialog_version,
                activity.getPackageManager()
                    .getPackageInfo(activity.getPackageName(), 0)
                    .versionName,
                activity.getPackageManager()
                    .getPackageInfo(activity.getPackageName(), 0).versionCode
            ));
        } catch(NameNotFoundException e) {
            // This should never occur.
            temp.setText("Unknown");
        }

        final TextView txtDBVersion = (TextView)layout.findViewById(R.id
                .aboutDBVersion);
        final TextView txtTopoVersion = (TextView)layout.findViewById(
                R.id.aboutTopoVersion);
        
        // Get the database mod time.
        long dbtime;
        final Calendar date = Calendar.getInstance();
        final BusStopDatabase bsd = BusStopDatabase.getInstance(activity);
        try {
            dbtime = bsd.getLastDBModTime();
        } catch(SQLException e) {
            dbtime = 0;
        }
        
        date.setTimeInMillis(dbtime);

        // Set the DB version text.
        txtDBVersion.setText(getString(R.string.aboutdialog_dbversion, dbtime,
                dateFormat.format(date.getTime())));
        // Set the topology ID text.
        txtTopoVersion.setText(getString(R.string.aboutdialog_topology,
                bsd.getTopoId()));
        
        final Button btnLicenses = (Button)layout
                .findViewById(R.id.btnLicenses);
        btnLicenses.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {
                new OpenSourceLicenseDialogFragment().show(getFragmentManager(),
                        LICENSE_DIALOG_TAG);
            }
        });

        // Get the AlertDialog.Builder with the correct theme set.
        final AlertDialog.Builder builder;
        if(isHoneycombOrGreater) {
            builder = getHoneycombDialog(activity);
        } else {
            builder = new AlertDialog.Builder(activity);
        }
        
        // Build the Dialog.
        builder.setView(layout)
                .setNegativeButton(R.string.close,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(final DialogInterface dialog,
                            final int id) {
                        dismiss();
                    }
                });

        return builder.create();
    }
    
    /**
     * Attempt to get a THEME_HOLO_DARK dialog when on Honeycomb (Android 3.0)
     * or greater.
     * 
     * @param context The Activity context.
     * @return An AlertDialog.Builder instance to create the Dialog on top of.
     */
    @TargetApi(11)
    public static AlertDialog.Builder getHoneycombDialog(
            final Context context) {
        try {
            // Get the class.
            Class cls = AlertDialog.Builder.class;
            Class[] partypes = new Class[2];
            // Set the parameter types.
            partypes[0] = Context.class;
            partypes[1] = Integer.TYPE;
            // Get the constructor.
            Constructor ct = cls.getConstructor(partypes);
            // Set the arguments
            Object[] arglist = new Object[2];
            arglist[0] = context;
            arglist[1] = Integer.valueOf(AlertDialog.THEME_HOLO_DARK);
            // Call the constructor, returning the new instance.
            return (AlertDialog.Builder)ct.newInstance(arglist);
        } catch(NoSuchMethodException e) {
            
        } catch(java.lang.InstantiationException e) {
            
        } catch(IllegalAccessException e) {
            
        } catch(InvocationTargetException e) {
            
        }
        
        return null;
    }
}