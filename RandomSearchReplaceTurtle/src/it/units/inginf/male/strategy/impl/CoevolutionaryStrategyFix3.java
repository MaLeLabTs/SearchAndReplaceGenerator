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
 */package it.units.inginf.male.strategy.impl;

import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.configuration.EvolutionParameters;
import it.units.inginf.male.configuration.SubConfiguration;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import it.units.inginf.male.generations.Generation;
import it.units.inginf.male.generations.InitialPopulationBuilder;
import it.units.inginf.male.generations.Ramped;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.coevolution.Forest;
import it.units.inginf.male.objective.Objective;
import it.units.inginf.male.objective.Ranking;
import it.units.inginf.male.objective.performance.PerformancesFactory;
import it.units.inginf.male.selections.Selection;
import it.units.inginf.male.selections.Tournament;
import it.units.inginf.male.strategy.ExecutionListener;
import it.units.inginf.male.strategy.RunStrategy;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.utils.Pair;
import it.units.inginf.male.utils.UniqueList;
import it.units.inginf.male.utils.Utils;
import it.units.inginf.male.variations.Variation;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

//Added elitarism but moderated to 50% individuals
/**
 * In case of re-start of all the experiment, YOU HAVE to set the stopSignal
 * variable to default FALSE!! This is not going to be automatically reset to
 * FALSE at experiment stop. Created by fabiano on 18/11/16.
 */
public class CoevolutionaryStrategyFix3 implements RunStrategy {

    protected Context context;
    private List<List<Node>> populations;
    private List<Ranking> rankings;
    private List<List<Ranking>> subRankings;

    protected Selection selection;
    protected Objective objective;
    private Variation variation;
    private ExecutionListener listener;
    //Termination criteria enables/disables the premature termination of thread when best regex/individual doesn't change for
    //a specified amount of generations (terminationCriteriaGenerations)
    private boolean terminationCriteria = true;
    private int terminationCriteriaGenerations = 50;
    private boolean globalStopWhenPerfect = false;
    private int maxGenerations;
    private Objective learningObjective;
    private int tournamentSize = 7;
    private static final AtomicBoolean stopSignal = new AtomicBoolean(false);

    protected double elitarismPopulationRatio = 0.4;
    protected double elitarismForestRatio = 1;

