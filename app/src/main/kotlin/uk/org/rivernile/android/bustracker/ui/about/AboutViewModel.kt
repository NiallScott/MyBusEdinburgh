/*
 * Copyright (C) 2018 - 2022 Niall 'Rivernile' Scott
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

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.onStart
import uk.org.rivernile.android.bustracker.core.app.AppRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopDatabaseRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.DatabaseMetadata
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.utils.SingleLiveEvent
import java.util.*
import javax.inject.Inject

/**
 * This is the [ViewModel] for the 'about' screen.
 *
 * @param appRepository Used to retrieve data relating to app version.
 * @param busStopDatabaseRepository Used to access metadata for the bus stop database.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
@HiltViewModel
class AboutViewModel @Inject constructor(
        private val appRepository: AppRepository,
        private val busStopDatabaseRepository: BusStopDatabaseRepository,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher): ViewModel() {

    /**
     * This [LiveData] emits the 'about' items to be displayed.
     */
    @OptIn(ExperimentalCoroutinesApi::class)
    val itemsLiveData = databaseMetadataFlow
            .mapLatest(this::createAboutItems)
            .asLiveData(viewModelScope.coroutineContext + defaultDispatcher)

    private val databaseMetadataFlow get() = busStopDatabaseRepository
            .databaseMetadataFlow
            .onStart { emit(null) }
            .distinctUntilChanged()

    private val appVersion by lazy {
        appRepository.appVersion
    }

    /**
     * This [LiveData] is invoked when the store listing should be shown.
     */
    val showStoreListingLiveData: LiveData<Unit> get() = showStoreListing
    private val showStoreListing = SingleLiveEvent<Unit>()

    /**
     * This [LiveData] is invoked when the author website should be shown.
     */
    val showAuthorWebsiteLiveData: LiveData<Unit> get() = showAuthorWebsite
    private val showAuthorWebsite = SingleLiveEvent<Unit>()

    /**
     * This [LiveData] is invoked when the app website should be shown.
     */
    val showAppWebsiteLiveData: LiveData<Unit> get() = showAppWebsite
    private val showAppWebsite = SingleLiveEvent<Unit>()

    /**
     * This [LiveData] is invoked when the app Twitter feed should be shown.
     */
    val showAppTwitterLiveData: LiveData<Unit> get() = showAppTwitter
    private val showAppTwitter = SingleLiveEvent<Unit>()

    /**
     * This [LiveData] is invoked when the app credits should be shown.
     */
    val showCreditsLiveData: LiveData<Unit> get() = showCredits
    private val showCredits = SingleLiveEvent<Unit>()

    /**
     * This [LiveData] is invoked when the open source licences should be shown.
     */
    val showOpenSourceLicencesLiveData: LiveData<Unit> get() = showOpenSourceLicences
    private val showOpenSourceLicences = SingleLiveEvent<Unit>()

    /**
     * This is called when an [UiAboutItem] has been clicked in the list.
     *
     * @param item The clicked [UiAboutItem].
     */
    fun onItemClicked(item: UiAboutItem) {
        when (item) {
            is UiAboutItem.OneLineItem.Credits -> showCredits.call()
            is UiAboutItem.OneLineItem.OpenSourceLicences -> showOpenSourceLicences.call()
            is UiAboutItem.TwoLinesItem.AppVersion -> showStoreListing.call()
            is UiAboutItem.TwoLinesItem.Author -> showAuthorWebsite.call()
            is UiAboutItem.TwoLinesItem.Website -> showAppWebsite.call()
            is UiAboutItem.TwoLinesItem.Twitter -> showAppTwitter.call()
            else -> {
                // Don't do anything for the other items.
            }
        }
    }

    /**
     * Create the [List] of [UiAboutItem]s.
     *
     * @param databaseMetadata The database metadata.
     * @return The [List] of [UiAboutItem]s.
     */
    private fun createAboutItems(databaseMetadata: DatabaseMetadata?) =
            listOf(
                    UiAboutItem.TwoLinesItem.AppVersion(
                            appVersion.versionName,
                            appVersion.versionCode),
                    UiAboutItem.TwoLinesItem.Author,
                    UiAboutItem.TwoLinesItem.Website,
                    UiAboutItem.TwoLinesItem.Twitter,
                    UiAboutItem.TwoLinesItem.DatabaseVersion(
                            databaseMetadata?.databaseVersion?.let(::Date)),
                    UiAboutItem.TwoLinesItem.TopologyVersion(
                            databaseMetadata?.topologyVersion),
                    UiAboutItem.OneLineItem.Credits,
                    UiAboutItem.OneLineItem.OpenSourceLicences)
}