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

package uk.org.rivernile.edinburghbustracker.android;

import uk.org.rivernile.android.bustracker.FragmentFactory;
import uk.org.rivernile.android.bustracker.ui.bustimes
        .DisplayStopDataFragment;
import uk.org.rivernile.edinburghbustracker.android.ui.bustimes
        .EdinburghDisplayStopDataFragment;

/**
 * The EdinburghFragmentFactory returns the correct Fragments for the
 * Edinburgh-specific application.
 * 
 * @author Niall Scott
 */
public final class EdinburghFragmentFactory implements FragmentFactory {
    
    /**
     * This constructor exists to prevent instantiation outside this package.
     */
    EdinburghFragmentFactory() {
        super();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public DisplayStopDataFragment getDisplayStopDataFragment(
            final String stopCode) {
        return EdinburghDisplayStopDataFragment.newInstance(stopCode);
    }
}