/*
 * Copyright (C) 2014 Niall 'Rivernile' Scott
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

package uk.org.rivernile.android.bustracker.parser.livetimes;

import android.text.TextUtils;
import java.util.List;

/**
 * A Journey represents a bus travelling from some origin to a destination with
 * the calling points in-between. A journey will have some sort of ID. The
 * journey will be carried out by a given service name and have an operator. It
 * will also have an destination/end point.
 * 
 * @author Niall Scott
 * @param <T> The implementation-specific type of {@link JourneyDeparture} that
 * this class will hold.
 */
public abstract class Journey<T extends JourneyDeparture> {
    
    private final String journeyId;
    private final String serviceName;
    private final List<T> departures;
    
    /**
     * Create a new Journey.
     * 
     * @param journeyId The unique ID of the journey. Cannot be null or empty.
     * @param serviceName The public visible name of the service. Cannot be null
     * or empty.
     * @param departures A List of {@link JourneyDeparture}s for this journey.
     * This cannot be null.
     */
    public Journey(final String journeyId, final String serviceName,
            final List<T> departures) {
        if (TextUtils.isEmpty(journeyId)) {
            throw new IllegalArgumentException("The journeyId must not be null "
                    + "or empty.");
        }
        
        if (TextUtils.isEmpty(serviceName)) {
            throw new IllegalArgumentException("The serviceName must not be "
                    + "null or empty.");
        }
        
        if (departures == null) {
            throw new IllegalArgumentException("The departures must not be "
                    + "null");
        }
        
        this.journeyId = journeyId;
        this.serviceName = serviceName;
        this.departures = departures;
        
    }
    
    /**
     * Get the ID of this journey.
     * 
     * @return The ID of this journey.
     */
    public String getJourneyId() {
        return journeyId;
    }

    /**
     * Get the name of the service completing this journey.
     * 
     * @return The name of the service completing this journey.
     */
    public String getServiceName() {
        return serviceName;
    }
    
    /**
     * Get the List of departures for this journey.
     * 
     * @return The List of departures for this journey. This will never be null.
     */
    public List<T> getDepartures() {
        return departures;
    }

    /**
     * Get the name of the operator performing the journey.
     * 
     * Not all real-time systems will support this field. In this case, null or
     * an empty String should be returned.
     * 
     * @return The name of the operator performing the journey. Could be null
     * or an empty String.
     */
    public abstract String getOperator();

    /**
     * Get a textual description of the route.
     * 
     * Not all real-time systems will support this field. In this case, null or
     * an empty String should be returned.
     * 
     * @return A textual description of the route. Could be null or an empty
     * String.
     */
    public abstract String getRoute();

    /**
     * Get the name of the destination of the journey.
     * 
     * Not all real-time systems will support this field. In this case, null or
     * an empty String should be returned.
     * 
     * @return The name of the destination of the journey. Could be null or an
     * empty String.
     */
    public abstract String getDestination();

    /**
     * Get the stopCode of the terminus stop.
     * 
     * Not all real-time systems will support this field. In this case, null or
     * an empty String should be returned.
     * 
     * @return The stopCode of the terminus stop.
     */
    public abstract String getTerminus();
    
    /**
     * Is there a disruption that affects the whole network or the real-time
     * system?
     * 
     * Not all real-time systems will support this flag. In this case,
     * subclasses should return false here in all instances.
     * 
     * @return true if there is a global disruption in place, false if not.
     */
    public abstract boolean hasGlobalDisruption();
    
    /**
     * Is the service that this journey is served by disrupted?
     * 
     * Not all real-time systems will support this flag. In this case,
     * subclasses should return false here in all instances.
     * 
     * @return true if there is a disruption in place for this service, false if
     * not.
     */
    public abstract boolean hasServiceDisruption();
    
    /**
     * Is the service that this journey is served by diverted?
     * 
     * Not all real-time systems will support this flag. In this case,
     * subclasses should return false here in all instances.
     * 
     * @return true if this service is diverted, false if not.
     */
    public abstract boolean hasServiceDiversion();
}