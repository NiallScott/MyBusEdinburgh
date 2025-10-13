/*
 * Copyright (C) 2018 - 2025 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.busstopmap

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.ViewGroupCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity
import uk.org.rivernile.android.bustracker.ui.HasScrollableContent
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.ActivityBusStopMapBinding

/**
 * This [android.app.Activity] displays the stop map to the user. This is usually hosted by
 * [uk.org.rivernile.android.bustracker.ui.main.MainActivity]. However, this is used when another
 * part of the app, or an external app, wants to deeplink in to the map.
 *
 * This [android.app.Activity] merely hosts the [BusStopMapFragment]. That is where the map display
 * logic is performed.
 *
 * @author Niall Scott
 */
@AndroidEntryPoint
class BusStopMapActivity : AppCompatActivity(), BusStopMapFragment.Callbacks {

    companion object {

        const val EXTRA_STOP_CODE = "stopCode"
        const val EXTRA_LATITUDE = "latitude"
        const val EXTRA_LONGITUDE = "longitude"
    }

    private lateinit var viewBinding: ActivityBusStopMapBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        viewBinding = ActivityBusStopMapBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)
        ViewGroupCompat.installCompatInsetsDispatch(viewBinding.root)

        setSupportActionBar(viewBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.toolbar) { view, windowInsets ->
            val insets = windowInsets.getInsets(
                WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.displayCutout()
            )

            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                leftMargin = insets.left
                rightMargin = insets.right
            }

            WindowInsetsCompat.CONSUMED
        }

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)

        if (savedInstanceState == null) {
            when {
                intent.hasExtra(EXTRA_STOP_CODE) ->
                    BusStopMapFragment.newInstance(intent.getStringExtra(EXTRA_STOP_CODE))
                intent.hasExtra(EXTRA_LATITUDE) && intent.hasExtra(EXTRA_LONGITUDE) -> {
                    val location = UiLatLon(
                        intent.getDoubleExtra(EXTRA_LATITUDE, 0.0),
                        intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
                    )
                    BusStopMapFragment.newInstance(location)
                }
                else -> BusStopMapFragment.newInstance()
            }.let { fragment ->
                supportFragmentManager.commit {
                    add(R.id.fragmentContainer, fragment)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)

        (currentFragment as? BusStopMapFragment)?.apply {
            when {
                intent.hasExtra(EXTRA_STOP_CODE) -> {
                    intent.getStringExtra(EXTRA_STOP_CODE)?.ifBlank { null }?.let {
                        onNewStopCode(it)
                        setIntent(intent)
                    }
                }
                intent.hasExtra(EXTRA_LATITUDE) && intent.hasExtra(EXTRA_LONGITUDE) -> {
                    val location = UiLatLon(
                        intent.getDoubleExtra(EXTRA_LATITUDE, 0.0),
                        intent.getDoubleExtra(EXTRA_LONGITUDE, 0.0)
                    )
                    onRequestCameraLocation(location)
                    setIntent(intent)
                }
            }
        }
    }

    override fun onShowBusTimes(stopCode: String) {
        Intent(this, DisplayStopDataActivity::class.java)
            .putExtra(DisplayStopDataActivity.EXTRA_STOP_CODE, stopCode)
            .let(this::startActivity)
    }

    /**
     * The current [androidx.fragment.app.Fragment] in the container.
     */
    private val currentFragment get() =
        supportFragmentManager.findFragmentById(R.id.fragmentContainer)

    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            viewBinding.appBarLayout.liftOnScrollTargetViewId =
                (f as? HasScrollableContent)?.scrollableContentIdRes ?: View.NO_ID
        }
    }
}
