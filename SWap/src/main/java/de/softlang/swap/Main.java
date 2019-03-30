package de.softlang.swap;

import com.google.gson.Gson;
import de.softlang.swap.config.TaskConfigSet;
import de.softlang.swap.task.CategoryDepthTask;
import de.softlang.swap.task.ConsoleTask;
import de.softlang.swap.task.SleepTask;
import de.softlang.swap.task.StanfordNERTask;
import de.softlang.swap.task.StanfordNounTask;
import de.softlang.swap.task.SummaryExtractionTask;
import de.softlang.swap.task.TaskManager;
import de.softlang.swap.task.WikiCleanTask;
import java.io.FileNotFoundException;
import java.io.FileReader;
import org.apache.spark.SparkConf;
import org.apache.spark.sql.SparkSession;

public class Main {

    public static void main(String[] args) throws FileNotFoundException {
        Gson gson = new Gson();
        TaskConfigSet taskSet;
        if (args.length == 1) {
            taskSet = gson.fromJson(new FileReader(args[0]), TaskConfigSet.class);
        } else {
            throw new IllegalArgumentException("No config specified.");
        }

        SparkSession spark = SparkSession.builder()
                .appName("SWap")
                .config(new SparkConf().setMaster("local[8]"))
                .config("spark.driver.maxResultSize", "2g")
                .getOrCreate();

        TaskManager.register(new SleepTask());
        TaskManager.register(new ConsoleTask());
        TaskManager.register(new CategoryDepthTask());
        TaskManager.register(new SummaryExtractionTask());
        TaskManager.register(new WikiCleanTask());
        TaskManager.register(new StanfordNERTask());
        TaskManager.register(new StanfordNounTask());

        TaskManager.execute(spark, taskSet);
    }
}
