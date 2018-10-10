/*
 * Copyright (C) 2018 Machine Learning Lab - University of Trieste, 
 * Italy (http://machinelearning.inginf.units.it/)  
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package it.units.inginf.male.inputs;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author fab
 */
public class Bounds implements Comparable<Bounds> {

    public Bounds(int start, int end) {
        this.start = start;
        this.end = end;
    }

    public int start;
    public int end;

    public int size() {
        return this.end - this.start;
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 97 * hash + this.start;
        hash = 97 * hash + this.end;
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final Bounds other = (Bounds) obj;
        return (this.end == other.end) && (this.start == other.start);
    }

    /**
     * Convert a list of bounds in a more compact representation; this works
     * with overlapping intervals too. Remind that bounds define ranges with
     * start inclusive and end exclusive index. This method generate new Bound
     * objects.
     *
     * @param boundsList
     * @return
     */
    public static List<Bounds> mergeBounds(List<Bounds> boundsList) {
        List<Bounds> newBoundsList = new LinkedList<>();
        if (boundsList.isEmpty()) {
            return newBoundsList;
        }
        Collections.sort(boundsList);
        Bounds prevBounds = new Bounds(boundsList.get(0).start, boundsList.get(0).end);
        for (int i = 1; i < boundsList.size(); i++) {
            Bounds currentBounds = boundsList.get(i);
            if (currentBounds.start <= prevBounds.end) {
                //merge
                prevBounds.end = Math.max(currentBounds.end, prevBounds.end);
            } else {
                newBoundsList.add(prevBounds);
                prevBounds = new Bounds(currentBounds.start, currentBounds.end);
            }
        }
        newBoundsList.add(prevBounds);
        return newBoundsList;
    }

    /**
     * Creates a new Bounds object representing this interval relative a
     * rangeBounds interval. --i.e: this [4,9] ; this.windowView([2,7]) == [2,7]
     * When this Bounds object is out of the window, windowView returns null.
     *
     * @param rangeBounds
     * @return
     */
    public Bounds windowView(Bounds rangeBounds) {
        Bounds newBounds = new Bounds(this.start - rangeBounds.start, this.end - rangeBounds.start);
        if ((newBounds.start >= rangeBounds.size()) || (newBounds.end <= 0)) {
            return null; //Out of window
        }
        newBounds.start = Math.max(newBounds.start, 0);
        newBounds.end = Math.min(newBounds.end, rangeBounds.size());
        return newBounds;
    }

    @Override
    public int compareTo(Bounds o) {
        return this.start - o.start;
    }

    public boolean isSubsetOf(Bounds bounds) {
        return (this.start >= bounds.start) && (this.end <= bounds.end);
    }

    /**
     * When this Bounds objects overlaps the bounds argument (range intersection
     * is not empty) returns true; otherwise returns false. Zero length bounds
     * are considered overlapping when they adhere each other --i.e: [3,5] and
     * [5,5] are considered overlapping [3,3] and [3,8] are overlapping ranges.
     *
     * @param bounds The range to compare, for intersections, with the calling
     * instance
     * @return true when the ranges overlap
     */
    public boolean overlaps(Bounds bounds) {
        return this.start == bounds.start || this.end == bounds.end || ((this.start < bounds.end) && (this.end > bounds.start));
    }

    //extracted ranges are always ordered collection. In case you have to sort the collection first.
    //AnnotatedRanges are sorted internally by the method
    /**
     * Counts the number of checkedRanges that overlaps with the zoneRanges. A
     * Bounds object in checkedRanges who doesn't overlap with zoneRanges are
     * not counted.
     *
     * @param ranges
     * @param zoneRanges
     * @return
     */
    public static int countRangesThatCollideZone(List<Bounds> ranges, List<Bounds> zoneRanges) {
        int overallEOAA = 0;
        Collections.sort(zoneRanges);
        //this is a verynaive approach, no time to optimize
        //This approach relies on the fact that both annotatedRanges and extracted ranges are ordered
        for (Bounds extractedBounds : ranges) {
            for (Bounds expectedBounds : zoneRanges) {
                if (expectedBounds.start >= extractedBounds.end) {
                    break;
                }
                if (extractedBounds.overlaps(expectedBounds)) {
                    overallEOAA++;
                    break;
                }
            }
        }
        return overallEOAA;
    }

    public int getOverlappingCharsNumber(Bounds target) {
        int d = Math.min(target.end, this.end) - Math.max(target.start, this.start);
        if (d < 0) {
            d = 0;
        }

        return d;
    }

    public boolean overlaps(List<Bounds> boundsList){
       boolean overlaps = false;
       for(Bounds b : boundsList){
            overlaps |= this.overlaps(b);
            if(overlaps){
                break;
            }
       }
       return overlaps;
    }
    
}
