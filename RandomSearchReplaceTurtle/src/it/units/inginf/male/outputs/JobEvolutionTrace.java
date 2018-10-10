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
 */package it.units.inginf.male.outputs;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author Fabiano
 */
public class JobEvolutionTrace {

    private List<FinalSolution> finalGeneration = new LinkedList<>(); //
    private List<List<Solution>> generationHistory = new LinkedList<>(); //generationHistory[generationNumber][rankingIndex]
    private FinalSolution bestEncounteredSolution;

    private long executionTimeMillis; //generationHistory[generationNumber][rankingIndex]
    private int numberJobGenerations;

    /**
     * Return the new added Generations
     *
     * @return
     */
    public List<Solution> addGeneration() {
        List<Solution> newGeneration = new LinkedList<>();
        this.generationHistory.add(newGeneration);
        return newGeneration;
    }

    public List<Solution> getCurrentGeneration() {
        return generationHistory.get(generationHistory.size() - 1);
    }

    public int getNumberOfRecordedGenerations() {
        return this.generationHistory.size();
    }

    /**
     * Get the best solution over the validation set encountered during the
     * evolution. Could be useful in order to avoid overfitting.
     *
     * @return
     */
    public FinalSolution getBestEncounteredSolution() {
        return this.bestEncounteredSolution;
    }

    /**
     * Use this method to set the best encountered solution in case the target
     * fitness is not the f-measure
     *
     * @param bestEncounteredSolution
     */
    public void setBestEncounteredSolution(FinalSolution bestEncounteredSolution) {
        this.bestEncounteredSolution = bestEncounteredSolution;
    }

    private double bestEncounteredSolutionIndex = Double.NEGATIVE_INFINITY; //Invalid value, default forces best update
    private long bestEncounteredSolutionLength = Long.MAX_VALUE; //Invalid value, default forces best update

    /**
     * Compare the proposed candidate with the previous bestEncounteredSolution.
     * If performanceIndex = (precision+recall)/2 is better (bigger) than the
     * older best the new candidate becomes the new bestEncounteredSolution.
     * When the performanceIndex is the same, the bestCandidate is promoted only
     * when its length is lesser than bestEncounteredSolution. Performances are
     * computed on learning.
     *
     * @param bestCandidate
     */
    public void checkBestCandidateSolution(FinalSolution bestCandidate) {
        
        double der = bestCandidate.getLearningPerformances().get("distance error rate");
        double cer = bestCandidate.getLearningPerformances().get("count error rate");
        int candidateLength = bestCandidate.getSolution().length();
        der = (Double.isNaN(der)) ? 0 : der;
        cer = (Double.isNaN(cer)) ? 0 : cer;
        double candidateIndex = der;

        if ((candidateIndex > bestEncounteredSolutionIndex) || ((candidateIndex == bestEncounteredSolutionIndex) && (bestEncounteredSolutionLength > candidateLength))) {
            bestEncounteredSolutionLength = candidateLength;
            this.bestEncounteredSolution = bestCandidate;
            bestEncounteredSolutionIndex = candidateIndex;
        }
    }

    /**
     * generationNumber is the number of the RECORDED generations. The
     * generations are sampled with a specified interval. See,
     * <code>resultsGenerationSamplingInterval</code> for details. The
     * realGenerationNumber = config.getResultsGenerationSamplingInterval *
     * generationNumber
     *
     * @param generationNumber
     * @return
     */
    public List<Solution> getGeneration(int generationNumber) {
        return generationHistory.get(generationNumber);
    }

    public List<FinalSolution> getFinalGeneration() {
        return finalGeneration;
    }

    public void setFinalGeneration(List<FinalSolution> finalGeneration) {
        this.finalGeneration = finalGeneration;
    }

    public List<List<Solution>> getGenerationHistory() {
        return generationHistory;
    }

    public void setGenerationHistory(List<List<Solution>> generationHistory) {
        this.generationHistory = generationHistory;
    }

    public long getExecutionTime() {
        return executionTimeMillis;
    }

    public void setExecutionTime(long executionTime) {
        this.executionTimeMillis = executionTime;
    }

    public int getNumberJobGenerations() {
        return numberJobGenerations;
    }

    public void setNumberJobGenerations(int numberJobGenerations) {
        this.numberJobGenerations = numberJobGenerations;
    }

}
