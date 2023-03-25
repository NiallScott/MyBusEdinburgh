/*
 * Copyright (C) 2022 - 2023 Niall 'Rivernile' Scott
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
import android.view.ViewGroup.MarginLayoutParams
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.launch
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.search.SearchView
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.ui.RequiresContentPadding
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
import uk.org.rivernile.android.bustracker.ui.HasScrollableContent
import uk.org.rivernile.android.bustracker.ui.HasTabBar
import uk.org.rivernile.android.bustracker.ui.settings.SettingsActivity
import uk.org.rivernile.android.bustracker.ui.turnongps.TurnOnGpsDialogFragment
import uk.org.rivernile.edinburghbustracker.android.BuildConfig
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.ActivityMainBinding

/**
 * This [android.app.Activity] is the root Activity of the app.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class MainActivity : AppCompatActivity(),
        AlertManagerFragment.Callbacks,
        BusStopMapFragment.Callbacks,
        ExploreFragment.Callbacks,
        FavouriteStopsFragment.Callbacks,
        InstallBarcodeScannerDialogFragment.Callbacks,
        NearestStopsFragment.Callbacks,
        TurnOnGpsDialogFragment.Callbacks {

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
        private const val DIALOG_INSTALL_QR_SCANNER = "installQrScannerDialog"

        private const val BARCODE_APP_PACKAGE =
                "market://details?id=com.google.zxing.client.android"
    }

    private val viewModel by viewModels<MainActivityViewModel>()

    private lateinit var viewBinding: ActivityMainBinding

    private var menuItemScan: MenuItem? = null

    private val scanQrCodeLauncher = registerForActivityResult(ScanQrCode()) {
        viewModel.onQrScanned(it)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        WindowCompat.setDecorFitsSystemWindows(window, false)
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setSupportActionBar(viewBinding.searchBar)

        viewBinding.apply {
            appBarLayout.statusBarForeground =
                    MaterialShapeDrawable.createWithElevationOverlay(this@MainActivity)

            setupHorizontalInsets()
            setupBottomNavigation()
            setupSearch()
        }

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)
        addMenuProvider(menuProvider)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        viewModel.showStopLiveData.observe(this, this::onShowBusTimes)
        viewModel.isScanMenuItemVisibleLiveData.observe(this, this::handleIsScanMenuItemVisible)
        viewModel.showQrCodeScannerLiveData.observe(this) {
            handleShowQrCodeScanner()
        }
        viewModel.showInstallQrScannerDialogLiveData.observe(this) {
            handleShowInstallQrScannerDialog()
        }
        viewModel.showInvalidQrCodeErrorLiveData.observe(this) {
            showInvalidQrCodeError()
        }
        viewModel.showSettingsLiveData.observe(this) {
            showSettings()
        }
        viewModel.showAboutLiveData.observe(this) {
            showAbout()
        }

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

    override fun onSupportActionModeStarted(mode: ActionMode) {
        super.onSupportActionModeStarted(mode)

        window.statusBarColor = SurfaceColors.SURFACE_2.getColor(this)
        viewBinding.apply {
            appBarLayout.isInvisible = true
            bottomNavigation.isVisible = false
        }

        (currentFragment as? HasTabBar)?.isTabBarVisible = false

        ViewCompat.getRootWindowInsets(viewBinding.root)
                ?.getInsets(WindowInsetsCompat.Type.systemBars())
                ?.let {
                    viewBinding.fragmentContainer.updateLayoutParams<MarginLayoutParams> {
                        bottomMargin = it.bottom
                    }
                }
    }

    override fun onSupportActionModeFinished(mode: ActionMode) {
        super.onSupportActionModeFinished(mode)

        window.statusBarColor = ContextCompat.getColor(this, android.R.color.transparent)
        viewBinding.apply {
            appBarLayout.isInvisible = false
            bottomNavigation.isVisible = true
        }

        (currentFragment as? HasTabBar)?.isTabBarVisible = true

        viewBinding.fragmentContainer.updateLayoutParams<MarginLayoutParams> {
            bottomMargin = viewBinding.bottomNavigation.height
        }
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
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .let(this::startActivity)
        } catch (ignored: ActivityNotFoundException) {
            Toast.makeText(
                this,
                R.string.barcodescannerdialog_noplaystore,
                Toast.LENGTH_LONG)
                .show()
        }
    }

    override fun onExploreTabSwitched() {
        viewBinding.appBarLayout.setExpanded(true, true)
    }

    /**
     * Setup the horizontal insets on the root [View] so that our UI takes account of system UI.
     */
    private fun ActivityMainBinding.setupHorizontalInsets() {
        ViewCompat.setOnApplyWindowInsetsListener(root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.updateLayoutParams<MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
            }

            windowInsets
        }
    }

    /**
     * Setup the bottom navigation [View].
     */
    private fun ActivityMainBinding.setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener {
            showItem(it.itemId, true)
            true
        }

        bottomNavigation.addOnLayoutChangeListener { _, _, top, _, bottom, _, _, _, _ ->
            fragmentContainer.updateLayoutParams<MarginLayoutParams> {
                bottomMargin = bottom - top
            }
        }
    }

    /**
     * Setup the search [View]s.
     */
    private fun ActivityMainBinding.setupSearch() {
        searchView.addTransitionListener { _, _, newState ->
            updateBackPressedCallbackState(newState)
        }

        updateBackPressedCallbackState(searchView.currentTransitionState)

        searchBar.addOnLayoutChangeListener { v, _, top, _, bottom, _, _, _, _ ->
            val marginParams = v.layoutParams as MarginLayoutParams
            val contentBottomPadding = bottom - top + marginParams.topMargin +
                    marginParams.bottomMargin
            applyBottomContentPadding(supportFragmentManager, contentBottomPadding)
        }

        searchView.inflateMenu(R.menu.search_option_menu)
        menuItemScan = searchView.toolbar.menu.findItem(R.id.search_option_menu_scan)
        handleIsScanMenuItemVisible(viewModel.isScanMenuItemVisibleLiveData.value ?: false)
        searchView.setOnMenuItemClickListener(this@MainActivity::handleSearchViewMenuItemClicked)
    }

    /**
     * Update the state on [backPressedCallback] based upon the [SearchView.TransitionState].
     *
     * @param newState The new [SearchView.TransitionState].
     */
    private fun updateBackPressedCallbackState(newState: SearchView.TransitionState) {
        backPressedCallback.isEnabled =
            newState == SearchView.TransitionState.SHOWN ||
                    newState == SearchView.TransitionState.SHOWING
    }

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
            currentFragment?.let(this::detach)

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
     * Handle a [MenuItem] inside [SearchView] being clicked.
     *
     * @param menuItem The clicked [MenuItem].
     * @return `true` if the click was handled here, otherwise `false`.
     */
    private fun handleSearchViewMenuItemClicked(menuItem: MenuItem) = when (menuItem.itemId) {
        R.id.search_option_menu_scan -> {
            viewModel.onScanMenuItemClicked()
            true
        }
        else -> false
    }

    /**
     * Handle the visibility of the scan QR code menu item changing.
     *
     * @param isVisible Is the scan QR code menu item visible?
     */
    private fun handleIsScanMenuItemVisible(isVisible: Boolean) {
        menuItemScan?.isVisible = isVisible
    }

    /**
     * Attempt to launch the QR code scanner application.
     */
    private fun handleShowQrCodeScanner() {
        try {
            scanQrCodeLauncher.launch()
        } catch (ignored: ActivityNotFoundException) {
            viewModel.onQrScannerNotFound()
        }
    }

    /**
     * Show a dialog to the user asking them if they wish to install the QR scanner application.
     */
    private fun handleShowInstallQrScannerDialog() {
        InstallBarcodeScannerDialogFragment()
            .show(supportFragmentManager, DIALOG_INSTALL_QR_SCANNER)
    }

    /**
     * Show a [Toast] notification which informs the user they scanned an invalid QR code.
     */
    private fun showInvalidQrCodeError() {
        Toast.makeText(this, R.string.main_invalid_qrcode, Toast.LENGTH_SHORT).show()
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

    /**
     * The current [Fragment] attached to the [FragmentManager].
     */
    private val currentFragment get() =
        supportFragmentManager.findFragmentById(R.id.fragmentContainer)

    /**
     * Apply the bottom content padding to all [Fragment]s owned by the supplied [FragmentManager].
     * This method recursively calls itself to make sure the padding is passed down to all
     * child [FragmentManager]s.
     *
     * @param fragmentManager The [FragmentManager] to query for [Fragment]s to apply bottom content
     * padding on.
     * @param bottomContentPadding The bottom content padding to apply.
     */
    private fun applyBottomContentPadding(
        fragmentManager: FragmentManager,
        bottomContentPadding: Int) {
        fragmentManager.fragments.forEach { fragment ->
            if (fragment is RequiresContentPadding) {
                fragment.contentBottomPadding = bottomContentPadding
            } else {
                applyBottomContentPadding(fragment.childFragmentManager, bottomContentPadding)
            }
        }
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
        override fun onFragmentViewCreated(
            fm: FragmentManager,
            f: Fragment,
            v: View,
            savedInstanceState: Bundle?) {
            if (f is RequiresContentPadding) {
                val marginParams = viewBinding.searchBar.layoutParams as MarginLayoutParams
                f.contentBottomPadding = viewBinding.searchBar.height +
                        marginParams.topMargin +
                        marginParams.bottomMargin
            }
        }

        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            viewBinding.appBarLayout.liftOnScrollTargetViewId =
                    (f as? HasScrollableContent)?.scrollableContentIdRes ?: View.NO_ID
        }
    }

    private val backPressedCallback = object : OnBackPressedCallback(false) {
        override fun handleOnBackPressed() {
            viewBinding.searchView.hide()
        }
    }
}