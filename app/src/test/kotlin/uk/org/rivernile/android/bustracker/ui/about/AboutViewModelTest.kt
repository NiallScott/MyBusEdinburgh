/*
 * Copyright (C) 2018 - 2020 Niall 'Rivernile' Scott
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
import androidx.lifecycle.Observer
import com.nhaarman.mockitokotlin2.anyOrNull
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.eq
import com.nhaarman.mockitokotlin2.spy
import com.nhaarman.mockitokotlin2.times
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.repositories.about.AboutItem
import uk.org.rivernile.android.bustracker.repositories.about.AboutRepository
import uk.org.rivernile.android.bustracker.repositories.about.DatabaseMetadata
import uk.org.rivernile.android.bustracker.utils.Strings
import uk.org.rivernile.android.utils.TestableClearableLiveData
import uk.org.rivernile.edinburghbustracker.android.R
import java.util.Date

/**
 * Tests for [AboutViewModel].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AboutViewModelTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @Mock
    lateinit var aboutRepository: AboutRepository
    @Mock
    lateinit var strings: Strings

    @Mock
    lateinit var voidObserver: Observer<Void>
    @Mock
    lateinit var aboutItemObserver: Observer<AboutItem>

    private val metadataLiveData = spy(TestableClearableLiveData<DatabaseMetadata>())

    private lateinit var aboutViewModel: AboutViewModel

    @Before
    fun setUp() {
        whenever(aboutRepository.createItems()).thenReturn(listOf(
                AboutItem(AboutRepository.ITEM_ID_DATABASE_VERSION, "Test"),
                AboutItem(AboutRepository.ITEM_ID_TOPOLOGY_VERSION, "Test 2")))
        whenever(aboutRepository.createDatabaseLiveData())
                .thenReturn(metadataLiveData)
        aboutViewModel = AboutViewModel(aboutRepository, strings)
    }

    @Test
    fun retrievesAboutDatabaseLiveDataOnCreate() {
        verify(aboutRepository)
                .createDatabaseLiveData()
    }

    @Test
    fun retrievingItemsCallsRepository() {
        aboutViewModel.items

        verify(aboutRepository, times(1))
                .createItems()
    }

    @Test
    fun clickingOnAppVersionItemShowsStoreListing() {
        aboutViewModel.showStoreListing.observeForever(voidObserver)

        aboutViewModel.onItemClicked(AboutItem(AboutRepository.ITEM_ID_APP_VERSION, "Test"))

        verify(voidObserver, times(1))
                .onChanged(anyOrNull())
    }

    @Test
    fun clickingOnAuthorItemShowsAuthorWebsite() {
        aboutViewModel.showAuthorWebsite.observeForever(voidObserver)

        aboutViewModel.onItemClicked(AboutItem(AboutRepository.ITEM_ID_AUTHOR, "Test"))

        verify(voidObserver, times(1))
                .onChanged(anyOrNull())
    }

    @Test
    fun clickingOnWebsiteItemShowsAppWebsite() {
        aboutViewModel.showAppWebsite.observeForever(voidObserver)

        aboutViewModel.onItemClicked(AboutItem(AboutRepository.ITEM_ID_WEBSITE, "Test"))

        verify(voidObserver, times(1))
                .onChanged(anyOrNull())
    }

    @Test
    fun clickingOnTwitterItemShowsAppTwitter() {
        aboutViewModel.showAppTwitter.observeForever(voidObserver)

        aboutViewModel.onItemClicked(AboutItem(AboutRepository.ITEM_ID_TWITTER, "Test"))

        verify(voidObserver, times(1))
                .onChanged(anyOrNull())
    }

    @Test
    fun clickingOnCreditsItemShowsCredits() {
        aboutViewModel.showCredits.observeForever(voidObserver)

        aboutViewModel.onItemClicked(AboutItem(AboutRepository.ITEM_ID_CREDITS, "Test"))

        verify(voidObserver, times(1))
                .onChanged(anyOrNull())
    }

    @Test
    fun clickingOnOpenSourceItemShowsOpenSourceLicences() {
        aboutViewModel.showOpenSourceLicences.observeForever(voidObserver)

        aboutViewModel.onItemClicked(
                AboutItem(AboutRepository.ITEM_ID_OPEN_SOURCE_LICENCES, "Test"))

        verify(voidObserver, times(1))
                .onChanged(anyOrNull())
    }

    @Test
    fun databaseVersionItemWithNullDatabaseMetadata() {
        aboutViewModel.databaseVersionItem.observeForever(aboutItemObserver)

        metadataLiveData.value = null

        argumentCaptor<AboutItem>().apply {
            verify(aboutItemObserver)
                    .onChanged(capture())

            assertNull(firstValue.subtitle)
        }
    }

    @Test
    fun databaseVersionItemWithPopulatedData() {
        aboutViewModel.databaseVersionItem.observeForever(aboutItemObserver)
        val date = Date()
        val metadata = DatabaseMetadata(date, "abc123")
        whenever(strings.getString(eq(R.string.about_database_version_format), eq(date.time),
                anyString()))
                .thenReturn("Time ms (time human)")

        metadataLiveData.value = metadata

        argumentCaptor<AboutItem>().apply {
            verify(aboutItemObserver)
                    .onChanged(capture())

            assertEquals("Time ms (time human)", firstValue.subtitle)
        }
    }

    @Test
    fun topologyVersionItemWithNullDatabaseMetadata() {
        aboutViewModel.topologyVersionItem.observeForever(aboutItemObserver)

        metadataLiveData.value = null

        argumentCaptor<AboutItem>().apply {
            verify(aboutItemObserver)
                    .onChanged(capture())

            assertNull(firstValue.subtitle)
        }
    }

    @Test
    fun topologyVersionItemWithPopulatedData() {
        aboutViewModel.topologyVersionItem.observeForever(aboutItemObserver)
        val date = Date()
        val metadata = DatabaseMetadata(date, "abc123")

        metadataLiveData.value = metadata

        argumentCaptor<AboutItem>().apply {
            verify(aboutItemObserver)
                    .onChanged(capture())

            assertEquals("abc123", firstValue.subtitle)
        }
    }

    @Test
    fun onClearedCallsOnClearedOnLiveData() {
        callOnCleared()

        verify(metadataLiveData)
                .onCleared()
    }

    private fun callOnCleared() {
        // This is required because Kotlin can't access the protected method.
        val method = aboutViewModel.javaClass.getDeclaredMethod("onCleared")
        method.isAccessible = true
        method.invoke(aboutViewModel)
    }
}