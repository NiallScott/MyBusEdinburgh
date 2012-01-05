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

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import java.util.List;

/**
 * The EnterStopCodeActivity allows the user to manually enter a bus stop code
 * to get the information for that stop.
 *
 * @author Niall Scott
 */
public class EnterStopCodeActivity extends Activity
        implements View.OnClickListener, View.OnKeyListener {
    
    private static final String BARCODE_INTENT =
            "com.google.zxing.client.android.SCAN";
    private static final String BARCODE_APP_PACKAGE =
            "com.google.zxing.client.android";
    
    private static final byte DIALOG_INSTALL_SCANNER = 1;

    private EditText txt;
    private InputMethodManager imm;
    private Intent barcodeIntent;
    private boolean barcodePackageAvailable = false;
    private Button scanButton, submitButton;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enterstopcode);
        setTitle(R.string.enterstopcode_title);
        
        submitButton = (Button)findViewById(R.id.enterstopcode_submit);
        submitButton.setOnClickListener(this);
        
        scanButton = (Button)findViewById(R.id.enterstopcode_barcode_button);
        scanButton.setOnClickListener(this);
        
        txt = (EditText)findViewById(R.id.enterstopcode_entry);
        txt.setOnKeyListener(this);
        txt.addTextChangedListener(new TextWatcher() {
            @Override
            public void afterTextChanged(final Editable s) {
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
        
        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
        barcodeIntent = new Intent(BARCODE_INTENT);
        barcodeIntent.putExtra("QR_CODE_MODE", true);
    }

    @Override
    public void onResume() {
        super.onResume();
        
        List<ResolveInfo> packages = getPackageManager()
                .queryIntentActivities(barcodeIntent, 0);
        if(packages == null || packages.isEmpty()) {
            barcodePackageAvailable = false;
        } else {
            barcodePackageAvailable = true;
        }
    }
    
    @Override
    public Dialog onCreateDialog(final int id) {
        switch(id) {
            case DIALOG_INSTALL_SCANNER:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(R.string.enterstopcode_scan_notavailable_title)
                        .setCancelable(true)
                        .setMessage(R.string
                                .enterstopcode_txt_scan_notavailable)
                        .setPositiveButton(R.string.yes,
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int which) {
                                Intent intent = new Intent(Intent.ACTION_VIEW);
                                intent.setData(Uri.parse("market://details?id="
                                    + BARCODE_APP_PACKAGE));
                                try {
                                    startActivity(intent);
                                } catch(ActivityNotFoundException e) {
                                    Toast.makeText(EnterStopCodeActivity.this,
                                            R.string.enterstopcode_no_market,
                                            Toast.LENGTH_LONG).show();
                                }
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton(R.string.no,
                                new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(final DialogInterface dialog,
                                    final int which) {
                                dialog.dismiss();
                            }
                        })
                        .setInverseBackgroundForced(true);
                
                return builder.create();
            default:
                return null;
        }
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(final View v) {
        if(v == scanButton) {
            if(barcodePackageAvailable) {
                startActivityForResult(barcodeIntent, 0);
            } else {
                showDialog(DIALOG_INSTALL_SCANNER);
            }
        } else if(v == submitButton) {
            task();
        }
    }
    
    @Override
    public void onActivityResult(final int requestCode, final int resultCode,
            final Intent data) {
        if(resultCode == RESULT_OK) {
            Uri uri = Uri.parse(data.getStringExtra("SCAN_RESULT"));
            String stopCode = uri.getQueryParameter("busStopCode");
            if(stopCode != null && stopCode.length() > 0) {
                txt.setText(stopCode);
                Intent intent = new Intent(this, DisplayStopDataActivity.class);
                intent.setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA);
                intent.putExtra("stopCode", stopCode);
                startActivity(intent);
            } else {
                Toast.makeText(this, R.string.enterstopcode_invalid_qrcode,
                        Toast.LENGTH_LONG).show();
            }
        }
    }
    
    public boolean onKey(final View v, final int keyCode,
            final KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_UP &&
                keyCode == KeyEvent.KEYCODE_ENTER &&
                txt.getText().length() == 8) {
            imm.hideSoftInputFromWindow(txt.getWindowToken(), 0);
            task();
        }
        return false;
    }

    private void task() {
        if(txt.getText().length() == 0) {
            Toast.makeText(this, R.string.enterstopcode_toast_inputerr,
                    Toast.LENGTH_LONG).show();
            return;
        } else {
            Intent intent = new Intent(this, DisplayStopDataActivity.class);
            intent.setAction(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA);
            intent.putExtra("stopCode", txt.getText().toString().trim());
            startActivity(intent);
        }
    }
}