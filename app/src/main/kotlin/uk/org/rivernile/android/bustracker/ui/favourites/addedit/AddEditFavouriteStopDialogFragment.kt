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

package uk.org.rivernile.android.bustracker.ui.favourites.addedit

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import dagger.android.support.AndroidSupportInjection
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.utils.Event
import uk.org.rivernile.android.bustracker.viewmodel.GenericSavedStateViewModelFactory
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.AddeditfavouritestopBinding
import javax.inject.Inject

/**
 * Show a [DialogFragment] which allows the user to add a new favourite stop, or edit the name
 * of an existing one. This [DialogFragment] will determine if the given stop code is already
 * a favourite stop and present the correct UI.
 *
 * @author Niall Scott
 */
class AddEditFavouriteStopDialogFragment : DialogFragment() {

    companion object {

        private const val ARG_STOPCODE = "stopCode"

        /**
         * Create a new instance of this [DialogFragment] with the given stop code.
         *
         * @param stopCode The stop to add or edit the favourite details for.
         * @return A new instance of this [DialogFragment].
         */
        @JvmStatic // TODO: remove this annotation when all callers are on Kotlin.
        fun newInstance(stopCode: String) = AddEditFavouriteStopDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_STOPCODE, stopCode)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: AddEditFavouriteStopDialogFragmentViewModelFactory
    @Inject
    lateinit var textFormattingUtils: TextFormattingUtils

    private val viewModel: AddEditFavouriteStopDialogFragmentViewModel by viewModels {
        GenericSavedStateViewModelFactory(viewModelFactory, this)
    }

    private val viewBinding by lazy {
        AddeditfavouritestopBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)

        viewModel.stopCode = arguments?.getString(ARG_STOPCODE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return AlertDialog.Builder(requireContext())
                .setTitle(R.string.addeditfavouritestopdialog_title_add)
                .setView(viewBinding.root)
                .setPositiveButton(R.string.addeditfavouritestopdialog_button_add) { _, _ ->
                    viewModel.onSubmitClicked()
                }
                .setNegativeButton(R.string.cancel, null)
                .create()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View = viewBinding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val viewLifecycle = viewLifecycleOwner
        viewModel.uiStateLiveData.observe(viewLifecycle, this::handleUiState)
        viewModel.isPositiveButtonEnabledLiveData.observe(viewLifecycle,
                this::updatePositiveButtonEnabledState)
        viewModel.prePopulateNameLiveData.observe(viewLifecycle, this::handleResetEditableName)

        // Sync ViewModel with what has been restored from saved state.
        viewModel.updateStopName(viewBinding.editName.text.toString())

        viewBinding.editName.doAfterTextChanged {
            viewModel.updateStopName(it?.toString())
        }
    }

    override fun getDialog(): AlertDialog? = super.getDialog() as? AlertDialog

    /**
     * Handle a new [UiState].
     *
     * @param state The new [UiState].
     */
    private fun handleUiState(state: UiState) {
        when (state) {
            is UiState.InProgress -> handleProgressUiState()
            is UiState.Mode.Add -> handleAddUiState(state)
            is UiState.Mode.Edit -> handleEditUiState(state)
        }
    }

    /**
     * Handle the progress UI state.
     */
    private fun handleProgressUiState() {
        dialog?.setTitle(R.string.addeditfavouritestopdialog_title_add)
        viewBinding.contentView.showProgressLayout()
    }

    /**
     * Handle the 'Add' UI state.
     *
     * @param state The 'Add' UI state.
     */
    private fun handleAddUiState(state: UiState.Mode.Add) {
        dialog?.setTitle(R.string.addeditfavouritestopdialog_title_add)

        viewBinding.apply {
            val stopName = textFormattingUtils.formatBusStopNameWithStopCode(
                    state.stopCode, state.stopName)
            txtBlurb.text = getString(R.string.addeditfavouritestopdialog_blurb_add, stopName)
            contentView.showContentLayout()
        }
    }

    /**
     * Handle the 'Edit' UI state.
     *
     * @param state The 'Edit' UI state.
     */
    private fun handleEditUiState(state: UiState.Mode.Edit) {
        dialog?.setTitle(R.string.addeditfavouritestopdialog_title_edit)

        viewBinding.apply {
            val stopName = textFormattingUtils.formatBusStopNameWithStopCode(
                    state.stopCode, state.stopName)
            txtBlurb.text = getString(R.string.addeditfavouritestopdialog_blurb_edit, stopName)
            contentView.showContentLayout()
        }
    }

    /**
     * Handle resetting the user editable field to a given value.
     *
     * @param event This is expressed as a consumable [Event] as to not overwrite the user's input
     * later.
     */
    private fun handleResetEditableName(event: Event<String>) {
        event.getContentIfNotHandled()?.let(viewBinding.editName::setText)
    }

    /**
     * Update the [AlertDialog] positive button enabled state.
     *
     * @param enabled Should the positive button be enabled?
     */
    private fun updatePositiveButtonEnabledState(enabled: Boolean) {
        dialog?.getButton(DialogInterface.BUTTON_POSITIVE)?.isEnabled = enabled
    }
}