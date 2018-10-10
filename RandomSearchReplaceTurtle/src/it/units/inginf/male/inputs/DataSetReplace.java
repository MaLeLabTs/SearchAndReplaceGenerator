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
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.logging.Logger;

/**
 * New dataset structure, this is intended to be serialized in Json format using
 * Gson
 *
 * @author Fabiano
 */
public class DataSetReplace {

    public DataSetReplace() {
    }

    public DataSetReplace(String name) {
        this.name = name;
    }

    public DataSetReplace(String name, String description, String regexTarget, String replaceExpressionTarget) {
        this.name = name;
        this.description = description;
        this.regexTarget = regexTarget;
        this.replaceExpressionTarget = replaceExpressionTarget;
    }

    public String name;
    public String description;
    public String regexTarget;
    public String replaceExpressionTarget;
    public List<ExampleReplace> examples = new LinkedList<>();

    private transient int numberOfChars;
    private transient int editDistance;
    private transient int numberReplacements;

    private final static Logger LOG = Logger.getLogger(DataSetReplace.class.getName());

    /**
     * Updates the dataset statistics, numberMatches, numberMatchesChars and so
     * on
     */
    public void updateStats() {
        
        this.numberOfChars = 0;
        this.editDistance = 0;
        this.numberReplacements = 0;
        
        for (ExampleReplace ex : this.examples) {
            this.numberOfChars += ex.getNumberOfChars();
            this.editDistance += ex.getEditDistance();
            if(ex.isReplacement()){
                this.numberReplacements++;
            }
        }
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

    public int getNumberOfChars() {
        return numberOfChars;
    }

    public void setNumberOfChars(int numberOfChars) {
        this.numberOfChars = numberOfChars;
    }

    public int getEditDistance() {
        return editDistance;
    }

    public void setEditDistance(int editDistance) {
        this.editDistance = editDistance;
    }

    public int getNumberReplacements() {
        return numberReplacements;
    }

    public void setNumberReplacements(int numberReplacements) {
        this.numberReplacements = numberReplacements;
    }

    
    public String getStatsString() {
        StringBuilder stats = new StringBuilder();
        stats.append("DataSet ").append(this.name).append(" stats:\n")
                .append("number examples: ").append(this.getNumberExamples())
                .append("\noverall chars in dataset: ").append(this.getNumberOfChars())
                .append("\noverall edit distance: ").append(this.getEditDistance());
        return stats.toString();
    }

    public void randomize() {
        Random random = new Random();
        for (int i = 0; i < examples.size(); i++) {
            if ((((i * 100) / examples.size()) % 5) == 0) {
                System.out.format("randomize %d%%\n", ((i * 100) / examples.size()));
            }
            ExampleReplace ex1;
            ExampleReplace ex2;
            int destIndex = random.nextInt(examples.size());
            ex1 = examples.get(i);
            ex2 = examples.get(destIndex);
            examples.set(i, ex2);
            examples.set(destIndex, ex1);
        }
    }

    public List<ExampleReplace> getExamples() {
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
    public DataSetReplace subDataset(String name, List<Range> ranges) {
        // ranges are inclusive
        DataSetReplace subDataset = new DataSetReplace(name);
        for (Range range : ranges) {
            for (int index = range.getStartIndex(); index <= range.getEndIndex(); index++) {
                subDataset.getExamples().add(this.getExamples().get(index));
            }
        }
        return subDataset;
    }

    public ExampleReplace getExample(int index) {
        return examples.get(index);
    }
}
