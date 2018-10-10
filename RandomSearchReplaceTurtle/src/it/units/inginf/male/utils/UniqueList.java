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


import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

/**
 * @author andrea
 */
public class UniqueList<E> extends ArrayList<E> {

    private Set<String> hashes = new HashSet<>();

    
    public UniqueList() {
        
    }
    
    public UniqueList(int initialCapacity) {
        super(initialCapacity);
    }

    public UniqueList(Collection<? extends E> c) {
        super(c.size());
        this.addAll(c);
    }

    @Override
    public boolean add(E e) {
        String hash = e.toString();
        if (hashes.contains(hash)) {
            return false;
        }
        hashes.add(hash);
        return super.add(e);
    }

    @Override
    public boolean addAll(Collection<? extends E> c) {
        boolean ret = false;
        for (E n : c) {
            ret = this.add(n) | ret;
        }
        return ret;
    }

    @Override
    public void clear() {
        super.clear();
        this.hashes.clear();
    }

    @Override
    public boolean contains(Object o) {
        return this.hashes.contains(o.toString()); 
    }

    @Override
    public boolean remove(Object o) {
        boolean isRemoved = super.remove(o);
        if(isRemoved && (o != null)){
            this.hashes.remove(o.toString());
        }
        return isRemoved; 
    }

    @Override
    public E remove(int i) {
        E removed = super.remove(i);
        if(removed != null){
            this.hashes.remove(removed.toString());
        }
        return removed;  
    }

    @Override
    public boolean removeAll(Collection<?> clctn) {
        for (Object object : clctn) {
            if(object != null){
                this.hashes.remove(object.toString());
            }
        }
        return super.removeAll(clctn); 
    }
    
}
