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

package uk.org.rivernile.android.bustracker.ui.main

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.MenuProvider
import androidx.core.view.WindowCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import uk.org.rivernile.android.bustracker.ui.about.AboutActivity
import uk.org.rivernile.android.bustracker.ui.alerts.AlertManagerFragment
import uk.org.rivernile.android.bustracker.ui.alerts.proximity.AddProximityAlertDialogFragment
import uk.org.rivernile.android.bustracker.ui.alerts.proximity.DeleteProximityAlertDialogFragment
import uk.org.rivernile.android.bustracker.ui.alerts.time.AddTimeAlertDialogFragment
import uk.org.rivernile.android.bustracker.ui.alerts.time.DeleteTimeAlertDialogFragment
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapActivity
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapFragment
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity
import uk.org.rivernile.android.bustracker.ui.explore.ExploreFragment
import uk.org.rivernile.android.bustracker.ui.favourites.FavouriteStopsFragment
import uk.org.rivernile.android.bustracker.ui.favourites.addedit.AddEditFavouriteStopDialogFragment
import uk.org.rivernile.android.bustracker.ui.favourites.remove.DeleteFavouriteDialogFragment
import uk.org.rivernile.android.bustracker.ui.neareststops.NearestStopsFragment
import uk.org.rivernile.android.bustracker.ui.news.TwitterUpdatesFragment
import uk.org.rivernile.android.bustracker.ui.scroll.HasScrollableContent
import uk.org.rivernile.android.bustracker.ui.search.InstallBarcodeScannerDialogFragment
import uk.org.rivernile.android.bustracker.ui.settings.SettingsActivity
import uk.org.rivernile.android.bustracker.ui.turnongps.TurnOnGpsDialogFragment
import uk.org.rivernile.edinburghbustracker.android.BuildConfig
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.ActivityMainBinding
import javax.inject.Inject

/**
 * This [android.app.Activity] is the root Activity of the app.
 *
 * @author Niall Scott
 */
