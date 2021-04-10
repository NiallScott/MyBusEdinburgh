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

package uk.org.rivernile.android.bustracker.ui.alerts.proximity

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.addproxalert.btnErrorResolve
import kotlinx.android.synthetic.main.addproxalert.btnLimitations
import kotlinx.android.synthetic.main.addproxalert.contentView
import kotlinx.android.synthetic.main.addproxalert.spinnerDistance
import kotlinx.android.synthetic.main.addproxalert.txtBlurb
import kotlinx.android.synthetic.main.addproxalert.txtErrorBlurb
import kotlinx.coroutines.ExperimentalCoroutinesApi
import uk.org.rivernile.android.bustracker.core.permission.PermissionState
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject

/**
 * This [DialogFragment] allows a user to add a proximity alert for a supplied stop. It is
 * presented as a [Dialog].
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class AddProximityAlertDialogFragment : DialogFragment() {

    companion object {

        private const val ARG_STOP_CODE = "stopCode"

        private const val PERMISSION_REQUEST_LOCATION = 1

        private const val DIALOG_PROXIMITY_LIMITATIONS = "dialogProximityLimitations"

        /**
         * Create a new instance of this [DialogFragment].
         *
         * @param stopCode The stop code to add a proximity alert for.
         * @return A new instance of this [DialogFragment].
         */
        @JvmStatic // TODO: remove this annotation when all callers are on Kotlin.
        fun newInstance(stopCode: String) = AddProximityAlertDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_STOP_CODE, stopCode)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var textFormattingUtils: TextFormattingUtils

    private val viewModel: AddProximityAlertDialogFragmentViewModel by viewModels {
        viewModelFactory
    }

    private val rootView by lazy { createContentView() }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)

        isCancelable = true
        viewModel.stopCode = arguments?.getString(ARG_STOP_CODE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.addproxalertdialog_title)
                .setView(rootView)
                .setPositiveButton(R.string.addproxalertdialog_button_add) { _, _ ->
                    handleAddClicked()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View = rootView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewLifecycle = viewLifecycleOwner
        viewModel.stopDetailsLiveData.observe(viewLifecycle, this::handleStopDetailsLoaded)
        viewModel.uiStateLiveData.observe(viewLifecycle, this::handleUiStateChanged)
        viewModel.addButtonEnabledLiveData.observe(viewLifecycle,
                this::handleAddButtonEnabledStateChanged)
        viewModel.showLimitationsLiveData.observe(viewLifecycle) {
            showLimitationsDialog()
        }
        viewModel.showLocationSettingsLiveData.observe(viewLifecycle) {
            showLocationSettings()
        }
        viewModel.requestLocationPermissionLiveData.observe(viewLifecycle) {
            requestLocationPermission()
        }
        viewModel.showAppSettingsLiveData.observe(viewLifecycle) {
            showAppSettings()
        }

        btnLimitations.setOnClickListener {
            viewModel.onLimitationsButtonClicked()
        }

        btnErrorResolve.setOnClickListener {
            viewModel.onResolveErrorButtonClicked()
        }
    }

    override fun onStart() {
        super.onStart()

        checkLocationPermissionState()
    }

    override fun onRequestPermissionsResult(
            requestCode: Int,
            permissions: Array<out String>,
            grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST_LOCATION) {
            if (grantResults.isNotEmpty()) {
                grantResults[0]
            } else {
                PackageManager.PERMISSION_DENIED
            }.let(this::handleLocationPermissionState)
        }
    }

    /**
     * Create the content [View] for the dialog. This is created here so that the Lint warning can
     * be easily suppressed.
     *
     * @return The dialog content [View].
     */
    @SuppressLint("InflateParams")
    private fun createContentView() = layoutInflater.inflate(R.layout.addproxalert, null, false)

    /**
     * Handle the stop details changing, which populates the content view with the stop details.
     *
     * @param stopDetails The loaded stop details.
     */
    private fun handleStopDetailsLoaded(stopDetails: StopDetails?) {
        txtBlurb.text = stopDetails?.let {
            textFormattingUtils.formatBusStopNameWithStopCode(it.stopCode, it.stopName)
        }
    }

    /**
     * Handle the [UiState] changing. This will show a different layout, which will either show
     * progress, the content view or an error.
     *
     * @param state The new [UiState].
     */
    private fun handleUiStateChanged(state: UiState) {
        when (state) {
            UiState.PROGRESS -> contentView.showProgressLayout()
            UiState.CONTENT -> contentView.showContentLayout()
            UiState.ERROR_NO_LOCATION_FEATURE -> {
                txtErrorBlurb.setText(R.string.addproxalertdialog_error_no_location_feature)
                btnErrorResolve.visibility = View.GONE
                contentView.showErrorLayout()
            }
            UiState.ERROR_LOCATION_DISABLED -> {
                txtErrorBlurb.setText(R.string.addproxalertdialog_error_location_disabled)
                btnErrorResolve.apply {
                    setText(R.string.addproxalertdialog_error_location_disabled_button)
                    visibility = View.VISIBLE
                }
                contentView.showErrorLayout()
            }
            UiState.ERROR_PERMISSION_UNGRANTED -> {
                txtErrorBlurb.setText(R.string.addproxalertdialog_error_no_location_permission)
                btnErrorResolve.apply {
                    setText(R.string.addproxalertdialog_error_grant_permission_button)
                    visibility = View.VISIBLE
                }
                contentView.showErrorLayout()
            }
            UiState.ERROR_PERMISSION_DENIED -> {
                txtErrorBlurb.setText(R.string.addproxalertdialog_error_permission_denied)
                btnErrorResolve.apply {
                    setText(R.string.addproxalertdialog_error_permission_denied_button)
                    visibility = View.VISIBLE
                }
                contentView.showErrorLayout()
            }
        }
    }

    /**
     * Handle the enabled state of the 'Add' button being changed.
     *
     * @param enabled Should the 'Add' button be enabled or not?
     */
    private fun handleAddButtonEnabledStateChanged(enabled: Boolean) {
        (dialog as? AlertDialog)
                ?.getButton(DialogInterface.BUTTON_POSITIVE)
                ?.isEnabled = enabled
    }

    /**
     * Handle the 'Add' button being clicked.
     */
    private fun handleAddClicked() {
        viewModel.handleAddClicked(getSelectedMeters())
    }

    /**
     * Get the current state of the location permission and update the
     * [AddProximityAlertDialogFragmentViewModel] with the state.
     */
    private fun checkLocationPermissionState() {
        val state = ContextCompat.checkSelfPermission(requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION)
        handleLocationPermissionState(state)
    }

    /**
     * Handle the detected state of the location permission for this app. This updates the
     * [AddProximityAlertDialogFragmentViewModel].
     *
     * @param state The detected state.
     */
    private fun handleLocationPermissionState(state: Int) {
        when {
            state == PackageManager.PERMISSION_GRANTED -> PermissionState.GRANTED
            !shouldShowRequestPermissionRationale(Manifest.permission.ACCESS_FINE_LOCATION) ->
                PermissionState.UNGRANTED
            else -> PermissionState.DENIED
        }.let {
            viewModel.locationPermissionState = it
        }
    }

    /**
     * Show a [Dialog] to the user which describes the limitations of the proximity alert feature.
     */
    private fun showLimitationsDialog() {
        ProximityLimitationsDialogFragment.newInstance()
                .show(childFragmentManager, DIALOG_PROXIMITY_LIMITATIONS)
    }

    /**
     * Show the system location settings to the user, in the system settings app, to allow them to
     * turn location back on if it has been detected as off.
     *
     * If the [Intent] used to deep-link the user is unresolvable, a [Toast] with an error will be
     * shown instead (extremely unlikely scenario).
     */
    private fun showLocationSettings() {
        try {
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .let(this::startActivity)
        } catch (ignored: ActivityNotFoundException) {
            Toast.makeText(
                    requireContext(),
                    R.string.addproxalertdialog_error_no_location_settings,
                    Toast.LENGTH_SHORT)
                    .show()
        }
    }

    /**
     * Request location permission from the user.
     */
    private fun requestLocationPermission() {
        requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                PERMISSION_REQUEST_LOCATION)
    }

    /**
     * Show the application settings by deep-linking the user in to the system settings app, to
     * show settings for this application.
     *
     * If the [Intent] used to deep-link the user is unresolvable, a [Toast] with an error will be
     * shown instead (extremely unlikely scenario).
     */
    private fun showAppSettings() {
        val context = requireContext()

        try {
            Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .setData(Uri.fromParts("package", context.packageName, null))
                    .addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
                    .let(this::startActivity)
        } catch (ignored: ActivityNotFoundException) {
            Toast.makeText(context,
                    R.string.addproxalertdialog_error_no_app_settings,
                    Toast.LENGTH_SHORT)
                    .show()
        }
    }

    /**
     * Based on the current selected position of [spinnerDistance], turn this in to the selected
     * distance in meters.
     *
     * @return The selected number of meters.
     */
    private fun getSelectedMeters() = when (spinnerDistance.selectedItemPosition) {
        1 -> 250
        2 -> 500
        3 -> 750
        4 -> 1000
        else -> 100
    }
}