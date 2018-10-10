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
 */package it.units.inginf.male.evaluators;

import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.coevolution.Forest;
import it.units.inginf.male.utils.Pair;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * Created by andrea on 21/11/16.
 */
public class DefaultCachedReplaceEvaluator extends DefaultReplaceEvaluator implements CachedReplaceEvaluator {

    private final WeakHashMap<Pair<Context.EvaluationPhases, String>, List<ReplaceResult>> cache = new WeakHashMap<>();
    private long hit = 0;
    private long miss = 0;

    @Override
    public double getRatio(){
        return (double)this.hit/(this.hit+this.miss);
    }

    @Override
    public long getCacheSizeBytes(){
        long cacheSize = 0;
        for (Map.Entry<Pair<Context.EvaluationPhases, String>, List<ReplaceResult>> entry : cache.entrySet()) {
            //Triplet<EvaluationPhases, Boolean, String> triplet = entry.getKey();
            List<ReplaceResult> list = entry.getValue();
            for (ReplaceResult exampleResult : list) {
                cacheSize+=exampleResult.length();
            }
        }
        cacheSize*=(Integer.SIZE/4);
        return cacheSize;
    }

    @Override
    public int getCacheSize() {
        return cache.size();
    }

    @Override
    public void setup(Map<String, String> parameters) {

    }

    @Override
    public List<ReplaceResult> evaluate(Forest root, Context context) throws TreeEvaluationException {
        StringBuilder sb = new StringBuilder();
        root.describe(sb);
        List<ReplaceResult> results;
        Pair<Context.EvaluationPhases, String> key = new Pair<>(context.getPhase(), sb.toString());
        synchronized (cache) {
            results = cache.get(key);
        }
        if (results != null) {
            hit++;
            return results;
        }

        miss++;
        results = super.evaluate(root, context);

        synchronized (cache) {
            cache.put(key, results);
        }
        return results;
    }

    @Override
    public Object getCache() {
        return cache;
    }
}
