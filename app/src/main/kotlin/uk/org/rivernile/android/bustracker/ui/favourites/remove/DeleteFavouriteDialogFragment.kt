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

package uk.org.rivernile.android.bustracker.ui.favourites.remove

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.android.support.AndroidSupportInjection
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject

/**
 * This [DialogFragment] will show an [AlertDialog] which asks the user to confirm if they wish to
 * delete the favourite stop or not.
 *
 * @author Niall Scott
 */
class DeleteFavouriteDialogFragment : DialogFragment() {

    companion object {

        private const val ARG_STOP_CODE = "stopCode"

        /**
         * Create a new instance of [DeleteFavouriteDialogFragment].
         *
         * @param stopCode The stop code to delete the favourite for.
         */
        @JvmStatic // TODO: remove when all callers are on Kotlin.
        fun newInstance(stopCode: String) = DeleteFavouriteDialogFragment().apply {
            arguments = Bundle().apply {
                putString(ARG_STOP_CODE, stopCode)
            }
        }
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: DeleteFavouriteDialogFragmentViewModel by viewModels { viewModelFactory }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)

        viewModel.stopCode = arguments?.getString(ARG_STOP_CODE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
            MaterialAlertDialogBuilder(requireContext())
                    .setTitle(R.string.deletefavouritedialog_title)
                    .setPositiveButton(R.string.okay) { _, _ ->
                        viewModel.onUserConfirmDeletion()
                    }
                    .setNegativeButton(R.string.cancel, null)
                    .create()
}