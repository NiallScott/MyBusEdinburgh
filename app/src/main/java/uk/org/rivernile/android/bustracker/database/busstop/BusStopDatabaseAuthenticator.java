/*
 * Copyright (C) 2016 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.database.busstop;

import android.accounts.AbstractAccountAuthenticator;
import android.accounts.Account;
import android.accounts.AccountAuthenticatorResponse;
import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;

/**
 * This {@link AbstractAccountAuthenticator} exists to satisfy the requirements of the sync
 * framework and is stubbed out as there is no concept of accounts for syncing the database.
 *
 * @author Niall Scott
 */
class BusStopDatabaseAuthenticator extends AbstractAccountAuthenticator {

    /**
     * Create a new {@code BusStopDatabaseAuthenticator}.
     *
     * @param context A {@link Context} instance.
     */
    BusStopDatabaseAuthenticator(@NonNull final Context context) {
        super(context);
    }

    @Override
    public Bundle editProperties(final AccountAuthenticatorResponse response,
            final String accountType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle addAccount(final AccountAuthenticatorResponse response, final String accountType,
            final String authTokenType, final String[] requiredFeatures, final Bundle options)
            throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle confirmCredentials(final AccountAuthenticatorResponse response,
            final Account account,
            final Bundle options) throws NetworkErrorException {
        return null;
    }

    @Override
    public Bundle getAuthToken(final AccountAuthenticatorResponse response, final Account account,
            final String authTokenType, final Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public String getAuthTokenLabel(final String authTokenType) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle updateCredentials(final AccountAuthenticatorResponse response,
            final Account account,
            final String authTokenType, final Bundle options) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Bundle hasFeatures(final AccountAuthenticatorResponse response, final Account account,
            final String[] features) throws NetworkErrorException {
        throw new UnsupportedOperationException();
    }
}
