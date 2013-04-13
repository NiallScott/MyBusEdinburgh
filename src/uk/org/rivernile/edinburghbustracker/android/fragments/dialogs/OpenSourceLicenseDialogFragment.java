/*
 * Copyright (C) 2013 Niall 'Rivernile' Scott
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

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import com.google.android.gms.common.GooglePlayServicesUtil;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This Fragment will show a Dialog which shows open source license information
 * for external code used inside this application.
 * 
 * @author Niall Scott
 */
public class OpenSourceLicenseDialogFragment extends DialogFragment {
    
    private static final boolean isHoneycombOrGreater =
            Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    
    /**
     * {@inheritDoc}
     */
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {
        final Context context = getActivity();
        
        // Get the AlertDialog.Builder with the correct theme set.
        final AlertDialog.Builder builder;
        if(isHoneycombOrGreater) {
            builder = AboutDialogFragment.getHoneycombDialog(context);
        } else {
            builder = new AlertDialog.Builder(context);
        }
        
        // License information may not be available for Google Play Services if
        // the device does not have Google Play Services installed.
        final String playServicesLicenses = GooglePlayServicesUtil
                .getOpenSourceSoftwareLicenseInfo(context);
        // This String contains non-Google Play Services license information.
        String appLicenses = getString(R.string.open_source_licenses);
        
        if(playServicesLicenses != null) {
            appLicenses = playServicesLicenses + appLicenses;
        }
        
        builder.setCancelable(true)
                .setTitle(R.string.opensourcelicensedialog_title)
                .setMessage(appLicenses)
                .setPositiveButton(R.string.close,
                    new DialogInterface.OnClickListener() {
             @Override
             public void onClick(final DialogInterface dialog, final int id) {
                dismiss();
             }
        });
        
        return builder.create();
    }
}