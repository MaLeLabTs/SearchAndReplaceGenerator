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
package it.units.inginf.male.configuration;

import it.units.inginf.male.coevolution.ForestBuilder;
import it.units.inginf.male.coevolution.NaiveForestBuilder;
import it.units.inginf.male.evaluators.CoevolutionaryEvaluator;
import it.units.inginf.male.evaluators.DefaultCachedReplaceEvaluator;
import it.units.inginf.male.evaluators.TreeEvaluator;
import it.units.inginf.male.generations.EmptyPopulationBuilder;
import it.units.inginf.male.objective.EditLengthObjective;
import it.units.inginf.male.objective.Objective;
import it.units.inginf.male.postprocessing.Postprocessor;
import it.units.inginf.male.postprocessing.Json2Postprocessor;
import it.units.inginf.male.selections.best.BasicLearningBestSelector;
import it.units.inginf.male.selections.best.BestSelector;
import it.units.inginf.male.strategy.ExecutionStrategy;
import it.units.inginf.male.strategy.impl.MultithreadStrategy;
import it.units.inginf.male.terminalsets.EmptyTerminalSetBuilder;
import it.units.inginf.male.utils.Range;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author fab
 */
public class Configuration {
    
    private static final Logger LOG = Logger.getLogger(Configuration.class.getName());    
  
    private List<SubConfiguration> configurations;
    
    private long initialSeed;
    private int jobs;
    private int generations;
    private int jobId;
    private transient File outputFolder;
    private String outputFolderName;
    private transient Objective objective;
    private String objectiveClass;
    private transient CoevolutionaryEvaluator evaluator;
    private String evaluatorClass;
    private transient ExecutionStrategy strategy;    
    private String strategyClass;
    private Map<String, String> strategyParameters;  
    private String configName;
    private DatasetContainer datasetContainer;
    private transient Postprocessor postprocessor;
    private String postprocessorClass;
    private Map<String, String> postprocessorParameters;
    private int resultsGenerationSamplingInterval;
    private transient BestSelector bestSelector;
    private String bestSelectorClass;
    private Map<String, String> bestSelectorParameters;
    private String forestBuilderClass;
    private Map<String, String> forestBuilderParameters;
    private transient ForestBuilder forestBuilder;
    private String datasetName;

    private Configuration() {
    }

    public Configuration(Configuration configuration) {
        this.configurations = new ArrayList<>(configuration.getSubConfigurations().size());
        for(SubConfiguration subConf : configuration.getSubConfigurations()){
            this.configurations.add(new SubConfiguration(subConf));
        }
        this.initialSeed = configuration.getInitialSeed();
        this.jobs = configuration.getJobs();
        this.jobId = configuration.getJobId();
        this.outputFolder = configuration.getOutputFolder();
        this.outputFolderName = configuration.getOutputFolderName();
        this.objective = configuration.getObjective();
        this.objectiveClass = configuration.getObjectiveClass();
        this.evaluator = configuration.getEvaluator();
        this.evaluatorClass = configuration.getEvaluatorClass();
        this.strategy = configuration.getStrategy();
        this.strategyClass = configuration.getStrategyClass();
        this.strategyParameters = configuration.getStrategyParameters();
        this.configName = configuration.getConfigName();
        this.datasetContainer = configuration.getDatasetContainer();
        this.postprocessor = configuration.getPostProcessor();
        this.postprocessorClass = configuration.getPostprocessorClass();
        this.postprocessorParameters = configuration.getPostprocessorParameters();
        this.resultsGenerationSamplingInterval = configuration.getResultsGenerationSamplingInterval();
        this.bestSelector = configuration.getBestSelector();
        this.bestSelectorClass = configuration.getBestSelectorClass();
        this.bestSelectorParameters = configuration.getBestSelectorParameters();
        this.forestBuilder = configuration.getForestBuilder();
        this.generations = configuration.getGenerations();
    }

    
    public void setup(){
        this.objective = buildObjective();
        this.evaluator = buildEvaluator();
        this.strategy = buildStrategy();
        this.outputFolder = new File(this.outputFolderName);
        checkOutputFolder(this.outputFolder);
        try {
            this.setupDatasetContainer();
        } catch (IOException ex) {
            throw new ConfigurationException(ex.getMessage());
        }
        
        this.populateDatasetContainerInSubconfigurations();
        this.setupInSubConfigurations();
        
        this.postprocessor = buildpostProcessor();
        this.bestSelector = buildBestSelector(); 
        this.forestBuilder = buildForestBuilder();
    }

