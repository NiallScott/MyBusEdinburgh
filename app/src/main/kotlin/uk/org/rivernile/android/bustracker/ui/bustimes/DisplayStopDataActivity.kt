/*
 * Copyright (C) 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes

import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.appbar.AppBarLayout
import dagger.android.AndroidInjection
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import kotlinx.android.synthetic.main.displaystopdata.appBarLayout
import kotlinx.android.synthetic.main.displaystopdata.collapsingLayout
import kotlinx.android.synthetic.main.displaystopdata.tabLayout
import kotlinx.android.synthetic.main.displaystopdata.toolbar
import kotlinx.android.synthetic.main.displaystopdata.txtStopCode
import kotlinx.android.synthetic.main.displaystopdata.txtStopName
import kotlinx.android.synthetic.main.displaystopdata.viewPager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import uk.org.rivernile.android.bustracker.core.database.busstop.entities.StopDetails
import uk.org.rivernile.android.bustracker.core.text.TextFormattingUtils
import uk.org.rivernile.android.bustracker.ui.alerts.proximity.AddProximityAlertDialogFragment
import uk.org.rivernile.android.bustracker.ui.alerts.proximity.DeleteProximityAlertDialogFragment
import uk.org.rivernile.android.bustracker.ui.alerts.time.AddTimeAlertDialogFragment
import uk.org.rivernile.android.bustracker.ui.alerts.time.DeleteTimeAlertDialogFragment
import uk.org.rivernile.android.bustracker.ui.busstopmap.BusStopMapActivity
import uk.org.rivernile.android.bustracker.ui.bustimes.details.StopDetailsFragment
import uk.org.rivernile.android.bustracker.ui.favourites.AddEditFavouriteStopDialogFragment
import uk.org.rivernile.android.bustracker.ui.favourites.DeleteFavouriteDialogFragment
import uk.org.rivernile.edinburghbustracker.android.BuildConfig
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject
import kotlin.math.abs

/**
 * The purpose of this [AppCompatActivity] is to display to the user live departure times and
 * details for the given stop code.
 *
 * @author Niall Scott
 */
