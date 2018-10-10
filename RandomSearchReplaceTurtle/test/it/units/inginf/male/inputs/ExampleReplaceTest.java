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
package it.units.inginf.male.inputs;

import it.units.inginf.male.utils.Pair;
import junit.framework.TestCase;

/**
 *
 * @author fab
 */
public class ExampleReplaceTest extends TestCase {
    
    public ExampleReplaceTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test of getChangedBeforeAndAfter method, of class ExampleReplace.
     */
    public void testGetChangedBeforeAndAfter_0args() {
        System.out.println("getChangedBeforeAndAfter");
        ExampleReplace instance;
        instance = new ExampleReplace("ciaosonociao", "ciaoziociao");
        Pair<String, String> expResult = new Pair<>("son","zi");
        Pair<String, String> result = instance.getChangedBeforeAndAfter();
        assertEquals(expResult, result);
        instance = new ExampleReplace("ciaosono", "ciaozia");
        expResult = new Pair<>("sono","zia");
        result = instance.getChangedBeforeAndAfter();
        assertEquals(expResult, result);
        instance = new ExampleReplace("ciaosono", "ciao");
        expResult = new Pair<>("sono","");
        result = instance.getChangedBeforeAndAfter();
        assertEquals(expResult, result);
        instance = new ExampleReplace("ciaosonociao", "ciaociao");
        expResult = new Pair<>("sono","");
        result = instance.getChangedBeforeAndAfter();
        assertEquals(expResult, result);
        instance = new ExampleReplace("1234ciao", "ciao");
        expResult = new Pair<>("1234","");
        result = instance.getChangedBeforeAndAfter();
        assertEquals(expResult, result);
        instance = new ExampleReplace("123", "");
        expResult = new Pair<>("123","");
        result = instance.getChangedBeforeAndAfter();
        assertEquals(expResult, result);
        instance = new ExampleReplace("1", "");
        expResult = new Pair<>("1","");
        result = instance.getChangedBeforeAndAfter();
        assertEquals(expResult, result);
        instance = new ExampleReplace("1234", "134");
        expResult = new Pair<>("2","");
        result = instance.getChangedBeforeAndAfter();
        assertEquals(expResult, result);
        
    }

    
}
