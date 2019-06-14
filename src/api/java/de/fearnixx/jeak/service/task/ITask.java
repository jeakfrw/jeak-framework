package de.fearnixx.jeak.service.task;

import java.util.concurrent.TimeUnit;

/**
 * Created by MarkL4YG on 11.06.17.
 *
 * Abstract task representation used by the {@link ITaskService}s.
 * Allows plugins to register asynchronous tasks
 *
 * See {@link TaskBuilder} to construct one
 */
public interface ITask {

    static TaskBuilder builder() {
        return new TaskBuilder();
    }

    /**
     * Determines the type of the task
     * Delayed or Repeated
     */
    enum TaskType {
        REPEAT,
        DELAY
    }

    /**
     * The tasks delay
     * @return The delay
     */
    long getDelay();

    /**
     * The tasks interval
     * @return The interval
     */
    long getInterval();

    /**
     * The {@link TimeUnit} of the interval or delay
     * @return The TimeUnit
     */
    TimeUnit getTimeUnit();

    /**
     * The {@link TaskType} of the task
     * @return The type
     */
    TaskType getType();

    /**
     * By default, tasks that are set to an interval will be rescheduled after they have been run.
     * Return false for when the task should no longer be re-scheduled.
     * @apiNote As this is for advanced tasks, this cannot be changed using the task builder. <br>Use {@link TaskType#DELAY} for the builder.
     */
    boolean shouldReschedule();

    /**
     * The {@link Runnable} of the task
     * @return The runnable
     */
    Runnable getRunnable();

    /**
     * The - somewhat - unique name of the task (For logging purposes mostly)
     * @return The name
     */
    String getName();
}
