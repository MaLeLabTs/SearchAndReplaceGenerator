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
 */package it.units.inginf.male.objective;

import it.units.inginf.male.coevolution.Forest;
import it.units.inginf.male.evaluators.CoevolutionaryEvaluator;
import it.units.inginf.male.evaluators.ReplaceEvaluator;
import it.units.inginf.male.evaluators.ReplaceResult;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.inputs.DataSetReplace;
import it.units.inginf.male.inputs.ExampleReplace;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.utils.Utils;

import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Created by andrea on 21/11/16.
 */
public class EditLengthObjective implements Objective {
    private Context context;

    @Override
    public void setup(Context context) {
        this.context = context;
    }

    @Override
    public double[] fitness(Node individual) {
        DataSetReplace dataSetView = this.context.getCurrentDataSet();
        ReplaceEvaluator evaluator = (ReplaceEvaluator) context.getConfiguration().getEvaluator();
        double[] fitness = new double[2];

        double fitnessLenght;
        double fitnessEdit = 0;

        List<ReplaceResult> evaluate;
        try {
            evaluate = evaluator.evaluate((Forest)individual, context);
            StringBuilder builder = new StringBuilder();
            ((Forest)individual).get(0).describe(builder);
            fitnessLenght = builder.length();

            for (int exampleIndex = 0; exampleIndex < dataSetView.getNumberExamples(); exampleIndex++) {
                ExampleReplace example = dataSetView.getExample(exampleIndex);
                String outcome = evaluate.get(exampleIndex).getReplacedString();
                fitnessEdit += Utils.computeLevenshteinDistance(example.targetString,outcome);
            }

        } catch (TreeEvaluationException ex) {
            Logger.getLogger(EditLengthObjective.class.getName()).log(Level.SEVERE, null, ex);
            Arrays.fill(fitness, Double.POSITIVE_INFINITY);
            return fitness;
        }

        fitness[0] = fitnessEdit;
        fitness[1] = fitnessLenght;

        return fitness;
    }

    @Override
    public CoevolutionaryEvaluator getTreeEvaluator() {
        return context.getConfiguration().getEvaluator();
    }

    @Override
    public Objective cloneObjective() {
        return new EditLengthObjective();
    }

}
