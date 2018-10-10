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
package it.units.inginf.male.strategy.impl;

import java.util.Collections;
import java.util.List;

/**
 *
 * @author fab
 */
public class PopStats {

    public PopStats() {
    }

    public PopStats(List<Integer> searchSizes, List<Integer> replaceSizes) {
        this.valid = !searchSizes.isEmpty() && !replaceSizes.isEmpty();
        if (valid) {
            this.replaceMedian = getMedian(replaceSizes);
            this.replaceAverage = getAverage(replaceSizes);
            this.searchMedian = getMedian(searchSizes);
            this.searchAverage = getAverage(searchSizes);
        }
    }

    public boolean valid = true;
    
    private int getMedian(List<Integer> numbers) {
        Collections.sort(numbers);
        return numbers.get(numbers.size() / 2);
    }

    private int getAverage(List<Integer> numbers) {
        long sum = 0;
        for (Integer number : numbers) {
            sum += number;
        }
        return (int) (sum /= numbers.size());
    }

    public int replaceMedian;
    public int searchMedian;
    public int replaceAverage;
    public int searchAverage;

    public String getCsvRow() {
        return "" + searchAverage + "," + searchMedian + "," + replaceAverage + "," + replaceMedian;
    }

    public String getCsvHeader() {
        return "searchAverage,searchMedian,replaceAverage,replaceMedian";
    }
}
