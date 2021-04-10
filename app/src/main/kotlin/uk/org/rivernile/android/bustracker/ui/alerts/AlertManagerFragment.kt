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

package uk.org.rivernile.android.bustracker.ui.alerts

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.alertmanager.contentView
import kotlinx.android.synthetic.main.alertmanager.recyclerView
import kotlinx.coroutines.ExperimentalCoroutinesApi
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmDeleteProximityAlertListener
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowConfirmDeleteTimeAlertListener
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject

/**
 * This [Fragment] allows the users to view what proximity and time alerts they have set and allow
 * them to delete single alerts or all alerts.
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class AlertManagerFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var textFormattingUtils: TextFormattingUtils
    @Inject
    lateinit var stopMapMarkerDecorator: StopMapMarkerDecorator

    private val viewModel: AlertManagerFragmentViewModel by viewModels {
        viewModelFactory
    }

    private lateinit var callbacks: Callbacks
    private lateinit var adapter: AlertAdapter

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = try {
            context as Callbacks
        } catch (ignored: ClassCastException) {
            throw IllegalStateException("${context.javaClass.name} does not implement " +
                    Callbacks::class.java.name)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)

        adapter = AlertAdapter(
                requireContext(),
                textFormattingUtils,
                stopMapMarkerDecorator,
                alertItemClickListener)
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View =
            inflater.inflate(R.layout.alertmanager, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.setHasFixedSize(true)
        recyclerView.adapter = adapter

        val viewLifecycle = viewLifecycleOwner
        viewModel.alertsLiveData.observe(viewLifecycle, adapter::submitList)
        viewModel.uiStateLiveData.observe(viewLifecycle, this::setState)
        viewModel.showLocationSettingsLiveData.observe(viewLifecycle) {
            showSystemLocationSettings()
        }
        viewModel.showRemoveArrivalAlertLiveData.observe(viewLifecycle,
                this::showRemoveArrivalAlertDialog)
        viewModel.showRemoveProximityAlertLiveData.observe(viewLifecycle,
                this::showRemoveProximityAlertDialog)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        requireActivity().setTitle(R.string.alertmanager_title)
    }

    /**
     * Set the current UI state.
     *
     * @param state The new state to set as the current state.
     */
    private fun setState(state: UiState) {
        when (state) {
            UiState.PROGRESS -> contentView.showProgressLayout()
            UiState.CONTENT -> contentView.showContentLayout()
            UiState.ERROR -> contentView.showErrorLayout()
        }
    }

    /**
     * Show the location settings UI provided by the system.
     */
    private fun showSystemLocationSettings() {
        try {
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .setFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .let(this::startActivity)
        } catch (ignored: ActivityNotFoundException) {
            Toast.makeText(
                    requireContext(),
                    R.string.alertmanager_error_no_location_settings,
                    Toast.LENGTH_SHORT)
                    .show()
        }
    }

    /**
     * Show the UI where the user can confirm their remove of the arrival alert for the given stop.
     *
     * @param stopCode The stop code to remove the proximity alert for.
     */
    private fun showRemoveArrivalAlertDialog(stopCode: String) {
        callbacks.onShowConfirmDeleteTimeAlert(stopCode)
    }

    /**
     * Show the UI where the user can confirm their removal of the proximity alert for the given
     * stop.
     *
     * @param stopCode The stop code to remove the proximity alert for.
     */
    private fun showRemoveProximityAlertDialog(stopCode: String) {
        callbacks.onShowConfirmDeleteProximityAlert(stopCode)
    }

    private val alertItemClickListener = object : OnAlertItemClickListener {
        override fun onLocationSettingsClicked() {
            viewModel.onShowLocationSettingsClicked()
        }

        override fun onRemoveArrivalAlertClicked(stopCode: String) {
            viewModel.onRemoveArrivalAlertClicked(stopCode)
        }

        override fun onRemoveProximityAlertClicked(stopCode: String) {
            viewModel.onRemoveProximityAlertClicked(stopCode)
        }
    }

    /**
     * Any [android.app.Activity] which host this [Fragment] must implement this interface to handle
     * navigation events.
     */
    interface Callbacks : OnShowConfirmDeleteProximityAlertListener,
            OnShowConfirmDeleteTimeAlertListener
}