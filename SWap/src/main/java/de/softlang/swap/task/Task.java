package de.softlang.swap.task;

import java.util.Map;
import org.apache.spark.sql.SparkSession;

public interface Task {
    
    String getName();
    
    void execute(Map<String,String> environment, SparkSession session);
    
}
