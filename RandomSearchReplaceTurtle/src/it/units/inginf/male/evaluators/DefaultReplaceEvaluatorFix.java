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
 */package it.units.inginf.male.evaluators;

import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.inputs.DataSetReplace;
import it.units.inginf.male.inputs.ExampleReplace;
import it.units.inginf.male.coevolution.Forest;
import it.units.inginf.male.tree.Node;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.regex.PatternSyntaxException;

/**
 * Created by andrea on 21/11/16.
 */
public class DefaultReplaceEvaluatorFix implements ReplaceEvaluator {
    @Override
    public void setup(Map<String, String> parameters) {

    }

    @Override
    public List<ReplaceResult> evaluate(Forest root, Context context) throws TreeEvaluationException {
        List<ReplaceResult> results = new ArrayList<>(context.getCurrentDataSetLength());

        Node first = root.get(0);
        Node second = root.get(1);

        StringBuilder sb = new StringBuilder();
        first.describe(sb);

        StringBuilder rb = new StringBuilder();
        second.describe(rb);
        String replace = rb.toString();

        try {
            Pattern regex = Pattern.compile(sb.toString());
            Matcher matcher = regex.matcher("");

            DataSetReplace dataSet = context.getCurrentDataSet();
            for (ExampleReplace example : dataSet.getExamples()) {
                try {
                    Matcher m = matcher.reset(example.getString());
                    int s = 0;
                    int e = 0;
                    if (matcher.find()) {
                        s = m.start();
                        e = m.end();
                    }
                    String replaced = m.replaceFirst(replace);
                    ReplaceResult result = new ReplaceResult(replaced, s, e);
                    results.add(result);
                } catch (StringIndexOutOfBoundsException ex) {
                    /**
                     * Workaround: riferimento BUG: 6984178
                     * http://bugs.sun.com/bugdatabase/view_bug.do?bug_id=6984178
                     * con i quantificatori greedy restituisce una eccezzione
                     * invece che restituire un "false".
                     */
                    results.add(new ReplaceResult(example.getString(), 0, 0));
                }

            }

        } catch (PatternSyntaxException ex) {
            throw new TreeEvaluationException(ex);
        }
        return results;

    }
}
