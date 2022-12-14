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

package uk.org.rivernile.android.bustracker.ui.settings

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import androidx.preference.ListPreferenceDialogFragmentCompat
import com.google.android.material.dialog.MaterialAlertDialogBuilder

/**
 * This class extends [ListPreferenceDialogFragmentCompat] as a workaround for the fact that
 * androidx.preference does not theme its dialogs with Material3 theming. This class instead uses a
 * [MaterialAlertDialogBuilder] to construct the dialog.
 *
 * This solution was found on
 * [Stack Overflow](https://stackoverflow.com/questions/70650073/adjust-androidx-preference-dialogs-to-follow-material-you)
 *
 * @author Niall Scott
 */
class MaterialListPreferenceDialogFragmentCompat : ListPreferenceDialogFragmentCompat() {

    companion object {

        /**
         * Create a new [MaterialListPreferenceDialogFragmentCompat].
         *
         * @param key The key of the preference.
         * @return A new instance of [MaterialListPreferenceDialogFragmentCompat].
         */
        fun newInstance(key: String) = MaterialListPreferenceDialogFragmentCompat().apply {
            arguments = Bundle(1).apply {
                putString(ARG_KEY, key)
            }
        }
    }

    private var whichButtonClicked = 0
    private var onDialogClosedWasCalledFromDismiss = false

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val context = requireContext()
        whichButtonClicked = DialogInterface.BUTTON_NEGATIVE

        return MaterialAlertDialogBuilder(context).apply {
            setTitle(preference.dialogTitle)
            setIcon(preference.dialogIcon)
            setPositiveButton(
                    preference.positiveButtonText,
                    this@MaterialListPreferenceDialogFragmentCompat)
            setNegativeButton(
                    preference.negativeButtonText,
                    this@MaterialListPreferenceDialogFragmentCompat)

            onCreateDialogView(context)?.let {
                onBindDialogView(it)
                setView(it)
            } ?: setMessage(preference.dialogMessage)

            onPrepareDialogBuilder(this)
        }.create()
    }

    override fun onClick(dialog: DialogInterface, which: Int) {
        whichButtonClicked = which
    }

    override fun onDismiss(dialog: DialogInterface) {
        onDialogClosedWasCalledFromDismiss = true
        super.onDismiss(dialog)
    }

    override fun onDialogClosed(positiveResult: Boolean) {
        if (onDialogClosedWasCalledFromDismiss) {
            onDialogClosedWasCalledFromDismiss = false
            super.onDialogClosed(whichButtonClicked == DialogInterface.BUTTON_POSITIVE)
        } else {
            super.onDialogClosed(positiveResult)
        }
    }
}