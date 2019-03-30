package de.softlang.swap.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class TaskConfig {

    private final String name;
    private final boolean skip;
    private Map<String, String> environment;

    public TaskConfig(String name, boolean skip, Map<String, String> environment) {
        this.name = name;
        this.skip = skip;
        this.environment = environment;
    }

    public String getName() {
        return name;
    }

    public boolean isSkip() {
        return skip;
    }

    public Map<String, String> getEnvironment() {
        return Collections.unmodifiableMap(environment);
    }

    public void setWorkspaceIfAbsent(String workspace) {
        if (environment == null) {
            environment = new HashMap<>();
        }
        environment.putIfAbsent("workspace", workspace);
    }

    public String getVariable(String variable) {
        return environment.get(variable);
    }
}
