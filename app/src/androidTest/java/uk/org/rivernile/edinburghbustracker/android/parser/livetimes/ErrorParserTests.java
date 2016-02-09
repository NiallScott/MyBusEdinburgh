/*
 * Copyright (C) 2014 - 2016 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.parser.livetimes;

import android.support.annotation.NonNull;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.IOException;

import uk.org.rivernile.android.bustracker.parser.livetimes.AuthenticationException;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.bustracker.parser.livetimes.MaintenanceException;
import uk.org.rivernile.android.bustracker.parser.livetimes.ServerErrorException;
import uk.org.rivernile.android.bustracker.parser.livetimes.SystemOverloadedException;
import uk.org.rivernile.android.fetchutils.fetchers.AssetFileFetcher;
import uk.org.rivernile.android.fetchutils.fetchers.readers.JSONFetcherStreamReader;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link ErrorParser}.
 *
 * @author Niall Scott
 */
@RunWith(AndroidJUnit4.class)
public class ErrorParserTests {

    /**
     * Test that {@link ErrorParser#getExceptionIfError(org.json.JSONObject)} returns
     * {@code null} when the JSON object is set as an empty object.
     *
     * @throws Exception When there was a problem reading the JSON file from assets. This is not
     * expected.
     */
    @Test
    public void testParseErrorWithEmptyJsonObject() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/generic/empty_object.json");
        final LiveTimesException exception =
                ErrorParser.getExceptionIfError(reader.getJSONObject());
        assertNull(exception);
    }

    /**
     * Test that {@link ErrorParser#getExceptionIfError(org.json.JSONObject)} returns a
     * {@link LiveTimesException} when the JSON object is set as an empty object.
     *
     * @throws Exception When there was a problem reading the JSON file from assets. This is not
     * expected.
     */
    @Test
    public void testParseErrorWithUnknownError() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_not_known.json");
        final LiveTimesException exception =
                ErrorParser.getExceptionIfError(reader.getJSONObject());
        assertNotNull(exception);
    }

    /**
     * Test that {@link ErrorParser#getExceptionIfError(org.json.JSONObject)} returns an
     * {@link AuthenticationException} when the error denotes an invalid API key.
     *
     * @throws Exception When there was a problem reading the JSON file from assets. This is not
     * expected.
     */
    @Test
    public void testParseErrorAppKey() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_invalid_app_key.json");
        final LiveTimesException exception =
                ErrorParser.getExceptionIfError(reader.getJSONObject());
        assertNotNull(exception);
        assertTrue(exception instanceof AuthenticationException);
    }

    /**
     * Test that {@link ErrorParser#getExceptionIfError(org.json.JSONObject)} returns a
     * {@link ServerErrorException} when the error denotes an invalid parameter.
     *
     * @throws Exception When there was a problem reading the JSON file from assets. This is not
     * expected.
     */
    @Test
    public void testParseErrorInvalidParameter() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_invalid_parameter.json");
        final LiveTimesException exception =
                ErrorParser.getExceptionIfError(reader.getJSONObject());
        assertNotNull(exception);
        assertTrue(exception instanceof ServerErrorException);
    }

    /**
     * Test that {@link ErrorParser#getExceptionIfError(org.json.JSONObject)} returns a
     * {@link ServerErrorException} when the error denotes a processing error.
     *
     * @throws Exception When there was a problem reading the JSON file from assets. This is not
     * expected.
     */
    @Test
    public void testParseErrorProcessingError() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_processing_error.json");
        final LiveTimesException exception =
                ErrorParser.getExceptionIfError(reader.getJSONObject());
        assertNotNull(exception);
        assertTrue(exception instanceof ServerErrorException);
    }

    /**
     * Test that {@link ErrorParser#getExceptionIfError(org.json.JSONObject)} returns a
     * {@link MaintenanceException} when the error denotes that the system is down for maintenance.
     *
     * @throws Exception When there was a problem reading the JSON file from assets. This is not
     * expected.
     */
    @Test
    public void testParseErrorSystemMaintenance() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_system_maintenance.json");
        final LiveTimesException exception =
                ErrorParser.getExceptionIfError(reader.getJSONObject());
        assertNotNull(exception);
        assertTrue(exception instanceof MaintenanceException);
    }

    /**
     * Test that {@link ErrorParser#getExceptionIfError(org.json.JSONObject)} returns a
     * {@link SystemOverloadedException} when the error denotes the system is overloaded.
     *
     * @throws Exception When there was a problem reading the JSON file from assets. This is not
     * expected.
     */
    @Test
    public void testParseErrorSystemOverloaded() throws Exception {
        final JSONFetcherStreamReader reader = getReaderAfterFetchingData(
                "endpoints/bustracker/generic/error_system_overloaded.json");
        final LiveTimesException exception =
                ErrorParser.getExceptionIfError(reader.getJSONObject());
        assertNotNull(exception);
        assertTrue(exception instanceof SystemOverloadedException);
    }

    /**
     * Get the data from assets.
     *
     * @param filePath The file path in the assets to get data from.
     * @return The data.
     * @throws IOException When there was a problem reading the data.
     */
    @NonNull
    private JSONFetcherStreamReader getReaderAfterFetchingData(@NonNull final String filePath)
            throws IOException {
        final AssetFileFetcher fetcher =
                new AssetFileFetcher(InstrumentationRegistry.getContext(), filePath);
        final JSONFetcherStreamReader reader = new JSONFetcherStreamReader();
        fetcher.executeFetcher(reader);

        return reader;
    }
}
