package de.softlang.swap.config;

public class TaskConfigSet {
    
    private final String workspace;
    private final TaskConfig[] taskConfigs;
    
    public TaskConfigSet(String workspace, TaskConfig[] taskConfigs) {
        this.workspace = workspace;
        this.taskConfigs = taskConfigs;
    }

    public String getWorkspace() {
        return workspace;
    }

    public TaskConfig[] getTaskConfigs() {
        return taskConfigs;
    }
    
    public TaskConfig getTaskConfig(int index) {
        return taskConfigs[index];
    }
}
