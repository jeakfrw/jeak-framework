package de.fearnixx.t3.task;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import de.fearnixx.t3.Main;
import de.fearnixx.t3.service.task.ITask;
import de.fearnixx.t3.service.task.ITaskService;
import de.mlessmann.logging.ILogReceiver;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

/**
 * Created by MarkL4YG on 11.06.17.
 */
public class TaskService extends Thread implements ITaskService {

    public static final Integer THREAD_POOL_SIZE = 10;
    public static Integer AWAIT_TERMINATION_DELAY = 5000;

    private ILogReceiver log;

    private final Map<ITask, Long> tasks;
    private boolean terminated = false;

    private ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("task-scheduler-%d").build();
    private ExecutorService taskExecutor;

    public TaskService(ILogReceiver log, int capacity) {
        this.log = log;
        tasks = new HashMap<>(capacity, 0.8f);
        taskExecutor = Executors.newFixedThreadPool(Main.getProperty("bot.taskmgr.poolsize", THREAD_POOL_SIZE), threadFactory);
        AWAIT_TERMINATION_DELAY = Main.getProperty("bot.eventmgr.terminatedelay", AWAIT_TERMINATION_DELAY);
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
        final Runnable r = task.getRunnable();
        taskExecutor.execute(() -> {
            try {
                r.run();
            } catch (Throwable e) {
                log.warning("Uncaught exception from task: ", task.getName(), e);
            }
            if (task.getType() == ITask.TaskType.REPEAT) {
                scheduleTask(task);
            }
        });
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
        taskExecutor.shutdownNow();
    }
}
