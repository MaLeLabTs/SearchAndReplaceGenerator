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

package it.units.inginf.male.tree;

import it.units.inginf.male.tree.operator.Concatenator;
import java.util.ArrayList;
import java.util.List;

/**
 *
 * @author andrea
 */
public class NodeFactory {

    private final List<Node> functionSet;
    private final List<Leaf> terminalSet;

    public NodeFactory(NodeFactory factory){
        functionSet = new ArrayList<>(factory.getFunctionSet());
        terminalSet = new ArrayList<>(factory.getTerminalSet());
    }

    public NodeFactory() {
        functionSet = new ArrayList<>();
        terminalSet = new ArrayList<>();

        functionSet.add(new Concatenator());
        
    }

    public List<Node> getFunctionSet() {
        return functionSet;
    }

    public List<Leaf> getTerminalSet() {
        return terminalSet;
    }

    
}
