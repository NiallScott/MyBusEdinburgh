/*
 * Copyright (C) 2013 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.fetchers;

import android.text.TextUtils;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * A FileFetcher fetches data from a given File or path. The data is then passed
 * in to an instance of a FetcherStreamReader. This class takes care of opening
 * and closing the file.
 * 
 * @author Niall Scott
 */
public class FileFetcher implements Fetcher {
    
    private final File file;
    
    /**
     * Create a new instance of FileFetcher, specifying the path of the file.
     * 
     * @param filePath The path to the file that is to be read from. Cannot be
     * null or empty.
     */
    public FileFetcher(final String filePath) {
        if (TextUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("filePath must not be null or "
                    + "empty.");
        }
        
        file = new File(filePath);
    }
    
    /**
     * Create a new instance of FileFetcher, specifying the File to read from.
     * 
     * @param file The File to read from. Cannot be null.
     */
    public FileFetcher(final File file) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null.");
        }
        
        this.file = file;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeFetcher(final FetcherStreamReader reader)
            throws IOException {
        if (reader == null) {
            throw new IllegalArgumentException("The reader cannot be null.");
        }
        
        FileInputStream in = null;
        
        try {
            in = new FileInputStream(file);
            reader.readInputStream(in);
        } catch (IOException e) {
            // Re-throw the Exception, so that the 'finally' clause is
            // executed.
            throw e;
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    // Nothing to do here.
                }
            }
        }
    }
    
    /**
     * Get the File that this instance uses to read data from.
     * 
     * @return A File object.
     */
    public File getFile() {
        return file;
    }
}