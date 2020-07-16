/*
 * Copyright (C) 2014 - 2020 Niall 'Rivernile' Scott
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
 */

package uk.org.rivernile.android.bustracker.ui.news;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.lang.ref.WeakReference;
import java.text.DateFormat;
import java.util.List;

import uk.org.rivernile.android.bustracker.parser.twitter.Tweet;
import uk.org.rivernile.android.utils.CircleTransform;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * This adapter shows a list of {@link Tweet}s which show the latest travel updates to the user.
 *
 * @author Niall Scott
 */
class TweetAdapter extends RecyclerView.Adapter<TweetAdapter.ViewHolder> {

    private static final DateFormat OUT_DATE_FORMAT = DateFormat.getDateTimeInstance();

    private final Context context;
    private final LayoutInflater inflater;
    private final Picasso picasso;
    private final int avatarSize;
    private final CircleTransform circleTransform;
    private List<Tweet> tweets;
    private WeakReference<OnItemClickListener> clickListenerRef;

    /**
     * Create a new {@code TweetAdaper}.
     *
     * @param context A {@link Context} instance from the hosting {@link android.app.Activity}.
     */
    TweetAdapter(
            @NonNull final Context context,
            @NonNull final Picasso picasso) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        this.picasso = picasso;
        avatarSize = context.getResources().getDimensionPixelSize(R.dimen.news_avatar_size);
        circleTransform = new CircleTransform();
        setHasStableIds(true);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull final ViewGroup parent, final int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.tweet_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        holder.populate(getItem(position));
    }

    @Override
    public int getItemCount() {
        return tweets != null ? tweets.size() : 0;
    }

    @Override
    public long getItemId(final int position) {
        final Tweet tweet = getItem(position);
        return tweet != null ? tweet.hashCode() : 0;
    }

    /**
     * Get the {@link Tweet} at the given {@code position}.
     *
     * @param position The position of the {@link Tweet} to get.
     * @return The {@link Tweet} at the given {@code position}, or {@code null} if the {@link List}
     *         of {@link Tweet}s is {@code null}.
     */
    @Nullable
    Tweet getItem(final int position) {
        return position >= 0 && tweets != null && position < tweets.size() ?
                tweets.get(position) : null;
    }

    /**
     * Does this adapter have {@link Tweet}s to populate the views with?
     *
     * @return {@code true} if the {@link List} of {@link Tweet}s is {@code null} or empty,
     *         {@code false} if there's data.
     */
    boolean isEmpty() {
        return tweets == null || tweets.isEmpty();
    }

    /**
     * Set the {@link List} of {@link Tweet}s to display.
     *
     * @param tweets The {@link List} of {@link Tweet}s to display.
     */
    void setTweets(@Nullable final List<Tweet> tweets) {
        this.tweets = tweets;
        notifyDataSetChanged();
    }

    /**
     * Set a listener to be invoked when an item has been clicked in the {@link Tweet} item.
     *
     * @param listener A listener to be invoked when an item has been clicked in the
     * {@link Tweet} item.
     */
    void setOnItemClickListener(@Nullable final OnItemClickListener listener) {
        clickListenerRef = listener != null ? new WeakReference<>(listener) : null;
    }

    /**
     * This is the {@link RecyclerView.ViewHolder} for this adapter.
     */
    class ViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        ImageView imgAvatar;
        TextView text1, text2;

        /**
         * Create a new {@code ViewHolder}.
         *
         * @param itemView The root {@link View} of the row.
         */
        ViewHolder(@NonNull final View itemView) {
            super(itemView);

            imgAvatar = itemView.findViewById(R.id.imgAvatar);
            text1 = itemView.findViewById(android.R.id.text1);
            text2 = itemView.findViewById(android.R.id.text2);

            imgAvatar.setOnClickListener(this);
        }

        @Override
        public void onClick(final View v) {
            final int position = getAdapterPosition();

            if (position == RecyclerView.NO_POSITION) {
                return;
            }

            final Tweet tweet = getItem(position);

            if (tweet == null) {
                return;
            }

            final OnItemClickListener listener =
                    clickListenerRef != null ? clickListenerRef.get() : null;

            if (listener != null) {
                listener.onAvatarClicked(tweet);
            }
        }

        /**
         * Populate the contents of the this {@code ViewHolder}.
         */
        void populate(@Nullable final Tweet tweet) {
            if (tweet != null) {
                final String displayName = tweet.getDisplayName();
                imgAvatar.setContentDescription(displayName);
                imgAvatar.setClickable(!TextUtils.isEmpty(tweet.getProfileUrl()));
                text1.setText(Html.fromHtml(tweet.getBody()));
                text2.setText(context.getString(R.string.tweetadapter_info_format, displayName,
                        OUT_DATE_FORMAT.format(tweet.getTime())));
                picasso.load(tweet.getProfileImageUrl())
                        .resize(avatarSize, avatarSize)
                        .centerCrop()
                        .transform(circleTransform)
                        .placeholder(R.drawable.avatar_placeholder)
                        .error(R.drawable.avatar_placeholder)
                        .into(imgAvatar);
            } else {
                imgAvatar.setImageResource(R.drawable.avatar_placeholder);
                imgAvatar.setContentDescription(null);
                imgAvatar.setClickable(false);
                text1.setText(null);
                text2.setText(null);
            }
        }
    }

    /**
     * This interface should be implemented by classes wanting to know when elements inside a
     * {@link Tweet} item has been clicked.
     */
    interface OnItemClickListener {

        /**
         * This is called when an avatar has been clicked.
         *
         * @param tweet The {@link Tweet} containing the clicked avatar.
         */
        void onAvatarClicked(@NonNull Tweet tweet);
    }
}
