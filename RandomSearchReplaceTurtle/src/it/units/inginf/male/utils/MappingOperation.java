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
package it.units.inginf.male.utils;

import it.units.inginf.male.inputs.Bounds;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

/**
 *
 * @author fabiano
 */
public class MappingOperation {

    public MappingOperation(Pair<String, String> changed, boolean orderMappingForSource) {
        this(changed.getFirst(), changed.getSecond(), orderMappingForSource);
    }

    public MappingOperation(String source, String target, boolean orderMappingForSource) {
        this.source = source;
        this.target = target;
        this.mapping = Utils.findLargestCommonSubstringNew(source, target);
        
        int groupCount = 0;
        for (Pair<Bounds, Bounds> pair : this.mapping) {
            groupCount++;
            this.groupIDmap.put(pair, groupCount);
        }
        
        if(!orderMappingForSource){
            Collections.sort(this.mapping, (Pair<Bounds, Bounds> f1, Pair<Bounds, Bounds> f2) -> (f1.getSecond().compareTo(f2.getSecond())));
        }
    }

    public String source;
    public String target;
    public List<Pair<Bounds, Bounds>> mapping;
    public Map<Pair<Bounds, Bounds>,Integer> groupIDmap = new HashMap<>();
    
    @Override
    public int hashCode() {
        int hash = this.source.hashCode() + this.target.hashCode();
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        final MappingOperation other = (MappingOperation) obj;
        if (!Objects.equals(this.source, other.source)) {
            return false;
        }
        if (!Objects.equals(this.target, other.target)) {
            return false;
        }
        return true;
    }

}
