/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.times

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import dagger.hilt.android.scopes.ViewModelScoped
import javax.inject.Inject

/**
 * We want to track the user's service expand/collapse state over configuration changes and UI
 * destruction/restoration events. The old way would involve persisting to a [android.os.Bundle]
 * when the instance state is saved, and read from the bundle on UI creation. As we want to keep as
 * much logic away from the UI as possible, we use [SavedStateHandle] coupled with
 * [androidx.lifecycle.ViewModel].
 *
 * [android.os.Bundle] objects can only persist some types of objects as it needs to know how to
 * serialise those objects. For tracking expanded services, we want to use a [HashSet] for optimal
 * lookup times. Unfortunately, it's not a type we can persist inside a bundle. So instead, we will
 * persist as an [ArrayList] of [String], and have to convert between [HashSet] and [ArrayList].
 *
 * It would have been problematic to use [SavedStateHandle.getLiveData] coupled with mapping the
 * [MutableLiveData] as this only works ideally in one way, but will cause problems when the value
 * is being set. Instead, we manage the updating ourselves in this class, and expose
 * [expandedServicesLiveData] which we control.
 *
 * @param handle The [SavedStateHandle] where the state is stored. We read from here and later
 * persist to here.
 * @author Niall Scott
 */
@ViewModelScoped
class ExpandedServicesTracker @Inject constructor(
        private val handle: SavedStateHandle) {

    companion object {

        private const val STATE_KEY_EXPANDED_SERVICES = "expandedServices"
    }

    private val expandedItems = HashSet<String>()
    private val expandedServices = MutableLiveData<Set<String>>()

    val expandedServicesLiveData: LiveData<Set<String>> get() = expandedServices

    init {
        handle.get<ArrayList<String>>(STATE_KEY_EXPANDED_SERVICES)?.let {
            expandedItems.addAll(it)
        }

        expandedServices.value = HashSet(expandedItems)
    }

    /**
     * When a service has been clicked, this method should be called to track the correct
     * expand/collapse state of each service. This method will expand or collapse as appropriate,
     * propagate the new state via [expandedServicesLiveData] and save the new state in the
     * [SavedStateHandle].
     *
     * @param serviceName The name of the service which was clicked.
     */
    fun onServiceClicked(serviceName: String) {
        if (!expandedItems.add(serviceName)) {
            expandedItems.remove(serviceName)
        }

        expandedServices.value = HashSet(expandedItems)
        handle[STATE_KEY_EXPANDED_SERVICES] = ArrayList(expandedItems)
    }
}