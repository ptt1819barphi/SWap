package de.softlang.swap.task;

import de.softlang.swap.config.TaskConfig;
import de.softlang.swap.config.TaskConfigSet;
import java.util.HashSet;
import java.util.Set;
import org.apache.spark.sql.SparkSession;

public class TaskManager {
    
    private static final Set<Task> TASKS = new HashSet<>();
    
    public static void register(Task task){
        TASKS.add(task);
    }
    
    public static void execute(SparkSession session, TaskConfigSet configSet){
        for (TaskConfig taskConfig : configSet.getTaskConfigs()) {
            if (taskConfig.isSkip()){
                continue;
            }
            taskConfig.setWorkspaceIfAbsent(configSet.getWorkspace());
            execute(session, taskConfig);
        }
    }
    
    public static void execute(SparkSession session, TaskConfig taskConfig){
        for (Task task : TASKS) {
            if (task.getName().equals(taskConfig.getName())){
                task.execute(taskConfig.getEnvironment(), session);
                return;
            }
        }
        throw new IllegalArgumentException("No Task with the name " + taskConfig.getName() + " found.");
    }
    
}
