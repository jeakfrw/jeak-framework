package de.fearnixx.jeak.commandline;

import de.fearnixx.jeak.antlr.CommandParserUtil;
import de.fearnixx.jeak.service.command.CommandInfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.OutputStream;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import static de.fearnixx.jeak.antlr.CommandParserUtil.parseCommandLine;

public class CLIService {

    private static final Object INSTANCE_LOCK = new Object();
    private static final Logger logger = LoggerFactory.getLogger(CLIService.getInstance().getClass());
    public static CLIService instance;

    public synchronized static CLIService getInstance() {
        synchronized (INSTANCE_LOCK) {
            if (instance == null) {
                instance = new CLIService();
            }
            return instance;
        }
    }

    /**
     * In case the framework is not started via its shipped main class, calling applications may override
     * the cli-command service instance so they can properly receive the command registrations.
     * @implNote This <em>MUST</em> be called before any interaction with the actual framework as the instance probably will be initialized by then.
     */
    protected static void setInstance(CLIService service) {
        synchronized (INSTANCE_LOCK) {
            if (instance != null) {
                throw new IllegalStateException("Replacement CLI-Services MUST be set before the first #getInstance call!");
            }
            instance = service;
        }
    }

    protected final Map<String, Consumer<CLICommandContext>> cliCommands = new ConcurrentHashMap<>();

    protected CLIService() {
    }

    protected Consumer<String> getMessageConsumer() {
        return System.out::println;
    }

    public Optional<Consumer<CLICommandContext>> registerCommand(String command, Consumer<CLICommandContext> commandConsumer) {
        return Optional.ofNullable(cliCommands.put(command, commandConsumer));
    }

    public void receiveLine(String input) {
        int spacePos = input.indexOf(' ');
        String command;
        String contextPart = null;
        if (spacePos < 0) {
            command = input;
        } else {
            command = input.substring(0, spacePos);
            contextPart = input.substring(spacePos).trim();
        }

        Consumer<CLICommandContext> cliConsumer = cliCommands.getOrDefault(command, null);
        if (cliConsumer != null) {
            CommandInfo commandInfo = parseCommandLine(contextPart, logger);
            CLICommandContext cliContext = new CLICommandContext(commandInfo, getMessageConsumer());
            cliConsumer.accept(cliContext);

        } else {
            final String unknownMsg = String.format("Unknown command: \"%s\"", command);
            logger.info(unknownMsg);
            getMessageConsumer().accept(unknownMsg);
        }
    }
}
