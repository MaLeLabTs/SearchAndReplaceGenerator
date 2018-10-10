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
import java.util.stream.Collectors;



/**
 * Same as CoevolutionaryStrategy but:
 * unicity, based on the bahviour(fitness), can be enabled by option
 * removed bug in enforcing maximum population size; right value is taken by option
 * Population splitting returns a list of solution used for individual evaluation,
 * these solutions are the only one that stays into the ranking. (the max ranking size is:
 * population1.size()+population2.size()-1 (worst scenario).
 * ForestBuilder may be executed more than 1 time, configurable by option.
 * Unicity is enforced in more points using UnicityList
 * Created by Fabiano on 18/11/16.
 */
public class CoevolutionaryDiversityBehaviourStrategy implements RunStrategy {
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
    
    private int populationMaxSize = 0;
    private List<Integer> populationTargetSize;
    private Double maxSameBehaviourClonesRatio = null;
    private Integer forestBuilderRounds = 1;

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
        this.populations = new ArrayList<>(configuration.getSubConfigurations().size());
        this.maxGenerations = configuration.getGenerations();

        this.populationTargetSize = new ArrayList<>(configuration.getSubConfigurations().size());
        
        this.populationMaxSize = 0;
        for (SubConfiguration c : configuration.getSubConfigurations()) {
            int pop = c.getEvolutionParameters().getPopulationSize();
            this.populationTargetSize.add(pop);
            if (pop > this.populationMaxSize) {
                this.populationMaxSize = pop;
            }
        }

        this.rankings = new UniqueList<>(this.populationMaxSize);

