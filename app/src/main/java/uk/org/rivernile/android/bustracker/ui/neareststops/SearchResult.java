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

package uk.org.rivernile.android.bustracker.ui.neareststops;

import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

/**
 * This object is used to hold data for a nearby bus stop.
 *
 * @author Niall Scott
 */
class SearchResult implements Parcelable, Comparable<SearchResult> {

    private final String stopCode;
    private final String stopName;
    private final String services;
    private final float distance;
    private final int orientation;
    private final String locality;

    /**
     * Create a new {@code SearchResult}.
     *
     * @param stopCode The stop code of the stop.
     * @param stopName The name of the stop.
     * @param services A displayable listing of the services that serve this stop.
     * @param distance The distance the device is from the stop, at the time of loading the data.
     * @param orientation The orientation of the stop, expressed between {@code 0...8}, with
     * {@code 0} being north and {@code 7} being north-west, going clockwise.
     * @param locality The name of the local area where the stop resides.
     */
    SearchResult(@NonNull final String stopCode, @Nullable final String stopName,
            @Nullable final String services, final float distance, final int orientation,
            @Nullable final String locality) {
        if (TextUtils.isEmpty(stopCode)) {
            throw new IllegalArgumentException("stopCode must not be null or empty.");
        }

        this.stopCode = stopCode;
        this.stopName = stopName;
        this.services = services;
        this.distance = distance;
        this.orientation = orientation;
        this.locality = locality;
    }

    /**
     * Create a new instance of this object from a serialised {@link Parcel}.
     *
     * @param parcel The {@link Parcel} to construct this object from.
     */
    private SearchResult(@NonNull final Parcel parcel) {
        stopCode = parcel.readString();
        stopName = parcel.readString();
        services = parcel.readString();
        distance = parcel.readFloat();
        orientation = parcel.readInt();
        locality = parcel.readString();
    }

    /**
     * Get the stop code of the stop.
     *
     * @return The stop code of the stop.
     */
    @NonNull
    public String getStopCode() {
        return stopCode;
    }

    /**
     * Get the name of the stop.
     *
     * @return The name of the stop.
     */
    @Nullable
    public String getStopName() {
        return stopName;
    }

    /**
     * Get a displayable listing of the services that serve this stop.
     *
     * @return A displayable listing of the services that serve this stop.
     */
    @Nullable
    public String getServices() {
        return services;
    }

    /**
     * Get the distance the device is from the stop, at the time of loading the data.
     *
     * @return The distance the device is from the stop, at the time of loading the data.
     */
    public float getDistance() {
        return distance;
    }

    /**
     * Get the orientation of the stop, expressed between {@code 0...8}, with {@code 0} being
     * north and {@code 7} being north-west, going clockwise.
     *
     * @return The orientation of the stop.
     */
    public int getOrientation() {
        return orientation;
    }

    /**
     * Get the name of the local area where the stop resides.
     *
     * @return The name of the local area where the stop resides.
     */
    @Nullable
    public String getLocality() {
        return locality;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        dest.writeString(stopCode);
        dest.writeString(stopName);
        dest.writeString(services);
        dest.writeFloat(distance);
        dest.writeInt(orientation);
        dest.writeString(locality);
    }

    @Override
    public int compareTo(@NonNull final SearchResult another) {
        return distance != another.distance ?
                (distance > another.distance ? 1 : -1) : 0;
    }

    @Override
    public boolean equals(final Object o) {
        if (this == o) {
            return true;
        }

        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        final SearchResult that = (SearchResult) o;

        return stopCode.equals(that.stopCode);
    }

    @Override
    public int hashCode() {
        return stopCode.hashCode();
    }

    /**
     * This is required by the platform to construct this {@link Parcelable} object from a
     * serialised {@link Parcel}.
     */
    public static final Creator<SearchResult> CREATOR = new Creator<SearchResult>() {
        @Override
        public SearchResult createFromParcel(final Parcel source) {
            return new SearchResult(source);
        }

        @Override
        public SearchResult[] newArray(final int size) {
            return new SearchResult[size];
        }
    };
}
