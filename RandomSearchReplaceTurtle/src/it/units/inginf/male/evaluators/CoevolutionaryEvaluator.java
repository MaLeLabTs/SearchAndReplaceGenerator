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
package it.units.inginf.male.evaluators;

import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.coevolution.Forest;

import java.util.List;
import java.util.Map;

/**
 *
 * @author fabiano
 * @param <T> evaluator returned value type
 */
public interface CoevolutionaryEvaluator<T> {

    public void setup(Map<String,String> parameters);
    List<T> evaluate(Forest root, Context context) throws TreeEvaluationException;
     
}
