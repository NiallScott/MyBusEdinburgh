/*
 * Copyright (C) 2021 - 2023 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.favourites

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.commit
import com.google.android.material.elevation.SurfaceColors
import com.google.android.material.shape.MaterialShapeDrawable
import dagger.hilt.android.AndroidEntryPoint
import uk.org.rivernile.android.bustracker.ui.bustimes.DisplayStopDataActivity
import uk.org.rivernile.android.bustracker.ui.HasScrollableContent
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.ActivitySelectFavouriteStopBinding

/**
 * This [android.app.Activity] displays the user's saved favourite stops and allows them to select
 * a stop which will go on to be displayed on hte device home screen.
 *
 * @author Niall Scott
 * @see FavouriteStopsFragment
 */
@AndroidEntryPoint
class SelectFavouriteStopActivity : AppCompatActivity(),
        FavouriteStopsFragment.CreateShortcutCallbacks {

    private lateinit var viewBinding: ActivitySelectFavouriteStopBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Intent.ACTION_CREATE_SHORTCUT != intent.action) {
            finish()
            return
        }

        WindowCompat.setDecorFitsSystemWindows(window, false)
        viewBinding = ActivitySelectFavouriteStopBinding.inflate(layoutInflater)
        setContentView(viewBinding.root)

        setSupportActionBar(viewBinding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        viewBinding.appBarLayout.statusBarForeground =
                MaterialShapeDrawable.createWithElevationOverlay(this)
        window.navigationBarColor = SurfaceColors.SURFACE_2.getColor(this)

        ViewCompat.setOnApplyWindowInsetsListener(viewBinding.root) { view, windowInsets ->
            val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

            view.updateLayoutParams<ViewGroup.MarginLayoutParams> {
                bottomMargin = insets.bottom
                leftMargin = insets.left
                rightMargin = insets.right
            }

            windowInsets
        }

        supportFragmentManager.registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, true)

        if (savedInstanceState == null) {
            supportFragmentManager.commit {
                add(R.id.fragmentContainer, FavouriteStopsFragment())
            }
        }
    }

    override fun onCreateShortcut(stopCode: String, stopName: String) {
        val busTimesIntent = Intent(DisplayStopDataActivity.ACTION_VIEW_STOP_DATA)
                .setClass(this, DisplayStopDataActivity::class.java)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                .putExtra(DisplayStopDataActivity.EXTRA_STOP_CODE, stopCode)

        val result = ShortcutInfoCompat.Builder(this, stopCode)
                .setIntent(busTimesIntent)
                .setShortLabel(stopName)
                .setLongLabel(stopName)
                .setIcon(IconCompat.createWithResource(this, R.drawable.appicon_favourite))
                .build()
                .let {
                    ShortcutManagerCompat.createShortcutResultIntent(this, it)
                }

        setResult(Activity.RESULT_OK, result)
        finish()
    }

    private val fragmentLifecycleCallbacks = object : FragmentManager.FragmentLifecycleCallbacks() {
        override fun onFragmentResumed(fm: FragmentManager, f: Fragment) {
            viewBinding.appBarLayout.liftOnScrollTargetViewId =
                    (f as? HasScrollableContent)?.scrollableContentIdRes ?: View.NO_ID
        }
    }
}