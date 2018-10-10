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

package it.units.inginf.male.objective;

import it.units.inginf.male.evaluators.CoevolutionaryEvaluator;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.inputs.Context;


/**
 * Remind that Node represents an individual or may represent a set of individual
 * for a coevolutional search.
 * @author andrea
 */
public interface Objective {

    public void setup(Context context);
    public double[] fitness(Node individual);
    CoevolutionaryEvaluator getTreeEvaluator();
    Objective cloneObjective();
}
