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
 */package it.units.inginf.male.terminalsets;

import it.units.inginf.male.configuration.SubConfiguration;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.inputs.DataSetReplace;
import it.units.inginf.male.inputs.ExampleReplace;
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Leaf;
import it.units.inginf.male.tree.NodeFactory;
import it.units.inginf.male.utils.Pair;
import it.units.inginf.male.utils.Utils;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by andrea on 23/11/16.
 */
public class SmallReplacementTerminalSetBuilder implements TerminalSetBuilder {
    @Override
    public void setup(SubConfiguration configuration) {
        configuration.initNodeFactory();
        NodeFactory nodeFactory = configuration.getNodeFactory();
        Set<Leaf> terminalSet = new HashSet<>(nodeFactory.getTerminalSet());
        DataSetReplace dataset = configuration.getDatasetContainer().getTrainingDataset();
        for(ExampleReplace example:dataset.getExamples()){
            Pair<String, String> beforeAndAfter = example.getChangedBeforeAndAfter();
            if(beforeAndAfter != null) {
                String before = beforeAndAfter.getFirst();
                String after = beforeAndAfter.getSecond();
                //Set<String> beforeSubparts = Utils.subparts(before,1,4);
                /*for(String ngram:beforeSubparts){
                    terminalSet.add(new Constant(Utils.escape(ngram)));
                }*/
                for(char c: after.toCharArray()){
                    terminalSet.add(new Constant(Utils.escape(c)));
                }
                //terminalSet.add(new Constant(Utils.escape(before)));
                terminalSet.add(new Constant(Utils.escape(after)));
            }
        }
        nodeFactory.getTerminalSet().clear();
        nodeFactory.getTerminalSet().addAll(terminalSet);
    }

    @Override
    public void setup(Context context, SubConfiguration configuration) {
        configuration.initNodeFactory();
        setup(configuration);
    }
}
