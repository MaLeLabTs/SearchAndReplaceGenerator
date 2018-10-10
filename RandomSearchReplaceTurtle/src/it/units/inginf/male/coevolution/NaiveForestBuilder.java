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
package it.units.inginf.male.coevolution;

import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.tree.Node;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 * @author andrea
 */
public class NaiveForestBuilder implements ForestBuilder{

    @Override
    public List<Forest> generate(List<List<Node>> populations, Context context) {
        int maxPop = 0;
        int maxPopIndex = 0;
        int index = 0;
        for(List<Node> population:populations){
            if(population.size() > maxPop){
                maxPop = population.size();
                maxPopIndex = index;
            }
            index++;
        }

        List<Forest> forests = new ArrayList<>(maxPop);

        for(Node tmp:populations.get(maxPopIndex)){
            List<Node> trees = new ArrayList<>(populations.size());

            for(int i=0; i < populations.size(); i++){
                if(i != maxPopIndex){
                    int r = context.getRandom().nextInt(populations.get(i).size());
                    trees.add(populations.get(i).get(r));
                } else {
                    trees.add(tmp);
                }
            }

            Forest forest = new Forest(trees);
            forests.add(forest);
        }

        return forests;
    }

    @Override
    public void setup(Map<String, String> parameters) {

    }
    
}
