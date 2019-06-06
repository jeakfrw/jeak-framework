package de.fearnixx.jeak.service.permission.teamspeak;

import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.task.ITask;
import de.fearnixx.jeak.service.task.ITaskService;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.data.IDataHolder;
import de.fearnixx.jeak.teamspeak.query.BlockingRequest;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.Objects;
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
                        .onError(err -> {
                            logger.error("Failed to refresh permission id cache! {} - {}", err.getErrorCode(), err.getErrorMessage());
                            logger.warn("Please grant \"b_serverinstance_permission_list\" to resolve this issue!");
                        })
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
        Objects.requireNonNull(permSID, "PermSID may not be null!");
        if (permSID.trim().isEmpty()) {
            throw new IllegalArgumentException("PermSID may not be empty!");
        }

        Integer permId = internalCache.getOrDefault(permSID, null);
        if (permId == null) {
            logger.info("Cache-miss for \"{}\". Attempting lazy retrieval.", permSID);
            Optional<Integer> optPermId = lazilyGetPermId(permSID);

             permId = optPermId.orElseThrow(() -> new IllegalArgumentException("Unknown permSID: " + permSID));
             logger.debug("Updating cache: {} -> {}", permSID, permId);
             internalCache.put(permSID, permId);
        }
        return permId;
    }

    private Optional<Integer> lazilyGetPermId(String permSid) {
        IQueryRequest request = IQueryRequest.builder()
                .command("permidgetbyname")
                .addKey("permsid", permSid)
                .build();

        BlockingRequest bRequest = new BlockingRequest(request);
        if (bRequest.waitForCompletion()) {
            IQueryEvent.IAnswer answer = bRequest.getAnswer();

            if (answer.getErrorCode() == 0) {
                List<IDataHolder> chain = answer.getDataChain();
                for (IDataHolder holder : chain) {
                    String dataSID = holder.getProperty("permsid").orElse("_");
                    if (dataSID.equals(permSid)) {
                        Integer permid = holder.getProperty("permid")
                                .map(Integer::parseInt)
                                .orElse(-1);

                        if (permid > 0) {
                            return Optional.of(permid);
                        } else {
                            logger.warn("Failed to retrieve permID from answer!");
                        }
                    } else {
                        logger.debug("Skipping result SID: {}", dataSID);
                    }
                }
            } else {
                logger.warn("Non-OK return code for permID lookup: {} - {}", answer.getErrorCode(), answer.getErrorMessage());
            }
        } else {
            logger.warn("Could not complete blocking request for permID lookup.");
        }

        return Optional.empty();
    }
}
