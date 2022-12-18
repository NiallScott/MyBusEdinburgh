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

package uk.org.rivernile.android.bustracker.ui.turnongps

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import uk.org.rivernile.android.bustracker.ui.callbacks.OnShowSystemLocationPreferencesListener
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.DialogTurnOnGpsBinding

/**
 * This [DialogFragment] asks the user if they wish to turn on the GPS receiver on their device.
 * It additionally asks the user if they wish to not be asked again. If the user confirms the
 * [Dialog], they are taken to the system settings where they can turn GPS on.
 *
 * @author Niall Scott
 */
class TurnOnGpsDialogFragment : DialogFragment() {

    private val viewModel: TurnOnGpsDialogFragmentViewModel by viewModels()

    private lateinit var callbacks: Callbacks

    private val viewBinding by lazy { DialogTurnOnGpsBinding.inflate(layoutInflater, null, false) }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        callbacks = try {
            context as Callbacks
        } catch (e: ClassCastException) {
            throw IllegalStateException("${context.javaClass.name} does not implement " +
                    Callbacks::class.java.name)
        }
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return MaterialAlertDialogBuilder(requireContext())
                .setCancelable(true)
                .setTitle(R.string.turnongpsdialog_title)
                .setView(viewBinding.root)
                .setPositiveButton(R.string.yes) { _, _ ->
                    viewModel.onYesClicked()
                }
                .setNegativeButton(R.string.no, null)
                .create()
    }

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?): View = viewBinding.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewBinding.chkTurnongps.setOnCheckedChangeListener { _, isChecked ->
            viewModel.onDoNotRemindCheckChanged(isChecked)
        }

        viewModel.showSystemLocationSettingsLiveData.observe(viewLifecycleOwner) {
            callbacks.onShowSystemLocationPreferences()
        }
    }

    /**
     * Any [android.app.Activity] which host this [DialogFragment] must implement this interface
     * to handle navigation events.
     */
    interface Callbacks : OnShowSystemLocationPreferencesListener
}