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

package it.units.inginf.male.terminalsets;

import it.units.inginf.male.configuration.SubConfiguration;
import it.units.inginf.male.inputs.Context;

/**
 * This terminal set do nothing. New TerminalSet nodes are not added to the NodeFactory.   
 * @author Fabiano
 */
public class EmptyTerminalSetBuilder implements TerminalSetBuilder{

    @Override
    public void setup(SubConfiguration configuration) {
        //Nothing to do
    }

    @Override
    public void setup(Context context, SubConfiguration configuration) {
        //Nothing to do
    }
    
}
