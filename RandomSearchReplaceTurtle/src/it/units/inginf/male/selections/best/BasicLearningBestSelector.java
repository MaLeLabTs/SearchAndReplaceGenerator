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

package it.units.inginf.male.selections.best;

import it.units.inginf.male.outputs.FinalSolution;
import it.units.inginf.male.outputs.JobEvolutionTrace;
import it.units.inginf.male.outputs.Results;
import java.util.Map;

/**
 * Picks one individual per job (the best one using the evolution fitness); then it evaluates their performance
 * on the learning set. The individual with the best performance is promoted as the best one.. 
 * 
 * @author Fabiano
 */
public class BasicLearningBestSelector implements BestSelector {  
    /**
     * You can assign values CER (Count error rate) or DER (Distance error rate)
     */
    public static final String SELECTION_INDEX_PROPERTY_NAME = "selectionindex";
    public enum SelectionIndex {
        DER,
        CER
    }
    private SelectionIndex selectionIndex;
    
    @Override
    public void setup(Map<String, String> parameters) {
        String selectionIndexString = parameters == null ? "DER" : parameters.getOrDefault(SELECTION_INDEX_PROPERTY_NAME, "DER");
        switch(selectionIndexString.toUpperCase()){
            case "DER": selectionIndex = SelectionIndex.DER; break;
            case "CER": selectionIndex = SelectionIndex.CER; break;
            default: throw(new RuntimeException("Error in BestSelector, invalid parameter value"));
        }               
    }

    @Override
    public void elaborate(Results results) {
        this.selectAndPopulateBest(results);
    }
    
    private void selectAndPopulateBest(Results results) {
        double bestIndividualIndex = Double.POSITIVE_INFINITY;
        int bestLength = Integer.MAX_VALUE;
        FinalSolution best = null;
        for (JobEvolutionTrace jobEvolutionTrace : results.getJobEvolutionTraces()) {
            FinalSolution bestOfJob = jobEvolutionTrace.getFinalGeneration().get(0);
            double der = bestOfJob.getLearningPerformances().get("distance error rate");
            double cer = bestOfJob.getLearningPerformances().get("count error rate");
            int bestJobLength = bestOfJob.getSolution().length();
            der = (Double.isNaN(der))? 1.0 : der;
            cer = (Double.isNaN(cer))? 1.0 : cer;
            
            double individualIndex = selectionIndex.equals(SelectionIndex.DER) ? der : cer;
            
            if ((individualIndex < bestIndividualIndex) || ((individualIndex == bestIndividualIndex) && (bestLength > bestJobLength))) {
                    bestLength = bestJobLength;
                    best = bestOfJob;
                    bestIndividualIndex = individualIndex;
            }
            
        }
        results.setBestSolution(best);
        if (best != null) {
            System.out.println("Best on validation: " + best.getSolution());
            System.out.println("******Stats on training******");
            System.out.println("Distance error rate: " + best.getTrainingPerformances().get("distance error rate"));
            System.out.println("Count error rate: " + best.getTrainingPerformances().get("count error rate"));
            System.out.println("******Stats on validation******");
            System.out.println("Distance error rate: " + best.getValidationPerformances().get("distance error rate"));
            System.out.println("Count error rate: " + best.getValidationPerformances().get("count error rate"));
            System.out.println("******Stats on learning******");
            System.out.println("Distance error rate: " + best.getLearningPerformances().get("distance error rate"));
            System.out.println("Count error rate: " + best.getLearningPerformances().get("count error rate"));
            System.out.println("******Stats on testing******");
            System.out.println("Distance error rate: " + best.getTestingPerformances().get("distance error rate"));
            System.out.println("Count error rate: " + best.getTestingPerformances().get("count error rate"));
        }
    }
}
