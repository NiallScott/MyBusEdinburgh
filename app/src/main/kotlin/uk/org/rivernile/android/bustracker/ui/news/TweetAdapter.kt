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

package uk.org.rivernile.android.bustracker.ui.news

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.edinburghbustracker.android.R
import java.lang.ref.WeakReference
import java.text.DateFormat

typealias OnItemClickedListener = (tweet: Tweet) -> Unit

/**
 * This [ListAdapter] is used to provide [Tweet]s to the recycler.
 *
 * @param context The [android.app.Activity] context.
 * @param avatarImageLoader An implementation used to load [Tweet] author avatars. This is passed
 * to the [TweetViewHolder]s.
 * @param clickListener This is given to us by the UI to handle list item clicks.
 * @author Niall Scott
 */
class TweetAdapter(
        context: Context,
        private val avatarImageLoader: TweetAvatarImageLoader,
        clickListener: OnItemClickedListener) : ListAdapter<Tweet, TweetViewHolder>(ItemEquator()) {

    private val inflater = LayoutInflater.from(context)
    private val dateFormat = DateFormat.getDateTimeInstance()
    private val clickListenerRef = WeakReference<OnItemClickedListener>(clickListener)

    init {
        setHasStableIds(true)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            TweetViewHolder(inflater.inflate(R.layout.tweet_item, parent, false), avatarImageLoader,
                    dateFormat, clickListenerRef)

    override fun onBindViewHolder(holder: TweetViewHolder, position: Int) {
        holder.populate(getItem(position))
    }

    override fun getItemId(position: Int) =
            getItem(position)?.hashCode()?.toLong() ?: 0L

    /**
     * This is used to compare [Tweet]s to determine recycler changes.
     */
    private class ItemEquator : DiffUtil.ItemCallback<Tweet>() {

        override fun areItemsTheSame(oldItem: Tweet, newItem: Tweet) =
                oldItem == newItem

        override fun areContentsTheSame(oldItem: Tweet, newItem: Tweet) =
                oldItem == newItem
    }
}