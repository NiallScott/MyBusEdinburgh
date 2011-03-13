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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

/**
 * The EnterStopCodeActivity allows the user to manually enter a bus stop code
 * to get the information for that stop.
 *
 * @author Niall Scott
 */
public class EnterStopCodeActivity extends Activity
        implements View.OnClickListener, View.OnKeyListener {

    private EditText txt;
    private InputMethodManager imm;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.enterstopcode);
        setTitle(R.string.enterstopcode_title);
        Button button = (Button)findViewById(R.id.enterstopcode_submit);
        button.setOnClickListener(this);
        txt = (EditText)findViewById(R.id.enterstopcode_entry);
        txt.setOnKeyListener(this);
        imm = (InputMethodManager)getSystemService(INPUT_METHOD_SERVICE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(final View v) {
        task();
    }
    
    public boolean onKey(final View v, final int keyCode,
            final KeyEvent event) {
        if(event.getAction() == KeyEvent.ACTION_UP &&
                keyCode == KeyEvent.KEYCODE_ENTER) {
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