package de.fearnixx.t3.task;

/**
 * Created by MarkL4YG on 11.06.17.
 *
 * Each bot provides its own task manager to its plugins
 * This allows plugins to schedule asynchronous tasks based on a delay or interval
 * @see ITask for further informaton
 */
public interface ITaskManager {

    /**
     * Checks if a task is already registered - By {@link java.util.Map#containsKey(Object)}
     * @param task The task
     * @return
     */
    boolean hasTask(ITask task);

    /**
     * Registers a task according to the tasks properties
     * Task is not run!
     * @param task The task
     */
    void scheduleTask(ITask task);

    /**
     * Removes a task from the queue and then calls {@link #scheduleTask(ITask)}
     * Effectively resets the delay/interval
     * @param task The task
     */
    void rescheduleTask(ITask task);

    /**
     * Asynchronously runs a task.
     * Also calls {@link #scheduleTask(ITask)} if the {@link de.fearnixx.t3.task.ITask.TaskType} is {@link de.fearnixx.t3.task.ITask.TaskType#REPEAT}
     * @param task The task
     */
    void runTask(ITask task);

    /**
     * Removes a task from the queue
     * @param task The task
     */
    void removeTask(ITask task);
}
