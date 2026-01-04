/*
 * Copyright (C) 2026 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.core.shortcuts

import android.content.Context
import android.content.Intent
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import uk.org.rivernile.android.bustracker.ui.deeplinks.BusTimesIntentFactory
import javax.inject.Inject

/**
 * This provides Android-specific extensions to [ShortcutsRepository].
 *
 * @author Niall Scott
 */
public interface AndroidShortcutsRepository : ShortcutsRepository {

    /**
     * Create an [Intent] which can be given as an [android.app.Activity] result when the Activity
     * was started with the [Intent.ACTION_CREATE_SHORTCUT] action.
     *
     * @param shortcut The data required for the shortcut.
     * @return The [Intent] to return back to the calling Activity.
     */
    public fun createPinFavouriteStopShortcutResultIntent(shortcut: FavouriteStopShortcut): Intent
}

internal class RealAndroidShortcutsRepository @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val busTimesIntentFactory: BusTimesIntentFactory
) : AndroidShortcutsRepository {

    override fun createPinFavouriteStopShortcutResultIntent(shortcut: FavouriteStopShortcut): Intent {
        return ShortcutManagerCompat
            .createShortcutResultIntent(
                context,
                createFavouriteShortcutInfo(shortcut)
            )
    }

    override fun pinFavouriteStopShortcut(shortcut: FavouriteStopShortcut) {
        ShortcutManagerCompat
            .requestPinShortcut(
                context,
                createFavouriteShortcutInfo(shortcut),
                null
            )
    }

    override fun updateFavouriteStopShortcut(shortcut: FavouriteStopShortcut) {
        ShortcutManagerCompat
            .updateShortcuts(
                context,
                listOf(createFavouriteShortcutInfo(shortcut))
            )
    }

    private fun createFavouriteShortcutInfo(shortcut: FavouriteStopShortcut): ShortcutInfoCompat {
        val busTimesIntent = busTimesIntentFactory.createBusTimesIntent(shortcut.stopCode)

        return ShortcutInfoCompat
            .Builder(context, shortcut.stopCode)
            .setIntent(busTimesIntent)
            .setShortLabel(shortcut.displayName)
            .setLongLabel(shortcut.displayName)
            .setIcon(IconCompat.createWithResource(context, R.drawable.appicon_favourite))
            .build()
    }
}
