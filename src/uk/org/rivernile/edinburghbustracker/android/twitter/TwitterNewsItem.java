/*
 * Copyright (C) 2012 - 2013 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.twitter;

import android.annotation.SuppressLint;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * This class is a bean class to hold data for a news item from Twitter.
 * 
 * @author Niall Scott
 */
public final class TwitterNewsItem {
    
    @SuppressLint({"SimpleDateFormat"})
    private static final SimpleDateFormat inDateFormat =
            new SimpleDateFormat("EEE MMM dd HH:mm:ss ZZZZZ yyyy",
                    Locale.ENGLISH);
    private static final SimpleDateFormat outDateFormat =
            new SimpleDateFormat("EEE dd MMM yyyy HH:mm:ss");
    
    private String body;
    private String poster;
    private Date date = new Date();
    
    /**
     * Create a new Twitter news item.
     * 
     * @param body The news text.
     * @param poster The name of the account which posted.
     * @param date The date of the post, as given by the Twitter API.
     */
    public TwitterNewsItem(final String body, final String poster,
            final String date) {
        setBody(body);
        setPoster(poster);
        setDate(date);
    }
    
    /**
     * Get the news text.
     * 
     * @return The news text.
     */
    public String getBody() {
        return body;
    }
    
    /**
     * Set the news text.
     * 
     * @param body The news text.
     */
    public void setBody(final String body) {
        if(body == null || body.length() == 0)
            throw new IllegalArgumentException("The body must not be null or " +
                    "blank.");
        
        this.body = body;
    }
    
    /**
     * Get the name of the account which posted this news item.
     * 
     * @return The name of the account which posted this news item
     */
    public String getPoster() {
        return poster;
    }
    
    /**
     * Set the name of the account which posted this news item.
     * 
     * @param poster The name of the account which posted this news item.
     */
    public void setPoster(final String poster) {
        if(poster == null || poster.length() == 0)
            throw new IllegalArgumentException("The poster must not be null " +
                    "or blank.");
        
        this.poster = poster;
    }
    
    /**
     * Get the date formatted as a human readable String.
     * 
     * @return The date.
     */
    public String getDate() {
        return outDateFormat.format(date);
    }
    
    /**
     * Set the date, as given by the Twitter API.
     * 
     * @param date The date.
     */
    public void setDate(final String date) {
        try {
            this.date = inDateFormat.parse(date);
        } catch(ParseException e) {
            
        }
    }
}