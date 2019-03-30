package de.softlang.swap.task;

import java.util.Map;
import org.apache.spark.sql.SparkSession;

public class SleepTask extends BaseTask{

    public SleepTask() {
        super("sleep");
    }
    

    @Override
    public void execute(Map<String, String> environment, SparkSession session) {
        try {
            Thread.sleep(Long.parseLong(environment.get("time")));
        } catch (InterruptedException ex) {
        }
    }
    
}
