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

package uk.org.rivernile.android.bustracker.ui.alerts.time

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import uk.org.rivernile.android.bustracker.core.alerts.AlertsRepository
import uk.org.rivernile.android.bustracker.core.di.ForDefaultDispatcher
import uk.org.rivernile.android.bustracker.core.di.ForGlobalCoroutineScope
import javax.inject.Inject

/**
 * This is the [ViewModel] for [DeleteTimeAlertDialogFragment].
 *
 * @param alertsRepository The [AlertsRepository], used to interface with the lower-level
 * architecture layers to handle alerts.
 * @param globalCoroutineScope The global [CoroutineScope].
 * @param defaultDispatcher The default [CoroutineDispatcher].
 * @author Niall Scott
 */
class DeleteTimeAlertDialogFragmentViewModel @Inject constructor(
        private val alertsRepository: AlertsRepository,
        @ForGlobalCoroutineScope private val globalCoroutineScope: CoroutineScope,
        @ForDefaultDispatcher private val defaultDispatcher: CoroutineDispatcher): ViewModel() {

    /**
     * This property contains the stop code for which the proximity alert should be deleted.
     */
    var stopCode: String? = null

    /**
     * This is called when the user has confirmed they wish to delete the arrival alert.
     */
    fun onUserConfirmDeletion() {
        stopCode?.ifEmpty { null }?.let {
            // Uses the global CoroutineScope as the Dialog dismisses immediately, and we need
            // this task to finish. Fire and forget is fine here.
            globalCoroutineScope.launch(defaultDispatcher) {
                alertsRepository.removeArrivalAlert(it)
            }
        }
    }
}