/*
 * Copyright (C) 2014 Niall 'Rivernile' Scott
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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.List;
import uk.org.rivernile.android.bustracker.parser.twitter.Tweet;
import uk.org.rivernile.edinburghbustracker.android.R;

/**
 * A TweetAdapter manages the creation of ListView rows for {@link Tweet}s. This
 * class is only package visible.
 * 
 * @author Niall Scott
 */
class TweetAdapter extends BaseAdapter {
    
    private static final DateFormat OUT_DATE_FORMAT =
            DateFormat.getDateTimeInstance();
    
    private final Context context;
    private final LayoutInflater inflater;
    private List<Tweet> tweets;
    
    /**
     * Create a new TweetAdapter.
     * 
     * @param context The Context to use in this Adapter.
     */
    public TweetAdapter(final Context context) {
        if (context == null) {
            throw new IllegalArgumentException("The context must not be null.");
        }
        
        this.context = context;
        this.inflater = LayoutInflater.from(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCount() {
        return tweets != null ? tweets.size() : 0;
    }

    /**
     * {@inheritDoc}
     */
    public Tweet getItem(final int position) {
        return tweets != null && position < tweets.size() ?
                tweets.get(position) : null;
    }

    /**
     * {@inheritDoc}
     */
    public long getItemId(final int position) {
        return position;
    }

    /**
     * {@inheritDoc}
     */
    public View getView(final int position, final View convertView,
            final ViewGroup parent) {
        final View v = convertView == null ?
                inflater.inflate(R.layout.tweet_item, parent, false) :
                convertView;
        final TextView txtBody = (TextView) v.findViewById(R.id.txtBody);
        final TextView txtInfo = (TextView) v.findViewById(R.id.txtInfo);
        final Tweet tweet = getItem(position);
        
        txtBody.setText(Html.fromHtml(tweet.getBody()));
        txtInfo.setText(context.getString(R.string.tweetadapter_info_format,
                tweet.getDisplayName(),
                OUT_DATE_FORMAT.format(tweet.getTime())));
        
        return v;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isEnabled(final int position) {
        return false;
    }
    
    /**
     * Get the Context used in this Adapter.
     * 
     * @return The Context used in this Adapter.
     */
    public Context getContext() {
        return context;
    }
    
    /**
     * Set the List of Tweets to use in this Adapter. Set to null or an empty
     * List if the Adapter is to be empty.
     * 
     * @param tweets The List of Tweets to use in this Adapter.
     */
    public void setTweets(final List<Tweet> tweets) {
        this.tweets = tweets;
        notifyDataSetChanged();
    }
    
    /**
     * Get the List of Tweets used by this Adapter. May be null if the List has
     * not yet been set, or the List has intentionally been set to null.
     * 
     * @return The List of Tweets used by this Adapter. Can be null.
     */
    public List<Tweet> getTweets() {
        return tweets;
    }
}