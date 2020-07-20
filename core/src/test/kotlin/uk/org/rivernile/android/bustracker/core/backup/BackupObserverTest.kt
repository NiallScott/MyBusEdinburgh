/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

import com.nhaarman.mockitokotlin2.any
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doAnswer
import com.nhaarman.mockitokotlin2.verify
import com.nhaarman.mockitokotlin2.whenever
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.junit.MockitoJUnitRunner
import uk.org.rivernile.android.bustracker.core.database.settings.daos.FavouritesDao
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceListener
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager

/**
 * Tests for [BackupObserver].
 *
 * @author Niall Scott
 */
@RunWith(MockitoJUnitRunner::class)
class BackupObserverTest {

    @Mock
    private lateinit var favouritesDao: FavouritesDao
    @Mock
    private lateinit var preferenceManager: PreferenceManager
    @Mock
    private lateinit var backupInvoker: BackupInvoker

    private lateinit var observer: BackupObserver

    @Before
    fun setUp() {
        observer = BackupObserver(favouritesDao, preferenceManager, backupInvoker)
    }

    @Test
    fun beginObservingCausesOnFavouritesChangedListenerToBeRegistered() {
        observer.beginObserving()

        verify(favouritesDao)
                .addOnFavouritesChangedListener(any())
    }

    @Test
    fun beginObservingCausesOnPreferencesChangedListenerToBeRegistered() {
        observer.beginObserving()

        argumentCaptor<PreferenceListener>().apply {
            verify(preferenceManager)
                    .addOnPreferenceChangedListener(capture())

            assertNull(firstValue.keys)
        }
    }

    @Test
    fun favouritesChangedCausesBackupInvocation() {
        doAnswer {
            it.getArgument<FavouritesDao.OnFavouritesChangedListener>(0)
                    .onFavouritesChanged()
        }.whenever(favouritesDao).addOnFavouritesChangedListener(any())

        observer.beginObserving()

        verify(backupInvoker)
                .performBackup()
    }

    @Test
    fun preferencesChangeCausesBackupInvocation() {
        doAnswer {
            it.getArgument<PreferenceListener>(0)
                    ?.listener
                    ?.onPreferenceChanged(null)
        }.whenever(preferenceManager).addOnPreferenceChangedListener(any())

        observer.beginObserving()

        verify(backupInvoker)
                .performBackup()
    }
}