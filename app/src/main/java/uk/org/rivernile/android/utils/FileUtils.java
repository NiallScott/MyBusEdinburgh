/*
 * Copyright (C) 2014 - 2018 Niall 'Rivernile' Scott
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
package uk.org.rivernile.android.utils;

import androidx.annotation.NonNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class contains static methods for carrying out operations on files.
 * 
 * @author Niall Scott
 */
public final class FileUtils {
    
    /**
     * Intentionally left blank to prevent instance creation.
     */
    private FileUtils() { }
    
    /**
     * Create a checksum for a {@link File}. This could be used to check that a file is of
     * correct consistency.
     *
     * <p>
     *     See:
     *     <a href="http://vyshemirsky.blogspot.com/2007/08/computing-md5-digest-checksum-in-java.html">
     *         http://vyshemirsky.blogspot.com/2007/08/computing-md5-digest-checksum-in-java.html</a>
     * </p>
     *
     * <p>
     *     This has been slightly modified.
     * </p>
     * 
     * @param file The file to run the MD5 checksum against.
     * @return The MD5 checksum string.
     */
    @NonNull
    public static String md5Checksum(@NonNull final File file) throws IOException {
        try {
            final InputStream fin = new FileInputStream(file);
            final MessageDigest md5er = MessageDigest.getInstance("MD5");
            final byte[] buffer = new byte[1024];
            int read;
            
            while ((read = fin.read(buffer)) != -1) {
                if (read > 0) {
                    md5er.update(buffer, 0, read);
                }
            }
            
            fin.close();
            final byte[] digest = md5er.digest();

            if (digest == null) {
                return "";
            }
            
            final StringBuilder builder = new StringBuilder();

            for (byte a : digest) {
                builder.append(Integer.toString((a & 0xff) + 0x100, 16).substring(1));
            }
            
            return builder.toString();
        } catch(NoSuchAlgorithmException e) {
            return "";
        }
    }
}