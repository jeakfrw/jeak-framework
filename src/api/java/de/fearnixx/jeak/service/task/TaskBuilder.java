package de.fearnixx.jeak.service.task;

import java.util.concurrent.TimeUnit;

/**
 * Builder to construct an object implementing {@link ITask}.
 */
public class TaskBuilder {
    private long delay;
    private TimeUnit tu;
    private ITask.TaskType type;
    private Runnable runnable;
    private String name;

    TaskBuilder() {
        reset();
    }

    /**
     * Resets this builder.
     * Built objects should keep their values
     * @return this
     */
    public TaskBuilder reset() {
        delay = 0;
        tu = TimeUnit.DAYS;
        type = ITask.TaskType.DELAY;
        runnable = null;
        name = null;
        return this;
    }

    /**
     * Set the task name - Should be somewhat unique
     * @param name The name
     * @return this
     */
    public TaskBuilder name(String name) {
        this.name = name;
        return this;
    }

    /**
     * Set the delay of this task - Resets interval!
     * @param l The delay
     * @param unit The {@link TimeUnit} of the delay (is converted to seconds)
     * @return this
     */
    public TaskBuilder delay(long l, TimeUnit unit) {
        type = ITask.TaskType.DELAY;
        this.tu = unit;
        this.delay = l;
        return this;
    }

    /**
     * Set the interval of this task - Resets delay!
     * @param l The delay
     * @param unit The {@link TimeUnit} of the interval (is converted to seconds)
     * @return this
     */
    public TaskBuilder interval(long l, TimeUnit unit) {
        type = ITask.TaskType.REPEAT;
        this.tu = unit;
        this.delay = l;
        return this;
    }

    /**
     * Set the {@link Runnable} for this task
     * @param r The Runnable
     * @return this
     */
    public TaskBuilder runnable(Runnable r) {
        this.runnable = r;
        return this;
    }

    /**
     * Construct the task
     * Can be used on {@link ITaskService#scheduleTask(ITask)}
     * @return The {@link ITask}
     */
    public ITask build() {
        final String fName = name;
        final ITask.TaskType fType = type;
        final long fL = delay;
        final TimeUnit fTU = tu;
        final Runnable fR = runnable;
        return new ITask() {
            @Override
            public long getDelay() {
                return fL;
            }

            @Override
            public long getInterval() {
                return fL;
            }

            @Override
            public TimeUnit getTimeUnit() {
                return tu;
            }

            @Override
            public TaskType getType() {
                return fType;
            }

            @Override
            public Runnable getRunnable() {
                return fR;
            }

            @Override
            public boolean shouldReschedule() {
                return true;
            }

            @Override
            public String getName() {
                return fName;
            }
        };
    }
}
