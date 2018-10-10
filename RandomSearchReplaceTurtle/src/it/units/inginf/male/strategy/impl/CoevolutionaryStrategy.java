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
import java.util.concurrent.atomic.AtomicLong;


/**
 * Created by andrea on 18/11/16.
 */
public class CoevolutionaryStrategy implements RunStrategy {
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
    private int maxGenerations;
    private Objective learningObjective;
    
    public static List<Integer> replaceSizes = new ArrayList<>();
    public static List<Integer> searchSizes = new ArrayList<>();
    
    public static synchronized void updatePopulationStats(int searchSize, int replaceSize){
        replaceSizes.add(replaceSize);
        searchSizes.add(searchSize);
    }
    
    public static PopStats getPopulationStats(){
        return new PopStats(searchSizes, replaceSizes);
    }
    
    @Override
    public void setup(Configuration configuration, ExecutionListener listener) throws TreeEvaluationException {

        this.readParameters(configuration);

        this.context = new Context(Context.EvaluationPhases.TRAINING, configuration);
        //cloning the objective
        this.objective = configuration.getObjective();
        this.selection = new Tournament(this.context);
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

            List<Ranking> tmp = buildRankings(populations, objective);
            while (tmp.size() > 0) {
                List<Ranking> t = Utils.getFirstParetoFront(tmp);
                tmp.removeAll(t);
                sortByOrder(t);
                rankings.addAll(t);
            }
            subRankings = splitRanking(rankings);
            //System.out.println("***START-Sub-ranking size, search: "+subRankings.get(0).size()+" replace: "+subRankings.get(1).size());
            //System.out.println("***START-Population size, search: "+populations.get(0).size()+" replace: "+populations.get(1).size());
            updatePopulationStats(populations.get(0).size(), populations.get(1).size());

            //Variables for termination criteria
            String oldGenerationBestValue = null;
            int terminationCriteriaGenerationsCounter = 0;

            for (generation = 0; generation < maxGenerations; generation++) {

                evolve();
                Ranking bestRegex = this.rankings.get(0);
                //Ranking bestString = this.rankings.get(1).get(0);
                if (listener != null) {
                    listener.logGeneration(this, generation + 1, bestRegex.getTree(), bestRegex.getFitness(), this.rankings);
                }

                // I suppose it makes no sense when an ojective is a length
                boolean allPerfect = true;
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
                    allPerfect = true;
                    for (double fitness : learningPerformance) {
                        if (Math.round(fitness * 10000) != 0) {
                            allPerfect = false;
                            break;
                        }
                    }
                    String newBestValue = bestRegex.getDescription();
                    if (newBestValue.equals(oldGenerationBestValue) && allPerfect) {
                        terminationCriteriaGenerationsCounter++;
                    } else {
                        terminationCriteriaGenerationsCounter = 0;
                    }
                    if (terminationCriteriaGenerationsCounter >= this.terminationCriteriaGenerations) {
                        break;
                    }
                    oldGenerationBestValue = newBestValue;


                }

                if (Thread.interrupted()) {
                    break;
                }

            }

            if (listener != null) {
                listener.evolutionComplete(this, generation - 1, rankings.get(0).getTree(), this.rankings);
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
            List<Node> newPopulation = generateNewPopulation(population, i, subRankings.get(i));
            //newPopulation.addAll(population);
            newPopulations.add(newPopulation);
        }

        List<Ranking> tmp = buildRankings(newPopulations, objective);
        sortRankings(tmp, rankings);
        subRankings = splitRanking(rankings);
        //System.out.println("***Sub-ranking size, search: "+subRankings.get(0).size()+" replace: "+subRankings.get(1).size());
              
        int maxPopSize = 0;
        for (int i = 0; i < populations.size(); i++) {
            List<Ranking> subRanking = subRankings.get(i);
            List<Node> population = populations.get(i);
            int popSize = population.size();
            maxPopSize = popSize > maxPopSize ? popSize : maxPopSize;
            if (subRanking.size() >= popSize) {
                subRanking = new ArrayList<>(subRanking.subList(0, popSize));
            }
            population.clear();
            for (Ranking r : subRanking) {
                population.add(r.getTree());
            }
        }
        //System.out.println("***Population size, search: "+populations.get(0).size()+" replace: "+populations.get(1).size());
        updatePopulationStats(populations.get(0).size(), populations.get(1).size());

        rankings = rankings.subList(0, maxPopSize);

    }

    private List<Node> generateNewPopulation(List<Node> population, int populationId, List<Ranking> ranking) {
        List<it.units.inginf.male.tree.Node> newPopulation = new UniqueList<>(population.size());
        EvolutionParameters params = getConfiguration().getSubConfiguration(populationId).getEvolutionParameters();
        int popSize = population.size();
        int oldPopSize = (int) (popSize * 0.9);

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
        List<Node> generated = ramped.generate(popSize - oldPopSize, populationId);
        newPopulation.addAll(generated);

        return newPopulation;
    }

    private List<Ranking> buildRankings(List<List<Node>> populations, Objective objective) {
        List<Ranking> result = new UniqueList<>(rankings);

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
}
