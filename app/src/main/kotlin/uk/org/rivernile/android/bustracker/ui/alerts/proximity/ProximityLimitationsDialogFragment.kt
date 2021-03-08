/*
 * Copyright (C) 2012 - 2021 Niall 'Rivernile' Scott
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

import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import uk.org.rivernile.edinburghbustracker.android.R

/**
 * This [DialogFragment] shows the user some disclaimer text regarding the proximity alert
 * feature in the application.
 *
 * @author Niall Scott
 */
class ProximityLimitationsDialogFragment : DialogFragment() {

    companion object {

        /**
         * Create a new instance of this [ProximityLimitationsDialogFragment].
         *
         * @return A new instance of this [ProximityLimitationsDialogFragment].
         */
        fun newInstance() = ProximityLimitationsDialogFragment()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        isCancelable = true
    }

    override fun onCreateDialog(savedInstanceState: Bundle?) =
            AlertDialog.Builder(requireContext())
                    .setTitle(R.string.proxlimitationsdialog_title)
                    .setMessage(R.string.proxlimitationsdialog_message)
                    .setNegativeButton(R.string.close, null)
                    .create()
}