class MainActivity : AppCompatActivity(),
        AlertManagerFragment.Callbacks,
        BusStopMapFragment.Callbacks,
        ExploreFragment.Callbacks,
        FavouriteStopsFragment.Callbacks,
        InstallBarcodeScannerDialogFragment.Callbacks,
        NearestStopsFragment.Callbacks,
        TurnOnGpsDialogFragment.Callbacks,
        HasAndroidInjector {

    companion object {

        /**
         * An [Intent] action that, when received by this Activity, shows the manage alerts UI.
         */
        const val ACTION_MANAGE_ALERTS = "${BuildConfig.APPLICATION_ID}.ACTION_MANAGE_ALERTS"

        private const val FRAGMENT_TAG_FAVOURITES = "tagFavourites"
        private const val FRAGMENT_TAG_EXPLORE = "tagExplore"
        private const val FRAGMENT_TAG_UPDATES = "tagUpdates"
        private const val FRAGMENT_TAG_ALERTS = "tagAlerts"

        private const val DIALOG_ADD_FAVOURITE = "dialogAddFavourite"
        private const val DIALOG_ADD_PROX_ALERT = "dialogAddProxAlert"
        private const val DIALOG_ADD_TIME_ALERT = "dialogAddTimeAlert"
        private const val DIALOG_DELETE_PROX_ALERT = "dialogDeleteProxAlert"
        private const val DIALOG_DELETE_TIME_ALERT = "dialogDeleteTimeAlert"
        private const val DIALOG_DELETE_FAVOURITE = "dialogDeleteFavourite"
        private const val DIALOG_TURN_ON_GPS = "dialogTurnOnGps"

        private const val BARCODE_APP_PACKAGE =
                "market://details?id=com.google.zxing.client.android"
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private val viewModel: MainActivityViewModel by viewModels { viewModelFactory }

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setSupportActionBar(viewBinding.toolbar)

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)

        viewBinding.apply {
            appBarLayout.statusBarForeground =
                    MaterialShapeDrawable.createWithElevationOverlay(this@MainActivity)

            bottomNavigation.setOnItemSelectedListener {
                showItem(it.itemId, true)
                true
            }
        }

        viewModel.showSettingsLiveData.observe(this) {
            showSettings()
        }
        viewModel.showAboutLiveData.observe(this) {
            showAbout()
        }

        addMenuProvider(menuProvider)

        if (savedInstanceState == null) {
            if (!handleIntent(intent)) {
                showItem(R.id.main_navigation_favourites, false)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    override fun onShowAddEditFavouriteStop(stopCode: String) {
        AddEditFavouriteStopDialogFragment.newInstance(stopCode)
                .show(supportFragmentManager, DIALOG_ADD_FAVOURITE)
    }

    override fun onShowAddProximityAlert(stopCode: String) {
        AddProximityAlertDialogFragment.newInstance(stopCode)
                .show(supportFragmentManager, DIALOG_ADD_PROX_ALERT)
    }

    override fun onShowAddTimeAlert(stopCode: String, defaultServices: Array<String>?) {
        AddTimeAlertDialogFragment.newInstance(stopCode, defaultServices)
                .show(supportFragmentManager, DIALOG_ADD_TIME_ALERT)
    }

    override fun onShowBusStopMapWithStopCode(stopCode: String) {
        Intent(this, BusStopMapActivity::class.java)
                .putExtra(BusStopMapActivity.EXTRA_STOP_CODE, stopCode)
                .let(this::startActivity)
    }

    override fun onShowBusTimes(stopCode: String) {
        Intent(this, DisplayStopDataActivity::class.java)
                .putExtra(DisplayStopDataActivity.EXTRA_STOP_CODE, stopCode)
                .let(this::startActivity)
    }

    override fun onShowConfirmDeleteProximityAlert(stopCode: String) {
        DeleteProximityAlertDialogFragment.newInstance(stopCode)
                .show(supportFragmentManager, DIALOG_DELETE_PROX_ALERT)
    }

    override fun onShowConfirmDeleteTimeAlert(stopCode: String) {
        DeleteTimeAlertDialogFragment.newInstance(stopCode)
                .show(supportFragmentManager, DIALOG_DELETE_TIME_ALERT)
    }

    override fun onShowConfirmFavouriteDeletion(stopCode: String) {
        DeleteFavouriteDialogFragment.newInstance(stopCode)
                .show(supportFragmentManager, DIALOG_DELETE_FAVOURITE)
    }

    override fun onShowSystemLocationPreferences(): Boolean {
        return try {
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    .let(this::startActivity)
            true
        } catch (ignored: ActivityNotFoundException) {
            false
        }
    }

    override fun onAskTurnOnGps() {
        TurnOnGpsDialogFragment()
                .show(supportFragmentManager, DIALOG_TURN_ON_GPS)
    }

    override fun onShowInstallBarcodeScanner() {
        try {
            Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse(BARCODE_APP_PACKAGE))
                    .let(this::startActivity)
        } catch (ignored: ActivityNotFoundException) {
            Toast.makeText(this, R.string.barcodescannerdialog_noplaystore, Toast.LENGTH_LONG)
                    .show()
        }
    }

    override fun onExploreTabSwitched() {
        viewBinding.appBarLayout.setExpanded(true, true)
    }

    override fun androidInjector() = dispatchingAndroidInjector

    /**
     * Handle a new [Intent] sent to this [android.app.Activity].
     *
     * @param intent The received [Intent].
     * @return `true` if the [Intent] was handled here, otherwise `false`.
     */
    private fun handleIntent(intent: Intent) = when (intent.action) {
        ACTION_MANAGE_ALERTS -> {
            showItem(R.id.main_navigation_alerts, false)
            true
        }
        else -> false
    }

    /**
     * Show an item on the UI.
     *
     * @param itemId The resource ID mapped to this item.
     * @param animate Whether the UI transition should be animated.
     */
    private fun showItem(@IdRes itemId: Int, animate: Boolean) {
        supportFragmentManager.commit {
            supportFragmentManager.findFragmentById(R.id.fragmentContainer)?.let(this::detach)

            when (itemId) {
                R.id.main_navigation_favourites -> {
                    supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_FAVOURITES)
                            ?.let(this::attach)
                            ?: add(
                                    R.id.fragmentContainer,
                                    FavouriteStopsFragment(),
                                    FRAGMENT_TAG_FAVOURITES)
                }
                R.id.main_navigation_explore -> {
                    supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_EXPLORE)
                            ?.let(this::attach)
                            ?: add(
                                    R.id.fragmentContainer,
                                    ExploreFragment(),
                                    FRAGMENT_TAG_EXPLORE)
                }
                R.id.main_navigation_updates -> {
                    supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_UPDATES)
                            ?.let(this::attach)
                            ?: add(
                                    R.id.fragmentContainer,
                                    TwitterUpdatesFragment(),
                                    FRAGMENT_TAG_UPDATES)
                }
                R.id.main_navigation_alerts -> {
                    supportFragmentManager.findFragmentByTag(FRAGMENT_TAG_ALERTS)
                            ?.let(this::attach)
                            ?: add(
                                    R.id.fragmentContainer,
                                    AlertManagerFragment(),
                                    FRAGMENT_TAG_ALERTS)
                }
                else -> return@showItem
            }

            if (animate) {
                setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE)
            }
        }

        viewBinding.appBarLayout.setExpanded(true, true)
    }

    /**
     * Show the settings UI.
     */
    private fun showSettings() {
        startActivity(Intent(this, SettingsActivity::class.java))
    }

    /**
     * Show the about UI.
     */
    private fun showAbout() {
        startActivity(Intent(this, AboutActivity::class.java))
    }

    private val menuProvider = object : MenuProvider {
        override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
            menuInflater.inflate(R.menu.main_option_menu, menu)
        }

        override fun onMenuItemSelected(menuItem: MenuItem) = when (menuItem.itemId) {
            R.id.main_option_menu_settings -> {
                viewModel.onSettingsMenuItemClicked()
                true
            }
            R.id.main_option_menu_about -> {
                viewModel.onAboutMenuItemClicked()
                true
            }
            else -> false
        }
    }

    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            viewBinding.appBarLayout.liftOnScrollTargetViewId =
                    (f as? HasScrollableContent)?.scrollableContentIdRes ?: View.NO_ID
        }
    }
}