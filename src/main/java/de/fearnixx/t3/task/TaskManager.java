package de.fearnixx.t3.task;

import de.mlessmann.logging.ILogReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by MarkL4YG on 11.06.17.
 */
public class TaskManager extends Thread implements ITaskManager {

    private ILogReceiver log;

    private final Map<ITask, Long> tasks;
    private boolean terminated = false;

    public TaskManager(ILogReceiver log, int capacity) {
        this.log = log;
        tasks = new HashMap<>(capacity, 0.8f);
    }

    @Override
    public boolean hasTask(ITask task) {
        synchronized (tasks) {
            return tasks.containsKey(task);
        }
    }

    @Override
    public void scheduleTask(ITask task) {
        if (task == null || task.getRunnable() == null || task.getTimeUnit() == null) {
            throw new IllegalArgumentException("Tried to register an invalid or NULL task! Report this to the plugin dev!");
        }
        long delay = task.getTimeUnit().toSeconds(task.getInterval());
        if (task.getType() == ITask.TaskType.REPEAT && delay < 5) {
            throw new IllegalArgumentException("Repeating task MUST have an interval of at least 5 seconds");
        }
        if (task.getName() == null || task.getName().trim().isEmpty()) {
            log.warning("Some plugin tried to register a task missing a proper name. Task not registered");
            return;
        }
        synchronized (tasks) {
            if (tasks.containsKey(task))
                return;
            tasks.put(task, delay);
        }
        log.fine("Task", task.getName(), " scheduled for ", delay, " seconds");
    }

    @Override
    public void rescheduleTask(ITask task) {
        if (hasTask(task)) {
            removeTask(task);
        }
        scheduleTask(task);
    }

    @Override
    public void removeTask(ITask task) {
        synchronized (tasks) {
            tasks.remove(task);
        }
    }

    @Override
    public void runTask(ITask task) {
        synchronized (tasks) {
            tasks.remove(task);
        }
        log.finer("Running task ", task.getName());
        Runnable r = task.getRunnable();
        Thread t = new Thread(() -> {
            try {
                r.run();
            } catch (Throwable e) {
                log.warning("Uncaught exception from task: ", task.getName(), e);
            }
            if (task.getType() == ITask.TaskType.REPEAT) {
                scheduleTask(task);
            }
        });
        t.start();
    }

    @Override
    public void run() {
        List<ITask> toDo = new ArrayList<>();
        while (!terminated) {
            try {
                toDo.clear();
                synchronized (tasks) {
                    tasks.forEach((t, l) -> {
                        if (--l == 0) {
                            toDo.add(t);
                        } else {
                            if (l % 100 == 0)
                                log.finer(t.getName(), " has ", l, " seconds left");
                            tasks.replace(t, l);
                        }
                    });
                }
                toDo.forEach(this::runTask);
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // IF interrupted - automatically checks for termination
            }
        }
    }

    public void shutdown() {
        kill();
    }

    public void kill() {
        synchronized (tasks) {
            log.finer(tasks.size(), " task(s) scheduled upon shutdown.");
            tasks.clear();
            terminated = true;
        }
    }
}
