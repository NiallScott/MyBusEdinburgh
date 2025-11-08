/*
 * Copyright (C) 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.removefavouritestop

import android.app.Dialog
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.core.os.bundleOf
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore

/**
 * This [DialogFragment] will show an [AlertDialog] which asks the user to confirm if they wish to
 * remove the favourite stop or not.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
public class RemoveFavouriteStopDialogFragment : DialogFragment() {

    public companion object {

        /**
         * Create a new instance of [RemoveFavouriteStopDialogFragment].
         *
         * @param stopCode The stop code to delete the favourite stop for.
         */
        public fun newInstance(stopCode: String): RemoveFavouriteStopDialogFragment {
            return RemoveFavouriteStopDialogFragment().apply {
                arguments = bundleOf(
                    ARG_STOP_CODE to stopCode
                )
            }
        }
    }

    private val viewModel by viewModels<RemoveFavouriteStopViewModel>()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
            .setTitle(R.string.deletefavouritestopdialog_title)
            .setPositiveButton(Rcore.string.okay) { _, _ ->
                viewModel.onUserConfirmRemoval()
            }
            .setNegativeButton(Rcore.string.cancel, null)
            .create()
    }
}
