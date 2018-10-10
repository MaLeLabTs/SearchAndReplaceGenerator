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
package it.units.inginf.male.strategy.impl;

import it.units.inginf.male.coevolution.Forest;
import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.evaluators.CachedEvaluator;
import it.units.inginf.male.evaluators.CachedReplaceEvaluator;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.utils.CacheInterface;
import it.units.inginf.male.objective.Objective;
import it.units.inginf.male.objective.Ranking;
import it.units.inginf.male.objective.performance.PerformacesObjective;
import it.units.inginf.male.objective.performance.PerformancesFactory;
import it.units.inginf.male.outputs.FinalSolution;
import it.units.inginf.male.outputs.JobEvolutionTrace;
import it.units.inginf.male.outputs.Results;
import it.units.inginf.male.outputs.Solution;
import it.units.inginf.male.strategy.ExecutionListener;
import it.units.inginf.male.strategy.ExecutionListenerFactory;
import it.units.inginf.male.strategy.ExecutionStrategy;
import it.units.inginf.male.strategy.RunStrategy;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.utils.Utils;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * A textual interface that works only on Unix systems. Uses the ANSI escape
 * sequence 0x1B+"[2J" to clear the screen. Handy for experiments that take a
 * long time.
 *
 * @author andrea
 */
public class CoolTextualExecutionListener implements ExecutionListener, ExecutionListenerFactory {

    private final static Logger LOG = Logger.getLogger(DefaultExecutionListener.class.getName());
    private final Map<Integer, String> screen = new TreeMap<>();
    private final Map<Integer, Long> jobStartTimes = new ConcurrentHashMap<>();
    private final NavigableSet<Integer> remove = new TreeSet<>();
    private final String header;
    private int jobDone = 0;
    private int jobTotal = 0;
    private int overallDone = 0;
    private int overallTotal = 0;
    private final long startTime = System.currentTimeMillis();
    private String eta;
    private String best = "";
    private double bestFitness[];
    private final Results results;
    private boolean isEvaluatorCached = false;
    private boolean isObjectiveCached = false;
    private final List<List<Ranking>> previousGenerationRankings;
    private List<Ranking> topTen = null;

    public CoolTextualExecutionListener(String config, Configuration configuration, Results results) {
        this.header = "Configuration file: " + config + " Output folder: " + configuration.getOutputFolder().getName();
        this.jobTotal = configuration.getJobs();
        this.overallTotal = configuration.getGenerations() * jobTotal;
        this.results = results;

        this.isObjectiveCached = configuration.getObjective() instanceof CacheInterface;
        this.isEvaluatorCached = configuration.getEvaluator() instanceof CachedReplaceEvaluator;

        this.previousGenerationRankings = new ArrayList<>(jobTotal);
        for (int i = 0; i < jobTotal; i++) {
            this.previousGenerationRankings.add(null);
        }
    }

    private synchronized void print() {
        char esc = 27;
        String clear = esc + "[2J";
        System.out.print(clear);

        int doneAll = 20 * overallDone / overallTotal;
        double percAll = Math.round(1000 * overallDone / (double) overallTotal) / 10.0;

        System.out.println(header);
        if (isEvaluatorCached || isObjectiveCached) {
            CacheInterface cacheStats;
            if (isObjectiveCached) {
                cacheStats = (CacheInterface) this.results.getConfiguration().getObjective();
            } else {
                cacheStats = (CacheInterface) this.results.getConfiguration().getEvaluator();
            }
            //System.out.printf("[%s] %.2f%%  | %d/%d | ETA: %s | CR: %.2f | CS: %d | CSKB: %d\n", progress(doneAll), percAll, jobDone, jobTotal, eta,cacheStats.getRatio(), cacheStats.getCacheSize(), cacheStats.getCacheSizeBytes()/1000);
            System.out.printf("[%s] %.2f%%  | %d/%d | ETA: %s | CR: %.2f | CS: %d\n", progress(doneAll), percAll, jobDone, jobTotal, eta, cacheStats.getRatio(), cacheStats.getCacheSize());
        } else {
            System.out.printf("[%s] %.2f%%  | %d/%d | ETA: %s\n", progress(doneAll), percAll, jobDone, jobTotal, eta);
        }
        for (Integer jobId : screen.keySet()) {
            String color = "";
            if (remove.contains(jobId)) {
                color = Utils.ANSI_GREEN;
            }
            System.out.println(color + screen.get(jobId) + Utils.ANSI_RESET);
        }

        System.out.println("Best: " + Utils.printableRegex(best));
        System.out.println(Utils.printPopulation(topTen));

    }