    public int getGenerations() {
        return generations;
    }

    public void setGenerations(int generations) {
        this.generations = generations;
    }
    
    public DatasetContainer getDatasetContainer() {
        return datasetContainer;
    }

    public void setDatasetContainer(DatasetContainer datasetContainer) {
        this.datasetContainer = datasetContainer;
    }
  
    private void setupDatasetContainer() throws IOException{
        this.datasetContainer.loadDataset();
        this.datasetContainer.getDataset().setName(datasetName);
    }      

    public String getOutputFolderName() {
        return outputFolderName;
    }

    public void setOutputFolderName(String outputFolderName) {
        this.outputFolderName = outputFolderName;
    }
    
    public String getConfigName() {
        return configName;
    }

    public void setConfigName(String configName) {
        this.configName = configName;
    }
    
     public long getInitialSeed() {
        return initialSeed;
    }

    public void setInitialSeed(long initialSeed) {
        this.initialSeed = initialSeed;
    }

    public String getEvaluatorClass() {
        return evaluatorClass;
    }

    public void setEvaluatorClass(String evaluatorClass) {
        this.evaluatorClass = evaluatorClass;
    }
    
    public String getStrategyClass() {
        return strategyClass;
    }

    public void setStrategyClass(String strategyClass) {
        this.strategyClass = strategyClass;
    }
    
    public int getJobId() {
        return jobId;
    }

    public void setJobId(int jobId) {
        this.jobId = jobId;
    }
    
    
    public String getPostprocessorClass() {
        return postprocessorClass;
    }

    public void setPostprocessorClass(String postprocessorClass) {
        this.postprocessorClass = postprocessorClass;
    }

    public Map<String, String> getPostprocessorParameters() {
        return postprocessorParameters;
    }

    public void setPostprocessorParameters(Map<String, String> postprocessorParameters) {
        this.postprocessorParameters = postprocessorParameters;
    }
    
    public Postprocessor getPostProcessor() {
        return postprocessor;
    }

    public void setPostProcessor(Postprocessor postprocessor) {
        this.postprocessor = postprocessor;
    }  
    
    /**
     * Returns a clone of the current objective, the strategies should get the objective once and cache the instance.
     * There should be and instance per strategy (and one instance per job).
     * Calling the objective a lot of times is going to instantiate a lot of instances. 
     * @return
     */
    public Objective getObjective() {
        return objective.cloneObjective();
    }

    public void setObjective(Objective objective) {
        this.objective = objective;
    }

    
    public String getObjectiveClass() {
        return objectiveClass;
    }

    public void setObjectiveClass(String objectiveClass) {
        this.objectiveClass = objectiveClass;
    }
    
    public CoevolutionaryEvaluator getEvaluator() {
        return evaluator;
    }

    public void setEvaluator(CoevolutionaryEvaluator evaluator) {
        this.evaluator = evaluator;
    }

    public File getOutputFolder() {
        return outputFolder;
    }

    public void setOutputFolder(File outputFolder) {
        this.outputFolder = outputFolder;
    }

    public int getJobs() {
        return jobs;
    }

    public void setJobs(int jobs) {
        this.jobs = jobs;
    }

    public ExecutionStrategy getStrategy() {
        return strategy;
    }

    public void setStrategy(ExecutionStrategy strategy) {
        this.strategy = strategy;
    }

    public Map<String, String> getStrategyParameters() {
        return strategyParameters;
    }

    public void setStrategyParameters(Map<String, String> strategyParameters) {
        this.strategyParameters = strategyParameters;
    }
    
    public int getResultsGenerationSamplingInterval() {
        return resultsGenerationSamplingInterval;
    }

    public void setResultsGenerationSamplingInterval(int resultsGenerationSamplingInterval) {
        this.resultsGenerationSamplingInterval = resultsGenerationSamplingInterval;
    }
    
    
    public BestSelector getBestSelector() {
        return bestSelector;
    }

    public void setBestSelector(BestSelector bestSelector) {
        this.bestSelector = bestSelector;
    }

    public String getBestSelectorClass() {
        return bestSelectorClass;
    }

    public void setBestSelectorClass(String bestSelectorClass) {
        this.bestSelectorClass = bestSelectorClass;
    }

