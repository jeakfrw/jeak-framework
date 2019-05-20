package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.task.ITask;
import de.fearnixx.jeak.service.task.ITaskService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PermIdCache {

    private static final Logger logger = LoggerFactory.getLogger(PermIdCache.class);
    private final Map<String, Integer> internalCache = new ConcurrentHashMap<>();

    @Inject
    private ITaskService taskService;

    @Inject
    private IServer server;

    private ITask refreshTask = ITask.builder()
            .name("permid-refresh")
            .interval(1, TimeUnit.HOURS)
            .runnable(this::refreshCache)
            .build();

    @Listener(order = Listener.Orders.SYSTEM)
    public void onPostConnected(IBotStateEvent.IConnectStateEvent.IPostConnect event) {
        taskService.runTask(refreshTask);
    }

    @Listener(order = Listener.Orders.SYSTEM)
    public void onDisconnected(IBotStateEvent.IConnectStateEvent.IDisconnect event) {
        taskService.removeTask(refreshTask);
    }

    private void refreshCache() {
        server.getConnection().sendRequest(
                IQueryRequest.builder()
                        .command("permissionlist")
                        .onError(err -> logger.error("Failed to refresh permission id cache! {} - {}", err.getErrorCode(), err.getErrorMessage()))
                        .onSuccess(this::parseAnswer)
                        .build());
    }

    private synchronized void parseAnswer(IQueryEvent.IAnswer answer) {
        internalCache.clear();
        answer.getDataChain().forEach(perm -> {
            Optional<String> optId = perm.getProperty("permid");
            Optional<String> optName = perm.getProperty("permname");

            if (optId.isPresent() && optName.isPresent()) {
                internalCache.put(optName.get(), Integer.parseInt(optId.get()));
            } else {
                logger.warn("Parse error for perm object: {}", perm.getValues());
            }
        });
    }

    public synchronized Integer getPermIdFor(String permSID) {
        Integer permId = internalCache.getOrDefault(permSID, null);
        if (permId == null) {
            throw new IllegalArgumentException("Unknown permSID: " + permSID);
        }
        return permId;
    }
}
