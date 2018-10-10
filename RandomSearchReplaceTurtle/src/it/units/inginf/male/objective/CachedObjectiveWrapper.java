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
 */package it.units.inginf.male.objective;

import it.units.inginf.male.utils.CacheInterface;
import it.units.inginf.male.evaluators.CoevolutionaryEvaluator;
import it.units.inginf.male.inputs.Context;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.utils.Triplet;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.atomic.AtomicLong;

/**
 * This class wraps an objective and cache it.
 * Default constructor has to be implemented in subclasses
 * Created by Fabiano
 */
public abstract class CachedObjectiveWrapper implements Objective, CacheInterface {
    private Context context;
    private final Objective wrappedOjective;
    private final int objectiveID;
    
    private static transient Map<Triplet<Integer, Context.EvaluationPhases, String>, double[]> cache = new WeakHashMap<>();
    //private static final Object cacheOwner = new Object();
    
    private static final AtomicLong hit = new AtomicLong(0);
    private static final AtomicLong miss = new AtomicLong(0);

    public CachedObjectiveWrapper(Objective wrappedOjective) {
        this.wrappedOjective = wrappedOjective;
        this.objectiveID = wrappedOjective.hashCode();
    }

    @Override
    public void setup(Context context) {
        this.wrappedOjective.setup(context);
        this.context = context;
    }

    @Override
    public double[] fitness(Node individual) {
        
        double[] result;
        
        StringBuilder sb = new StringBuilder();
        individual.describe(sb);
        Triplet<Integer, Context.EvaluationPhases, String> key = new Triplet<>(objectiveID, context.getPhase(), sb.toString());
        synchronized (cache) {
            result = cache.get(key);
        }
        if (result != null) {
            hit.incrementAndGet();
            return result;
        }

        miss.incrementAndGet();
        result = this.wrappedOjective.fitness(individual);

        synchronized (cache) {
            cache.putIfAbsent(key, result);
        }
        
        return result;
        
    }

    @Override
    public CoevolutionaryEvaluator getTreeEvaluator() {
        return context.getConfiguration().getEvaluator();
    }

    @Override
    public abstract Objective cloneObjective();

    @Override
    public double getRatio(){
        return (double)hit.get()/(hit.get()+miss.get());
    }

    @Override
    public long getCacheSizeBytes(){
        long cacheSize = 0;
        synchronized (cache) {
            for (Map.Entry<Triplet<Integer, Context.EvaluationPhases, String>, double[]> entry : cache.entrySet()) {
                //Triplet<EvaluationPhases, Boolean, String> triplet = entry.getKey();
                double[] cachedValue = entry.getValue();
                Triplet<Integer, Context.EvaluationPhases, String> cachedKey = entry.getKey();
                cacheSize += cachedValue.length * Double.SIZE / 8; //Bytes size
                cacheSize += (2 * Integer.SIZE + 8 * ((cachedKey.getThird().length()  * 2) + 45)) / 8;
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
    public Object getCache() {
        return cache;
    }
    
}
