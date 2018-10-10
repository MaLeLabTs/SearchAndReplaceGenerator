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
public class SearchWithGroupsPopulationBuilderTest extends TestCase {
    
    public SearchWithGroupsPopulationBuilderTest(String testName) {
        super(testName);
    }
    
    @Override
    protected void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Test of createGroupedExample method, of class SearchWithGroupsPopulationBuilder.
     */
    public void testCreateGroupedExample() {
        System.out.println("createGroupedExample");
        MappingOperation mapperOperation = new MappingOperation("ciao 12-1356z", "c:1356:12", true);
        SearchWithGroupsPopulationBuilder instance = new SearchWithGroupsPopulationBuilder(true, true); //dottification with word classes
        
        //case no-compact
        boolean replace = true;
        boolean compact = false;
        String expResult = "(\\w)\\w\\w\\w\\s(\\d\\d)\\-(\\d\\d\\d\\d)\\w";
        Node resultNode = instance.createGroupedExample(mapperOperation, replace, compact);
        StringBuilder builder = new StringBuilder();
        resultNode.describe(builder);
        String resultString = builder.toString();
        assertEquals(expResult, resultString);
        
        //case compact
        replace = true;
        compact = true;
        expResult = "(\\w)\\w++\\s(\\d++)\\-(\\d++)\\w";
        resultNode = instance.createGroupedExample(mapperOperation, replace, compact);
        builder = new StringBuilder();
        resultNode.describe(builder);
        resultString = builder.toString();
        assertEquals(expResult, resultString);
        
        
    }

    

    
    
}
