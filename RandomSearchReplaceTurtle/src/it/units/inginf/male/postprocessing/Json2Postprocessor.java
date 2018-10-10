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
import it.units.inginf.male.evaluators.CachedCoevolutionaryEvaluator;
import it.units.inginf.male.utils.CacheInterface;
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
public class Json2Postprocessor implements Postprocessor {

    private static Logger LOG = Logger.getLogger(Json2Postprocessor.class.getName());
    
    @Override
    public void setup(Map<String, String> parameters) {
        
    }

    @Override
    public void elaborate(Configuration config, Results results, long timeTaken) {
        
        if (config.getEvaluator() instanceof CachedCoevolutionaryEvaluator) {
            CachedCoevolutionaryEvaluator evaluator = (CachedCoevolutionaryEvaluator) config.getEvaluator();
            long cacheSize = evaluator.getCacheSizeBytes();
            System.out.println("Evaluator cache size: " + Utils.humanReadableByteCount(cacheSize, true));
            System.out.println("Evaluator cache number entries: " + evaluator.getCacheSize());
        }
        if (config.getObjective() instanceof CacheInterface) {
            CacheInterface cachedObjective = (CacheInterface) config.getObjective();
            long cacheSize = cachedObjective.getCacheSizeBytes();
            System.out.println("Objective cache size: " + Utils.humanReadableByteCount(cacheSize, true));
            System.out.println("Objective cache number entries: " + cachedObjective.getCacheSize());
        }
        
        System.out.println("Start evaluating results...");
        //crunches the results file and find out the best individual
        config.getBestSelector().elaborate(results);
        results.setOverallExecutionTimeMillis(timeTaken);

        double numberAverageGenerationsPerJob = 0;
        for (JobEvolutionTrace jobTrace : results.getJobEvolutionTraces()) {
            numberAverageGenerationsPerJob += jobTrace.getNumberJobGenerations();
        }
        numberAverageGenerationsPerJob /= results.getJobEvolutionTraces().size();
        results.setAverageNumberGenerationsPerJob(numberAverageGenerationsPerJob);

        //DataSetReplace training = config.getDatasetContainer().getTrainingDataset();
        //DataSetReplace validation = config.getDatasetContainer().getValidationDataset();
        //Populate knowledge avaiable 
        //training stats
        int numberAllChars = config.getDatasetContainer().getTrainingDataset().getNumberOfChars();
        results.setNumberAllChars(numberAllChars);
        int numberReplacements = config.getDatasetContainer().getTrainingDataset().getNumberReplacements();
        results.setNumberReplacements(numberReplacements);
        int numberExamples = config.getDatasetContainer().getTrainingDataset().getNumberExamples();
        results.setNumberExamples(numberExamples);
        System.out.println("Saving results to file...");
        save(results);

        //save(results);
        String time = String.format("%d h, %d m, %d s",
                TimeUnit.MILLISECONDS.toHours(timeTaken),
                TimeUnit.MILLISECONDS.toMinutes(timeTaken) - TimeUnit.HOURS.toMinutes(TimeUnit.MILLISECONDS.toHours(timeTaken)),
                TimeUnit.MILLISECONDS.toSeconds(timeTaken) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(timeTaken)));
        System.out.println("Time taken: " + time);
    }

    private void saveFile(String text, String pathOfFile) {
        this.saveFile(text, pathOfFile, false);
    }

    private void saveFile(String text, String pathOfFile, boolean append) {
        Writer writer;
        try {
            writer = new OutputStreamWriter(new FileOutputStream(pathOfFile, append), "utf-8");
            writer.write(text);
            writer.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Cannot save:", ex);
        }

    }

    private void save(Results results) {
        //note: we need a Double serialization for NaN
        Gson gson = new GsonBuilder().disableHtmlEscaping().setPrettyPrinting().serializeNulls().registerTypeAdapter(Double.class, new DoubleTypeAdapter()).create();
        String json = gson.toJson(results);

        //save results json to file too. The output folder is in configuration file (which is in results)
        String pathOfFile = results.getConfiguration().getOutputFolder().getAbsolutePath() + File.separator + "results.json";
        saveFile(json, pathOfFile);

    }

}
