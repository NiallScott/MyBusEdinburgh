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

package uk.org.rivernile.android.bustracker.ui.news

import android.view.View
import androidx.recyclerview.widget.RecyclerView
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.edinburghbustracker.android.R
import uk.org.rivernile.edinburghbustracker.android.databinding.ListItemTweetBinding
import java.lang.ref.WeakReference
import java.text.DateFormat

/**
 * This [RecyclerView.ViewHolder] contains a single [Tweet] as part of a list shown to the user.
 *
 * @param viewBinding The [View] binding.
 * @param tweetAvatarImageLoader An implementation which loads avatar images.
 * @param dateFormat A [DateFormat] instance to render timestamps in.
 * @param clickListenerRef A [WeakReference] to the click listener.
 * @author Niall Scott
 */
class TweetViewHolder(
        private val viewBinding: ListItemTweetBinding,
        private val tweetAvatarImageLoader: TweetAvatarImageLoader,
        private val dateFormat: DateFormat,
        private val clickListenerRef: WeakReference<OnItemClickedListener>)
    : RecyclerView.ViewHolder(viewBinding.root) {

    private var currentItem: Tweet? = null

    init {
        viewBinding.imgAvatar.setOnClickListener {
            handleAvatarClick()
        }
    }

    /**
     * Populate the contents of a [Tweet] in to the [View]s held by this [RecyclerView.ViewHolder].
     *
     * @param tweet The [Tweet] to populate with. If `null`, nominal default values will instead be
     * used.
     */
    fun populate(tweet: Tweet?) {
        currentItem = tweet

        viewBinding.apply {
            tweet?.let {
                tweetAvatarImageLoader.loadAvatar(imgAvatar, it.profileImageUrl)
                val displayName = it.displayName
                imgAvatar.contentDescription = displayName
                text1.text = it.body
                text2.text = text2.context.getString(R.string.tweetadapter_info_format, displayName,
                        dateFormat.format(it.time))
            } ?: run {
                tweetAvatarImageLoader.assignPlaceholderToAvatarImageView(imgAvatar)
                imgAvatar.contentDescription = null
                imgAvatar.isClickable = false
                text1.text = null
                text2.text = null
            }
        }
    }

    /**
     * Handle the avatar being clicked.
     */
    private fun handleAvatarClick() {
        currentItem?.let {
            clickListenerRef.get()?.invoke(it)
        }
    }
}