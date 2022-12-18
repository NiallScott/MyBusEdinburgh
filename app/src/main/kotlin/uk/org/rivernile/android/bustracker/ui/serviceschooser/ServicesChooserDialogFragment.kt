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

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.edinburghbustracker.android.R

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

        /** The argument name for services.  */
        private const val ARG_SERVICES = "services"

        /** The argument name for the default selected services.  */
        private const val ARG_SELECTED_SERVICES = "selectedServices"

        /** The argument name for the dialog title.  */
        private const val ARG_TITLE = "dialogTitle"

        /**
         * Create a new instance of this [DialogFragment], providing an [Array] of services to
         * select from, an [Array] of services to select by default, and the title for the [Dialog].
         *
         * @param services An [Array] of services to present to the user to choose from.
         * @param selectedServices The services to select by default. Set as `null` if none.
         * @param dialogTitle The title to show on the [Dialog].
         * @return A new instance of this [DialogFragment].
         */
        fun newInstance(
                services: Array<String>,
                selectedServices: Array<String>?,
                dialogTitle: String) = ServicesChooserDialogFragment().apply {
            arguments = Bundle().apply {
                putStringArray(ARG_SERVICES, services)
                putStringArray(ARG_SELECTED_SERVICES, selectedServices)
                putString(ARG_TITLE, dialogTitle)
            }
        }
    }

    private val viewModel: ServicesChooserDialogFragmentViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        viewModel.services = arguments?.getStringArray(ARG_SERVICES)

        if (savedInstanceState == null) {
            viewModel.selectedServices = arguments?.getStringArray(ARG_SELECTED_SERVICES)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
                .setTitle(arguments?.getString(ARG_TITLE))
                .setMultiChoiceItems(
                        viewModel.services,
                        viewModel.checkBoxes) { _, which, isChecked ->
                    viewModel.onItemClicked(which, isChecked)
                }
                .setPositiveButton(R.string.close, null)
                .create()
    }

    override fun onDismiss(dialog: DialogInterface) {
        super.onDismiss(dialog)

        dispatchSelectedServices()
    }

    /**
     * Dispatch the selected services.
     *
     * This sets the result upon the parent [androidx.fragment.app.FragmentManager] with the request
     * key of [REQUEST_KEY], containing a [Bundle] with the selected services stored as an [Array],
     * under the key of [RESULT_CHOSEN_SERVICES].
     */
    private fun dispatchSelectedServices() {
        val selectedServices = viewModel.selectedServices

        Bundle().apply {
            putStringArray(RESULT_CHOSEN_SERVICES, selectedServices)
        }.let {
            parentFragmentManager.setFragmentResult(REQUEST_KEY, it)
        }
    }
}