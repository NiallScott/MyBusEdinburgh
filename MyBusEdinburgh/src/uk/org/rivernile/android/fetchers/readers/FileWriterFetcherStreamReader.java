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

package uk.org.rivernile.android.fetchers.readers;

import android.text.TextUtils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import uk.org.rivernile.android.fetchers.FetcherStreamReader;

/**
 * A FileWriterFetcherStreamReader will take an InputStream and pipe the input
 * out to the given file. This could be useful, for example, if data was being
 * transferred over HTTP and needed to be piped out to a file on disk.
 * 
 * @author Niall Scott
 */
public class FileWriterFetcherStreamReader implements FetcherStreamReader {
    
    private final File file;
    private final boolean append;
    
    /**
     * Create a new FileWriterFetcherStreamReader.
     * 
     * @param file The File that the data will be written out to. Must not be
     * null.
     * @param append true if the data should be appended to the end of the file,
     * false if the file should be overwritten.
     */
    public FileWriterFetcherStreamReader(final File file,
            final boolean append) {
        if (file == null) {
            throw new IllegalArgumentException("file must not be null.");
        }
        
        this.file = file;
        this.append = append;
    }
    
    /**
     * Create a new FileWriterFetcherStreamReader.
     * 
     * @param filePath The path to the file that data will be written to. Must
     * not be null or empty.
     * @param append true if the data should be appended to the end of the file,
     * false if the file should be overwritten.
     */
    public FileWriterFetcherStreamReader(final String filePath,
            final boolean append) {
        if (TextUtils.isEmpty(filePath)) {
            throw new IllegalArgumentException("filePath must not be null or "
                    + "empty.");
        }
        
        file = new File(filePath);
        this.append = append;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void readInputStream(final InputStream stream) throws IOException {
        if (stream == null) {
            throw new IllegalArgumentException("The stream must not be null.");
        }
        
        final FileOutputStream out = new FileOutputStream(file, append);
        final byte[] buf = new byte[1024];
        int len;

        while ((len = stream.read(buf)) > 0) {
            out.write(buf, 0, len);
        }

        out.flush();
        out.close();
    }
    
    /**
     * Get a File object, describing the file that the data will be written out
     * to.
     * 
     * @return A File object, describing the file that the data will be written
     * out to.
     */
    public File getFile() {
        return file;
    }
    
    /**
     * Does output to the file append or overwrite?
     * 
     * @return true if the data is appended to the file, false if it is
     * overwritten.
     */
    public boolean doesAppend() {
        return append;
    }
}