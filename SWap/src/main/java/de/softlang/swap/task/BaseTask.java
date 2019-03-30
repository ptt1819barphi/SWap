package de.softlang.swap.task;

public abstract class BaseTask implements Task {

    protected final String name;

    public BaseTask(String name) {
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }
}
