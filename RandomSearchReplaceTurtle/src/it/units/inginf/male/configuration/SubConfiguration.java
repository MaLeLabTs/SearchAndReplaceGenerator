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

import it.units.inginf.male.generations.EmptyPopulationBuilder;
import it.units.inginf.male.generations.InitialPopulationBuilder;
import it.units.inginf.male.terminalsets.EmptyTerminalSetBuilder;
import it.units.inginf.male.terminalsets.TerminalSetBuilder;
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Leaf;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.NodeFactory;
import it.units.inginf.male.tree.RegexRange;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Marco
 */
public class SubConfiguration {

    private static final Logger LOG = Logger.getLogger(SubConfiguration.class.getName());    
  
    /**
     * Create a specific configuration for the parent coevConfig, the coevConfig parent is only used
     * in order to acquire the dataSetContainer object. You can use the same SpecificConfig instance inside
 other Configuration instances, with no hassle--when they share the same datasetContainer--
     * @param coevConfig
     */
    public SubConfiguration(Configuration coevConfig) {
        this.nodeFactory = new NodeFactory();
        this.datasetContainer = coevConfig.getDatasetContainer();
        //
        //this.initialSeed = System.currentTimeMillis();
        // Enables legacy collections behaviours for sorting
        // Fixes multi objective with more than 2 fitnesses.
      
    }
    
    public SubConfiguration() {
        this.nodeFactory = new NodeFactory();
    }
    
    public void setup(){
        initNodeFactory();
        
        
        this.terminalSetBuilder = buildTerminalSetBuilder();
        //Only this initialization are valid, the other initializations are not dangerous but are useless.
        this.populationBuilder = buildPopulationBuilder();
        
    }

    public SubConfiguration(SubConfiguration cc) {
        this.evolutionParameters = cc.getEvolutionParameters();    
        this.populationBuilder = cc.getPopulationBuilder();
        this.populationBuilderClass = cc.getPopulationBuilderClass();
        this.terminalSetBuilderParameters = cc.getTerminalSetBuilderParameters();
        this.terminalSetBuilderClass = cc.getTerminalSetBuilderClass();
        this.terminalSetBuilder = cc.getTerminalSetBuilder();
        this.populationBuilderParameters = cc.getPopulationBuilderParameters();
        this.nodeFactory = cc.getNodeFactory();
        this.constants = cc.constants;
        this.ranges = cc.ranges;
        this.operators = cc.operators;
    }
    
    
    private EvolutionParameters evolutionParameters;
    private transient NodeFactory nodeFactory;
    private transient InitialPopulationBuilder populationBuilder = new EmptyPopulationBuilder();
    private Map<String, String> populationBuilderParameters;
    private String populationBuilderClass;
    private Map<String, String> terminalSetBuilderParameters;
    private String terminalSetBuilderClass;
    private transient TerminalSetBuilder terminalSetBuilder = new EmptyTerminalSetBuilder();
    private List<String> constants;
    private List<String> ranges;
    private List<String> operators;
    private transient DatasetContainer datasetContainer;
    
    public NodeFactory getNodeFactory() {
        return nodeFactory;
    }

    public void setNodeFactory(NodeFactory nodeFactory) {
        this.nodeFactory = nodeFactory;
    }

    public void setConstants(List<String> constants) {
        this.constants = constants;
    }

    public void setRanges(List<String> ranges) {
        this.ranges = ranges;
    }

    public void setOperators(List<String> operators) {
        this.operators = operators;
    }

    public EvolutionParameters getEvolutionParameters() {
        return evolutionParameters;
    }

    public void setEvolutionParameters(EvolutionParameters evolutionParameters) {
        this.evolutionParameters = evolutionParameters;
    }

    public String getPopulationBuilderClass() {
        return populationBuilderClass;
    }

    public void setPopulationBuilderClass(String populationBuilderClass) {
        this.populationBuilderClass = populationBuilderClass;
    }

