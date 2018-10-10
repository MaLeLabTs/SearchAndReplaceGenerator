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
package it.units.inginf.male.generations;

import it.units.inginf.male.configuration.SubConfiguration;
import it.units.inginf.male.inputs.Bounds;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.inputs.DataSetReplace;
import it.units.inginf.male.inputs.ExampleReplace;
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.operator.Concatenator;
import it.units.inginf.male.tree.operator.Group;
import it.units.inginf.male.tree.operator.MatchOneOrMore;
import it.units.inginf.male.utils.MappingOperation;
import it.units.inginf.male.utils.Pair;
import it.units.inginf.male.utils.Utils;
import java.util.*;

/**
 *
 * @author andrea +Fabiano
 */
public class SearchWithGroupsPopulationBuilder implements InitialPopulationBuilder {

    private List<Node> population = new LinkedList<>();
    private final boolean useDottification;
    private final boolean useWordClasses;
    
    /**
     * Initialises a primaryPopulation from examples by replacing charcters with "\\w"
     * and digits with "\\d"
     */
    public SearchWithGroupsPopulationBuilder() {
        this.useDottification = true;
        this.useWordClasses = true;
    }

    /**
     * When dottification is true and useWordClasses is true, instances are
     * initialized using character classes "\\w" "\\d" When dottification is
     * true and useWordClasses is false, instances are initialized by replacing
     * characters and digits with "."
     *
     * @param useDottification
     * @param useWordClasses
     */
    public SearchWithGroupsPopulationBuilder(boolean useDottification, boolean useWordClasses) {
        this.useDottification = useDottification;
        this.useWordClasses = useWordClasses;
    }

    @Override
    public List<Node> init() {
        return new ArrayList<>(population);
    }

    @Override
    public void setup(SubConfiguration configuration) {
        DataSetReplace trainingDataset = configuration.getDatasetContainer().getTrainingDataset();
        this.population.addAll(this.setup(configuration, trainingDataset));
    }
    
    private List<Node> setup(SubConfiguration configuration, DataSetReplace usedTrainingDataset) {
        Set<MappingOperation> phrases = new HashSet<>();
        List<Node> newPopulation = new LinkedList<>();
        DataSetReplace dataSet = usedTrainingDataset;

        
        for (ExampleReplace example : dataSet.getExamples()) {
            Pair<String, String> findChangedZone = example.getChangedBeforeAndAfter();
            if(findChangedZone != null) {
                int lengthChanged = findChangedZone.getFirst().length();
                if(lengthChanged!=0){
                    phrases.add(new MappingOperation(findChangedZone, true));
                }
                //phrases.add(findChangedZone.getSecond());
                //With border
                findChangedZone = example.getChangedBeforeAndAfter(Math.max(1, lengthChanged / 2));
                if(findChangedZone != null){
                    phrases.add(new MappingOperation(findChangedZone, true));
                }
            }
        }

        int examples = Math.min(configuration.getEvolutionParameters().getPopulationSize() / 2, phrases.size());

        List<MappingOperation> uniquePhrases = new ArrayList<>(phrases);

        int counter = 0;
        for (MappingOperation node : uniquePhrases) {
            if (this.useDottification) {
                newPopulation.add(createGroupedExample(node, true, true));
                newPopulation.add(createGroupedExample(node, true, false));
                counter++;
            }
            newPopulation.add(createGroupedExample(node, false, true));
            newPopulation.add(createGroupedExample(node, false, false));
            counter++;
            if (counter >= examples) {
                break;
            }
            
        }
        return newPopulation;
    } 
    