    @Override
    public void setup(Configuration configuration, ExecutionListener listener) throws TreeEvaluationException {

        this.readParameters(configuration);

        this.context = new Context(Context.EvaluationPhases.TRAINING, configuration);
        //cloning the objective
        this.objective = configuration.getObjective();
        this.selection = new Tournament(this.context, tournamentSize);
        this.variation = new Variation(this.context);
        this.listener = listener;

        this.objective.setup(context);
        populations = new ArrayList<>(configuration.getSubConfigurations().size());
        this.maxGenerations = configuration.getGenerations();

        int popMax = 0;
        for (SubConfiguration c : configuration.getSubConfigurations()) {
            int pop = c.getEvolutionParameters().getPopulationSize();
            if (pop > popMax) {
                popMax = pop;
            }
        }

        this.rankings = new UniqueList<>(popMax);

        learningObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.LEARNING, configuration);
    }

    private void readParameters(Configuration configuration) {
        Map<String, String> parameters = configuration.getStrategyParameters();
        if (parameters != null) {
            //add parameters if needed
            if (parameters.containsKey("terminationCriteriaGenerations")) {
                terminationCriteriaGenerations = Integer.valueOf(parameters.get("terminationCriteriaGenerations"));
            }
            if (parameters.containsKey("terminationCriteria")) {
                terminationCriteria = Boolean.valueOf(parameters.get("terminationCriteria"));
            }
            if (parameters.containsKey("globalStopWhenPerfect")) {
                globalStopWhenPerfect = Boolean.valueOf(parameters.get("globalStopWhenPerfect"));
            }
            if (parameters.containsKey("tournamentSize")) {
                this.tournamentSize = Integer.valueOf(parameters.get("tournamentSize"));
            }
        }
    }

    @Override
    public Void call() throws TreeEvaluationException {
        try {
            int generation;
            listener.evolutionStarted(this);
            int p = 0;
            for (SubConfiguration subConfig : getConfiguration().getSubConfigurations()) {
                InitialPopulationBuilder populationBuilder = subConfig.getPopulationBuilder();
                List<Node> population = populationBuilder.init();
                final int creationMaxDepth = subConfig.getEvolutionParameters().getCreationMaxDepth();
                final int popSize = subConfig.getEvolutionParameters().getPopulationSize();
                Generation ramped = new Ramped(creationMaxDepth, this.context);
                population.addAll(ramped.generate(popSize - population.size(), p));
                populations.add(population);
                p++;
            }

            List<Ranking> tmp = buildRankings(populations, objective, 1);
            while (tmp.size() > 0) {
                List<Ranking> t = Utils.getFirstParetoFront(tmp);
                tmp.removeAll(t);
                sortByOrder(t);
                rankings.addAll(t);
            }
            subRankings = splitRanking(rankings);

            //Variables for termination criteria
            double[] oldGenerationBestValue = null;
            int terminationCriteriaGenerationsCounter = 0;
            boolean allPerfect = false;
            for (generation = 0; generation < maxGenerations; generation++) {

                evolve();
                Ranking bestRegex = this.rankings.get(0);
                //Ranking bestString = this.rankings.get(1).get(0);
                if (listener != null) {
                    listener.logGeneration(this, generation + 1, bestRegex.getTree(), bestRegex.getFitness(), this.rankings);
                }

                allPerfect = true;
                for (double fitness : rankings.get(0).getFitness()) {
                    if (Math.round(fitness * 10000) != 0) {
                        allPerfect = false;
                        break;
                    }
                }
                if (allPerfect) {
                    break;
                }

                if (terminationCriteria) {
                    double[] learningPerformance = learningObjective.fitness(bestRegex.getTree());
                    double[] fitnessBest = bestRegex.getFitness();
                    allPerfect = true;
                    for (double fitness : learningPerformance) {
                        if (Math.round(fitness * 10000) != 0) {
                            allPerfect = false;
                            break;
                        }
                    }
                    //String newBestValue = bestRegex.getDescription();
                    if (Arrays.equals(fitnessBest, oldGenerationBestValue) && allPerfect) {
                        terminationCriteriaGenerationsCounter++;
                    } else {
                        terminationCriteriaGenerationsCounter = 0;
                    }
                    if (terminationCriteriaGenerationsCounter >= this.terminationCriteriaGenerations) {
                        break;
                    }
                    oldGenerationBestValue = fitnessBest;

                }

                if (stopSignal.get() || Thread.interrupted()) {
                    break;
                }

            }

            if (listener != null) {
                listener.evolutionComplete(this, generation - 1, rankings.get(0).getTree(), this.rankings);
            }

            if (this.globalStopWhenPerfect && allPerfect) {
                stopSignal.set(true);
            }
            return null;
        } catch (Throwable x) {
            throw new TreeEvaluationException("Error during evaluation of a tree", x, this);
        }
    }

    private void evolve() {
        boolean allPerfect = true;
        for (double fitness : rankings.get(0).getFitness()) {
            if (Math.round(fitness * 10000) != 0) {
                allPerfect = false;
                break;
            }
        }
        if (allPerfect) {
            return;
        }

        List<List<Node>> newPopulations = new ArrayList<>();

        for (int i = 0; i < populations.size(); i++) {
            List<Node> population = populations.get(i);
            List<Node> newPopulation = generateNewPopulation(population, i, subRankings.get(i), elitarismPopulationRatio);
            //newPopulation.addAll(population);
            newPopulations.add(newPopulation);
        }

        List<Ranking> tmp = buildRankings(newPopulations, objective, elitarismForestRatio);
        sortRankings(tmp, rankings);
        subRankings = splitRanking(rankings);
        //System.out.println("***Sub-ranking size, search: "+subRankings.get(0).size()+" replace: "+subRankings.get(1).size());

        int maxPopSize = 0;
        for (int i = 0; i < populations.size(); i++) {
            EvolutionParameters params = getConfiguration().getSubConfiguration(i).getEvolutionParameters();
            int targetPopsize = params.getPopulationSize();//population.size();

            List<Ranking> subRanking = subRankings.get(i);
            List<Node> population = populations.get(i);

            maxPopSize = targetPopsize > maxPopSize ? targetPopsize : maxPopSize;
            if (subRanking.size() >= targetPopsize) {
                subRanking = new ArrayList<>(subRanking.subList(0, targetPopsize));
            }
            population.clear();
            for (Ranking r : subRanking) {
                population.add(r.getTree());
            }

            Generation ramped = new Ramped(params.getCreationMaxDepth(), context);
            List<Node> generated = ramped.generate(targetPopsize - population.size(), i);
            population.addAll(generated);
        }

        rankings = rankings.subList(0, Math.min(maxPopSize, rankings.size()));

    }

    private List<Node> generateNewPopulation(List<Node> population, int populationId, List<Ranking> ranking, double elitarismRatio) {
        EvolutionParameters params = getConfiguration().getSubConfiguration(populationId).getEvolutionParameters();
        int targetPopsize = params.getPopulationSize();//population.size();

        List<it.units.inginf.male.tree.Node> newPopulation;
        newPopulation = new UniqueList<>(population.size());
        for (Ranking ranked : ranking) {
            if (newPopulation.size() >= targetPopsize * elitarismRatio) {
                break;
            }
            newPopulation.add(ranked.getTree());
        }

        int oldPopSize = (int) (targetPopsize * 0.9);

        while (newPopulation.size() < oldPopSize) {

            double random = context.getRandom().nextDouble();

            if (random <= params.getCrossoverProbability() && oldPopSize - newPopulation.size() >= 2) {
                Node selectedA = selection.select(ranking);
                Node selectedB = selection.select(ranking);

                Pair<Node, Node> newIndividuals = variation.crossover(selectedA, selectedB, populationId);
                if (newIndividuals != null) {
                    newPopulation.add(newIndividuals.getFirst());
                    newPopulation.add(newIndividuals.getSecond());
                }
            } else if (random <= params.getCrossoverProbability() + params.getMutationPobability()) {
                Node mutant = selection.select(ranking);
                mutant = variation.mutate(mutant, populationId);
                newPopulation.add(mutant);
            } else {
                Node duplicated = selection.select(ranking);
                newPopulation.add(duplicated);
            }
        }

        Generation ramped = new Ramped(params.getCreationMaxDepth(), context); //(params.getMaxDepthAfterCrossover(), context)
        List<Node> generated = ramped.generate(targetPopsize - oldPopSize, populationId);
        newPopulation.addAll(generated);

        return newPopulation;
    }

    private List<Ranking> buildRankings(List<List<Node>> populations, Objective objective, double elitarismRatio) {
        List<Ranking> bestRanking = rankings.subList(0, (int) (rankings.size() * elitarismRatio));
        List<Ranking> result = new UniqueList<>(bestRanking);

        List<Forest> forests = getConfiguration().getForestBuilder().generate(populations, context);

        for (Forest forest : forests) {
            double fitness[] = objective.fitness(forest);
            result.add(new Ranking(forest, fitness));
        }

        return result;
    }

    private List<List<Ranking>> splitRanking(List<Ranking> rankings) {
        List<List<Ranking>> ret = new ArrayList<>(getConfiguration().getSubConfigurations().size());
        List<Set<String>> tmp = new ArrayList<>(getConfiguration().getSubConfigurations().size());
        for (SubConfiguration subConfig : getConfiguration().getSubConfigurations()) {
            ret.add(new ArrayList<>(subConfig.getEvolutionParameters().getPopulationSize()));
            tmp.add(new HashSet<>(subConfig.getEvolutionParameters().getPopulationSize()));
        }

        for (Ranking r : rankings) {
            Forest forest = (Forest) r.getTree();
            int n = 0;
            for (Node tree : forest) {
                StringBuilder builder = new StringBuilder();
                tree.describe(builder);
                String description = builder.toString();
                if (!tmp.get(n).contains(description)) {
                    ret.get(n).add(new Ranking(tree, r.getFitness()));
                    tmp.get(n).add(description);
                }
                n++;
            }
        }

        return ret;
    }

    @Override
    public Configuration getConfiguration() {
        return context.getConfiguration();
    }

    @Override
    public ExecutionListener getExecutionListener() {
        return listener;
    }

    private void sortByOrder(List<Ranking> front) {
        Collections.sort(front, (o1, o2) -> {
            double[] fitness1 = o1.getFitness();
            double[] fitness2 = o2.getFitness();
            int compare = 0;
            for (int i = 0; i < fitness1.length; i++) {
                compare = Double.compare(fitness1[i], fitness2[i]);
                if (compare != 0) {
                    return compare;
                }
            }
            return -o1.getDescription().compareTo(o2.getDescription());
        });
    }

    private void sortRankings(List<Ranking> tmp, List<Ranking> dst) {
        dst.clear();
        while (tmp.size() > 0) {
            List<Ranking> t = Utils.getFirstParetoFront(tmp);
            tmp.removeAll(t);
            sortByOrder(t);
            dst.addAll(t);
        }
    }

    @Override
    public Context getContext() {
        return this.context;
    }

    /**
     * In case of restart of all the experiment, YOU HAVE to set this variable
     * to FALSE!! This is not going to be automatically reset to FALSE at
     * experiment stop.
     *
     * @return
     */
    public static AtomicBoolean getStopSignal() {
        return stopSignal;
    }

}
