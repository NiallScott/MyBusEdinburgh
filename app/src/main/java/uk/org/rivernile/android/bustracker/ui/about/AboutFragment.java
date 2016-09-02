/*
 * Copyright (C) 2015 - 2016 Niall 'Rivernile' Scott
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
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import uk.org.rivernile.android.bustracker.database.busstop.BusStopContract;
import uk.org.rivernile.android.bustracker.database.busstop.loaders.DatabaseInformationLoader;
import uk.org.rivernile.android.utils.DividerItemDecoration;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This {@link Fragment} shows application 'about' information to the user.
 *
 * @author Niall Scott
 */
public class AboutFragment extends Fragment
        implements LoaderManager.LoaderCallbacks<Cursor>,
        AboutAdapter.OnItemClickedListener {

    private static final DateFormat DATE_FORMAT = DateFormat.getDateTimeInstance();

    private static final int LOADER_BUS_STOP_DATABASE_INFO = 1;

    private AboutAdapter adapter;
    private Callbacks callbacks;

    private AboutItem itemDatabaseVersion;
    private AboutItem itemTopologyVersion;

    @Override
    public void onAttach(final Context context) {
        super.onAttach(context);

        try {
            callbacks = (Callbacks) context;
        } catch (ClassCastException e) {
            throw new IllegalStateException(context.getClass().getName() + " does not implement "
                    + Callbacks.class.getName());
        }
    }

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        adapter = new AboutAdapter(getContext());
        adapter.setOnItemClickedListener(this);
        adapter.setAboutItems(createItems());
    }

    @Override
    public View onCreateView(final LayoutInflater inflater, @Nullable final ViewGroup container,
                             @Nullable final Bundle savedInstanceState) {
        final View v = inflater.inflate(R.layout.about_fragment, container, false);
        final RecyclerView recyclerView = (RecyclerView) v.findViewById(android.R.id.list);

        recyclerView.setHasFixedSize(true);
        recyclerView.addItemDecoration(new DividerItemDecoration(getContext(),
                DividerItemDecoration.VERTICAL_LIST));
        recyclerView.setAdapter(adapter);

        return v;
    }

    @Override
    public void onActivityCreated(@Nullable final Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        getLoaderManager().initLoader(LOADER_BUS_STOP_DATABASE_INFO, null, this);
    }

    @Override
    public Loader<Cursor> onCreateLoader(final int id, final Bundle args) {
        switch (id) {
            case LOADER_BUS_STOP_DATABASE_INFO:
                return new DatabaseInformationLoader(getContext());
            default:
                return null;
        }
    }

    @Override
    public void onLoadFinished(final Loader<Cursor> loader, final Cursor data) {
        switch (loader.getId()) {
            case LOADER_BUS_STOP_DATABASE_INFO:
                handleBusStopDatabaseInformationLoaded(data);
                break;
            default:
                break;
        }
    }

    @Override
    public void onLoaderReset(final Loader<Cursor> loader) {
        // Nothing to do here.
    }

    @Override
    public void onItemClicked(@NonNull final AboutItem item) {
        item.doAction(getActivity(), callbacks);
    }

    /**
     * Handle the loading of the bus stop database information {@link Cursor}.
     *
     * @param cursor The {@link Cursor} containing the bus stop database information.
     */
    private void handleBusStopDatabaseInformationLoaded(@Nullable final Cursor cursor) {
        if (cursor != null && cursor.moveToFirst()) {
            final Date version = new Date(cursor.getLong(cursor.getColumnIndex(
                    BusStopContract.DatabaseInformation.LAST_UPDATE_TIMESTAMP)));
            itemDatabaseVersion.setSubTitle(getString(R.string.about_database_version_format,
                    version.getTime(), DATE_FORMAT.format(version)));
            itemTopologyVersion.setSubTitle(cursor.getString(
                    cursor.getColumnIndex(
                            BusStopContract.DatabaseInformation.CURRENT_TOPOLOGY_ID)));
        } else {
            itemDatabaseVersion.setSubTitle(getString(R.string.about_database_version_error));
            itemTopologyVersion.setSubTitle(getString(R.string.about_topology_version_error));
        }

        adapter.rebindItem(itemDatabaseVersion);
        adapter.rebindItem(itemTopologyVersion);
    }

    /**
     * Create the items to be shown in the 'about' {@link RecyclerView}.
     *
     * @return The items to be shown in the 'about' {@link RecyclerView}.
     */
    private List<AboutItem> createItems() {
        // These items are stored in member variables in this class as they are updated later.
        itemDatabaseVersion = createDatabaseVersionItem();
        itemTopologyVersion = createTopologyVersionItem();

        final ArrayList<AboutItem> items = new ArrayList<>(8);
        items.add(createVersionItem());
        items.add(createAuthorItem());
        items.add(createWebsiteItem());
        items.add(createTwitterItem());
        items.add(itemDatabaseVersion);
        items.add(itemTopologyVersion);
        items.add(createCreditsItem());
        items.add(createOpenSourceLicencesItem());

        return items;
    }

    /**
     * Create the version item.
     *
     * @return The version item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem createVersionItem() {
        return new AboutItem(getString(R.string.about_version), getVersionString(), true) {
            @Override
            void doAction(@NonNull final Activity activity,
                    @NonNull final Callbacks callbacks) {
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
     * Create the author item.
     *
     * @return The author item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem createAuthorItem() {
        return new AboutItem(getString(R.string.about_author),
                getString(R.string.app_author), true) {
            @Override
            void doAction(@NonNull final Activity activity,
                    @NonNull final Callbacks callbacks) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.app_author_website)));

                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Fail silently.
                }
            }
        };
    }

    /**
     * Create the website item.
     *
     * @return The website item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem createWebsiteItem() {
        return new AboutItem(getString(R.string.about_website),
                getString(R.string.app_website), true) {
            @Override
            void doAction(@NonNull final Activity activity,
                    @NonNull final Callbacks callbacks) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.app_website)));

                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Fail silently.
                }
            }
        };
    }

    /**
     * Create the Twitter item.
     *
     * @return The Twitter item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem createTwitterItem() {
        return new AboutItem(getString(R.string.about_twitter),
                getString(R.string.app_twitter), true) {
            @Override
            void doAction(@NonNull final Activity activity,
                    @NonNull final Callbacks callbacks) {
                final Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(getString(R.string.app_twitter)));

                try {
                    activity.startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    // Fail silently.
                }
            }
        };
    }

    /**
     * Create the database version item.
     *
     * @return The database version item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem createDatabaseVersionItem() {
        return new AboutItem(getString(R.string.about_database_version),
                getString(R.string.about_database_version_loading), false);
    }

    /**
     * Create the topology version item.
     *
     * @return The topology version item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem createTopologyVersionItem() {
        return new AboutItem(getString(R.string.about_topology_version),
                getString(R.string.about_topology_version_loading), false);
    }

    /**
     * Create the credits item.
     *
     * @return The credits item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem createCreditsItem() {
        return new AboutItem(getString(R.string.about_credits), null, true) {
            @Override
            void doAction(@NonNull final Activity activity,
                    @NonNull final Callbacks callbacks) {
                callbacks.onShowCredits();
            }
        };
    }

    /**
     * Create the open source licences item.
     *
     * @return The open source licences item. Will not be {@code null}.
     */
    @NonNull
    private AboutItem createOpenSourceLicencesItem() {
        return new AboutItem(getString(R.string.about_open_source), null, true) {
            @Override
            void doAction(@NonNull final Activity activity,
                    @NonNull final Callbacks callbacks) {
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
            return getString(R.string.about_version_format, info.versionName, info.versionCode);
        } catch (PackageManager.NameNotFoundException e) {
            // This should never happen.
            return "";
        }
    }

    /**
     * Any {@link Activity Activities} which host this {@link Fragment} must implement this
     * interface to handle navigation events.
     */
    public interface Callbacks {

        /**
         * This is called when the user wants to see credits.
         */
        void onShowCredits();

        /**
         * This is called when the user wants to see the open source licences.
         */
        void onShowLicences();
    }
}
