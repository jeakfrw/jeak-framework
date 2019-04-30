package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.event.bot.IBotStateEvent;
import de.fearnixx.jeak.reflect.Inject;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.teamspeak.IServer;
import de.fearnixx.jeak.teamspeak.PropertyKeys;
import de.fearnixx.jeak.teamspeak.QueryCommands;
import de.fearnixx.jeak.teamspeak.TargetType;
import de.fearnixx.jeak.teamspeak.query.IQueryRequest;
import de.fearnixx.jeak.teamspeak.query.QueryBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

/**
 * Created by MarkL4YG on 08-Nov-17
 */
public class CommandService implements ICommandService {

    public static final String COMMAND_PREFIX = "!";
    public static final Integer THREAD_POOL_SIZE = 5;
    public static Integer AWAIT_TERMINATION_DELAY = 5000;

    private static final Logger logger = LoggerFactory.getLogger(CommandService.class);

    @Inject
    public IServer server;

    private Map<String, ICommandReceiver> commands;
    private CommandParser parser;
    private boolean terminated = false;
    private final Object lock = new Object();

    private ExecutorService executorSvc;

    public CommandService() {
        commands = new HashMap<>();
        executorSvc = Executors.newFixedThreadPool(Main.getProperty("bot.commandmgr.poolsize", THREAD_POOL_SIZE));
        AWAIT_TERMINATION_DELAY = Main.getProperty("bot.commandmgr.terminatedelay", AWAIT_TERMINATION_DELAY);
        parser = new CommandParser();
    }

    /**
     * The CommandService actually uses given functionality and parses TextMessages starting with "!"
     * (Character is defined at compile-time by a static field)
     *
     * Actual execution is done asynchronously!
     * @param event The event
     */
    @Listener
    public void onTextMessage(IQueryEvent.INotification.ITextMessage event) {
        if (terminated) return;
        String msg = event.getProperty(PropertyKeys.TextMessage.MESSAGE).orElse(null);
        if (msg != null) {
            try {
                Optional<CommandContext> optContext = parser.parseLine(msg + '\n');

                if (optContext.isPresent()) {
                    String command = optContext.get().getCommand();

                    synchronized (lock) {
                        Iterator<Map.Entry<String, ICommandReceiver>> it = commands.entrySet().iterator();
                        final List<ICommandReceiver> receivers = new ArrayList<>();
                        while (it.hasNext()) {
                            Map.Entry<String, ICommandReceiver> entry = it.next();

                            if (command.equals(entry.getKey())) {
                                receivers.add(entry.getValue());
                            }
                        }

                        // Unknown command
                        if (receivers.isEmpty()) {
                            Integer targetID = Integer.valueOf(event.getProperty(PropertyKeys.TextMessage.SOURCE_ID).get());
                            QueryBuilder request = IQueryRequest.builder()
                                                                         .command(QueryCommands.TEXTMESSAGE_SEND)
                                                                         .addKey(PropertyKeys.TextMessage.TARGET_TYPE, TargetType.CLIENT.getQueryNum())
                                                                         .addKey(PropertyKeys.TextMessage.TARGET_ID, targetID)
                                                                         .addKey(PropertyKeys.TextMessage.MESSAGE, "Unknown command!");
                            server.getConnection().sendRequest(request.build());
                            return;
                        }

                        // Execute receivers
                        final CommandContext ctx = optContext.get();
                        ctx.setRawEvent(event);
                        ctx.setTargetType(TargetType.fromQueryNum(Integer.parseInt(event.getProperty(PropertyKeys.TextMessage.TARGET_TYPE).get())));
                        executorSvc.execute(() -> {
                            ICommandReceiver last = null;
                            logger.debug("Executing command receiver");
                            for (int i = receivers.size() - 1; i >= 0; i--) {
                                last = receivers.get(i);
                                try {
                                    last.receive(ctx);
                                }  catch (CommandException ex) {
                                    handleExceptionOn(ctx.getRawEvent(), ex, last);

                                }  catch (Exception thrown) {
                                    logger.error("Uncaught exception while executing command!", thrown);
                                    handleExceptionOn(ctx.getRawEvent(), thrown, last);
                                }
                            }
                        });
                    }
                }

            } catch (CommandException ex) {
                handleExceptionOn(event, ex, null);
            }
        }
    }

