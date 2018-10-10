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
 */package it.units.inginf.male.objective.performance;

import it.units.inginf.male.objective.*;
import it.units.inginf.male.tree.Node;

/**
 * Created by Fabiano
 * DefaultCachedReplaceEvaluatorFix has a fix that IS needed in order to work
 */
public class EditComplexitySearchRecallObjective extends EditSearchRecallComplexityObjective{
    

    @Override
    public double[] fitness(Node individual) {
        double[] fitness = super.fitness(individual);
        return new double[]{fitness[0],fitness[2],fitness[1]};
    }


    @Override
    public Objective cloneObjective() {
        return new EditComplexitySearchRecallObjective();
    }
}
