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
package it.units.inginf.male.configuration;

import it.units.inginf.male.inputs.DataSetReplace;
import it.units.inginf.male.inputs.ExampleReplace;
import it.units.inginf.male.utils.Range;
import org.supercsv.io.CsvListReader;
import org.supercsv.io.ICsvListReader;
import org.supercsv.prefs.CsvPreference;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

/**
 * Same as usual DatasetContainer but striping has been removed
 * @author Fabiano
 */
public class DatasetContainer {

    private String path;
    private transient DataSetReplace dataset;
    private List<Range> training = new ArrayList<>();
    private List<Range> validation = new ArrayList<>();
    private List<Range> testing = new ArrayList<>();
    private boolean dataSetStriped = false;
    private double datasetStripeMarginSize = Integer.MAX_VALUE; //no slices are done
    private int normalProposedDatasetInterval = 0;
    private transient DataSetReplace trainingDataset;
    private transient DataSetReplace validationDataset;
    private transient DataSetReplace testingDataset;
    private transient DataSetReplace learningDataset;

    public DatasetContainer() {
    }

    public DatasetContainer(DatasetContainer datasetContainer) {
        this.path = datasetContainer.getPath();
        this.dataset = datasetContainer.getDataset();
        this.training = new LinkedList<>(datasetContainer.getTraining());
        this.validation = new LinkedList<>(datasetContainer.getValidation());
        this.testing = new LinkedList<>(datasetContainer.getTesting());
        this.datasetStripeMarginSize = datasetContainer.getDatasetStripeMarginSize();
        this.normalProposedDatasetInterval = datasetContainer.getProposedNormalDatasetInterval();
        this.trainingDataset = datasetContainer.getTrainingDataset();
        this.validationDataset = datasetContainer.getValidationDataset();
        this.testingDataset = datasetContainer.getTestingDataset();
        this.learningDataset = datasetContainer.getLearningDataset();
    }

    /**
     * Initialize the dataset container for the provided dataset. Ranges and
     * sub-dataset are not initialized, you have to set the ranges and call the
     * <code>update</code> instance method in order to generate them. Random
     * generator is initialized to a default value.
     *
     * @param dataset
     */
    public DatasetContainer(DataSetReplace dataset) {
        this(dataset, false, 0);
    }

    /**
     * Initialize the dataset container for the provided dataset. When
     * defaultRanges is true, it initializes automatically training and
     * validation ranges; not less than 50% of matches have to stay in training,
     * 50% in validation. Random generator is initialized to a default value.
     *
     * @param dataset
     * @param defaultRanges
     */
    public DatasetContainer(DataSetReplace dataset, boolean defaultRanges) {
        this(dataset, defaultRanges, 0);
    }

    public DatasetContainer(DataSetReplace dataset, boolean defaultRanges, int defaultRangesSeed) {
        this();
        this.dataset = dataset;
        this.dataset.updateStats();
        this.updateSubDataset();
    }

    public String getPath() {
        return path;
    }

    /**
     * Set new dataset and automatically loads it
     *
     * @param path
     * @throws IOException
     */
    public void setPath(String path) throws IOException {
        this.path = path;
        this.loadDataset();
    }

    public List<Range> getTraining() {
        return training;
    }

    public void setTraining(List<Range> training) {
        this.training = training;
    }

    public List<Range> getValidation() {
        return validation;
    }

    public void setValidation(List<Range> validation) {
        this.validation = validation;
    }

    public List<Range> getTesting() {
        return testing;
    }

    public void setTesting(List<Range> testing) {
        this.testing = testing;
    }

    /**
     * When true, the training dataset includes its reduced version (striped).
     * Update and load operations also affects the linked striped views.
     *
     * @return
     */
    public boolean isDataSetReplaceStriped() {
        return dataSetStriped;
    }

    public void setDataSetReplacesStriped(boolean datasetStriped) {
        this.dataSetStriped = datasetStriped;
    }

    /**
     * <code>normalDataInterval</code> is a proposed value for the number of
     * iterations between the usage of the normal training dataset view; this i
     * meaningful when hasStripedDataSetReplaces is true. This value does not affect
     * the DatasetContatiner behavior; The strategy is supposed to read this
     * value and change its behavior accordingly. The strategy has to inform the
     * context when the striped dataset version is needed and ALWAYS use the
     * Context.getCurrentDataset() method in order to access the right dataset
     * view.
     *
     * @return
     */
    public int getProposedNormalDatasetInterval() {
        return normalProposedDatasetInterval;
    }

    public void setProposedNormalDatasetInterval(int unstripedDatasetInterval) {
        this.normalProposedDatasetInterval = unstripedDatasetInterval;
    }

    public DataSetReplace getDataset() {//throws IOException {
//        if(dataset == null){
//            this.loadDataset();
//        }
        return dataset;
    }

    public DataSetReplace getTrainingDataset() {
        return trainingDataset;
    }

    public void setTrainingDataset(DataSetReplace trainingDataset) {
        this.trainingDataset = trainingDataset;
    }

    public DataSetReplace getValidationDataset() {
        return validationDataset;
    }

    public void setValidationDataset(DataSetReplace validationDataset) {
        this.validationDataset = validationDataset;
    }

    public DataSetReplace getTestingDataset() {
        return testingDataset;
    }

    public void setTestingDataset(DataSetReplace testingDataset) {
        this.testingDataset = testingDataset;
    }

    public DataSetReplace getLearningDataset() {
        return learningDataset;
    }

    public double getDatasetStripeMarginSize() {
        return datasetStripeMarginSize;
    }

    public void setDatasetStripeMarginSize(double datasetStripeMarginSize) {
        this.datasetStripeMarginSize = datasetStripeMarginSize;
    }

    /**
     * Forces reloading of the dataset from file; the dataset path url is the
     * instance <code>path</code> property
     *
     * @throws IOException
     */
    public void loadDataset() throws IOException {
        this.dataset = new DataSetReplace();
        FileInputStream fis = new FileInputStream(new File(this.path));
        InputStreamReader isr = new InputStreamReader(fis, "UTF-8");

        ICsvListReader reader = new CsvListReader(isr, CsvPreference.STANDARD_PREFERENCE);
        reader.getHeader(true);

        List<String> columns;
        while((columns =reader.read()) != null){
            if(columns.size() != 2){
                throw new RuntimeException();
            }
            ExampleReplace example = new ExampleReplace(columns.get(0),columns.get(1));
            this.dataset.examples.add(example);
        }

        this.dataset.updateStats();
        this.updateSubDataset();
    }

    //this method regenerate the sub-datasets from the provided ranges definitions.
    public void updateSubDataset() {
        this.trainingDataset = this.dataset.subDataset("training", training);
        this.validationDataset = this.dataset.subDataset("validation", validation);
        this.testingDataset = this.dataset.subDataset("testing", testing);
        this.trainingDataset.updateStats();
        this.validationDataset.updateStats();
        this.testingDataset.updateStats();

        this.learningDataset = new DataSetReplace("learning");
        this.learningDataset.getExamples().addAll(this.trainingDataset.getExamples());
        this.learningDataset.getExamples().addAll(this.validationDataset.getExamples());
        this.learningDataset.updateStats();

    }
}
