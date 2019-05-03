package de.fearnixx.jeak.task;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.event.IEventService;
import de.fearnixx.jeak.service.task.ITask;
import de.fearnixx.jeak.service.task.ITaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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

    private static final Logger logger = LoggerFactory.getLogger(TaskService.class);

    public static final Integer THREAD_POOL_SIZE = 10;
    public static Integer AWAIT_TERMINATION_DELAY = 5000;

    private final Map<ITask, Long> tasks;
    private boolean terminated = false;

    private ThreadFactory threadFactory = new ThreadFactoryBuilder().setNameFormat("task-scheduler-%d").build();
    private ExecutorService taskExecutor;

    @Inject
    private IEventService eventService;

    public TaskService(int capacity) {
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
            logger.warn("Some plugin tried to register a task missing a proper name. Task not registered");
            return;
        }
        synchronized (tasks) {
            if (tasks.containsKey(task))
                return;
            tasks.put(task, delay);
        }
        logger.debug("Task {} scheduled for {} seconds", task.getName(), delay);
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
        logger.debug("Running task {}", task.getName());
        final Runnable r = task.getRunnable();
        taskExecutor.execute(() -> {
            try {
                r.run();
            } catch (Exception e) {
                logger.error("Uncaught exception from task: {}", task.getName(), e);
            }
            if (task.getType() == ITask.TaskType.REPEAT && task.shouldReschedule()) {
                scheduleTask(task);
            }
        });
    }

    @Override
    public void run() {
        eventService.registerListener(this);
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
                                logger.debug("{} has {} seconds left", t.getName(), l);
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

    @Listener
    public void onPreShutdown(IBotStateEvent.IPreShutdown event) {
        synchronized (tasks) {
            logger.debug("{} task(s) scheduled upon shutdown.", tasks.size());
            tasks.clear();
            terminated = true;
            taskExecutor.shutdown();
            event.addExecutor(taskExecutor);
        }
    }
}
