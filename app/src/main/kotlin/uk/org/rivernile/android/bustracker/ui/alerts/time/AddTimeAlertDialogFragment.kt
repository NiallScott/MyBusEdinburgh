/*
 * Copyright (C) 2021 - 2023 Niall 'Rivernile' Scott
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

import android.Manifest
import android.annotation.SuppressLint
import android.app.Dialog
import android.content.ActivityNotFoundException
import android.content.DialogInterface
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.core.permission.AndroidPermissionChecker
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.ui.serviceschooser.ServicesChooserDialogFragment
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.DialogAddTimeAlertBinding
import javax.inject.Inject

/**
 * This [DialogFragment] allows a user to add a time alert for a supplied stop. It is presented
 * as a dialog.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class AddTimeAlertDialogFragment : DialogFragment() {

    companion object {

        private const val ARG_STOP_CODE = AddTimeAlertDialogFragmentViewModel.STATE_STOP_CODE
        private const val ARG_DEFAULT_SERVICES =
                AddTimeAlertDialogFragmentViewModel.STATE_SELECTED_SERVICES

        private const val DIALOG_SELECT_SERVICES = "selectServicesDialog"
        private const val DIALOG_TIME_ALERT_LIMITATIONS = "timeLimitationsDialog"

        /**
         * Create a new [AddTimeAlertDialogFragment], supplying only the stop code.
         *
         * @param stopCode The stop code to add a time alert for.
         * @return A new [AddTimeAlertDialogFragment].
         */
        fun newInstance(stopCode: String) = AddTimeAlertDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_STOP_CODE, stopCode)
            }
        }

        /**
         * Create a new [AddTimeAlertDialogFragment], supplying the stop code and services that
         * are to be selected by default.
         *
         * @param stopCode The stop code to add a time alert for.
         * @param defaultServices Services that are to be selected by default.
         * @return A new [AddTimeAlertDialogFragment].
         */
        fun newInstance(stopCode: String, defaultServices: Array<String>?) =
                AddTimeAlertDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_STOP_CODE, stopCode)
                        putStringArray(ARG_DEFAULT_SERVICES, defaultServices)
                    }
                }
    }

    @Inject
    lateinit var textFormattingUtils: TextFormattingUtils
    @Inject
    lateinit var permissionChecker: AndroidPermissionChecker

    private val viewModel: AddTimeAlertDialogFragmentViewModel by viewModels()

    private val requestPermissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),
            this::handleRequestPermissionResult)

    private val viewBinding by lazy {
        DialogAddTimeAlertBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.addtimealertdialog_title)
                .setView(viewBinding.root)
                .setPositiveButton(R.string.addtimealertdialog_button_add) { _, _ ->
                    handleAddClicked()
                }
                .setNegativeButton(android.R.string.cancel, null)
                .create()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
            savedInstanceState: Bundle?): View = viewBinding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewLifecycle = viewLifecycleOwner
        childFragmentManager.setFragmentResultListener(ServicesChooserDialogFragment.REQUEST_KEY,
                viewLifecycle) { _, result ->
            handleServicesChosen(result)
        }

        viewModel.requestPermissionsLiveData.observe(viewLifecycle) {
            handleRequestPermissions()
        }
        viewModel.uiStateLiveData.observe(viewLifecycle, this::handleUiStateChanged)
        viewModel.stopDetailsLiveData.observe(viewLifecycle, this::handleStopDetailsLoaded)
        viewModel.selectedServicesLiveData.observe(viewLifecycle,
                this::handleSelectedServicesChanged)
        viewModel.addButtonEnabledLiveData.observe(viewLifecycle,
                this::handleAddButtonEnabledStateChanged)
        viewModel.showServicesChooserLiveData.observe(viewLifecycle, this::showServicesChooser)
        viewModel.showLimitationsLiveData.observe(viewLifecycle) {
            showLimitationsDialog()
        }
        viewModel.showAppSettingsLiveData.observe(viewLifecycle) {
            showAppSettings()
        }

        viewBinding.apply {
            btnResolve.setOnClickListener {
                viewModel.onResolveButtonClicked()
            }

            btnSelectServices.setOnClickListener {
                viewModel.onSelectServicesClicked()
            }

            btnLimitations.setOnClickListener {
                viewModel.onLimitationsButtonClicked()
            }
        }
    }

    override fun onResume() {
        super.onResume()

        updatePermissionState()
    }

    /**
     * Update the current permission state in [AddTimeAlertDialogFragmentViewModel].
     */
    private fun updatePermissionState() {
        viewModel.onPermissionsUpdated(
                UiPermissionsState(permissionChecker.checkPostNotificationPermission()))
    }

    /**
     * This is called when the permission request result is returned.
     *
     * @param isGranted Was the permission granted?
     */
    private fun handleRequestPermissionResult(isGranted: Boolean) {
        viewModel.onPermissionsResult(UiPermissionsState(isGranted))
    }

    /**
     * This is called when the permissions should be requested from the user.
     */
    @SuppressLint("InlinedApi")
    private fun handleRequestPermissions() {
        requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
    }

    /**
     * Handle the UI state changing by changing the visible layout.
     *
     * @param uiState The new [UiState] to display to the user.
     */
    private fun handleUiStateChanged(uiState: UiState) {
        viewBinding.apply {
            when (uiState) {
                UiState.PROGRESS -> contentView.showProgressLayout()
                UiState.CONTENT -> contentView.showContentLayout()
                UiState.ERROR_NO_STOP_CODE -> {
                    txtErrorBlurb.setText(R.string.addtimealertdialog_error_no_stop_code)
                    btnResolve.isVisible = false
                    contentView.showErrorLayout()
                }
                UiState.ERROR_NO_SERVICES -> {
                    viewModel.stopDetailsLiveData.value?.let {
                        val formattedName =
                                textFormattingUtils.formatBusStopNameWithStopCode(it.stopCode,
                                        it.stopName)
                        txtErrorBlurb.text =
                                getString(R.string.addtimealertdialog_error_no_services,
                                        formattedName)
                        btnResolve.isVisible = false
                        contentView.showErrorLayout()
                    } ?: contentView.showProgressLayout() // This should never happen.
                }
                UiState.ERROR_PERMISSION_REQUIRED -> {
                    txtErrorBlurb.setText(R.string.addtimealertdialog_error_request_permissions)
                    btnResolve.apply {
                        isVisible = true
                        setText(R.string.addtimealertdialog_error_btn_grant_permission)
                    }
                    contentView.showErrorLayout()
                }
                UiState.ERROR_PERMISSION_DENIED -> {
                    txtErrorBlurb.setText(R.string.addtimealertdialog_error_permission_denied)
                    btnResolve.apply {
                        isVisible = true
                        setText(R.string.addtimealertdialog_error_btn_permission_denied)
                    }
                    contentView.showErrorLayout()
                }
            }
        }
    }

    /**
     * Handle the stop details changing, which populates the content view with the stop details.
     *
     * @param stopDetails The loaded stop details.
     */
    private fun handleStopDetailsLoaded(stopDetails: StopDetails?) {
        viewBinding.txtBlurb.text = stopDetails?.let {
            val formattedName = textFormattingUtils.formatBusStopNameWithStopCode(
                    it.stopCode, it.stopName)
            getString(R.string.addtimealertdialog_blurb, formattedName)
        }
    }

    /**
     * Handle the user's selected services listing changed.
     *
     * @param selectedServices The current [List] of selected services.
     */
    private fun handleSelectedServicesChanged(selectedServices: List<String>?) {
        viewBinding.apply {
            selectedServices?.ifEmpty { null }?.let {
                txtSelectedServices.text = it.joinToString(separator = ", ")
            } ?: txtSelectedServices.setText(R.string.addtimealertdialog_no_services_selected)
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
        viewModel.onAddClicked(getSelectedTimeTrigger())
    }

    /**
     * Show a [Dialog] to the user which allows them to choose which services to set the time alert
     * for.
     *
     * @param params The parameters to start the chooser with,
     */
    private fun showServicesChooser(params: ServicesChooserParams) {
        ServicesChooserDialogFragment.newInstance(
                params.services.toTypedArray(),
                params.selectedServices?.toTypedArray(),
                getString(R.string.addtimealertdialog_services_chooser_dialog_title))
                .show(childFragmentManager, DIALOG_SELECT_SERVICES)
    }

    /**
     * Handle the user selecting their chosen services for the time alert.
     *
     * @param result The [Bundle] result generated by [ServicesChooserDialogFragment].
     */
    private fun handleServicesChosen(result: Bundle) {
        viewModel.selectedServices = result.getStringArray(
                ServicesChooserDialogFragment.RESULT_CHOSEN_SERVICES)
                ?.toList()
    }

    /**
     * Show a [Dialog] to the user which describes the limitations of the time alert feature.
     */
    private fun showLimitationsDialog() {
        TimeLimitationsDialogFragment.newInstance()
                .show(childFragmentManager, DIALOG_TIME_ALERT_LIMITATIONS)
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
     * Get the selected number of minutes to use as the time trigger.
     *
     * @return The selected number of minutes to use as the time trigger.
     */
    private fun getSelectedTimeTrigger() = when (viewBinding.spinnerTime.selectedItemPosition) {
        0 -> 1
        1 -> 2
        2 -> 5
        3 -> 10
        4 -> 15
        5 -> 20
        6 -> 25
        7 -> 30
        else -> 0
    }
}