    protected Node createGroupedExample(MappingOperation mapperOperation, boolean replace, boolean compact) {
        if(mapperOperation.mapping.isEmpty()){
            return createByExample(mapperOperation.source, replace, compact);
        }
        String source = mapperOperation.source;
        LinkedList<Node> portions = new LinkedList<>();
        int currentStart = 0;
        for (Pair<Bounds,Bounds> oneMap : mapperOperation.mapping) {
            Bounds oneMapSearchBounds = oneMap.getFirst();
            if(currentStart < oneMap.getFirst().start){
                portions.add(createByExample(source.substring(currentStart, oneMapSearchBounds.start), replace, compact));
            }
            Node groupedPortion = new Group();
            groupedPortion.getChildrens().add(createByExample(source.substring(oneMapSearchBounds.start, oneMapSearchBounds.end), replace, compact));
            portions.add(groupedPortion);
            currentStart = oneMapSearchBounds.end;
        }
        
        if(currentStart < source.length()){
            portions.add(createByExample(source.substring(currentStart, source.length()), replace, compact));
        }
        
        ///call stuff on portions
        Node finalSearchIndividual = createByExample(portions, compact);
        return finalSearchIndividual;
    }
    

    protected Node createByExample(String example, boolean replace, boolean compact) {
        Deque<Node> nodes = new LinkedList<>();
        
        String w = this.useWordClasses ? "\\w" : ".";
        String d = this.useWordClasses ? "\\d" : ".";
        String s = this.useWordClasses ? "\\s" : ".";
        for (char c : example.toCharArray()) {
            if (replace) {
                if (Character.isLetter(c)) {
                    nodes.add(new Constant(w));
                } else if (Character.isDigit(c)) {
                    nodes.add(new Constant(d));
                } else if (Character.isWhitespace(c)) {
                    nodes.add(new Constant(s));
                } else {
                    nodes.add(new Constant(Utils.escape(c)));
                }
            } else {
                nodes.add(new Constant(Utils.escape(c)));
            }
        }
        
        return createByExample(nodes, compact);
    }
    
    protected Node createByExample(Deque<Node> nodes, boolean compact) {
        if(nodes.isEmpty()){
            return new Constant(""); //But this is quite wrong
        }
        //TerminalSetBuilder compact code, use quantifiers!!
        Deque<Node> tmp = new LinkedList<>();

        if (compact) {
            Deque<Node> newNodes = new LinkedList<>();
            String nodeValue;
            String nextValue;
            //do compact

            while (nodes.size() > 0) {
                Node node = nodes.pollFirst();
                StringBuilder builder = new StringBuilder();
                node.describe(builder);
                nodeValue = builder.toString();
                boolean isRepeat = false;
                while (nodes.size() > 0) {
                    Node next = nodes.peek();
                    builder = new StringBuilder();
                    next.describe(builder);
                    nextValue = builder.toString();
                    if (nodeValue.equals(nextValue)) {
                        isRepeat = true;
                        //Consume and drop the repetition
                        nodes.pollFirst();
                    } else {
                        //They are different, get out
                        break;
                    }
                }
                if (isRepeat) {
                    Node finalNode = new MatchOneOrMore();
                    finalNode.getChildrens().add(node);
                    node = finalNode;
                }
                newNodes.add(node);
            }
            nodes = newNodes;
        }
        
        
        while (nodes.size() > 1) {

            while (nodes.size() > 0) {
                Node first = nodes.pollFirst();
                Node second = nodes.pollFirst();

                if (second != null) {
                    Node conc = new Concatenator();
                    conc.getChildrens().add(first);
                    conc.getChildrens().add(second);
                    first.setParent(conc);
                    second.setParent(conc);
                    tmp.addLast(conc);
                } else {
                    tmp.addLast(first);
                }
            }

            nodes = tmp;
            tmp = new LinkedList<>();

        }

//        StringBuilder builder = new StringBuilder();
//        nodes.getFirst().describe(builder);
//        System.out.println(builder);
        return nodes.getFirst();
    }
    
    
    @Override
    public List<Node> init(Context context, SubConfiguration configuration) {
         return setup(configuration, context.getCurrentDataSet());
    }
}
