/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.removefavouritestop

import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForApplicationCoroutineScope
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.coroutines.di.ForViewModelCoroutineScope
import uk.org.rivernile.android.bustracker.core.favourites.FavouritesRepository
import javax.inject.Inject

/**
 * This is the [ViewModel] for [RemoveFavouriteStopDialogFragment].
 *
 * @param arguments Provides access to the arguments sent to this dialog.
 * @param favouritesRepository Used to access favourite stops.
 * @param defaultDispatcher The default [CoroutineDispatcher] for performing work on.
 * @param applicationCoroutineScope The application [CoroutineScope], so that work is continued to
 * be processed even after the dialog is dismissed. This is so the removal is not cancelled.
 * @param viewModelCoroutineScope The [ViewModel] [CoroutineScope], injected for testing.
 * @author Niall Scott
 */
@HiltViewModel
internal class RemoveFavouriteStopViewModel @Inject constructor(
    private val arguments: Arguments,
    private val favouritesRepository: FavouritesRepository,
    @param:ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher,
    @param:ForApplicationCoroutineScope private val applicationCoroutineScope: CoroutineScope,
    @ForViewModelCoroutineScope viewModelCoroutineScope: CoroutineScope
) : ViewModel(viewModelCoroutineScope) {

    /**
     * This is called when the user has confirmed they wish to remove the favourite stop.
     */
    fun onUserConfirmRemoval() {
        arguments.stopCode?.ifEmpty { null }?.let { stopCode ->
            // Uses the application CoroutineScope as the Dialog dismisses immediately, and we need
            // this task to finish. Fire and forget is fine here.
            applicationCoroutineScope.launch(defaultDispatcher) {
                favouritesRepository.removeFavouriteStop(stopCode = stopCode)
            }
        }
    }
}
