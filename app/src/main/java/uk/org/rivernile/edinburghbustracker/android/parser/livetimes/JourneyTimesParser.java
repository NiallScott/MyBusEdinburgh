/*
 * Copyright (C) 2011 - 2016 Niall 'Rivernile' Scott
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

import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import uk.org.rivernile.android.bustracker.parser.livetimes.Journey;
import uk.org.rivernile.android.bustracker.parser.livetimes.JourneyDeparture;
import uk.org.rivernile.android.bustracker.parser.livetimes.LiveTimesException;
import uk.org.rivernile.android.utils.JSONUtils;

/**
 * This class contains static methods to turn the JSON response for {@code getJourneyTimes} from the
 * Edinburgh bus tracker API in to an object graph.
 *
 * @author Niall Scott
 */
class JourneyTimesParser {

    /**
     * This constructor is private and empty to prevent instantiation.
     */
    private JourneyTimesParser() { }

    /**
     * Parse a whole journey times response.
     *
     * @param joRoot The {@link JSONObject} containing the journey times data.
     * @return A {@link Journey} object with its data supplied by {@code joRoot} and its children.
     * @throws LiveTimesException When there was a problem parsing the response.
     */
    @NonNull
    static Journey parseJourneyTimes(@NonNull final JSONObject joRoot)
            throws LiveTimesException {
        final LiveTimesException exception = ErrorParser.getExceptionIfError(joRoot);

        if (exception != null) {
            throw exception;
        }

        try {
            final JSONArray jaJourneyTimes = joRoot.getJSONArray("journeyTimes");

            if (jaJourneyTimes.length() == 0) {
                throw new LiveTimesException("There are no journey times.");
            }

            final Journey journey = parseJourney(jaJourneyTimes.getJSONObject(0));

            if (journey == null) {
                throw new LiveTimesException("The journey could not be parsed.");
            } else {
                return journey;
            }
        } catch (JSONException e) {
            throw new LiveTimesException(e);
        }
    }

    /**
     * Parse a whole {@link Journey} from a single journey object.
     *
     * @param joJourney The {@link JSONObject} containing the journey details.
     * @return A {@link Journey} object with its data supplied by {@code joJourney}, or
     * {@code null} if there was a problem handling the data (e.g. {@code null}/empty fields).
     */
    @Nullable
    protected static Journey parseJourney(
            @NonNull final JSONObject joJourney) {
        try {
            final JSONArray jaDepartures = joJourney.getJSONArray("journeyTimeDatas");
            final int departuresLen = jaDepartures.length();
            final List<JourneyDeparture> departures;

            if (departuresLen > 0) {
                final ArrayList<JourneyDeparture> tempDepartures = new ArrayList<>(departuresLen);
                EdinburghJourneyDeparture departure;

                for (int i = 0; i < departuresLen; i++) {
                    try {
                        departure = parseJourneyDeparture(jaDepartures.getJSONObject(i));

                        if (departure != null) {
                            tempDepartures.add(departure);
                        }
                    } catch (JSONException e) {
                        // Do nothing.
                    }
                }

                Collections.sort(tempDepartures);
                departures = Collections.unmodifiableList(tempDepartures);
            } else {
                departures = Collections.emptyList();
            }

            return new Journey.Builder()
                    .setJourneyId(JSONUtils.getString(joJourney, "journeyId"))
                    .setServiceName(EdinburghParser.serviceNameConversion(
                            JSONUtils.getString(joJourney, "mnemoService")))
                    .setDepartures(departures)
                    .setOperator(JSONUtils.optString(joJourney, "operatorId", null))
                    .setRoute(JSONUtils.optString(joJourney, "nameService", null))
                    .setDestination(JSONUtils.optString(joJourney, "nameDest", null))
                    .setTerminus(JSONUtils.getString(joJourney, "terminus"))
                    .setHasGlobalDisruption(joJourney.optBoolean("globalDisruption"))
                    .setHasServiceDisruption(joJourney.optBoolean("serviceDisruption"))
                    .setHasServiceDiversion(joJourney.optBoolean("serviceDiversion"))
                    .setReceiveTime(SystemClock.elapsedRealtime())
                    .build();
        } catch (JSONException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    /**
     * Parse a single {@link EdinburghJourneyDeparture} from a list of journey departures.
     *
     * @param joDeparture The {@link JSONObject} containing the departure details.
     * @return An {@link EdinburghJourneyDeparture} object with its data supplied by
     * {@code joDeparture}, or {@code null} if there was a problem in handling this data (e.g.
     * {@code null}/empty fields).
     */
    @Nullable
    static EdinburghJourneyDeparture parseJourneyDeparture(@NonNull final JSONObject joDeparture) {
        try {
            final String reliabilityString = JSONUtils.getString(joDeparture, "reliability");
            final String typeString = JSONUtils.getString(joDeparture, "type");

            if (TextUtils.isEmpty(reliabilityString) || TextUtils.isEmpty(typeString)) {
                return null;
            }

            final char reliability = reliabilityString.charAt(0);
            final char type = typeString.charAt(0);
            final int minutes = joDeparture.getInt("minutes");
            final EdinburghJourneyDeparture.Builder builder =
                    new EdinburghJourneyDeparture.Builder();

            builder.setDepartureMinutes(minutes)
                    .setStopCode(JSONUtils.getString(joDeparture, "stopId"))
                    .setStopName(JSONUtils.optString(joDeparture, "stopName", null))
                    .setDepartureTime(EdinburghParser.addMinutes(null, minutes))
                    .setIsBusStopDisrupted(joDeparture.optBoolean("busStopDisruption"))
                    .setIsEstimatedTime(reliability == EdinburghConstants.RELIABILITY_ESTIMATED)
                    .setIsDelayed(reliability == EdinburghConstants.RELIABILITY_DELAYED)
                    .setIsDiverted(reliability == EdinburghConstants.RELIABILITY_DIVERTED)
                    .setIsTerminus(type == EdinburghConstants.TYPE_TERMINUS)
                    .setIsPartRoute(type == EdinburghConstants.TYPE_PART_ROUTE)
                    .setOrder(joDeparture.optInt("order", Integer.MAX_VALUE));

            return builder.build();
        } catch (JSONException e) {
            return null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
