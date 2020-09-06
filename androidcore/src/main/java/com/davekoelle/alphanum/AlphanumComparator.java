/*
 * The Alphanum Algorithm is an improved sorting algorithm for strings
 * containing numbers.  Instead of sorting numbers in ASCII order like
 * a standard sort, this algorithm sorts numbers in numeric order.
 *
 * The Alphanum Algorithm is discussed at http://www.DaveKoelle.com
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301 USA
 *
 */

package com.davekoelle.alphanum;

import java.util.Comparator;

/**
 * This is an updated version with enhancements made by Daniel Migowski,
 * Andre Bogus, and David Koelle.
 *
 * This class has been modified by Niall Scott for better code formatting and
 * other enhancements.
 */
public class AlphanumComparator<T> implements Comparator<T> {

    @Override
    public int compare(final T o1, final T o2) {
        // Get the Strings by calling toString() on the passed in objects.
        final String s1 = o1.toString();
        final String s2 = o2.toString();
        
        int thisMarker = 0;
        int thatMarker = 0;
        // Cache their lengths to avoid looking this up later.
        final int s1Length = s1.length();
        final int s2Length = s2.length();

        // Keep looping until the end of either String is reached.
        while (thisMarker < s1Length && thatMarker < s2Length) {
            final String thisChunk = getChunk(s1, thisMarker);
            thisMarker += thisChunk.length();

            final String thatChunk = getChunk(s2, thatMarker);
            thatMarker += thatChunk.length();

            // If both chunks contain numeric characters, sort them numerically.
            int result;

            if (Character.isDigit(thisChunk.charAt(0)) &&
                    Character.isDigit(thatChunk.charAt(0))) {
                // Simple chunk comparison by length.
                final int thisChunkLength = thisChunk.length();
                result = thisChunkLength - thatChunk.length();

                // If equal, the first different number counts.
                if (result == 0) {
                    for (int i = 0; i < thisChunkLength; i++) {
                        result = thisChunk.charAt(i) - thatChunk.charAt(i);

                        if (result != 0) {
                            return result;
                        }
                    }
                }
            } else {
                result = thisChunk.compareTo(thatChunk);
            }

            if (result != 0) {
                return result;
            }
        }

        return s1Length - s2Length;
    }
    
    /**
     * Split the String in to chunks of digits and non-digits.
     * 
     * @param s The String to get the chunk from.
     * @param marker The index of the String to start looking at.
     * @return A chunk of digits or non-digits.
     */
    private static String getChunk(final String s, int marker) {
        if (s == null) {
            throw new IllegalArgumentException("String is null.");
        }
        
        // Cache the character array to avoid repeated calls to String.charAt()
        final char[] chars = s.toCharArray();
        final int len = chars.length;

        if (marker < 0 || marker > (len - 1)) {
            throw new IllegalArgumentException("marker is invalid.");
        }
        
        final StringBuilder chunk = new StringBuilder();
        // The first character will always appear in the chunk.
        chunk.append(chars[marker]);
        marker++;
        
        if (Character.isDigit(chars[marker - 1])) {
            // If first character is a digit, keep appending characters until we
            // encounter a non-digit.
            while (marker < len) {
                if (!Character.isDigit(chars[marker])) {
                    break;
                }

                chunk.append(chars[marker]);
                marker++;
            }
        } else {
            // If first character is a non-digit, keep appending character until
            // we encounter a digit.
            while (marker < len) {
                if (Character.isDigit(chars[marker])) {
                    break;
                }

                chunk.append(chars[marker]);
                marker++;
            }
        }
        
        return chunk.toString();
    }
}
