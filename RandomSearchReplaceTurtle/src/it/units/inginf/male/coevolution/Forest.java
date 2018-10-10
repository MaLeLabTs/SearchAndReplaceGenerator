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
package it.units.inginf.male.coevolution;

import it.units.inginf.male.tree.DescriptionContext;
import it.units.inginf.male.tree.Node;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
/**
 * Permits the creation of the individuals list with:
 * Forest coevGuy = new Forest(node1, node2);
 * 
 * @author Fabiano
 */
public class Forest extends ArrayList<Node> implements Node{

    public Forest(int i) {
        super(i);
    }

    public Forest() {
    }

    public Forest(Collection<? extends Node> clctn) {
        super(clctn);
    }
    
    public Forest(Node... individuals){
        super(Arrays.asList(individuals));
    }

    @Override
    public Node cloneTree() {
        Forest tempCoev = new Forest(this.size());
        for (Node node : this) {
            tempCoev.add(node.cloneTree());
        }
        return tempCoev;
    }

    @Override
    public Node getParent() {
        return null;
    }

    @Override
    public void setParent(Node parent) {
        throw new UnsupportedOperationException(""+this.getClass()+" can be only a root node.");
        //Do nothing 
    }

    @Override
    public int getMinChildrenCount() {
        return 1;
    }

    @Override
    public int getMaxChildrenCount() {
        return Integer.MAX_VALUE;
    }

    @Override
    public List<Node> getChildrens() {
       return this;
    }

    @Override
    public long getId() {
        return Integer.MIN_VALUE;
    }

    @Override
    public void describe(StringBuilder builder){
        builder.append("{");
        for(Node thi : this) {
            thi.describe(builder);
            builder.append("},{");
        }
        builder.delete(builder.length()-2, builder.length());
    }

    @Override
    public void describe(StringBuilder builder, DescriptionContext context, RegexFlavour flavour) {
        throw new UnsupportedOperationException("Only plain describe(StringBuffer) is supported");
    }

    @Override
    public boolean isValid() {
        boolean isValidValue = true;
        for (Node thi : this) {
            isValidValue &= thi.isValid();;
        }
        return isValidValue;
    }

    @Override
    public boolean isCharacterClass() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean isEscaped() {
        throw new UnsupportedOperationException();
    }

    @Override
    public int getAge() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void growOld() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void resetAge() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void setAge(int age) {
        throw new UnsupportedOperationException();
    }
    
}
