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

package uk.org.rivernile.edinburghbustracker.android;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.widget.Toast;
import uk.org.rivernile.android.utils.NavigationUtils;
import uk.org.rivernile.edinburghbustracker.android.fragments.dialogs
        .InstallBarcodeScannerDialogFragment;
import uk.org.rivernile.edinburghbustracker.android.fragments.general
        .EnterStopCodeFragment;

/**
 * The EnterStopCodeActivity allows the user to manually enter a bus stop code
 * to get the information for that stop.
 *
 * @author Niall Scott
 * @see EnterStopCodeFragment
 */
public class EnterStopCodeActivity extends ActionBarActivity
        implements EnterStopCodeFragment.Callbacks,
        InstallBarcodeScannerDialogFragment.Callbacks {
    
    private static final String BARCODE_APP_PACKAGE =
            "market://details?id=com.google.zxing.client.android";
    
    private static final String DIALOG_INSTALL_BARCODE_SCANNER =
            "installBarcodeScannerDialog";

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setContentView(R.layout.enterstopcode_activity);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onOptionsItemSelected(final MenuItem item) {
        switch(item.getItemId()) {
            case android.R.id.home:
                NavigationUtils.navigateUpOnActivityWithSingleEntryPoint(this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onAskInstallBarcodeScanner() {
        new InstallBarcodeScannerDialogFragment()
                .show(getSupportFragmentManager(),
                        DIALOG_INSTALL_BARCODE_SCANNER);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowInstallBarcodeScanner() {
        final Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(BARCODE_APP_PACKAGE));

        try {
            startActivity(intent);
        } catch(ActivityNotFoundException e) {
            Toast.makeText(this, R.string.barcodescannerdialog_noplaystore,
                    Toast.LENGTH_LONG).show();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onShowBusTimes(final String stopCode) {
        final Intent intent = new Intent(this, DisplayStopDataActivity.class);
        intent.putExtra(DisplayStopDataActivity.ARG_STOPCODE, stopCode);
        startActivity(intent);
    }
}