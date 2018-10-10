/*
 * Copyright (C) 2015 Machine Learning Lab - University of Trieste, 
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

import it.units.inginf.male.utils.Pair;
import it.units.inginf.male.utils.Triplet;
import it.units.inginf.male.utils.Utils;

public class ExampleReplace {

    public ExampleReplace() {
    }

    public ExampleReplace(ExampleReplace example) {
        this.string = example.string;
        this.targetString = example.string;
//        if (example.changedString != null) {
//            this.changedString = new LinkedList<>(example.changedString);
//        }
    }

    public ExampleReplace(String string, String targetString) {
        this.string = string;
        this.targetString = targetString;
    }

    public String string;
    public String targetString;

    private Pair<String, String> cachedBeforeAndAfter = null;
    private Bounds cachedBeforeAndAfterBounds = null;
    //public transient List<Bounds> changed = new LinkedList<>();
    //protected transient List<String> changedString = new LinkedList<>();

//    public void addMatchBounds(int bs, int bf) {
//        Bounds boundaries = new Bounds(bs, bf);
//        changed.add(boundaries);
//    }

//    public int getNumberMatchedChars() {
//        return getNumberCharsInsideIntervals(changed);
//    }

    public int getNumberOfChars() {
        return string.length();
    }

//    private int getNumberCharsInsideIntervals(List<Bounds> textIntervals) {
//        int countChars = 0;
//        for (Bounds interval : textIntervals) {
//            countChars += (interval.end - interval.start);
//        }
//        return countChars;
//    }

//    public List<String> getMatchedStrings() {
//        return changedString;
//    }

    public String getString() {
        return string;
    }

//    public List<Bounds> getMatch() {
//        return changed;
//    }

    public void setString(String string) {
        this.string = string;
    }

    public int getEditDistance() {
        return Utils.computeLevenshteinDistance(string, targetString);
    }

    /**
     * Returns the portion of the string and targetString strings that
     * starts when a difference between them si seen and stops when the last
     * difference is seen.
     *
     * @return a pair containing <portionString, portionTargetString>, the changed parts
     */
    public Pair<String, String> getChangedBeforeAndAfter() {
        if (cachedBeforeAndAfter != null) {
            return cachedBeforeAndAfter;
        }
        this.updateCached();
        return this.cachedBeforeAndAfter;
    }

    /**
     * Returns the portion of the string and targetString strings that
     * starts when a difference between them si seen and stops when the last
     * difference is seen.
     * This method extends the portion length on left and right in order to
     * grab a bit of context. It takes maximum border/2 characters on left and
     * right.
     *
     * @param border
     * @return a pair containing <portionString, portionTargetString>, the changed parts, extended on left and right
     */
    public Pair<String, String> getChangedBeforeAndAfter(int border) {
        Triplet<String, String, Bounds> changedBeforeAndAfterWithChangedBounds = getChangedBeforeAndAfterWithChangedBounds(border);
        return new Pair<>(changedBeforeAndAfterWithChangedBounds.getFirst(), changedBeforeAndAfterWithChangedBounds.getSecond());       
    }
    
    //The returned bounds are the changed part in the original string
    private Triplet<String, String, Bounds> getChangedBeforeAndAfterWithChangedBounds(int border) {
        if (this.string.equals(this.targetString)) {
            return null;
        }
        int start;
        int maxSpan = Math.min(this.string.length(), this.targetString.length());
        for (start = 0; start < maxSpan; start++) {
            if (this.string.charAt(start) != this.targetString.charAt(start)) {
                break;
            }
        }
        int fromEnd;
        for (fromEnd = 1; fromEnd <= maxSpan && (maxSpan - fromEnd) >= start; fromEnd++) {
            if (this.string.charAt(this.string.length() - fromEnd) != this.targetString.charAt(this.targetString.length() - fromEnd)) {
                break;
            }
        }
        int effectiveStart = Math.max(0, start - border);
        int effectiveEnd = Math.min(this.string.length(), this.string.length() - fromEnd + 1 + border);
        String changedString = this.string.substring(effectiveStart, effectiveEnd);
        Bounds cachedBeforeAndAfterBoundsLocal = new Bounds(effectiveStart, effectiveEnd);
        effectiveEnd = Math.min(this.targetString.length(), this.targetString.length() - fromEnd + 1 + border);
        String changedTargetString = this.targetString.substring(effectiveStart, effectiveEnd);

        return new Triplet(changedString, changedTargetString, cachedBeforeAndAfterBoundsLocal);
    }

    public Bounds getChangedBeforeAndAfterBounds() {
        if (cachedBeforeAndAfterBounds != null) {
            return cachedBeforeAndAfterBounds;
        }
        this.updateCached();
        return this.cachedBeforeAndAfterBounds;
    }
    
    private void updateCached(){
        synchronized(this){
            if (this.cachedBeforeAndAfter == null || this.cachedBeforeAndAfterBounds == null) {
                Triplet<String, String, Bounds> temp = this.getChangedBeforeAndAfterWithChangedBounds(0);
                if(temp != null){
                    this.cachedBeforeAndAfter = new Pair<>(temp.getFirst(), temp.getSecond());
                    this.cachedBeforeAndAfterBounds = temp.getThird();
                }
            }
        }
    }
    
    public boolean isReplacement(){
        return !this.string.equals(this.targetString);
    }
//    public int getNumberMatches() {
//        return this.changed.size();
//    }
//    
}
