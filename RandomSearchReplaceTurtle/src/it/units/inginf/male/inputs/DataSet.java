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

import it.units.inginf.male.utils.Range;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * New dataset structure, this is intended to be serialized in Json format using
 * Gson
 *
 * @author Fabiano
 */
public class DataSet {

    public DataSet() {
    }

    public DataSet(String name) {
        this.name = name;
    }

    public DataSet(String name, String description, String regexTarget) {
        this.name = name;
        this.description = description;
        this.regexTarget = regexTarget;
    }

    public String name;
    public String description;
    public String regexTarget;
    public List<Example> examples = new LinkedList<>();

    private transient int numberMatches;
    private transient int numberUnmatches;
    private transient int numberMatchedChars;
    private transient int numberUnmatchedChars;
    private transient int numberUnAnnotatedChars;
    private transient int numberOfChars;

    private transient DataSet stripedDataset;

    private transient Map<Long, List<DataSet>> divideAndConquerLevels = new ConcurrentHashMap<>();
    //private transient DataSet datasetFocus = null;
    private final static Logger LOG = Logger.getLogger(DataSet.class.getName());

    /**
     * Updates the dataset statistics, numberMatches, numberMatchesChars and so
     * on
     */
    public void updateStats() {
        this.numberMatches = 0;
        this.numberUnmatches = 0;
        this.numberMatchedChars = 0;
        this.numberUnmatchedChars = 0;
        this.numberUnAnnotatedChars = 0;
        this.numberOfChars = 0;

        for (Example ex : this.examples) {
            this.numberMatches += ex.match.size();
            this.numberUnmatches += ex.unmatch.size();
            this.numberMatchedChars += ex.getNumberMatchedChars();
            this.numberUnmatchedChars += ex.getNumberUnmatchedChars();
            this.numberOfChars += ex.getNumberOfChars();
        }
        this.numberUnAnnotatedChars = this.numberOfChars - this.numberMatchedChars - this.numberUnmatchedChars;
    }

    public int getNumberMatches() {
        return numberMatches;
    }

    public int getNumberUnmatches() {
        return numberUnmatches;
    }

    public int getNumberMatchedChars() {
        return numberMatchedChars;
    }

    public int getNumberUnmatchedChars() {
        return numberUnmatchedChars;
    }

    public int getNumberUnannotatedChars() {
        return numberUnAnnotatedChars;
    }

    public int getNumberOfChars() {
        return numberOfChars;
    }