    public Map<String, String> getBestSelectorParameters() {
        return bestSelectorParameters;
    }

    public void setBestSelectorParameters(Map<String, String> bestSelectorParameters) {
        this.bestSelectorParameters = bestSelectorParameters;
    }

    public List<SubConfiguration> getSubConfigurations() {
        return configurations;
    }

    public void setConfigurations(List<SubConfiguration> configurations) {
        this.configurations = configurations;
    }
    
    public SubConfiguration getSubConfiguration(int n){
        return this.configurations.get(n);
    }

    public String getForestBuilderClass() {
        return forestBuilderClass;
    }

    public void setForestBuilderClass(String forestBuilderClass) {
        this.forestBuilderClass = forestBuilderClass;
    }

    public Map<String, String> getForestBuilderParameters() {
        return forestBuilderParameters;
    }

    public void setForestBuilderParameters(Map<String, String> forestBuilderParameters) {
        this.forestBuilderParameters = forestBuilderParameters;
    }

    public ForestBuilder getForestBuilder() {
        return forestBuilder;
    }

    public void setForestBuilder(ForestBuilder forestBuilder) {
        this.forestBuilder = forestBuilder;
    }
        
    
    
    private BestSelector buildBestSelector(){
        try {
            Class<? extends BestSelector> operatorClass = Class.forName(this.bestSelectorClass).asSubclass(BestSelector.class);
            BestSelector bestSelectorInstance = operatorClass.newInstance();
            bestSelectorInstance.setup(this.bestSelectorParameters);
            return bestSelectorInstance;
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
            LOG.log(Level.SEVERE, "Unable to create required bestSelector class: " + this.bestSelectorClass, ex);
            System.exit(1);
        }
        return null;
    }
    
    private CoevolutionaryEvaluator buildEvaluator() {
        Map<String, String> parameters = Collections.emptyMap();

        try {
            Class<? extends CoevolutionaryEvaluator> operatorClass = Class.forName(this.evaluatorClass).asSubclass(CoevolutionaryEvaluator.class);
            CoevolutionaryEvaluator evaluatorLocal = operatorClass.newInstance();
            evaluatorLocal.setup(parameters);
            return evaluatorLocal;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            LOG.log(Level.SEVERE, "Unable to create required evaluator: " + this.evaluatorClass, ex);
            System.exit(1);
        }
        //NO OP
        return null;
    }
    
     private Objective buildObjective() {
        try {
            Class<? extends Objective> operatorClass = Class.forName(this.objectiveClass).asSubclass(Objective.class);
            Objective operator = operatorClass.newInstance();
            return operator;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            LOG.log(Level.SEVERE, "Unable to create required objective: " + this.objectiveClass, ex);
            System.exit(1);
        }
        //NO OP
        return null;
    }
     
    private ExecutionStrategy buildStrategy() {
        try {
            Class<? extends ExecutionStrategy> strategyClass = Class.forName(this.strategyClass).asSubclass(ExecutionStrategy.class);
            ExecutionStrategy stategy = strategyClass.newInstance();
            /*Map<String, String> params = this.strategyParameters;
            params = params == null ? Collections.<String, String>emptyMap() : params;
            conf.setStrategyParameters(params);*/
            return stategy;
        } catch (ClassNotFoundException | InstantiationException | IllegalAccessException ex) {
            LOG.log(Level.SEVERE, "Unable to create required strategy: " + this.strategyClass, ex);
            System.exit(1);
        }
        //NO OP
        return null;
    }
       
    private Postprocessor buildpostProcessor() {
        if (this.postprocessorClass != null && !this.postprocessorClass.isEmpty()) {
            try {
                Class<? extends Postprocessor> operatorClass = Class.forName(this.postprocessorClass).asSubclass(Postprocessor.class);
                Postprocessor processor = operatorClass.newInstance();
                processor.setup(this.postprocessorParameters);
                return processor;
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                LOG.log(Level.SEVERE, "Unable to create required post processor: " + this.postprocessorClass, ex);
                System.exit(1);
            }
        }
        
        return null;
    }
    
    private void checkOutputFolder(File outputFolder) throws ConfigurationException {
        if (outputFolder == null) {
            throw new IllegalArgumentException("The output folder must be set");
        }
        if (!outputFolder.isDirectory()) {
            if (!outputFolder.mkdirs()) {
                throw new ConfigurationException("Unable to create output folder \""+outputFolder+"\"");
            }
        }
    }
    
