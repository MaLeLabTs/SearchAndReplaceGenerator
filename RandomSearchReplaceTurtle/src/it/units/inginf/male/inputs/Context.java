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

import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.configuration.DatasetContainer;
import java.util.Random;

/**
 *
 * @author andrea
 */
public class Context {

    public enum EvaluationPhases {

        TRAINING, VALIDATION, TESTING, LEARNING;
    }

    //Looks not useful
    //private DataSet dataSet;

    private EvaluationPhases phase;
    private Random random;
    private Configuration configuration;
    private long seed;

    public Context(EvaluationPhases phase, final Configuration configuration) {
        this.phase = phase;
        this.configuration = configuration;
        this.seed = configuration.getInitialSeed();
        this.random = new Random(this.seed);
    }

    public Random getRandom() {
        return random;
    }

    public EvaluationPhases getPhase() {
        return phase;
    }

    public void setPhase(EvaluationPhases phase) {
        this.phase = phase;
    }

//    public DataSet getDataSet() {
//        return dataSet;
//    }
//
//    public int getDataSetLength() {
//        return this.dataSet.getNumberExamples();
//
//    }
    
    public int getCurrentDataSetLength() {
        return this.getCurrentDataSet().getNumberExamples();
    }

    public long getSeed() {
        return seed;
    }
    
    public Configuration getConfiguration() {
        return configuration;
    }
    
    public DatasetContainer getDataSetContainer() {
        return this.configuration.getDatasetContainer();
    }
    
    /**
     * Returns the dataset view for the current phase; the phases are TRAINING,VALIDATION,TESTING.
     * The returned dataset depends also by the isStripedPhase property.When true, the training set is
     * returned in a more compact version. Let's see the DataSet class for more info.
     * @return
     */
    public DataSetReplace getCurrentDataSet(){
        switch(this.phase){
            //NOTE: only training dataset admits a striped dataset version
            case TESTING: 
                    return this.getDataSetContainer().getTestingDataset();
            case TRAINING:
                    DataSetReplace trainingDataset;
                    trainingDataset = this.getDataSetContainer().getTrainingDataset();
                    return trainingDataset;
            case VALIDATION: return this.getDataSetContainer().getValidationDataset();
            case LEARNING: return this.getDataSetContainer().getLearningDataset();
            default : throw new UnsupportedOperationException("unhandled phase in getDataSet");
        }
        
    }

    @Override
    public String toString() {
        return this.phase.toString();
    }
 
    /**
     * Returns the full training dataset.(this is the original training, not the striped or other sub-datasets)
     * @return
     */
    public DataSetReplace getTrainingDataset(){
        return configuration.getDatasetContainer().getTrainingDataset();
    }
    
     
}
