package de.fearnixx.jeak.service.command;

import de.fearnixx.jeak.Main;
import de.fearnixx.jeak.event.IQueryEvent;
import de.fearnixx.jeak.reflect.FrameworkService;
import de.fearnixx.jeak.reflect.Listener;
import de.fearnixx.jeak.service.command.spec.ICommandSpec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@FrameworkService(serviceInterface = ICommandService.class)
public class TypedCommandService extends CommandService {

    private static final String COMMAND_PREFIX = "!";
    private static final Boolean DISABLE_LEGACY_WARN = Main.getProperty("jeak.commandSvc.disableLegacyWarn", false);
    private static final Integer THREAD_POOL_SIZE = Main.getProperty("jeak.commandSvc.poolSize", 2);
    private static final Integer AWAIT_TERMINATION_DELAY = Main.getProperty("jeak.commandSvc.terminateDelay", 5000);

    private static final Logger logger = LoggerFactory.getLogger(TypedCommandService.class);
    private final Map<String, ICommandSpec> commandSpecs = new ConcurrentHashMap<>();

    @Override
    protected int getThreadPoolSize() {
        return THREAD_POOL_SIZE;
    }

    @Override
    protected int getTerminateDelay() {
        return AWAIT_TERMINATION_DELAY;
    }

    @Override
    @Listener
    public void onTextMessage(IQueryEvent.INotification.ITextMessage event) {
        if (event.getMessage().startsWith(COMMAND_PREFIX)) {
            triggerCommand(event);
        }
    }

    private synchronized void triggerCommand(IQueryEvent.INotification.ITextMessage txtEvent) {
        String msg = txtEvent.getMessage();
        int firstSpace = msg.indexOf(' ');
        String command = msg.substring(COMMAND_PREFIX.length(), firstSpace);
        String arguments = msg.substring(firstSpace, msg.length()).trim();

        if (commandSpecs.containsKey(command)) {
            dispatchTyped(txtEvent, arguments, commandSpecs.get(command));

        } else if (getLegacyReceivers().containsKey(command)) {
            if (!DISABLE_LEGACY_WARN) {
                logger.warn("Command \"{}\" is only implemented using legacy command receivers. " +
                        "These will ony continue to work in Jeak version 1.X", command);
            }
            super.onTextMessage(txtEvent);
        }
    }

    private void dispatchTyped(IQueryEvent.INotification.ITextMessage txtEvent, String arguments, ICommandSpec iCommandSpec) {

    }
}
