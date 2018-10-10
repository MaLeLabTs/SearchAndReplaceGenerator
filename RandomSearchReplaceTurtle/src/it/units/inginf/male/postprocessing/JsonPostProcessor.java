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
package it.units.inginf.male.postprocessing;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import it.units.inginf.male.configuration.Configuration;
import it.units.inginf.male.configuration.SubConfiguration;
import it.units.inginf.male.evaluators.CachedCoevolutionaryEvaluator;
import it.units.inginf.male.inputs.DataSetReplace;
import it.units.inginf.male.outputs.FinalSolution;
import it.units.inginf.male.outputs.JobEvolutionTrace;
import it.units.inginf.male.outputs.Results;
import it.units.inginf.male.outputs.gson.DoubleTypeAdapter;
import it.units.inginf.male.utils.Utils;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author andrea
 */
public class JsonPostProcessor implements Postprocessor {

    @Override
    public void setup(Map<String, String> parameters) {
    }

    @Override
    public void elaborate(Configuration config, Results results, long timeTaken) {
        if (config.getEvaluator() instanceof CachedCoevolutionaryEvaluator) {
            CachedCoevolutionaryEvaluator evaluator = (CachedCoevolutionaryEvaluator)config.getEvaluator();
            long cacheSize = evaluator.getCacheSizeBytes();            
            System.out.println("Cache size: "+Utils.humanReadableByteCount(cacheSize, true));
            System.out.println("Cache entry: "+evaluator.getCacheSize());
        }
        System.out.println("Start evaluating results...");
        config.getBestSelector().elaborate(results);
        results.setOverallExecutionTimeMillis(timeTaken);
       //Populate knowledge avaiable 

        DataSetReplace training = config.getDatasetContainer().getTrainingDataset();
        DataSetReplace validation = config.getDatasetContainer().getValidationDataset();
        //training stats
        //Here is the KA from definitions
        int numberAllChars = config.getDatasetContainer().getDataset().getNumberOfChars();
        results.setNumberAllChars(numberAllChars);
        String pathOfFile = config.getOutputFolder().getAbsolutePath() + File.separator + "results.json";
        saveToJson(results, pathOfFile);
        
        String time = String.format("%d h, %d m, %d s",
                TimeUnit.MILLISECONDS.toHours(timeTaken),
                TimeUnit.MILLISECONDS.toMinutes(timeTaken) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeTaken)),
                TimeUnit.MILLISECONDS.toSeconds(timeTaken) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeTaken)));
        System.out.println("Time taken: "+time);
        
    }

    private void saveToJson(Results results, String pathOfFile) {
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().registerTypeAdapter(Double.class, new DoubleTypeAdapter()).create();
        String json = gson.toJson(results);
        saveFile(json, pathOfFile);

    }

    private void saveFile(String text, String pathOfFile) {
        Writer writer;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(pathOfFile), "utf-8");
            writer.write(text);
            writer.close();
        } catch (IOException ex) {
            Logger.getLogger(JsonPostProcessor.class.getName()).log(Level.SEVERE, "Cannot save:", ex);
        }

    }
   
}
