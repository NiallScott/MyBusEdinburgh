/*
 * Copyright (C) 2023 - 2026 Niall 'Rivernile' Scott
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

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.core.bundle.getParcelableCompat
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.DialogServicesChooserBinding

/**
 * This [DialogFragment] allows the user to select services from a list and then return the user's
 * selection back to the caller. This may be used to ask the user to filter services or to declare
 * which services they are interested in.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class ServicesChooserDialogFragment : DialogFragment() {

    companion object {

        /** The request key used for the new Fragment result API.  */
        const val REQUEST_KEY = "requestChosenServices"

        /**
         * The key to use on the [Bundle] returned from the new Fragment result API to get the
         * user chosen services.
         */
        const val RESULT_CHOSEN_SERVICES = "chosenServices"

        /**
         * Create a new instance of this [DialogFragment], providing the [ServicesChooserParams].
         *
         * @param parameters The parameters to use to start this chooser.
         * @return A new instance of this [DialogFragment].
         */
        fun newInstance(parameters: ServicesChooserParams) = ServicesChooserDialogFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ServicesChooserDialogFragmentViewModel.STATE_PARAMS, parameters)
            }
        }
    }

    private val viewModel by viewModels<ServicesChooserDialogFragmentViewModel>()

    private lateinit var adapter: ServicesChooserAdapter

    private val viewBinding by lazy {
        DialogServicesChooserBinding.inflate(layoutInflater, null, false)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isCancelable = true

        adapter = ServicesChooserAdapter(requireContext(), viewModel::onServiceClicked)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val titleResId = arguments
            ?.getParcelableCompat<ServicesChooserParams>(
                ServicesChooserDialogFragmentViewModel.STATE_PARAMS)
            ?.titleResId
            ?: 0

        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(titleResId)
            .setView(viewBinding.root)
            .setPositiveButton(R.string.close, null)
            .setNeutralButton(R.string.serviceschooserdialog_btn_clear_all) { _, _ ->
                viewModel.onClearAllClicked()
            }
            .create()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?): View =
        viewBinding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.recyclerView.apply {
            adapter = this@ServicesChooserDialogFragment.adapter

            val drawable = ResourcesCompat.getDrawable(
                resources,
                R.drawable.divider_services_chooser_grid,
                null)
            val context = requireContext()
            val verticalDecorator = DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
            val horizontalDecorator = DividerItemDecoration(
                context,
                DividerItemDecoration.HORIZONTAL)

            drawable?.let {
                verticalDecorator.setDrawable(it)
                horizontalDecorator.setDrawable(it)
            }

            addItemDecoration(verticalDecorator)
            addItemDecoration(horizontalDecorator)

            addOnScrollListener(recyclerViewScrollListener)
        }

        val viewLifecycleOwner = viewLifecycleOwner
        viewModel.servicesLiveData.observe(viewLifecycleOwner, adapter::submitList)
        viewModel.uiStateLiveData.observe(viewLifecycleOwner, this::handleUiStateChanged)
        viewModel.isClearAllButtonEnabledLiveData.observe(viewLifecycleOwner,
            this::handleClearAllButtonEnabledStateChanged)
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        dispatchSelectedServices()
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Because of the way we set this up in DialogFragments, we can't null out the View. But at
        // least null out the RecyclerView's reference to the adapter to prevent a memory leak.
        viewBinding.recyclerView.adapter = null
    }

    /**
     * Handle the enabled state of the 'Clear all' button being changed.
     *
     * @param enabled Should the 'Clear all' button be enabled or not?
     */
    private fun handleClearAllButtonEnabledStateChanged(enabled: Boolean) {
        (dialog as? AlertDialog)
            ?.getButton(DialogInterface.BUTTON_NEUTRAL)
            ?.isEnabled = enabled
    }

    /**
     * Dispatch the selected services.
     *
     * This sets the result upon the parent [androidx.fragment.app.FragmentManager] with the request
     * key of [REQUEST_KEY], containing a [Bundle] with the selected services stored as an [Array],
     * under the key of [RESULT_CHOSEN_SERVICES].
     */
    private fun dispatchSelectedServices() {
        Bundle().apply {
            putParcelableArrayList(RESULT_CHOSEN_SERVICES, viewModel.selectedServices)
        }.let {
            parentFragmentManager.setFragmentResult(REQUEST_KEY, it)
        }
    }

    /**
     * Handle the UI state changing.
     *
     * @param state The new UI state.
     */
    private fun handleUiStateChanged(state: UiState) {
        viewBinding.apply {
            when (state) {
                UiState.PROGRESS -> contentView.showProgressLayout()
                UiState.CONTENT -> contentView.showContentLayout()
                UiState.ERROR_NO_SERVICES_GLOBAL -> {
                    contentView.showErrorLayout()
                    txtError.setText(R.string.serviceschooserdialog_error_no_services_global)
                }
                UiState.ERROR_NO_SERVICES_STOP -> {
                    contentView.showErrorLayout()
                    txtError.setText(R.string.serviceschooserdialog_error_no_services_stop)
                }
            }
        }
    }

    private val recyclerViewScrollListener = object : RecyclerView.OnScrollListener() {
        override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
            super.onScrolled(recyclerView, dx, dy)

            viewBinding.viewTopDividerLine.isVisible = recyclerView.canScrollVertically(-1)
            viewBinding.viewBottomDividerLine.isVisible = recyclerView.canScrollVertically(1)
        }
    }
}
