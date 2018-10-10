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
package it.units.inginf.male.generations;

import it.units.inginf.male.tree.Node;
import it.units.inginf.male.utils.MappingOperation;
import java.util.Deque;
import junit.framework.TestCase;

/**
 *
 * @author fab
 */
public class ReplaceWithGroupsPopulationBuilderTest extends TestCase {
    
    public ReplaceWithGroupsPopulationBuilderTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    public void testCreateGroupedExample() {
        System.out.println("createGroupedExample");
        MappingOperation mapperOperation = new MappingOperation("ciao 12-1356z", "c:1356:12yu", false);
        ReplaceWithGroupsPopulationBuilder instance = new ReplaceWithGroupsPopulationBuilder();
        
        //case no-compact
        String expResult = "$1:$3:$2yu";
        Node resultNode = instance.createGroupedReplaceExample(mapperOperation);
        StringBuilder builder = new StringBuilder();
        resultNode.describe(builder);
        String resultString = builder.toString();
        assertEquals(expResult, resultString);
        
        
        
        
    }
    
}
