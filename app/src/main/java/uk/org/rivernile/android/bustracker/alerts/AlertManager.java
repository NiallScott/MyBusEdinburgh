/*
 * Copyright (C) 2016 - 2021 Niall 'Rivernile' Scott
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

import androidx.annotation.IntRange;
import androidx.annotation.NonNull;
import androidx.annotation.Size;

/**
 * The purpose of the alert manager is to add and remove user set alerts. For example, the user may
 * wish to be notified when they are within a specified distance of a bus stop, or they may wish to
 * be alerted when a bus service is within a specified number of minutes from a bus stop.
 *
 * @author Niall Scott
 * @deprecated This is currently being re-written.
 */
public interface AlertManager {

    /**
     * Add a new time alert. A time alert is fired when any of the services specified by
     * {@code services} is within the number of minutes specified by {@code timeTrigger} of the bus
     * stop specified by {@code stopCode}.
     *
     * @param stopCode The stop code to be notified of time alerts.
     * @param services The user will be notified when any of these services are within the time
     * trigger.
     * @param timeTrigger The alert will be fired when any of the services are this number of
     * minutes or less from the bus stop.
     */
    void addTimeAlert(@NonNull @Size(min = 1) String stopCode,
            @NonNull @Size(min = 1) String[] services, @IntRange(from = 0) int timeTrigger);
}
