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

package uk.org.rivernile.android.bustracker.ui.bustimes.details

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import dagger.android.support.AndroidSupportInjection
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.map.StopMapMarkerDecorator
import uk.org.rivernile.android.bustracker.viewmodel.GenericSavedStateViewModelFactory
import uk.org.rivernile.edinburghbustracker.android.databinding.StopdetailsFragmentBinding
import javax.inject.Inject

/**
 * This [Fragment] shows the user details pertaining to a given stop code.
 *
 * @author Niall Scott
 */
class StopDetailsFragment : Fragment() {

    companion object {

        private const val ARG_STOP_CODE = "stopCode"

        /**
         * Create a new instance of this [Fragment].
         *
         * @param stopCode The stop code this [Fragment] should show stop details for.
         * @return A new instance of this [Fragment].
         */
        fun newInstance(stopCode: String?) = StopDetailsFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_STOP_CODE, stopCode)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: StopDetailsFragmentViewModelFactory
    @Inject
    lateinit var stopMapMarkerDecorator: StopMapMarkerDecorator

    private val viewModel: StopDetailsFragmentViewModel by viewModels {
        GenericSavedStateViewModelFactory(viewModelFactory, this)
    }

    private lateinit var callbacks: Callbacks
    private lateinit var adapter: StopDetailsAdapter

    private var _viewBinding: StopdetailsFragmentBinding? = null
    private val viewBinding get() = _viewBinding!!

    private val requestLocationPermissionsLauncher =
            registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions(),
                    this::handleLocationPermissionsResult)

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = try {
            context as Callbacks
        } catch (ignored: ClassCastException) {
            throw IllegalStateException("${context::class.qualifiedName} must implement " +
                    Callbacks::class.qualifiedName)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)

        adapter = StopDetailsAdapter(
                requireContext(),
                stopMapMarkerDecorator) {
            viewModel.onMapClicked()
        }

        viewModel.stopCode = arguments?.getString(ARG_STOP_CODE)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View {
        _viewBinding = StopdetailsFragmentBinding.inflate(inflater, container, false)

        return viewBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.recyclerView.apply {
            setHasFixedSize(true)
            adapter = this@StopDetailsFragment.adapter
        }

        val lifecycle = viewLifecycleOwner
        viewModel.askForLocationPermissionsLiveData.observe(lifecycle) {
            handleAskLocationForPermissions()
        }
        viewModel.uiStateLiveData.observe(lifecycle, this::handleUiState)
        viewModel.itemsLiveData.observe(lifecycle, adapter::submitList)
        viewModel.showStopMapLiveData.observe(lifecycle, callbacks::showMapForStop)
    }

    override fun onResume() {
        super.onResume()

        updatePermissions()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        _viewBinding = null
    }

    /**
     * Update [StopDetailsFragmentViewModel] with the current state of permissions.
     */
    private fun updatePermissions() {
        viewModel.permissionsState = PermissionsState(
                getPermissionState(Manifest.permission.ACCESS_FINE_LOCATION),
                getPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    /**
     * Handle asking the user to grant permissions.
     */
    private fun handleAskLocationForPermissions() {
        requestLocationPermissionsLauncher.launch(
                arrayOf(
                        Manifest.permission.ACCESS_FINE_LOCATION,
                        Manifest.permission.ACCESS_COARSE_LOCATION))
    }

    /**
     * Handle the [UiState] changing.
     *
     * @param state The new [UiState] to be applied to the UI.
     */
    private fun handleUiState(state: UiState) {
        viewBinding.apply {
            when (state) {
                UiState.PROGRESS -> contentView.showProgressLayout()
                UiState.CONTENT -> contentView.showContentLayout()
            }
        }
    }

    /**
     * Handle the result of asking for location permission access.
     *
     * @param states A [Map] of the permission to a boolean which informs us if the permission was
     * granted or not.
     */
    private fun handleLocationPermissionsResult(states: Map<String, Boolean>) {
        val fineLocationState = states[Manifest.permission.ACCESS_FINE_LOCATION]
                ?.let(this::getPermissionState)
                ?: getPermissionState(Manifest.permission.ACCESS_FINE_LOCATION)
        val coarseLocationState = states[Manifest.permission.ACCESS_COARSE_LOCATION]
                ?.let(this::getPermissionState)
                ?: getPermissionState(Manifest.permission.ACCESS_COARSE_LOCATION)

        viewModel.permissionsState = PermissionsState(fineLocationState, coarseLocationState)
    }

    /**
     * For a given [permission], determine the [PermissionState].
     *
     * @param permission The permission to obtain the [PermissionState] for.
     * @return The determined [PermissionState].
     */
    private fun getPermissionState(permission: String) =
            getPermissionState(
                    ContextCompat.checkSelfPermission(requireContext(), permission) ==
                            PackageManager.PERMISSION_GRANTED)

    /**
     * Maps the permission granted status in to a [PermissionState].
     *
     * @param isGranted Has the permission been granted? `true` implies [PermissionState.GRANTED],
     * otherwise [PermissionState.UNGRANTED].
     * @return The determined [PermissionState].
     */
    private fun getPermissionState(isGranted: Boolean) = if (isGranted) {
        PermissionState.GRANTED
    } else {
        PermissionState.UNGRANTED
    }

    /**
     * This interface must be implemented by any [android.app.Activity] which hosts this [Fragment].
     */
    interface Callbacks {

        /**
         * This is called when the user wishes to see the stop map, with this stop being selected
         * by default.
         *
         * @param stopCode The stop the map should show.
         */
        fun showMapForStop(stopCode: String)
    }
}