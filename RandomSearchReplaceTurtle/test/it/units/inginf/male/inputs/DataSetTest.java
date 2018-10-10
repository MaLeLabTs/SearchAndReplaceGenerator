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

package it.units.inginf.male.inputs;


import java.util.Arrays;
import java.util.List;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author fab
 */
public class DataSetTest {
    
    public DataSetTest() {
    }
    
    @BeforeClass
    public static void setUpClass() {
    }
    
    @AfterClass
    public static void tearDownClass() {
    }
    
    @Before
    public void setUp() {
    }
    
    @After
    public void tearDown() {
    }

    

    /**
     * Test of initStripedDatasetView method, of class DataSet.
     */
    @Test
    public void testInitStripedDatasetView() {
        DataSet dataSet = new DataSet("test", "striping test", "");
        Example example = new Example();
        example.setString("123456789123456789PROVA123456789123456789 123456789123456789123456789123456789PROVA123456789123456789");
        int provaIndex1 = example.getString().indexOf("PROVA");
        int provaIndex2 = example.getString().indexOf("PROVA", provaIndex1+2);
        example.getMatch().add(new Bounds(provaIndex1,provaIndex1+"PROVA".length()));
        example.getMatch().add(new Bounds(provaIndex2,provaIndex2+"PROVA".length()));
        dataSet.getExamples().add(example);
        dataSet.updateStats();
        int marginSize = 2;
        DataSet stripedDataset = dataSet.initStripedDatasetView(marginSize);
        int expExperimentsNumber = 2;
        assertEquals(expExperimentsNumber, stripedDataset.getNumberExamples());
        for(Example stripedExample : stripedDataset.getExamples()){
            stripedExample.populateAnnotatedStrings();
            assertEquals("PROVA".length()*(marginSize+1), stripedExample.getString().length());
            for(String matchString : stripedExample.getMatchedStrings()){
                assertEquals("PROVA", matchString);
            }
        }
        
        //Test the boundires merge operation
        marginSize = 20;
        stripedDataset = dataSet.initStripedDatasetView(marginSize);
        expExperimentsNumber = 1;
        assertEquals(expExperimentsNumber, stripedDataset.getNumberExamples());
        for(Example stripedExample : stripedDataset.getExamples()){
            stripedExample.populateAnnotatedStrings();
            //Example should be unaltered
            assertEquals(example.getString(), stripedExample.getString());
            for(String matchString : stripedExample.getMatchedStrings()){
                assertEquals("PROVA", matchString);
            }
            assertArrayEquals(example.getMatch().toArray(), stripedExample.getMatch().toArray());
        }
    }
    
    /**
     * Test of stripeExample method, of class DataSet.
     */
    @Test
    public void testStripeExample() {
        Example example = new Example();
        example.setString("I'm an example 123 Unnannotated1234 hello");
        example.getMatch().add(new Bounds(15,18));
        example.getMatch().add(new Bounds(31,35));
        example.getUnmatch().add(new Bounds(0,15));
        example.getUnmatch().add(new Bounds(18,19));
        example.getUnmatch().add(new Bounds(35,41));
        List<Example> stripedExampels = DataSet.stripeExample(example, 2);
        assertEquals(2, stripedExampels.size());
        assertTrue(stripedExampels.get(0).getMatchedStrings().contains("123") &&  stripedExampels.get(0).getUnmatchedStrings().contains("le ") 
                &&  stripedExampels.get(0).getUnmatchedStrings().contains(" ")&&  stripedExampels.get(0).getUnmatchedStrings().size()==2);
        assertTrue(stripedExampels.get(1).getMatchedStrings().contains("1234") &&  stripedExampels.get(1).getUnmatchedStrings().contains(" hel") &&  stripedExampels.get(1).getUnmatchedStrings().size()==1);
    }

    /**
     * Test of stripeExample method, of class DataSet.
     */
    @Test
    public void testBoundsCountRangesCollidingZone() {
        List<Bounds> ranges = Arrays.asList(new Bounds(1, 10),new Bounds(13, 15),new Bounds(43, 56));
        List<Bounds> ranges2 = Arrays.asList(new Bounds(100, 102));
        List<Bounds> zone = Arrays.asList(new Bounds(14, 60));
        assertEquals(2,Bounds.countRangesThatCollideZone(ranges, zone));
        assertEquals(0,Bounds.countRangesThatCollideZone(ranges2, zone));
    }
      
}
