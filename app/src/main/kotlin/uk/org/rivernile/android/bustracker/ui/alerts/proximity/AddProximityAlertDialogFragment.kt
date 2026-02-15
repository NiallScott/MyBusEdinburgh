/*
 * Copyright (C) 2021 - 2026 Niall 'Rivernile' Scott
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
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.toParcelableStopIdentifier
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
import uk.org.rivernile.android.bustracker.core.permission.AndroidPermissionChecker
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.DialogAddProxAlertBinding
import javax.inject.Inject

/**
 * This [DialogFragment] allows a user to add a proximity alert for a supplied stop. It is
 * presented as a [Dialog].
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class AddProximityAlertDialogFragment : DialogFragment() {

    companion object {

        private const val DIALOG_PROXIMITY_LIMITATIONS = "dialogProximityLimitations"

        /**
         * Create a new instance of this [DialogFragment].
         *
         * @param stopIdentifier The stop to add a proximity alert for.
         * @return A new instance of this [DialogFragment].
         */
        fun newInstance(stopIdentifier: StopIdentifier) =
            AddProximityAlertDialogFragment().apply {
                arguments = Bundle().apply {
                    putParcelable(
                        AddProximityAlertDialogFragmentViewModel.STATE_STOP_IDENTIFIER,
                        stopIdentifier.toParcelableStopIdentifier()
                    )
                }
            }
    }

    @Inject
    lateinit var textFormattingUtils: TextFormattingUtils
    @Inject
    lateinit var permissionChecker: AndroidPermissionChecker
    @Inject
    lateinit var exceptionLogger: ExceptionLogger

    private val viewModel: AddProximityAlertDialogFragmentViewModel by viewModels()

    private val viewBinding by lazy {
        DialogAddProxAlertBinding.inflate(layoutInflater, null, false)
    }

    private val requestPermissionsLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions(),
        this::handleRequestPermissionsResult
    )

    private val requestBackgroundLocationPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
        this::handleBackgroundLocationPermissionResult
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.addproxalertdialog_title)
            .setView(viewBinding.root)
            .setPositiveButton(R.string.addproxalertdialog_button_add) { _, _ ->
                handleAddClicked()
            }
            .setNegativeButton(android.R.string.cancel, null)
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View = viewBinding.root

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
        viewModel.requestPermissionsLiveData.observe(viewLifecycle) {
            handleRequestPermissions()
        }
        viewModel.requestBackgroundLocationPermissionLiveData.observe(viewLifecycle) {
            handleRequestBackgroundLocationPermission()
        }
        viewModel.showAppSettingsLiveData.observe(viewLifecycle) {
            showAppSettings()
        }

        viewBinding.apply {
            btnLimitations.setOnClickListener {
                viewModel.onLimitationsButtonClicked()
            }

            btnErrorResolve.setOnClickListener {
                viewModel.onResolveErrorButtonClicked()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        updatePermissionState()
    }

    /**
     * Update the current permission state in [AddProximityAlertDialogFragmentViewModel].
     */
    private fun updatePermissionState() {
        viewModel.onPermissionsUpdated(
            UiPermissionsState(
                hasCoarseLocationPermission = permissionChecker.checkCoarseLocationPermission(),
                hasFineLocationPermission = permissionChecker.checkFineLocationPermission(),
                hasBackgroundLocationPermission = permissionChecker
                    .checkBackgroundLocationPermission(),
                hasPostNotificationsPermission = permissionChecker.checkPostNotificationPermission()
            )
        )
    }

    /**
     * This is called when the permission request result is returned.
     *
     * @param result The permission grant results as a [Map].
     */
    @SuppressLint("InlinedApi")
    private fun handleRequestPermissionsResult(result: Map<String, Boolean>) {
        val hasCoarseLocationPermission = result[Manifest.permission.ACCESS_COARSE_LOCATION]
            ?: permissionChecker.checkCoarseLocationPermission()
        val hasFineLocationPermission = result[Manifest.permission.ACCESS_FINE_LOCATION]
            ?: permissionChecker.checkFineLocationPermission()
        val hasBackgroundLocationPermission = permissionChecker.checkBackgroundLocationPermission()
        val hasPostNotificationsPermission = result[Manifest.permission.POST_NOTIFICATIONS]
            ?: permissionChecker.checkPostNotificationPermission()

        viewModel.onPermissionsResult(
            UiPermissionsState(
                hasCoarseLocationPermission = hasCoarseLocationPermission,
                hasFineLocationPermission = hasFineLocationPermission,
                hasBackgroundLocationPermission = hasBackgroundLocationPermission,
                hasPostNotificationsPermission = hasPostNotificationsPermission
            )
        )
    }

    /**
     * This is called when the background location permission request result is returned.
     *
     * @param result The permission grant result.
     */
    private fun handleBackgroundLocationPermissionResult(result: Boolean) {
        viewModel.onPermissionsUpdated(
            UiPermissionsState(
                hasCoarseLocationPermission = permissionChecker.checkCoarseLocationPermission(),
                hasFineLocationPermission = permissionChecker.checkFineLocationPermission(),
                hasBackgroundLocationPermission = result,
                hasPostNotificationsPermission = permissionChecker.checkPostNotificationPermission()
            )
        )
    }

    /**
     * Handle the stop details changing, which populates the content view with the stop details.
     *
     * @param stopDetails The loaded stop details.
     */
    private fun handleStopDetailsLoaded(stopDetails: StopDetails?) {
        viewBinding.txtBlurb.text = getString(
            R.string.addproxalertdialog_blurb,
            stopDetails?.let {
                textFormattingUtils.formatBusStopNameWithStopCode(it.stopIdentifier, it.stopName)
            }
        )
    }

    /**
     * Handle the [UiState] changing. This will show a different layout, which will either show
     * progress, the content view or an error.
     *
     * @param state The new [UiState].
     */
    private fun handleUiStateChanged(state: UiState) {
        viewBinding.apply {
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
                    txtErrorBlurb.setText(R.string.addproxalertdialog_error_insufficient_permissions)
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
                UiState.ERROR_NO_BACKGROUND_LOCATION_PERMISSION -> {
                    txtErrorBlurb.setText(
                        R.string.addproxalertdialog_error_background_location_permission_not_granted
                    )
                    btnErrorResolve.apply {
                        setText(R.string.addproxalertdialog_error_grant_permission_button)
                        visibility = View.VISIBLE
                    }
                    contentView.showErrorLayout()
                }
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
        } catch (e: ActivityNotFoundException) {
            exceptionLogger.log(e)
            Toast
                .makeText(
                    requireContext(),
                    R.string.addproxalertdialog_error_no_location_settings,
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    /**
     * This is called when permissions should be requested from the user.
     */
    private fun handleRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.POST_NOTIFICATIONS
            )
        } else {
            arrayOf(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }.let(requestPermissionsLauncher::launch)
    }

    /**
     * This is called when the background location permission should be requested from the user.
     */
    private fun handleRequestBackgroundLocationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            requestBackgroundLocationPermissionLauncher
                .launch(Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        }
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
        } catch (e: ActivityNotFoundException) {
            exceptionLogger.log(e)
            Toast
                .makeText(
                    context,
                    R.string.addproxalertdialog_error_no_app_settings,
                    Toast.LENGTH_SHORT
                )
                .show()
        }
    }

    /**
     * Based on the current selected position of the distance spinner, turn this in to the selected
     * distance in meters.
     *
     * @return The selected number of meters.
     */
    private fun getSelectedMeters() = when (viewBinding.spinnerDistance.selectedItemPosition) {
        1 -> 250
        2 -> 500
        3 -> 750
        4 -> 1000
        else -> 100
    }
}
