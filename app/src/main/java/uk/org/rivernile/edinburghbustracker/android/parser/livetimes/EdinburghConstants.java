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

/**
 * This class contains static fields which are constants returned by the Edinburgh real-time system.
 * 
 * @author Niall Scott
 */
public final class EdinburghConstants {
    
    /** This field is used when the reliability is unknown. */
    public static final char RELIABILITY_UNKNOWN = 0;
    /** Denotes a bus which is delayed. */
    public static final char RELIABILITY_DELAYED = 'B';
    /** Denotes when a bus has been delocated. */
    public static final char RELIABILITY_DELOCATED = 'D';
    /** Denotes a bus which is real-time but not low floor */
    public static final char RELIABILITY_REAL_TIME_NOT_LOW_FLOOR = 'F';
    /** Denotes a bus which is real-time and is low floor */
    public static final char RELIABILITY_REAL_TIME_LOW_FLOOR = 'H';
    /** Denotes a bus which is immobilised? Broken down perhaps? */
    public static final char RELIABILITY_IMMOBILISED = 'I';
    /** Denotes a bus which is neutralised? The army got to it? */
    public static final char RELIABILITY_NEUTRALISED = 'N';
    /** Denotes a bus which has a radio fault. */
    public static final char RELIABILITY_RADIO_FAULT = 'R';
    /** Denotes a bus for which real-time tracking is not available. */
    public static final char RELIABILITY_ESTIMATED = 'T';
    /** Denotes a bus which has been diverted. */
    public static final char RELIABILITY_DIVERTED = 'V';
    
    /** This field is used when the type is unknown. */
    public static final char TYPE_UNKNOWN = 0;
    /** Denotes this stop is a terminus on this bus route. */
    public static final char TYPE_TERMINUS = 'D';
    /** Denotes this stop is a normal stop on this bus route. */
    public static final char TYPE_NORMAL = 'N';
    /** Denotes this service is part route. */
    public static final char TYPE_PART_ROUTE = 'P';
    /** Denotes this stop is a timing reference stop on this bus route. */
    public static final char TYPE_REFERENCE = 'R';
}