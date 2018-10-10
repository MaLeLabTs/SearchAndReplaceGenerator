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
 */package it.units.inginf.male.coevolution;

import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.operator.Group;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by andrea on 23/11/16.
 */
public class GroupBasedForestBuilder implements ForestBuilder {

    private int forestsForOneRegex = 1;

    @Override
    public List<Forest> generate(List<List<Node>> populations, Context context) {
        Map<Integer, List<Node>> regexes = new TreeMap<>();
        Map<Integer, List<Node>> strings = new TreeMap<>();
        List<Forest> forests = new ArrayList<>();

        if (populations.size() != 2) {
            throw new RuntimeException("Regex replace requires exactly 2 populations");
        }

        List<Node> r = populations.get(0);
        List<Node> s = populations.get(1);

        for (Node tree : r) {
            int n = groupInTreeCounter(tree);
            List<Node> regexList = regexes.getOrDefault(n, new LinkedList<>());
            regexList.add(tree);
            regexes.putIfAbsent(n, regexList);
        }

        for (Node tree : s) {
            StringBuilder builder = new StringBuilder();
            tree.describe(builder);
            int n = groupInStringCounter(builder.toString());
            List<Node> stringList = strings.getOrDefault(n, new LinkedList<>());
            stringList.add(tree);
            strings.putIfAbsent(n, stringList);
        }

//        Set<Integer> keysToBeRemovedR = new HashSet<>();
//
//        for (Map.Entry<Integer, List<Node>> entry : regexes.entrySet()) {
//            int n = entry.getKey();
//            List<Node> rr = entry.getValue();
//            List<Node> ss = strings.get(n);
//
//            Iterator<Node> it = rr.iterator();
//
//            while (it.hasNext() && ss != null) {
//                final Node regex = it.next();
//                final Node string = ss.get(context.getRandom().nextInt(ss.size()));
//                Forest forest = new Forest(regex, string);
//                forests.add(forest);
//                it.remove();
//            }
//
//            if (rr.isEmpty()) {
//                keysToBeRemovedR.add(n);
//            }
//
//        }
//
//        for (Integer k : keysToBeRemovedR) {
//            regexes.remove(k);
//        }
        for (int w = 0; w < this.forestsForOneRegex; w++) {

            for (Map.Entry<Integer, List<Node>> entry : regexes.entrySet()) {
                int n = entry.getKey();
                List<Node> rr = entry.getValue();
                Iterator<Node> it = rr.iterator();

                while (it.hasNext()) {
                    final Node regex = it.next();
                    Node string = null;
                    for (int i = n; i >= 0; i--) {
                        List<Node> ss = strings.get(i);
                        if (ss != null && !ss.isEmpty()) {
                            string = ss.get(context.getRandom().nextInt(ss.size()));
                            break;
                        }
                    }
                    if (string != null) {
                        Forest forest = new Forest(regex, string);
                        forests.add(forest);
                    }
                    it.remove();
                }

            }
        }

        return forests;
    }

    @Override
    public void setup(Map<String, String> parameters) {
        if (parameters != null) {
            //add parameters if needed
            if (parameters.containsKey("numRounds")) {
                this.forestsForOneRegex = Integer.valueOf(parameters.get("numRounds"));
            }
        }
    }

    private int groupInTreeCounter(Node tree) {
        int g = 0;
        if (tree instanceof Group) {
            g++;
        }

        for (Node child : tree.getChildrens()) {
            g += groupInTreeCounter(child);
        }

        return g;
    }

    private int groupInStringCounter(String string) {
        Pattern pattern = Pattern.compile("\\$(\\d)");//in order to support bigger number of groups add \\$(\\d++)
        Matcher matcher = pattern.matcher(string);
        int count = 0;

        while (matcher.find()) {
            int i = Integer.parseInt(matcher.group(1));
            if (count < i) {
                count = i;
            }
        }

        return count;
    }

}