    public Map<String, String> getTerminalSetBuilderParameters() {
        return terminalSetBuilderParameters;
    }

    public void setTerminalSetBuilderParameters(Map<String, String> terminalSetBuilderParameters) {
        this.terminalSetBuilderParameters = terminalSetBuilderParameters;
    }

    public String getTerminalSetBuilderClass() {
        return terminalSetBuilderClass;
    }

    public void setTerminalSetBuilderClass(String terminalSetBuilderClass) {
        this.terminalSetBuilderClass = terminalSetBuilderClass;
    }

    public TerminalSetBuilder getTerminalSetBuilder() {
        return terminalSetBuilder;
    }

    public void setTerminalSetBuilder(TerminalSetBuilder terminalSetBuilder) {
        this.terminalSetBuilder = terminalSetBuilder;
    }


    public InitialPopulationBuilder getPopulationBuilder() {
        return populationBuilder;
    }

    public void setPopulationBuilder(InitialPopulationBuilder populationBuilder) {
        this.populationBuilder = populationBuilder;
    } 

    public DatasetContainer getDatasetContainer() {
        return datasetContainer;
    }

    public void setDatasetContainer(DatasetContainer datasetContainer) {
        this.datasetContainer = datasetContainer;
    }

    
    
    private TerminalSetBuilder buildTerminalSetBuilder(){
        if (this.terminalSetBuilderClass != null && !this.terminalSetBuilderClass.isEmpty()) {
            try {
                Class<? extends TerminalSetBuilder> operatorClass = Class.forName(this.terminalSetBuilderClass).asSubclass(TerminalSetBuilder.class);
                TerminalSetBuilder terminalSetBuilderInstance = operatorClass.newInstance();
                terminalSetBuilderInstance.setup(this);
                return terminalSetBuilderInstance;
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                LOG.log(Level.SEVERE, "Unable to create required terminalSetBuilder class: " + this.terminalSetBuilderClass, ex);
                System.exit(1);
            }
        } else {
            return new EmptyTerminalSetBuilder();
        }
        return null;
    }
 

    
    public Map<String, String> getPopulationBuilderParameters() {
        return populationBuilderParameters;
    }    
    
    /**
     * Resets NodeFactory configuration; Creates a new NodeFactory instance and populates a terminal and function set based
     * on the configuration file defined constants and ranges. Other TerminalSetBuilder are not invoked by this method.
     */
    public void initNodeFactory() {
        NodeFactory factory = new NodeFactory();
        List<Leaf> terminals = factory.getTerminalSet();
        
        for (String c : constants) {            
            terminals.add(new Constant(c));
        }

        for (String s : ranges) {
            terminals.add(new RegexRange(s));
        }

        List<Node> functions = factory.getFunctionSet();
        for (String o : operators) {
            try {
                functions.add(buildOperatorInstance(o));
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                LOG.log(Level.SEVERE, "Unable to create required operator: " + o, ex);
                System.exit(1);
            }
        }
        this.nodeFactory = factory;
    }
    
    private Node buildOperatorInstance(String o) throws ClassNotFoundException, InstantiationException, IllegalAccessException {
        Class<? extends Node> operatorClass = Class.forName(o).asSubclass(Node.class);
        Node operator = operatorClass.newInstance();
        return operator;
    }
    
    private InitialPopulationBuilder buildPopulationBuilder() {
        if (this.populationBuilderClass != null && !this.populationBuilderClass.isEmpty()) {
            try {
                Class<? extends InitialPopulationBuilder> operatorClass = Class.forName(this.populationBuilderClass).asSubclass(InitialPopulationBuilder.class);
                InitialPopulationBuilder popBuilder = operatorClass.newInstance();
                popBuilder.setup(this);
                return popBuilder;
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException ex) {
                LOG.log(Level.SEVERE, "Unable to create required post processor: " + this.populationBuilderClass, ex);
                System.exit(1);
            }
        }

        return new EmptyPopulationBuilder();
    }
}