    @Override
    public void evolutionStarted(RunStrategy strategy) {
        int jobId = strategy.getConfiguration().getJobId();
        String print = "[                     ] 0% Gen --> 0 job: " + jobId;

        synchronized (screen) {
            screen.put(jobId, print);
        }

        this.jobStartTimes.put(jobId, System.currentTimeMillis());
    }

    @Override
    public void logGeneration(RunStrategy strategy, int generation, Node best, double[] fitness, List<Ranking> population) {
        int jobId = strategy.getConfiguration().getJobId();
        int done = 20 * generation / strategy.getConfiguration().getGenerations();
        double perc = Math.round(1000 * generation / (double) strategy.getConfiguration().getGenerations()) / 10f;

        overallDone++;

        long timeTakenPerGen = (System.currentTimeMillis() - startTime) / overallDone;
        long elapsedMillis = (overallTotal - overallDone) * timeTakenPerGen;

        eta = String.format("%d h, %d m, %d s",
                TimeUnit.MILLISECONDS.toHours(elapsedMillis),
                TimeUnit.MILLISECONDS.toMinutes(elapsedMillis) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(elapsedMillis)),
                TimeUnit.MILLISECONDS.toSeconds(elapsedMillis) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(elapsedMillis)));

        //This is the best based on fitnesses, for visualization sake
        if (bestFitness == null) {
            updateBest(best, fitness);
        } else if (fitness[0] <= bestFitness[0]) {
            if (!Utils.isAParetoDominateByB(fitness, bestFitness)) {
                updateBest(best, fitness);
            }
        }
        double diversity = Utils.diversity(population);
        List<Ranking> previousGenerationRanking = previousGenerationRankings.get(jobId);
        double storicalDiversity;
        if (previousGenerationRanking != null) {
            storicalDiversity = Utils.intraPopulationsDiversity(previousGenerationRankings.get(jobId), population);
        } else {
            storicalDiversity = 100.0;
        }

        String print = String.format("[%s] %.2f%% g: %d j: %d f: %s ds: %.2f%%", progress(done), perc, generation, jobId, Utils.printArray(fitness), storicalDiversity);
        synchronized (screen) {
            topTen = population.subList(0, Math.min(10, population.size()));
            screen.put(jobId, print);
            print();
        }

        //let's store the current generatin best(fitness) individual performances on validation. remind performances indexes != fintesses 
        FinalSolution generationBestSolution = new FinalSolution(population.get(0));

        //NOTE: only validation info is populated. Testing information has to be populated later. The best place is into BestSelector
        //Only  the learning performance i needed by the checkBestCandidate
        Objective learningObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.LEARNING, strategy.getConfiguration());
        double[] learningPerformance = learningObjective.fitness(population.get(0).getTree());
        PerformacesObjective.populatePerformancesMap(learningPerformance, generationBestSolution.getLearningPerformances());

        results.getJobTrace(jobId).checkBestCandidateSolution(generationBestSolution);

        if (generation % strategy.getConfiguration().getResultsGenerationSamplingInterval() == 0) {
            results.getJobTrace(jobId).addGeneration();
            List<Ranking> firstParetoFront = Utils.getFirstParetoFront(population);
            List<Solution> currentGeneration = results.getJobTrace(jobId).getCurrentGeneration();
            for (Ranking individual : firstParetoFront) {
                Solution solution = new Solution(individual);
                currentGeneration.add(solution);
            }
        }
        results.addCharachterEvaluated(strategy.getContext().getCurrentDataSet().getNumberOfChars() * population.size());

        /* plots fitness, diversity, precision and recall over the training set */
        Objective trainingObjective = new PerformacesObjective();
        trainingObjective.setup(strategy.getContext());
        double[] trainingPerformance = trainingObjective.fitness(population.get(0).getTree());
        PerformacesObjective.populatePerformancesMap(trainingPerformance, generationBestSolution.getTestingPerformances());

        //save previous generation primaryRankings; the new list is created in order to avoid later collection changes
        previousGenerationRankings.set(jobId, new ArrayList<>(population));
    }

    @Override
    public void evolutionComplete(RunStrategy strategy, int generation, Node best, List<Ranking> population) {
        int jobId = strategy.getConfiguration().getJobId();
        long executionTime = System.currentTimeMillis() - this.jobStartTimes.remove(jobId);

        synchronized (screen) {
            remove.add(jobId);

            if (screen.size() > 10) {
                screen.remove(remove.pollFirst());
            }
        }

        jobDone++;

        if (jobDone >= strategy.getConfiguration().getJobs()) {
            print();
        }
        JobEvolutionTrace jobTrace = this.results.getJobTrace(jobId);
        jobTrace.setExecutionTime(executionTime);
        jobTrace.setNumberJobGenerations(generation);
        /*
         Populate Job final primaryPopulation with FinalSolution(s). The final primaryPopulation has the same order as fitness ranking but contains fitness and performance info
         The performance are propulated here:
         */
        Objective trainingObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.TRAINING, strategy.getConfiguration());
        Objective validationObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.VALIDATION, strategy.getConfiguration());
        Objective learningObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.LEARNING, strategy.getConfiguration());
        Objective testingObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.TESTING, strategy.getConfiguration());
        for (int i = 0; i < population.size(); i++) {
            Ranking individual = population.get(i);
            FinalSolution finalSolution = new FinalSolution(individual);
            double[] trainingPerformace = trainingObjective.fitness(individual.getTree());
            double[] validationPerformance = validationObjective.fitness(individual.getTree());
            double[] learningPerformance = learningObjective.fitness(individual.getTree());
            PerformacesObjective.populatePerformancesMap(trainingPerformace, finalSolution.getTrainingPerformances());
            PerformacesObjective.populatePerformancesMap(validationPerformance, finalSolution.getValidationPerformances());
            PerformacesObjective.populatePerformancesMap(learningPerformance, finalSolution.getLearningPerformances());
            //The best (fitness wise) solution 
            if (i == 0) {
                double[] testingPerformance = testingObjective.fitness(individual.getTree());
                PerformacesObjective.populatePerformancesMap(testingPerformance, finalSolution.getTestingPerformances());
            }
            jobTrace.getFinalGeneration().add(finalSolution);
        }

    }

    @Override
    public void evolutionFailed(RunStrategy strategy, TreeEvaluationException cause) {
        int jobId = strategy.getConfiguration().getJobId();
        try {
            /*
             * TODO: save error log
             */
        } catch (Exception ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
    }

    private String progress(int done) {
        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < done; i++) {
            builder.append("=");
        }

        if (done < 20) {
            builder.append(">");
            for (int i = 19; i > done; i--) {
                builder.append(" ");
            }
        }

        return builder.toString();
    }

    @Override
    public void register(ExecutionStrategy strategy) {
        //NO OP
    }

    @Override
    public ExecutionListener getNewListener() {
        return this;
    }

    private void updateBest(Node best, double fitness[]) {
        if (best instanceof Forest) {
            this.best = Utils.getCoolForestRapresentation((Forest) best);
        } else {
            StringBuilder nodeDescription = new StringBuilder();
            best.describe(nodeDescription);
            this.best = nodeDescription.toString();
        }
        bestFitness = fitness;
    }
}