@ExperimentalCoroutinesApi
class DisplayStopDataActivity : AppCompatActivity(), StopDetailsFragment.Callbacks,
        HasAndroidInjector {

    companion object {

        /**
         * Use this [Intent] action to show live details for a specific stop, including departure
         * times.
         *
         * Must include the [Intent] extra [EXTRA_STOP_CODE].
         */
        const val ACTION_VIEW_STOP_DATA = "${BuildConfig.APPLICATION_ID}.ACTION_VIEW_STOP_DATA"

        /**
         * An [Intent] extra for the [ACTION_VIEW_STOP_DATA] action to specify what stop to show
         * details for. This is mandatory.
         */
        const val EXTRA_STOP_CODE = "stopCode"

        private const val DIALOG_ADD_FAVOURITE = "addFavouriteDialog"
        private const val DIALOG_ADD_ARRIVAL_ALERT = "addArrivalAlertDialog"
        private const val DIALOG_ADD_PROX_ALERT = "addProxAlertDialog"
        private const val DIALOG_REMOVE_FAVOURITE = "removeFavourite"
        private const val DIALOG_REMOVE_ARRIVAL_ALERT = "removeArrivalAlert"
        private const val DIALOG_REMOVE_PROX_ALERT = "removeProxAlert"

        private const val DEEPLINK_QUERY_PARAMETER_STOP_CODE = "busStopCode"
    }

    @Inject
    lateinit var dispatchingAndroidInjector: DispatchingAndroidInjector<Any>
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    @Inject
    lateinit var textFormattingUtils: TextFormattingUtils

    private lateinit var viewModel: DisplayStopDataActivityViewModel

    private var favouriteMenuItem: MenuItem? = null
    private var arrivalAlertMenuItem: MenuItem? = null
    private var proximityAlertMenuItem: MenuItem? = null
    private var streetViewMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        AndroidInjection.inject(this)

        super.onCreate(savedInstanceState)

        setContentView(R.layout.displaystopdata)

        viewModel = ViewModelProvider(this, viewModelFactory)
                .get(DisplayStopDataActivityViewModel::class.java)

        intent.let {
            viewModel.stopCode = if (Intent.ACTION_VIEW == it.action) {
                it.data?.getQueryParameter(DEEPLINK_QUERY_PARAMETER_STOP_CODE)
            } else {
                it.getStringExtra(EXTRA_STOP_CODE)
            }
        }

        setSupportActionBar(toolbar)
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            setDisplayShowTitleEnabled(false)
        }

        viewPager.apply {
            pageMargin = resources.getDimensionPixelSize(R.dimen.padding_default)
            adapter = StopDataPagerAdapter(this@DisplayStopDataActivity, supportFragmentManager,
                    viewModel.stopCode)
        }.let(tabLayout::setupWithViewPager)

        appBarLayout.addOnOffsetChangedListener(appBarOffsetChangedListener)

        viewModel.busStopDetails.observe(this, Observer(this::handleBusStop))
        viewModel.isFavouriteLiveData.observe(this, Observer(this::configureFavouriteMenuItem))
        viewModel.hasArrivalAlertLiveData.observe(this,
                Observer(this::configureArrivalAlertMenuItem))
        viewModel.hasProximityAlertLiveData.observe(this,
                Observer(this::configureProximityAlertMenuItem))
        viewModel.showAddFavouriteLiveData.observe(this, Observer(this::showAddFavourite))
        viewModel.showRemoveFavouriteLiveData.observe(this, Observer(this::showRemoveFavourite))
        viewModel.showAddArrivalAlertLiveData.observe(this, Observer(this::showAddArrivalAlert))
        viewModel.showRemoveArrivalAlertLiveData.observe(this, Observer {
            showRemoveArrivalAlert()
        })
        viewModel.showAddProximityAlertLiveData.observe(this, Observer(this::showAddProximityAlert))
        viewModel.showRemoveProximityAlertLiveData.observe(this, Observer {
            showRemoveProximityAlert()
        })
        viewModel.showStreetViewLiveData.observe(this, Observer(this::showStreetView))
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        super.onCreateOptionsMenu(menu)

        menuInflater.inflate(R.menu.displaystopdata_option_menu, menu)
        favouriteMenuItem = menu.findItem(R.id.displaystopdata_option_menu_favourite)
        arrivalAlertMenuItem = menu.findItem(R.id.displaystopdata_option_menu_time)
        proximityAlertMenuItem = menu.findItem(R.id.displaystopdata_option_menu_prox)
        streetViewMenuItem = menu.findItem(R.id.displaystopdata_option_menu_street_view)

        return true
    }

    override fun onPrepareOptionsMenu(menu: Menu): Boolean {
        super.onPrepareOptionsMenu(menu)

        configureFavouriteMenuItem(viewModel.isFavouriteLiveData.value)
        configureArrivalAlertMenuItem(viewModel.hasArrivalAlertLiveData.value)
        configureProximityAlertMenuItem(viewModel.hasProximityAlertLiveData.value)
        configureStreetViewMenuItem(viewModel.busStopDetails.value)

        return true
    }

    override fun onOptionsItemSelected(item: MenuItem) = when (item.itemId) {
        R.id.displaystopdata_option_menu_favourite -> {
            viewModel.onFavouriteMenuItemClicked()
            true
        }
        R.id.displaystopdata_option_menu_time -> {
            viewModel.onArrivalAlertMenuItemClicked()
            true
        }
        R.id.displaystopdata_option_menu_prox -> {
            viewModel.onProximityAlertMenuItemClicked()
            true
        }
        R.id.displaystopdata_option_menu_street_view -> {
            viewModel.onStreetViewMenuItemClicked()
            true
        }
        else -> super.onOptionsItemSelected(item)
    }

    override fun showMapForStop(stopCode: String) {
        Intent(this, BusStopMapActivity::class.java)
                .putExtra(BusStopMapActivity.EXTRA_STOP_CODE, stopCode)
                .let(this::startActivity)
    }

    override fun androidInjector() = dispatchingAndroidInjector

    /**
     * Handle the bus stop data being loaded. It also caters for the case when the details are
     * `null`, when the stop isn't known.
     *
     * @param details The stop details.
     */
    private fun handleBusStop(details: StopDetails?) {
        details?.also {
            val stopCode = it.stopCode
            val nameToDisplay = textFormattingUtils.formatBusStopName(it.stopName)

            txtStopName.text = nameToDisplay
            txtStopCode.text = stopCode

            supportActionBar?.apply {
                title = nameToDisplay
                subtitle = stopCode
            }
        } ?: run {
            val stopCode = viewModel.stopCode
            val nameToDisplay = getString(R.string.displaystopdata_unknown_stop_name)

            txtStopName.text = nameToDisplay
            txtStopCode.text = stopCode

            supportActionBar?.apply {
                title = nameToDisplay
                subtitle = stopCode
            }
        }

        configureStreetViewMenuItem(details)
    }

    /**
     * Configure the add/remove favourite [MenuItem] with the current favourite status of the stop.
     *
     * @param isFavourite Is the stop added as a favourite?
     */
    private fun configureFavouriteMenuItem(isFavourite: Boolean?) {
        isFavourite?.let {
            favouriteMenuItem?.let { menuItem ->
                menuItem.isEnabled = true

                if (it) {
                    menuItem.setTitle(R.string.displaystopdata_menu_favourite_rem)
                            .setIcon(R.drawable.ic_action_star)
                } else {
                    menuItem.setTitle(R.string.displaystopdata_menu_favourite_add)
                            .setIcon(R.drawable.ic_action_star_border)
                }
            }
        } ?: run {
            favouriteMenuItem?.isEnabled = false
        }
    }

    /**
     * Configure the add/remove arrival alert [MenuItem] with the current arrival alert status of
     * the stop.
     *
     * @param isArrivalAlert Is the stop added as an arrival alert?
     */
    private fun configureArrivalAlertMenuItem(isArrivalAlert: Boolean?) {
        isArrivalAlert?.let {
            arrivalAlertMenuItem?.let { menuItem ->
                menuItem.isEnabled = true

                if (it) {
                    menuItem.setTitle(R.string.displaystopdata_menu_time_rem)
                            .setIcon(R.drawable.ic_action_alarm_off)
                } else {
                    menuItem.setTitle(R.string.displaystopdata_menu_time_add)
                            .setIcon(R.drawable.ic_action_alarm_add)
                }
            }
        } ?: run {
            arrivalAlertMenuItem?.isEnabled = false
        }
    }

    /**
     * Configure the add/remove proximity alert [MenuItem] with the current proximity alert status
     * of the stop.
     *
     * @param isProximityAlert Is the stop added as a proximity alert?
     */
    private fun configureProximityAlertMenuItem(isProximityAlert: Boolean?) {
        isProximityAlert?.let {
            proximityAlertMenuItem?.let { menuItem ->
                menuItem.isEnabled = true

                if (it) {
                    menuItem.setTitle(R.string.displaystopdata_menu_prox_rem)
                            .setIcon(R.drawable.ic_action_location_off)
                } else {
                    menuItem.setTitle(R.string.displaystopdata_menu_prox_add)
                            .setIcon(R.drawable.ic_action_location_on)
                }
            }
        } ?: run {
            proximityAlertMenuItem?.isEnabled = false
        }
    }

    /**
     * Configure the Street View menu item depending on whether we have valid [StopDetails] yet,
     * and if so, is there any [android.app.Activity] on the device which responds to the Street
     * View [Intent]?
     *
     * @param details The [StopDetails] to use to configure the [MenuItem].
     */
    private fun configureStreetViewMenuItem(details: StopDetails?) {
        streetViewMenuItem?.let { menuItem ->
            menuItem.isVisible = details?.let {
                buildStreetViewIntent(it.latitude, it.longitude)
                        .resolveActivity(packageManager) != null
            } ?: false
        }
    }

    /**
     * Build an [Intent] which is used with [startActivity] to launch Street View on the device.
     * The [android.app.Activity] will live inside another application, and may not be present on
     * the device. Thus, it should be checked before/during usage to ensure the [Intent] will be
     * responded to.
     *
     * @param latitude The latitude of the stop.
     * @param longitude The longitude of the stop.
     * @return An [Intent] for launching Street View on this device.
     */
    private fun buildStreetViewIntent(latitude: Double, longitude: Double) =
            Intent(Intent.ACTION_VIEW)
                    .setData(Uri.parse("google.streetview:cbll=$latitude,$longitude"))
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

    /**
     * Show the 'Add favourite' UI.
     *
     * @param stopCode The stop code to add the favourite for.
     */
    private fun showAddFavourite(stopCode: String) {
        AddEditFavouriteStopDialogFragment.newInstance(stopCode)
                .show(supportFragmentManager, DIALOG_ADD_FAVOURITE)
    }

    /**
     * Show the 'Remove favourite' UI.
     *
     * @param stopCode The stop code to remove the favourite for.
     */
    private fun showRemoveFavourite(stopCode: String) {
        DeleteFavouriteDialogFragment.newInstance(stopCode)
                .show(supportFragmentManager, DIALOG_REMOVE_FAVOURITE)
    }

    /**
     * Show the 'Add arrival alert' UI.
     *
     * @param stopCode The stop code to add an arrival alert for.
     */
    private fun showAddArrivalAlert(stopCode: String) {
        AddTimeAlertDialogFragment.newInstance(stopCode)
                .show(supportFragmentManager, DIALOG_ADD_ARRIVAL_ALERT)
    }

    /**
     * Show the 'Remove arrival alert' UI.
     */
    private fun showRemoveArrivalAlert() {
        DeleteTimeAlertDialogFragment()
                .show(supportFragmentManager, DIALOG_REMOVE_ARRIVAL_ALERT)
    }

    /**
     * Show the 'Add proximity alert' UI.
     *
     * @param stopCode The stop code to add a proximity alert for.
     */
    private fun showAddProximityAlert(stopCode: String) {
        AddProximityAlertDialogFragment.newInstance(stopCode)
                .show(supportFragmentManager, DIALOG_ADD_PROX_ALERT)
    }

    /**
     * Show the 'Remove proximity alert' UI.
     */
    private fun showRemoveProximityAlert() {
        DeleteProximityAlertDialogFragment()
                .show(supportFragmentManager, DIALOG_REMOVE_PROX_ALERT)
    }

    /**
     * Show the Street View UI, which lives inside another application.
     *
     * This method assumes the Street View [Intent] has been pre-checked to ensure an
     * [android.app.Activity] on the system responds to it. The UI to enable this [Intent] to be
     * launched should be either disabled or not visible if this is not the case.
     *
     * As a last resort, this wraps the call to [startActivity] in a `try-catch` so that the app
     * does not crash. In this case, the user is shown a [Toast] which tells them Street View is
     * missing. This is a corner case.
     *
     * @param details The [StopDetails] to launch Street View with.
     */
    private fun showStreetView(details: StopDetails) {
        val intent = buildStreetViewIntent(details.latitude, details.longitude)

        try {
            startActivity(intent)
        } catch (ignored: ActivityNotFoundException) {
            // This should not happen as a check happened before even allowing the Street View menu
            // item to be displayed. Handling in case the device state changed since then.
            Toast.makeText(this, R.string.displaystopdata_error_street_view_missing,
                    Toast.LENGTH_SHORT)
                    .show()
        }
    }

    private val appBarOffsetChangedListener =
            AppBarLayout.OnOffsetChangedListener { _, verticalOffset ->
        supportActionBar?.setDisplayShowTitleEnabled(
                abs(verticalOffset) >= collapsingLayout.scrimVisibleHeightTrigger)
    }
}