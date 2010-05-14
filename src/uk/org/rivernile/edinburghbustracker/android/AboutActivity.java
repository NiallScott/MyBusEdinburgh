/*
 * Copyright (C) 2009 - 2010 Niall 'Rivernile' Scott
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
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.widget.TextView;
import java.util.Calendar;

/**
 * The AboutActivity displays to the user information about the application.
 *
 * @author Niall Scott
 */
public class AboutActivity extends Activity {

    private final static String URL = "href=\"http://www.rivernile.org.uk/" +
            "bustracker/\">http://www.rivernile.org.uk/bustracker/";

    private int versionCode;
    private String versionName;
    private String appName;

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(getString(R.string.about_title) + " " +
                getString(R.string.app_name));
        setContentView(R.layout.about);
        try {
            versionCode = getPackageManager().getPackageInfo(getPackageName(),
                    0).versionCode;
            versionName = getPackageManager().getPackageInfo(getPackageName(),
                    0).versionName;
        } catch(NameNotFoundException e) { }
        appName = getString(R.string.app_name);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        super.onResume();
        long dbVersion = BusStopDatabase.getInstance(this).getLastDBModTime();
        Calendar date = Calendar.getInstance();
        date.setTimeInMillis(dbVersion);

        String str = getString(R.string.app_about);
        str = str.replaceAll("%appname", appName);
        str = str.replaceAll("%vername", versionName);
        str = str.replaceAll("%vernumber", "" + versionCode);
        str = str.replaceAll("%dbversion", "" + dbVersion);
        str = str.replaceAll("%dbtime", date.getTime().toLocaleString());
        str = str.replaceAll("%quote", "&quot;");
        str = str.replaceAll("%url", URL);

        TextView tv = (TextView)findViewById(R.id.textabout);
        tv.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setText(Html.fromHtml(str));
    }
}