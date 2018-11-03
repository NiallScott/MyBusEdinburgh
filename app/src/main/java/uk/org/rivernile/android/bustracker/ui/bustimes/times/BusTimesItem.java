/*
 * Copyright (C) 2017 - 2018 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.ui.bustimes.times;

import androidx.annotation.NonNull;

import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBus;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveBusService;

/**
 * This class holds data to represent a row in the {@link BusTimesAdapter}.
 *
 * @author Niall Scott
 */
class BusTimesItem {

    private final LiveBusService liveBusService;
    private final LiveBus liveBus;
    private final int position;

    /**
     * Create a new {@code BusTimesItem}.
     *
     * @param liveBusService The {@link LiveBusService} for the group of rows.
     * @param liveBus The {@link LiveBus} for the row.
     * @param position The position of the item within the group.
     */
    BusTimesItem(@NonNull final LiveBusService liveBusService, @NonNull final LiveBus liveBus,
            final int position) {
        this.liveBusService = liveBusService;
        this.liveBus = liveBus;
        this.position = position;
    }

    /**
     * Get the {@link LiveBusService} for the group.
     *
     * @return The {@link LiveBusService} for the group.
     */
    @NonNull
    LiveBusService getLiveBusService() {
        return liveBusService;
    }

    /**
     * The {@link LiveBus} for the row.
     *
     * @return The {@link LiveBus} for the row.
     */
    @NonNull
    LiveBus getLiveBus() {
        return liveBus;
    }

    /**
     * Is this item a group parent?
     *
     * @return {@code true} if the item is a group parent, {@code false} if not.
     */
    boolean isParent() {
        return position == 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final BusTimesItem item = (BusTimesItem) o;

        return liveBusService.equals(item.liveBusService) &&
                position == item.position;
    }

    @Override
    public int hashCode() {
        int result = liveBusService.hashCode();
        result = 31 * result + position;
        return result;
    }
}
