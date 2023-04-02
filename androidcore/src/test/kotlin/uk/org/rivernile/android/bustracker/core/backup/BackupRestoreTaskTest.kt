/*
 * Copyright (C) 2020 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.backup

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import uk.org.rivernile.android.bustracker.core.database.settings.SettingsDatabaseRepository
import uk.org.rivernile.android.bustracker.core.database.settings.daos.FavouritesDao
import uk.org.rivernile.android.bustracker.core.database.settings.entities.FavouriteStop
import uk.org.rivernile.android.bustracker.core.utils.TimeUtils
import java.io.File

/**
 * Tests for [BackupRestoreTask].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class BackupRestoreTaskTest {

    companion object {

        private const val MOCK_DATABASE_VERSION = 3
        private const val MOCK_TIME = 123L
    }

    @Mock
    private lateinit var favouritesDao: FavouritesDao
    @Mock
    private lateinit var settingsDatabaseRepository: SettingsDatabaseRepository
    @Mock
    private lateinit var backupPersistence: BackupPersistence
    @Mock
    private lateinit var timeUtils: TimeUtils
    private val mockFile = File("mock.file")

    private lateinit var task: BackupRestoreTask

    @Before
    fun setUp() {
        task = BackupRestoreTask(favouritesDao, settingsDatabaseRepository, backupPersistence,
                timeUtils)
    }

    @Test
    fun serialiseFavouriteStopsWithNoFavouritesHasNullFavourites() {
        givenNoFavourites()
        givenSettingsDatabaseVersionIsReturned()
        givenTimestampIsReturned()
        val expected = Backup(MOCK_DATABASE_VERSION, 1, MOCK_TIME, null)

        task.serialiseFavouriteStops(mockFile)

        verify(backupPersistence)
                .persistBackup(mockFile, expected)
    }

    @Test
    fun serialiseFavouriteStopsWithSingleFavourite() {
        val favouriteStop = FavouriteStop(0, "123456", "Name")
        val favourites = listOf(favouriteStop)
        whenever(favouritesDao.getAllFavouriteStops())
                .thenReturn(favourites)
        givenSettingsDatabaseVersionIsReturned()
        givenTimestampIsReturned()
        val expectedFavourite = BackupFavouriteStop("123456", "Name")
        val expectedFavourites = listOf(expectedFavourite)
        val expected = Backup(MOCK_DATABASE_VERSION, 1, MOCK_TIME, expectedFavourites)

        task.serialiseFavouriteStops(mockFile)

        verify(backupPersistence)
                .persistBackup(mockFile, expected)
    }

    @Test
    fun serialiseFavouriteStopsWithMultipleFavourites() {
        val favouriteStop1 = FavouriteStop(0, "100001", "Name 1")
        val favouriteStop2 = FavouriteStop(1, "100002", "Name 2")
        val favouriteStop3 = FavouriteStop(2, "100003", "Name 3")
        val favourites = listOf(favouriteStop1, favouriteStop2, favouriteStop3)
        whenever(favouritesDao.getAllFavouriteStops())
                .thenReturn(favourites)
        givenSettingsDatabaseVersionIsReturned()
        givenTimestampIsReturned()
        val expectedFavourite1 = BackupFavouriteStop("100001", "Name 1")
        val expectedFavourite2 = BackupFavouriteStop("100002", "Name 2")
        val expectedFavourite3 = BackupFavouriteStop("100003", "Name 3")
        val expectedFavourites = listOf(expectedFavourite1, expectedFavourite2, expectedFavourite3)
        val expected = Backup(MOCK_DATABASE_VERSION, 1, MOCK_TIME, expectedFavourites)

        task.serialiseFavouriteStops(mockFile)

        verify(backupPersistence)
                .persistBackup(mockFile, expected)
    }

    @Test
    fun restoreFavouriteStopsWithNullBackupDoesntRestoreFavouriteStops() {
        whenever(backupPersistence.readBackup(mockFile))
                .thenReturn(null)

        task.restoreFavouriteStops(mockFile)

        verify(favouritesDao, never())
                .removeAllFavouriteStops()
        verify(favouritesDao, never())
                .addFavouriteStops(any())
    }

    @Test
    fun restoreFavouriteStopsWithNullFavouritesDoesntRestoreFavouriteStops() {
        val backup = Backup(MOCK_DATABASE_VERSION, 1, MOCK_TIME, null)
        whenever(backupPersistence.readBackup(mockFile))
                .thenReturn(backup)

        task.restoreFavouriteStops(mockFile)

        verify(favouritesDao, never())
                .removeAllFavouriteStops()
        verify(favouritesDao, never())
                .addFavouriteStops(any())
    }

    @Test
    fun restoreFavouriteStopsWithEmptyFavouritesDoesntRestoreFavouriteStops() {
        val backup = Backup(MOCK_DATABASE_VERSION, 1, MOCK_TIME, emptyList())
        whenever(backupPersistence.readBackup(mockFile))
                .thenReturn(backup)

        task.restoreFavouriteStops(mockFile)

        verify(favouritesDao, never())
                .removeAllFavouriteStops()
        verify(favouritesDao, never())
                .addFavouriteStops(any())
    }

    @Test
    fun restoreFavouriteStopsWithSingleFavouriteRestoresFavouriteStops() {
        val backupFavourite = BackupFavouriteStop("100001", "Name 1")
        val backup = Backup(MOCK_DATABASE_VERSION, 1, MOCK_TIME, listOf(backupFavourite))
        whenever(backupPersistence.readBackup(mockFile))
                .thenReturn(backup)
        val expectedFavourite = FavouriteStop(0, "100001", "Name 1")
        val expectedFavourites = listOf(expectedFavourite)

        task.restoreFavouriteStops(mockFile)

        verify(favouritesDao)
                .removeAllFavouriteStops()
        verify(favouritesDao)
                .addFavouriteStops(expectedFavourites)
    }

    @Test
    fun restoreFavouriteStopsWithMultipleFavouritesRestoresFavouriteStops() {
        val backupFavourite1 = BackupFavouriteStop("100001", "Name 1")
        val backupFavourite2 = BackupFavouriteStop("100002", "Name 2")
        val backupFavourite3 = BackupFavouriteStop("100003", "Name 3")
        val backup = Backup(MOCK_DATABASE_VERSION, 1, MOCK_TIME,
                listOf(backupFavourite1, backupFavourite2, backupFavourite3))
        whenever(backupPersistence.readBackup(mockFile))
                .thenReturn(backup)
        val expectedFavourite1 = FavouriteStop(0, "100001", "Name 1")
        val expectedFavourite2 = FavouriteStop(0, "100002", "Name 2")
        val expectedFavourite3 = FavouriteStop(0, "100003", "Name 3")
        val expectedFavourites = listOf(expectedFavourite1, expectedFavourite2, expectedFavourite3)

        task.restoreFavouriteStops(mockFile)

        verify(favouritesDao)
                .removeAllFavouriteStops()
        verify(favouritesDao)
                .addFavouriteStops(expectedFavourites)
    }

    private fun givenNoFavourites() {
        whenever(favouritesDao.getAllFavouriteStops())
                .thenReturn(null)
    }

    private fun givenSettingsDatabaseVersionIsReturned() {
        whenever(settingsDatabaseRepository.getDatabaseVersion())
                .thenReturn(MOCK_DATABASE_VERSION)
    }

    private fun givenTimestampIsReturned() {
        whenever(timeUtils.currentTimeMills)
                .thenReturn(MOCK_TIME)
    }
}