/*
 * Copyright (C) 2011 - 2018 Niall 'Rivernile' Scott
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.json.JSONException;
import org.json.JSONObject;

import uk.org.rivernile.android.bustracker.parser.livetimes.AuthenticationException;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.bustracker.parser.livetimes.MaintenanceException;
import uk.org.rivernile.android.bustracker.parser.livetimes.ServerErrorException;
import uk.org.rivernile.android.bustracker.parser.livetimes.SystemOverloadedException;
import uk.org.rivernile.android.utils.JSONUtils;

/**
 * This class checks for an error response from the Edinburgh real time service and if so, turns the
 * error in to a {@link LiveTimesException}.
 *
 * @author Niall Scott
 */
class ErrorParser {

    private static final String ERROR_INVALID_APP_KEY = "INVALID_APP_KEY";
    private static final String ERROR_INVALID_PARAMETER = "INVALID_PARAMETER";
    private static final String ERROR_PROCESSING_ERROR = "PROCESSING_ERROR";
    private static final String ERROR_SYSTEM_MAINTENANCE = "SYSTEM_MAINTENANCE";
    private static final String ERROR_SYSTEM_OVERLOADED = "SYSTEM_OVERLOADED";

    /**
     * This constructor is private and empty to prevent instantiation.
     */
    private ErrorParser() { }

    /**
     * Check the {@link JSONObject} for an error response. If there is an error, this is turned in
     * to a {@link LiveTimesException} (or one of its subclasses). If there is no error,
     * {@code null} is returned.
     *
     * @param joRoot The {@link JSONObject} to check.
     * @return A {@link LiveTimesException} (or one of its subclasses) if there was an error, or
     * {@code null} otherwise.
     */
    @Nullable
    static LiveTimesException getExceptionIfError(@NonNull final JSONObject joRoot) {
        if (!joRoot.has("faultcode")) {
            return null;
        }

        final String faultCode;

        try {
            faultCode = JSONUtils.getString(joRoot, "faultcode");
        } catch (JSONException e) {
            // This should never happen because of the check for "faultcode" above.
            return null;
        }

        if (ERROR_INVALID_APP_KEY.equals(faultCode)) {
            return new AuthenticationException("The API key was not accepted by the server.");
        } else if (ERROR_INVALID_PARAMETER.equals(faultCode) ||
                ERROR_PROCESSING_ERROR.equals(faultCode)) {
            return new ServerErrorException();
        } else if (ERROR_SYSTEM_MAINTENANCE.equals(faultCode)) {
            return new MaintenanceException();
        } else if (ERROR_SYSTEM_OVERLOADED.equals(faultCode)) {
            return new SystemOverloadedException();
        } else {
            return new LiveTimesException("An unknown error occurred.");
        }
    }
}
