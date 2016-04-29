/*
 * Copyright (C) 2009 - 2016 Niall 'Rivernile' Scott
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
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.List;
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowBusTimesListener;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This Fragment allows the user to manually enter a bus stop code or to
 * initiate the QR code scanning.
 * 
 * @author Niall Scott
 */
public class EnterStopCodeFragment extends Fragment
        implements View.OnClickListener, View.OnKeyListener {
    
    private static final Intent BARCODE_INTENT;
    
    private Callbacks callbacks;
    private EditText txt;
    private InputMethodManager imm;
    private boolean barcodePackageAvailable = false;
    private Button scanButton, submitButton;
    
    static {
        // Set up this Intent statically as it can be reused.
        BARCODE_INTENT = new Intent("com.google.zxing.client.android.SCAN");
        BARCODE_INTENT.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
        BARCODE_INTENT.putExtra("QR_CODE_MODE", true);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);
        
        try {
            callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new IllegalStateException(context.getClass().getName() +
                    " does not implement " + Callbacks.class.getName());
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        // Get the Input Method Service.
        imm = (InputMethodManager)getActivity().getSystemService(
                Context.INPUT_METHOD_SERVICE);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater,
            final ViewGroup container, final Bundle savedInstanceState) {
        // Inflate the View from the XML resource.
        final View v = inflater.inflate(R.layout.enterstopcode, container,
                false);
        
        // Get the UI elements.
        submitButton = (Button)v.findViewById(R.id.enterstopcode_submit);
        submitButton.setOnClickListener(this);
        
        scanButton = (Button)v.findViewById(R.id.enterstopcode_barcode_button);
        scanButton.setOnClickListener(this);
        
        txt = (EditText)v.findViewById(R.id.enterstopcode_entry);
        txt.setOnKeyListener(this);
        txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(final Editable s) {
                // Only enable the confirm button if the length is 8 chars.
                if(s.length() == 8) {
                    submitButton.setEnabled(true);
                } else {
                    submitButton.setEnabled(false);
                }
            }
            
            @Override
            public void beforeTextChanged(final CharSequence s, final int start,
                    final int count, final int after) { }
            
            @Override
            public void onTextChanged(final CharSequence s, final int start,
                    final int before, final int count) { }
        });
        
        return v;
    }

    @Override
    public void onActivityCreated(final Bundle bundle) {
        super.onActivityCreated(bundle);
        
        getActivity().setTitle(R.string.enterstopcode_title);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        
        // Get a list of all applications which handle the barcode intent.
        final List<ResolveInfo> packages = getActivity().getPackageManager()
                .queryIntentActivities(BARCODE_INTENT, 0);
        // If the list does not exist or is empty, there are no Activities to
        // handle the barcode intent.
        if(packages == null || packages.isEmpty()) {
            barcodePackageAvailable = false;
        } else {
            barcodePackageAvailable = true;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(final View v) {
        if(v == scanButton) {
            if(barcodePackageAvailable) {
                try {
                    // Attempt to start the barcode scanning Activity if it is
                    // available.
                    startActivityForResult(BARCODE_INTENT, 0);
                } catch(ActivityNotFoundException e) { }
            } else {
                // The barcode scanning Activity is not available, alert the
                // user.
                callbacks.onAskInstallBarcodeScanner();
            }
        } else if(v == submitButton) {
            // The user typed in a stop code, load the bus times.
            task();
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
        final Activity activity = getActivity();
        
        // The result code signified success.
        if(resultCode == Activity.RESULT_OK) {
            // Make sure there is a data Intent. There have been some crashes
            // caused by this being null.
            if (data == null) {
                Toast.makeText(activity, R.string.enterstopcode_scan_error,
                        Toast.LENGTH_LONG).show();
                return;
            }
            
            // Get the data from the Intent.
            final Uri uri = Uri.parse(data.getStringExtra("SCAN_RESULT"));
            // We can only handle hierarchical URIs. Make sure it is so.
            if(!uri.isHierarchical()) {
                // Tell the user the URI was invalid.
                Toast.makeText(activity, R.string.enterstopcode_invalid_qrcode,
                        Toast.LENGTH_LONG).show();
                return;
            }
            
            // Get the busStopCode parameter from the URI.
            final String stopCode = uri.getQueryParameter("busStopCode");
            // Do basic sanity checking on the parameter.
            if(stopCode != null && stopCode.length() > 0) {
                // Set the EditText to the value of this stop code.
                txt.setText(stopCode);
                // Launch bus times.
                callbacks.onShowBusTimes(stopCode);
            } else {
                Toast.makeText(activity, R.string.enterstopcode_invalid_qrcode,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onKey(final View v, final int keyCode,
            final KeyEvent event) {
        // Grab the enter key.
        if(event.getAction() == KeyEvent.ACTION_UP &&
                keyCode == KeyEvent.KEYCODE_ENTER &&
                txt.getText().length() == 8) {
            imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);
            task();
        }
        
        return false;
    }
    
    /**
     * Call the DisplayStopDataActivity with the stop code given in the EditText
     * box.
     */
    private void task() {
        final Activity activity = getActivity();
        
        // Should never happen, but make sure there is a stop code to work with.
        if(txt.getText().length() == 0) {
            Toast.makeText(activity, R.string.enterstopcode_toast_inputerr,
                    Toast.LENGTH_LONG).show();
        } else {
            // Load bus times.
            callbacks.onShowBusTimes(txt.getText().toString().trim());
        }
    }
    
    /**
     * Any Activities which host this Fragment must implement this interface to
     * handle navigation events.
     */
    public static interface Callbacks extends OnShowBusTimesListener {
        
        /**
         * This is called when the user is asked if they want to install
         * a barcode scanner or not.
         */
        public void onAskInstallBarcodeScanner();
    }
}