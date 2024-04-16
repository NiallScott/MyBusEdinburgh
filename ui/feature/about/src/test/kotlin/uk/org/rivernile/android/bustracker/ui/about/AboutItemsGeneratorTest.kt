/*
 * Copyright (C) 2024 Niall 'Rivernile' Scott
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

import app.cash.turbine.test
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.app.AppRepository
import uk.org.rivernile.android.bustracker.core.app.AppVersion
import uk.org.rivernile.android.bustracker.core.database.busstop.BusStopDatabaseRepository
import uk.org.rivernile.android.bustracker.core.database.busstop.database.FakeDatabaseMetadata
import java.util.Date
import kotlin.test.Test
import kotlin.test.assertEquals

private const val EXPECTED_VERSION_NAME = "1.2.3"
private const val EXPECTED_VERSION_CODE = 4L

/**
 * Tests for [AboutItemsGenerator].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class AboutItemsGeneratorTest {

    @Mock
    private lateinit var appRepository: AppRepository
    @Mock
    private lateinit var busStopDatabaseRepository: BusStopDatabaseRepository

    private lateinit var generator: AboutItemsGenerator

    @Before
    fun setUp() {
        generator = AboutItemsGenerator(
            appRepository,
            busStopDatabaseRepository
        )
    }

    @Test
    fun createAboutItemsReturnsItemsButMissingDatabaseInfoWhenNoDatabaseInfo() {
        givenAppRepositoryReturnsAppVersion()
        val expected = createdExpectedItems()

        val result = generator.createAboutItems()

        assertEquals(expected, result)
    }

    @Test
    fun createAboutItemsReturnsItemsWithDatabaseInfoWhenDatabaseInfoIsPresent() {
        givenAppRepositoryReturnsAppVersion()
        val expected = createdExpectedItems(
            databaseUpdateTime = Date(123L),
            topologyId = "abc123"
        )

        val result = generator.createAboutItems(
            FakeDatabaseMetadata(
                updateTimestamp = 123L,
                topologyVersionId = "abc123"
            )
        )

        assertEquals(expected, result)
    }

    @Test
    fun aboutItemsFlowEmitsItemWithDatabaseInfoThenItemWithDatabaseInfo() = runTest {
        givenAppRepositoryReturnsAppVersion()
        whenever(busStopDatabaseRepository.databaseMetadataFlow)
            .thenReturn(
                flowOf(
                    FakeDatabaseMetadata(
                        updateTimestamp = 123L,
                        topologyVersionId = "abc123"
                    )
                )
            )
        val expected1 = createdExpectedItems()
        val expected2 = createdExpectedItems(
            databaseUpdateTime = Date(123L),
            topologyId = "abc123"
        )

        generator.aboutItemsFlow.test {
            assertEquals(expected1, awaitItem())
            assertEquals(expected2, awaitItem())
            awaitComplete()
        }
    }

    private fun givenAppRepositoryReturnsAppVersion() {
        whenever(appRepository.appVersion)
            .thenReturn(
                AppVersion(
                    versionName = EXPECTED_VERSION_NAME,
                    versionCode = EXPECTED_VERSION_CODE
                )
            )
    }

    private fun createdExpectedItems(
        databaseUpdateTime: Date? = null,
        topologyId: String? = null
    ) = listOf(
        UiAboutItem.TwoLinesItem.AppVersion(
            versionName = EXPECTED_VERSION_NAME,
            versionCode = EXPECTED_VERSION_CODE
        ),
        UiAboutItem.TwoLinesItem.Author,
        UiAboutItem.TwoLinesItem.Website,
        UiAboutItem.TwoLinesItem.Twitter,
        UiAboutItem.TwoLinesItem.DatabaseVersion(databaseUpdateTime),
        UiAboutItem.TwoLinesItem.TopologyVersion(topologyId),
        UiAboutItem.OneLineItem.Credits,
        UiAboutItem.OneLineItem.PrivacyPolicy,
        UiAboutItem.OneLineItem.OpenSourceLicences
    )
}