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
 */package it.units.inginf.male.utils;

import it.units.inginf.male.inputs.Bounds;
import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.operator.Concatenator;
import it.units.inginf.male.tree.operator.MatchOneOrMore;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Created by andrea on 21/11/16.
 */
public class UtilsTest {

    @Test
    public void testcomputeLevenshteinDistance() {
        assertEquals(0,Utils.computeLevenshteinDistance("prova","prova"));
        assertEquals(3,Utils.computeLevenshteinDistance("provaxxx","prova"));
        assertEquals(1,Utils.computeLevenshteinDistance("prova","prov"));
        assertEquals(3,Utils.computeLevenshteinDistance("prova","provaxxx"));
        assertEquals(1,Utils.computeLevenshteinDistance("prov","prova"));
        assertEquals(3,Utils.computeLevenshteinDistance("ov","prova"));
        assertEquals(2,Utils.computeLevenshteinDistance("prova","ova"));
        assertEquals(1,Utils.computeLevenshteinDistance("prova","prxva"));
        assertEquals(0,Utils.computeLevenshteinDistance("",""));
    }
    
    @Test
    public void testfindLargestCommonSubstringNew(){
        String first = "ciao 13 2013 4078 40";
        String second = "2013-13xxxxxxxx-40-4078";
        List<Pair<Bounds, Bounds>> expected = Arrays.asList(new Pair<>(new Bounds(5, 7),new Bounds(5, 7)),
                new Pair<>(new Bounds(8, 12),new Bounds(0, 4)),
                new Pair<>(new Bounds(13, 17),new Bounds(19, 23)),
                new Pair<>(new Bounds(18, 20),new Bounds(16, 18))                
                );
        List<Pair<Bounds, Bounds>> outcome = Utils.findLargestCommonSubstringNew(first, second);
        
        assertEquals(expected, outcome);
    }
    
    /**
     * Test of complexityRegex method, of class Utils.
     */
    public void testComplexity() {
        System.out.println("complexity");
        Node node = new Concatenator();
        List<Node> childrens = node.getChildrens();
        childrens.add(new MatchOneOrMore());
        childrens.add(new Constant("ab"));
        childrens.get(0).getChildrens().add(new Constant("\\w"));
        boolean pushGeneralization = true;
        double expResult = 3.4;
        double result = Utils.complexityRegex(node, pushGeneralization);
        assertEquals(expResult, result);
    }

    
}
