/*
 * Copyright (C) 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.about

import android.arch.lifecycle.LiveData
import android.arch.lifecycle.Transformations
import android.arch.lifecycle.ViewModel
import uk.org.rivernile.android.bustracker.repositories.about.AboutItem
import uk.org.rivernile.android.bustracker.repositories.about.AboutRepository
import uk.org.rivernile.android.bustracker.repositories.about.DatabaseMetadata
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import uk.org.rivernile.android.bustracker.utils.Strings
import uk.org.rivernile.edinburghbustracker.android.R
import java.text.DateFormat
import javax.inject.Inject

/**
 * This is the [ViewModel] for the 'about' screen.
 *
 * @param aboutRepository The [AboutRepository] to access data for the 'about' screen.
 * @property strings Platform [String] accessor.
 * @author Niall Scott
 */
class AboutViewModel @Inject constructor(aboutRepository: AboutRepository,
                                         private val strings: Strings): ViewModel() {

    /** The [List] of items to display. */
    val items = aboutRepository.createItems()
    /** This [LiveData] is invoked when the store listing should be shown. */
    val showStoreListing: LiveData<Void>
        get() = _showStoreListing
    /** This [LiveData] is invoked when the author website should be shown. */
    val showAuthorWebsite: LiveData<Void>
        get() = _showAuthorWebsite
    /** This [LiveData] is invoked when the app website should be shown. */
    val showAppWebsite: LiveData<Void>
        get() = _showAppWebsite
    /** This [LiveData] is invoked when the app Twitter feed should be shown. */
    val showAppTwitter: LiveData<Void>
        get() = _showAppTwitter
    /** This [LiveData] is invoked when the app credits should be shown. */
    val showCredits: LiveData<Void>
        get() = _showCredits
    /** This [LiveData] is invoked when the open source licences should be shown. */
    val showOpenSourceLicences: LiveData<Void>
        get() = _showOpenSourceLicences

    private val databaseData = aboutRepository.databaseLiveData
    /** This [LiveData] changes when the database version item is updated. */
    val databaseVersionItem: LiveData<AboutItem> =
            Transformations.map(databaseData, this::processDatabaseVersion)
    /** This [LiveData] changes when the topology version item is updated. */
    val topologyVersionItem: LiveData<AboutItem> =
            Transformations.map(databaseData, this::processTopologyVersion)

    private val _showStoreListing = SingleLiveEvent<Void>()
    private val _showAuthorWebsite = SingleLiveEvent<Void>()
    private val _showAppWebsite = SingleLiveEvent<Void>()
    private val _showAppTwitter = SingleLiveEvent<Void>()
    private val _showCredits = SingleLiveEvent<Void>()
    private val _showOpenSourceLicences = SingleLiveEvent<Void>()

    private val dateFormat = DateFormat.getDateTimeInstance()

    /**
     * This is called when an [AboutItem] has been clicked in the list.
     *
     * @param item The clicked [AboutItem].
     */
    fun onItemClicked(item: AboutItem) {
        when (item.id) {
            AboutRepository.ITEM_ID_APP_VERSION -> _showStoreListing.call()
            AboutRepository.ITEM_ID_AUTHOR -> _showAuthorWebsite.call()
            AboutRepository.ITEM_ID_WEBSITE -> _showAppWebsite.call()
            AboutRepository.ITEM_ID_TWITTER -> _showAppTwitter.call()
            AboutRepository.ITEM_ID_CREDITS -> _showCredits.call()
            AboutRepository.ITEM_ID_OPEN_SOURCE_LICENCES -> _showOpenSourceLicences.call()
        }
    }

    /**
     * Process a [DatabaseMetadata] item and turn it in to a user displayable [String] inside the
     * [AboutItem] for the database version.
     *
     * @param metadata The loaded [DatabaseMetadata].
     * @return A new version of the database version [AboutItem].
     */
    private fun processDatabaseVersion(metadata: DatabaseMetadata?): AboutItem {
        return items.first {
            item -> item.id == AboutRepository.ITEM_ID_DATABASE_VERSION
        }.apply {
            val version = metadata?.databaseVersion

            subtitle = if (version != null) {
                strings.getString(R.string.about_database_version_format, version.time,
                        dateFormat.format(version))
            } else {
                null
            }
        }
    }

    /**
     * Process a [DatabaseMetadata] item and turn it in to a user displayable [String] inside the
     * [AboutItem] for the topology version.
     *
     * @param metadata The loaded [DatabaseMetadata].
     * @return A new version of the topology version [AboutItem].
     */
    private fun processTopologyVersion(metadata: DatabaseMetadata?): AboutItem {
        return items.first {
            item -> item.id == AboutRepository.ITEM_ID_TOPOLOGY_VERSION
        }.apply {
            subtitle = metadata?.topologyVersion
        }
    }
}