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
package it.units.inginf.male.generations;

import it.units.inginf.male.configuration.SubConfiguration;
import it.units.inginf.male.inputs.Bounds;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.inputs.DataSetReplace;
import it.units.inginf.male.inputs.ExampleReplace;
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.operator.Concatenator;
import it.units.inginf.male.utils.MappingOperation;
import it.units.inginf.male.utils.Pair;
import it.units.inginf.male.utils.Utils;
import java.util.*;

/**
 *
 * @author andrea + Fabiano
 */
public class ReplaceWithGroupsPopulationBuilder implements InitialPopulationBuilder {

    private List<Node> population = new LinkedList<>();
    
    /**
     * Initialises a primaryPopulation from examples by replacing charcters with "\\w"
     * and digits with "\\d"
     */
    public ReplaceWithGroupsPopulationBuilder() {
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
        
        newPopulation.add(new Constant(""));
        
        for (ExampleReplace example : dataSet.getExamples()) {
            Pair<String, String> findChangedZone = example.getChangedBeforeAndAfter();
            if(findChangedZone != null) {
                int lengthChanged = findChangedZone.getFirst().length();
                phrases.add(new MappingOperation(findChangedZone, false));
                //phrases.add(findChangedZone.getSecond());
                //With border
                findChangedZone = example.getChangedBeforeAndAfter(Math.max(1, lengthChanged / 2));
                if(findChangedZone != null){
                    phrases.add(new MappingOperation(findChangedZone, false));
                }
            }
        }

        int examples = Math.min(configuration.getEvolutionParameters().getPopulationSize() / 2, phrases.size());

        List<MappingOperation> uniquePhrases = new ArrayList<>(phrases);

        int counter = 1;
        for (MappingOperation node : uniquePhrases) {
            newPopulation.add(createGroupedReplaceExample(node));
            counter++;
            
            if (counter >= examples) {
                break;
            }
        }
        return newPopulation;
    }
    
    protected Node createGroupedReplaceExample(MappingOperation mapperOperation) {
        if(mapperOperation.mapping.isEmpty()){
            return createByExample(mapperOperation.target);
        }
        String target = mapperOperation.target;
        LinkedList<Node> portions = new LinkedList<>();
        int currentStart = 0;
        for (Pair<Bounds,Bounds> oneMap : mapperOperation.mapping) {
            Bounds oneMapReplaceBounds = oneMap.getSecond();
            if(currentStart < oneMapReplaceBounds.start){
                portions.add(createByExample(target.substring(currentStart, oneMapReplaceBounds.start)));
            }
            portions.add(new Constant("$"+mapperOperation.groupIDmap.get(oneMap)));
            currentStart = oneMapReplaceBounds.end;
        }
        
        if(currentStart < target.length()){
            portions.add(createByExample(target.substring(currentStart, target.length())));
        }
        
        ///call stuff on portions
        Node finalSearchIndividual = createByExample(portions);
        return finalSearchIndividual;
    }
    

    protected Node createByExample(String example) {
        if(example.isEmpty()){
            return new Constant("");
        }
        Deque<Node> nodes = new LinkedList<>();
        for (char c : example.toCharArray()) {
                nodes.add(new Constant(Utils.escape(c)));
        }
        return createByExample(nodes);
    }
    
    protected Node createByExample(Deque<Node> nodes) {
        //TerminalSetBuilder compact code, use quantifiers!!
        Deque<Node> tmp = new LinkedList<>();

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
