/*
 * Copyright (C) 2014 - 2015 Niall 'Rivernile' Scott
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
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.DateFormat;
import java.util.List;

import uk.org.rivernile.android.bustracker.parser.twitter.Tweet;
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
    private List<Tweet> tweets;

    /**
     * Create a new {@code TweetAdaper}.
     *
     * @param context A {@link Context} instance from the hosting {@link android.app.Activity}.
     */
    TweetAdapter(@NonNull final Context context) {
        this.context = context;
        inflater = LayoutInflater.from(context);
        setHasStableIds(true);
    }

    @Override
    public ViewHolder onCreateViewHolder(final ViewGroup parent, final int viewType) {
        return new ViewHolder(inflater.inflate(R.layout.tweet_item, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, final int position) {
        final Tweet tweet = getItem(position);

        if (tweet != null) {
            holder.text1.setText(Html.fromHtml(tweet.getBody()));
            holder.text2.setText(context.getString(R.string.tweetadapter_info_format,
                    tweet.getDisplayName(), OUT_DATE_FORMAT.format(tweet.getTime())));
        } else {
            holder.text1.setText(null);
            holder.text2.setText(null);
        }
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
     * This is the {@link RecyclerView.ViewHolder} for this adapter.
     */
    static class ViewHolder extends RecyclerView.ViewHolder {

        TextView text1, text2;

        /**
         * Create a new {@code ViewHolder}.
         *
         * @param itemView The root {@link View} of the row.
         */
        ViewHolder(@NonNull final View itemView) {
            super(itemView);

            text1 = (TextView) itemView.findViewById(android.R.id.text1);
            text2 = (TextView) itemView.findViewById(android.R.id.text2);
        }
    }
}
