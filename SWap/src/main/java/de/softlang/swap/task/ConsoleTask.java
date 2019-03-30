package de.softlang.swap.task;

import java.io.IOException;
import java.util.Map;
import org.apache.spark.sql.SparkSession;

public class ConsoleTask extends BaseTask {

    public ConsoleTask() {
        super("console");
    }

    @Override
    public void execute(Map<String, String> environment, SparkSession session) {
        String command = environment.get("command");
        try {
            Process process = new ProcessBuilder(command).start();
            if (Boolean.getBoolean(environment.getOrDefault("wait", "false"))) {
                process.waitFor();
            }

        } catch (IOException | InterruptedException ex) {
        }
    }
}
