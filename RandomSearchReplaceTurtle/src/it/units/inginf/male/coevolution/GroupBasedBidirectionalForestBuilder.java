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
public class GroupBasedBidirectionalForestBuilder implements ForestBuilder {

    @Override
    public List<Forest> generate(List<List<Node>> populations, Context context) {
        Map<Integer, List<Node>> searchRegexMap = new TreeMap<>();
        Map<Integer, List<Node>> replaceExpMap = new TreeMap<>();
        List<Forest> forests = new ArrayList<>();

        if (populations.size() != 2) {
            throw new RuntimeException("Regex replace requires exactly 2 populations");
        }

        List<Node> searchRegexes = populations.get(0);
        List<Node> replaceExpressions = populations.get(1);

        int maxSearchGroups = 0;
        for (Node tree : searchRegexes) {
            int n = groupInTreeCounter(tree);
            if(n > maxSearchGroups){
                maxSearchGroups = n;
            }
            List<Node> regexList = searchRegexMap.getOrDefault(n, new LinkedList<>());
            regexList.add(tree);
            searchRegexMap.putIfAbsent(n, regexList);
        }


        for (Node tree : replaceExpressions) {
            StringBuilder builder = new StringBuilder();
            tree.describe(builder);
            int n = groupInStringCounter(builder.toString());
            List<Node> replaceList = replaceExpMap.getOrDefault(n, new LinkedList<>());
            replaceList.add(tree);
            replaceExpMap.putIfAbsent(n, replaceList);
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

        for (Map.Entry<Integer, List<Node>> entry : searchRegexMap.entrySet()) {
            int n = entry.getKey();
            List<Node> rr = entry.getValue();
            Iterator<Node> it = rr.iterator();

            while (it.hasNext()) {
                final Node regex = it.next();
                Node string = null;
                for (int i = n; i >= 0; i--) {
                    List<Node> ss = replaceExpMap.get(i);
                    if (ss != null && !ss.isEmpty()) {
                        string = ss.get(context.getRandom().nextInt(ss.size()));
                        break;
                    }
                }
                if (string != null) {
                    Forest forest = new Forest(regex, string);
                    forests.add(forest);
                }
                //it.remove();
            }

        }

        for (Map.Entry<Integer, List<Node>> entry : replaceExpMap.entrySet()) {
            int n = entry.getKey();
            List<Node> replaceListForN = entry.getValue();
            Iterator<Node> it = replaceListForN.iterator();

            while (it.hasNext()) {
                final Node replaceExp = it.next();
                Node regex = null;
                for (int i = n; i < maxSearchGroups; i++) {
                    List<Node> searchRegexList = searchRegexMap.get(i);
                    if (searchRegexList != null && !searchRegexList.isEmpty()) {
                        regex = searchRegexList.get(context.getRandom().nextInt(searchRegexList.size()));
                        break;
                    }
                }
                if (regex != null) {
                    Forest forest = new Forest(regex, replaceExp);
                    forests.add(forest);
                }
                //it.remove();
            }

        }

        return forests;
    }

    @Override
    public void setup(Map<String, String> parameters) {

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
