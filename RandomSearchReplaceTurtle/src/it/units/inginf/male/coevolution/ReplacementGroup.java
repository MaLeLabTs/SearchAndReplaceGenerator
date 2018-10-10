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
 */package it.units.inginf.male.coevolution;

import it.units.inginf.male.tree.Constant;
import it.units.inginf.male.tree.DescriptionContext;
import it.units.inginf.male.tree.Node;
import it.units.inginf.male.tree.operator.UnaryOperator;

/**
 * Created by andrea on 21/11/16.
 */
public class ReplacementGroup extends UnaryOperator {
    @Override
    public void describe(StringBuilder builder, DescriptionContext context, RegexFlavour flavour) {
        Node child = getChildrens().get(0);
        switch (flavour) {
            case JAVA:
                builder.append("$");
                break;
            default:
                builder.append("\\");
        }
            child.describe(builder, context, flavour);
        }

        @Override
        public boolean isValid () {
            Node child = getChildrens().get(0);

            if(child instanceof Constant){
                int val;
                try {
                    val = Integer.parseInt(child.toString());
                } catch (NumberFormatException ex) {
                    return false;
                }

                if(val<0){
                    return false;
                }

                return true;
            }

            return false;
        }

        @Override
        protected UnaryOperator buildCopy () {
            return new ReplacementGroup();
        }
    }
