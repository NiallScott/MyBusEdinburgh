/*
 * Copyright (C) 2018 - 2019 Niall 'Rivernile' Scott
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
 *
 */

package uk.org.rivernile.android.bustracker.repositories.about

import androidx.lifecycle.LiveData
import uk.org.rivernile.android.bustracker.data.platform.PlatformDataSource
import uk.org.rivernile.android.bustracker.core.utils.OpenForTesting
import uk.org.rivernile.android.bustracker.utils.Strings
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject

/**
 * Data for the application about screen is provided here.
 *
 * @param strings Platform string accessor.
 * @param platformDataSource Accessor for platform data values.
 * @param aboutLiveDataFactory A factory for creating [LiveData] instances.
 * @author Niall Scott
 */
@OpenForTesting
class AboutRepository @Inject constructor(private val strings: Strings,
                                          private val platformDataSource: PlatformDataSource,
                                          private val aboutLiveDataFactory: AboutLiveDataFactory) {

    companion object {

        /** App version item. */
        const val ITEM_ID_APP_VERSION = 0
        /** Author item. */
        const val ITEM_ID_AUTHOR = 1
        /** Website item. */
        const val ITEM_ID_WEBSITE = 2
        /** Twitter item. */
        const val ITEM_ID_TWITTER = 3
        /** Database version item. */
        const val ITEM_ID_DATABASE_VERSION = 4
        /** Topology version item. */
        const val ITEM_ID_TOPOLOGY_VERSION = 5
        /** Credits item. */
        const val ITEM_ID_CREDITS = 6
        /** Open source licences item. */
        const val ITEM_ID_OPEN_SOURCE_LICENCES = 7
    }

    /**
     * Create and return the items to be displayed on the app 'about' screen.
     *
     * @return The items to be displayed on the app 'about' screen.
     */
    fun createItems() = listOf(
            createVersionItem(),
            createAuthorItem(),
            createWebsiteItem(),
            createTwitterItem(),
            createDatabaseVersionItem(),
            createTopologyVersionItem(),
            createCreditsItem(),
            createOpenSourceLicencesItem())

    /**
     * Create a [LiveData] instance for accessing database items.
     */
    fun createDatabaseLiveData() = aboutLiveDataFactory.createDatabaseLiveData()

    /**
     * Create the version item.
     *
     * @return The version item.
     */
    private fun createVersionItem() = AboutItem(ITEM_ID_APP_VERSION,
            strings.getString(R.string.about_version), platformDataSource.getAppVersionString(),
            true)

    /**
     * Create the author item.
     *
     * @return The author item.
     */
    private fun createAuthorItem() = AboutItem(ITEM_ID_AUTHOR,
            strings.getString(R.string.about_author), strings.getString(R.string.app_author), true)

    /**
     * Create the website item.
     *
     * @return The website item.
     */
    private fun createWebsiteItem() = AboutItem(ITEM_ID_WEBSITE,
            strings.getString(R.string.about_website), strings.getString(R.string.app_website),
            true)

    /**
     * Create the Twitter item.
     *
     * @return The Twitter item.
     */
    private fun createTwitterItem() = AboutItem(ITEM_ID_TWITTER,
            strings.getString(R.string.about_twitter), strings.getString(R.string.app_twitter),
            true)

    /**
     * Create the database version item.
     *
     * @return The database version item.
     */
    private fun createDatabaseVersionItem() = AboutItem(ITEM_ID_DATABASE_VERSION,
            strings.getString(R.string.about_database_version),
            strings.getString(R.string.about_database_version_loading))

    /**
     * Create the topology version item.
     *
     * @return The topology version item.
     */
    private fun createTopologyVersionItem() = AboutItem(ITEM_ID_TOPOLOGY_VERSION,
            strings.getString(R.string.about_topology_version),
            strings.getString(R.string.about_topology_version_loading))

    /**
     * Create the credits item.
     *
     * @return The credits item.
     */
    private fun createCreditsItem() = AboutItem(ITEM_ID_CREDITS,
            strings.getString(R.string.about_credits), clickable = true)

    /**
     * Create the open source licences item.
     *
     * @return The open source licences item.
     */
    private fun createOpenSourceLicencesItem() = AboutItem(ITEM_ID_OPEN_SOURCE_LICENCES,
            strings.getString(R.string.about_open_source), clickable = true)
}