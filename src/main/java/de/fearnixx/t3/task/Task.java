package de.fearnixx.t3.task;

import java.util.concurrent.TimeUnit;

/**
 * Created by MarkL4YG on 11.06.17.
 */
public class Task implements ITask {

    private String name;
    private long l;
    private TimeUnit unit;
    private Runnable r;
    private TaskType t;

    private Task(String name, long l, TimeUnit unit, Runnable r, TaskType type) {
        this.name = name;
        this.l = l;
        this.unit = unit;
        this.r = r;
        this.t = type;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public long getDelay() {
        return l;
    }

    @Override
    public long getInterval() {
        return l;
    }

    @Override
    public Runnable getRunnable() {
        return r;
    }

    @Override
    public TimeUnit getTimeUnit() {
        return unit;
    }

    @Override
    public TaskType getType() {
        return t;
    }
}
