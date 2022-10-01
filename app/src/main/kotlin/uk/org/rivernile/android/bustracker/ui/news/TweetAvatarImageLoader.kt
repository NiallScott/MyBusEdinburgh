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

import android.content.Context
import android.widget.ImageView
import com.squareup.picasso.Picasso
import uk.org.rivernile.android.bustracker.core.endpoints.twitter.Tweet
import uk.org.rivernile.android.bustracker.picasso.CircleTransform
import uk.org.rivernile.edinburghbustracker.android.R
import javax.inject.Inject

/**
 * This class provides an implementation to load images for tweet avatars.
 *
 * @param context A [Context] instance.
 * @param picasso An instance of [Picasso] - the image loading library.
 * @param avatarTransformation The [CircleTransform] to use on the avatar to transform it in to the
 * correct shape.
 * @author Niall Scott
 */
class TweetAvatarImageLoader @Inject constructor(
        context: Context,
        private val picasso: Picasso,
        private val avatarTransformation: CircleTransform) {

    private val avatarSize = context.resources.getDimensionPixelSize(R.dimen.news_avatar_size)

    /**
     * Load the avatar.
     *
     * If the URL is unknown or the avatar failed to load, a placeholder image will be used for the
     * avatar instead.
     *
     * @param imageView The [ImageView] the avatar should be loaded in to.
     * @param url The URL of the avatar image.
     */
    fun loadAvatar(imageView: ImageView, url: String?) {
        url?.let {
            picasso.load(it)
                    .resize(avatarSize, avatarSize)
                    .centerCrop()
                    .transform(avatarTransformation)
                    .placeholder(R.drawable.avatar_placeholder)
                    .error(R.drawable.avatar_placeholder)
                    .into(imageView)
        } ?: assignPlaceholderToAvatarImageView(imageView)
    }

    /**
     * When we don't have a [Tweet] or a profile image URL, we assign the placeholder image as the
     * avatar instead.
     *
     * @param imageView The [ImageView] the placeholder should be assigned to.
     */
    fun assignPlaceholderToAvatarImageView(imageView: ImageView) {
        imageView.setImageResource(R.drawable.avatar_placeholder)
    }
}