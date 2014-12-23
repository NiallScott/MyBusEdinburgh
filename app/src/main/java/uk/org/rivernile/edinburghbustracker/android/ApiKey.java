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

package uk.org.rivernile.edinburghbustracker.android;

import android.annotation.SuppressLint;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * This class contains API keys used for various services in the app.
 */
public final class ApiKey {

    /**
     * The API key used by BugSense.
     */
    public static final String BUGSENSE_KEY = BuildConfig.BUGSENSE_KEY;

    @SuppressLint({"SimpleDateFormat"})
    private static final SimpleDateFormat formatter;

    static {
        formatter = new SimpleDateFormat("yyyyMMddHH");
        formatter.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    /**
     * This constructor has intentionally been left blank so that this class cannot be instantiated.
     */
    private ApiKey() {

    }

    /**
     * Get the hashed version of the API key.
     *
     * @return The hashed version of the API key.
     */
    public static String getHashedKey() {
        final String combinedKey = BuildConfig.API_KEY + formatter.format(new Date());

        try {
            final MessageDigest m = MessageDigest.getInstance("MD5");
            m.update(combinedKey.getBytes(), 0, combinedKey.length());
            String hashedKey = new BigInteger(1, m.digest()).toString(16);

            while(hashedKey.length() < 32) {
                hashedKey = "0" + hashedKey;
            }

            return hashedKey;
        } catch(NoSuchAlgorithmException e) {
            return "";
        }
    }
}