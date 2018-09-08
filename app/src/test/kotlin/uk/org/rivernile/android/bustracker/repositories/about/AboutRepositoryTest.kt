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

package uk.org.rivernile.android.bustracker.repositories.about

import com.nhaarman.mockito_kotlin.verify
import com.nhaarman.mockito_kotlin.whenever
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.data.platform.PlatformDataSource
import uk.org.rivernile.android.bustracker.utils.Strings
import uk.org.rivernile.edinburghbustracker.android.R

/**
 * Tests for [AboutRepository].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AboutRepositoryTest {

    @Mock
    lateinit var strings: Strings
    @Mock
    lateinit var platformDataSource: PlatformDataSource
    @Mock
    lateinit var aboutLiveDataFactory: AboutLiveDataFactory

    private lateinit var aboutRepository: AboutRepository

    @Before
    fun setUp() {
        aboutRepository = AboutRepository(strings, platformDataSource, aboutLiveDataFactory)
    }

    @Test
    fun creatingDatabaseLiveDataCallsFavtory() {
        aboutRepository.createDatabaseLiveData()

        verify(aboutLiveDataFactory)
                .createDatabaseLiveData()
    }

    @Test
    fun returnsCorrectDefaultData() {
        givenDefaultDataIsSetUp()

        val items = aboutRepository.createItems()

        assertEquals(createExpectedList(), items)
    }

    private fun givenDefaultDataIsSetUp() {
        whenever(strings.getString(R.string.about_version))
                .thenReturn("Application Version")
        whenever(strings.getString(R.string.about_author))
                .thenReturn("Author")
        whenever(strings.getString(R.string.about_website))
                .thenReturn("Website")
        whenever(strings.getString(R.string.about_twitter))
                .thenReturn("Twitter")
        whenever(strings.getString(R.string.about_database_version))
                .thenReturn("Database version")
        whenever(strings.getString(R.string.about_topology_version))
                .thenReturn("Topology version")
        whenever(strings.getString(R.string.about_credits))
                .thenReturn("Credits")
        whenever(strings.getString(R.string.about_open_source))
                .thenReturn("Open source licences")

        whenever(platformDataSource.getAppVersionString())
                .thenReturn("1.0.0 (#1)")
        whenever(strings.getString(R.string.app_author))
                .thenReturn("Niall Scott")
        whenever(strings.getString(R.string.app_website))
                .thenReturn("http://www.rivernile.org.uk/bustracker/")
        whenever(strings.getString(R.string.app_twitter))
                .thenReturn("https://twitter.com/MyBusEdinburgh")
        whenever(strings.getString(R.string.about_database_version_loading))
                .thenReturn("DB Loading...")
        whenever(strings.getString(R.string.about_topology_version_loading))
                .thenReturn("Topology loading...")
    }

    private fun createExpectedList() = listOf(
            AboutItem(0, "Application Version", "1.0.0 (#1)", true),
            AboutItem(1, "Author", "Niall Scott", true),
            AboutItem(2, "Website", "http://www.rivernile.org.uk/bustracker/", true),
            AboutItem(3, "Twitter", "https://twitter.com/MyBusEdinburgh", true),
            AboutItem(4, "Database version", "DB Loading...", false),
            AboutItem(5, "Topology version", "Topology loading...", false),
            AboutItem(6, "Credits", null, true),
            AboutItem(7, "Open source licences", null, true)
    )
}