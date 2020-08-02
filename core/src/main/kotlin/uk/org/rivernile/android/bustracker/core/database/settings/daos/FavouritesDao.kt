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

package uk.org.rivernile.android.bustracker.core.database.settings.daos

import uk.org.rivernile.android.bustracker.core.database.settings.entities.FavouriteStop

/**
 * This DAO is used to access favourites created in the app.
 *
 * @author Niall Scott
 */
interface FavouritesDao {

    /**
     * Add a new [OnFavouritesChangedListener] to be informed when the favourites data has been
     * changed.
     *
     * @param listener The listener to add.
     */
    fun addOnFavouritesChangedListener(listener: OnFavouritesChangedListener)

    /**
     * Remove a [OnFavouritesChangedListener] so it is no longer informed that favourites have been
     * changed.
     *
     * @param listener The listener to remove.
     */
    fun removeOnFavouritesChangedListener(listener: OnFavouritesChangedListener)

    /**
     * Is the given `stopCode` added as a favourite stop?
     *
     * @param stopCode The `stopCode` to check.
     * @return `true` if the stop is added as a favourite, otherwise `false`.
     */
    fun isStopAddedAsFavourite(stopCode: String): Boolean

    /**
     * Add [FavouriteStop]s.
     *
     * @param favouriteStops The favourite stops to add.
     * @return The number of rows added.
     */
    fun addFavouriteStops(favouriteStops: List<FavouriteStop>): Int

    /**
     * Remove all [FavouriteStop]s from the database.
     *
     * @return The number of deleted items.
     */
    fun removeAllFavouriteStops(): Int

    /**
     * Get all user-saved favourite stops.
     *
     * @return All user-saved favourite stops.
     */
    fun getAllFavouriteStops(): List<FavouriteStop>?

    /**
     * This interface should be implemented to listen for changes to favourites. Call
     * [addOnFavouritesChangedListener] to register the listener.
     */
    interface OnFavouritesChangedListener {

        /**
         * This is called when the favourites have changed.
         */
        fun onFavouritesChanged()
    }
}