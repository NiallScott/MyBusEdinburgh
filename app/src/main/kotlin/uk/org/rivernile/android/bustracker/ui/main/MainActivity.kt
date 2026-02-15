/*
 * Copyright (C) 2022 - 2026 Niall 'Rivernile' Scott
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

import android.app.SearchManager
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup.MarginLayoutParams
import androidx.activity.OnBackPressedCallback
import androidx.activity.SystemBarStyle
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.annotation.IdRes
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.core.content.ContextCompat
import androidx.core.view.MenuProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.isInvisible
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.core.widget.doAfterTextChanged
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentTransaction
import androidx.fragment.app.commit
import com.google.android.material.color.MaterialColors
import com.google.android.material.search.SearchBar
import com.google.android.material.search.SearchView
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.core.log.ExceptionLogger
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
import uk.org.rivernile.android.bustracker.ui.core.R as Rcore
import uk.org.rivernile.android.bustracker.ui.explore.ExploreFragment
import uk.org.rivernile.android.bustracker.ui.neareststops.NearestStopsFragment
import uk.org.rivernile.android.bustracker.ui.HasScrollableContent
import uk.org.rivernile.android.bustracker.ui.HasTabBar
import uk.org.rivernile.android.bustracker.ui.news.NewsFragment
import uk.org.rivernile.android.bustracker.ui.search.SearchFragment
import uk.org.rivernile.android.bustracker.ui.settings.SettingsActivity
import uk.org.rivernile.android.bustracker.ui.turnongps.TurnOnGpsDialogFragment
import uk.org.rivernile.edinburghbustracker.android.BuildConfig
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.ActivityMainBinding
import javax.inject.Inject
import androidx.core.view.ViewGroupCompat
import uk.org.rivernile.android.bustracker.ui.addoreditfavouritestop.AddOrEditFavouriteStopDialogFragment
import uk.org.rivernile.android.bustracker.ui.favouritestops.FavouriteStopsFragment
import uk.org.rivernile.android.bustracker.core.domain.NaptanStopIdentifier
import uk.org.rivernile.android.bustracker.core.domain.ServiceDescriptor
import uk.org.rivernile.android.bustracker.core.domain.StopIdentifier
import uk.org.rivernile.android.bustracker.ui.removefavouritestop.RemoveFavouriteStopDialogFragment

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
        NearestStopsFragment.Callbacks,
        SearchFragment.Callbacks,
        TurnOnGpsDialogFragment.Callbacks {

    companion object {

        /**
         * An [Intent] action that, when received by this Activity, shows the manage alerts UI.
         */
        const val ACTION_MANAGE_ALERTS = "${BuildConfig.APPLICATION_ID}.ACTION_MANAGE_ALERTS"

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
    }

    @Inject
    lateinit var exceptionLogger: ExceptionLogger

    private val viewModel by viewModels<MainActivityViewModel>()

    private lateinit var viewBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val isDarkMode = (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) ==
            Configuration.UI_MODE_NIGHT_YES

        enableEdgeToEdge(
            navigationBarStyle = if (isDarkMode) {
                SystemBarStyle.dark(scrim = Color.TRANSPARENT)
            } else {
                SystemBarStyle.light(
                    scrim = Color.TRANSPARENT,
                    darkScrim = Color.TRANSPARENT
                )
            }
        )

        viewBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ViewGroupCompat.installCompatInsetsDispatch(viewBinding.root)

        setSupportActionBar(viewBinding.searchBar)

        viewBinding.apply {
            setUpAppBar()
            setupBottomNavigation()
            setupSearch()
        }

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)
        addMenuProvider(menuProvider)
        onBackPressedDispatcher.addCallback(this, backPressedCallback)

        viewModel.showSettingsLiveData.observe(this) {
            showSettings()
        }
        viewModel.showAboutLiveData.observe(this) {
            showAbout()
        }

        if (savedInstanceState == null) {
            if (!handleIntent(intent)) {
                viewBinding.bottomNavigation.selectedItemId = R.id.main_navigation_explore
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        handleIntent(intent)
    }

    override fun onSupportActionModeStarted(mode: ActionMode) {
        super.onSupportActionModeStarted(mode)

        @Suppress("DEPRECATION")
        window.statusBarColor = MaterialColors.getColor(
            viewBinding.root,
            com.google.android.material.R.attr.colorSurfaceContainer
        )
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

        @Suppress("DEPRECATION")
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

    override fun onShowAddOrEditFavouriteStop(stopIdentifier: StopIdentifier) {
        AddOrEditFavouriteStopDialogFragment
            .newInstance(stopIdentifier)
            .show(supportFragmentManager, DIALOG_ADD_FAVOURITE)
    }

    override fun onShowAddProximityAlert(stopIdentifier: StopIdentifier) {
        AddProximityAlertDialogFragment
            .newInstance(stopIdentifier)
            .show(supportFragmentManager, DIALOG_ADD_PROX_ALERT)
    }

    override fun onShowAddArrivalAlert(
        stopIdentifier: StopIdentifier,
        defaultServices: List<ServiceDescriptor>?
    ) {
        AddTimeAlertDialogFragment
            .newInstance(stopIdentifier, defaultServices)
            .show(supportFragmentManager, DIALOG_ADD_TIME_ALERT)
    }

    override fun onShowBusStopMapWithStopIdentifier(stopIdentifier: StopIdentifier) {
        Intent(this, BusStopMapActivity::class.java)
            .putExtra(BusStopMapActivity.EXTRA_STOP_CODE, stopIdentifier.toNaptanStopCodeOrThrow())
            .let(this::startActivity)
    }

    override fun onShowBusTimes(stopIdentifier: StopIdentifier) {
        Intent(this, DisplayStopDataActivity::class.java)
            .putExtra(
                DisplayStopDataActivity.EXTRA_STOP_CODE,
                stopIdentifier.toNaptanStopCodeOrThrow()
            )
            .let(this::startActivity)
    }

    override fun onShowConfirmRemoveProximityAlert(stopIdentifier: StopIdentifier) {
        DeleteProximityAlertDialogFragment
            .newInstance(stopIdentifier)
            .show(supportFragmentManager, DIALOG_DELETE_PROX_ALERT)
    }

    override fun onShowConfirmRemoveArrivalAlert(stopIdentifier: StopIdentifier) {
        DeleteTimeAlertDialogFragment
            .newInstance(stopIdentifier)
            .show(supportFragmentManager, DIALOG_DELETE_TIME_ALERT)
    }

    override fun onShowConfirmFavouriteRemoval(stopIdentifier: StopIdentifier) {
        RemoveFavouriteStopDialogFragment
            .newInstance(stopIdentifier)
            .show(supportFragmentManager, DIALOG_DELETE_FAVOURITE)
    }

    override fun onShowSystemLocationPreferences(): Boolean {
        return try {
            Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .let(this::startActivity)
            true
        } catch (e: ActivityNotFoundException) {
            exceptionLogger.log(e)
            false
        }
    }

    override fun onAskTurnOnGps() {
        TurnOnGpsDialogFragment()
            .show(supportFragmentManager, DIALOG_TURN_ON_GPS)
    }

    override fun onExploreTabSwitched() {
        viewBinding.appBarLayout.setExpanded(true, true)
    }

    /**
     * Setup the app bar insets.
     */
    private fun ActivityMainBinding.setUpAppBar() {
        ViewCompat.setOnApplyWindowInsetsListener(searchBar) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout()
            )

            val paddingDouble = resources.getDimensionPixelOffset(Rcore.dimen.padding_double)

            view.updateLayoutParams<MarginLayoutParams> {
                leftMargin = paddingDouble + insets.left
                rightMargin = paddingDouble + insets.right
            }

            WindowInsetsCompat.CONSUMED
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

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                when (newState) {
                    SearchView.TransitionState.SHOWING,
                    SearchView.TransitionState.SHOWN -> {
                        window.isNavigationBarContrastEnforced = true
                    }
                    else -> {
                        window.isNavigationBarContrastEnforced = false
                    }
                }
            }
        }

        updateBackPressedCallbackState(searchView.currentTransitionState)

        searchBar.addOnLayoutChangeListener { v, _, top, _, bottom, _, _, _, _ ->
            val marginParams = v.layoutParams as MarginLayoutParams
            val contentBottomPadding = bottom - top + marginParams.topMargin +
                    marginParams.bottomMargin
            applyBottomContentPadding(supportFragmentManager, contentBottomPadding)
        }

        searchView.editText.doAfterTextChanged {
            performSearch(it?.toString())
        }

        if (!viewModel.hasShownInitialAnimation) {
            searchBar.addOnLoadAnimationCallback(object : SearchBar.OnLoadAnimationCallback() {
                override fun onAnimationEnd() {
                    viewModel.onInitialAnimationFinished()
                    searchBar.removeOnLoadAnimationCallback(this)
                }
            })

            searchBar.startOnLoadAnimation()
        }
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
            viewBinding.bottomNavigation.selectedItemId = R.id.main_navigation_alerts
            true
        }
        Intent.ACTION_SEARCH -> {
            viewBinding.searchView.apply {
                show()
                setText(intent.getStringExtra(SearchManager.QUERY))
            }
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
                R.id.main_navigation_explore -> {
                    supportFragmentManager
                        .findFragmentByTag(FRAGMENT_TAG_EXPLORE)
                        ?.let(this::attach)
                        ?: add(
                            R.id.fragmentContainer,
                            ExploreFragment(),
                            FRAGMENT_TAG_EXPLORE
                        )
                }
                R.id.main_navigation_updates -> {
                    supportFragmentManager
                        .findFragmentByTag(FRAGMENT_TAG_UPDATES)
                        ?.let(this::attach)
                        ?: add(
                            R.id.fragmentContainer,
                            NewsFragment(),
                            FRAGMENT_TAG_UPDATES
                        )
                }
                R.id.main_navigation_alerts -> {
                    supportFragmentManager
                        .findFragmentByTag(FRAGMENT_TAG_ALERTS)
                        ?.let(this::attach)
                        ?: add(
                            R.id.fragmentContainer,
                            AlertManagerFragment(),
                            FRAGMENT_TAG_ALERTS
                        )
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
     * Given the supplied [searchTerm], perform a search.
     *
     * @param searchTerm The user's search term.
     */
    private fun performSearch(searchTerm: String?) {
        (supportFragmentManager.findFragmentById(R.id.fragmentSearch) as SearchFragment)
            .searchTerm = searchTerm
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
        bottomContentPadding: Int
    ) {
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
            savedInstanceState: Bundle?
        ) {
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

    private fun StopIdentifier.toNaptanStopCodeOrThrow(): String {
        return if (this is NaptanStopIdentifier) {
            naptanStopCode
        } else {
            throw UnsupportedOperationException("Only Naptan stop codes are supported for now.")
        }
    }
}
