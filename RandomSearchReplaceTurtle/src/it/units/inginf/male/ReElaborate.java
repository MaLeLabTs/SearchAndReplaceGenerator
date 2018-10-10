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

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.evaluators.TreeEvaluationException;
import it.units.inginf.male.outputs.Results;
import java.io.File;
import java.io.IOException;
import java.util.logging.FileHandler;
import java.util.logging.Logger;

/**
 * Alternative Main. This main is intended to load configuration and the last generation snapshot in
 * order to execute a different <code>BestSelector</code>.
 * The first argument is the name of the result file the second argument is the class name of the alternative <code>BestSelector</code> class.
 * The <code>configNameSuffix</code> is a string which is appended to the original configuration name, in this way the postProcessor should upload/save the new result with a meaningful name.
 * <code>ReElaborate</code> saves the <code>results.json</code> file into a sub-folder of the original output path; the name of the sub-folder is <code>configNameSuffix</code> 
 * @author Fabiano
 */
public class ReElaborate {

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws TreeEvaluationException, IOException, Exception {
        if (args.length < 3) {
            printUsage();
            System.exit(0);
        }

        Results  results = Results.load(args[0]);
        String bestSelectorClassName = args[1];
        String configNameSuffix = args[2];
        
        Configuration configuration = results.getConfiguration();
        configuration.setBestSelectorClass(bestSelectorClassName);
        configuration.setConfigName(configuration.getConfigName().concat(configNameSuffix));
        configuration.setOutputFolderName(configuration.getOutputFolderName()+File.separator+configNameSuffix);
        configuration.setup();
        Logger.getLogger("").addHandler(new FileHandler(new File(configuration.getOutputFolder(), "log.xml").getCanonicalPath()));
        
        long startTime = System.currentTimeMillis();
                 
        if (configuration.getPostProcessor() != null) {
            startTime = System.currentTimeMillis() - startTime;
            configuration.getPostProcessor().elaborate(configuration, results, startTime);
        }

    }

    private static void printUsage() {
        System.out.println("Usage: java -cp \"Random_Regex_Turtle.jar\" \"it.units.inginf.male.ReElaborate\" resultsFileName bestSelectorClassName configNameSuffix [startGui]");
    }
}
