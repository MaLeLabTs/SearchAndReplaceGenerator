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
package it.units.inginf.male.objective;

import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author andrea
 */
public class Domination {

    private List<Ranking> dominated = new LinkedList<Ranking>();
    private int dominatedCount = 0;

    public List<Ranking> getDominated() {
        return dominated;
    }

    public int getDominatedCount() {
        return dominatedCount;
    }

    public void incDominatedCount() {
        dominatedCount++;
    }
    
    public void decDominatedCount() {
        dominatedCount--;
    }
}
