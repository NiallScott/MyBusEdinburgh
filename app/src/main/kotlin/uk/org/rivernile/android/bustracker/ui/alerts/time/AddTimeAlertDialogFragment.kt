/*
 * Copyright (C) 2021 - 2022 Niall 'Rivernile' Scott
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

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.ui.serviceschooser.ServicesChooserDialogFragment
import uk.org.rivernile.android.bustracker.viewmodel.GenericSavedStateViewModelFactory
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.AddtimealertBinding
import javax.inject.Inject

/**
 * This [DialogFragment] allows a user to add a time alert for a supplied stop. It is presented
 * as a dialog.
 *
 * @author Niall Scott
 */
class AddTimeAlertDialogFragment : DialogFragment(), HasAndroidInjector {

    companion object {

        private const val ARG_STOP_CODE = "stopCode"
        private const val ARG_DEFAULT_SERVICES = "defaultServices"

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
        @JvmStatic // TODO: remove this annotation when all callers are on Kotlin.
        fun newInstance(stopCode: String, defaultServices: Array<String>?) =
                AddTimeAlertDialogFragment().apply {
                    arguments = Bundle().apply {
                        putString(ARG_STOP_CODE, stopCode)
                        putStringArray(ARG_DEFAULT_SERVICES, defaultServices)
                    }
                }
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    @Inject
    lateinit var viewModelFactory: AddTimeAlertDialogFragmentViewModelFactory
    @Inject
    lateinit var textFormattingUtils: TextFormattingUtils

    private val viewModel: AddTimeAlertDialogFragmentViewModel by viewModels {
        val defaultArgs = arguments?.getStringArray(ARG_DEFAULT_SERVICES)?.let {
            Bundle().apply {
                putStringArray(AddTimeAlertDialogFragmentViewModel.STATE_SELECTED_SERVICES, it)
            }
        }

        GenericSavedStateViewModelFactory(viewModelFactory, this, defaultArgs)
    }

    private val viewBinding by lazy { AddtimealertBinding.inflate(layoutInflater, null, false) }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)

        isCancelable = true
        viewModel.stopCode = arguments?.getString(ARG_STOP_CODE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
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

        viewBinding.apply {
            btnSelectServices.setOnClickListener {
                viewModel.onSelectServicesClicked()
            }

            btnLimitations.setOnClickListener {
                viewModel.onLimitationsButtonClicked()
            }
        }
    }

    override fun androidInjector() = dispatchingAndroidInjector

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
                        contentView.showErrorLayout()
                    } ?: contentView.showProgressLayout() // This should never happen.
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