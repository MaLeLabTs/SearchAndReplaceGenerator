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

//FOCUS DATASET

//    public DataSet getDatasetFocus() {
//        return datasetFocus;
//    }
//
//    public void resetDatasetFocus(){
//        datasetFocus = null;
//    }
//
//    public DataSet updateDatasetFocus(){
//
//    }
//
//    private DataSet focusDivideAndConquerDataset(String individualRegex){
//        //initialize pattern matcher
//        Pattern pattern = Pattern.compile(individualRegex);
//        Matcher individualMatcher = pattern.matcher("");
//
//
//        DataSet focusDataset = new DataSet(this.name, "Focus: "+individualRegex, this.regexTarget);
//        for(Example example : this.examples){
//            focusDataset.getExamples().add(this.focusDivideAndConquerExample(example, individualMatcher));
//        }
//        return focusDataset;
//    }
//
//    private Example focusDivideAndConquerExample(Example example, Matcher individualRegexMatcher) {
//           return this.manipulateDivideAndConquerExample(example, individualRegexMatcher, true);
//    }
public class Example {

    public Example() {
    }

    public Example(Example example) {
        this.string = example.string;
        this.match = new LinkedList<>(example.match);
        this.unmatch = new LinkedList<>(example.unmatch);
        if (example.matchedStrings != null) {
            this.matchedStrings = new LinkedList<>(example.matchedStrings);
        }
        if (example.unmatchedStrings != null) {
            this.unmatchedStrings = new LinkedList<>(example.unmatchedStrings);
        }
    }
    public String string;
    public List<Bounds> match = new LinkedList<>();
    public List<Bounds> unmatch = new LinkedList<>();
    protected transient List<String> matchedStrings = new LinkedList<>();
    protected transient List<String> unmatchedStrings = new LinkedList<>();

    public void addMatchBounds(int bs, int bf) {
        Bounds boundaries = new Bounds(bs, bf);
        match.add(boundaries);
    }

    public void addUnmatchBounds(int bs, int bf) {
        Bounds boundaries = new Bounds(bs, bf);
        unmatch.add(boundaries);
    }

    public int getNumberMatchedChars() {
        return getNumberCharsInsideIntervals(match);
    }

    public int getNumberUnmatchedChars() {
        return getNumberCharsInsideIntervals(unmatch);
    }

    public int getNumberOfChars() {
        return string.length();
    }

    private int getNumberCharsInsideIntervals(List<Bounds> textIntervals) {
        int countChars = 0;
        for (Bounds interval : textIntervals) {
            countChars += (interval.end - interval.start);
        }
        return countChars;
    }

    public void populateAnnotatedStrings() {
        this.matchedStrings.clear();
        for (Bounds bounds : this.match) {
            this.matchedStrings.add(this.string.substring(bounds.start, bounds.end));
        }
        this.unmatchedStrings.clear();
        for (Bounds bounds : this.unmatch) {
            this.unmatchedStrings.add(this.string.substring(bounds.start, bounds.end));
        }
    }

    public List<String> getMatchedStrings() {
        return matchedStrings;
    }

    public List<String> getUnmatchedStrings() {
        return unmatchedStrings;
    }

    public String getString() {
        return string;
    }

    public List<Bounds> getMatch() {
        return match;
    }

    public List<Bounds> getUnmatch() {
        return unmatch;
    }

    public void setString(String string) {
        this.string = string;
    }

    /**
     * Creates a fully annotated example based on the provided matches (bounds)
     * All chars are going to be annotated.
     */
    public void populateUnmatchesFromMatches() {
        this.unmatch.clear();
        //generate unmatches
        int previousMatchFinalIndex = 0;
        for (Bounds oneMatch : this.match) {
            if (oneMatch.start > previousMatchFinalIndex) {
                this.addUnmatchBounds(previousMatchFinalIndex, oneMatch.start);
            }
            previousMatchFinalIndex = oneMatch.end;
        }
        if (previousMatchFinalIndex < string.length()) {
            /*
            the right value of the interval can be equal than the string.lenght
            because the substrings are left-inclusive and right-exclusive
             */
            this.addUnmatchBounds(previousMatchFinalIndex, string.length());
        }
    }

    public int getNumberMatches() {
        return this.match.size();
    }

    public List<String> getAnnotatedStrings() {
        List<String> annotatedStrings = new LinkedList<>();
        for (Bounds bounds : this.getMatch()) {
            annotatedStrings.add(this.getString().substring(bounds.start, bounds.end));
        }
        for (Bounds bounds : this.getUnmatch()) {
            annotatedStrings.add(this.getString().substring(bounds.start, bounds.end));
        }
        return annotatedStrings;
    }

    /**
     * This method generates all the annotated strings; the strings are in the same order
     * they appear into the example. This method has been created in order to mantain the
     * same behavior for the getAnnotatedStrings method (different order).
     * @return
     */
    public List<String> getOrderedAnnotatedStrings() {
        List<Bounds> boundsList = new LinkedList<>(this.getMatch());
        boundsList.addAll(this.getUnmatch());
        Collections.sort(boundsList); 
        List<String> annotatedStrings = new LinkedList<>();
        for (Bounds bounds : boundsList) {
            annotatedStrings.add(this.getString().substring(bounds.start, bounds.end));
        }
        return annotatedStrings;
    }

    public void mergeUnmatchesBounds() {
        this.unmatch = Bounds.mergeBounds(this.unmatch);
    }
    
}
