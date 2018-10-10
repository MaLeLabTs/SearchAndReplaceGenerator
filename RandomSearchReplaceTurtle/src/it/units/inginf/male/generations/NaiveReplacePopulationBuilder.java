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
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.inputs.DataSetReplace;
import it.units.inginf.male.inputs.ExampleReplace;
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.operator.Concatenator;
import it.units.inginf.male.utils.Pair;
import it.units.inginf.male.utils.Utils;
import java.util.*;

/**
 * //TODO WORKING IN PROGRESS
 * @author andrea
 */
public class NaiveReplacePopulationBuilder implements InitialPopulationBuilder {

    private List<Node> population = new LinkedList<>();
    private final boolean useDottification;
    private final boolean useWordClasses;
    
    /**
     * Initialises a primaryPopulation from examples by replacing charcters with "\\w"
     * and digits with "\\d"
     */
    public NaiveReplacePopulationBuilder() {
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
    public NaiveReplacePopulationBuilder(boolean useDottification, boolean useWordClasses) {
        this.useDottification = useDottification;
        this.useWordClasses = useWordClasses;
    }

    @Override
    public List<Node> init() {
        return new ArrayList<Node>(population);
    }

    @Override
    public void setup(SubConfiguration configuration) {
        DataSetReplace trainingDataset = configuration.getDatasetContainer().getTrainingDataset();
        this.population.addAll(this.setup(configuration, trainingDataset));
    }
    
    private List<Node> setup(SubConfiguration configuration, DataSetReplace usedTrainingDataset) {
        Set<String> phrases = new HashSet<String>();
        List<Node> newPopulation = new LinkedList<>();
        DataSetReplace dataSet = usedTrainingDataset;

        
        for (ExampleReplace example : dataSet.getExamples()) {
            Pair<String, String> findChangedZone = example.getChangedBeforeAndAfter();
            phrases.add(findChangedZone.getFirst());
            phrases.add(findChangedZone.getSecond());
        }

        int examples = Math.min(configuration.getEvolutionParameters().getPopulationSize() / 4, phrases.size());

        List<String> uniquePhrases = new ArrayList<String>(phrases);

        int counter = 0;
        for (String node : uniquePhrases) {
            if (this.useDottification) {
                newPopulation.add(createByExample(node, true));
            }
            newPopulation.add(createByExample(node, false));
            counter++;
            if (counter >= examples) {
                break;
            }
        }
        return newPopulation;
    }
    
    

    private Node createByExample(String example, boolean replace) {
        Deque<Node> nodes = new LinkedList<Node>();
        Deque<Node> tmp = new LinkedList<Node>();

        String w = this.useWordClasses ? "\\w" : ".";
        String d = this.useWordClasses ? "\\d" : ".";
        for (char c : example.toCharArray()) {
            if (replace) {
                if (Character.isLetter(c)) {
                    nodes.add(new Constant(w));
                } else if (Character.isDigit(c)) {
                    nodes.add(new Constant(d));
                } else {
                    nodes.add(new Constant(Utils.escape(c)));
                }
            } else {
                nodes.add(new Constant(Utils.escape(c)));
            }
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
