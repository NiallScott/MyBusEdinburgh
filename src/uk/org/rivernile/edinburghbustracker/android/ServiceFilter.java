/*
 * Copyright (C) 2011 Niall 'Rivernile' Scott
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
import android.content.Context;
import android.content.DialogInterface;

public class ServiceFilter {
    
    private static ServiceFilter instance = null;
    
    private String[] services;
    private boolean[] checkBoxes;
    private Context context;
    private BusStopDatabase bsd;
    private Filterable callback;
    
    private ServiceFilter(final Context context) {
        bsd = BusStopDatabase.getInstance(context);
        this.context = context;
        services = bsd.getBusServiceList();
        checkBoxes = new boolean[services.length];
    }
    
    public static ServiceFilter getInstance(final Context context) {
        if(instance == null) {
            instance = new ServiceFilter(context);
        } else {
            instance.context = context;
        }
        return instance;
    }
    
    public void setCallback(final Filterable callback) {
        this.callback = callback;
    }
    
    public Dialog getFilterDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true).setTitle(R.string.servicefilter_title);
        builder.setMultiChoiceItems(bsd.getBusServiceList(), checkBoxes,
                new DialogInterface.OnMultiChoiceClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int which,
                    boolean isChecked) {
                checkBoxes[which] = isChecked;
            }
        });
        
        builder.setPositiveButton(R.string.close,
                new DialogInterface.OnClickListener() {
            @Override
            public void onClick(final DialogInterface dialog, final int id) {
                dialog.dismiss();
            }
        });
        
        Dialog d = builder.create();
        d.setOnDismissListener(new Dialog.OnDismissListener() {
            @Override
            public void onDismiss(final DialogInterface dialog) {
                if(callback != null) callback.onFilterSet();
            }
        });
        return d;
    }
    
    public boolean isFiltered() {
        int length = checkBoxes.length;
        
        for(int i = 0; i < length; i++) {
            if(checkBoxes[i]) return true;
        }
        
        return false;
    }
    
    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        int length = checkBoxes.length;
        
        for(int i = 0; i < length; i++) {
            if(checkBoxes[i]) {
                if(sb.length() == 0) {
                    sb.append('\'');
                    sb.append(services[i]);
                    sb.append('\'');
                } else {
                    sb.append(',');
                    sb.append('\'');
                    sb.append(services[i]);
                    sb.append('\'');
                }
            }
        }
        
        if(sb.length() == 0) {
            return null;
        } else {
            return sb.toString();
        }
    }
}