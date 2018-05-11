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
 *
 */

package uk.org.rivernile.android.bustracker.ui.about

import android.arch.core.executor.testing.InstantTaskExecutorRule
import android.arch.lifecycle.MutableLiveData
import android.arch.lifecycle.Observer
import com.nhaarman.mockito_kotlin.anyOrNull
import com.nhaarman.mockito_kotlin.argumentCaptor
import com.nhaarman.mockito_kotlin.eq
import com.nhaarman.mockito_kotlin.mock
import com.nhaarman.mockito_kotlin.times
import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.mockito.ArgumentMatchers.anyString
import uk.org.rivernile.android.bustracker.repositories.about.AboutItem
import uk.org.rivernile.android.bustracker.repositories.about.AboutRepository
import uk.org.rivernile.android.bustracker.repositories.about.DatabaseMetadata
import uk.org.rivernile.android.bustracker.utils.Strings
import uk.org.rivernile.edinburghbustracker.android.R
import java.util.Date

/**
 * Tests for [AboutViewModel].
 *
 * @author Niall Scott
 */
class AboutViewModelTest {

    @Rule
    @JvmField
    val rule = InstantTaskExecutorRule()

    private val aboutRepository: AboutRepository = mock()
    private val strings: Strings = mock()
    private val metadataLiveData = MutableLiveData<DatabaseMetadata>()

    private lateinit var aboutViewModel: AboutViewModel

    @Before
    fun setUp() {
        whenever(aboutRepository.createItems()).thenReturn(listOf(
                AboutItem(AboutRepository.ITEM_ID_DATABASE_VERSION, "Test"),
                AboutItem(AboutRepository.ITEM_ID_TOPOLOGY_VERSION, "Test 2")))
        whenever(aboutRepository.databaseLiveData)
                .thenReturn(metadataLiveData)
        aboutViewModel = AboutViewModel(aboutRepository, strings)
    }

    @Test
    fun retrievingItemsCallsRepository() {
        aboutViewModel.items

        verify(aboutRepository, times(1))
                .createItems()
    }

    @Test
    fun clickingOnAppVersionItemShowsStoreListing() {
        val observer: Observer<Void> = mock()
        aboutViewModel.showStoreListing.observeForever(observer)

        aboutViewModel.onItemClicked(AboutItem(AboutRepository.ITEM_ID_APP_VERSION, "Test"))

        verify(observer, times(1))
                .onChanged(anyOrNull())
    }

    @Test
    fun clickingOnAuthorItemShowsAuthorWebsite() {
        val observer: Observer<Void> = mock()
        aboutViewModel.showAuthorWebsite.observeForever(observer)

        aboutViewModel.onItemClicked(AboutItem(AboutRepository.ITEM_ID_AUTHOR, "Test"))

        verify(observer, times(1))
                .onChanged(anyOrNull())
    }

    @Test
    fun clickingOnWebsiteItemShowsAppWebsite() {
        val observer: Observer<Void> = mock()
        aboutViewModel.showAppWebsite.observeForever(observer)

        aboutViewModel.onItemClicked(AboutItem(AboutRepository.ITEM_ID_WEBSITE, "Test"))

        verify(observer, times(1))
                .onChanged(anyOrNull())
    }

    @Test
    fun clickingOnTwitterItemShowsAppTwitter() {
        val observer: Observer<Void> = mock()
        aboutViewModel.showAppTwitter.observeForever(observer)

        aboutViewModel.onItemClicked(AboutItem(AboutRepository.ITEM_ID_TWITTER, "Test"))

        verify(observer, times(1))
                .onChanged(anyOrNull())
    }

    @Test
    fun clickingOnCreditsItemShowsCredits() {
        val observer: Observer<Void> = mock()
        aboutViewModel.showCredits.observeForever(observer)

        aboutViewModel.onItemClicked(AboutItem(AboutRepository.ITEM_ID_CREDITS, "Test"))

        verify(observer, times(1))
                .onChanged(anyOrNull())
    }

    @Test
    fun clickingOnOpenSourceItemShowsOpenSourceLicences() {
        val observer: Observer<Void> = mock()
        aboutViewModel.showOpenSourceLicences.observeForever(observer)

        aboutViewModel.onItemClicked(
                AboutItem(AboutRepository.ITEM_ID_OPEN_SOURCE_LICENCES, "Test"))

        verify(observer, times(1))
                .onChanged(anyOrNull())
    }

    @Test
    fun databaseVersionItemWithNullDatabaseMetadata() {
        val observer: Observer<AboutItem> = mock()
        aboutViewModel.databaseVersionItem.observeForever(observer)

        metadataLiveData.value = null

        argumentCaptor<AboutItem>().apply {
            verify(observer)
                    .onChanged(capture())

            assertNull(firstValue.subtitle)
        }
    }

    @Test
    fun databaseVersionItemWithPopulatedData() {
        val observer: Observer<AboutItem> = mock()
        aboutViewModel.databaseVersionItem.observeForever(observer)
        val date = Date()
        val metadata = DatabaseMetadata(date, "abc123")
        whenever(strings.getString(eq(R.string.about_database_version_format), eq(date.time),
                anyString()))
                .thenReturn("Time ms (time human)")

        metadataLiveData.value = metadata

        argumentCaptor<AboutItem>().apply {
            verify(observer)
                    .onChanged(capture())

            assertEquals("Time ms (time human)", firstValue.subtitle)
        }
    }

    @Test
    fun topologyVersionItemWithNullDatabaseMetadata() {
        val observer: Observer<AboutItem> = mock()
        aboutViewModel.topologyVersionItem.observeForever(observer)

        metadataLiveData.value = null

        argumentCaptor<AboutItem>().apply {
            verify(observer)
                    .onChanged(capture())

            assertNull(firstValue.subtitle)
        }
    }

    @Test
    fun topologyVersionItemWithPopulatedData() {
        val observer: Observer<AboutItem> = mock()
        aboutViewModel.topologyVersionItem.observeForever(observer)
        val date = Date()
        val metadata = DatabaseMetadata(date, "abc123")

        metadataLiveData.value = metadata

        argumentCaptor<AboutItem>().apply {
            verify(observer)
                    .onChanged(capture())

            assertEquals("abc123", firstValue.subtitle)
        }
    }
}