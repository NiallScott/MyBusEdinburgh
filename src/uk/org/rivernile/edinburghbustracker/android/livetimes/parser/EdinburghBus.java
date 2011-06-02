/*
 * Copyright (C) 2011 Niall 'Rivernile' Scott
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

package uk.org.rivernile.edinburghbustracker.android.livetimes.parser;

import java.util.Calendar;
import java.util.Date;
import uk.org.rivernile.android.bustracker.parser.livetimes.Bus;

public class EdinburghBus extends Bus {
    
    private Date arrivalObj;
    
    public EdinburghBus(final String serviceName, final String destination,
            String arrivalTime) throws IllegalArgumentException {
        super(serviceName, destination, arrivalTime);
        
        Calendar time = Calendar.getInstance();
        if(arrivalTime.charAt(0) == '*')
            arrivalTime = arrivalTime.substring(1);
        try {
            time.add(Calendar.MINUTE, Integer.parseInt(arrivalTime));
        } catch(NumberFormatException e) {
            if(arrivalTime.indexOf(':') != -1) {
                time.set(Calendar.HOUR_OF_DAY,
                    Integer.parseInt(arrivalTime.split(":")[0]));
                time.set(Calendar.MINUTE,
                    Integer.parseInt(arrivalTime.split(":")[1]));
                if(time.compareTo(Calendar.getInstance()) < 0)
                    time.add(Calendar.DAY_OF_WEEK, 1);
            } else if(!arrivalTime.equals("DUE")) {
                time.add(Calendar.DAY_OF_WEEK, 2);
            }
        }
        arrivalObj = time.getTime();
    }
    
    public Date getArrivalDateObject() {
        return arrivalObj;
    }
}