    private ForestBuilder buildForestBuilder() {
        if (this.forestBuilderClass != null && !this.forestBuilderClass.isEmpty()) {
            try {
                Class<? extends ForestBuilder> operatorClass = Class.forName(this.forestBuilderClass).asSubclass(ForestBuilder.class);
                ForestBuilder forestBuilderLocal = operatorClass.newInstance();
                forestBuilderLocal.setup(this.forestBuilderParameters);
                return forestBuilderLocal;
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                LOG.log(Level.SEVERE, "Unable to create required forest builder: " + this.forestBuilderClass, ex);
                System.exit(1);
            }
        }      
        return null;
    }
    
    private void populateDatasetContainerInSubconfigurations(){
        for(SubConfiguration subconf : this.configurations){
            subconf.setDatasetContainer(this.datasetContainer);
        }
    }
    
    private void setupInSubConfigurations(){
        for(SubConfiguration subconf : this.configurations){
            subconf.setup();
        }
    }

    public static Configuration getDefaultConfiguration() throws IOException {
        List<SubConfiguration> subConfigurations = new ArrayList<>(2);

        SubConfiguration s1 = new SubConfiguration();
        SubConfiguration s2 = new SubConfiguration();

        subConfigurations.add(s1);
        subConfigurations.add(s2);

        s1.setPopulationBuilder(new EmptyPopulationBuilder());
        s2.setPopulationBuilder(new EmptyPopulationBuilder());
        s1.setPopulationBuilderClass("it.units.inginf.male.generations.EmptyPopulationBuilder");
        s2.setPopulationBuilderClass("it.units.inginf.male.generations.EmptyPopulationBuilder");

        s1.setTerminalSetBuilder(new EmptyTerminalSetBuilder());
        s2.setTerminalSetBuilder(new EmptyTerminalSetBuilder());
        s1.setTerminalSetBuilderClass("it.units.inginf.male.terminalsets.EmptyTerminalSetBuilder");
        s2.setTerminalSetBuilderClass("it.units.inginf.male.terminalsets.EmptyTerminalSetBuilder");

        s1.setEvolutionParameters(new EvolutionParameters());
        s2.setEvolutionParameters(new EvolutionParameters());

        s1.getEvolutionParameters().setCrossoverProbability(0.8f);
        s1.getEvolutionParameters().setElitarism(1);
        s1.getEvolutionParameters().setLeafCrossoverSelectionProbability(0.1f);
        s1.getEvolutionParameters().setMaxCreationDepth(7);
        s1.getEvolutionParameters().setMaxDepthAfterCrossover(15);
        s1.getEvolutionParameters().setMutationPobability(0.1f);
        s1.getEvolutionParameters().setNodeCrossoverSelectionProbability(0.9f);
        s1.getEvolutionParameters().setRootCrossoverSelectionProbability(0.0f);
        s1.getEvolutionParameters().setPopulationSize(500);

        s2.getEvolutionParameters().setCrossoverProbability(0.8f);
        s2.getEvolutionParameters().setElitarism(1);
        s2.getEvolutionParameters().setLeafCrossoverSelectionProbability(0.1f);
        s2.getEvolutionParameters().setMaxCreationDepth(7);
        s2.getEvolutionParameters().setMaxDepthAfterCrossover(15);
        s2.getEvolutionParameters().setMutationPobability(0.1f);
        s2.getEvolutionParameters().setNodeCrossoverSelectionProbability(0.9f);
        s2.getEvolutionParameters().setRootCrossoverSelectionProbability(0.0f);
        s2.getEvolutionParameters().setPopulationSize(100);

        List<String> constants1 = Arrays.asList(
                "\\d",
                "\\w",
                "0",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "\\.",
                ":",
                ",",
                ";",
                "_",
                "=",
                "\"",
                "'",
                "\\\\",
                "/",
                "\\?",
                "\\!",
                "\\}",
                "\\{",
                "\\(",
                "\\)",
                "\\[",
                "\\]",
                "<",
                ">",
                "@",
                "#",
                " ");
        List<String> constants2 = Arrays.asList(
                "0",
                "1",
                "2",
                "3",
                "4",
                "5",
                "6",
                "7",
                "8",
                "9",
                "A","a",
                "B","b",
                "C","c",
                "D","d",
                "E","e",
                "F","f",
                "G","g",
                "H","h",
                "I","i",
                "J","j",
                "K","k",
                "L","l",
                "M","m",
                "N","n",
                "O","o",
                "P","p",
                "Q","q",
                "R","r",
                "S","s",
                "T","t",
                "U","u",
                "V","v",
                "X","x",
                "Y","y",
                "Z","z",
                "\\.",":",",",";",
                "_","=","\"","'",
                "\\\\",
                "/",
                "\\?","\\!",
                "\\}","\\{","\\(","\\)","\\[","\\]","<",">",
                "@","#"," ");
        s1.setConstants(constants1);
        s2.setConstants(constants2);

        List<String> operators1 = Arrays.asList(
                "it.units.inginf.male.tree.operator.Group",
                "it.units.inginf.male.tree.operator.NonCapturingGroup",
                "it.units.inginf.male.tree.operator.ListMatch",
                "it.units.inginf.male.tree.operator.ListNotMatch",
                "it.units.inginf.male.tree.operator.MatchOneOrMore",
                "it.units.inginf.male.tree.operator.MatchZeroOrMore",
                "it.units.inginf.male.tree.operator.MatchZeroOrOne",
                "it.units.inginf.male.tree.operator.MatchMinMax",
                "it.units.inginf.male.tree.operator.PositiveLookbehind",
                "it.units.inginf.male.tree.operator.NegativeLookbehind",
                "it.units.inginf.male.tree.operator.PositiveLookahead",
                "it.units.inginf.male.tree.operator.NegativeLookahead"
        );
        List<String> operators2 = Arrays.asList(
                "it.units.inginf.male.coevolution.ReplacementGroup"
        );
        s1.setOperators(operators1);
        s2.setOperators(operators2);

        List<String> ranges = Arrays.asList("a-z","A-Z");
        s1.setRanges(ranges);
        s2.setRanges(Collections.<String>emptyList());

        Configuration configuration = new Configuration();
        configuration.setObjective(new EditLengthObjective());
        configuration.setObjectiveClass("it.units.inginf.male.objective.EditLengthObjective");
        configuration.setBestSelector(new BasicLearningBestSelector());
        configuration.setBestSelectorClass("it.units.inginf.male.selections.best.BasicLearningBestSelector");
        configuration.setConfigName("test");
        configuration.setEvaluator(new DefaultCachedReplaceEvaluator());
        configuration.setEvaluatorClass("it.units.inginf.male.evaluators.DefaultCachedRerplaceEvaluator");
        configuration.setForestBuilder(new NaiveForestBuilder());
        configuration.setForestBuilderClass("it.units.inginf.male.coevolution.NaiveForestBuilder");
        configuration.setGenerations(1000);
        configuration.setInitialSeed(0);
        configuration.setJobs(16);
        configuration.setJobId(0);
        configuration.setResultsGenerationSamplingInterval(50);
        configuration.setConfigurations(subConfigurations);
        configuration.setObjective(new EditLengthObjective());
        configuration.setOutputFolderName("results");
        configuration.setPostProcessor(new Json2Postprocessor());
        configuration.setPostprocessorClass("it.units.inginf.male.postprocessing.RemoteCouchDBPostprocessor");
        Map<String, String> postprocessorParameters = new HashMap<>();
        postprocessorParameters.put("url" , "http://172.30.42.125:5984");
        postprocessorParameters.put("spreadsheet" , "Coevolutionary Replace");
        configuration.setPostprocessorParameters(postprocessorParameters);
        configuration.setStrategy(new MultithreadStrategy());
        configuration.setStrategyClass("it.units.inginf.male.strategy.impl.MultithreadStrategy");
        Map<String, String> strategyParameters = new HashMap<>();
        strategyParameters.put("runStrategy", "it.units.inginf.male.strategy.impl.CoevolutionaryStrategy");
        strategyParameters.put("threads", "4");
        configuration.setStrategyParameters(strategyParameters);
        DatasetContainer datasetContainer = new DatasetContainer();
        datasetContainer.setPath("dataset/replace_twitter_anonymized.csv");
        List<Range> tmpranges = new ArrayList<>();
        tmpranges.add(new Range(0,100));
        tmpranges.add(new Range(105,200));
        tmpranges.add(new Range(203,300));
        datasetContainer.setTraining(tmpranges);
        datasetContainer.setValidation(tmpranges);
        datasetContainer.setTesting(tmpranges);
        configuration.setDatasetContainer(datasetContainer);

        return configuration;
    }
}