    public int getNumberExamples() {
        return this.examples.size();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getRegexTarget() {
        return regexTarget;
    }

    public void setRegexTarget(String regexTarget) {
        this.regexTarget = regexTarget;
    }

    public String getStatsString() {
        StringBuilder stats = new StringBuilder();
        stats.append("DataSet ").append(this.name).append(" stats:\n")
                .append("number examples: ").append(this.getNumberExamples())
                .append("\noverall chars in dataset: ").append(this.getNumberOfChars())
                .append("\nnumber matches: ").append(this.getNumberMatches())
                .append("\ncharacters in matches: ").append(this.getNumberMatchedChars())
                .append("\nnumber unmatches: ").append(this.getNumberUnmatches())
                .append("\ncharacters in unmatches: ").append(this.getNumberUnmatchedChars())
                .append("\nunannotated chars: ").append(this.getNumberUnannotatedChars());
        return stats.toString();
    }

    public void randomize() {
        Random random = new Random();
        for (int i = 0; i < examples.size(); i++) {
            if ((((i * 100) / examples.size()) % 5) == 0) {
                System.out.format("randomize %d%%\n", ((i * 100) / examples.size()));
            }
            Example ex1;
            Example ex2;
            int destIndex = random.nextInt(examples.size());
            ex1 = examples.get(i);
            ex2 = examples.get(destIndex);
            examples.set(i, ex2);
            examples.set(destIndex, ex1);
        }
    }

    public void populateUnmatchesFromMatches() {
        for (Example ex : this.examples) {
            ex.populateUnmatchesFromMatches();
        }
    }

    /**
     * Populate examples with the temporary list of matched and unmatched
     * strings (using current match and unmatch bounds)
     */
    public void populateAnnotatedStrings() {
        for (Example example : this.examples) {
            example.populateAnnotatedStrings();
        }
    }

    public List<Example> getExamples() {
        return this.examples;
    }

    /**
     * Create a dataset which is a "view" of the current dataset.A subset of the
     * dataset defined by ranges.
     *
     * @param name
     * @param ranges
     * @return
     */
    public DataSet subDataset(String name, List<Range> ranges) {
        // ranges are inclusive
        DataSet subDataset = new DataSet(name);
        for (Range range : ranges) {
            for (int index = range.getStartIndex(); index <= range.getEndIndex(); index++) {
                subDataset.getExamples().add(this.getExamples().get(index));
            }
        }
        return subDataset;
    }

    /**
     *
     * @param marginSize is the how much the margin is bigger than match size.
     * marginSize = 3 means the number of characters in margins is three times
     * the match characters.
     * @return
     */
    public DataSet initStripedDatasetView(double marginSize) {
        this.stripedDataset = new DataSet(this.name, this.description, this.regexTarget);
        for (Example example : this.examples) {
            this.stripedDataset.getExamples().addAll(this.stripeExample(example, marginSize));
        }
        return this.stripedDataset;
    }

    /**
     * creates a list of examples created from examples by splitting example in
     * littler pieces. The pieces are created from a window surrounding the
     * example matches. When two or more windows overlaps, the windows merge
     * together and a single multi-match example is created.
     *
     * @param example
     * @param marginSize
     * @return
     */
    protected static List<Example> stripeExample(Example example, double marginSize) {
        List<Example> slicesExampleList = new LinkedList<>();
        //create ranges covering the saved portions 
        List<Bounds> savedBounds = new LinkedList<>();
        for (Bounds match : example.getMatch()) {
            //double -> int cast works like Math.floor(); Gives the same results of the older integer version
            int charMargin = (int) Math.max(((match.size() * marginSize) / 2.0), 1.0);
            Bounds grownMatch = new Bounds(match.start - charMargin, match.end + charMargin);
            grownMatch.start = (grownMatch.start >= 0) ? grownMatch.start : 0;
            grownMatch.end = (grownMatch.end <= example.getNumberOfChars()) ? grownMatch.end : example.getNumberOfChars();
            savedBounds.add(grownMatch);
        }
        //compact bounds, create a compact representation of saved portions
        //This bounds represents the slices we are going to cut the example to 
        savedBounds = Bounds.mergeBounds(savedBounds);

        //Create examples from slices
        for (Bounds slice : savedBounds) {
            Example sliceExample = new Example();
            sliceExample.setString(example.getString().substring(slice.start, slice.end));

            //find owned matches
            for (Bounds match : example.getMatch()) {
                if (match.start >= slice.end) {
                    break; //only the internal for
                } else {
                    Bounds slicedMatch = match.windowView(slice);
                    if (slicedMatch != null) {
                        sliceExample.getMatch().add(slicedMatch);
                    }
                }
            }
            //find owned unmatches
            for (Bounds unmatch : example.getUnmatch()) {
                if (unmatch.start >= slice.end) {
                    break; //only the internal for
                } else {
                    Bounds slicedUnmatch = unmatch.windowView(slice);
                    if (slicedUnmatch != null) {
                        sliceExample.getUnmatch().add(slicedUnmatch);
                    }
                }
            }

            sliceExample.populateAnnotatedStrings();
            slicesExampleList.add(sliceExample);
        }
        return slicesExampleList;
    }

    /**
     * Return the last initialized striped version of this DataSet. In order to
     * change the window size you have to call initStripedDatasetView() again.
     *
     * @return
     */
    public DataSet getStripedDataset() {
        /*if(this.stripedDataset == null){
              LOG.info("getStripedDataset returns, null dataset cause uninitialized striped dataset.");
          }*/
        return this.stripedDataset;
    }

    /**
     * Returns the Dividi Et Impera sub-dataset of the requested level. Level 0
     * is the original dataset, level 1 is the dataset obtained from the first
     * reduction, level 2 is the dataset from the second reduction and so on.
     *
     * @param divideEtImperaLevel
     * @param jobId
     * @return
     */
    public DataSet getDivideAndConquerDataSet(int divideEtImperaLevel, int jobId) {
        if (divideEtImperaLevel == 0) {
            return this;
        }
        return getDivideAndConquerLevels(jobId).get(divideEtImperaLevel - 1);
    }

    /**
     * Divide dataset, defaults to converting match to unmatches and text
     * extraction problem.
     *
     * @param individualRegex
     * @param jobId
     * @return
     */
    public boolean addDivideAndConquerLevel(String individualRegex, int jobId) {
        return this.addDivideAndConquerLevel(individualRegex, jobId, true, false);
    }

    /**
     * Divide dataset, defaults to converting match to unmatches and text
     * extraction problem.
     *
     * @param individualRegex
     * @param jobId
     * @param convertToUnmatch
     * @return
     */
    public boolean addDivideAndConquerLevel(String individualRegex, int jobId, boolean convertToUnmatch) {
        return this.addDivideAndConquerLevel(individualRegex, jobId, convertToUnmatch, false);
    }

    /**
     * From the last generated Dividi Et Impera sub-dataset, creates a new
     * sub-dataset. We evaluate the individualRegex on the dataset examples,
     * matches that are correctly extracted are removed. A removed match is
     * converted to unmatch or unannotated depending on the convertToUnmatch
     * value: True==unmatch In striping mode, the base dataset and striped
     * dataset are reduced separately. There is no direct connection from --i.e
     * the third level of the original dataset and the third level of the
     * striped dataset. The reduced dataset depends only by its parent. NOTE:
     * The levels are the sub-dataset, level 0 is the original dataset, level 1
     * is the sub-dataset reduced from the original dataset, level 2 is a
     * sub-dataset reduced from the level 1 dataset and so on.
     *
     * @param individualRegex
     * @param jobId
     * @param convertToUnmatch when true, the eliminated matches are converted
     * into unmatches, otherwise unannotated area.
     * @param isFlagging
     * @return true, when the dataset has been modified by reduction
     */
    public boolean addDivideAndConquerLevel(String individualRegex, int jobId, boolean convertToUnmatch, boolean isFlagging) {
        boolean modified = false;
        DataSet oldDataset = this.getLastDivideAndConquerDataSet(jobId);
        DataSet dataset = oldDataset.reduceDivideAndConquerDataset(individualRegex, convertToUnmatch, isFlagging);
        dataset.updateStats();
        modified = (dataset.getNumberMatches() != oldDataset.getNumberMatches());
        this.getDivideAndConquerLevels(jobId).add(dataset);
        if (this.getStripedDataset() != null) {
            modified = this.getStripedDataset().addDivideAndConquerLevel(individualRegex, jobId, convertToUnmatch, isFlagging) || modified;
        }
        return modified;
    }

    /**
     * Creates a DataSet (sub-dataset for Dividi Et Impera) from this dataset
     * instance. We evaluate the individualRegex on the dataset examples,
     * matches that are correctly extracted are removed. A removed match is
     * converted to unmatch or unannotated depending on the convertToUnmatch
     * value: True==unmatch
     */
    private DataSet reduceDivideAndConquerDataset(String individualRegex, boolean convertToUnmatch, boolean isFlagging) {
        //initialize pattern matcher
        Pattern pattern = Pattern.compile(individualRegex);
        Matcher individualRegexMatcher = pattern.matcher("");

        DataSet reducedDataset = new DataSet(this.name, "Reduction: " + individualRegex, this.regexTarget);
        for (Example example : this.examples) {
            if (!isFlagging) {
                reducedDataset.getExamples().add(this.reduceDivideAndConquerExample(example, individualRegexMatcher, convertToUnmatch));
            } else {
                reducedDataset.getExamples().add(this.reduceDivideAndConquerFlaggingExample(example, individualRegexMatcher));
            }
        }
        return reducedDataset;
    }

    private boolean isTruePositiveFlaggingExample(Example example, Matcher individualRegexMatcher) {
        try {
            Matcher m = individualRegexMatcher.reset(example.getString());
            return (m.find() && !example.match.isEmpty());
        } catch (StringIndexOutOfBoundsException ex) {
            return false;
            /**
             * Workaround: riferimento BUG: 6984178
             * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6984178 con i
             * quantificatori greedy restituisce una eccezzione invece che
             * restituire un "false".
             */
        }
    }

    private Example reduceDivideAndConquerFlaggingExample(Example example, Matcher individualRegexMatcher) {
        //Negative or unannotated are left unchanged
        if (!isTruePositiveFlaggingExample(example, individualRegexMatcher)) {
            return new Example(example);
        }
        Example unannotatedExample = new Example();
        unannotatedExample.setString(example.getString());
        return unannotatedExample;
    }

    private Example reduceDivideAndConquerExample(Example example, Matcher individualRegexMatcher, boolean convertToUnmatch) {
        return this.manipulateDivideAndConquerExample(example, individualRegexMatcher, convertToUnmatch);
    }

    //creates a reduced (Dividi Et Impera) Example instance from an example and a given Regex Instance 
    //ELIMINATED Feature: When doFocus is true, the method perform focus action instead of examples reduction (Focus action creates the complementary of the reduction in order to focus evolution).
    //When convertToUnmatch is true extracted matches are converted into unannotated. 
    private Example manipulateDivideAndConquerExample(Example example, Matcher individualRegexMatcher, boolean convertToUnmatch) {
        Example exampleClone = new Example(example);
        List<Bounds> extractions = new LinkedList<>();
        try {
            Matcher m = individualRegexMatcher.reset(example.getString());
            while (m.find()) {
                Bounds bounds = new Bounds(m.start(0), m.end(0));
                extractions.add(bounds);
            }
        } catch (StringIndexOutOfBoundsException ex) {
            /**
             * Workaround: riferimento BUG: 6984178
             * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6984178 con i
             * quantificatori greedy restituisce una eccezzione invece che
             * restituire un "false".
             */
        }

        //remove extracted matches
        for (Iterator<Bounds> it = exampleClone.getMatch().iterator(); it.hasNext();) {
            Bounds match = it.next();
            for (Bounds extraction : extractions) {
                //when doFocus is true, match is remove when equals
                if (match.equals(extraction)) {
                    it.remove();
                    if (convertToUnmatch) {
                        exampleClone.getUnmatch().add(match);
                    }
                    break;
                }
                //Optimization
                //extractions are in ascending order.. because find is going to extract in this order
                if (extraction.start > match.end) {
                    break;
                }
            }
        }

        exampleClone.mergeUnmatchesBounds();
        exampleClone.populateAnnotatedStrings();
        return exampleClone;
    }

    public DataSet getLastDivideAndConquerDataSet(int jobId) {
        List<DataSet> datasetsList = this.getDivideAndConquerLevels(jobId);
        if (datasetsList.isEmpty()) {
            return this;
        }
        return datasetsList.get(datasetsList.size() - 1);
    }

    public int getNumberOfDivideAndConquerLevels(int jobId) {
        return this.getDivideAndConquerLevels(jobId).size();
    }

    /**
     * Returns all the Dividi Et Impera sub-datasets lists, for all the current
     * threads. The map resolves a ThreadID to the thread sub-datasets (levels)
     * list.
     *
     * @return
     */
    public Map<Long, List<DataSet>> getAllDivideAndConquerLevels() {
        return this.divideAndConquerLevels;
    }

    /**
     * Returns the list of the sub-datasets created by Dividi Et Impera, for the
     * current thread. The threads (aka active Jobs) have their own
     * sub-datasets. This works right as far as there is a *:1 relation between
     * Jobs and Threads and we clean the generated levels when a new Job is
     * stated. The levels are the sub-dataset, level 0 is the original dataset,
     * level 1 is the sub-dataset reduced from the original dataset, level 2 is
     * a sub-dataset reduced from the level 1 dataset and so on.
     *
     * @param jobId
     * @return The list of sub datasets
     */
    public List<DataSet> getDivideAndConquerLevels(long jobId) {
        if (this.divideAndConquerLevels.containsKey(jobId)) {
            return this.divideAndConquerLevels.get(jobId);
        } else {
            List<DataSet> newDatasetList = new LinkedList<>();
            this.divideAndConquerLevels.put(jobId, newDatasetList);
            return newDatasetList;
        }
    }

    /**
     * Resets the Dividi Et Impera for the current thread only. Deletes all the
     * generated (reduced) sub-datasets for the current thread.
     */
    public void resetDivideAndConquer(long jobId) {
        this.getDivideAndConquerLevels(jobId).clear();
        if (this.getStripedDataset() != null) {
            this.getStripedDataset().getDivideAndConquerLevels(jobId).clear();
        }
    }

    /**
     * Resets the Dividi Et Impera, delete reduced sub-datasets, for all
     * threads.
     */
    public void resetAllDivideAndConquer() {
        this.getAllDivideAndConquerLevels().clear();
    }

    public void removeDivideAndConquerLevel(int jobID) {
        List<DataSet> divideAndConquerLevelsForJob = this.getDivideAndConquerLevels(jobID);
        if (!divideAndConquerLevelsForJob.isEmpty()) {
            divideAndConquerLevelsForJob.remove(divideAndConquerLevelsForJob.size() - 1);
        }
    }

    public Example getExample(int index) {
        return examples.get(index);
    }
}
