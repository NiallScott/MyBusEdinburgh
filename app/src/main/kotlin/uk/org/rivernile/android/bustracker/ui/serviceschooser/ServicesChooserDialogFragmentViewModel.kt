/*
 * Copyright (C) 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.serviceschooser

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel

/**
 * This is the [ViewModel] for [ServicesChooserDialogFragment].
 *
 * @param savedState Used to access the saved instance state.
 * @author Niall Scott
 */
class ServicesChooserDialogFragmentViewModel(
        private val savedState: SavedStateHandle) : ViewModel() {

    companion object {

        private const val STATE_SELECTED_SERVICES = "selectedServices"
    }

    /**
     * This property hold the [Array] of services which the user can select from.
     */
    var services: Array<String>? = null

    /**
     * This property holds the services which the user has selected.
     */
    var selectedServices: Array<String>?
        get() = savedState[STATE_SELECTED_SERVICES]
        set(value) {
            savedState[STATE_SELECTED_SERVICES] = value
        }

    /**
     * This property exists as a convenience for the Dialog API, to turn the state of the selected
     * services in to a [BooleanArray] to dictate which checkboxes are checked (or not).
     */
    val checkBoxes: BooleanArray? get() {
        val services = this.services?.ifEmpty { null } ?: return null
        val selectedServices = selectedServices?.toSet() ?: emptySet()

        return BooleanArray(services.size) { index ->
            selectedServices.contains(services[index])
        }
    }

    /**
     * This is called when an item has been clicked.
     *
     * @param index The index of the clicked item, which corresponds to the index within [services].
     * @param isChecked Is the item at the given [index] checked?
     */
    fun onItemClicked(index: Int, isChecked: Boolean) {
        val serviceName = services?.getOrNull(index) ?: return
        val selectedServices = selectedServices?.toMutableSet() ?: mutableSetOf()

        if (isChecked) {
            selectedServices += serviceName
        } else {
            selectedServices -= serviceName
        }

        this.selectedServices = selectedServices.toTypedArray()
    }
}