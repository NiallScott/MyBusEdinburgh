/*
 * Copyright (C) 2020 - 2022 Niall 'Rivernile' Scott
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

import android.content.Context
import android.os.Bundle
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.preference.ListPreference
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import dagger.android.support.AndroidSupportInjection
import uk.org.rivernile.android.bustracker.core.preferences.AppTheme
import uk.org.rivernile.android.bustracker.core.preferences.PreferenceManager
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject

/**
 * This [SettingsFragment] shows the app's settings to the user and allows them to change their
 * settings. This uses the AndroidX Preferences API to render the UI.
 *
 * @author Niall Scott
 */
class SettingsFragment : PreferenceFragmentCompat(),
        PreferenceFragmentCompat.OnPreferenceDisplayDialogCallback,
        HasAndroidInjector {

    companion object {

        private const val DIALOG_CLEAR_SEARCH_HISTORY = "dialogClearSearchHistory"

        // Copied from the super class, so that our workaround uses the same Fragment tag.
        // Remove if/when the workaround is no longer required.
        private const val DIALOG_FRAGMENT_TAG = "androidx.preference.PreferenceFragment.DIALOG"
    }

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>

    private val viewModel: SettingsFragmentViewModel by viewModels {
        viewModelFactory
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidSupportInjection.inject(this)

        super.onCreate(savedInstanceState)
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        preferenceManager.sharedPreferencesName = PreferenceManager.PREF_FILE
        preferenceManager.sharedPreferencesMode = Context.MODE_PRIVATE
        setPreferencesFromResource(R.xml.preferences, rootKey)

        setupSummaries()
        setupClickListeners()
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.showClearSearchHistoryLiveData.observe(viewLifecycleOwner) {
            showClearSearchHistoryDialog()
        }
    }

    override fun onPreferenceDisplayDialog(
            caller: PreferenceFragmentCompat,
            pref: Preference): Boolean {
        return if (pref is ListPreference) {
            showListPreferenceDialog(pref)

            true
        } else {
            false
        }
    }

    override fun androidInjector() = dispatchingAndroidInjector

    /**
     * Set up the binding of preference summaries. These are used when the summary is dynamic based
     * on what the set value is.
     */
    private fun setupSummaries() {
        findPreference<ListPreference>(
                PreferenceManager.PREF_THEME)
                ?.setSummaryProvider {
                    when (viewModel.appTheme) {
                        AppTheme.SYSTEM_DEFAULT ->
                            getString(R.string.preferences_list_theme_system_default)
                        AppTheme.LIGHT -> getString(R.string.preferences_list_theme_light)
                        AppTheme.DARK -> getString(R.string.preferences_list_theme_dark)
                    }
                }

        findPreference<ListPreference>(
                PreferenceManager.PREF_NUMBER_OF_SHOWN_DEPARTURES_PER_SERVICE)
                ?.setSummaryProvider {
                    val numberOfDepartures = viewModel.numberOfDeparturesPerService
                    val strings = resources.getStringArray(
                            R.array.preferences_num_departures_entries)
                    strings[numberOfDepartures - 1]
                }
    }

    /**
     * Set up any required click listeners
     */
    private fun setupClickListeners() {
        findPreference<Preference>(PreferenceManager.PREF_CLEAR_SEARCH_HISTORY)
                ?.setOnPreferenceClickListener {
                    viewModel.onClearSearchHistoryClicked()
                    true
                }
    }

    /**
     * This is called when the user should confirm they wish to delete their search history.
     */
    private fun showClearSearchHistoryDialog() {
        ClearSearchHistoryDialogFragment()
                .show(childFragmentManager, DIALOG_CLEAR_SEARCH_HISTORY)
    }

    /**
     * Show a [MaterialListPreferenceDialogFragmentCompat] for a given [ListPreference]. This is a
     * workaround because the androidx.preference library currently does not support Material3
     * theming, and this allows Material3 themed dialogs to be shown.
     *
     * @param preference The [ListPreference] to show the
     * [MaterialListPreferenceDialogFragmentCompat] for.
     */
    @Suppress("deprecation")
    private fun showListPreferenceDialog(preference: ListPreference) {
        if (parentFragmentManager.findFragmentByTag(DIALOG_FRAGMENT_TAG) == null) {
            MaterialListPreferenceDialogFragmentCompat.newInstance(preference.key).apply {
                setTargetFragment(this@SettingsFragment, 0)
                show(this@SettingsFragment.parentFragmentManager, DIALOG_FRAGMENT_TAG)
            }
        }
    }
}