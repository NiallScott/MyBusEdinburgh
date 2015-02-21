/*
 * Copyright (C) 2015 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.about;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.support.annotation.NonNull;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import uk.org.rivernile.android.bustracker.BusApplication;
import uk.org.rivernile.android.utils.SimpleResultLoader;
import uk.org.rivernile.edinburghbustracker.android.BusStopDatabase;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link android.support.v4.content.Loader} loads the items to display in the 'about' list.
 * This is required because of database accessing, which must not block the main thread.
 *
 * @author Niall Scott
 */
class AboutItemLoader extends SimpleResultLoader<List<AboutItem>> {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();

    private final BusStopDatabase bsd;

    /**
     * Create a new {@code AboutItemLoader}.
     *
     * @param context A {@link Context} instance. Must not be {@code null}.
     */
    AboutItemLoader(@NonNull final Context context) {
        super(context);

        bsd = ((BusApplication) context.getApplicationContext()).getBusStopDatabase();
    }

    @Override
    public List<AboutItem> loadInBackground() {
        final ArrayList<AboutItem> items = new ArrayList<AboutItem>(8);
        final Context context = getContext();

        items.add(getVersionItem(context));
        items.add(getAuthorItem(context));
        items.add(getWebsiteItem(context));
        items.add(getTwitterItem(context));
        items.add(getDatabaseVersionItem(context));
        items.add(getTopologyVersionItem(context));
        items.add(getCreditsItem(context));
        items.add(getOpenSourceLicencesItem(context));

        return items;
    }

    /**
     * Get the version item.
     *
     * @param context A {@link Context} instance. Must not be {@code null}.
     * @return The version item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem getVersionItem(@NonNull final Context context) {
        return new AboutItem(context.getString(R.string.about_version), getVersionString(), true) {
            @Override
            void doAction(@NonNull final Activity activity,
                          @NonNull final AboutFragment.Callbacks callbacks) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse("market://details?id=" + activity.getPackageName()));

                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Fail silently.
                }
            }
        };
    }

    /**
     * Get the author item.
     *
     * @param context A {@link Context} instance. Must not be {@code null}.
     * @return The author item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem getAuthorItem(@NonNull final Context context) {
        return new AboutItem(context.getString(R.string.about_author),
                context.getString(R.string.app_author), true) {
            @Override
            void doAction(@NonNull final Activity activity,
                          @NonNull final AboutFragment.Callbacks callbacks) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(context.getString(R.string.app_author_website)));

                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Fail silently.
                }
            }
        };
    }

    /**
     * Get the website item.
     *
     * @param context A {@link Context} instance. Must not be {@code null}.
     * @return The website item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem getWebsiteItem(@NonNull final Context context) {
        return new AboutItem(context.getString(R.string.about_website),
                context.getString(R.string.app_website), true) {
            @Override
            void doAction(@NonNull final Activity activity,
                          @NonNull final AboutFragment.Callbacks callbacks) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(context.getString(R.string.app_website)));

                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Fail silently.
                }
            }
        };
    }

    /**
     * Get the Twitter item.
     *
     * @param context A {@link Context} instance. Must not be {@code null}.
     * @return The Twitter item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem getTwitterItem(@NonNull final Context context) {
        return new AboutItem(context.getString(R.string.about_twitter),
                context.getString(R.string.app_twitter), true) {
            @Override
            void doAction(@NonNull final Activity activity,
                          @NonNull final AboutFragment.Callbacks callbacks) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(context.getString(R.string.app_twitter)));

                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Fail silently.
                }
            }
        };
    }

    /**
     * Get the database version item.
     *
     * @param context A {@link Context} instance. Must not be {@code null}.
     * @return The database version item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem getDatabaseVersionItem(@NonNull final Context context) {
        final Calendar dbVersion = getDatabaseVersion();
        return new AboutItem(context.getString(R.string.about_database_version),
                context.getString(R.string.about_database_version_format,
                        dbVersion.getTimeInMillis(), DATE_FORMAT.format(dbVersion.getTime())),
                false);
    }

    /**
     * Get the topology version item.
     *
     * @param context A {@link Context} instance. Must not be {@code null}.
     * @return The topology version item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem getTopologyVersionItem(@NonNull final Context context) {
        return new AboutItem(context.getString(R.string.about_topology_version), bsd.getTopoId(),
                false);
    }

    /**
     * Get the credits item.
     *
     * @param context A {@link Context} instance. Must not be {@code null}.
     * @return The credits item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem getCreditsItem(@NonNull final Context context) {
        return new AboutItem(context.getString(R.string.about_credits), null, true) {
            @Override
            void doAction(@NonNull final Activity activity,
                          @NonNull final AboutFragment.Callbacks callbacks) {
                callbacks.onShowCredits();
            }
        };
    }

    /**
     * Get the open source licences item.
     *
     * @param context A {@link Context} instance. Must not be {@code null}.
     * @return The open source licences item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem getOpenSourceLicencesItem(@NonNull final Context context) {
        return new AboutItem(context.getString(R.string.about_open_source), null, true) {
            @Override
            void doAction(@NonNull final Activity activity,
                          @NonNull final AboutFragment.Callbacks callbacks) {
                callbacks.onShowLicences();
            }
        };
    }

    /**
     * Get the version {@link String}.
     *
     * @return The version {@link String}. Will not be {@code null}.
     */
    @NonNull
    private String getVersionString() {
        try {
            final Context context = getContext();
            final PackageInfo info = context.getPackageManager()
                    .getPackageInfo(context.getPackageName(), 0);
            return context.getString(R.string.about_version_format, info.versionName,
                    info.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            // This should never happen.
            return "";
        }
    }

    /**
     * Get the database version (which is a UNIX timestamp) as a {@link Calendar} instance.
     *
     * @return The database version as a {@link Calendar} instance.
     */
    @NonNull
    private Calendar getDatabaseVersion() {
        long dbVersion;

        try {
            dbVersion = bsd.getLastDBModTime();
        } catch (SQLiteException e) {
            dbVersion = 0L;
        }

        final Calendar date = Calendar.getInstance();
        date.setTimeInMillis(dbVersion);

        return date;
    }
}
