package de.fearnixx.jeak.teamspeak.cache;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.task.ITask;
import de.fearnixx.jeak.service.task.ITaskService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.fearnixx.jeak.teamspeak.except.ConsistencyViolationException;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

public class GenericInfoCache {

    private static final int SERVERINFO_INTERVALL = Main.getProperty("jeak.cache.serverInfoRefresh", 180);
    private static final int INSTANCEINFO_INTERVALL = Main.getProperty("jeak.cache.instanceInfoRefresh", 180);

    private static final Logger logger = LoggerFactory.getLogger(GenericInfoCache.class);

    @Inject
    private IServer server;

    @Inject
    private ITaskService taskService;

    private final ITask serverInfoRefresh = ITask.builder()
            .name("ServerInfo-Refresh")
            .interval(SERVERINFO_INTERVALL, TimeUnit.SECONDS)
            .runnable(this::refreshServerInfo)
            .build();

    private final ITask instanceInfoRefresh = ITask.builder()
            .name("InstanceInfo-Refresh")
            .interval(INSTANCEINFO_INTERVALL, TimeUnit.SECONDS)
            .runnable(this::refreshInstanceInfo)
            .build();

    private IDataHolder lastServerInfo = null;
    private IDataHolder lastInstanceInfo = null;

    private void refreshServerInfo() {
        server.optConnection().ifPresent(conn -> {
            conn.sendRequest(IQueryRequest.builder()
                    .command(QueryCommands.SERVER.SERVER_INFO)
                    .onError(a -> {
                        logger.warn("Failed to refresh server info: {} - {}", a.getErrorCode(), a.getErrorMessage());
                        synchronized (this) {
                            lastServerInfo = null;
                        }
                    })
                    .onSuccess(a -> {
                        synchronized (this) {
                            if (a.getDataChain().size() != 1) {
                                throw new ConsistencyViolationException("ServerInfo returned unfit data: " + a.getDataChain().size());
                            }
                            lastServerInfo = a.getDataChain().get(0);
                        }
                    })
                    .build());
        });
    }

    private void refreshInstanceInfo() {
        server.optConnection().ifPresent(conn -> {
            conn.sendRequest(IQueryRequest.builder()
                    .command(QueryCommands.SERVER.INSTANCE_INFO)
                    .onError(a -> {
                        logger.warn("Failed to refresh instance info: {} - {}", a.getErrorCode(), a.getErrorMessage());
                        synchronized (this) {
                            lastInstanceInfo = null;
                        }
                    })
                    .onSuccess(a -> {
                        synchronized (this) {
                            if (a.getDataChain().size() != 1) {
                                throw new ConsistencyViolationException("InstanceInfo returned unfit data: " + a.getDataChain().size());
                            }
                            lastInstanceInfo = a.getDataChain().get(0);
                        }
                    })
                    .build());
        });
    }

    public synchronized Optional<IDataHolder> getServerInfo() {
        return Optional.ofNullable(lastServerInfo);
    }

    public synchronized Optional<IDataHolder> getInstanceInfo() {
        return Optional.ofNullable(lastInstanceInfo);
    }

    @Listener
    public void onConnected(IBotStateEvent.IConnectStateEvent.IPostConnect event) {
        taskService.runTask(serverInfoRefresh);
        taskService.runTask(instanceInfoRefresh);
    }

    @Listener
    public void onDisconnected(IBotStateEvent.IConnectStateEvent.IDisconnect event) {
        taskService.removeTask(serverInfoRefresh);
        taskService.removeTask(instanceInfoRefresh);
    }
}
