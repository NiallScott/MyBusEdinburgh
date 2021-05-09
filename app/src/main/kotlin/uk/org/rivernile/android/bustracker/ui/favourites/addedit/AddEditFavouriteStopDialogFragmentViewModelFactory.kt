/*
 * Copyright (C) 2021 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites.addedit

import androidx.lifecycle.SavedStateHandle
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.viewmodel.ViewModelSavedStateFactory
import javax.inject.Inject

/**
 * This [ViewModelSavedStateFactory] creates an [AddEditFavouriteStopDialogFragmentViewModel] with
 * the ability to use [SavedStateHandle].
 *
 * @param favouritesRepository The repository to access the user's favourites.
 * @param fetcher Used to fetch data.
 * @param textFormattingUtils Used to format stop names in to [String]s.
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @param applicationCoroutineScope The application [CoroutineScope].
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class AddEditFavouriteStopDialogFragmentViewModelFactory @Inject constructor(
        private val favouritesRepository: FavouritesRepository,
        private val fetcher: FavouriteStopFetcher,
        private val textFormattingUtils: TextFormattingUtils,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
        @ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope)
    : ViewModelSavedStateFactory<AddEditFavouriteStopDialogFragmentViewModel> {

    override fun create(handle: SavedStateHandle) =
            AddEditFavouriteStopDialogFragmentViewModel(
                    handle,
                    favouritesRepository,
                    fetcher,
                    textFormattingUtils,
                    defaultDispatcher,
                    applicationCoroutineScope)
}