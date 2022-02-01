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
 *
 */

package uk.org.rivernile.android.bustracker.ui.about

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import com.nhaarman.mockitokotlin2.whenever
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.app.AppRepository
import uk.org.rivernile.android.bustracker.core.app.AppVersion
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopDatabaseRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.DatabaseMetadata
import uk.org.rivernile.android.bustracker.coroutines.MainCoroutineRule
import uk.org.rivernile.android.bustracker.testutils.test
import java.util.*

/**
 * Tests for [AboutViewModel].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
@RunWith(MockitoJUnitRunner::class)
class AboutViewModelTest {

    @get:Rule
    val coroutineRule = MainCoroutineRule()
    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    private lateinit var appRepository: AppRepository
    @Mock
    private lateinit var busStopDatabaseRepository: BusStopDatabaseRepository

    @Test
    fun itemsLiveDataEmitsItemsWhenDatabaseMetadataIsUnavailable() {
        givenAppVersion()
        whenever(busStopDatabaseRepository.databaseMetadataFlow)
                .thenReturn(flowOf(null))
        val viewModel = createViewModel()
        val expected = listOf(
                UiAboutItem.TwoLinesItem.AppVersion("1.2.3", 4L),
                UiAboutItem.TwoLinesItem.Author,
                UiAboutItem.TwoLinesItem.Website,
                UiAboutItem.TwoLinesItem.Twitter,
                UiAboutItem.TwoLinesItem.DatabaseVersion(null),
                UiAboutItem.TwoLinesItem.TopologyVersion(null),
                UiAboutItem.OneLineItem.Credits,
                UiAboutItem.OneLineItem.OpenSourceLicences)

        val observer = viewModel.itemsLiveData.test()

        observer.assertValues(expected)
    }

    @Test
    fun itemsLiveDataEmitsItemsWhenDatabaseMetadataIsAvailable() {
        givenAppVersion()
        whenever(busStopDatabaseRepository.databaseMetadataFlow)
                .thenReturn(flowOf(DatabaseMetadata(123L, "abc123")))
        val viewModel = createViewModel()
        val expected1 = listOf(
                UiAboutItem.TwoLinesItem.AppVersion("1.2.3", 4L),
                UiAboutItem.TwoLinesItem.Author,
                UiAboutItem.TwoLinesItem.Website,
                UiAboutItem.TwoLinesItem.Twitter,
                UiAboutItem.TwoLinesItem.DatabaseVersion(null),
                UiAboutItem.TwoLinesItem.TopologyVersion(null),
                UiAboutItem.OneLineItem.Credits,
                UiAboutItem.OneLineItem.OpenSourceLicences)
        val expected2 = listOf(
                UiAboutItem.TwoLinesItem.AppVersion("1.2.3", 4L),
                UiAboutItem.TwoLinesItem.Author,
                UiAboutItem.TwoLinesItem.Website,
                UiAboutItem.TwoLinesItem.Twitter,
                UiAboutItem.TwoLinesItem.DatabaseVersion(Date(123L)),
                UiAboutItem.TwoLinesItem.TopologyVersion("abc123"),
                UiAboutItem.OneLineItem.Credits,
                UiAboutItem.OneLineItem.OpenSourceLicences)

        val observer = viewModel.itemsLiveData.test()

        observer.assertValues(expected1, expected2)
    }

    @Test
    fun onItemClickedWithCreditsItemShowsCredits() {
        val viewModel = createViewModel()
        val observer = viewModel.showCreditsLiveData.test()

        viewModel.onItemClicked(UiAboutItem.OneLineItem.Credits)

        observer.assertSize(1)
    }

    @Test
    fun onItemClickedWithOpenSourceLicencesItemShowsOpenSourceLicences() {
        val viewModel = createViewModel()
        val observer = viewModel.showOpenSourceLicencesLiveData.test()

        viewModel.onItemClicked(UiAboutItem.OneLineItem.OpenSourceLicences)

        observer.assertSize(1)
    }

    @Test
    fun onItemClickedWithAppVersionItemShowsStoreListing() {
        val viewModel = createViewModel()
        val observer = viewModel.showStoreListingLiveData.test()

        viewModel.onItemClicked(UiAboutItem.TwoLinesItem.AppVersion("1.2.3", 4))

        observer.assertSize(1)
    }

    @Test
    fun onItemClickedWithAuthorItemShowsAuthorWebsite() {
        val viewModel = createViewModel()
        val observer = viewModel.showAuthorWebsiteLiveData.test()

        viewModel.onItemClicked(UiAboutItem.TwoLinesItem.Author)

        observer.assertSize(1)
    }

    @Test
    fun onItemClickedWithWebsiteItemShowsAppWebsite() {
        val viewModel = createViewModel()
        val observer = viewModel.showAppWebsiteLiveData.test()

        viewModel.onItemClicked(UiAboutItem.TwoLinesItem.Website)

        observer.assertSize(1)
    }

    @Test
    fun onItemClickedWithTwitterItemShowsAppTwitter() {
        val viewModel = createViewModel()
        val observer = viewModel.showAppTwitterLiveData.test()

        viewModel.onItemClicked(UiAboutItem.TwoLinesItem.Twitter)

        observer.assertSize(1)
    }

    private fun createViewModel() =
            AboutViewModel(
                    appRepository,
                    busStopDatabaseRepository,
                    coroutineRule.testDispatcher)

    private fun givenAppVersion() {
        whenever(appRepository.appVersion)
                .thenReturn(AppVersion("1.2.3", 4L))
    }
}