        this.learningObjective = PerformancesFactory.buildObjective(Context.EvaluationPhases.LEARNING, configuration);
    }

    private void readParameters(Configuration configuration) {
        Map<String, String> parameters = configuration.getStrategyParameters();
        if (parameters != null) {
            //add parameters if needed
            if (parameters.containsKey("terminationCriteriaGenerations")) {
                this.terminationCriteriaGenerations = Integer.valueOf(parameters.get("terminationCriteriaGenerations"));
            }
            if (parameters.containsKey("terminationCriteria")) {
                this.terminationCriteria = Boolean.valueOf(parameters.get("terminationCriteria"));
            }
            if (parameters.containsKey("maxSameBehaviourClonesRatio")) {
                this.maxSameBehaviourClonesRatio = Double.valueOf(parameters.get("maxSameBehaviourClonesRatio"));
            }
            if (parameters.containsKey("forestBuilderRounds")) {
                this.forestBuilderRounds = Integer.valueOf(parameters.get("forestBuilderRounds"));
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
        
            List<Ranking> tmp = buildRankings(populations, objective, this.forestBuilderRounds);
            //System.out.println("TMP rankings\n"+Utils.printPopulation(tmp));
            
            while (tmp.size() > 0) {
                List<Ranking> t = Utils.getFirstParetoFront(tmp);
                tmp.removeAll(t);
                sortByOrder(t);
                rankings.addAll(t);
            }
            
            //System.out.println("TMP Ordered rankings\n\n"+Utils.printPopulation(rankings));
            UniqueList<Ranking> usedRankingsForEvaluation = new UniqueList<>(rankings.size());
            subRankings = splitRanking(rankings, usedRankingsForEvaluation);

            //Variables for termination criteria
            String oldGenerationBestValue = null;
            int terminationCriteriaGenerationsCounter = 0;

            for (generation = 0; generation < maxGenerations; generation++) {

                evolve();
                
                //System.err.println("Population size: "+populations.get(0).size()+" "+populations.get(1).size());
                
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
        List<Ranking> tmp = buildRankings(newPopulations, objective, forestBuilderRounds);
        
        if(this.maxSameBehaviourClonesRatio != null){
            tmp  = stratifiedUnicity(tmp, maxSameBehaviourClonesRatio);
        }
            
        
        sortRankings(tmp, rankings);
        
        UniqueList<Ranking> usedRankingsForEvaluation = new UniqueList<>(rankings.size());
        subRankings = splitRanking(rankings, usedRankingsForEvaluation); //now exact population sizes are returned
//        rankings.clear();
//        rankings.addAll(usedRankingsForEvaluation);
        
        for (int i = 0; i < populations.size(); i++) {
            List<Ranking> subRanking = subRankings.get(i);
            List<Node> population = populations.get(i);
//            int popSize = this.populationTargetSize.get(i);
//            if (subRanking.size() >= popSize) {
//                subRanking = new ArrayList<>(subRanking.subList(0, popSize));
//            }
            population.clear();
            for (Ranking r : subRanking) {
                population.add(r.getTree());
            }
        }
        
        rankings = rankings.subList(0, populationMaxSize);
    }

    private List<Node> generateNewPopulation(List<Node> population, int populationId, List<Ranking> ranking) {
        int popSize = this.populationTargetSize.get(populationId);
        int oldPopSize = (int) (popSize * 0.9);

        List<it.units.inginf.male.tree.Node> newPopulation = new UniqueList<>(popSize);
        EvolutionParameters params = getConfiguration().getSubConfiguration(populationId).getEvolutionParameters();
        
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
          return buildRankings(populations, objective, 1);
     }
    
    private List<Ranking> buildRankings(List<List<Node>> populations, Objective objective, int rounds) {
        List<Ranking> result = new UniqueList<>(rankings);
        for (int i = 0; i < rounds; i++) {
            List<Forest> forests = getConfiguration().getForestBuilder().generate(populations, context);
            for (Forest forest : forests) {
                double fitness[] = objective.fitness(forest);
                result.add(new Ranking(forest, fitness));
            }
        }
        return result;
    }

//    public static boolean isValidPop(List<Node> node){
//        for (Node nodellino : node) {
//            Queue<Node> currentNodes = new LinkedList<>();
//            currentNodes.offer(nodellino);
//            while(!currentNodes.isEmpty()){
//                Node pollNode = currentNodes.poll();
//                currentNodes.addAll(pollNode.getChildrens());
//                if(pollNode instanceof Constant){
//                    Constant constant = (Constant)pollNode; 
//                    if(constant.toString().equals("")){
//                        System.out.println("Uch");
//                        return false;
//                    }
//                }
//                //ListMatch and ListUnMatch are considered like "other things" at the moment
//            }
//        }
//        return true;
//    }
    
    
    /**
     * The order (among individuals with the same fitness) inside the provided rankings is respected
     * returns an unordered rankings
     * @param rankings
     * @param duplicateAllowedMaxRatio
     * @return
     */
    private static List<Ranking> stratifiedUnicity(List<Ranking> rankings, double allowedDuplicatesRates){
        int rankSize = rankings.size();
        Map<List<Double>, LinkedList<Ranking>> fitnessGroups = new HashMap<>();
        List<Ranking> returnList = new ArrayList<>(rankings.size());
        int inserted = 0;
        for (Ranking ranking : rankings) {
            double[] fitness = ranking.getFitness();
            List<Double> fitnessList = Arrays.stream(fitness).boxed().collect(Collectors.toList());
            LinkedList<Ranking> orDefault = fitnessGroups.get(fitnessList);
            if(orDefault == null){
                orDefault = new LinkedList<>();
                fitnessGroups.put(fitnessList, orDefault);
            }
            orDefault.offer(ranking);
            inserted++;
        }
        
        int uniqueValues = fitnessGroups.size();
        int allowedDuplicates = (int) (uniqueValues * (allowedDuplicatesRates/(1 - allowedDuplicatesRates))); 
        int overallPreservedValues = Math.min(rankSize, uniqueValues + allowedDuplicates);
        
        while(overallPreservedValues > 0){
            for (Map.Entry<List<Double>, LinkedList<Ranking>> entry : fitnessGroups.entrySet()) {
                //double[] key = entry.getKey();
                LinkedList<Ranking> value = entry.getValue();
                if(!value.isEmpty()){
                    returnList.add(value.poll());
                    overallPreservedValues--;
                }
            }
        }
        return returnList;
    }
    
    private List<List<Ranking>> splitRanking(List<Ranking> rankings, UniqueList<Ranking> returnedUsedRankings) {
        List<List<Ranking>> ret = new ArrayList<>(getConfiguration().getSubConfigurations().size());
        List<Set<String>> tmp = new ArrayList<>(getConfiguration().getSubConfigurations().size());
        for (int posSize : this.populationTargetSize) {
            ret.add(new ArrayList<>(posSize));
            tmp.add(new HashSet<>(posSize));
        }

        for (Ranking r : rankings) {
            Forest forest = (Forest) r.getTree();
            int n = 0;
            for (Node tree : forest) {
                StringBuilder builder = new StringBuilder();
                tree.describe(builder);
                String description = builder.toString();
                if (!tmp.get(n).contains(description) && tmp.get(n).size() < this.populationTargetSize.get(n)) {
                    ret.get(n).add(new Ranking(tree, r.getFitness()));
                    tmp.get(n).add(description);
                    returnedUsedRankings.add(r);
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
