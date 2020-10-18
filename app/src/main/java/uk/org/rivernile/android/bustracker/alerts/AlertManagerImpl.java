/*
 * Copyright (C) 2011 - 2020 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.alerts;

import android.content.Context;
import android.content.Intent;

import javax.inject.Inject;
import javax.inject.Singleton;

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;

import androidx.core.content.ContextCompat;
import uk.org.rivernile.android.bustracker.core.alerts.arrivals.ArrivalAlertRunnerService;
import uk.org.rivernile.android.bustracker.core.alerts.proximity.ProximityAlertRunnerService;
import uk.org.rivernile.android.bustracker.database.settings.loaders.AddProximityAlertTask;
import uk.org.rivernile.android.bustracker.database.settings.loaders.AddTimeAlertTask;

/**
 * This is a concrete implementation of {@link AlertManager}.
 * 
 * @author Niall Scott
 * @deprecated This is currently being re-written.
 */
@Singleton
public class AlertManagerImpl implements AlertManager {
    
    private final Context context;
    
    /**
     * Create a new instance of {@link AlertManagerImpl}.
     * 
     * @param context The {@link android.app.Application} {@link Context}.
     */
    @Inject
    AlertManagerImpl(@NonNull final Context context) {
        this.context = context;
    }

    @Override
    public void addProximityAlert(@NonNull final String stopCode,
            @IntRange(from = 1) final int distance) {
        AddProximityAlertTask.start(context, stopCode, distance);
        final Intent intent = new Intent(context, ProximityAlertRunnerService.class);
        ContextCompat.startForegroundService(context, intent);
    }

    @Override
    public void addTimeAlert(@NonNull final String stopCode, @NonNull final String[] services,
            @IntRange(from = 0) final int timeTrigger) {
        // Add a new time alert to the database.
        AddTimeAlertTask.start(context, stopCode, services, timeTrigger);
        final Intent intent = new Intent(context, ArrivalAlertRunnerService.class);
        ContextCompat.startForegroundService(context, intent);
    }
}