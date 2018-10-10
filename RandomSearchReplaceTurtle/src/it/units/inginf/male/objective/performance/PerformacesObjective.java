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
 */package it.units.inginf.male.objective.performance;

import it.units.inginf.male.evaluators.CoevolutionaryEvaluator;
import it.units.inginf.male.evaluators.ReplaceResult;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.inputs.DataSetReplace;
import it.units.inginf.male.inputs.ExampleReplace;
import it.units.inginf.male.coevolution.Forest;
import it.units.inginf.male.objective.Objective;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This is not a fitness, but returns individual performances like in a
 * mutliobjective fitness style. fitness[0] = precision fitness[1] = recall
 * fitness[2] = charPrecision fitness[3] = charRecall fitness[4] = charAccuracy
 * fitness[5] = match fmeasure
 *
 * @author andrea
 */
public class PerformacesObjective implements Objective {

    private Context context;
    //private DataSet dataSetView;

    //private int numberCharsInMatches = 0;
    @Override
    public void setup(Context context) {
        this.context = context;
        //this.dataSetView = this.context.getCurrentDataSet();
        //this.dataSetView.populateMatchesStrings();
    }

    @Override
    public double[] fitness(Node individual) {
        DataSetReplace dataSetView = this.context.getCurrentDataSet();
        CoevolutionaryEvaluator evaluator = context.getConfiguration().getEvaluator();
        double[] fitness = new double[2];
        List<ReplaceResult> evaluate;
        try {
            evaluate = evaluator.evaluate((Forest) individual, context);
        } catch (TreeEvaluationException ex) {
            Logger.getLogger(PerformacesObjective.class.getName()).log(Level.SEVERE, null, ex);
            Arrays.fill(fitness, Double.POSITIVE_INFINITY);
            return fitness;
        }

        double distanceErrorRate = 0;
        double countErrorRate = 0;

        int i = 0;
        for (ReplaceResult result : evaluate) {
            //Characted extracted in the right place (match)
            ExampleReplace example = dataSetView.getExample(i);
            int edit = Utils.computeLevenshteinDistance(example.targetString, result.getReplacedString());
            if (edit > 0) {
                countErrorRate++;
            }
            distanceErrorRate += ((double) edit) / example.string.length();
            i++;
        }

        distanceErrorRate /= i;
        countErrorRate /= i;

        fitness[0] = distanceErrorRate;
        fitness[1] = countErrorRate;

        return fitness;
    }

    @Override
    public CoevolutionaryEvaluator getTreeEvaluator() {
        return context.getConfiguration().getEvaluator();
    }

    @Override
    public Objective cloneObjective() {
        return new PerformacesObjective();
    }

    public static void populatePerformancesMap(double[] performances, Map<String, Double> performancesMap) {
        performancesMap.put("distance error rate", performances[0]);
        performancesMap.put("count error rate", performances[1]);
    }

}