    private void handleExceptionOn(IQueryEvent.INotification.ITextMessage textMessage, Throwable exception, ICommandReceiver receiver) {
        logger.warn("Error executing command", (exception instanceof CommandParameterException ? null : exception));
        Integer targetType = Integer.valueOf(textMessage.getProperty(PropertyKeys.TextMessage.TARGET_TYPE).get());
        Integer targetID = Integer.valueOf(textMessage.getProperty(PropertyKeys.TextMessage.SOURCE_ID).get());

        String message = null;
        if (exception instanceof CommandParameterException) {
            CommandParameterException cpe = ((CommandParameterException) exception);
            message = "Rejected parameter!"
                      + "\nParameter name: " + cpe.getParamName()
                      + "\nPassed value: " + cpe.getPassedValue()
                      + "\nMessage: " + cpe.getMessage();
            if (cpe.getCause() != null)
                message += "\nCaused by: " + cpe.getCause().getClass().getSimpleName() + ": " + cpe.getCause().getMessage();
            if (receiver != null)
                message += "\nRejected by: " + receiver.getClass().getName();
        } else {
            StringBuilder msgBuilder = new StringBuilder("There was an error processing your command");

            if(!(exception instanceof CommandException)) {
                msgBuilder.append("!\n");
                msgBuilder.append(exception.getClass().getSimpleName());
            }

            msgBuilder.append(": ").append(exception.getMessage());

            message = msgBuilder.toString();
        }
        if (message.length() > 1024) {
            logger.info("Cropped error feedback!");
            logger.debug(message);
            message = message.substring(0, 1022) + "...";
        }

        QueryBuilder request = IQueryRequest.builder()
                                                     .command(QueryCommands.TEXTMESSAGE_SEND)
                                                     .addKey(PropertyKeys.TextMessage.TARGET_TYPE, TargetType.CLIENT.getQueryNum())
                                                     .addKey(PropertyKeys.TextMessage.TARGET_ID, targetID)
                                                     .addKey(PropertyKeys.TextMessage.MESSAGE, message);
        server.getConnection().sendRequest(request.build());
    }

    @Override
    public void registerCommand(String command, ICommandReceiver receiver) {
        if (receiver == null)
            throw new IllegalArgumentException("CommandReceiver may not be null!");
        synchronized (lock) {
            commands.put(command, receiver);
        }
    }

    @Override
    public void unregisterCommand(String command, ICommandReceiver receiver) {
        synchronized (lock) {
            ICommandReceiver storedReceiver = commands.get(command);
            if (storedReceiver != null && (receiver == null || receiver == storedReceiver)) {
                commands.remove(command);
            }
        }
    }

    @Listener
    public void shutdown(IBotStateEvent.IPreShutdown event) {
        synchronized (lock) {
            terminated = true;
            boolean terminated_successfully = false;
            try {
                executorSvc.shutdown();
                terminated_successfully = executorSvc.awaitTermination(AWAIT_TERMINATION_DELAY, TimeUnit.MILLISECONDS);
            } catch (InterruptedException e) {
                logger.error("Got interrupted while awaiting thread termination!", e);
            }
            if (!terminated_successfully) {
                logger.warn("Some command receivers did not terminate gracefully! Either consider increasing the wait timeout or debug what plugin delays the shutdown!");
                logger.warn("Be aware that the JVM will not exit until ALL threads have terminated!");
            }
            event.addExecutor(executorSvc);
        }
    }
}
