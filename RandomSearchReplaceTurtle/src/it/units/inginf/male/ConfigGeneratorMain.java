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
 */package it.units.inginf.male;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.units.inginf.male.configuration.Configuration;

import java.io.FileWriter;
import java.io.Writer;

/**
 * Created by andrea on 22/11/16.
 */
public class ConfigGeneratorMain {
    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws Exception {

        Configuration configuration = Configuration.getDefaultConfiguration();
        try (Writer writer = new FileWriter("default_configuration.json")) {
            Gson gson = new GsonBuilder().setPrettyPrinting().create();
            gson.toJson(configuration, writer);
            System.out.println("Default configuration created");
            return;
        }
    }

}
